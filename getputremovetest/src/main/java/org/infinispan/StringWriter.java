package org.infinispan;

import java.io.IOException;

public interface StringWriter {
   void writeUTF(String string) throws IOException;
}
