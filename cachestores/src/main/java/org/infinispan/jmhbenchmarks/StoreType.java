package org.infinispan.jmhbenchmarks;

import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder;
import org.infinispan.persistence.sifs.configuration.SoftIndexFileStoreConfigurationBuilder;

/**
 * @author wburns
 * @since 9.0
 */
public enum StoreType {
   SINGLE {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addSingleFileStore();
      }
   },
   SOFT_INDEX {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addStore(SoftIndexFileStoreConfigurationBuilder.class);
      }
   },
   ROCKS {
      @Override
      AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder) {
         return builder.addStore(RocksDBStoreConfigurationBuilder.class);
      }
   },
   ;

   abstract AbstractStoreConfigurationBuilder apply(PersistenceConfigurationBuilder builder);
}
