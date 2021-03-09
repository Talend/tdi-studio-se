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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog.tns;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.talend.designer.core.i18n.Messages;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TnsEditorDialog extends Dialog {

    private final File tnsFile;

    private TnsInfo tnsInfo;

    TreeViewer treeViewer;

    public TnsEditorDialog(Shell parentShell, File tnsFile) {
        super(parentShell);
        this.tnsFile = tnsFile;
        this.setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.FINISH_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        TnsParser tnsparser = new TnsParser(tnsFile);
        treeViewer = new TreeViewer(parent);
        Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.heightHint = 400;
        layoutData.widthHint = 300;
        tree.setLayoutData(layoutData);

        TreeColumn column1st = new TreeColumn(tree, SWT.NONE);
        column1st.setText(Messages.getString("TnsEditorDialog.Service")); //$NON-NLS-1$
        column1st.setWidth(200);

        TreeColumn column2nd = new TreeColumn(tree, SWT.NONE);
        column2nd.setText(Messages.getString("TnsEditorDialog.Value")); //$NON-NLS-1$
        column2nd.setWidth(200);

        treeViewer.setContentProvider(new TnsContentProvider());
        treeViewer.setLabelProvider(new TnsTableProvider());
        treeViewer.setInput(tnsparser.getTree());
        treeViewer.expandAll();
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                okPressed();
            }

        });

        return tree;
    }

    public TnsInfo getTnsInfo() {
        return tnsInfo;
    }

    @Override
    protected void okPressed() {
        if (treeViewer.getSelection().isEmpty()) {
            MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
            box.setText("WARNING"); //$NON-NLS-1$
            box.setMessage("Please select a Item."); //$NON-NLS-1$
            box.open();
            return;
        } else {
            setDBConnectionUseTnsFile();
        }

    }

    private void setDBConnectionUseTnsFile() {

        TnsNodeModel rootNode = (TnsNodeModel) ((TreeSelection) treeViewer.getSelection()).getFirstElement();
        if (rootNode.getLevel() == 1) {
            tnsInfo = new TnsInfo();
            if (rootNode.findChildByName("HOST") != null) { //$NON-NLS-1$
                tnsInfo.setHost(rootNode.findChildByName("HOST").getValue()); //$NON-NLS-1$
            }

            if (rootNode.findChildByName("PORT") != null) { //$NON-NLS-1$
                tnsInfo.setPort(rootNode.findChildByName("PORT").getValue()); //$NON-NLS-1$
            }

            if (rootNode.findChildByName("SID") != null) { //$NON-NLS-1$
                tnsInfo.setConnectionType("ORACLE_SID"); //$NON-NLS-1$
                tnsInfo.setDbName(rootNode.findChildByName("SID").getValue()); //$NON-NLS-1$
            }

            if (rootNode.findChildByName("SERVICE_NAME") != null) { //$NON-NLS-1$
                tnsInfo.setConnectionType("ORACLE_SERVICE_NAME"); //$NON-NLS-1$
                tnsInfo.setDbName(rootNode.findChildByName("SERVICE_NAME").getValue()); //$NON-NLS-1$
            }
            super.okPressed();
        } else if (rootNode.getLevel() != 1) {
            MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
            box.setText("WARNING"); //$NON-NLS-1$
            box.setMessage("Please select a root Item."); //$NON-NLS-1$
            box.open();
            return;
        }

    }

}
