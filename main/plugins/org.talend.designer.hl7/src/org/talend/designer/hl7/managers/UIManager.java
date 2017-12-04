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
package org.talend.designer.hl7.managers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;

/**
 * UI Manager<br/>
 * 
 * $Id: UIManager.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 * 
 */
public class UIManager {

    protected HL7UI hl7UI;

    private int uiResponse = SWT.NONE;

    protected HL7Manager hl7Manager;

    public UIManager(HL7Manager hl7Manager) {
        this.hl7Manager = hl7Manager;
    }

    public HL7Manager getHl7Manager() {
        return this.hl7Manager;
    }

    public void setHl7Manager(HL7Manager hl7Manager) {
        this.hl7Manager = hl7Manager;
    }

    public HL7UI getHl7UI() {
        return this.hl7UI;
    }

    public void setHl7UI(HL7UI hl7ui) {
        this.hl7UI = hl7ui;
    }

    /**
     * Getter for uiResponse.
     * 
     * @return the uiResponse
     */
    public int getUiResponse() {
        return this.uiResponse;
    }

    /**
     * Sets the uiResponse.
     * 
     * @param uiResponse the uiResponse to set
     */
    public void setUiResponse(int uiResponse) {
        this.uiResponse = uiResponse;
    }

    /**
     * DOC qiang.zhang Comment method "closeFOX".
     * 
     * @param reponse
     */
    public void closeHL7(int response) {

        if (response == SWT.CANCEL) {
            setUiResponse(SWT.CANCEL);
        }

        if (response == SWT.OK) {
            if (saveAllData()) {
                setUiResponse(SWT.OK);
            } else {
                setUiResponse(SWT.CANCEL);
            }
        }
        Composite parent = hl7UI.getHl7UIParent();
        if (parent instanceof Shell) {
            ((Shell) parent).close();
        }

    }

    /**
     * 
     * DOC xzhang Comment method "autoMap".
     */
    public void autoMap(String currentSchema) {
        if (this.hl7Manager instanceof HL7OutputManager) {
            List<HL7TreeNode> roots = this.hl7Manager.getTreeData("");
            List<HL7TreeNode> mappableNodes = new ArrayList<HL7TreeNode>();
            for (HL7TreeNode root : roots) {
                getMappableNode((Element) root, mappableNodes);
            }

            List<IMetadataColumn> schemaData = this.hl7Manager.getSchemaData(currentSchema);

            for (HL7TreeNode node : mappableNodes) {
                for (IMetadataColumn column : schemaData) {
                    if (node.getLabel().equals(column.getLabel())) {
                        node.setDefaultValue(null);
                        node.setColumn(column);
                        break;
                    }
                    // String[] splits = node.getColumnLabel().split(":");
                    // for (String s : splits) {
                    // if (s.equals(column.getLabel())) {
                    // node.setDefaultValue(null);
                    // node.setColumn(column);
                    // break;
                    // }
                    // }
                }
            }
            // this.hl7UI.refreshXMLViewer(root);
            this.hl7UI.redrawLinkers();
            return;
        }
        this.hl7UI.autoMap();
    }

    protected void getMappableNode(Element node, List<HL7TreeNode> mappableNodes) {
        if (node.getElementChildren().size() == 0) {
            if (node.getColumn() == null) {
                mappableNodes.add(node);
            }
        }
        for (HL7TreeNode attri : node.getAttributeChildren()) {
            if (attri.getColumn() == null) {
                mappableNodes.add(attri);
            }
        }
        for (HL7TreeNode elem : node.getElementChildren()) {
            getMappableNode((Element) elem, mappableNodes);
        }
    }

    private boolean saveAllData() {
        return hl7Manager.saveDataToComponent();

    }

}
