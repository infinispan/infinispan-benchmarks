package org.infinispan.json;

import java.util.UUID;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class JsonSetup {

   @Param({"1024", "524288", "1048576"})
   int stringSize;

   @Param({"new", "main"})
   String type;

   String toEscape;

   @Setup
   public void setup() {
      String uuid = UUID.randomUUID().toString();
      StringBuilder sb = new StringBuilder(uuid);
      while (sb.length() + uuid.length() < stringSize) {
         sb.append(UUID.randomUUID());
      }
      String v = sb.toString();
      v = v.replaceAll("-", "\"");
      toEscape = String.format("\"%s\"", v); // We have something like "33b71d1c"12dd"462a"ad08"466a46789cad"
   }

   public String parse() {
      switch (type) {
         case "new":
            return EscapeNew.help.escape(toEscape);
         case "main":
            return EscapeMain.help.escape(toEscape);
         default:
            throw new IllegalArgumentException("Unknown type: " + type);
      }
   }
}
