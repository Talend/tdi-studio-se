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
package org.talend.designer.core.ui.editor.properties.controllers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class SchemaTypeController extends AbstractSchemaController {

    public SchemaTypeController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = lastControl;
        if (elem instanceof Node) {
            lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        }
        lastControlUsed = addButton(subComposite, param, lastControlUsed, numInRow, top);
        return lastControlUsed;
    }

    @Override
    public IMetadataTable getMetadataTableFromXml(INode node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
        if (param.getValue() instanceof IMetadataTable) {
            IMetadataTable table = (IMetadataTable) param.getValue();
            return table;
        }
        return null;
    }
}
