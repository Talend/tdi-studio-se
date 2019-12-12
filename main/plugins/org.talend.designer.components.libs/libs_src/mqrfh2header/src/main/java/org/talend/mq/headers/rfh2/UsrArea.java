package org.talend.mq.headers.rfh2;

public class UsrArea extends RFH2Area  {
    public UsrArea() {
        super("usr");
}

public UsrArea(String areaName) {
        super(areaName);
}

public boolean containsProperty(String name) {
        return properties.containsKey(name);
}

public String getStringProperty(String name) {
        Object value = properties.get(name);
        
        if(value == null) {
                return null;
        } else {
                return value.toString();
        }
}

public void setStringProperty(String name, String value) {
        properties.put(name, value);
}

public int getIntProperty(String name) {
        return returnInt(name, 0);
}

public void setIntProperty(String name, int value) {
        setProperty(name, new Integer(value));
}

public long getLongProperty(String name) {
        return returnLong(name, 0);
}

public void setLongProperty(String name, long value) {
        setProperty(name, new Long(value));
}       

public short getShortProperty(String name) {
        return returnShort(name, (short) 0);
}

public void setShortProperty(String name, short value) {
        setProperty(name, new Short(value));
}


public byte getByteProperty(String name) {
        return returnByte(name, (byte) 0);
}

public void setByteProperty(String name, byte value) {
        setProperty(name, new Byte(value));
}


public float getFloatProperty(String name) {
        return returnFloat(name, 0f);
}

public void setFloatProperty(String name, float value) {
        setProperty(name, new Float(value));
}


public double getDoubleProperty(String name) {
        return returnDouble(name, 0d);
}

public void setDoubleProperty(String name, double value) {
        setProperty(name, new Double(value));
}


public boolean getBooleanProperty(String name) {
        return returnBoolean(name, false);
}

public void setBooleanProperty(String name, boolean value) {
        setProperty(name, Boolean.valueOf(value));
}

}
