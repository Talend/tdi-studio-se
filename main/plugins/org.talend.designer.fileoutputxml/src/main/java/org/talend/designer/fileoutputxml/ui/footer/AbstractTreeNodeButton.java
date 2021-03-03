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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.talend.designer.fileoutputxml.managers.FOXManager;

/**
 * DOC wchen class global comment. Detailled comment
 */
public abstract class AbstractTreeNodeButton implements ISelectionChangedListener {

    protected Composite parent;

    protected FOXManager manager;

    protected TreeViewer treeViewer;

    private Button button;

    public AbstractTreeNodeButton(Composite parent, FOXManager manager, String tooltip, Image image) {
        this.manager = manager;
        this.parent = parent;
        init(parent, tooltip, image);
    }

    private void init(Composite parent, String tooltip, Image image) {
        button = new Button(parent, SWT.PUSH);
        button.setToolTipText(tooltip);
        button.setImage(image);
        button.setEnabled(false);

        button.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (treeViewer != null && !treeViewer.getTree().isDisposed()
                        && treeViewer.getSelection() instanceof ITreeSelection) {
                    handleSelectionEvent((TreeSelection) treeViewer.getSelection());
                }
            }

        });
        treeViewer = manager.getUiManager().getFoxUI().getTreeViewer();
        addTreeListeners();
    }

    protected void addTreeListeners() {
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            treeViewer.addSelectionChangedListener(this);
        }
    }

    public Button getButton() {
        return this.button;
    }

    protected abstract void handleSelectionEvent(TreeSelection selection);

    protected abstract void updateStatus(TreeSelection selection);

    public void selectionChanged(SelectionChangedEvent event) {
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            if (treeViewer.getSelection() instanceof TreeSelection) {
                TreeSelection selection = (TreeSelection) treeViewer.getSelection();
                updateStatus(selection);
            }
        }
    }

}
