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
package org.talend.repository.ui.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class PropsTitleLabelProvider extends LabelProvider {

    // The TabbedPropertySheetPage(RepositoryView.java row 417 ) and TalendTabbedPropertySheetPage(TalendEditor row 428)
    // will use the same TabbedPropertyRegistry(TabbedPropertyRegistryFactory.java row 69) with the same LabelProvider.
    // So the GefEditorLabelProvider and the PropsTitleLabelProvider will be confilcted.
    // See issue 973 and commitment 3231
    // BTW,I feel this adapter kind of solution is not good.
    // by bqian 26 April 2007
    private ILabelProvider gefEditorNodeLabelProvider = RepositoryPlugin.getDefault().getDesignerCoreService()
            .getGEFEditorNodeLabelProvider();

    @Override
    public String getText(Object element) {
        RepositoryNode repositoryNode = getRepositoryNode(element);
        if (repositoryNode != null) {
            return repositoryNode.getLabel();
        }
        return gefEditorNodeLabelProvider.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        RepositoryNode repositoryNode = getRepositoryNode(element);
        if (repositoryNode != null) {
            return ImageProvider.getImage(repositoryNode.getIcon());
        }

        return gefEditorNodeLabelProvider.getImage(element);
    }

    private RepositoryNode getRepositoryNode(Object element) {
        if (element instanceof IStructuredSelection) {
            Object firstElement = ((IStructuredSelection) element).getFirstElement();
            return SectionFilter.getRepositoryNode(firstElement);
        }
        return null;
    }

}
