package org.infinispan.jmhbenchmarks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.commons.dataconversion.IdentityEncoder;
import org.infinispan.commons.dataconversion.IdentityWrapper;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.test.TestingUtil;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {

	@Param({"6"})
	private int nodes;
	@Param({ "50000", "250000"})
	private int entryAmount;
	@Param({"512", "4096", "16384", "32768"})
	private int batchSize;

	@Param({"true"})
	private boolean useIdentityCache;

	public List<DefaultCacheManager> mgrs = new ArrayList<>();

	private Cache<Object, Object> readCache;
	private Cache<Object, Object> writeCache;

	public int getBatchSize() {
		return batchSize;
	}

	@Setup
	public void initializeState(KeySequenceGenerator generator) throws IOException {
		if ( SharedConfigurationSettings.distributedRun ) {
			System.out.println( "Running in distributed mode: starting a single node only!" );
		}
		else {
			System.out.println( "Running in embedded mode: starting " + nodes + " nodes" );
		}

		for ( int i=0; i< nodes; i++ ) {
			DefaultCacheManager mgr = new DefaultCacheManager( InfinispanNodeStarter.cfg );
			mgr.start();
			readCache = mgr.getCache();
			if (useIdentityCache) {
				readCache = (Cache<Object, Object>) readCache.getAdvancedCache()
						.withWrapping(IdentityWrapper.class).withEncoding(IdentityEncoder.class);
			}
			writeCache = readCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
			readCache.start();
			mgrs.add(mgr);
			if (SharedConfigurationSettings.distributedRun) {
				break;
			}
		}

		System.out.println( readCache.getCacheConfiguration() );

		// Wait until views are complete before inserting
		TestingUtil.blockUntilViewReceived(readCache, nodes);

		int writeBatch = 10000;
		Map<Object, Object> mapToWrite = new HashMap<>(writeBatch);
		// Now we actually populate the cache
		for (int i = 1; i < entryAmount + 1; ++i) {
			mapToWrite.put(generator.getNextKey(), generator.getNextValue());
			if (i % writeBatch == 0) {
				writeCache.putAll(mapToWrite);
				mapToWrite.clear();
			}
		}
		if (!mapToWrite.isEmpty()) {
			writeCache.putAll(mapToWrite);
		}
	}

	@TearDown
	public void shutdownState() {
		for (DefaultCacheManager cm : mgrs) {
			cm.stop();
		}
	}

	public Cache<Object, Object> getReadCache() {
		return readCache;
	}
	public Cache<Object, Object> getWriteCache() {
		return writeCache;
	}
}
