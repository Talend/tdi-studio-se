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
package org.talend.designer.core.ui.editor.nodes;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;

/**
 * Tracker that allow to move the node's label. <br/>
 *
 * $Id$
 *
 */
public class NodeTextTracker extends DragEditPartsTracker {

    EditPart part;

    /**
     * Gives the Node Part and the NodeLabelEditPart to the tracker.
     *
     * @param source
     * @param node
     */
    public NodeTextTracker(EditPart source, EditPart part) {
        super(source);
        this.part = part;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.TargetingTool#getTargetEditPart()
     */
    protected EditPart getTargetEditPart() {
        return part;
    }
}
