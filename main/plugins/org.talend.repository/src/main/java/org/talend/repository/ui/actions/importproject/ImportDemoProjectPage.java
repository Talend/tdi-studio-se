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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.NewImportProjectWizard;

/**
 * This class is used for creating a page for importing demo project.<br/>
 *
 * @author ftang
 *
 */
public class ImportDemoProjectPage extends WizardFileSystemResourceExportPage1 implements ISelectionChangedListener {

    private TableViewer wizardSelectionViewer;

    private Browser descriptionBrowser;

    private Text descriptionText;

    private List<DemoProjectBean> demoProjectList;

    private int selectedDemoProjectIndex = Integer.MAX_VALUE;

    private final static String DEFAUTL_DEMO_ICON = "icons/java.png";

    protected String projectName;

    /**
     * ImportDemoProjectPage constructor.
     *
     * @param selection
     */
    public ImportDemoProjectPage(IStructuredSelection selection) {
        super(selection);
        this.setMessage(Messages.getString("ImportDemoProjectPage.message1")); //$NON-NLS-1$
        this.setTitle(Messages.getString("ImportDemoProjectPage.title")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#createControl(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("ImportDemoProjectPage.availableProjectsPrompt")); //$NON-NLS-1$
        GridData gd = new GridData();
        label.setLayoutData(gd);

        SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 300;
        sashForm.setLayoutData(gd);

        wizardSelectionViewer = new TableViewer(sashForm, SWT.BORDER);
        createDescriptionIn(sashForm);
        initializeViewer();
        wizardSelectionViewer.addSelectionChangedListener(this);
        Dialog.applyDialogFont(container);
        setControl(container);
    }

    /**
     * DOC Administrator Comment method "createDescriptionIn".
     *
     * @param composite
     */
    public void createDescriptionIn(Composite composite) {

        if (TalendPropertiesUtil.isEnabledUseBrowser()) {
            descriptionBrowser = new Browser(composite, SWT.BORDER);
            descriptionBrowser.setText(""); //$NON-NLS-1$
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.widthHint = 200;
            descriptionBrowser.setLayoutData(gd);
        } else {
            descriptionText = new Text(composite, SWT.BORDER | SWT.WRAP);
            descriptionText.setText(""); //$NON-NLS-1$
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.widthHint = 200;
            descriptionText.setLayoutData(gd);
        }
    }

    /**
     * initializeViewer.
     */
    private void initializeViewer() {
        for (int i = 0; i < this.demoProjectList.size(); i++) {
            DemoProjectBean demoProject = this.demoProjectList.get(i);

            TableItem tableItem = new TableItem(wizardSelectionViewer.getTable(), i);
            tableItem.setText(demoProject.getDemoDesc());
            tableItem.setImage(getFullImagePath(demoProject));
        }
    }

    /**
     * getFullImagePath.
     *
     * @param languageName
     * @return
     */
    private Image[] getFullImagePath(DemoProjectBean node) {
        URL url = null;
        String pluginPath = null;
        String relatedImagePath = null;
        Bundle bundle = null;
        if (node != null) {
            relatedImagePath = node.getIconUrl();// ;
            bundle = Platform.getBundle(node.getPluginId());
        }
        try {
            if (FileLocator.find(bundle, new Path(relatedImagePath), null) != null) {
                url = FileLocator.toFileURL(FileLocator.find(bundle, new Path(relatedImagePath), null));
                pluginPath = new Path(url.getFile()).toOSString();
            } else {
                url = FileLocator.find(bundle, new Path(DEFAUTL_DEMO_ICON), null);
                pluginPath = new Path(url.getFile()).toOSString();
            }
        } catch (IOException e1) {
        }

        return new Image[] { new Image(null, pluginPath) };
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent
     * )
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        selectedDemoProjectIndex = ((TableViewer) event.getSource()).getTable().getSelectionIndex();

        // MOD gdbu 2011-5-10 bug : 21138
        DemoProjectBean demoProjectBean = this.demoProjectList.get(selectedDemoProjectIndex);
        String demoDescription = demoProjectBean.getDescriptionContents();

        if (demoProjectBean != null) {
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }

        // ~21138
        if (descriptionBrowser != null && TalendPropertiesUtil.isEnabledUseBrowser() && !descriptionBrowser.isDisposed()) {
            descriptionBrowser.setText(demoDescription);
        } else if (descriptionText != null && !descriptionText.isDisposed()) {
            descriptionText.setText(demoDescription);
        }
    }

    /**
     * Sets import demo project list.
     *
     * @param demoProjectList
     */
    public void setImportDemoProjectList(List<DemoProjectBean> demoProjectList) {
        this.demoProjectList = demoProjectList;
        if (demoProjectList != null && demoProjectList.size() > 1) {
            this.setMessage(Messages.getString("ImportDemoProjectPage.message")); //$NON-NLS-1$
        }
    }

    /**
     * Gets the index of selected demo project.
     *
     * @return
     */
    public int getSelectedDemoProjectIndex() {
        return selectedDemoProjectIndex;
    }

    public boolean prefromFinish() {
        final DemoProjectBean selectPro = this.demoProjectList.get(selectedDemoProjectIndex);

        NewImportProjectWizard newPrjWiz = new NewImportProjectWizard();
        WizardDialog newProjectDialog = new WizardDialog(getShell(), newPrjWiz);
        newProjectDialog.setTitle(Messages.getString("NewImportProjectWizard.windowTitle")); //$NON-NLS-1$
        if (newProjectDialog.open() == Window.OK) {
            projectName = newPrjWiz.getName().trim().replace(' ', '_');
            // final String demoProjName = selectPro.getProjectName();

            //
            ProgressDialog progressDialog = new ProgressDialog(getShell()) {

                private IProgressMonitor monitorWrap;

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitorWrap = new EventLoopProgressMonitor(monitor);

                    try {
                        // final List<DemoProjectBean> demoProjectsList = ImportProjectsUtilities.getAllDemoProjects();
                        // DemoProjectBean demoProjectBean = null;
                        // for (DemoProjectBean demoBean : demoProjectsList) {
                        // if (demoBean.getProjectName().equals(demoProjName)) {
                        // demoProjectBean = demoBean;
                        // break;
                        // }
                        // }
                        if (null == selectPro) {
                            throw new IOException("cannot find selected demo project"); //$NON-NLS-1$
                        }
                        ImportProjectsUtilities.importDemoProject(getShell(), projectName, selectPro, monitor);
                    } catch (Exception e1) {
                        projectName = null;
                        throw new InvocationTargetException(e1);
                    }

                    monitorWrap.done();
                }
            };

            try {
                progressDialog.executeProcess();
            } catch (InvocationTargetException e1) {
                projectName = null;
                MessageBoxExceptionHandler.process(e1.getTargetException(), getShell());
            } catch (InterruptedException e1) {
                projectName = null;
            }
        }
        return true;
    }

    public String getProjectName() {
        return this.projectName;
    }
}
