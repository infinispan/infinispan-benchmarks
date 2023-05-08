package org.infinispan.protostream.state;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.userclasses.User;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class UserState {
   @Param({"10", "8096"})
   int userByteArraySize;
   private User user;
   private byte[] userBytes;
   @Setup
   public void setup(ContextState contextState, AddressState addressState) throws IOException {
      byte[] data = new byte[userByteArraySize];
      Random random = new Random(732183718);
      random.nextBytes(data);
      Set<Integer> accounts = new HashSet<>();
      IntStream.range(10, 20).forEach(accounts::add);
      user = new User(10, "John", "Doe", accounts, addressState.getAddressesForUser(), 18,
            User.Gender.MALE, null, Instant.now(), Instant.now().plusMillis(TimeUnit.DAYS.toMillis(30)), data);

      userBytes = ProtobufUtil.toWrappedByteArray(contextState.getCtx(), user);
   }

   public User getUser() {
      return user;
   }

   public byte[] getUserBytes() {
      return userBytes;
   }
}
