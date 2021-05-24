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
package org.talend.designer.abstractmap.ui.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TableEntriesTransfer.java 54939 2011-02-11 01:34:57Z mhirt $
 *
 */
public class TableEntriesTransfer extends ByteArrayTransfer {

    private DraggedData draggedData;

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

    public DraggedData getDraggedData() {
        return draggedData;
    }

    public void setDraggedData(DraggedData draggedData) {
        this.draggedData = draggedData;
    }

}
