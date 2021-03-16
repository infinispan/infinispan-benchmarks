package org.infinispan.jmhbenchmarks;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.persistence.PersistenceMarshaller;
import org.infinispan.marshall.persistence.impl.MarshalledEntryUtil;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.support.SegmentPublisherWrapper;
import org.infinispan.persistence.support.WaitNonBlockingStore;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestInternalCacheEntryFactory;
import org.infinispan.util.concurrent.CompletionStages;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import io.reactivex.rxjava3.core.Flowable;

@State(Scope.Benchmark)
public class InfinispanHolder {

	@Param({ "SINGLE", "SOFT_INDEX", "ROCKS" })
	private StoreType storeType;

	@Param({ "true", "false" })
	private boolean segmented;

	@Param("100")
	private int batchSize;

	private Marshaller marshaller;
	private EmbeddedCacheManager cacheManager;
	private Cache cache;
	private WaitNonBlockingStore store;
	private IntSet allSegments;
	private IntSet halfSegments;
	private KeyPartitioner keyPartitioner;

	private KeySequenceGenerator generator;

	@Setup
	public void initializeState(KeySequenceGenerator generator) throws IOException {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		storeType.apply(builder.persistence())
				.segmented(segmented)
				// Make sure runs between don't leak into each other
				.purgeOnStartup(true);
		GlobalConfigurationBuilder globalConfigurationBuilder = new GlobalConfigurationBuilder();
		globalConfigurationBuilder.globalState()
				.enable()
				.persistentLocation(Paths.get(System.getProperty("java.io.tmpdir"), getClass().getName()).toString());
		cacheManager = new DefaultCacheManager(globalConfigurationBuilder.nonClusteredDefault().defaultCacheName("default").build(),
				builder.build());
		cache = cacheManager.getCache();

		marshaller = TestingUtil.extractPersistenceMarshaller(cacheManager);
		store = TestingUtil.getFirstStore(cache);
		int segments = cache.getCacheConfiguration().clustering().hash().numSegments();
		allSegments = IntSets.immutableRangeSet(segments);
		halfSegments = IntSets.immutableRangeSet(segments / 2);
		keyPartitioner = TestingUtil.extractComponent(cache, KeyPartitioner.class);

		int writeBatch = 10000;
		Set<MarshallableEntry> entries = new HashSet<>();
		// Now we actually populate the cache
		for (int i = 1; i < generator.distinctEntries + 1; ++i) {
			InternalCacheEntry ice = TestInternalCacheEntryFactory.create(generator.getNextKey(), generator.getNextValue());
			MarshallableEntry entry = MarshalledEntryUtil.create(ice, marshaller);
			entries.add(entry);
			if (i % writeBatch == 0) {
				CompletionStages.join(store.batch(segments, Flowable.empty(), Flowable.fromIterable(entries).groupBy(innerIce ->
						keyPartitioner.getSegment(innerIce.getKey())).map(SegmentPublisherWrapper::wrap)));
				entries.clear();
			}
		}
		if (!entries.isEmpty()) {
			CompletionStages.join(store.batch(segments, Flowable.empty(), Flowable.fromIterable(entries).groupBy(innerIce ->
					keyPartitioner.getSegment(innerIce.getKey())).map(SegmentPublisherWrapper::wrap)));
		}
		this.generator = generator;
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}

	public WaitNonBlockingStore getStore() {
		return store;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public IntSet getAllSegments() {
		return allSegments;
	}

	public IntSet getHalfSegments() {
		return halfSegments;
	}

	public KeyPartitioner getKeyPartitioner() {
		return keyPartitioner;
	}

	public KeySequenceGenerator getGenerator() {
		return generator;
	}

	@TearDown
	public void shutdownState() {
		cache.stop();
		storeType.tearDown();
	}
}
