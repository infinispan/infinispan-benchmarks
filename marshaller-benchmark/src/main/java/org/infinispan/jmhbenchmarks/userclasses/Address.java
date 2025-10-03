package org.infinispan.jmhbenchmarks.userclasses;

import java.io.Serializable;
import java.util.Objects;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

@ProtoTypeId(800000)
public class Address implements Serializable {

   private String street;
   private String postCode;
   private int number;
   private boolean isCommercial;

   @ProtoFactory
   public Address(String street, String postCode, int number, boolean commercial) {
      this.street = street;
      this.postCode = postCode;
      this.number = number;
      this.isCommercial = commercial;
   }

   @ProtoField(1)
   public String getStreet() {
      return street;
   }

   public void setStreet(String street) {
      this.street = street;
   }

   @ProtoField(2)
   public String getPostCode() {
      return postCode;
   }

   public void setPostCode(String postCode) {
      this.postCode = postCode;
   }

   @ProtoField(value = 3, defaultValue = "1")
   public int getNumber() {
      return number;
   }

   public void setNumber(int number) {
      this.number = number;
   }

   @ProtoField(value = 4, defaultValue = "false")
   public boolean isCommercial() {
      return isCommercial;
   }

   public void setCommercial(boolean isCommercial) {
      this.isCommercial = isCommercial;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Address address = (Address) o;
      return number == address.number &&
            isCommercial == address.isCommercial &&
            Objects.equals(street, address.street) &&
            Objects.equals(postCode, address.postCode);
   }

   @Override
   public int hashCode() {
      return Objects.hash(street, postCode, number, isCommercial);
   }

   @Override
   public String toString() {
      return "Address{" +
            "street='" + street + '\'' +
            ", postCode='" + postCode + '\'' +
            ", number=" + number +
            ", isCommercial=" + isCommercial +
            '}';
   }
}
