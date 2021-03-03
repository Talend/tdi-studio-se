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
package org.talend.designer.webservice.ui.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.talend.designer.rowgenerator.data.Parameter;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WebServiceTransfer extends ByteArrayTransfer {

    private static final String WEBSERVICE_TABLE_ENTRIES_TYPE_NAME = "WEBSERVICE_TABLE_ENTRIES"; //$NON-NLS-1$

    private static final int WEBSERVICE_TABLE_ENTRIES_ID = registerType(WEBSERVICE_TABLE_ENTRIES_TYPE_NAME);

    private Parameter parameter;

    private static final WebServiceTransfer INSTANCE = new WebServiceTransfer();

    public static WebServiceTransfer getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { WEBSERVICE_TABLE_ENTRIES_ID };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { WEBSERVICE_TABLE_ENTRIES_TYPE_NAME };
    }

    protected Parameter getParameter() {
        return this.parameter;
    }

    protected void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

}
