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
package org.talend.designer.core.ui.editor.subjobcontainer.sparkstreaming;

import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SparkStreamingSubjobContainer extends SubjobContainer {

    private int batchStarted;

    private int batchCompleted;

    private String lastProcessingDelay;

    private String lastSchedulingDelay;

    private String lastTotalDelay;

    public SparkStreamingSubjobContainer(IProcess2 process) {
        super(process);

        this.batchStarted = 0;
        this.batchCompleted = 0;
        this.lastProcessingDelay = ""; //$NON-NLS-1$
        this.lastSchedulingDelay = ""; //$NON-NLS-1$
        this.lastTotalDelay = ""; //$NON-NLS-1$
    }

    public void updateState(final String id, Object value, final int batchCompleted, final int batchStarted,
            final String lastProcessingDelay, final String lastSchedulingDelay, final String lastTotalDelay) {
        if (id.equals("UPDATE_SPARKSTREAMING_STATUS")) { //$NON-NLS-1$
            this.batchCompleted = batchCompleted;
            this.batchStarted = batchStarted;
            this.lastProcessingDelay = lastProcessingDelay;
            this.lastSchedulingDelay = lastSchedulingDelay;
            this.lastTotalDelay = lastTotalDelay;
            firePropertyChange("UPDATE_SPARKSTREAMING_STATUS", null, value); //$NON-NLS-1$
        }
    }

    /**
     * Getter for batchStarted.
     *
     * @return the batchStarted
     */
    public int getBatchStarted() {
        return batchStarted;
    }

    /**
     * Getter for batchCompleted.
     *
     * @return the batchCompleted
     */
    public int getBatchCompleted() {
        return batchCompleted;
    }

    /**
     * Getter for lastProcessingDelay.
     *
     * @return the lastProcessingDelay
     */
    public String getLastProcessingDelay() {
        return lastProcessingDelay;
    }

    /**
     * Getter for lastSchedulingDelay.
     *
     * @return the lastSchedulingDelay
     */
    public String getLastSchedulingDelay() {
        return lastSchedulingDelay;
    }

    /**
     * Getter for lastTotalDelay.
     *
     * @return the lastTotalDelay
     */
    public String getLastTotalDelay() {
        return lastTotalDelay;
    }
}
