/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.wsdl.Message;
import javax.wsdl.Part;

import org.talend.webservice.exception.LocalizedException;

/**
 *
 * @author rlamarche
 */
public class MessageMapperImpl implements MessageMapper {

    /**
     * contains TypeMappers for each parts of the message
     */
    private Map<String, TypeMapper> mappers;
    private Map<String, Integer> partsOrder;
    private Message message;
    private boolean unwrapped;

    protected MessageMapperImpl(Map<String, TypeMapper> mappers, Message message) {
        this.mappers = mappers;
        this.message = message;
        partsOrder = new HashMap<String, Integer>(mappers.size());

        int i = 0;
        for (Part part : (List<Part>) message.getOrderedParts(null)) {
            partsOrder.put(part.getName(), i);
            i++;
        }
    }

    public Message getMessage() {
        return message;
    }

    public Object[] convertToParams(Object value) throws LocalizedException {
        if (value instanceof Map) {
            Map<String, Object> values = (Map<String, Object>) value;
            return convertToParams(values);
        } else if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            return convertToParams(values);
        } else {
            return convertToParams(new Object[]{value});
        }
    }

    private Object[] convertToParams(Map<String, Object> values) throws LocalizedException {
        if (unwrapped) {
            TypeMapper typeMapper = mappers.entrySet().iterator().next().
                    getValue();

            if (!(typeMapper instanceof ComplexTypeMapper)) {
                throw new IllegalArgumentException("This message can't be unwrapped.");
            }

            ComplexTypeMapper complexTypeMapper = (ComplexTypeMapper) typeMapper;

            Map.Entry<String, Object> entry = values.entrySet().iterator().next();
            if (!(entry.getValue() instanceof Map)) {
                throw new IllegalArgumentException("These params can't be unwrapped.");
            }
            return complexTypeMapper.convertToTypeUnwrapped((Map) entry.getValue());
        } else {
            Object[] params = new Object[mappers.size()];
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                TypeMapper typeMapper = mappers.get(entry.getKey());
                if (typeMapper == null) {
                    // TODO skip and warn ?
                } else {
                    params[partsOrder.get(entry.getKey())] = typeMapper.
                            convertToType(entry.getValue());
                }
            }

            return params;
        }
    }

    private Object[] convertToParams(Object[] values) throws LocalizedException {
        if (values == null) {
            return null;
        }
        if (values.length == 0) {
            return null;
        }
        if (unwrapped) {
            TypeMapper typeMapper = mappers.entrySet().iterator().next().
                    getValue();

            if (!(typeMapper instanceof ComplexTypeMapper)) {
                throw new IllegalArgumentException("This message can't be unwrapped.");
            }

            ComplexTypeMapper complexTypeMapper = (ComplexTypeMapper) typeMapper;

            Object value = values[0];
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException("These params can't be unwrapped.");
            }
            return complexTypeMapper.convertToTypeUnwrapped((Map) value);
        } else {
            List<Part> orderedParts = message.getOrderedParts(null);
            Object[] params = new Object[orderedParts.size()];
            int i = 0;
            for (Part part : orderedParts) {
                if (i >= values.length) {
                    break;
                }

                params[i] = mappers.get(part.getName()).convertToType(values[i]);
                i++;
            }

            return params;
        }
    }

    public Map<String, Object> convertToValue(Object params[]) throws LocalizedException {
        if (params == null || params != null && params.length == 0) {
            return null;
        }
        if (unwrapped) {
            Part part = (Part) message.getOrderedParts(null).get(0);
            if (part == null) {
                return null;
            }
            TypeMapper typeMapper = mappers.entrySet().iterator().next().
                    getValue();

            if (!(typeMapper instanceof ComplexTypeMapper)) {
                throw new IllegalArgumentException("This message can't be unwrapped.");
            }

            ComplexTypeMapper complexTypeMapper = (ComplexTypeMapper) typeMapper;

            Map<String, Object> values = complexTypeMapper.typeToValueUnwrapped(params);

            Map<String, Object> wrappedValue = new HashMap<String, Object>(1);

            wrappedValue.put(part.getName(), values);

            return wrappedValue;
        } else {
            List<Part> orderedParts = message.getOrderedParts(null);
            Map<String, Object> values = new LinkedHashMap<String, Object>(params.length);
            int i = 0;
            for (Object param : params) {
                Part part = orderedParts.get(i);
                if (part == null) {
                    throw new IllegalArgumentException("Too much params.");
                }
                TypeMapper typeMapper = mappers.get(part.getName());
                values.put(part.getName(), typeMapper.typeToValue(param));
                i++;
            }

            return values;
        }
    }

    public boolean isUnwrapped() {
        return unwrapped;
    }

    public void setUnwrapped(boolean unwrapped) {
        this.unwrapped = unwrapped;
    }
}
