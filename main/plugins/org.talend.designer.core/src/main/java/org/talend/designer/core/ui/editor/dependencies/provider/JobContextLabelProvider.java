package org.talend.designer.core.ui.editor.dependencies.provider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;

public class JobContextLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        // TODO Auto-generated method stub
        return super.getImage(element);
    }

    @Override
    public String getText(Object element) {
        return ((JobContextTreeNode) element) == null ? "" : ((JobContextTreeNode) element).getName();
    }

}
