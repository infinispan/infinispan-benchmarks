package org.infinispan.server.resp;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;

@State(Scope.Benchmark)
public class NettyChannelState {
   public enum DECODER {
      LETTUCE,
      GENERATED
   }

   @Param
   public GetSetState.DECODER decoderToUse;

   public EmbeddedChannel channel;

   @Setup
   public void initializeState() {

      channel = new EmbeddedChannel();
      channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

      OurRespHandler ourRespHandler = new OurRespHandler();

      switch (decoderToUse) {
         case LETTUCE:
            channel.pipeline()
                  .addLast(new RespLettuceHandler(ourRespHandler));
            break;
         case GENERATED:
            channel.pipeline()
                  .addLast(new RespDecoder(ourRespHandler));
            break;
      }
   }
}
