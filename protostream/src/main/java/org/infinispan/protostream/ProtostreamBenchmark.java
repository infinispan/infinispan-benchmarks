package org.infinispan.protostream;

import static org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializer.INSTANCE;

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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

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
   }


   @Benchmark
   public void testMarshallAddress(Blackhole blackhole) throws IOException {
      blackhole.consume(ProtobufUtil.toWrappedByteArray(ctx, address));
   }

   @Benchmark
   public void testMarshallUser(Blackhole blackhole) throws IOException {
      blackhole.consume(ProtobufUtil.toWrappedByteArray(ctx, user));
   }

   @Benchmark
   public void testMarshallIracMetadata(Blackhole blackhole) throws IOException {
      blackhole.consume(ProtobufUtil.toWrappedByteArray(ctx, metadata));
   }

}
