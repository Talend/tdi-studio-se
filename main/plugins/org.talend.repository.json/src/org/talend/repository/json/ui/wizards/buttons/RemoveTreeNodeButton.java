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
package org.talend.repository.json.ui.wizards.buttons;

import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;
import org.talend.repository.json.ui.wizards.action.DeleteJSONNodeAction;

/**
 * hwang class global comment. Detailled comment
 */
public class RemoveTreeNodeButton extends AbstractTreeNodeButton {

    public RemoveTreeNodeButton(Composite parent, AbstractJSONStepForm form) {
        super(parent, form, "Remove", ImageProvider.getImage(EImage.DELETE_ICON));
    }

    protected void handleSelectionEvent(TreeSelection selection) {
        if (form != null) {
            DeleteJSONNodeAction deleteAction = new DeleteJSONNodeAction(treeViewer, form, "");
            deleteAction.run();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.fileoutputJSON.ui.footer.AbstractTreeNodeButton#updateButtonStatus()
     */
    @Override
    protected void updateStatus(TreeSelection selection) {
        if (selection.getFirstElement() instanceof FOXTreeNode) {
            FOXTreeNode node = (FOXTreeNode) selection.getFirstElement();
            if (node == null) {
                getButton().setEnabled(false);
            } else {
                if (node.getParent() == null) {
                    getButton().setEnabled(false);
                } else {
                    getButton().setEnabled(true);
                }
            }
        } else {
            getButton().setEnabled(false);
        }

    }

    @Override
    protected void addTreeListeners() {
        super.addTreeListeners();
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            treeViewer.getTree().addKeyListener(new KeyListener() {

                public void keyReleased(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    if (e.keyCode == SWT.DEL) {
                        if (form != null) {
                            DeleteJSONNodeAction deleteAction = new DeleteJSONNodeAction(treeViewer, form, "");
                            deleteAction.run();
                        }
                    }
                }
            });
        }
    }

}
