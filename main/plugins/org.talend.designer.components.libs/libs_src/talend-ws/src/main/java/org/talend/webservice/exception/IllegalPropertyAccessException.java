/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.talend.webservice.exception;

/**
 *
 * @author rlamarche
 */
public class IllegalPropertyAccessException extends LocalizedException {

    /**
     * Constructs an instance of <code>IllegalPropertyAccessException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalPropertyAccessException(String property, String clazz, IllegalAccessException ex) {
        super("org.talend.ws.exception.IllegalPropertyAccessException", new String[] {property, clazz}, ex);
    }
}
