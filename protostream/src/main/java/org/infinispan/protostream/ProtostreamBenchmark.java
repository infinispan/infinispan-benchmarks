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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 6, time = 10)
@Fork(1)
@State(Scope.Benchmark)
public class ProtostreamBenchmark {

   @Param
   SerializationType serializationType;

   @Benchmark
   public Object testMarshallAddress(ContextState contextState, AddressState addressState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      Address address = addressState.getAddress();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.toWrappedByteArray(ctx, address);
         case INPUT_STREAM -> {
            ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(addressState.getAddressBytes().length);
            ProtobufUtil.toWrappedStream(ctx, os, address);
            yield os.getByteBuffer();
         }
         case BYTE_BUF -> {
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(addressState.getAddressBytes().length);
            ProtobufUtil.toWrappedEncoder(ctx, new ByteBufEncoder(buf), address);
            yield buf.release();
         }
      };
   }

   @Benchmark
   public Object testMarshallUser(ContextState contextState, UserState userState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      User user = userState.getUser();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.toWrappedByteArray(ctx, user);
         case INPUT_STREAM -> {
            ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(userState.getUserBytes().length);
            ProtobufUtil.toWrappedStream(ctx, os, user);
            yield os.getByteBuffer();
         }
         case BYTE_BUF -> {
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(userState.getUserBytes().length);
            ProtobufUtil.toWrappedEncoder(ctx, new ByteBufEncoder(buf), user);
            yield buf.release();
         }
      };
   }

   @Benchmark
   public Object testMarshallIracMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      IracMetadata metadata = metadataState.getMetadata();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.toWrappedByteArray(ctx, metadata);
         case INPUT_STREAM -> {
            ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(metadataState.getMetadataBytes().length);
            ProtobufUtil.toWrappedStream(ctx, os, metadata);
            yield os.getByteBuffer();
         }
         case BYTE_BUF -> {
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(metadataState.getMetadataBytes().length);
            ProtobufUtil.toWrappedEncoder(ctx, new ByteBufEncoder(buf), metadata);
            yield buf.release();
         }
      };
   }

   @Benchmark
   public Address testUnmarshallAddress(ContextState contextState, AddressState addressState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] addressBytes = addressState.getAddressBytes();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.fromWrappedByteArray(ctx, addressBytes);
         case INPUT_STREAM -> ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(addressBytes));
         case BYTE_BUF -> ProtobufUtil.fromWrappedDecoder(ctx, new ByteBufDecoder(addressState.getAddressByteBuf()));
      };
   }

   @Benchmark
   public User testUnmarshallUser(ContextState contextState, UserState userState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] userBytes = userState.getUserBytes();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.fromWrappedByteArray(ctx, userBytes);
         case INPUT_STREAM -> ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(userBytes));
         case BYTE_BUF -> ProtobufUtil.fromWrappedDecoder(ctx, new ByteBufDecoder(userState.getUserByteBuf()));
      };
   }

   @Benchmark
   public IracMetadata testUnmarshallMetadata(ContextState contextState, MetadataState metadataState) throws IOException {
      SerializationContext ctx = contextState.getCtx();
      byte[] metadataBytes = metadataState.getMetadataBytes();
      return switch (serializationType) {
         case ARRAY -> ProtobufUtil.fromWrappedByteArray(ctx, metadataBytes);
         case INPUT_STREAM -> ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(metadataBytes));
         case BYTE_BUF -> ProtobufUtil.fromWrappedDecoder(ctx, new ByteBufDecoder(metadataState.getMetadataByteBuf()));
      };
   }
}
