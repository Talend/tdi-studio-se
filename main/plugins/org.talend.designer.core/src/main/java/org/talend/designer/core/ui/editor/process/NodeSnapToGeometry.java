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
package org.talend.designer.core.ui.editor.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SnapToGeometry;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class NodeSnapToGeometry extends SnapToGeometry {

    /**
     * Class that allows to use SnapToGeometry on NodePart only, not on NodeLabelEditPart. <br/>
     *
     * @param container
     */
    public NodeSnapToGeometry(GraphicalEditPart container) {
        super(container);
        List<EditPart> exl = new ArrayList<EditPart>();
        List children = container.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (!(children.get(i) instanceof NodePart)) {
                exl.add((EditPart) children.get(i));
            }
        }
        populateRowsAndCols(generateSnapPartsList(exl));
    }

}
