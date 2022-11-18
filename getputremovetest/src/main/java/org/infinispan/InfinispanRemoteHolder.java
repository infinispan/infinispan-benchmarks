package org.infinispan;

import java.io.IOException;
import java.util.concurrent.atomic.LongAdder;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Thread)
public class InfinispanRemoteHolder {

	@Param("localhost")
	private String remoteHost;

	@Param("11222")
	private int remotePort;

	private RemoteCache cache;

	private final LongAdder gets = new LongAdder();
	private final LongAdder puts = new LongAdder();
	private final LongAdder removes = new LongAdder();

	private RemoteCacheManager remoteCacheManager;

	@Setup
	public void initializeState() throws IOException {
		HotRodServerConfigurationBuilder configurationBuilder = new HotRodServerConfigurationBuilder();
		configurationBuilder.port(remotePort).host(remoteHost);
		HotRodServerConfiguration hotRodServerConfiguration = configurationBuilder.build();
		System.out.printf("Started client %d\n", hotRodServerConfiguration.port());

		ConfigurationBuilder remoteCacheConfigurationBuilder = new ConfigurationBuilder();
		remoteCacheConfigurationBuilder.addServer().host(hotRodServerConfiguration.host()).port(hotRodServerConfiguration.port());
		remoteCacheManager = new RemoteCacheManager(remoteCacheConfigurationBuilder.build());

		cache = remoteCacheManager.getCache();
	}

	@TearDown
	public void shutdownState() {
		remoteCacheManager.stop();
		if (gets.longValue() > 0) System.out.println( "Gets performed: " + gets.longValue() );
		if (puts.longValue() > 0) System.out.println( "Puts performed: " + puts.longValue() );
		if (removes.longValue() > 0) System.out.println( "Removes performed: " + removes.longValue() );
	}

	public RemoteCache getCache() {
		return cache;
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
