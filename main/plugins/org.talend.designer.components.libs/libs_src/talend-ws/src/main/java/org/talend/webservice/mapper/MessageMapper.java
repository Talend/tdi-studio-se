/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.util.Map;
import javax.wsdl.Message;

import org.talend.webservice.exception.LocalizedException;

/**
 * Define the transformation between params and Message parts
 * @author rlamarche
 */
public interface MessageMapper {

    /**
     * @return the targeted message
     */
    public Message getMessage();

    /**
     * Convert value to parts
     * @param value
     * @return
     */
    public Object[] convertToParams(Object value) throws LocalizedException;
    ;

    /**
     * Convert parts to value
     * @param params
     * @return
     */
    public Map<String, Object> convertToValue(Object[] params) throws LocalizedException;

    public boolean isUnwrapped();
    public void setUnwrapped(boolean unwrapped);
}
