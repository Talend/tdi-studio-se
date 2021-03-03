// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.utils;

/**
 * DataConnection describe the data need to make a connection of a DataBase.
 *
 * $Id$
 *
 * @Deprecated by 7052
 */
public class DataConnection {

    private String label;

    private String string;

    private String regex;

    private String defaultPort;

    /**
     * DOC ocarbone DataConnection constructor comment.
     *
     * @param label
     * @param string
     * @param regex
     * @param defaultPort
     */
    public DataConnection(String label, String string, String regex, String defaultPort) {
        super();
        this.label = label;
        this.string = string;
        this.regex = regex;
        this.defaultPort = defaultPort;
    }

    /**
     * DOC ocarbone DataConnection constructor comment.
     *
     * @param label
     * @param string
     * @param regex
     */
    public DataConnection(String label, String string, String regex) {
        this(label, string, regex, null);
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the label.
     *
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Getter for regex.
     *
     * @return the regex
     */
    public String getRegex() {
        return this.regex;
    }

    /**
     * Sets the regex.
     *
     * @param regex the regex to set
     */
    public void setRegex(final String regex) {
        this.regex = regex;
    }

    /**
     * Getter for string.
     *
     * @return the string
     */
    public String getString() {
        return this.string;
    }

    /**
     * Sets the string.
     *
     * @param string the string to set
     */
    public void setString(final String string) {
        this.string = string;
    }

    /**
     * Getter for defaultPort.
     *
     * @return the defaultPort
     */
    public String getDefaultPort() {
        return this.defaultPort;
    }

    /**
     * Sets the defaultPort.
     *
     * @param defaultPort the defaultPort to set
     */
    public void setDefaultPort(final String defaultPort) {
        this.defaultPort = defaultPort;
    }

}
