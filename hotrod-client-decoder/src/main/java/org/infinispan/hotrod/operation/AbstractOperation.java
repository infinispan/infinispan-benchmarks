package org.infinispan.hotrod.operation;

import java.util.Objects;

import org.infinispan.hotrod.impl.operations.HotRodOperation;
import org.infinispan.hotrod.impl.operations.OperationContext;

import io.netty.buffer.ByteBuf;

public abstract class AbstractOperation {

   protected final OperationContext ctx;

   protected org.infinispan.hotrod.impl.operations.HotRodOperation<String> request;
   protected ByteBuf response;

   protected AbstractOperation(OperationContext ctx) {
      this.ctx = ctx;
   }

   public final HotRodOperation<String> getOperation() {
      return Objects.requireNonNull(request, "Setup was not called!");
   }

   public final ByteBuf resetAndGetResponse() {
      return Objects.requireNonNull(response, "Setup was not called!")
            .readerIndex(0);
   }

   public abstract void setup();

   public abstract boolean isResultCorrect(String result);
}
