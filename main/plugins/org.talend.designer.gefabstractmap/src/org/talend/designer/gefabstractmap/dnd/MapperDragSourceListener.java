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
package org.talend.designer.gefabstractmap.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

/**
 * created by Administrator on 2013-1-16 Detailled comment
 *
 */
public abstract class MapperDragSourceListener extends AbstractTransferDragSourceListener {

    public MapperDragSourceListener(EditPartViewer viewer) {
        super(viewer, TemplateTransfer.getInstance());
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        Object template = getTemplate(event);
        TemplateTransfer.getInstance().setTemplate(template);
    }

    /**
     *
     * DOC talend Comment method "getTemplate".
     *
     * @param event
     * @return the validate drag able node list
     */
    protected abstract Object getTemplate(DragSourceEvent event);

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = getTemplate(event);
    }

    @Override
    protected void setTransfer(Transfer xfer) {
        // TODO Auto-generated method stub
        super.setTransfer(xfer);
    }

}
