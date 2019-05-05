/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.exception;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author rlamarche
 */
public class LocalizedException extends Exception {

    protected Object[] arguments;
    private static ResourceBundle messages;


    static {
        messages = ResourceBundle.getBundle("messages");
    }

    /**
     * Creates a new instance of <code>LocalizedException</code> without detail message.
     */
    public LocalizedException() {
    }

    public LocalizedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public LocalizedException(String msg, Object[] arguments, Throwable cause) {
        super(msg, cause);
        this.arguments = arguments;
    }

    public LocalizedException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>LocalizedException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LocalizedException(String msg, Object[] arguments) {
        this(msg);
        this.arguments = arguments;
    }

    @Override
    public String getLocalizedMessage() {
        String msg = getMessage();
        if (msg != null) {
            msg = messages.getString(msg);
            if (msg == null) {
                return getMessage();
            } else {
                if (arguments == null) {
                    return msg;
                } else {
                    return MessageFormat.format(msg, arguments);
                }
            }
        } else {
            return null;
        }
    }
}
