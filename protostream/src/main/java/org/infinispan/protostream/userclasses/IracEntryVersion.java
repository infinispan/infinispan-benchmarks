package org.infinispan.protostream.userclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

@ProtoTypeId(4)
public class IracEntryVersion {

   private final MapEntry[] vectorClock;

   private IracEntryVersion(MapEntry[] vectorClock) {
      this.vectorClock = vectorClock;
   }

   public IracEntryVersion(Map<String, TopologyIracVersion> vectorClock) {
      this.vectorClock = new MapEntry[vectorClock.size()];
      int i = 0;
      for (Map.Entry<String, TopologyIracVersion> entry : vectorClock.entrySet()) {
         this.vectorClock[i++] = new MapEntry(entry.getKey(), entry.getValue());
      }
   }

   @ProtoFactory
   static IracEntryVersion protoFactory(List<MapEntry> entries) {
      MapEntry[] vc = entries.toArray(new MapEntry[entries.size()]);
      Arrays.sort(vc);
      return new IracEntryVersion(vc);
   }

   @ProtoField(number = 1, collectionImplementation = ArrayList.class)
   List<MapEntry> entries() {
      return Arrays.asList(vectorClock);
   }

   @ProtoTypeId(5)
   public static class MapEntry {

      final String site;

      @ProtoField(2)
      final TopologyIracVersion version;

      @ProtoFactory
      MapEntry(String site, TopologyIracVersion version) {
         this.site = site;
         this.version = version;
      }

      @ProtoField(1)
      public String getSite() {
         return site;
      }
   }
}
