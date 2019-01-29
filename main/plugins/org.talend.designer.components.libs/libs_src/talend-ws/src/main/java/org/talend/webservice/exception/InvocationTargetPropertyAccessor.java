/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.exception;

/**
 *
 * @author rlamarche
 */
public class InvocationTargetPropertyAccessor extends LocalizedException {

    /**
     * Constructs an instance of <code>NoSuchPropertyException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvocationTargetPropertyAccessor(String property, String clazz, Throwable ex) {
        super("org.talend.ws.exception.InvocationTargetPropertyAccessor", new String[]{property, clazz}, ex);
    }

    public String getProperty() {
        return (String) arguments[0];
    }

    public String getClazz() {
        return (String) arguments[1];
    }
}
