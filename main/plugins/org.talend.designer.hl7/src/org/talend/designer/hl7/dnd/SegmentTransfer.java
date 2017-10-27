// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.dnd;

import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TreeItem;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: TableEntriesTransfer.java 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class SegmentTransfer extends ByteArrayTransfer {

    private MessageToSchemaDraggedData draggedData;

    private TreeItem dragedItem;

    private static final String SEGMENT_TO_SCHEMA_NAME = "SEGMENT_TO_SCHEMA_NAME"; //$NON-NLS-1$

    private static final int SEGMENT_TO_SCHEMA_NAME_ID = registerType(SEGMENT_TO_SCHEMA_NAME);

    private static final SegmentTransfer INSTANCE = new SegmentTransfer();

    private List<TreeItem> dragedItemList;

    public static SegmentTransfer getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { SEGMENT_TO_SCHEMA_NAME_ID };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { SEGMENT_TO_SCHEMA_NAME };
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

    public MessageToSchemaDraggedData getDraggedData() {
        return this.draggedData;
    }

    public void setDraggedData(MessageToSchemaDraggedData draggedData) {
        this.draggedData = draggedData;
    }

    public TreeItem getDragedItem() {
        return this.dragedItem;
    }

    public void setDragedItem(TreeItem dragedItem) {
        this.dragedItem = dragedItem;
    }

    /**
     * Getter for dragedItemList.
     * 
     * @return the dragedItemList
     */
    public List<TreeItem> getDragedItemList() {
        return this.dragedItemList;
    }

    /**
     * Sets the dragedItemList.
     * 
     * @param dragedItemList the dragedItemList to set
     */
    public void setDragedItemList(List<TreeItem> dragedItemList) {
        this.dragedItemList = dragedItemList;
    }

}
