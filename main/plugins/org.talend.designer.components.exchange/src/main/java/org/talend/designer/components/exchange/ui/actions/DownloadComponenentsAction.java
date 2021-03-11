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
package org.talend.designer.components.exchange.ui.actions;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.helper.LocalComponentInstallHelper;
import org.talend.commons.runtime.service.ComponentsInstallComponent;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.utils.resource.UpdatesHelper;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.download.DownloadHelper;
import org.talend.core.download.DownloadListener;
import org.talend.core.download.IDownloadHelper;
import org.talend.core.model.components.ComponentManager;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.components.exchange.ExchangeComponentsProvider;
import org.talend.designer.components.exchange.ExchangeConstants;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.jobs.ComponentInstaller;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.ui.htmlcontent.ContentConstants;
import org.talend.designer.components.exchange.ui.views.ExchangeManager;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.talend.designer.components.exchange.util.ExchangeWebService;
import org.talend.designer.components.exchange.util.WebserviceStatus;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.utils.io.FilesUtils;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class DownloadComponenentsAction extends Action implements IIntroAction {

    private static final String FOLDER_DOWNLOAD = "downloaded"; //$NON-NLS-1$

    private int fExtensionDownloaded;

    private List<ComponentExtension> fDownloadedComponents;

    @Override
    public void run() {
        try {
            final ComponentExtension selectedExtension = ExchangeManager.getInstance().getSelectedExtension();
            Job job = new DownloadJob(selectedExtension);
            fExtensionDownloaded = 0;
            fDownloadedComponents = new ArrayList<ComponentExtension>();
            job.addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(final IJobChangeEvent event) {

                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            updateUI(event);
                        }
                    });
                }
            });
            ExchangeUtils.scheduleUserJob(job);
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Update ui after job finished.
     *
     * @param action
     * @param event
     */
    private void updateUI(final IJobChangeEvent event) {
        // activate aection again after job finished
        if (fExtensionDownloaded > 0) {
            ExchangeUtils.reloadComponents(); // refresh palette
            // update needed modules.
            CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
            // see feature 0005050: confirmation popup once the component is installed
            confirmInstallation();
            // Start Code Generation Init
            ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                    ICodeGeneratorService.class);
            Job job = codeGenService.refreshTemplates();
            job.addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {

                    ComponentPaletteUtilities.setSkipUpdatePalette(false);
                }
            });
            RefreshComponenentsAction action = new RefreshComponenentsAction();
            action.run(new String[] { RefreshComponenentsAction.REFRESH_INSTALLED }, ContentConstants.UL_DOWNLOADED_EXTENSIONS);
        }
    }

    protected File getComponentsFolder() {
        File componentFolder = ExchangeUtils.getComponentFolder(FOLDER_DOWNLOAD);
        if (!componentFolder.exists()) {
            componentFolder.mkdirs();
        }
        return componentFolder;
    }

    private void confirmInstallation() {
        IComponent emfcomponent = null;
        FileFilter propertiesFilter = new FileFilter() {

            // gcui:search xml file.
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith("_java.xml"); //$NON-NLS-1$
            }
        };

        String location = getComponentsFolder().getAbsolutePath();
        File folder = ExchangeComponentsProvider.searchComponentFolder(new File(location));
        File[] files = folder.listFiles(propertiesFilter);
        if (files == null || files.length == 0) {
            return;
        }
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        emfcomponent = componentsFactory.get(files[0].getParentFile().getName());
        if (emfcomponent == null) {
            return;
        }

        String componentName = null;
        StringBuilder message = new StringBuilder();
        String name = null;
        String family = null;
        if (emfcomponent != null) {
            name = emfcomponent.getName();
            family = emfcomponent.getOriginalFamilyName();
        }
        // String name = component.getName();
        //String name = prop.getProperty("NAME"); //$NON-NLS-1$
        //String family = prop.getProperty("FAMILY"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(family)) {
            componentName = name;
            message.append("Component ").append(name).append(" installed at ").append(family).append(".\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (componentName != null) {
            // see 0005051: [exchange view] select component in the palette once component is installed
            selectPaletteEntry(componentName);
        }
        MessageDialog
                .openInformation(
                        null,
                        Messages.getString("DownloadComponenentsAction.installExchange"), Messages.getString("ExchangeWebService.downloadingExtensionSuccessful")); //$NON-NLS-1$ //$NON-NLS-2$
        ComponentManager.saveResource();

    }

    private void selectPaletteEntry(String componentName) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            if (activePage != null) {
                IEditorReference[] editorReferences = activePage.getEditorReferences();
                if (editorReferences != null) {
                    for (IEditorReference er : editorReferences) {
                        IEditorPart part = er.getEditor(false);
                        if (part instanceof AbstractMultiPageTalendEditor) {
                            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) part;
                            AbstractTalendEditor talendEditor = editor.getTalendEditor();
                            try {
                                ComponentPaletteUtilities.setSkipUpdatePalette(true);
                                talendEditor.selectPaletteEntry(componentName);
                            } catch (Exception e) {
                                ExceptionHandler.process(e);
                            }
                            return;
                        }
                    }
                }
            }
        }

    }

    /**
     * Notify after download complete.
     *
     * @param extension
     */
    void extensionDownloadCompleted(ComponentExtension extension) {
        ExchangeManager.getInstance().saveDownloadedExtensionsToFile(extension);
        fExtensionDownloaded++;
        fDownloadedComponents.add(extension);
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    class DownloadJob extends Job implements DownloadListener {

        private IProgressMonitor fMonitor = null;

        private String fProgressLabel;

        private int fBytesDownloaded;

        private ComponentExtension fExtension;

        public DownloadJob(ComponentExtension extension) {
            super(ExchangeConstants.getDownloadTaskTitleLable());
            fExtension = extension;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            SubMonitor progress = SubMonitor.convert(monitor, 25);
            progress.setTaskName(this.getName());
            if (progress.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            fMonitor = progress.newChild(10);
            downloadExtension(fExtension, fMonitor);

            progress.setTaskName(ExchangeConstants.getReloadPaletteLable());
            // progress.done();
            return Status.OK_STATUS;

        }

        private void downloadExtension(ComponentExtension extension, IProgressMonitor monitor) {
            // get the url
            if (ExchangeUtils.checkUserAndPassword()) {
                WebserviceStatus webserviceStatus = ExchangeWebService.downloadingExtensionService(extension.getIdExtension(),
                        ExchangeUtils.TYPEEXTENSION, ExchangeUtils.getUserName(), ExchangeUtils.getPasswordHash());
                if (webserviceStatus.isResult()) {
                    String downloadUrl = webserviceStatus.getValue();
                    if (downloadUrl != null && !downloadUrl.equals("")) { //$NON-NLS-1$
                        monitor.setTaskName(ExchangeConstants.getDownloadTaskNameLable() + downloadUrl);
                        String targetFolder = getComponentsFolder().getAbsolutePath();
                        try {
                            // if file name has special char ,replace it.
                            String regex = "[^a-zA-Z&&[^0-9]&&[^\\_]]"; //$NON-NLS-1$
                            String fileName = extension.getLabel().replaceAll(regex, "_") + ".zip"; //$NON-NLS-1$ //$NON-NLS-2$
                            File localZipFile = new File(targetFolder, fileName);

                            monitor.done();

                            URL url = new URL(downloadUrl);

                            monitor.setTaskName(ExchangeConstants.getDownloadTaskNameLable() + url.toString());
                            DownloadHelper downloader = new DownloadHelper();
                            downloader.addDownloadListener(this);
                            // block until download complete
                            downloader.download(url, localZipFile);

                            // check if the job is cancelled
                            if (!monitor.isCanceled()) {
                                afterDownload(monitor, extension, localZipFile);
                            }
                            // the component zip file
                            // localZipFile.delete();
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }

        protected void afterDownload(IProgressMonitor monitor, ComponentExtension extension, File localZipFile) throws Exception {
            if (UpdatesHelper.isComponentUpdateSite(localZipFile)) {
            	if (!SharedStudioUtils.isSharedStudioMode()) {
                    final File workFolder = org.talend.utils.files.FileUtils.createTmpFolder("downloadedComponents", ""); //$NON-NLS-1$  //$NON-NLS-2$

                    try {
                        FilesUtils.copyFile(localZipFile, new File(workFolder, localZipFile.getName()));

                        ComponentsInstallComponent component = LocalComponentInstallHelper.getComponent();
                        if (component != null) {
                            try {
                                component.setComponentFolder(workFolder);
                                if (component.install()) {


                                	if (component.needRelaunch()) {
                                        askReboot();
                                    } else {
                                        MessageDialog.openInformation(DisplayUtils.getDefaultShell(),
                                                Messages.getString("DownloadComponenentsAction.installComponentsTitle"),
                                                component.getInstalledMessages());
                                    }
                                } else {// install failure
                                    MessageDialog.openWarning(DisplayUtils.getDefaultShell(),
                                            Messages.getString("DownloadComponenentsAction_failureTitle"), //$NON-NLS-1$
                                            Messages.getString("DownloadComponenentsAction_failureMessage", extension.getLabel())); //$NON-NLS-1$
                                }
                            } finally {
                                // after install, clear the setting for service.
                                component.setComponentFolder(null);
                            }
                        }
                    } catch (Exception e) {
                        // Popup dialog to user to waring install failed.
                        Display.getDefault().syncExec(new Runnable() {

                            @Override
                            public void run() {
                                MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                                        Messages.getString("DownloadComponenentsAction_failureTitle"), //$NON-NLS-1$
                                        Messages.getString("DownloadComponenentsAction_failureMessage", extension.getLabel())); //$NON-NLS-1$
                            }
                        });
                        throw e;
                    } finally {
                        FilesUtils.deleteFolder(workFolder, true);
                    }
                    monitor.done();
                    ExchangeManager.getInstance().saveDownloadedExtensionsToFile(extension);
            	}
            } else {
                File installedLocation = ComponentInstaller.unzip(localZipFile.getAbsolutePath(), getComponentsFolder()
                        .getAbsolutePath());
                if (installedLocation != null) {
                    // update extesion status
                    monitor.done();
                    extensionDownloadCompleted(extension);
                }
            }
        }

        private void askReboot() {
            DisplayUtils.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    boolean reboot = MessageDialog.openQuestion(DisplayUtils.getDefaultShell(),
                            Messages.getString("DownloadComponenentsAction_restartTitle"), //$NON-NLS-1$
                            Messages.getString("DownloadComponenentsAction_restartMessage")); //$NON-NLS-1$
                    if (reboot) {
                        PlatformUI.getWorkbench().restart();
                    }
                }

            });
        }

        @Override
        public void downloadComplete() {
        }

        @Override
        public void downloadProgress(IDownloadHelper downloader, int bytesRead) {
            if (fMonitor.isCanceled()) {
                // cancel download
                downloader.setCancel(true);
                return;
            }
            fBytesDownloaded += bytesRead;
            fMonitor.setTaskName(toKbFormat(fBytesDownloaded) + fProgressLabel);
            fMonitor.worked(bytesRead);
        }

        @Override
        public void downloadStart(int totalSize) {
            fProgressLabel = "/" + toKbFormat(totalSize); //$NON-NLS-1$
            fBytesDownloaded = 0;
            fMonitor.beginTask("0 KB" + fProgressLabel, totalSize); //$NON-NLS-1$
        }

        private String toKbFormat(int size) {
            return String.format("%1$s KB", size >> 10); //$NON-NLS-1$
        }
    }

    @Override
    public void run(IIntroSite site, Properties params) {
        run();
    }
}
