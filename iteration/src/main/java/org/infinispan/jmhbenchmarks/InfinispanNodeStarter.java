package org.infinispan.jmhbenchmarks;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

public final class InfinispanNodeStarter {

	static final String cfg = System.getProperty( "infinispan.cfg", "dist-sync.xml" );

	public static void main(String[] args) throws IOException {
		DefaultCacheManager mgr = new DefaultCacheManager( InfinispanNodeStarter.cfg );
		mgr.start();
		Cache<Object, Object> cache = mgr.getCache();
		cache.start();
	}

}
