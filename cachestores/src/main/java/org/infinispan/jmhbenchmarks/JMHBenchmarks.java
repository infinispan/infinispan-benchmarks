package org.infinispan.jmhbenchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.marshall.persistence.impl.MarshalledEntryUtil;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.NonBlockingStore;
import org.infinispan.persistence.support.SegmentPublisherWrapper;
import org.infinispan.test.fwk.TestInternalCacheEntryFactory;
import org.infinispan.util.concurrent.CompletionStages;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;

@Fork(value=1, jvmArgs = {
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-XX:+AlwaysPreTouch",
		"-Xss512k",
		"-XX:HeapDumpPath=/tmp/java_heap",
		"-XX:+UseLargePages",
		"-XX:LargePageSizeInBytes=2m"})
@BenchmarkMode(Mode.Throughput)
public class JMHBenchmarks {
	private static MarshallableEntry newEntry(Marshaller marshaller, KeySequenceGenerator generator) {
		InternalCacheEntry ice = TestInternalCacheEntryFactory.create(generator.getNextKey(), generator.getNextValue());
		return MarshalledEntryUtil.create(ice, marshaller);
	}

	@Benchmark
	public void testWrite(InfinispanHolder holder, KeySequenceGenerator generator) {
		holder.getStore().write(newEntry(holder.getMarshaller(), generator));
	}

	@Benchmark
	public void testWriteBatch(InfinispanHolder holder, KeySequenceGenerator generator) {
		Flowable<MarshallableEntry<?, ?>> entryFlowable = (Flowable)
				Flowable.fromSupplier(() -> newEntry(holder.getMarshaller(), generator))
						.take(holder.getBatchSize());

		Publisher<NonBlockingStore.SegmentedPublisher<MarshallableEntry<?, ?>>> writePublisher = entryFlowable
				.groupBy(innerIce -> holder.getKeyPartitioner().getSegment(innerIce.getKey()))
				.map(SegmentPublisherWrapper::wrap);

		CompletionStages.join(holder.getStore().batch(holder.getAllSegments().size(), Flowable.empty(), writePublisher));
	}

	@Benchmark
	public Object testLoad(InfinispanHolder holder, KeySequenceGenerator generator) {
		return holder.getStore().loadEntry(generator.getNextKey());
	}

	@Benchmark
	public boolean testContains(InfinispanHolder holder, KeySequenceGenerator generator) {
		return holder.getStore().contains(generator.getNextKey());
	}

	@Benchmark
	public long testSize(InfinispanHolder holder) {
		return holder.getStore().sizeWait(holder.getAllSegments());
	}

	@Benchmark
	public void testPublishKeysTrue(Blackhole blackhole, InfinispanHolder holder) {
		holder.getStore().publishKeysWait(holder.getAllSegments(), f -> true)
				.forEach(blackhole::consume);
	}

	@Benchmark
	public void testPublishKeysFalse(Blackhole blackhole, InfinispanHolder holder) {
		holder.getStore().publishKeysWait(holder.getAllSegments(), f -> false)
				.forEach(blackhole::consume);
	}

	@Benchmark
	public void testPublishEntriesTrue(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishEntries(holder.getAllSegments(), f -> true, true))
				.doOnNext(blackhole::consume)
				.blockingSubscribe();
	}

	@Benchmark
	public void testPublishEntriesUseValues(Blackhole blackhole, InfinispanHolder holder) {
		Long count = (Long) Flowable.fromPublisher(holder.getStore().publishEntries(holder.getAllSegments(), f -> true, true))
				.map(o -> ((MarshallableEntry) o).getValue())
				.count()
				.blockingGet();

		if (count != holder.getGenerator().distinctEntries) {
			throw new RuntimeException("Number of entries does not match! - expected " +
					holder.getGenerator().distinctEntries + " but received " + count);
		}
	}

	@Benchmark
	public void testPublishEntriesFalse(Blackhole blackhole, InfinispanHolder holder) {
		Flowable.fromPublisher(holder.getStore().publishEntries(holder.getAllSegments(), f -> false, true))
				.doOnNext(blackhole::consume)
				.blockingSubscribe();
	}

	<R> void keysPublisherHalfSegments(Blackhole blackhole, InfinispanHolder holder,
			Predicate<Object> predicate) {
		holder.getStore().publishKeysWait(holder.getHalfSegments(), predicate)
				.forEach(blackhole::consume);
	}

	void entriesPublisherHalfSegments(Blackhole blackhole, InfinispanHolder holder,
												 Predicate<Object> predicate, Function<MarshallableEntry, Object> function) {
		Flowable<Object> flowable = Flowable.fromPublisher(holder.getStore().publishEntries
				(holder.getHalfSegments(), predicate, true));
		if (function != null) {
			flowable = flowable.map((Function) function);
		}
		flowable
				.doOnNext(blackhole::consume)
				.blockingSubscribe();
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
		entriesPublisherHalfSegments(blackhole, holder, f -> true, null);
	}

	@Benchmark
	public void testPublishEntriesTrueHalfSegmentsUseValues(Blackhole blackhole, InfinispanHolder holder) {
		entriesPublisherHalfSegments(blackhole, holder, f -> true, MarshallableEntry::getValue);
	}

	@Benchmark
	public void testPublishEntriesFalseHalfSegments(Blackhole blackhole, InfinispanHolder holder) {
		entriesPublisherHalfSegments(blackhole, holder, f -> false, null);
	}
}
