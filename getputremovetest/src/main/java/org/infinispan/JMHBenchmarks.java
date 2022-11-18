package org.infinispan;

import org.infinispan.client.hotrod.RemoteCache;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value=1, jvmArgs = {
		"-Xmx10G",
		"-Xms10G",
		"-XX:+HeapDumpOnOutOfMemoryError",
//		"-XX:+AlwaysPreTouch",
//		"-XX:+PrintFlagsFinal",
		"-Xss512k",
//		"-XX:HeapDumpPath=/tmp/java_heap",
		"-Djava.net.preferIPv4Stack=true",
		"-XX:+UseLargePages",
		"-XX:LargePageSizeInBytes=2m",
//		"-javaagent:/home/wburns/RedHat/type-pollution-agent/agent/target/type-pollution-agent-0.1-SNAPSHOT.jar=org.infinispan -Dio.type.pollution.full.traces=true"
})
@BenchmarkMode(Mode.Throughput)
public class JMHBenchmarks {

	@Benchmark
	@GroupThreads(2)
	@Group("getPutHotRod")
	public void infinispanRemoteRemove(InfinispanRemoteHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		RemoteCache cache = ih.getCache();
		bh.consume( cache.remove( key) );
		ih.cacheRemoveDone();
	}

	@Benchmark
	@GroupThreads(4)
	@Group("getPutHotRod")
	public void infinispanRemotePut(InfinispanRemoteHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Object value = kg.getNextValue();
		RemoteCache cache = ih.getCache();
		bh.consume( cache.put( key, value ) );
		ih.cachePutDone();
	}

	@Benchmark
	@GroupThreads(16)
	@Group("getPutHotRod")
	public void infinispanRemoteGet(InfinispanRemoteHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		RemoteCache cache = ih.getCache();
		bh.consume( cache.get( key ) );
		ih.cacheGetDone();
	}

	@Benchmark
	@Threads(16)
	@SuppressWarnings("rawtypes")
	public void infinispanGetIsolated(InfinispanHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Cache cache = ih.getReadCache();
		bh.consume( cache.get( key ) );
		ih.cacheGetDone();
	}

	@Benchmark
	@GroupThreads(16)
	@Group("getPutInfinispan")
	@SuppressWarnings("rawtypes")
	public void infinispanGet(InfinispanHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Cache cache = ih.getReadCache();
		bh.consume( cache.get( key ) );
		ih.cacheGetDone();
	}

	@Benchmark
	@GroupThreads(4)
	@Group("getPutInfinispan")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void infinispanPut(InfinispanHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Object value = kg.getNextValue();
		Cache cache = ih.getWriteCache();
		bh.consume( cache.put( key, value ) );
		ih.cachePutDone();
	}

	@Benchmark
	@GroupThreads(2)
	@Group("getPutInfinispan")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void infinispanRemove(InfinispanHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Cache cache = ih.getWriteCache();
		bh.consume( cache.remove( key ) );
		ih.cacheRemoveDone();
	}

	@Benchmark
	@Threads(4)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void infinispanPutIsolated(InfinispanHolder ih, Blackhole bh, KeySequenceGenerator kg) {
		Object key = kg.getNextKey();
		Object value = kg.getNextValue();
		Cache cache = ih.getWriteCache();
		bh.consume( cache.put( key, value ) );
		ih.cachePutDone();
	}
}
