// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.projectsetting;


import org.talend.core.model.process.Element;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.utils.ConnectionUtil;

/**
 * this class is only used in projectsetting to contains project's StatsAndLogsSettings
 */
public class StatsAndLogsElement extends Element {

    private static final String STATSANDLOGS_ELEMENT_NAME = "statsAndLogs"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Element#getElementName()
     */
    @Override
    public String getElementName() {
        // TODO Auto-generated method stub
        return STATSANDLOGS_ELEMENT_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPropertyValue(String id, Object value) {
        super.setPropertyValue(id, value);
    }
}
