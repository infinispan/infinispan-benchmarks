package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.util.function.TriConsumer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class OurRespHandler extends RespRequestHandler {
   private static final CompletableFuture<byte[]> GET_FUTURE = CompletableFuture.completedFuture(new byte[] { 0x1, 0x12});
   public static byte[] OK = "+OK\r\n".getBytes(StandardCharsets.US_ASCII);

   @Override
   public CompletionStage<RespRequestHandler> actualHandleRequest(ChannelHandlerContext ctx, String type, List<byte[]> arguments) {
      switch (type) {
         case "GET":
            return stageToReturn(GET_FUTURE, ctx, GET_TRICONSUMER);
         case "SET":
            return stageToReturn(CompletableFutures.completedNull(), ctx, OK_BICONSUMER);
      }
      return super.handleRequest(ctx, type, arguments);
   }

   protected static final BiConsumer<Object, ByteBufPool> OK_BICONSUMER = (ignore, alloc) ->
         alloc.acquire(OK.length).writeBytes(OK);

   protected static final BiConsumer<byte[], ByteBufPool> GET_TRICONSUMER = (innerValueBytes, alloc) -> {
      if (innerValueBytes != null) {
         bytesToResult(innerValueBytes, alloc);
      } else {
         stringToByteBuf("$-1\r\n", alloc);
      }
   };
}
