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

   @Setup
   public void initializeState() {

      channel = new EmbeddedChannel();
      channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

      OurRespHandler ourRespHandler = new OurRespHandler();

      channel.pipeline()
            .addLast(new RespDecoder(ourRespHandler));
   }
}
