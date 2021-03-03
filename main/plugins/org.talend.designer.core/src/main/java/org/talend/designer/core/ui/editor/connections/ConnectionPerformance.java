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

import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.Element;
import org.talend.core.model.runprocess.data.CommonPerformance;

/**
 * Model part of connection performance.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ConnectionPerformance.java 下午02:22:20 2007-6-8 +0000 (2007-6-8) yzhang $
 *
 */
public class ConnectionPerformance extends Element {

    public static final String LABEL_PROP = "label.prop"; //$NON-NLS-1$

    protected String label = ""; //$NON-NLS-1$

    protected Point offset;

    protected Connection connection;

    /**
     * Constructor.
     *
     * yzhang ConnectionPerformance constructor comment.
     */
    public ConnectionPerformance(Connection conn) {
        connection = conn;
        offset = new Point(0, -30);
    }

    /**
     * Sets the label.
     *
     * @param label the label to set
     */
    public void setLabel(String msg) {
        String oldData = this.label;
        if (!oldData.equals(msg)) {
            label = htmlFromPerformance(msg);
            firePropertyChange(LABEL_PROP, oldData, label);
        }
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
     * Get offset.
     *
     * yzhang Comment method "getLocation".
     *
     * @return
     */
    public Point getOffset() {
        return offset;
    }

    /**
     * Format performance to a simple HTML.
     *
     * @param data Performance data to be rendered.
     * @return HTML string.
     */
    private String htmlFromPerformance(String data) {
        return new CommonPerformance(connection.getLineStyle()).getLabel(data);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return connection.getElementName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    public boolean isReadOnly() {
        return connection.isReadOnly();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) {
        // do nothing.
    }

    protected void resetStatus() {
        // do nothing now
    }

    /**
     * DOC xtan After every iterate, there should clear the last data on the link lable.(If/OnComponentOK/row/...).
     */
    public void clearPerformanceDataOnUI() {
        setLabel(""); //$NON-NLS-1$
    }

}
