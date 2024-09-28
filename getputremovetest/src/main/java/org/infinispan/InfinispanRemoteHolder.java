package org.infinispan;

import java.io.IOException;

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

@State(Scope.Benchmark)
public class InfinispanRemoteHolder {

   @Param("localhost")
   private String remoteHost;

   @Param("11222")
   private int remotePort;

   @Param("1")
   private int maxPoolSize;

   private RemoteCache cache;

   private RemoteCacheManager remoteCacheManager;

   @Setup
   public void initializeState() throws IOException {
      HotRodServerConfigurationBuilder configurationBuilder = new HotRodServerConfigurationBuilder();
      configurationBuilder.port(remotePort).host(remoteHost);
      HotRodServerConfiguration hotRodServerConfiguration = configurationBuilder.build();
      System.out.printf("Started client %d\n", hotRodServerConfiguration.port());

      ConfigurationBuilder remoteCacheConfigurationBuilder = new ConfigurationBuilder();
      remoteCacheConfigurationBuilder.disableTracingPropagation();
      remoteCacheConfigurationBuilder.connectionPool().maxActive(maxPoolSize);
      remoteCacheConfigurationBuilder.addServer().host(hotRodServerConfiguration.host()).port(hotRodServerConfiguration.port());
      remoteCacheManager = new RemoteCacheManager(remoteCacheConfigurationBuilder.build());

      cache = remoteCacheManager.getCache("benchmark");
   }

   @TearDown
   public void shutdownState() {
      remoteCacheManager.stop();
   }

   public RemoteCache getCache() {
      return cache;
   }

}
