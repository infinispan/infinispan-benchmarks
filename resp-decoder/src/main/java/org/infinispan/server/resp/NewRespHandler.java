package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.util.function.TriConsumer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class NewRespHandler extends RespRequestHandler {
   private static final CompletableFuture<byte[]> GET_FUTURE = CompletableFuture.completedFuture(new byte[] { 0x1, 0x12});
   public static byte[] OK = "+OK\r\n".getBytes(StandardCharsets.US_ASCII);

   public IntFunction<ByteBuf> allocator;
   public Consumer<ByteBuf> writer;

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

   private final TriConsumer<byte[], ChannelHandlerContext, Throwable> SET_TRICONSUMER = (ignore, innerCtx, t) -> {
      if (t != null) {
         throw new AssertionError(t);
      } else {
         ByteBuf buf = allocator.apply(OK.length);
         writer.accept(buf.writeBytes(OK));
      }
   };

   private final TriConsumer<byte[], ChannelHandlerContext, Throwable> GET_TRICONSUMER = (innerValueBytes, innerCtx, t) -> {
      if (t != null) {
         throw new AssertionError(t);
      } else if (innerValueBytes != null) {
         ByteBuf buf = bytesToResult(innerValueBytes, allocator);
         writer.accept(buf);
      } else {
         innerCtx.writeAndFlush(RespRequestHandler.stringToByteBuf("$-1\r\n", innerCtx.alloc()), innerCtx.voidPromise());
      }
   };

   protected static ByteBuf bytesToResult(byte[] result, IntFunction<ByteBuf> allocator) {
      int length = result.length;
      int stringLength = stringSize(length);

      // Need 5 extra for $ and 2 sets of /r/n
      int exactSize = stringLength + length + 5;
      ByteBuf buffer = allocator.apply(exactSize);
      buffer.writeByte('$');
      // This method is anywhere from 10-100% faster than ByteBufUtil.writeAscii and avoids allocations
      setIntChars(length, stringLength, buffer);
      buffer.writeByte('\r').writeByte('\n');
      buffer.writeBytes(result);
      buffer.writeByte('\r').writeByte('\n');

      return buffer;
   }
}
