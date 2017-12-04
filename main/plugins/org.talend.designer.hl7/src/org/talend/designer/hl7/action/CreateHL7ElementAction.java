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
package org.talend.designer.hl7.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.designer.hl7.managers.HL7OutputManager;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.Attribute;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.designer.hl7.ui.data.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: CreateElementAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class CreateHL7ElementAction extends SelectionProviderAction {

    // the xml viewer, see HL7UI.
    private TreeViewer xmlViewer;

    private HL7UI hl7ui;

    private AbstractForm from;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public CreateHL7ElementAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public CreateHL7ElementAction(TreeViewer xmlViewer, String text, AbstractForm from) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.from = from;
    }

    public CreateHL7ElementAction(TreeViewer xmlViewer, HL7UI hl7ui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.hl7ui = hl7ui;
    }

    public void init() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();

        if (node == null) {
            this.setEnabled(false);
            return;
        }
        // if (node.getParent() == null) {
        // this.setEnabled(false);
        // return;
        // }

        if (node instanceof Attribute) {
            this.setEnabled(false);
            return;
        }

        if (node instanceof NameSpaceNode) {
            this.setEnabled(false);
            return;
        }
        this.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        createChildNode(node);
    }

    /**
     * Create the child node of the input node
     *
     * @param node
     */
    private boolean createChildNode(final HL7TreeNode node) {
        if (node.getColumn() != null) {
            if (!MessageDialog.openConfirm(
                    xmlViewer.getControl().getShell(),
                    "Warning",
                    "Do you want to disconnect the existing linker and then add an sub element for the selected element"
                            + node.getLabel() + "\"?")) {
                return false;
            }
            node.setColumn(null);
        }
        String label = "";
        final String nodeLabel = node.getLabel() + "-";
        while (!StringUtil.validateLabelForXML(label)) {
            // add validator
            IInputValidator validator = new IInputValidator() {

                @Override
                public String isValid(String newText) {
                    if (newText != null) {
                        String text = newText.trim();
                        for (HL7TreeNode children : node.getChildren()) {
                            if (text.equals(children.getLabel())) {
                                return "The name already existed."; //$NON-NLS-1$
                            }
                        }
                    }
                    return null;
                }
            };
            InputDialog dialog = new InputDialog(null, "Input element's label", "Input the new element's valid label", nodeLabel,
                    validator) {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
                 */
                @Override
                protected void okPressed() {
                    super.okPressed();
                }

            };
            dialog.setErrorMessage("name is error");
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
            }
            if (status == InputDialog.CANCEL) {
                return false;
            }
        }
        HL7TreeNode child = new Element(label);
        // if the root not have CurSchema
        if (node.getRow() == null || node.getRow().equals("")) {
            if (hl7ui != null && hl7ui.gethl7Manager() instanceof HL7OutputManager) {
                if (label.length() == 3) {
                    child.setRow(label);
                    IMetadataTable table = null;
                    for (IMetadataTable curTable : hl7ui.gethl7Manager().getHl7Component().getMetadataList()) {
                        if (label.equals(curTable.getLabel())) {
                            table = curTable;
                            break;
                        }
                    }
                    if (table == null) {
                        table = new MetadataTable();
                        table.setLabel(label);
                        table.setTableName(label);
                        hl7ui.gethl7Manager().getHl7Component().getMetadataList().add(table);
                    }
                    List<Map<String, String>> maps = (List<Map<String, String>>) hl7ui.gethl7Manager().getHl7Component()
                            .getElementParameter("SCHEMAS").getValue(); //$NON-NLS-1$
                    boolean found = false;
                    for (Map<String, String> map : maps) {
                        if (map.get("SCHEMA").equals(table.getTableName())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        Map<String, String> hl7Schema = new HashMap<String, String>();
                        maps.add(hl7Schema);
                        hl7Schema.put("SCHEMA", table.getTableName());
                    }
                }
            } else if (label.length() == 3) {
                child.setRow(label);
            }
        } else {
            child.setRow(node.getRow());
        }

        node.addChild(child);
        this.xmlViewer.refresh();
        return true;
    }
}
