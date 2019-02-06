package org.talend.mq.headers.rfh2;

public class McdArea extends RFH2Area {
	public McdArea() {
		super("mcd");
	}
 
	public McdArea(String folderName) {
	    super(folderName);
	}

 public String getMessageDomain() {
         return (String) properties.get("Msd");
 }
 public void setMessageDomain(String messageDomain) {
         properties.put("Msd", messageDomain);
 }
 public String getMessageSet() {
         return (String) properties.get("Set");
 }
 public void setMessageSet(String messageSet) {
         properties.put("Set", messageSet);
 }
 public String getMessageType() {
         return (String) properties.get("Type");
 }
 public void setMessageType(String messageType) {
         properties.put("Type", messageType);
 }
 public String getOutputFormat() {
         return (String) properties.get("Fmt");
 }
 public void setOutputFormat(String outputFormat) {
         properties.put("Fmt", outputFormat);
 }

}
