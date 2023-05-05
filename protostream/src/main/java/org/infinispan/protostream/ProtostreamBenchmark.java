package org.infinispan.protostream;

import static org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.infinispan.protostream.impl.ByteArrayOutputStreamEx;
import org.infinispan.protostream.userclasses.Address;
import org.infinispan.protostream.userclasses.IracEntryVersion;
import org.infinispan.protostream.userclasses.IracMetadata;
import org.infinispan.protostream.userclasses.TopologyIracVersion;
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

   private final SerializationContext ctx;
   private final Address address;
   private final User user;
   private final IracMetadata metadata;

   byte[] addressBytes;
   byte[] userBytes;
   byte[] metadataBytes;

   @Param({"true", "false"})
   boolean byteArrayOrStream;

   public ProtostreamBenchmark() {
      ctx = ProtobufUtil.newSerializationContext();
      INSTANCE.registerSchema(ctx);
      INSTANCE.registerMarshallers(ctx);
      address = new Address("That street", "1234-567", 1024, true);
      Set<Integer> accounts = new HashSet<>();
      IntStream.range(10, 20).forEach(accounts::add);
      List<Address> addresses = new ArrayList<>(4);
      addresses.add(new Address("This street", "1234-678", 256, false));
      addresses.add(new Address("The other street", "1234-789", 1, true));
      addresses.add(new Address("Yet another street", "1111-222", 56, true));
      addresses.add(new Address("Out of street", "3333-111", 1, true));
      user = new User(10, "John", "Doe", accounts, addresses, 18, User.Gender.MALE, null, Instant.now(), Instant.now().plusMillis(TimeUnit.DAYS.toMillis(30)));
      Map<String, TopologyIracVersion> versions = new HashMap<>();
      versions.put("site_1", TopologyIracVersion.newVersion(10));
      versions.put("site_2", TopologyIracVersion.newVersion(15).increment(15));
      IracEntryVersion version = new IracEntryVersion(versions);
      metadata = new IracMetadata("site_1", version);

      try {
         addressBytes = ProtobufUtil.toWrappedByteArray(ctx, address);
         userBytes = ProtobufUtil.toWrappedByteArray(ctx, user);
         metadataBytes = ProtobufUtil.toWrappedByteArray(ctx, metadata);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }


   @Benchmark
   public Object testMarshallAddress() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, address);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(addressBytes.length);
         ProtobufUtil.toWrappedStream(ctx, os, address);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Object testMarshallUser() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, user);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(userBytes.length);
         ProtobufUtil.toWrappedStream(ctx, os, user);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Object testMarshallIracMetadata() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.toWrappedByteArray(ctx, metadata);
      else {
         ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx(metadataBytes.length);
         ProtobufUtil.toWrappedStream(ctx, os, metadata);
         return os.getByteBuffer();
      }
   }

   @Benchmark
   public Address testUnmarshallAddress() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, addressBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(addressBytes));
   }

   @Benchmark
   public User testUnmarshallUser() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, userBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(userBytes));
   }

   @Benchmark
   public IracMetadata testUnmarshallMetadata() throws IOException {
      if (byteArrayOrStream)
         return ProtobufUtil.fromWrappedByteArray(ctx, metadataBytes);
      else
         return ProtobufUtil.fromWrappedStream(ctx, new ByteArrayInputStream(metadataBytes));
   }
}
