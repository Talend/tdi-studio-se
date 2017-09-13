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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.core.nexus.NexusServerBean;
import org.talend.core.nexus.TalendLibsServerManager;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.librariesmanager.ui.LibManagerUiPlugin;

/**
 * 
 * DOC wchen class global comment. Detailled comment
 */
public class InstallModuleDialog extends TitleAreaDialog {

    private Text jarPathTxt;

    private Text nameTxt;

    private Text uriTxt;

    private Button platfromRadioBtn;

    private Combo platformCombo;

    private Button repositoryRadioBtn;

    private Button fileRadioBtn;

    private Button detectButton;

    private Button fileBrowse;

    private String moduleName;

    private String initValue;

    /**
     * DOC wchen InstallModuleDialog constructor comment.
     * 
     * @param parentShell
     */
    public InstallModuleDialog(Shell parentShell, String initValue) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
        this.initValue = initValue;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("InstallModuleDialog.text"));//$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        GridData data = new GridData(GridData.FILL_BOTH);
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = 10;
        layout.marginLeft = 20;
        layout.marginRight = 20;
        layout.marginBottom = 50;
        container.setLayout(layout);
        container.setLayoutData(data);
        createPlatformGroup(container);
        createRepositoryGroup(container);
        browseFileGroup(container);

        return parent;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        setPlatformGroupEnabled(true);
        setRepositoryGroupEnabled(false);
        setFileGroupEnabled(false);
        return control;
    }

    private void createPlatformGroup(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        platfromRadioBtn = new Button(composite, SWT.RADIO);
        platfromRadioBtn.setText(Messages.getString("InstallModuleDialog.platfromBtn"));
        platfromRadioBtn.setSelection(true);

        platformCombo = new Combo(composite, SWT.READ_ONLY);
        platformCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        platfromRadioBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPlatformGroupEnabled(true);
                setRepositoryGroupEnabled(false);
                setFileGroupEnabled(false);
            }
        });

        ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        Set<String> jarsAvailable = librairesManagerService.list(new NullProgressMonitor());
        List<ModuleNeeded> unUsedModules = ModulesNeededProvider.getUnUsedModules();
        for (ModuleNeeded module : unUsedModules) {
            if (module.getStatus() == ELibraryInstallStatus.INSTALLED) {
                jarsAvailable.add(module.getModuleName());
            }
        }
        String[] moduleValueArray = jarsAvailable.toArray(new String[jarsAvailable.size()]);
        Comparator<String> comprarator = new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        Arrays.sort(moduleValueArray, comprarator);
        platformCombo.setItems(moduleValueArray);
        if (jarsAvailable.contains(initValue)) {
            platformCombo.setText(initValue);
        } else {
            platformCombo.setText(moduleValueArray[0]);
        }
    }

    private void setPlatformGroupEnabled(boolean enable) {
        platfromRadioBtn.setSelection(enable);
        platformCombo.setEnabled(enable);
        if (enable) {
            setMessage(Messages.getString("InstallModuleDialog.message"), IMessageProvider.INFORMATION);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    private void createRepositoryGroup(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        repositoryRadioBtn = new Button(composite, SWT.RADIO);
        repositoryRadioBtn.setText(Messages.getString("InstallModuleDialog.repositoryBtn"));
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        repositoryRadioBtn.setLayoutData(data);

        Composite subComposite = new Composite(container, SWT.NONE);
        layout = new GridLayout();
        layout.marginLeft = 25;
        layout.numColumns = 2;
        subComposite.setLayout(layout);
        data = new GridData(GridData.FILL_BOTH);
        subComposite.setLayoutData(data);

        Label nameLabel = new Label(subComposite, SWT.NONE);
        nameLabel.setText(Messages.getString("InstallModuleDialog.repository.name"));
        nameTxt = new Text(subComposite, SWT.BORDER);
        nameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label uriLabel = new Label(subComposite, SWT.NONE);
        uriLabel.setText(Messages.getString("InstallModuleDialog.repository.mvnURI"));
        uriTxt = new Text(subComposite, SWT.BORDER);
        uriTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        uriTxt.setEnabled(false);

        detectButton = new Button(subComposite, SWT.NONE);
        detectButton.setText(Messages.getString("InstallModuleDialog.detectButton.text"));
        detectButton.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        detectButton.setLayoutData(data);

        repositoryRadioBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPlatformGroupEnabled(false);
                setRepositoryGroupEnabled(true);
                setFileGroupEnabled(false);
            }
        });
        detectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkMavenRepository();
            }
        });
        nameTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String jarName = nameTxt.getText().trim();
                if (jarName.contains(".")) {
                    detectButton.setEnabled(true);
                    String generatedMavenURI = MavenUrlHelper.generateMvnUrlForJarName(jarName, true, true);
                    uriTxt.setText(generatedMavenURI);
                    setMessage(Messages.getString("InstallModuleDialog.repository.message"), IMessageProvider.ERROR);
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    setMessage(Messages.getString("InstallModuleDialog.repository.name.error"), IMessageProvider.ERROR);
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }
        });
    }

    private void setRepositoryGroupEnabled(boolean enable) {
        repositoryRadioBtn.setSelection(enable);
        nameTxt.setEnabled(enable);
        // detectButton.setEnabled(enable);
        if (enable) {
            setMessage(Messages.getString("InstallModuleDialog.repository.message"), IMessageProvider.ERROR);
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        }
    }

    private void browseFileGroup(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        fileRadioBtn = new Button(composite, SWT.RADIO);
        fileRadioBtn.setText(Messages.getString("InstallModuleDialog.browseFileBtn"));

        jarPathTxt = new Text(composite, SWT.BORDER);
        jarPathTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        fileBrowse = new Button(composite, SWT.PUSH);
        fileBrowse.setText("...");//$NON-NLS-1$

        fileRadioBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPlatformGroupEnabled(false);
                setRepositoryGroupEnabled(false);
                setFileGroupEnabled(true);
            }
        });

        fileBrowse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleButtonPressed();
            }
        });
        jarPathTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                File file = new File(jarPathTxt.getText());
                if (!file.exists()) {
                    setMessage(Messages.getString("InstallModuleDialog.browseFile.error"), IMessageProvider.ERROR);
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    setMessage(Messages.getString("InstallModuleDialog.message"), IMessageProvider.INFORMATION);
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
    }

    private void setFileGroupEnabled(boolean enable) {
        fileRadioBtn.setSelection(enable);
        jarPathTxt.setEnabled(enable);
        fileBrowse.setEnabled(enable);
        if (enable) {
            File file = new File(jarPathTxt.getText());
            if (file.exists()) {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            } else {
                setMessage(Messages.getString("InstallModuleDialog.browseFile.error"), IMessageProvider.ERROR);
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
        }
    }

    private void checkMavenRepository() {
        String jarName = nameTxt.getText().trim();
        String mvnURI = uriTxt.getText().trim();
        ModuleNeeded needed = new ModuleNeeded(jarName, null, true, mvnURI);
        ELibraryInstallStatus installStatus = needed.getStatus();
        final boolean[] status = new boolean[1];
        status[0] = false;
        if (installStatus == ELibraryInstallStatus.INSTALLED) {
            status[0] = true;
        } else {
            final IRunnableWithProgress acceptOursProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    NexusServerBean customNexusServer = TalendLibsServerManager.getInstance().getCustomNexusServer();
                    if (customNexusServer != null) {
                        try {
                            ILibraryManagerService libManagerService = (ILibraryManagerService) GlobalServiceRegister
                                    .getDefault().getService(ILibraryManagerService.class);
                            File resolveJar = libManagerService.resolveJar(TalendLibsServerManager.getInstance(),
                                    customNexusServer, mvnURI);
                            if (resolveJar != null) {
                                status[0] = true;
                                LibManagerUiPlugin.getDefault().getLibrariesService().checkLibraries();
                            }
                        } catch (Exception e) {
                            status[0] = false;
                        }
                    }
                }
            };

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell());
            try {
                dialog.run(true, true, acceptOursProgress);
            } catch (Throwable e) {
                if (!(e instanceof TimeoutException)) {
                    ExceptionHandler.process(e);
                }
            }

        }
        if (!status[0]) {
            setMessage(Messages.getString("InstallModuleDialog.repository.error"), IMessageProvider.ERROR);
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else {
            List<String> modulesNeededNames = ModulesNeededProvider.getModulesNeededNames();
            if (!modulesNeededNames.contains(jarName)) {
                ModulesNeededProvider.userAddUnusedModules(ELibraryInstallStatus.UNKNOWN.name(), jarName);
            }
            setMessage(Messages.getString("InstallModuleDialog.message"), IMessageProvider.INFORMATION);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    private void handleButtonPressed() {
        FileDialog dialog = new FileDialog(getShell());
        dialog.setFilterExtensions(FilesUtils.getAcceptJARFilesSuffix());
        dialog.setText(Messages.getString("InstallModuleDialog.text")); //$NON-NLS-1$

        String filePath = this.jarPathTxt.getText().trim();
        if (filePath.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File file = new File(filePath);
            if (file.exists()) {
                dialog.setFilterPath(new Path(filePath).toOSString());
            }
        }

        String result = dialog.open();
        if (result != null) {
            this.jarPathTxt.setText(result);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (platfromRadioBtn.getSelection()) {
            moduleName = platformCombo.getText();
        } else if (repositoryRadioBtn.getSelection()) {
            moduleName = nameTxt.getText().trim();
        } else {
            final File file = new File(jarPathTxt.getText().trim());
            final IRunnableWithProgress acceptOursProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask("Install module " + file.getName(), 100);
                        monitor.worked(10);
                        CorePlugin.getDefault().getLibrariesService().deployLibrary(file.toURL());
                        monitor.worked(80);
                        moduleName = file.getName();
                        List<String> modulesNeededNames = ModulesNeededProvider.getModulesNeededNames();
                        if (!modulesNeededNames.contains(moduleName)) {
                            ModulesNeededProvider.userAddUnusedModules(ELibraryInstallStatus.UNKNOWN.name(), moduleName);
                        }
                        monitor.done();
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            };

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell());
            try {
                dialog.run(true, true, acceptOursProgress);
            } catch (Throwable e) {
                if (!(e instanceof TimeoutException)) {
                    ExceptionHandler.process(e);
                }
            }

        }
        super.okPressed();
    }

    public String getResult() {
        return moduleName;
    }
}
