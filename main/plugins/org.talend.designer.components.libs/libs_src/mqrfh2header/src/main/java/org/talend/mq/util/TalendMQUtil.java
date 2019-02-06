package org.talend.mq.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.mq.headers.rfh2.UsrArea;

public class TalendMQUtil {
	
	/**
	 * set the key, value in the properites to the UserArea
	 * @param usrArea
	 * @param properties
	 */
	public static void setDefinedPropertiesToUser(UsrArea usrArea, Map<String, Object> properties) {
		Iterator<Map.Entry<String, Object>> propsIter = properties.entrySet().iterator();
		while (propsIter.hasNext()) {
			Entry<String,Object> entry =  propsIter.next();
			// ignore the keys which doesn't start with "usr"
//			if (!(entry.getKey().toString().startsWith(usrArea.getAreaName()))) {
//				continue;
//			}
			String key = entry.getKey(); //entry.getKey().substring(4);
			Object value = entry.getValue();

			if (value instanceof String) {
				usrArea.setStringProperty(key, value.toString());
			} else if (value instanceof Integer) {
				usrArea.setIntProperty(key, (Integer)value);
			} else if (value instanceof Long) {
				usrArea.setLongProperty(key, (Long)value);
			} else if (value instanceof Short) {
				usrArea.setShortProperty(key, (Short)value);
			} else if (value instanceof Byte) {
				usrArea.setByteProperty(key, (Byte)value);
			} else if (value instanceof Float) {
				usrArea.setFloatProperty(key, (Float)value);
			} else if (value instanceof Double) {
				usrArea.setDoubleProperty(key,(Double)value);
			} else if (value instanceof Boolean) {
				usrArea.setBooleanProperty(key, (Boolean)value);
			} else {
				throw new RuntimeException("Illegal property type:" + value);
			}
		}
	}

}
