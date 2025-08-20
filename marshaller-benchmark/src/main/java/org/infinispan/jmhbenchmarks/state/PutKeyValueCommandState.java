package org.infinispan.jmhbenchmarks.state;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.SecureRandom;

import org.infinispan.commands.CommandInvocationId;
import org.infinispan.commands.write.PutKeyValueCommand;
import org.infinispan.jmhbenchmarks.InfinispanHolder;
import org.infinispan.metadata.Metadata;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.ByteString;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class PutKeyValueCommandState {

    @Param({"false", "true"})
    private boolean useUserObjectValue;

    private PutKeyValueCommand putKeyValueCommand;
    private byte[] putKeyValueCommandBytes;

    @Setup
    public void setup(InfinispanHolder holder, UserState userState) throws Exception {
        Object value;
        if (useUserObjectValue) {
            value = userState.getUser();
        } else {
            byte[] randomBytes = new byte[userState.userByteArraySize];
            new SecureRandom().nextBytes(randomBytes);
            value = randomBytes;
        }
        Object key = 23;
        putKeyValueCommand = createPutKeyValueCommand(key, value);
        putKeyValueCommandBytes = holder.getMarshaller().objectToByteBuffer(putKeyValueCommand);
    }

    private static PutKeyValueCommand createPutKeyValueCommand(Object key, Object value) {
        try {
            Address address = createAddress();
            CommandInvocationId commandInvocationId = CommandInvocationId.generateId(address);

            // Try Infinispan 16 constructor first (ByteString, Object, Object)
            try {
                Constructor<PutKeyValueCommand> ctor = PutKeyValueCommand.class.getConstructor(
                        ByteString.class, Object.class, Object.class, boolean.class, boolean.class,
                        Metadata.class, int.class, long.class, CommandInvocationId.class);
                return ctor.newInstance(ByteString.fromString("test"), key, value, false, false, null, 0, 23L, commandInvocationId);
            } catch (NoSuchMethodException e) {
                // Try Infinispan 15 constructor (Object, Object)
                try {
                    Constructor<PutKeyValueCommand> ctor = PutKeyValueCommand.class.getConstructor(
                            Object.class, Object.class, boolean.class, boolean.class,
                            Metadata.class, int.class, long.class, CommandInvocationId.class);
                    return ctor.newInstance(key, value, false, false, null, 0, 23L, commandInvocationId);
                } catch (NoSuchMethodException e2) {
                    // Add outer exception as suppressed
                    e2.addSuppressed(e);
                    throw new RuntimeException("Failed to create PutKeyValueCommand for Infinispan 15", e2);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PutKeyValueCommand", e);
        }
    }

    private static Address createAddress() {
        try {
            // Try to use Infinispan 15 JGroupsAddress
            Class<?> jgroupsAddressClass = Class.forName("org.infinispan.remoting.transport.jgroups.JGroupsAddress");
            Class<?> uuidClass = Class.forName("org.jgroups.util.UUID");
            Object uuid = uuidClass.getMethod("randomUUID").invoke(null);
            return (Address) jgroupsAddressClass.getConstructor(Class.forName("org.jgroups.Address")).newInstance(uuid);
        } catch (Exception e) {
            // Try to use Infinispan 16 Address.random()
            try {
                Class<?> addressClass = Class.forName("org.infinispan.remoting.transport.Address");
                Method randomMethod = addressClass.getMethod("random", String.class);
                return (Address) randomMethod.invoke(null, "benchmark-node");
            } catch (Exception ex) {
                ex.addSuppressed(e);
                throw new RuntimeException("Failed to create Address for either Infinispan 15 or 16", ex);
            }
        }
    }

    public PutKeyValueCommand getPutKeyValueCommand() {
        return putKeyValueCommand;
    }

    public byte[] getPutKeyValueCommandBytes() {
        return putKeyValueCommandBytes;
    }
}
