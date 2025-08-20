package org.infinispan.jmhbenchmarks.state;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.types.java.CommonTypesSchema;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import static org.infinispan.jmhbenchmarks.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

@State(Scope.Benchmark)
public class ContextState {
   private SerializationContext ctx;

   @Setup
   public void setup() {
      ctx = ProtobufUtil.newSerializationContext();
      register(INSTANCE);
      register(new CommonTypesSchema());
   }

   private void register(SerializationContextInitializer serializationContextInitializer) {
      serializationContextInitializer.registerSchema(ctx);
      serializationContextInitializer.registerMarshallers(ctx);
   }

   public SerializationContext getCtx() {
      return ctx;
   }
}
