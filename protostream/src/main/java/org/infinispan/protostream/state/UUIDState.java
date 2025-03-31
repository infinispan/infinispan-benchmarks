package org.infinispan.protostream.state;

import org.infinispan.protostream.ProtobufUtil;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.UUID;

@State(Scope.Benchmark)
public class UUIDState {

    private UUID uuid;
    private byte[] uuidBytes;

    @Setup
    public void setup(ContextState contextState) throws IOException {
        uuid = UUID.randomUUID();
        uuidBytes = ProtobufUtil.toWrappedByteArray(contextState.getCtx(), uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getUuidBytes() {
        return uuidBytes;
    }
}
