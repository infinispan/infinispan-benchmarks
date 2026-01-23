package org.infinispan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.commons.configuration.io.ConfigurationResourceResolvers;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.commons.util.Util;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {

   private static final String CACHE_NAME = "query-benchmark-cache";

   static final String cfg = System.getProperty( "infinispan.cfg", "dist-sync.xml" );
   private final AtomicInteger cacheRequestCount = new AtomicInteger();

   private DefaultCacheManager[] mgrs;
   private Cache<String, CacheValue>[] caches;

   private long startInterval;
   private long endInterval;
   private String persistentStatePath;

   @Param("3")
   private int nodes;

   @Param({"1000000", "3000000"})
   private int size;

   @Param({"50", "99"})
   private int hitRatio;

   @Setup
   public void initializeState() throws IOException {
      mgrs = new DefaultCacheManager[nodes];
      caches = new Cache[nodes];

      long pid = ProcessHandle.current().pid();
      persistentStatePath = Path.of("target", Long.toString(pid)).toAbsolutePath().toString();
      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.indexing().enabled(true)
            .addIndexedEntities(CacheValue.class);

      cb.encoding().mediaType(MediaType.APPLICATION_PROTOSTREAM);
      cb.clustering().cacheMode(CacheMode.DIST_SYNC);

      for (int i = 0; i < nodes; ++i) {
         mgrs[i] = createCacheManager(i);
         mgrs[i].defineConfiguration(CACHE_NAME, cb.build());
         caches[i] = mgrs[i].getCache(CACHE_NAME);
      }

      startInterval = System.currentTimeMillis();
      endInterval = startInterval + (int) (size * (1f * hitRatio / 100));
      populate(getCache(), startInterval);
   }

   private void populate(Cache<String, CacheValue> cache, long now) {
      final int numThreads = 50;
      AtomicInteger generate = new AtomicInteger(1);
      CountDownLatch latch = new CountDownLatch(numThreads);
      CompletableFuture<?>[] cfs = new CompletableFuture[numThreads];

      for (int i = 0; i < numThreads; i++) {
         cfs[i] = CompletableFuture.runAsync(() -> {
            while (true) {
               final int key = generate.getAndIncrement();
               CacheValue payload = CacheValue.create(now + key);
               if (key > size)
                  break;

               try {
                  cache.put(Integer.toString(key), payload);
               } catch (Throwable t) {
                  t.printStackTrace(System.err);
               }
            }
            latch.countDown();
         }, Executors.newVirtualThreadPerTaskExecutor());
      }

      try {
         while (true) {
            int progress = generate.get();
            if (!latch.await(5, TimeUnit.MINUTES)) {
               if (progress != generate.get()) continue;
               throw new IllegalStateException("Timed out to populate the cache");
            }
            break;
         }

         CompletableFuture.allOf(cfs).get(3, TimeUnit.MINUTES);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
         throw new RuntimeException(e);
      }
   }

   private DefaultCacheManager createCacheManager(int index) throws IOException {
      ConfigurationBuilderHolder holder = parseConfiguration(cfg);
      GlobalConfigurationBuilder builder = holder.getGlobalConfigurationBuilder();
      builder.serialization().addContextInitializer(new ReproducerGeneratedSchemaImpl());
      builder.globalState().enabled(true)
            .persistentLocation(Path.of(persistentStatePath, Integer.toString(index)).toAbsolutePath().toString());

      return new DefaultCacheManager(builder.build());
   }

   private static ConfigurationBuilderHolder parseConfiguration(String config) throws IOException {
      try (InputStream is = FileLookupFactory.newInstance().lookupFileStrict(config, Thread.currentThread().getContextClassLoader())) {
         return new ParserRegistry().parse(is, ConfigurationResourceResolvers.DEFAULT, MediaType.APPLICATION_XML);
      }
   }

   @TearDown
   public void shutdownState() {
      Arrays.stream(mgrs).forEach(DefaultCacheManager::stop);
      Util.recursiveFileRemove(persistentStatePath);
   }

   public Cache<String, CacheValue> getCache() {
      int offset = cacheRequestCount.getAndIncrement();
      return caches[offset % caches.length];
   }

   public long startInterval() {
      return startInterval;
   }

   public long endInterval() {
      return endInterval;
   }

   @ProtoSchema(
         schemaPackageName = "reproducer",
         includeClasses = CacheValue.class
   )
   public interface ReproducerGeneratedSchema extends GeneratedSchema { }
}

