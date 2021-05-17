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
package org.talend.designer.fileoutputxml.ui.footer;

import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.fileoutputxml.action.CreateAttributeAction;
import org.talend.designer.fileoutputxml.action.CreateElementAction;
import org.talend.designer.fileoutputxml.action.CreateNameSpaceAction;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.wizard.metadata.xml.dialog.AddElementDialog;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;

/**
 * DOC talend class global comment. Detailled comment
 */
public class AddTreeNodeButton extends AbstractTreeNodeButton {

    public AddTreeNodeButton(Composite parent, FOXManager manager) {
        super(parent, manager, "Add", ImageProvider.getImage(EImage.ADD_ICON));
    }

    protected void handleSelectionEvent(TreeSelection selection) {
        final FOXUI foxUI = manager.getUiManager().getFoxUI();
        if (foxUI != null) {
            AddElementDialog dialog = new AddElementDialog(parent.getShell());
            if (dialog.open() == Window.CANCEL) {
                return;
            }
            if (AddElementDialog.CREATE_AS_SUBELEMENT.equals(dialog.getSelectValue())) {
                CreateElementAction createElement = new CreateElementAction(foxUI.getTreeViewer(), foxUI, "");
                createElement.run();
            } else if (AddElementDialog.CREATE_AS_ATTRIBUTE.equals(dialog.getSelectValue())) {
                CreateAttributeAction createAttr = new CreateAttributeAction(foxUI.getTreeViewer(), foxUI, "");
                createAttr.run();
            } else if (AddElementDialog.CREATE_AS_NAME_SPACE.equals(dialog.getSelectValue())) {
                CreateNameSpaceAction createNameSpace = new CreateNameSpaceAction(foxUI.getTreeViewer(), foxUI, "");
                createNameSpace.run();
            }
            treeViewer.setSelection(selection);
        }
    }

    @Override
    protected void updateStatus(TreeSelection selection) {
        if (selection.getFirstElement() instanceof FOXTreeNode) {
            FOXTreeNode node = (FOXTreeNode) selection.getFirstElement();
            if (node == null) {
                getButton().setEnabled(false);
                return;
            }
            if (node instanceof Attribute) {
                getButton().setEnabled(false);
                return;
            }

            if (node instanceof NameSpaceNode) {
                getButton().setEnabled(false);
                return;
            }
            getButton().setEnabled(true);
        } else {
            getButton().setEnabled(false);
        }

    }

}
