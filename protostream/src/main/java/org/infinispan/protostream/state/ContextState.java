package org.infinispan.protostream.state;

import static org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

import java.io.OutputStream;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.RandomAccessOutputStream;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.impl.RandomAccessOutputStreamImpl;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ContextState {
   private SerializationContext ctx;
   private RandomAccessOutputStreamImpl os;

   @Setup
   public void setup() {
      ctx = ProtobufUtil.newSerializationContext();
      INSTANCE.registerSchema(ctx);
      INSTANCE.registerMarshallers(ctx);

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
