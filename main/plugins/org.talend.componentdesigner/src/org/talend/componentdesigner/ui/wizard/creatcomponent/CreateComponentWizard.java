// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.wizard.creatcomponent;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.ImageLib;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.manager.ComponentFolderManager;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.componentpref.ComponentPrefCollection;
import org.talend.componentdesigner.model.enumtype.LanguageType;

/**
 * Standard workbench wizard that creates a new component project resource in the workspace. Example:
 * 
 * <pre>
 * ComponentProjectWizard wizard = new ComponentProjectWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to the user. When the user hits Finish, a
 * project resource with the user-specified name is created, the dialog closes, and the call to <code>open</code>
 * returns.
 */
public class CreateComponentWizard extends BasicNewResourceWizard {

    private WizardComponentFolderPage creatProjectPage;

    private WizardJetFilesChoosePage creatJetFilesPage;

    private final ComponentPref componentPref;

    private ComponentFolderManager manager;

    private static final String JAVAWIZARD = "JAVAWIZARD"; //$NON-NLS-1$

    private static final String PERLWIZARD = "PERLWIZARD"; //$NON-NLS-1$

    /**
     * Creates a wizard for creating a new project resource in the workspace.
     */
    public CreateComponentWizard() {
        IDialogSettings workbenchSettings = ComponentDesigenerPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);
        componentPref = new ComponentPref();
    }

    /**
     * Creates a wizard for creating a new project resource in the workspace.
     */
    public CreateComponentWizard(String selectedComponentName) {
        IDialogSettings workbenchSettings = ComponentDesigenerPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);
        componentPref = ComponentPrefCollection.getInstance().getComponentPref(selectedComponentName);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    @Override
    public void addPages() {
        creatProjectPage = new WizardComponentFolderPage("componentNewProjectPage", componentPref); //$NON-NLS-1$
        creatProjectPage.setTitle(Messages.getString("CreateComponentWizard.MainProperties")); //$NON-NLS-1$
        creatProjectPage.setDescription(Messages.getString("CreateComponentWizard.FillIn")); //$NON-NLS-1$
        this.addPage(creatProjectPage);

        creatJetFilesPage = new WizardJetFilesChoosePage("creatJetFilesPage", //$NON-NLS-1$
                componentPref);
        creatJetFilesPage.setTitle(Messages.getString("CreateComponentWizard.SpecifyRes")); //$NON-NLS-1$
        this.addPage(creatJetFilesPage);

        JavaXMLConfigWizardPage javaXMLConfigPage = new JavaXMLConfigWizardPage(JAVAWIZARD, componentPref);
        javaXMLConfigPage.setTitle(Messages.getString("CreateComponentWizard.CreateXMLJava")); //$NON-NLS-1$
        this.addPage(javaXMLConfigPage);

        PerlXMLConfigWizardPage perlXMLConfigPage = new PerlXMLConfigWizardPage(PERLWIZARD, componentPref);
        perlXMLConfigPage.setTitle(Messages.getString("CreateComponentWizard.CreateXMLPerl")); //$NON-NLS-1$
        this.addPage(perlXMLConfigPage);
    }

    @Override
    public boolean canFinish() {
        IWizardPage currentPage = this.getContainer().getCurrentPage();
        if (currentPage.getName().equals(PERLWIZARD)) {
            return true;
        }
        if (currentPage.getName().equals(JAVAWIZARD) && (componentPref.getLanguageType() == LanguageType.JAVALANGUAGETYPE)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc) Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("CreateComponentWizard.NewComponent")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc) Method declared on BasicNewResourceWizard.
     */
    @Override
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = ImageLib.getImageDescriptor(ImageLib.NEWCOMPONENT_WIZARD); //$NON-NLS-1$
        setDefaultPageImageDescriptor(desc);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    @Override
    public boolean performFinish() {
        manager = new ComponentFolderManager();
        try {
            manager.generateComponentContent(componentPref, ResourcesPlugin.getWorkspace().getRoot().getProject(
                    PluginConstant.COMPONENT_PROJECT));
            ComponentPrefCollection.getInstance().save(componentPref);
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        } catch (CoreException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        } catch (IOException ioe) {
            // ioe.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(ioe);
        }
        return true;
    }

    @Override
    public void dispose() {

    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.getName().equals(JAVAWIZARD)) {
            if (componentPref.getLanguageType() == LanguageType.JAVALANGUAGETYPE) {
                return null;
            }
        }
        IWizardPage tempPage = super.getNextPage(page);
        if (tempPage != null) {
            if (tempPage.getName().equals(JAVAWIZARD)) {
                if (componentPref.getLanguageType() == LanguageType.PERLLANGUAGETYPE) {
                    return this.getPage(PERLWIZARD);
                }
            }
        }
        return tempPage;
    }
}
