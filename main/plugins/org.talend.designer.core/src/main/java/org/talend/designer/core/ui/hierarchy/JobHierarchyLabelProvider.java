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
package org.talend.designer.core.ui.hierarchy;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IProcess2;
import org.talend.repository.ProjectManager;

/**
 * Label provider for the job hierarchy viewers.
 */
public class JobHierarchyLabelProvider extends LabelProvider {

    private JobHierarchyLifeCycle fHierarchy;

    private boolean isTIS;

    public JobHierarchyLabelProvider(JobHierarchyLifeCycle lifeCycle) {
        super();
        fHierarchy = lifeCycle;
        isTIS = PluginChecker.isRefProjectLoaded();
    }

    @Override
    public Image getImage(Object element) {
        return ImageProvider.getImage(ECoreImage.PROCESS_ICON);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IProcess2) {
            IProcess2 process = (IProcess2) element;

            String label = process.getLabel();
            if (isTIS) {
                label += "     [" + getProjectLabel(process) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }

            return label;
        }
        return super.getText(element);
    }

    private String getProjectLabel(IProcess2 process) {
        org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(
                process.getProperty().getItem());
        return project.getTechnicalLabel();
    }
}
