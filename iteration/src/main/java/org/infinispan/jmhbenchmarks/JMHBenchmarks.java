package org.infinispan.jmhbenchmarks;

import java.util.Iterator;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.container.entries.CacheEntry;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value=1, jvmArgs = {
		"-Xmx4G",
		"-Xms4G",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-XX:+AlwaysPreTouch",
		"-Xss512k",
		"-XX:HeapDumpPath=/tmp/java_heap",
		"-Djava.net.preferIPv4Stack=true",
		"-XX:+UseLargePages",
		"-XX:LargePageSizeInBytes=2m"})
@BenchmarkMode(Mode.Throughput)
public class JMHBenchmarks {

	private void performSequentialIteration(InfinispanHolder ih, Blackhole bh) {
		Iterator<Map.Entry<Object, Object>> iterator = ih.getReadCache().entrySet().stream()
				.sequentialDistribution()
				.distributedBatchSize(ih.getBatchSize())
				.iterator();
		iterator.forEachRemaining(bh::consume);
	}

	private void performParallelIteration(InfinispanHolder ih, Blackhole bh) {
		Iterator<Map.Entry<Object, Object>> iterator = ih.getReadCache().entrySet().stream()
				.parallelDistribution()
				.distributedBatchSize(ih.getBatchSize())
				.iterator();
		iterator.forEachRemaining(bh::consume);
	}

	private void performSequentialIterationNoRehash(InfinispanHolder ih, Blackhole bh) {
		Iterator<Map.Entry<Object, Object>> iterator = ih.getReadCache().entrySet().stream()
				.sequentialDistribution()
				.disableRehashAware()
				.distributedBatchSize(ih.getBatchSize())
				.iterator();
		iterator.forEachRemaining(bh::consume);
	}

	private void performParallelIterationNoRehash(InfinispanHolder ih, Blackhole bh) {
		Iterator<Map.Entry<Object, Object>> iterator = ih.getReadCache().entrySet().stream()
				.parallelDistribution()
				.disableRehashAware()
				.distributedBatchSize(ih.getBatchSize())
				.iterator();
		iterator.forEachRemaining(bh::consume);
	}

	@Benchmark
	@SuppressWarnings("rawtypes")
	public void iteratorSequential(InfinispanHolder ih, Blackhole bh) {
		performSequentialIteration(ih, bh);
	}

	@Benchmark
	@SuppressWarnings("rawtypes")
	public void iteratorParallel(InfinispanHolder ih, Blackhole bh) {
		performParallelIteration(ih, bh);
	}

	@Benchmark
	@SuppressWarnings("rawtypes")
	public void noRehashIteratorSequential(InfinispanHolder ih, Blackhole bh) {
		performSequentialIterationNoRehash(ih, bh);
	}

	@Benchmark
	@SuppressWarnings("rawtypes")
	public void noRehashIteratorParallel(InfinispanHolder ih, Blackhole bh) {
		performParallelIterationNoRehash(ih, bh);
	}
}
