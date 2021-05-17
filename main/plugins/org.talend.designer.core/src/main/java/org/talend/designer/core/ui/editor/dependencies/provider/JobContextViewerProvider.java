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
package org.talend.designer.core.ui.editor.dependencies.provider;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceContextHelper;
import org.talend.repository.viewer.ui.viewer.RepositoryTreeViewer;

public class JobContextViewerProvider {

    private ResourceContextHelper contextHelper;

    public JobContextViewerProvider(ResourceContextHelper contextHelper) {
        super();
        this.contextHelper = contextHelper;
    }

    protected int getStyle() {
        return SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
    }

    protected TreeViewer createTreeViewer(Composite parent, int style) {
        return new RepositoryTreeViewer(parent, style);
    }

    public TreeViewer createViewer(Composite parent) {
        TreeViewer treeViewer = createTreeViewer(parent, getStyle());
        treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        addProviders(treeViewer);
        checkSorter(treeViewer);
        treeViewer.setInput(contextHelper.createRootJobContextTreeNode());
        return treeViewer;
    }

    protected void addProviders(TreeViewer treeViewer) {
        JobContextContentProvider contentProvider = new JobContextContentProvider();
        treeViewer.setContentProvider(contentProvider);
        JobContextLabelProvider labelProvider = new JobContextLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
    }

    protected void checkSorter(TreeViewer treeViewer) {
        treeViewer.setSorter(new TreePathViewerSorter() {

            @Override
            public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2) {
                if (e1 instanceof JobContextTreeNode && e2 instanceof JobContextTreeNode) {
                    final JobContextTreeNode node1 = (JobContextTreeNode) e1;
                    final JobContextTreeNode node2 = (JobContextTreeNode) e2;
                    return node1.compareTo(node2);

                }
                return super.compare(viewer, parentPath, e1, e2);
            }

        });
    }



}
