/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.exception;

/**
 *
 * @author rlamarche
 */
public class InvalidEnumValueException extends LocalizedException {

    /**
     * Constructs an instance of <code>InvalidEnumValueException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidEnumValueException(String value, String enumeration) {
        super("org.talend.ws.exception.InvalidEnumValueException", new String[]{value, enumeration});
    }

    public String getValue() {
        return (String) arguments[0];
    }

    public String getEnum() {
        return (String) arguments[1];
    }
}
