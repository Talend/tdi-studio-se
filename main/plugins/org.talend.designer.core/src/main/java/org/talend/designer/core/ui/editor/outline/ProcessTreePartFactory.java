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
package org.talend.designer.core.ui.editor.outline;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * This class is the main factory of the Tree in the Outline View. <br/>
 *
 * $Id$
 *
 */
public class ProcessTreePartFactory implements EditPartFactory {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof Process) {
            return new ProcessTreeEditPart(model);
        } else if (model instanceof Node) {
            return new NodeTreeEditPart(model);
        } else if (model instanceof NodeReturn) {
            return new NodeReturnsTreeEditPart(model);
        } else {
            return null;
        }
    }
}
