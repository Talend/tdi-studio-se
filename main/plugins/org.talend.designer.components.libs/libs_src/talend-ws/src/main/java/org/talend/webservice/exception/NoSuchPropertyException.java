/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.talend.webservice.exception;

/**
 *
 * @author rlamarche
 */
public class NoSuchPropertyException extends LocalizedException {
    
    /**
     * Constructs an instance of <code>NoSuchPropertyException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchPropertyException(String property, String clazz, NoSuchMethodException ex) {
        super("org.talend.ws.exception.NoSuchPropertyException", new String[] {property, clazz}, ex);
    }
}
