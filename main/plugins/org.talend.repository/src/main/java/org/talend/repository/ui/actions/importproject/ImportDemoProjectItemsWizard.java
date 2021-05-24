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
package org.talend.repository.ui.actions.importproject;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.repository.items.importexport.manager.ResourcesManager;

public class ImportDemoProjectItemsWizard extends Wizard implements IImportWizard {

    private ImportDemoProjectItemsPage demoProjectPage;

    private List<DemoProjectBean> demoProjectList;

    public void init(IWorkbench workbench, IStructuredSelection selection) {

        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    public void addPages() {
        super.addPages();
        demoProjectPage = new ImportDemoProjectItemsPage(null);
        demoProjectPage.setImportDemoProjectList(this.demoProjectList);
        demoProjectPage.setPageComplete(false);
        super.addPage(demoProjectPage);
    }

    public ImportDemoProjectItemsWizard(List<DemoProjectBean> demoProjectList) {
        this.demoProjectList = demoProjectList;
    }

    public List<ResourcesManager> getSelectedDemoManagers() {
        return demoProjectPage.getSelectedDemoManagers();
    }

    @Override
    public boolean performCancel() {
        return demoProjectPage.performCancel();
    }

    @Override
    public boolean performFinish() {
        return demoProjectPage.performFinish();
    }

    @Override
    public boolean canFinish() {
        if (!demoProjectPage.isPageComplete()) {
            return false;
        }
        return true;
    }

}
