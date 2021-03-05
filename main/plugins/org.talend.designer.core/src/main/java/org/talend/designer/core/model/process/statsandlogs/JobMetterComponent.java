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
package org.talend.designer.core.model.process.statsandlogs;

import org.talend.core.model.components.EComponentType;
import org.talend.core.model.process.IProcess;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class JobMetterComponent extends AbstractStatsLogsComponent {

    public JobMetterComponent(boolean useFile, boolean useConsole, String dbComponent) {
        if (dbComponent != null) {
            this.useDb = true;
            this.dbComponent = dbComponent;
        }
        this.useFile = useFile;
        this.useConsole = useConsole;
        this.componentId = "METTER"; //$NON-NLS-1$
        this.subComponent = "tFlowMeterCatcher"; //$NON-NLS-1$

        loadMultipleComponentManager();
    }

    public String getVersion() {
        return "0.1"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    public EComponentType getComponentType() {
        return EComponentType.JOB_METTER;
    }

    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

}
