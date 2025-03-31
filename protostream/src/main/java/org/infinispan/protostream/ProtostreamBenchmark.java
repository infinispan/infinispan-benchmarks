package org.infinispan.protostream;

import org.infinispan.protostream.impl.RandomAccessOutputStreamImpl;
import org.infinispan.protostream.impl.TagWriterImpl;
import org.infinispan.protostream.state.AddressState;
import org.infinispan.protostream.state.ContextState;
import org.infinispan.protostream.state.MetadataState;
import org.infinispan.protostream.state.UUIDState;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
   public Object testSizedAddress(ContextState contextState, AddressState addressState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      Address address = addressState.getAddress();
      TagWriterImpl sizedWriter = TagWriterImpl.newInstance(ctx);
      WrappedMessage.write(ctx, sizedWriter, address);
      return sizedWriter;
   }

   @Benchmark
   public Object testMarshallAddress(ContextState contextState, AddressState addressState) throws IOException {
      RandomAccessOutputStreamImpl raos = contextState.getOutputStream();
      SerializationContext ctx = contextState.getCtx();
      Address address = addressState.getAddress();
      if (byteArrayOrStream) {
         raos.write(ProtobufUtil.toWrappedByteArray(ctx, address));
      } else {
         ProtobufUtil.toWrappedStream(ctx, (RandomAccessOutputStream) raos, address);
      }
      return raos;
   }

   @Benchmark
   public Object testSizedUser(ContextState contextState, UserState userState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      User user = userState.getUser();
      TagWriterImpl sizedWriter = TagWriterImpl.newInstance(ctx);
      WrappedMessage.write(ctx, sizedWriter, user);
      return sizedWriter;
   }

   @Benchmark
   public Object testMarshallUser(ContextState contextState, UserState userState) throws IOException {
      RandomAccessOutputStreamImpl raos = contextState.getOutputStream();
      SerializationContext ctx = contextState.getCtx();
      User user = userState.getUser();
      if (byteArrayOrStream) {
         raos.write(ProtobufUtil.toWrappedByteArray(ctx, user));
      } else {
         ProtobufUtil.toWrappedStream(ctx, (RandomAccessOutputStream) raos, user);
      }
      return raos;
   }

   @Benchmark
   public Object testSizedIracMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      IracMetadata metadata = metadataState.getMetadata();
      TagWriterImpl sizedWriter = TagWriterImpl.newInstance(ctx);
      WrappedMessage.write(ctx, sizedWriter, metadata);
      return sizedWriter;
   }

   @Benchmark
   public Object testMarshallIracMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      RandomAccessOutputStreamImpl raos = contextState.getOutputStream();
      SerializationContext ctx = contextState.getCtx();
      IracMetadata metadata = metadataState.getMetadata();
      if (byteArrayOrStream) {
         raos.write(ProtobufUtil.toWrappedByteArray(ctx, metadata));
      } else {
         ProtobufUtil.toWrappedStream(ctx, (RandomAccessOutputStream) raos, metadata);
      }
      return raos;
   }

   @Benchmark
   public Object testSizedUUID(ContextState contextState, UUIDState uuidState) throws IOException {
      var ctx = contextState.getCtx();
      var uuid = uuidState.getUuid();
      var sizedWriter = TagWriterImpl.newInstance(ctx);
      WrappedMessage.write(ctx, sizedWriter, uuid);
      return sizedWriter;
   }

   @Benchmark
   public Object testMarshallUUID(ContextState contextState, UUIDState uuidState) throws IOException {
      var raos = contextState.getOutputStream();
      var ctx = contextState.getCtx();
      var data = uuidState.getUuid();
      if (byteArrayOrStream) {
         raos.write(ProtobufUtil.toWrappedByteArray(ctx, data));
      } else {
         ProtobufUtil.toWrappedStream(ctx, (RandomAccessOutputStream) raos, data);
      }
      return raos;
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

   @Benchmark
   public UUID testUnmarshallUUID(ContextState contextState, UUIDState uuidState) throws IOException {
      var ctx = contextState.getCtx();
      var bytes = uuidState.getUuidBytes();
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, bytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(bytes));
   }
}
