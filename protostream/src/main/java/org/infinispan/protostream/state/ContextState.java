package org.infinispan.protostream.state;

import static org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ContextState {
   private SerializationContext ctx;

   @Setup
   public void setup() {
      ctx = ProtobufUtil.newSerializationContext();
      INSTANCE.registerSchema(ctx);
      INSTANCE.registerMarshallers(ctx);
   }

   public SerializationContext getCtx() {
      return ctx;
   }
}
