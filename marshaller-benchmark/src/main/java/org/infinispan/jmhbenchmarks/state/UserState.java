package org.infinispan.jmhbenchmarks.state;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.infinispan.jmhbenchmarks.InfinispanHolder;
import org.infinispan.jmhbenchmarks.userclasses.User;
import org.infinispan.jmhbenchmarks.util.ResettableInputStream;
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
   private ResettableInputStream userStream;

   @Setup
   public void setup(InfinispanHolder holder, AddressState addressState) throws IOException, InterruptedException {
      byte[] data = new byte[userByteArraySize];
      Random random = new Random(732183718);
      random.nextBytes(data);
      Set<Integer> accounts = new HashSet<>();
      IntStream.range(10, 20).forEach(accounts::add);
      user = new User(10, "John", "Doe", accounts, addressState.getAddressesForUser(), 18,
            User.Gender.MALE, null, Instant.now(), Instant.now().plusMillis(TimeUnit.DAYS.toMillis(30)), data);

      userBytes = holder.getMarshaller().objectToByteBuffer(user);
      userStream = new ResettableInputStream(userBytes, userBytes.length);
   }

   public User getUser() {
      return user;
   }

   public byte[] getUserBytes() {
      return userBytes;
   }

   public ResettableInputStream getUserStream() {
      userStream.restart();
      return userStream;
   }
}
