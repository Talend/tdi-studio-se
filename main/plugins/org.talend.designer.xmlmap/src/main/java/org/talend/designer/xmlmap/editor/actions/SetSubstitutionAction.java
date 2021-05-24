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
package org.talend.designer.xmlmap.editor.actions;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class SetSubstitutionAction extends SelectionAction {

    private TreeNode selectedNode;

    private boolean input;

    private MapperManager mapperManager;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.SetSubstitutionAction";

    private static final String HIDE_MESSAGE = "HIDE_MESSAGE";

    private GraphicalViewer graphicViewer;

    public SetSubstitutionAction(IWorkbenchPart part, GraphicalViewer graphicViewer) {
        super(part);
        this.graphicViewer = graphicViewer;
        setId(ID);
        setText("Set as substitution");
    }

    @Override
    public void run() {
        TreeNode treeNode = null;
        boolean needWarning = false;
        if (input) {
            treeNode = XmlmapFactory.eINSTANCE.createTreeNode();
            if (!selectedNode.getOutgoingConnections().isEmpty()) {
                needWarning = true;
            }
        } else {
            treeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
            EList<Connection> incomingConnections = selectedNode.getIncomingConnections();
            if (!incomingConnections.isEmpty()) {
                needWarning = true;
            }
        }
        boolean canContinue = true;
        // Shell shell = this.part.getSite().getShell();
        if (needWarning) {
            canContinue = MessageDialog.openConfirm(null, "Warning",
                    "Do you want to disconnect the existing linker and then add a choice for the selected element ?");
        }

        if (canContinue) {
            AbstractUIPlugin plugin = WorkbenchPlugin.getDefault();
            IDialogSettings workbenchSettings = plugin.getDialogSettings();
            IDialogSettings section = workbenchSettings.getSection("SetSubstitutionAction.SubsMessageDialog"); //$NON-NLS-1$
            if (section == null) {
                section = workbenchSettings.addNewSection("SetSubstitutionAction.SubsMessageDialog"); //$NON-NLS-1$
            }
            boolean popupMessageDialog = !section.getBoolean(HIDE_MESSAGE);
            if (popupMessageDialog) {
                String message = "This element will be copied as part of the substitution group automatically." + "\n"
                        + "If this element must be abstract and must be extended only, you can delete it.";
                SubsMessageDialog dialog = new SubsMessageDialog(null, message);
                int open = dialog.open();
                if (open == Window.OK) {
                    boolean hideDialogNextTime = dialog.getResult();
                    if (hideDialogNextTime) {
                        section.put(HIDE_MESSAGE, true);
                    }
                }
                if (open == Window.CANCEL) {
                    return;
                }
            }

            XmlMapUtil.detachNodeConnections(selectedNode, mapperManager.getExternalData(), false);
            treeNode.setName(selectedNode.getName() + XSDPopulationUtil2.SUBS);
            treeNode.setNodeType(NodeType.ELEMENT);
            treeNode.setXpath(selectedNode.getXpath() + XmlMapUtil.XPATH_SEPARATOR + treeNode.getName());
            treeNode.setSubstitution(true);
            TreeNode parentNode = (TreeNode) selectedNode.eContainer();
            int index = parentNode.getChildren().indexOf(selectedNode);
            parentNode.getChildren().remove(selectedNode);
            treeNode.getChildren().add(selectedNode);
            parentNode.getChildren().add(index, treeNode);

            if (!input) {
                OutputTreeNode output = (OutputTreeNode) selectedNode;
                if (!XmlMapUtil.isExpressionEditable(output) && output.isAggregate()) {
                    output.setAggregate(false);
                }
            }

            Object object = graphicViewer.getEditPartRegistry().get(treeNode);
            if (object instanceof TreeNodeEditPart) {
                graphicViewer.select((TreeNodeEditPart) object);
            }

        }

    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object object = selectedarts.get(selectedarts.size() - 1);
                if (object instanceof TreeNodeEditPart) {
                    TreeNodeEditPart nodePart = (TreeNodeEditPart) object;
                    this.selectedNode = (TreeNode) nodePart.getModel();

                    if (selectedNode.isChoice() || selectedNode.isSubstitution()) {
                        return false;
                    }

                    if (hasSameNameWithParentSubs(selectedNode, selectedNode)) {
                        return false;
                    }

                    boolean isElement = NodeType.ELEMENT.equals(selectedNode.getNodeType());
                    if (isElement && XmlMapUtil.getXPathLength(selectedNode.getXpath()) > 3) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private boolean hasSameNameWithParentSubs(TreeNode node, TreeNode toValidate) {
        if (node.eContainer() instanceof TreeNode) {
            TreeNode parent = (TreeNode) node.eContainer();
            if (parent.isSubstitution() && parent.getName().equals(toValidate.getName() + XSDPopulationUtil2.SUBS)) {
                return true;
            } else {
                return hasSameNameWithParentSubs(parent, toValidate);
            }
        }
        return false;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    class SubsMessageDialog extends Dialog {

        private String message;

        private boolean hideMessage;

        protected SubsMessageDialog(Shell parentShell, String message) {
            super(parentShell);
            this.message = message;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());

            Label messageLabel = new Label(composite, SWT.NONE);
            messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            messageLabel.setText(message);

            final Button checkButton = new Button(parent, SWT.CHECK);
            checkButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 20, 1));
            checkButton.setText("Don't display anymore");

            checkButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    hideMessage = checkButton.getSelection();
                }
            });

            return composite;
        }

        public boolean getResult() {
            return this.hideMessage;
        }
    }

}
