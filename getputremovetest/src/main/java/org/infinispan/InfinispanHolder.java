package org.infinispan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import org.infinispan.commons.time.DefaultTimeService;
import org.infinispan.commons.time.TimeService;
import org.infinispan.commons.util.ProcessorInfo;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.factories.threads.BlockingThreadFactory;
import org.infinispan.factories.threads.DefaultThreadFactory;
import org.infinispan.factories.threads.NonBlockingThreadFactory;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.util.ControlledConsistentHashFactory;
import org.infinispan.util.concurrent.BlockingTaskAwareExecutorService;
import org.infinispan.util.concurrent.BlockingTaskAwareExecutorServiceImpl;
import org.jgroups.protocols.TP;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {

	@Param("1")
	private int nodes;

	@Param("false")
	private boolean insertEntries;

	public List<DefaultCacheManager> mgrs = new ArrayList<>();

	private Cache readCache;
	private Cache writeCache;

	private final LongAdder gets = new LongAdder();
	private final LongAdder puts = new LongAdder();
	private final LongAdder removes = new LongAdder();

	// This makes sure that data is always in the same places
	private ControlledConsistentHashFactory.Default createCH(int nodes) {
		int[][] segments = new int[nodes][];
		for (int i = 0; i < nodes; ++i) {
			segments[i] = new int[] {i, i + 1 == nodes ? 0 : i + 1};
		}
		return new ControlledConsistentHashFactory.Default(segments);
	}

	@Setup
	public void initializeState(KeySequenceGenerator generator) throws IOException {
		System.out.println( "Running in embedded mode: starting " + nodes + " nodes" );

		TimeService timeService = new DefaultTimeService();

		Executor jgroupsThreadPool = Executors.newFixedThreadPool(100, new org.jgroups.util.DefaultThreadFactory("jgroups", true));

		ExecutorService nonBlockingExecutor = Executors.newFixedThreadPool(ProcessorInfo.availableProcessors() * 2, new NonBlockingThreadFactory("ISPN-non-blocking-thread-group", Thread.NORM_PRIORITY,
				DefaultThreadFactory.DEFAULT_PATTERN, "All", "non-blocking"));

		BlockingTaskAwareExecutorService nonBlockingTaskAwareExecutorService = new BlockingTaskAwareExecutorServiceImpl(nonBlockingExecutor, timeService);

		ExecutorService blockingExecutor = Executors.newFixedThreadPool(150, new BlockingThreadFactory("ISPN-blocking-thread-group", Thread.NORM_PRIORITY,
				DefaultThreadFactory.DEFAULT_PATTERN, "All", "blocking"));

		BlockingTaskAwareExecutorService blockingTaskAwareExecutorService = new BlockingTaskAwareExecutorServiceImpl(blockingExecutor, timeService);

		for ( int i=0; i< nodes; i++ ) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.clustering().hash().consistentHashFactory(createCH(nodes)).numSegments(nodes);
			configurationBuilder.clustering().cacheMode(CacheMode.DIST_SYNC);
			// This is only using a local cache for now
			DefaultCacheManager mgr = new DefaultCacheManager(new GlobalConfigurationBuilder().clusteredDefault().defaultCacheName("default").build(),
					configurationBuilder.build(), false);
			GlobalComponentRegistry gcr = mgr.getGlobalComponentRegistry();
			BasicComponentRegistry gbcr = gcr.getComponent(BasicComponentRegistry.class);
			gbcr.replaceComponent(KnownComponentNames.NON_BLOCKING_EXECUTOR, nonBlockingTaskAwareExecutorService, true);
			gbcr.replaceComponent(KnownComponentNames.BLOCKING_EXECUTOR, blockingTaskAwareExecutorService, true);

			gbcr.rewire();

			mgr.start();

			JGroupsTransport jGroupsTransport = (JGroupsTransport) gbcr.getComponent(Transport.class).wired();
			TP tp = jGroupsTransport.getChannel().getProtocolStack().getTransport();
			tp.setThreadPool(jgroupsThreadPool);

			readCache = mgr.getCache();
			writeCache = readCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
			readCache.start();
			mgrs.add(mgr);
			System.out.println( "Started node: " + i );
		}

		if (insertEntries) {
			for (int i = 0; i < KeySequenceGenerator.keySpaceSize; ++i) {
				writeCache.put(generator.getNextKey(), generator.getNextValue());
			}
		}
	}

	@TearDown
	public void shutdownState() {
		for (DefaultCacheManager cm : mgrs) {
			cm.stop();
		}
		if (gets.longValue() > 0) System.out.println( "Gets performed: " + gets.longValue() );
		if (puts.longValue() > 0) System.out.println( "Puts performed: " + puts.longValue() );
		if (removes.longValue() > 0) System.out.println( "Removes performed: " + removes.longValue() );
	}

	public Cache getReadCache() {
		return readCache;
	}
	public Cache getWriteCache() {
		return writeCache;
	}

	public void cacheGetDone() {
		gets.increment();
	}
	
	public void cachePutDone() {
		puts.increment();
	}

	public void cacheRemoveDone() {
		removes.increment();
	}

}
