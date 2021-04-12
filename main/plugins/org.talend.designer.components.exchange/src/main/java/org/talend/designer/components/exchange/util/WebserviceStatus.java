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
package org.talend.designer.components.exchange.util;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class WebserviceStatus {

    private boolean result = false;

    private String value;

    private String messageException;

    /**
     * Getter for result.
     *
     * @return the result
     */
    public boolean isResult() {
        return this.result;
    }

    /**
     * Sets the result.
     *
     * @param result the result to set
     */
    public void setResult(boolean result) {
        this.result = result;
    }

    /**
     * Getter for value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter for messageException.
     *
     * @return the messageException
     */
    public String getMessageException() {
        return this.messageException;
    }

    /**
     * Sets the messageException.
     *
     * @param messageException the messageException to set
     */
    public void setMessageException(String messageException) {
        this.messageException = messageException;
    }

}
