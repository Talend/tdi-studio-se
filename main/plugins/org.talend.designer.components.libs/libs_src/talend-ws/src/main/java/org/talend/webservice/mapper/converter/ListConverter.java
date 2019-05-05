package org.talend.webservice.mapper.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.Converter;

public class ListConverter implements Converter {

    public Object convert(Class type, Object value) {
        if (value == null) {
            return new ArrayList();
        }
        if (value instanceof List) {
            return value;
        } else {
            List v = new ArrayList();
            v.add(value);
            return v;
        }
    }
}
