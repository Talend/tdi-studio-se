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

import org.talend.core.model.runprocess.data.SparkBatchPerformance;

/**
 * Control the statistical message that display on iterate link.
 */
public class SparkBatchConnectionPerformance extends ConnectionPerformance {

    private SparkBatchPerformance batchPerformance;

    /**
     * DOC hcw IterateConnectionPerformance constructor comment.
     *
     * @param conn
     */
    public SparkBatchConnectionPerformance(Connection conn) {
        super(conn);
        this.batchPerformance = new SparkBatchPerformance(conn.getLineStyle());
    }

    @Override
    public void resetStatus() {
        batchPerformance.resetStatus();
    }

    @Override
    public void setLabel(String msg) {
        String oldLabel = label;
        label = batchPerformance.getLabel(msg);
        firePropertyChange(LABEL_PROP, oldLabel, label);
    }
}
