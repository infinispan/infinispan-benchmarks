package org.infinispan.jmhbenchmarks;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.RangeSet;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.persistence.impl.MarshalledEntryUtil;
import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestInternalCacheEntryFactory;
import org.infinispan.util.concurrent.CompletionStages;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import io.reactivex.Flowable;

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
	private AdvancedLoadWriteStore store;
	private IntSet halfSegments;
	private KeyPartitioner keyPartitioner;

	@Setup
	public void initializeState(KeySequenceGenerator generator) throws IOException {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		storeType.apply(builder.persistence())
				.segmented(segmented)
				// Make sure runs between don't leak into each other
				.purgeOnStartup(true);
		cacheManager = new DefaultCacheManager(new GlobalConfigurationBuilder().nonClusteredDefault().defaultCacheName("default").build(),
				builder.build());
		cache = cacheManager.getCache();

		marshaller = TestingUtil.extractPersistenceMarshaller(cacheManager);
		store = (AdvancedLoadWriteStore) TestingUtil.getFirstLoader(cache);
		halfSegments = new RangeSet(cache.getCacheConfiguration().clustering().hash().numSegments() / 2);
		keyPartitioner = TestingUtil.extractComponent(cache, KeyPartitioner.class);

		int writeBatch = 10000;
		Set<MarshallableEntry> entries = new HashSet<>();
		// Now we actually populate the cache
		for (int i = 1; i < generator.distinctEntries + 1; ++i) {
			InternalCacheEntry ice = TestInternalCacheEntryFactory.create(generator.getNextKey(), generator.getNextValue());
			MarshallableEntry entry = MarshalledEntryUtil.create(ice, marshaller);
			entries.add(entry);
			if (i % writeBatch == 0) {
				CompletionStages.join(store.bulkUpdate(Flowable.fromIterable(entries)));
				entries.clear();
			}
		}
		if (!entries.isEmpty()) {
			CompletionStages.join(store.bulkUpdate(Flowable.fromIterable(entries)));
		}
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}

	public AdvancedLoadWriteStore getStore() {
		return store;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public IntSet getHalfSegments() {
		return halfSegments;
	}

	public KeyPartitioner getKeyPartitioner() {
		return keyPartitioner;
	}

	@TearDown
	public void shutdownState() {
		cache.stop();
	}
}
