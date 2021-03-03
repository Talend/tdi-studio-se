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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.repository.i18n.Messages;

/**
 * Job scripts export wizard. <br/>
 *
 * $Id: JobScriptsExportWizard.java 1 2006-12-13 下午03:13:18 bqian
 *
 */
public class ConfigExternalLibWizard extends Wizard {

    private IStructuredSelection selection;

    private ConfigExternalLibPage mainPage;

    /**
     * Creates a wizard for importing the external library for java and perl.
     */
    public ConfigExternalLibWizard() {
        this.setWindowTitle(Messages.getString("ImportExternalLibWizard.windows.title")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    @Override
    public void addPages() {
        super.addPages();

        switch (((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject()
                .getLanguage()) {
        case JAVA:
            mainPage = new ConfigExternalJarPage(selection);
            break;
        case PERL:
            mainPage = new ConfigExternalPerlModulePage(selection);
            break;
        }
        addPage(mainPage);
        mainPage.setPageComplete(!mainPage.isReadOnly());
    }

    /*
     * (non-Javadoc) Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.Wizard#performCancel()
     */
    @Override
    public boolean performCancel() {
        mainPage.cancel();
        return super.performCancel();
    }

}
