package org.infinispan.jmhbenchmarks;

import java.lang.reflect.Method;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.StreamAwareMarshaller;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {

    private EmbeddedCacheManager cacheManager;
    private Marshaller marshaller;
    private StreamAwareMarshaller streamAwareMarshaller;

    @Setup
    public void initializeState() {
        GlobalConfigurationBuilder globalConfigBuilder = new GlobalConfigurationBuilder();
        globalConfigBuilder.defaultCacheName("default");
        Configuration config = new ConfigurationBuilder().build();
        cacheManager = new DefaultCacheManager(globalConfigBuilder.build());
        cacheManager.defineConfiguration("default", config);
        cacheManager.start();
        Cache<?, ?> cache = cacheManager.getCache("default");
        ComponentRegistry componentRegistry;
        try {
            // Old way for Infinispan 15.2
            Method getComponentRegistryMethod = AdvancedCache.class.getMethod("getComponentRegistry");
            AdvancedCache<?, ?> advancedCache = cache.getAdvancedCache();
            componentRegistry = (ComponentRegistry) getComponentRegistryMethod.invoke(advancedCache);
        } catch (ReflectiveOperationException e) {
            // New way for Infinispan 16.0+
            try {
                Method ofMethod = ComponentRegistry.class.getMethod("of", Cache.class);
                componentRegistry = (ComponentRegistry) ofMethod.invoke(null, cache);
            } catch (ReflectiveOperationException e2) {
                throw new RuntimeException(e2);
            }
        }
        marshaller = componentRegistry.getComponent(Marshaller.class, KnownComponentNames.INTERNAL_MARSHALLER);
        if (marshaller instanceof StreamAwareMarshaller sam) {
           streamAwareMarshaller = sam;
        }
    }

    @TearDown
    public void shutdownState() {
        cacheManager.stop();
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

   public StreamAwareMarshaller getStreamAwareMarshaller() {
      return streamAwareMarshaller;
   }
}
