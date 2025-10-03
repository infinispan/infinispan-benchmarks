package org.infinispan.jmhbenchmarks;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashSet;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.marshall.StreamAwareMarshaller;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.jboss.marshalling.commons.GenericJBossMarshaller;
import org.infinispan.jmhbenchmarks.userclasses.Address;
import org.infinispan.jmhbenchmarks.userclasses.BenchmarkSerializationContextInitializer;
import org.infinispan.jmhbenchmarks.userclasses.User;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.core.impl.DelegatingUserMarshaller;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class InfinispanHolder {

    @Param({"protostream", "jbmar"})
    String marshallerType;

    private EmbeddedCacheManager cacheManager;
    private Marshaller marshaller;
    private StreamAwareMarshaller streamAwareMarshaller;

    @Setup
    public void initializeState() {
        GlobalConfigurationBuilder globalConfigBuilder = new GlobalConfigurationBuilder();
        globalConfigBuilder.defaultCacheName("default");
        switch (marshallerType) {
            case "protostream" -> globalConfigBuilder.serialization().marshaller(new ProtoStreamMarshaller());
            case "jbmar" -> globalConfigBuilder.serialization().marshaller(new GenericJBossMarshaller());
            default -> throw new IllegalStateException("Unknown marshaller type: " + marshallerType);
        }
        globalConfigBuilder.serialization().allowList()
                .addClasses(User.class, Address.class, User.Gender.class, Instant.class, HashSet.class);
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
        marshaller = componentRegistry.getComponent(Marshaller.class, KnownComponentNames.USER_MARSHALLER);
        if (marshaller instanceof DelegatingUserMarshaller dum)
            marshaller = dum.getDelegate();

        if (marshaller instanceof StreamAwareMarshaller sam) {
           streamAwareMarshaller = sam;
        }

        if (marshaller instanceof ProtoStreamMarshaller psm)
            psm.register(BenchmarkSerializationContextInitializer.INSTANCE);
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
