package org.infinispan.protostream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.infinispan.protostream.impl.ByteArrayOutputStreamEx;
import org.infinispan.protostream.state.AddressState;
import org.infinispan.protostream.state.ContextState;
import org.infinispan.protostream.state.MetadataState;
import org.infinispan.protostream.state.UserState;
import org.infinispan.protostream.userclasses.Address;
import org.infinispan.protostream.userclasses.IracMetadata;
import org.infinispan.protostream.userclasses.User;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 6, time = 10)
@Fork(1)
@State(Scope.Benchmark)
public class ProtostreamBenchmark {

   @Param({"true", "false"})
   boolean byteArrayOrStream;

   @Benchmark
   public Object testMarshallAddress(ContextState contextState, AddressState addressState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      Address address = addressState.getAddress();
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, address);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(addressState.getAddressBytes().length);
         ProtobufUtil.toWrappedStream(ctx, os, address);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Object testMarshallUser(ContextState contextState, UserState userState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      User user = userState.getUser();
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, user);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(userState.getUserBytes().length);
         ProtobufUtil.toWrappedStream(ctx, os, user);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Object testMarshallIracMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      IracMetadata metadata = metadataState.getMetadata();
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, metadata);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(metadataState.getMetadataBytes().length);
         ProtobufUtil.toWrappedStream(ctx, os, metadata);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Address testUnmarshallAddress(ContextState contextState, AddressState addressState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] addressBytes = addressState.getAddressBytes();
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, addressBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(addressBytes));
   }

   @Benchmark
   public User testUnmarshallUser(ContextState contextState, UserState userState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] userBytes = userState.getUserBytes();
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, userBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(userBytes));
   }

   @Benchmark
   public IracMetadata testUnmarshallMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] metadataBytes = metadataState.getMetadataBytes();
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, metadataBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(metadataBytes));
   }
}
