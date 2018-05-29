package net.sf.json.sample;

import java.util.HashMap;
import java.util.Map;

public class JSONTestBean {
   private String email = "";
   private String inventoryID = "";
   private String notes = "";
   private Map options = new HashMap();
   private String rateID = "";

   public String getEmail() {
      return email;
   }

   public String getInventoryID() {
      return inventoryID;
   }

   public String getNotes() {
      return notes;
   }

   public Map getOptions() {
      return options;
   }

   public String getRateID() {
      return rateID;
   }

   public void setEmail( String email ) {
      this.email = email;
   }

   public void setInventoryID( String inventoryID ) {
      this.inventoryID = inventoryID;
   }

   public void setNotes( String notes ) {
      this.notes = notes;
   }

   public void setOptions( Map options ) {
      this.options = options;
   }

   public void setRateID( String rateID ) {
      this.rateID = rateID;
   }
}