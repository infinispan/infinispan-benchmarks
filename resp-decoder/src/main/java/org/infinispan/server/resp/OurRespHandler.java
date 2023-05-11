package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.infinispan.commons.util.concurrent.CompletableFutures;

import io.netty.channel.ChannelHandlerContext;

public class OurRespHandler extends RespRequestHandler {
   private static final CompletableFuture<byte[]> GET_FUTURE = CompletableFuture.completedFuture(new byte[] { 0x1, 0x12});
   @Override
   protected CompletionStage<RespRequestHandler> actualHandleRequest(ChannelHandlerContext ctx, RespCommand command,
                                                                     List<byte[]> arguments) {
      if (command.getName().equals("GET")) {
         return stageToReturn(GET_FUTURE, ctx, Consumers.GET_BICONSUMER);
      } else if (command.getName().equals("SET")) {
         return stageToReturn(CompletableFutures.completedNull(), ctx, Consumers.OK_BICONSUMER);
      }
      return super.actualHandleRequest(ctx, command, arguments);
   }
}
