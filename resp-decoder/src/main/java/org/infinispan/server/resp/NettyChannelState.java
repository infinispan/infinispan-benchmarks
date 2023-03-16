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
      CURRENT,
      NEW
   }

   @Param
   public DECODER decoder;

   @Setup
   public void initializeState() {

      channel = new EmbeddedChannel();
      channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

      switch (decoder) {
         case NEW:
            channel.pipeline()
                  .addLast(new NewDecoder(new NewRespHandler()));
            break;
         case CURRENT:
            channel.pipeline()
                  .addLast(new RespDecoder(new OurRespHandler()));
            break;
      }
   }
}
