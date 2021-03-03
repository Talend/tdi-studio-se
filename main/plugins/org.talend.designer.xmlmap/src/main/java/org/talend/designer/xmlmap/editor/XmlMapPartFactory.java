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
package org.talend.designer.xmlmap.editor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.talend.designer.gefabstractmap.part.ConnectionEditPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.GlobalMapNodeEditPart;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataEditPart;
import org.talend.designer.xmlmap.parts.XmlMapFilterConnectionPart;
import org.talend.designer.xmlmap.parts.XmlMapLookupConnectionPart;
import org.talend.designer.xmlmap.parts.XmlMapVarTablePart;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapPartFactory implements EditPartFactory {

    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;
        if (model instanceof XmlMapData) {
            part = new XmlMapDataEditPart();
        } else if (model instanceof InputXmlTree) {
            part = new InputXmlTreeEditPart();
        } else if (model instanceof OutputXmlTree) {
            part = new OutputXmlTreeEditPart();
        } else if (model instanceof VarTable) {
            part = new XmlMapVarTablePart();
        } else if (model instanceof OutputTreeNode) {
            part = new OutputTreeNodeEditPart();
        } else if (model instanceof GlobalMapNode) {
            part = new GlobalMapNodeEditPart();
        } else if (model instanceof TreeNode) {
            part = new TreeNodeEditPart();
        } else if (model instanceof VarNode) {
            part = new VarNodeEditPart();
        } else if (model instanceof Connection) {
            part = new ConnectionEditPart();
        } else if (model instanceof LookupConnection) {
            part = new XmlMapLookupConnectionPart();
        } else if (model instanceof FilterConnection) {
            part = new XmlMapFilterConnectionPart();
        }
        if (part != null) {
            part.setModel(model);
        }
        return part;
    }

}
