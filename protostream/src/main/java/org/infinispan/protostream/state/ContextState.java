package org.infinispan.protostream.state;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.impl.RandomAccessOutputStreamImpl;
import org.infinispan.protostream.types.java.CommonTypesSchema;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import static org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

@State(Scope.Benchmark)
public class ContextState {
   private SerializationContext ctx;
   private RandomAccessOutputStreamImpl os;

   @Setup
   public void setup() {
      ctx = ProtobufUtil.newSerializationContext();
      register(INSTANCE);
      register(new CommonTypesSchema());
      os = new RandomAccessOutputStreamImpl();
   }

   private void register(SerializationContextInitializer serializationContextInitializer) {
      serializationContextInitializer.registerSchema(ctx);
      serializationContextInitializer.registerMarshallers(ctx);

      os = new RandomAccessOutputStreamImpl();
   }

   public RandomAccessOutputStreamImpl getOutputStream() {
      os.setPosition(0);
      return os;
   }

   public SerializationContext getCtx() {
      return ctx;
   }
}
