package org.infinispan.protostream.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.userclasses.Address;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@State(Scope.Benchmark)
public class AddressState {
   private Address address;
   private byte[] addressBytes;

   private List<Address> addressesForUser;
   private ByteBuf addressByteBuf;

   @Setup
   public void setup(ContextState contextState) throws IOException {
      address = new Address("That street", "1234-567", 1024, true);

      addressesForUser = new ArrayList<>(4);
      addressesForUser.add(new Address("This street", "1234-678", 256, false));
      addressesForUser.add(new Address("The other street", "1234-789", 1, true));
      addressesForUser.add(new Address("Yet another street", "1111-222", 56, true));
      addressesForUser.add(new Address("Out of street", "3333-111", 1, true));

      addressBytes = ProtobufUtil.toWrappedByteArray(contextState.getCtx(), address);
      addressByteBuf = Unpooled.wrappedBuffer(addressBytes);
   }

   public Address getAddress() {
      return address;
   }

   public byte[] getAddressBytes() {
      return addressBytes;
   }

   public List<Address> getAddressesForUser() {
      return addressesForUser;
   }

   public ByteBuf getAddressByteBuf() {
      return addressByteBuf.slice();
   }
}
