// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.json.ui.wizards.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;
import org.talend.repository.json.util.JSONUtil;

/**
 * hwang class global comment. Detailled comment
 */
public class ImportTreeFromJSONAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public ImportTreeFromJSONAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public ImportTreeFromJSONAction(TreeViewer jsonViewer, AbstractJSONStepForm form, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

    @Override
    public void run() {
        List<FOXTreeNode> newInput = new ArrayList<FOXTreeNode>();

        String filePath = getFilePath();
        if (filePath == null) {
            return;
        }

        boolean changed = true;
        try {
            newInput = TreeUtil.getFoxTreeNodes(JSONUtil.changeJsonToXml(filePath));
            changed = true;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        if (newInput.size() == 0) {
            return;
        }

        if (changed) {
            List<FOXTreeNode> treeData = form.getTreeData();
            treeData.clear();
            treeData.addAll(newInput);
            jsonViewer.setInput(treeData);
            jsonViewer.refresh();
            jsonViewer.expandAll();
            form.updateStatus();
            form.redrawLinkers();
            form.updateConnection();
        }
    }

    private String getFilePath() {
        FileDialog f = new FileDialog(jsonViewer.getControl().getShell());
        String file = f.open();
        return file;
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.setEnabled(true);
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null) {
            form.setSelectedText(node.getLabel());
        }
    }

}
