package org.infinispan.jmhbenchmarks.userclasses;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.infinispan.protostream.annotations.ProtoEnumValue;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

@ProtoTypeId(800001)
public class User {

   public enum Gender {
      @ProtoEnumValue(1)
      MALE,
      @ProtoEnumValue(2)
      FEMALE
   }

   private int id;
   private String name;
   private String surname;
   private Set<Integer> accountIds;
   private List<Address> addresses;
   private Integer age;
   private Gender gender;
   private String notes;
   private Instant creationDate;
   private Instant passwordExpirationDate;

   private byte[] encryptedAcountInfo;

   @ProtoFactory
   public User(int id, String name, String surname, Set<Integer> accountIds, List<Address> addresses, Integer age, Gender gender, String notes, Instant creationDate, Instant passwordExpirationDate, byte[] encryptedAcountInfo) {
      this.id = id;
      this.name = name;
      this.surname = surname;
      this.accountIds = accountIds;
      this.addresses = addresses;
      this.age = age;
      this.gender = gender;
      this.notes = notes;
      this.creationDate = creationDate;
      this.passwordExpirationDate = passwordExpirationDate;
      this.encryptedAcountInfo = encryptedAcountInfo;
   }

   @ProtoField(value = 1, defaultValue = "0")
   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   @ProtoField(value = 2, collectionImplementation = HashSet.class)
   public Set<Integer> getAccountIds() {
      return accountIds;
   }

   public void setAccountIds(Set<Integer> accountIds) {
      this.accountIds = accountIds;
   }

   @ProtoField(3)
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @ProtoField(4)
   public String getSurname() {
      return surname;
   }

   public void setSurname(String surname) {
      this.surname = surname;
   }

   @ProtoField(value = 5, collectionImplementation = ArrayList.class)
   public List<Address> getAddresses() {
      return addresses;
   }

   public void setAddresses(List<Address> addresses) {
      this.addresses = addresses;
   }

   @ProtoField(value = 6, defaultValue = "18")
   public Integer getAge() {
      return age;
   }

   public void setAge(Integer age) {
      this.age = age;
   }

   @ProtoField(7)
   public Gender getGender() {
      return gender;
   }

   public void setGender(Gender gender) {
      this.gender = gender;
   }

   @ProtoField(8)
   public String getNotes() {
      return notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   @ProtoField(value = 9, defaultValue = "0")
   public Instant getCreationDate() {
      return creationDate;
   }

   public void setCreationDate(Instant creationDate) {
      this.creationDate = creationDate;
   }

   @ProtoField(value = 10, defaultValue = "0")
   public Instant getPasswordExpirationDate() {
      return passwordExpirationDate;
   }

   public void setPasswordExpirationDate(Instant passwordExpirationDate) {
      this.passwordExpirationDate = passwordExpirationDate;
   }

   @ProtoField(value = 11)
   public byte[] getEncryptedAcountInfo() {
      return encryptedAcountInfo;
   }

   public void setEncryptedAcountInfo(byte[] encryptedAcountInfo) {
      this.encryptedAcountInfo = encryptedAcountInfo;
   }

   @Override
   public String toString() {
      return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", surname='" + surname + '\'' +
            ", accountIds=" + accountIds +
            ", addresses=" + addresses +
            ", age=" + age +
            ", gender=" + gender +
            ", notes='" + notes + '\'' +
            ", creationDate=" + creationDate +
            ", passwordExpirationDate=" + passwordExpirationDate +
            ", encryptedAcountInfo=" + Arrays.toString(encryptedAcountInfo) +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return id == user.id &&
            Objects.equals(name, user.name) &&
            Objects.equals(surname, user.surname) &&
            Objects.equals(accountIds, user.accountIds) &&
            Objects.equals(addresses, user.addresses) &&
            Objects.equals(age, user.age) &&
            gender == user.gender &&
            Objects.equals(notes, user.notes) &&
            Objects.equals(creationDate, user.creationDate) &&
            Objects.equals(passwordExpirationDate, user.passwordExpirationDate) &&
            Arrays.equals(encryptedAcountInfo, user.encryptedAcountInfo);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, name, surname, accountIds, addresses, age, gender, notes, creationDate, passwordExpirationDate, encryptedAcountInfo);
   }
}
