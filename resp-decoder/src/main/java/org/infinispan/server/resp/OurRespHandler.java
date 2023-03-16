package org.infinispan.server.resp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.util.function.TriConsumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

public class OurRespHandler extends RespRequestHandler {
   private static final CompletableFuture<byte[]> GET_FUTURE = CompletableFuture.completedFuture(new byte[] { 0x1, 0x12});
   public static ByteBuf OK = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("+OK\r\n", CharsetUtil.US_ASCII));

   // Returns a cached OK status that is retained for multiple uses
   static ByteBuf statusOK() {
      return OK.duplicate();
   }

   @Override
   public CompletionStage<RespRequestHandler> handleRequest(ChannelHandlerContext ctx, String type, List<byte[]> arguments) {
      switch (type) {
         case "GET":
            return stageToReturn(GET_FUTURE, ctx, GET_TRICONSUMER);
         case "SET":
            return stageToReturn(CompletableFutures.completedNull(), ctx, SET_TRICONSUMER);
      }
      return super.handleRequest(ctx, type, arguments);
   }

   private static final TriConsumer<byte[], ChannelHandlerContext, Throwable> SET_TRICONSUMER = (ignore, innerCtx, t) -> {
      if (t != null) {
         throw new AssertionError(t);
      } else {
         innerCtx.writeAndFlush(statusOK(), innerCtx.voidPromise());
      }
   };

   private static final TriConsumer<byte[], ChannelHandlerContext, Throwable> GET_TRICONSUMER = (innerValueBytes, innerCtx, t) -> {
      if (t != null) {
         throw new AssertionError(t);
      } else if (innerValueBytes != null) {
         ByteBuf buf = bytesToResult(innerValueBytes, innerCtx.alloc());
         innerCtx.writeAndFlush(buf, innerCtx.voidPromise());
      } else {
         innerCtx.writeAndFlush(RespRequestHandler.stringToByteBuf("$-1\r\n", innerCtx.alloc()), innerCtx.voidPromise());
      }
   };
}
