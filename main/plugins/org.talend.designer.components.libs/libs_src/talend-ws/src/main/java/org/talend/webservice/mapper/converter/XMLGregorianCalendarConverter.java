package org.talend.webservice.mapper.converter;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

public class XMLGregorianCalendarConverter implements Converter {

    public Object convert(Class type, Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof XMLGregorianCalendar) {
            return (value);
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
