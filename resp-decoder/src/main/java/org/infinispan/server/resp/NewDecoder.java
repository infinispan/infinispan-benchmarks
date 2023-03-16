package org.infinispan.server.resp;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.infinispan.commons.util.Util;
import org.infinispan.util.concurrent.CompletionStages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.FastThreadLocal;

public class NewDecoder extends RespDecoder {
   public NewDecoder(NewRespHandler initialHandler) {
      super(initialHandler);
      initialHandler.writer = this::flushBuffer;
      initialHandler.allocator = this::retrieveBuffer;
   }

   protected static final int THREAD_LOCAL_CAPACITY = 1024;

   private ChannelHandlerContext ctx;

   private boolean isReading;
   private ByteBuf pendingBuffer;

   private static final FastThreadLocal<ByteBuf> BYTE_BUF_FAST_THREAD_LOCAL = new FastThreadLocal<>() {
      @Override
      protected ByteBuf initialValue() {
         return Unpooled.directBuffer(THREAD_LOCAL_CAPACITY, THREAD_LOCAL_CAPACITY);
      }

      @Override
      protected void onRemoval(ByteBuf value) {
         value.release();
      }
   };

   protected void flushBuffer(ByteBuf buffer) {
      assert buffer == pendingBuffer : "Buffer mismatch expected " + pendingBuffer + " but was " + buffer;
      // Only flush the buffer it is done outside of the read loop, as we handle that ourselves
      if (!isReading) {
         ctx.writeAndFlush(pendingBuffer, ctx.voidPromise());
         pendingBuffer = null;
      }
   }

   private void flushPendingBuffer() {
      if (pendingBuffer != null) {
         ctx.writeAndFlush(pendingBuffer, ctx.voidPromise());
         pendingBuffer = null;
      }
   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      flushPendingBuffer();
      super.channelInactive(ctx);
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      this.ctx = ctx;
      super.handlerAdded(ctx);
   }

   protected ByteBuf retrieveBuffer(int requiredBytes) {
      if (pendingBuffer != null) {
         if (requiredBytes < pendingBuffer.writableBytes()) {
            return pendingBuffer;
         }
         ctx.write(pendingBuffer, ctx.voidPromise());
         pendingBuffer = null;
      }
      ByteBuf buf = BYTE_BUF_FAST_THREAD_LOCAL.get();
      if (requiredBytes < buf.writableBytes()) {
         // This will reserve the buffer for our usage only, other channels in same event loop may try to reserve
         if (buf.refCnt() == 1) {
            buf.retain();
            buf.clear();
            pendingBuffer = buf;
            return buf;
         }
      }
      int reserveSize = Math.max(requiredBytes, 4096);
      pendingBuffer = ctx.alloc().buffer(reserveSize, reserveSize);
      return pendingBuffer;
   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      isReading = false;
      flushPendingBuffer();
      super.channelReadComplete(ctx);
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      isReading = true;
      super.channelRead(ctx, msg);
   }

   @Override
   protected boolean handleCommandAndArguments(ChannelHandlerContext ctx, String command, List<byte[]> arguments) {
      boolean canContinue = handleCommandAndArgumentsOverride(ctx, command, arguments);
      if (canContinue) {
         if (ctx.channel().bytesBeforeUnwritable() < pendingBuffer.readableBytes()) {
            ctx.writeAndFlush(pendingBuffer);
            pendingBuffer = null;
            // TODO: we should probably check for writeability still and block reading if possible until is cleared
         }
      } else {
         assert !ctx.channel().config().isAutoRead();
      }
      return canContinue;
   }

   protected boolean handleCommandAndArgumentsOverride(ChannelHandlerContext ctx, String command, List<byte[]> arguments) {
      if (log.isTraceEnabled()) {
         log.tracef("Received command: %s with arguments %s for %s", command, Util.toStr(arguments), ctx.channel());
      }

      CompletionStage<RespRequestHandler> stage = requestHandler.handleRequest(ctx, command, arguments);
      if (CompletionStages.isCompletedSuccessfully(stage)) {
         requestHandler = CompletionStages.join(stage);
         return true;
      }
      log.tracef("Disabling auto read for channel %s until previous command is complete", ctx.channel());
      // Disable reading any more from socket - until command is complete
      ctx.channel().config().setAutoRead(false);
      stage.whenComplete((handler, t) -> {
         assert ctx.channel().eventLoop().inEventLoop();
         log.tracef("Re-enabling auto read for channel %s as previous command is complete", ctx.channel());
         ctx.channel().config().setAutoRead(true);
         if (t != null) {
            exceptionCaught(ctx, t);
         } else {
            // Instate the new handler if there was no exception
            requestHandler = handler;
         }

         ctx.read();
      });
      return false;
   }
}
