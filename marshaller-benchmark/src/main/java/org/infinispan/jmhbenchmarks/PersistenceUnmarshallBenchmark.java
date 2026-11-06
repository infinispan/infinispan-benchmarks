package org.infinispan.jmhbenchmarks;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.WrappedByteArray;
import org.infinispan.multimap.impl.HashMapBucket;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures the read of an entry from a store, from the stored bytes to the object, with the same call that
 * {@code MarshallableEntryImpl} makes.
 * <p>
 * Run with {@code -prof gc}: {@code gc.alloc.rate.norm} is the allocation per read, the main effect of removing a copy.
 * Compare a baseline and a candidate with one jar built against each.
 * </p>
 */
@Fork(value = 2, jvmArgsAppend = {"-Xms4g", "-Xmx4g"})
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PersistenceUnmarshallBenchmark {

   /**
    * Reads a hash bucket, whose keys and values go through {@code MarshallableUserObject}.
    *
    * @param holder provides the persistence marshaller.
    * @param state provides the entries.
    * @return the unmarshalled bucket.
    * @throws Exception if the entry cannot be unmarshalled.
    */
   @Benchmark
   public Object bucket(InfinispanHolder holder, BucketEntries state) throws Exception {
      byte[] entry = state.next();
      return holder.getPersistenceMarshaller().objectFromByteBuffer(entry, 0, entry.length);
   }

   /**
    * Reads a plain value that never reaches {@code MarshallableUserObject}. A change to that class should not move
    * this result.
    *
    * @param holder provides the persistence marshaller.
    * @param state provides the entries.
    * @return the unmarshalled value.
    * @throws Exception if the entry cannot be unmarshalled.
    */
   @Benchmark
   public Object wrappedBytes(InfinispanHolder holder, WrappedBytesEntries state) throws Exception {
      byte[] entry = state.next();
      return holder.getPersistenceMarshaller().objectFromByteBuffer(entry, 0, entry.length);
   }

   /**
    * A set of entries in the form a store hands them to the persistence marshaller.
    * <p>
    * The number of entries decides what a read pays for. A single entry stays in the CPU caches, so it measures the
    * marshalling code alone. A set larger than the CPU caches, read in a shuffled order, makes the first access to each
    * entry a cold memory read, as on a server reading from a store. The two differ because removing a copy from a cold
    * read moves part of its cost to the next copy.
    * </p>
    * <p>
    * Each thread holds its own set, so memory use grows with the number of benchmark threads.
    * </p>
    */
   @State(Scope.Thread)
   public abstract static class StoredEntries {

      private static final long SEED = 0xBAAD_C0DEL;

      @Param({"1", "5000"})
      public int entries;

      /**
       * The value bytes of each entry.
       */
      @Param({"100000"})
      public int size;

      private byte[][] stored;
      private int[] order;
      private int position;

      /**
       * Marshals the entries and verifies that one of them reads back correctly, so that a marshaller decoding the
       * wrong bytes fails the benchmark instead of reporting a good result.
       *
       * @param holder provides the persistence marshaller.
       * @throws Exception if an entry cannot be marshalled or unmarshalled.
       * @throws IllegalStateException if the entry read back is not the expected one.
       */
      @Setup
      public void setup(InfinispanHolder holder) throws Exception {
         Marshaller marshaller = holder.getPersistenceMarshaller();
         Random random = new Random(SEED);
         stored = new byte[entries][];
         order = new int[entries];
         for (int i = 0; i < entries; i++) {
            stored[i] = marshaller.objectToByteBuffer(create(random));
            order[i] = i;
         }
         for (int i = entries - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);
            int swapped = order[i];
            order[i] = order[other];
            order[other] = swapped;
         }
         verify(marshaller.objectFromByteBuffer(stored[0], 0, stored[0].length));
      }

      /**
       * Returns the next entry to read, cycling through all the entries in a fixed shuffled order.
       *
       * @return the marshalled bytes of the entry, which must not be changed.
       */
      public byte[] next() {
         byte[] entry = stored[order[position]];
         position = position + 1 == order.length ? 0 : position + 1;
         return entry;
      }

      /**
       * Creates the object to store in an entry.
       *
       * @param random the source of the value bytes.
       * @return the object to marshal.
       */
      protected abstract Object create(Random random);

      /**
       * Verifies an object read back from the bytes of an entry.
       *
       * @param decoded the unmarshalled object.
       * @throws IllegalStateException if the object does not have the expected type or size.
       */
      protected abstract void verify(Object decoded);
   }

   /**
    * Entries holding a {@link HashMapBucket}, as the RESP endpoint stores a hash. Each key and value of the bucket goes
    * through {@code MarshallableUserObject}. The size of an entry is split evenly between its fields.
    */
   @State(Scope.Thread)
   public static class BucketEntries extends StoredEntries {

      @Param({"50"})
      public int fields;

      @Override
      protected Object create(Random random) {
         Map<byte[], byte[]> values = new HashMap<>();
         for (int i = 0; i < fields; i++) {
            byte[] value = new byte[size / fields];
            random.nextBytes(value);
            values.put(String.format("field:%012d", i).getBytes(StandardCharsets.US_ASCII), value);
         }
         return HashMapBucket.create(values);
      }

      @Override
      protected void verify(Object decoded) {
         if (!(decoded instanceof HashMapBucket<?, ?> bucket) || bucket.size() != fields) {
            throw new IllegalStateException("Expected a bucket of " + fields + " fields, found " + decoded);
         }
         for (Object value : bucket.values()) {
            if (!(value instanceof byte[] bytes) || bytes.length != size / fields) {
               throw new IllegalStateException("Expected values of " + size / fields + " bytes, found " + value);
            }
         }
      }
   }

   /**
    * Entries holding a single {@link WrappedByteArray}. ProtoStream marshals this type directly, so it never reaches
    * {@code MarshallableUserObject}. It is the control of the benchmark.
    */
   @State(Scope.Thread)
   public static class WrappedBytesEntries extends StoredEntries {

      @Override
      protected Object create(Random random) {
         byte[] value = new byte[size];
         random.nextBytes(value);
         return new WrappedByteArray(value);
      }

      @Override
      protected void verify(Object decoded) {
         if (!(decoded instanceof WrappedByteArray wrapped) || wrapped.getLength() != size) {
            throw new IllegalStateException("Expected " + size + " bytes, found " + decoded);
         }
      }
   }
}
