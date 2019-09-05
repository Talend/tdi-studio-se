package org.talend.mq.headers.rfh2;

public class JmsArea extends RFH2Area {
    private static final String KEY_DESTINATION = "Dst";
    private static final String KEY_REPLY_TO = "Rto";
    private static final String KEY_TIMESTAMP = "Tms";
    private static final String KEY_EXPIRATION = "Exp";
    private static final String KEY_CORRELATION_ID = "Cid";
    private static final String KEY_PRIORITY = "Pri";
    private static final String KEY_DELIVERY_MODE = "Dlv";
    private static final String KEY_GROUP_ID = "Gid";
    private static final String KEY_GROUP_SEQ = "Seq";
    
    public JmsArea() {
            super("jms");
    }
    
    public JmsArea(String areaName) {
            super(areaName);
    }

    public String getDestination() {
            return (String) properties.get(KEY_DESTINATION);
    }

    public void setDestination(String destination) {
            setProperty(KEY_DESTINATION, destination);
    }

    public String getReplyTo() {
            return (String) properties.get(KEY_REPLY_TO);
    }

    public void setReplyTo(String replyTo) {
            setProperty(KEY_REPLY_TO, replyTo);
    }

    public long getExpiration() {
            return returnLong(KEY_EXPIRATION, 0);
    }

    public void setExpiration(long expiration) {
            setProperty(KEY_EXPIRATION, Long.toString(expiration));
    }

    public String getCorrelationId() {
            return (String) properties.get(KEY_CORRELATION_ID);
    }

    public void setCorrelationId(String correlationId) {
            setProperty(KEY_CORRELATION_ID, correlationId);
    }


    protected int returnInt(String key, int defaultValue) {
            Object value = properties.get(key);
            
            if(value == null) {
                    return defaultValue;
            } else {
                    return Integer.parseInt((String)value);
            }
    }

    protected long returnLong(String key, long defaultValue) {
            Object value = properties.get(key);
            
            if(value == null) {
                    return defaultValue;
            } else {
                    return Long.parseLong((String)value);
            }
    }
    
    
    public int getPriority() {
            return returnInt(KEY_PRIORITY, 0);
    }

    public void setPriority(int priority) {
            setProperty(KEY_PRIORITY, Integer.toString(priority));
    }

    public int getDeliveryMode() {
            return returnInt(KEY_DELIVERY_MODE, 0);
    }

    public void setDeliveryMode(int deliveryMode) {
            setProperty(KEY_DELIVERY_MODE, Integer.toString(deliveryMode));
    }

    public long getTimestamp() {
            return returnLong(KEY_TIMESTAMP, 0);
    }

    public void setTimestamp(long timestamp) {
            setProperty(KEY_TIMESTAMP, Long.toString(timestamp));
    }
    
    protected boolean includeNullValueInToString() {
            return false;
    }
    
    public String getGroupId() {
    	return (String)properties.get(KEY_GROUP_ID);
    }
    
    public void setGroupId(String gid) {
    	setProperty(KEY_GROUP_ID,gid);
    }
    
    public int getGroupSeq() {
    	return returnInt(KEY_GROUP_SEQ,0);
    }
    
    public void setGroupSeq(int seq) {
    	setProperty(KEY_GROUP_SEQ,Integer.toString(seq));
    }
    
    


}
