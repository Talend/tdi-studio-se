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
package org.talend.designer.core.ui.editor.properties.controllers.tdq;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.CheckController;

/**
 * Controller for TGenKey
 */
public class TGenKeyCheckController extends CheckController {

    /**
     * DOC zshen TGenKeyCheckController constructor comment.
     *
     * @param dp
     */
    public TGenKeyCheckController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#executeCommand
     * (org.eclipse.gef.commands.Command)
     */
    @Override
    protected void executeCommand(Command c) {
        // change T_GEN_KEY field
        // because CheckGenerator has a check about this so elem do is a Node.
        super.executeCommand(c);
        Node source = (Node) elem;
        Node target = null;
        String targetNodeName = source.getElementParameter("COMPONENT").getValue().toString();
        Boolean isLink = (Boolean) source.getElementParameter("LINK_WITH").getValue();
        if (StringUtils.isEmpty(targetNodeName)) {
            target = getNextTgenKeyNode(source);
        } else {
            for (INode tempNode : source.getProcess().getGraphicalNodes()) {
                if (tempNode.getElementName().equalsIgnoreCase(targetNodeName)) {
                    target = (Node) tempNode;
                    break;
                }
            }
        }
        if (target != null) {

            changeKeyColumnLabel(target, isLink);
            changeKeyColumnLabel(source, isLink);

        }

    }

    /**
     * DOC zshen Comment method "getNextTgenKeyNode".
     *
     * @param source
     */
    private Node getNextTgenKeyNode(Node source) {
        Node target = null;
        for (INode tempNode : source.getProcess().getGraphicalNodes()) {
            if (tempNode.getComponent().getName().equalsIgnoreCase("tGenKeyHadoop")
                    && !tempNode.getLabel().equals(source.getLabel())) {
                target = (Node) tempNode;
                break;
            }
        }
        return target;

    }

    /**
     * DOC zshen Comment method "changeKeyColumnLabel".
     *
     * @param changeNode
     * @param isLink
     */
    private void changeKeyColumnLabel(Node changeNode, Boolean isLink) {
        if (!isLink) {
            return;
        }
        IElementParameter schemPara = changeNode.getElementParameter(EParameterName.SCHEMA.getName());
        MetadataTable sourceTable = ((MetadataTable) schemPara.getValue());
        IMetadataTable originaleOutputTable = changeNode.getMetadataFromConnector(schemPara.getContext());

        IMetadataColumn genKeyColumn = sourceTable.getColumn("T_GEN_KEY"); //$NON-NLS-1$
        IMetadataColumn originalegenKeyColumn = originaleOutputTable.getColumn("T_GEN_KEY");//$NON-NLS-1$
        int lastIndexOf = changeNode.getLabel().lastIndexOf("_"); //$NON-NLS-1$
        if (genKeyColumn != null) {
            genKeyColumn.setLabel(genKeyColumn.getLabel() + changeNode.getLabel().substring(lastIndexOf));
            if (originalegenKeyColumn != null && !(genKeyColumn.getLabel().equalsIgnoreCase(originalegenKeyColumn.getLabel()))) {
                originalegenKeyColumn.setLabel(originalegenKeyColumn.getLabel() + changeNode.getLabel().substring(lastIndexOf));
            }
        }
    }
}
