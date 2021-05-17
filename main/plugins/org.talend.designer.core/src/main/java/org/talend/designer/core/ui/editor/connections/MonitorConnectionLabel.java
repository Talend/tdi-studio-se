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
package org.talend.designer.core.ui.editor.connections;

import org.talend.core.model.process.Element;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public class MonitorConnectionLabel extends Element {

    private String labelText = "Monitored"; //$NON-NLS-1$

    private Connection parent = null;

    public MonitorConnectionLabel(Connection parent) {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return parent.getElementName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    public boolean isReadOnly() {
        return parent.isReadOnly();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) {
        parent.setReadOnly(readOnly);

    }

    /**
     * Get the connection parent of the label.
     *
     * @return Connection
     */
    public Connection getConnection() {
        return parent;
    }

    /**
     * Getter for labelText.
     *
     * @return the labelText
     */
    public String getLabelText() {
        return this.labelText;
    }

}
