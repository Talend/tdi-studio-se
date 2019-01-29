package org.talend.webservice.mapper.converter;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.ByteArrayConverter;

public class ConvertTool {

    static byte byteArray[] = new byte[0];

    private static final Map<Class, Converter> converters;
    static {
        converters = new HashMap<Class, Converter>();
        converters.put(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter());
        converters.put(byteArray.getClass(), new ByteArrayConverter());
        converters.put(List.class, new ListConverter());
    }

    public static Object convert(Object value, Class clazz) {
        Converter converter = converters.get(clazz);
        if (converter != null) {
            return converter.convert(clazz, value);
        }

        return ConvertUtils.convert(ConvertUtils.convert(value), clazz);
    }

    private static XMLGregorianCalendar DateConverter(Object value, Class clazz) {
        if (value instanceof XMLGregorianCalendar) {
            return (XMLGregorianCalendar) value;
        } else if (value instanceof Date) {
            try {
                GregorianCalendar c = new GregorianCalendar();
                c.setTime((Date) value);
                XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                return date;
            } catch (javax.xml.datatype.DatatypeConfigurationException ex) {
                ex.printStackTrace();
            }
        } else {
            throw new ConversionException("Should be javax.xml.datatype.XMLGregorianCalendar type or java.util.Date type");
        }
        return null;
    }
}
