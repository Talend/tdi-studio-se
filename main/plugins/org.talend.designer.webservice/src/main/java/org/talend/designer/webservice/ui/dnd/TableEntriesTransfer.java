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
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TableItem;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TableEntriesTransfer.java 24712 2009-05-13 12:22:19Z amaumont $
 *
 */
public class TableEntriesTransfer extends ByteArrayTransfer {

    private TableItem tableItem;

    private ParameterInfo parameter;

    private static final String MAPPER_TABLE_ENTRIES_TYPE_NAME = "MAPPER_TABLE_ENTRIES"; //$NON-NLS-1$

    private static final int MAPPER_TABLE_ENTRIES_ID = registerType(MAPPER_TABLE_ENTRIES_TYPE_NAME);

    private static final TableEntriesTransfer INSTANCE = new TableEntriesTransfer();

    public static TableEntriesTransfer getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { MAPPER_TABLE_ENTRIES_ID };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { MAPPER_TABLE_ENTRIES_TYPE_NAME };
    }

    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        // FIX for issue 1225
        super.javaToNative(new byte[1], transferData);
    }

    @Override
    protected Object nativeToJava(TransferData transferData) {
        return new byte[0];
    }

    public ParameterInfo getParameter() {
        return this.parameter;
    }

    public void setParameter(ParameterInfo parameter) {
        this.parameter = parameter;
    }

    public TableItem getTableItem() {
        return this.tableItem;
    }

    public void setTableItem(TableItem tableItem) {
        this.tableItem = tableItem;
    }

}
