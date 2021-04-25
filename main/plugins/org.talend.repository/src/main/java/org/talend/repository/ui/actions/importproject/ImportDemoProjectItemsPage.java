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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.repository.i18n.Messages;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;

public class ImportDemoProjectItemsPage extends WizardFileSystemResourceExportPage1 implements ICheckStateListener {

    private CheckboxTableViewer wizardSelectionViewer;

    private Browser descriptionBrowser;

    private Text descriptionText;

    private List<DemoProjectBean> demoProjectList;

    private List<ResourcesManager> selectedDemoManagers = new ArrayList<ResourcesManager>();

    // private final ImportNodesBuilder nodesBuilder = new ImportNodesBuilder();

    private final static String DEFAUTL_DEMO_ICON = "icons/java.png";

    private final ImportExportHandlersManager importManager = new ImportExportHandlersManager();

    /**
     * ImportDemoProjectPage constructor.
     *
     * @param selection
     */
    public ImportDemoProjectItemsPage(IStructuredSelection selection) {
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

        wizardSelectionViewer = CheckboxTableViewer.newCheckList(sashForm, SWT.CHECK | SWT.SINGLE | SWT.BORDER);
        wizardSelectionViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

        wizardSelectionViewer.addCheckStateListener(this);
        wizardSelectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof DemoProjectBean) {
                    DemoProjectBean selectNode = (DemoProjectBean) ((IStructuredSelection) event.getSelection())
                            .getFirstElement();
                    showDescriptionIn(selectNode);
                }
            }
        });
        createDescriptionIn(sashForm);

        initializeViewer();
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

    public void showDescriptionIn(DemoProjectBean node) {
        String currentProDes = node.getDescriptionContents();
        if (descriptionBrowser != null && TalendPropertiesUtil.isEnabledUseBrowser() && !descriptionBrowser.isDisposed()) {
            descriptionBrowser.setText(currentProDes);
        } else if (descriptionText != null && !descriptionText.isDisposed()) {
            descriptionText.setText(currentProDes);
        }
    }

    /**
     * initializeViewer.
     */
    private void initializeViewer() {
        wizardSelectionViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // TODO Auto-generated method stub

            }

            @Override
            public void dispose() {
                // TODO Auto-generated method stub

            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    return ((List) inputElement).toArray();
                }
                return null;
            }
        });
        wizardSelectionViewer.setLabelProvider(new ITableLabelProvider() {

            @Override
            public void removeListener(ILabelProviderListener listener) {

            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void dispose() {

            }

            @Override
            public void addListener(ILabelProviderListener listener) {

            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof DemoProjectBean) {
                    DemoProjectBean field = (DemoProjectBean) element;
                    return field.getDemoDesc();
                }
                return "";
            }

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (element instanceof DemoProjectBean) {
                    return getFullImagePath((DemoProjectBean) element);
                }
                return null;
            }
        });
        wizardSelectionViewer.setInput(this.demoProjectList);
    }

    /**
     * getFullImagePath.
     *
     * @param languageName
     * @return
     */
    private Image getFullImagePath(DemoProjectBean node) {
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
            // nothing to do
        }

        return new Image(null, pluginPath);
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
    public List<ResourcesManager> getSelectedDemoManagers() {
        return selectedDemoManagers;
    }

    public boolean performCancel() {
        return true;
    }

    private List<DemoProjectBean> getCheckedElements() {
        List<DemoProjectBean> checkedElements = new ArrayList<DemoProjectBean>();
        for (Object obj : wizardSelectionViewer.getCheckedElements()) {
            if (obj instanceof DemoProjectBean) {
                checkedElements.add((DemoProjectBean) obj);
            }
        }
        return checkedElements;
    }

    public boolean performFinish() {
        List<DemoProjectBean> checkedElements = getCheckedElements();
        final List<ResourcesManager> finalCheckManagers = getResourceManagers(checkedElements);
        String warnMessage = populateExistItemRecords(finalCheckManagers);
        if (!warnMessage.equals("")) {
            MessageDialog dialog = new MessageDialog(getShell(), Messages.getString("ImportDemoProjectPage.overwrite"), null,
                    warnMessage, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                    0);
            dialog.open();
            int result = dialog.getReturnCode();
            if (result != MessageDialog.OK) {
                return false;
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(getShell()) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitorWrap = new EventLoopProgressMonitor(monitor);
                ResourceOption demoImportOption = ResourceOption.DEMO_IMPORTATION;
                try {
                    EmfResourcesFactoryReader.INSTANCE.addOption(demoImportOption);

                    for (ResourcesManager resManager : finalCheckManagers) {
                        List<ImportItem> projectRecords = importManager.populateImportingItems(resManager, true, monitorWrap);
                        // clearOverWriteErrorMessages(projectRecords, overwrite);
                        importManager.importItemRecords(monitorWrap, resManager, projectRecords, true,
                                projectRecords.toArray(new ImportItem[0]), null);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                } finally {
                    EmfResourcesFactoryReader.INSTANCE.removOption(demoImportOption);
                }
                monitorWrap.done();
                if (monitor.isCanceled()) {
                    MessageDialog.openInformation(getShell(),
                            Messages.getString("ImportDemoProjectAction.messageDialogTitle.demoProjectCancel"),
                            Messages.getString("ImportDemoProjectAction.messageDialogContent.demoProjectImportedIncompletely"));
                    return;
                } else {
                    MessageDialog.openInformation(getShell(),
                            Messages.getString("ImportDemoProjectAction.messageDialogTitle.demoProject"), //$NON-NLS-1$
                            Messages.getString("ImportDemoProjectAction.messageDialogContent.demoProjectImportedSuccessfully")); //$NON-NLS-1$

                }
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
        } catch (InterruptedException e) {
            // Nothing to do
        }

        return true;
    }

    private List<ResourcesManager> getResourceManagers(List<DemoProjectBean> checkedProjectBean) {
        List<ResourcesManager> resManagers = new ArrayList<ResourcesManager>();
        try {
            for (DemoProjectBean pro : checkedProjectBean) {
                ResourcesManager resManager = null;

                Bundle bundle = Platform.getBundle(pro.getPluginId());
                URL demoURL = FileLocator.find(bundle, new Path(pro.getDemoProjectFilePath()), null);
                demoURL = FileLocator.toFileURL(demoURL);
                String filePath = new Path(demoURL.getFile()).toOSString();
                File srcFile = new File(filePath);
                // TUP-1924:use UnityManager here ,same with import normal items.
                FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance()
                        .createFileUnityManager(srcFile);
                resManager = fileUnityManager.doUnify();
                if (resManager != null) {
                    resManagers.add(resManager);
                }
            }
        } catch (ZipException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_ZipImport_badFormat")); //$NON-NLS-1$
        } catch (TarException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_TarImport_badFormat")); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return resManagers;
    }

    private TarFile getSpecifiedTarSourceFile(File srcFile) {
        if (!srcFile.exists()) {
            return null;
        }
        try {
            return new TarFile(srcFile);
        } catch (TarException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_TarImport_badFormat")); //$NON-NLS-1$
        } catch (IOException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_couldNotRead")); //$NON-NLS-1$
        }

        return null;
    }

    private ZipFile getSpecifiedZipSourceFile(File srcFile) {
        if (!srcFile.exists()) {
            return null;
        }
        try {
            return new ZipFile(srcFile);
        } catch (ZipException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_ZipImport_badFormat")); //$NON-NLS-1$
        } catch (IOException e) {
            displayErrorDialog(Messages.getString("ImportItemsWizardPage_couldNotRead")); //$NON-NLS-1$
        }

        return null;
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        if (event.getChecked()) {
            wizardSelectionViewer.setSelection(new StructuredSelection(event.getElement()));
            setPageComplete(true);
        } else {
            // descriptionBrowser.setText("");
            if (getCheckedElements().size() == 0) {
                setPageComplete(false);
            } else {
                setPageComplete(true);
            }
        }

    }

    private String populateExistItemRecords(final List<ResourcesManager> manager) {
        String messageInfo = ""; //$NON-NLS-1$
        final Collection<ImportItem> items = new ArrayList<ImportItem>();
        IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    for (ResourcesManager rm : manager) {
                        items.addAll(importManager.populateImportingItems(rm, true, monitor));
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }

        };
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, op);
        } catch (Exception e) {
            // ignore me
        }

        for (ImportItem itemRecord : items) {
            if (itemRecord.getExistingItemWithSameId() != null
                    && itemRecord.getExistingItemWithSameId() instanceof RepositoryViewObject) {
                RepositoryViewObject reObject = (RepositoryViewObject) itemRecord.getExistingItemWithSameId();
                if (itemRecord.getProperty() != null && reObject != null) {
                    if (!itemRecord.getProperty().getId().equals(reObject.getId())
                            && itemRecord.getProperty().getLabel().equalsIgnoreCase(reObject.getLabel())) {
                        messageInfo = Messages.getString("ImportDemoProjectPage.loseItems", reObject.getLabel());
                    }
                    if (itemRecord.getProperty().getId().equals(reObject.getId())
                            && itemRecord.getProperty().getLabel().equals(reObject.getLabel())) {
                        messageInfo = Messages.getString("ImportDemoProjectPage.overwriteItems");
                    }

                }
            }
        }
        return messageInfo;
    }

    private void clearOverWriteErrorMessages(List<ImportItem> projectRecords, boolean overwrite) {
        if (overwrite) {
            String overWriteErrorMessage = Messages.getString("RepositoryUtil.nameUsed").trim();
            List<ImportItem> overWriteRecords = new ArrayList<ImportItem>();
            for (ImportItem itemRecord : projectRecords) {
                for (String errorMessage : itemRecord.getErrors()) {
                    if (errorMessage.equals(overWriteErrorMessage)) {
                        overWriteRecords.add(itemRecord);
                    }
                }
            }
            for (ImportItem overRecord : overWriteRecords) {
                overRecord.getErrors().clear();
            }
        }
    }
}
