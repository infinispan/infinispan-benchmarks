package org.infinispan.jmhbenchmarks;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.test.TestingUtil;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;
import org.jgroups.util.Util;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {
   private static final Log log = LogFactory.getLog(InfinispanHolder.class);

   @Param({"2"})
   private int nodes;
   @Param({"1000000"})
   private int entryAmount;

   @Param({"dist-sync.xml"})
   private String cfg;

   public List<DefaultCacheManager> mgrs = new ArrayList<>();

   private Cache<Object, Object> readCache;
   private Cache<Object, Object> writeCache;

   @Setup
   public void initializeState(KeySequenceGenerator generator) throws IOException {
      System.setProperty("jgroups.tcpping.addresses", Util.getAddress(Util.AddressScope.SITE_LOCAL).getHostName());
      DefaultCacheManager mgr0 = new DefaultCacheManager(cfg);
      readCache = mgr0.getCache();
      writeCache = readCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
      mgrs.add(mgr0);

      log.info(readCache.getCacheConfiguration());

      populateCache(generator);
   }

   private void populateCache(KeySequenceGenerator generator) {
      int writeBatch = 1000;
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
      log.infof("Cache population finished, inserted %d entries with key size %d and value size %d",
                writeCache.size(), generator.getKeyObjectSize(), generator.getValueObjectSize());
   }

   @TearDown
   public void shutdownState() {
      for (DefaultCacheManager cm : mgrs) {
         cm.stop();
      }
   }

   @Setup(Level.Invocation)
   public void delay() throws InterruptedException {
      // Delay between iterations to avoid merges in the next iteration
      Thread.sleep(1000);
   }

   public Cache<Object, Object> getReadCache() {
      return readCache;
   }

   public Cache<Object, Object> getWriteCache() {
      return writeCache;
   }

   public void scaleUp() throws IOException {
      for (int i = 1; i < nodes; i++) {
         DefaultCacheManager mgr = new DefaultCacheManager(cfg);
         mgr.getCache();
         mgrs.add(mgr);

         waitForNoRebalance();
         log.infof("Started node %s", mgr.getAddress());
      }
   }

   public void scaleDown() {
      for (int i = nodes - 1; i >= 1; i--) {
         DefaultCacheManager mgr = mgrs.remove(i);
         mgr.stop();

         waitForNoRebalance();
         log.infof("Stopped node %s", mgr.getAddress());
      }
   }

   private void waitForNoRebalance() {
      List<Cache<Object, Object>> caches = mgrs.stream()
                                               .map(DefaultCacheManager::getCache)
                                               .collect(Collectors.toList());
      TestingUtil.waitForNoRebalance(caches);
   }
}
