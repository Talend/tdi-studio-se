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
package org.talend.sqlbuilder.erdiagram.ui.editor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;
import org.talend.sqlbuilder.erdiagram.ui.parts.ColumnPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.ErDiagramPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.RelationPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.TablePart;

/**
 * The qzhang ErDiagramPartFactory will create an EditPart factory for each model object that is created in the diagram.
 * <br/>
 *
 * $Id: PartFactory.java 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */
public class ErDiagramPartFactory implements EditPartFactory {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        if (model instanceof ErDiagram) {
            part = new ErDiagramPart();
        } else if (model instanceof Column) {
            part = new ColumnPart();
        } else if (model instanceof Table) {
            part = new TablePart();
        } else if (model instanceof Relation) {
            part = new RelationPart();

        } else {
            return null;
        }
        // tell the newly created part about the model object
        part.setModel(model);

        return part;
    }
}
