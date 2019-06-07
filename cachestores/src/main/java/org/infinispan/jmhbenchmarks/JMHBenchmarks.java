package org.infinispan.jmhbenchmarks;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.infinispan.commons.marshall.StreamingMarshaller;
import org.infinispan.commons.util.IntSet;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.marshall.persistence.impl.MarshalledEntryUtil;
import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.SegmentedAdvancedLoadWriteStore;
import org.infinispan.test.fwk.TestInternalCacheEntryFactory;
import org.infinispan.util.concurrent.CompletionStages;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;

@Fork(value=1, jvmArgs = {
		"-Xmx4G",
		"-Xms4G",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-XX:+AlwaysPreTouch",
		"-Xss512k",
		"-XX:HeapDumpPath=/tmp/java_heap",
		"-XX:+UseLargePages",
		"-XX:LargePageSizeInBytes=2m"})
@BenchmarkMode(Mode.Throughput)
public class JMHBenchmarks {
	private static MarshallableEntry newEntry(StreamingMarshaller marshaller, KeySequenceGenerator generator) {
		InternalCacheEntry ice = TestInternalCacheEntryFactory.create(generator.getNextKey(), generator.getNextValue());
		return MarshalledEntryUtil.create(ice, marshaller);
	}

	@Benchmark
	public void testWrite(InfinispanHolder holder, KeySequenceGenerator generator) {
		holder.getStore().write(newEntry(holder.getMarshaller(), generator));
	}

	@Benchmark
	public void testWriteBatch(InfinispanHolder holder, KeySequenceGenerator generator) {
		int batchSize = holder.getBatchSize();
		Set<MarshallableEntry> batch = new HashSet<>(batchSize);
		StreamingMarshaller marshaller = holder.getMarshaller();
		for (int i = 0; i < holder.getBatchSize(); ++i) {
			batch.add(newEntry(marshaller, generator));
		}
		CompletionStages.join(holder.getStore().bulkUpdate(Flowable.fromIterable(batch)));
	}

	@Benchmark
	public Object testLoad(InfinispanHolder holder, KeySequenceGenerator generator) {
		return holder.getStore().load(generator.getNextKey());
	}

	@Benchmark
	public boolean testContains(InfinispanHolder holder, KeySequenceGenerator generator) {
		return holder.getStore().contains(generator.getNextKey());
	}

	@Benchmark
	public int testSize(InfinispanHolder holder) {
		return holder.getStore().size();
	}

	@Benchmark
	public void testPublishKeysTrue(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishKeys(f -> true))
				.blockingForEach(blackhole::consume);
	}

	@Benchmark
	public void testPublishKeysFalse(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishKeys(f -> false))
				.blockingForEach(blackhole::consume);
	}

	@Benchmark
	public void testPublishEntriesTrue(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishEntries(f -> true, true, true))
				.blockingForEach(blackhole::consume);
	}

	@Benchmark
	public void testPublishEntriesFalse(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishEntries(f -> false, true, true))
				.blockingForEach(blackhole::consume);
	}

	<R> void keysPublisherHalfSegments(Blackhole blackhole, InfinispanHolder holder,
			Predicate<Object> predicate) {
		AdvancedLoadWriteStore alws = holder.getStore();
		IntSet intSet = holder.getHalfSegments();
		KeyPartitioner keyPartitioner = holder.getKeyPartitioner();
		if (alws instanceof SegmentedAdvancedLoadWriteStore) {
			Flowable.fromPublisher(((SegmentedAdvancedLoadWriteStore) alws).publishKeys(intSet, predicate))
					.blockingForEach(blackhole::consume);
		} else {
			Predicate<? super R> segmentFilter = k -> intSet.contains(keyPartitioner.getSegment(k));
			Flowable.fromPublisher(alws.publishKeys(segmentFilter.and(predicate)))
					.blockingForEach(blackhole::consume);
		}
	}

	void entriesPublisherHalfSegments(Blackhole blackhole, InfinispanHolder holder,
			Predicate<Object> predicate) {
		AdvancedLoadWriteStore alws = holder.getStore();
		IntSet intSet = holder.getHalfSegments();
		KeyPartitioner keyPartitioner = holder.getKeyPartitioner();
		if (alws instanceof SegmentedAdvancedLoadWriteStore) {
			Flowable.fromPublisher(((SegmentedAdvancedLoadWriteStore) alws).publishEntries(intSet, predicate, true, true))
					.blockingForEach(blackhole::consume);
		} else {
			Predicate<Object> segmentFilter = e -> intSet.contains(keyPartitioner.getSegment(e));
			Flowable.fromPublisher(alws.publishEntries(segmentFilter.and(predicate), true, true))
					.blockingForEach(blackhole::consume);
		}
	}

	@Benchmark
	public void testPublishKeysTrueHalfSegments(Blackhole blackhole, InfinispanHolder holder) {
		keysPublisherHalfSegments(blackhole, holder, f -> true);
	}

	@Benchmark
	public void testPublishKeysFalseHalfSegments(Blackhole blackhole, InfinispanHolder holder) {
		keysPublisherHalfSegments(blackhole, holder, f -> false);
	}

	@Benchmark
	public void testPublishEntriesTrueHalfSegments(Blackhole blackhole, InfinispanHolder holder) {
		entriesPublisherHalfSegments(blackhole, holder, f -> true);
	}

	@Benchmark
	public void testPublishEntriesFalseHalfSegments(Blackhole blackhole, InfinispanHolder holder) {
		entriesPublisherHalfSegments(blackhole, holder, f -> false);
	}
}
