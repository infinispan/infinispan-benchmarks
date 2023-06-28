package org.infinispan;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanRemoteHolder {

   private static final AtomicInteger hotRodPort = new AtomicInteger(ConfigurationProperties.DEFAULT_HOTROD_PORT);

   static final String cfg = System.getProperty( "infinispan.cfg", "dist-sync.xml" );

   private final LongAdder gets = new LongAdder();
   private final LongAdder puts = new LongAdder();
   private final LongAdder removes = new LongAdder();

   private final AtomicInteger cacheRequestCount = new AtomicInteger();

   private DefaultCacheManager[] mgrs;
   private RemoteCacheManager[] remotes;
   private RemoteCache<?, ?>[] caches;

   private HotRodServer[] servers;

   @Param("3")
   private int nodes;

   @Param({"1", "3"})
   private int remoteClients;

   @Param({"-1"})
   int maxPoolSize = -1;

   @Setup
   public void initializeState() throws IOException {
      mgrs = new DefaultCacheManager[nodes];
      servers = new HotRodServer[nodes];
      remotes = new RemoteCacheManager[remoteClients];
      caches = new RemoteCache[remoteClients];
      HotRodServerConfiguration firstConfig = null;

      for (int i = 0; i < nodes; ++i) {
         mgrs[i] = new DefaultCacheManager(cfg);
         HotRodServerConfigurationBuilder configurationBuilder = new HotRodServerConfigurationBuilder();
         configurationBuilder.port(hotRodPort.getAndIncrement());
         HotRodServerConfiguration hotRodServerConfiguration = configurationBuilder.build();
         if (firstConfig == null) {
            firstConfig = hotRodServerConfiguration;
         }
         servers[i] = new HotRodServer();
         servers[i].start(hotRodServerConfiguration, mgrs[i]);
         System.out.printf("Started server %d\n", hotRodServerConfiguration.port());
      }

      ConfigurationBuilder remoteCacheConfigurationBuilder = new ConfigurationBuilder();
      remoteCacheConfigurationBuilder.connectionPool().maxActive(maxPoolSize);
      remoteCacheConfigurationBuilder.addServer().host(firstConfig.host()).port(firstConfig.port());
      for (int i = 0; i < remoteClients; ++i) {
         remotes[i] = new RemoteCacheManager(remoteCacheConfigurationBuilder.build());
         caches[i] = remotes[i].getCache();
      }
   }

   @TearDown
   public void shutdownState() {
      Arrays.stream(servers).forEach(HotRodServer::stop);
      Arrays.stream(mgrs).forEach(DefaultCacheManager::stop);
      System.out.println( "Gets performed: " + gets.longValue() );
      System.out.println( "Puts performed: " + puts.longValue() );
      System.out.println( "Removes performed: " + removes.longValue() );
   }

   public RemoteCache getCache() {
      int offset = cacheRequestCount.getAndIncrement();
      return caches[offset % caches.length];
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

