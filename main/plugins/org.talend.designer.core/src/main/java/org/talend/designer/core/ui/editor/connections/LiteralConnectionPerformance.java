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

import org.talend.core.model.runprocess.data.LiteralPerformance;

/**
 * DOC xtan Control the statistical message that display on "OnComponentOK/OnComponentError/OnSubJobOK/OnSubJobError/If"
 * link. (feature No.3443: show "true/false" on If trigger Links , show "ok/error" on OK / Error links)
 */
public class LiteralConnectionPerformance extends ConnectionPerformance {

    private LiteralPerformance literalPerformance;

    /**
     * DOC xtan LiteralConnectionPerformance constructor comment.
     *
     * @param conn
     */
    public LiteralConnectionPerformance(Connection conn) {
        super(conn);
        this.literalPerformance = new LiteralPerformance(conn.getLineStyle());
    }

    @Override
    protected void resetStatus() {
        super.resetStatus();
    }

    /**
     * msg like this style: if1|true, if1|false, OnComponentOK1|ok, OnComponentError1|error.
     */
    @Override
    public void setLabel(String msg) {
        String oldLabel = label;
        label = literalPerformance.getLabel(msg);
        firePropertyChange(LABEL_PROP, oldLabel, label);
    }
}
