package org.talend.designer.core.ui.editor.dependencies.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;

public class JobContextContentProvider implements IStructuredContentProvider, ITreeContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((JobContextTreeNode) parentElement).getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        return ((JobContextTreeNode) element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return ((JobContextTreeNode) element).getChildren().size() > 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return ((JobContextTreeNode) inputElement).getChildren().toArray();
    }

}
