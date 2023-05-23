package org.infinispan.server.resp;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;

@State(Scope.Benchmark)
public class NettyChannelState {
   public EmbeddedChannel channel;

   public enum DECODER {
      CURRENT
   }

   @Param
   public DECODER decoder;

   @Setup
   public void initializeState() {

      channel = new EmbeddedChannel();
      channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

      switch (decoder) {
         case CURRENT:
            RespDecoder d = new RespDecoder();
            channel.pipeline()
                  .addLast(d, new RespHandler(d, new OurRespHandler()));
            break;
      }

      // Ensure the ByteBufPool is initialized
      channel.pipeline().fireChannelRegistered();
   }
}
