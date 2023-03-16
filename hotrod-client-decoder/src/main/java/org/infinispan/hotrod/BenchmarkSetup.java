package org.infinispan.hotrod;

import static org.infinispan.hotrod.Constants.CACHE_NAME;
import static org.infinispan.hotrod.Constants.DEFAULT_CODEC;
import static org.infinispan.hotrod.Constants.VALUE;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import org.infinispan.commons.marshall.StringMarshaller;
import org.infinispan.hotrod.configuration.HotRodConfiguration;
import org.infinispan.hotrod.configuration.HotRodConfigurationBuilder;
import org.infinispan.hotrod.impl.cache.Utils;
import org.infinispan.hotrod.impl.operations.HotRodOperation;
import org.infinispan.hotrod.impl.operations.OperationContext;
import org.infinispan.hotrod.impl.transport.handler.CacheRequestProcessor;
import org.infinispan.hotrod.impl.transport.netty.ChannelFactory;
import org.infinispan.hotrod.impl.transport.netty.HeaderDecoder;
import org.infinispan.hotrod.impl.transport.netty.HotRodClientDecoder;
import org.infinispan.hotrod.operation.AbstractOperation;
import org.infinispan.hotrod.operation.TestGetOperation;
import org.infinispan.hotrod.operation.TestPutOperation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;

@State(Scope.Benchmark)
public class BenchmarkSetup {
   public enum Decoder {
      MANUAL,
      GENERATED;
   }

   public enum Operation {
      GET,
      PUT
   }

   @Param({"MANUAL", "GENERATED"})
   public Decoder decoder;

   @Param({"GET", "PUT"})
   public Operation operation;
   public EmbeddedChannel channel;
   private HeaderDecoder dec;

   private ChannelFactory channelFactory;
   private AbstractOperation op;

   @Setup
   public void initializeState() {
      channel = new EmbeddedChannel();
      channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
      HotRodConfigurationBuilder builder = new HotRodConfigurationBuilder();
      builder.servers().clear();
      HotRodConfiguration configuration = builder.build(false);
      channelFactory = new ChannelFactory();
      channelFactory.start(DEFAULT_CODEC, configuration, new StringMarshaller(StandardCharsets.UTF_8),
            Executors.newSingleThreadExecutor(), null, null);
      OperationContext ctx = new OperationContext(channelFactory, DEFAULT_CODEC, null, configuration, Utils.CLIENT_STATISTICS, null, CACHE_NAME);

      switch (decoder) {
         case MANUAL:
            dec = new HeaderDecoder(ctx) {
               @Override
               public boolean isHandlingMessage() {
                  return true;
               }
            };
            channel.pipeline().addLast(HeaderDecoder.NAME, dec);
            break;
         case GENERATED:
            CacheRequestProcessor handler = new CacheRequestProcessor(channelFactory, configuration);
            dec = new HeaderDecoder(ctx) {
               @Override
               public boolean isHandlingMessage() {
                  if (super.isHandlingMessage()) throw new IllegalStateException("Should not have a delegate message!");
                  return false;
               }
            };
            channel.pipeline().addLast(HeaderDecoder.NAME, new HotRodClientDecoder(dec, handler));
            break;
      }

      switch (operation) {
         case GET:
            op = new TestGetOperation(ctx);
            break;
         case PUT:
            op = new TestPutOperation(ctx);
            break;
      }

      op.setup();
   }

   @TearDown
   public void teardown() {
      channel.checkException();
      channel.close();
      channelFactory.destroy();

      HotRodOperation<String> hro = op.getOperation();

      if (!hro.isDone())
         throw new IllegalStateException("Operation never completed!");

      if (!op.isResultCorrect(VALUE))
         throw new IllegalStateException("Operation completed unexpected value: " + hro.join());
   }

   public ByteBuf prepareGetOperation() {
      dec.registerOperation(channel, op.getOperation());
      return op.resetAndGetResponse();
   }
}
