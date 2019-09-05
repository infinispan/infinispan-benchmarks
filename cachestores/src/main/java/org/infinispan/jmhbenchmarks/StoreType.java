package org.infinispan.jmhbenchmarks;

import java.io.File;

import org.infinispan.commons.util.Util;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.cache.SingleFileStoreConfiguration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfiguration;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfiguration;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfigurationBuilder;

/**
 * @author wburns
 * @since 9.0
 */
public enum StoreType {
   SINGLE {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addSingleFileStore().location(dataRoot + "SFS");
      }

      @Override
      void tearDown() {
         Util.recursiveFileRemove(new File(dataRoot));
      }
   },
   SOFT_INDEX {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addStore(SoftIndexFileStoreConfigurationBuilder.class)
               .dataLocation(dataRoot + "SIFS/data")
               .indexLocation(dataRoot + "SIFS/index");
      }

      @Override
      void tearDown() {
         Util.recursiveFileRemove(new File(dataRoot));
      }
   },
   ROCKS {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addStore(RocksDBStoreConfigurationBuilder.class)
               .location(dataRoot + "ROCKS/data")
               .expiredLocation(dataRoot + "ROCKS/expired");
      }

      @Override
      void tearDown() {
         Util.recursiveFileRemove(new File(dataRoot));
      }
   };

   abstract AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder);
   abstract void tearDown();

   String dataRoot = "Infinispan-Data/";
}
