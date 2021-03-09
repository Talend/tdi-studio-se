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
package org.talend.repository.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.projectsetting.IProjectSettingContainer;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.actions.ExportProjectSettings;
import org.talend.repository.ui.actions.ImportProjectSettings;

/**
 * wchen class global comment. Detailled comment
 */
public class ProjectSettingsPreferenceDialog extends PreferenceDialog implements IProjectSettingContainer {

    private static final String TEMP_PRODUCT_SETTING_XML = "TempProductSetting.xml"; //$NON-NLS-1$

    private Button importButton;

    private Button exportButton;

    public static final int IMPORT = 97;

    public static final int EXPORT = 98;

    private static boolean isInReopen = false;

    /**
     * wchen ProjectSettingsPreferenceDialog constructor comment.
     *
     * @param parentShell
     * @param manager
     */
    public ProjectSettingsPreferenceDialog(Shell parentShell, PreferenceManager manager) {
        super(parentShell, manager);
        unloadProject();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (!factory.isUserReadOnlyOnCurrentProject()) {
            importButton = createButton(parent, IMPORT, "Import", false); //$NON-NLS-1$
            exportButton = createButton(parent, EXPORT, "Export", false); //$NON-NLS-1$
        }
        super.createButtonsForButtonBar(parent);

    }

    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case IDialogConstants.OK_ID: {
            okPressed();
            commiteProjectSettings();
            return;
        }
        case IDialogConstants.CANCEL_ID: {
            cancelPressed();
            return;
        }
        case IDialogConstants.HELP_ID: {
            helpPressed();
            return;
        }
        case IMPORT: {
            importPressed();
            commiteProjectSettings();
            return;
        }
        case EXPORT: {
            exportPressed();
            return;
        }
        }
    }

    private void commiteProjectSettings() {
        // excute a workUnit to do svn commite
        RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(ProjectManager.getInstance().getCurrentProject(), "") { //$NON-NLS-1$

            @Override
            public void run() throws PersistenceException {
                // do nothing
            }
        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);

    }

    @Override
    protected void okPressed() {
        super.okPressed();
        rollBack(false);
        setInReopen(false);
    }

    /**
     * roll back if click cancel button and delete temp file.
     *
     * @param rollback
     */
    private void rollBack(boolean rollback) {
        File file = new File(TEMP_PRODUCT_SETTING_XML);
        if (file.exists()) {
            if (rollback) {
                ImportProjectSettings settings = new ImportProjectSettings(TEMP_PRODUCT_SETTING_XML);
                try {
                    settings.updateProjectSettings();
                } catch (Exception e) {
                    //
                }
            }
            file.delete();
        }
    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        rollBack(true);
        setInReopen(false);
    }

    private void importPressed() {

        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        String[] files = new String[] { "*.xml" }; //$NON-NLS-1$
        fileDialog.setFilterExtensions(files);

        String path = fileDialog.open();
        ImportProjectSettings settings = new ImportProjectSettings(path);

        boolean error = false;
        ExportProjectSettings original = new ExportProjectSettings(TEMP_PRODUCT_SETTING_XML);
        original.saveProjectSettings();
        try {
            settings.updateProjectSettings();
        } catch (Exception e) {
            error = true;
            showErrorMessage();
        }

        // close the projec settings and open it again to get new settings
        if (!error) {
            setInReopen(true);
            close();
            ProjectSettingDialog dialog = new ProjectSettingDialog();
            dialog.open();
            setInReopen(false);
        }
    }

    private void exportPressed() {
        saveCurrentSettings();
        FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
        fileDialog.setFileName("ProjectSettings.xml"); //$NON-NLS-1$
        String[] files = new String[] { "*.xml" }; //$NON-NLS-1$
        fileDialog.setFilterExtensions(files);

        String path = fileDialog.open();
        if (path != null) {
            if (!new File(path).exists()
                    || MessageDialog.openConfirm(getShell(),
                            Messages.getString("ProjectSettingsPreferenceDialog.overwriteTitle"), //$NON-NLS-1$
                            Messages.getString("ProjectSettingsPreferenceDialog.overwriteMessage"))) { //$NON-NLS-1$
                ExportProjectSettings settings = new ExportProjectSettings(path);
                settings.saveProjectSettings();
            }
        }
    }

    private void showErrorMessage() {
        MessageBox message = new MessageBox(DisplayUtils.getDefaultShell(false), SWT.ICON_ERROR | SWT.OK);
        message.setMessage(Messages.getString("ImportProjectSettings.Error")); //$NON-NLS-1$
        message.open();
    }

    protected void saveCurrentSettings() {
        SafeRunnable.run(new SafeRunnable() {

            private boolean errorOccurred;

            @Override
            public void run() {
                errorOccurred = false;
                boolean hasFailedOK = false;
                try {
                    Iterator nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
                    while (nodes.hasNext()) {
                        IPreferenceNode node = (IPreferenceNode) nodes.next();
                        IPreferencePage page = node.getPage();
                        if (page != null) {
                            if (!page.performOk()) {
                                hasFailedOK = true;
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    handleException(e);
                } finally {

                    if (hasFailedOK) {
                        setReturnCode(FAILED);
                        return;
                    }

                    if (!errorOccurred) {

                        handleSave();
                    }
                    setReturnCode(OK);
                }
            }

            @Override
            public void handleException(Throwable e) {
                errorOccurred = true;

                Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, 0, e.toString(), e));

                clearSelectedNode();
                String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$

                Policy.getStatusHandler().show(new Status(IStatus.ERROR, Policy.JFACE, message, e),
                        JFaceResources.getString("Error")); //$NON-NLS-1$

            }
        });
    }

    void clearSelectedNode() {
        setSelectedNodePreference(null);
    }

    private void unloadProject() {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final ProxyRepositoryFactory instance = ProxyRepositoryFactory.getInstance();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            try {
                if (!instance.isLocalConnectionProvider()) {
                    // instance.getRepositoryFactoryFromProvider().reloadProject(currentProject);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    private void fillParentsFolderInfos(FolderItem folderItem) {
        for (Item curItem : (List<Item>) folderItem.getChildren()) {
            if (curItem instanceof FolderItem) {
                fillParentsFolderInfos((FolderItem) curItem);
            }
            curItem.setParent(folderItem);
        }
    }

    @Override
    public boolean addChildrenPreferenceNodes(String parentId, List<IPreferenceNode> childrenNodes) {
        IPreferenceNode findNodeMatching = findNodeMatching(parentId);
        if (findNodeMatching != null && childrenNodes != null) {
            IPreferenceNode[] subNodes = findNodeMatching.getSubNodes();

            // remove old one to make sure the add nodes is front.
            if (subNodes != null) {
                for (IPreferenceNode n : subNodes) {
                    findNodeMatching.remove(n);
                }
            }
            // add new nodes.
            for (IPreferenceNode n : childrenNodes) {
                findNodeMatching.add(n);
            }
            // add back the original nodes.
            for (IPreferenceNode n : subNodes) {
                findNodeMatching.add(n);
            }

            this.getTreeViewer().refresh();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChildrenPreferenceNodes(String parentId, List<String> childrenIds) {
        IPreferenceNode findNodeMatching = findNodeMatching(parentId);
        if (findNodeMatching != null && childrenIds != null) {
            // remove existed nodes.
            List<IPreferenceNode> recordRemovedNodes = new ArrayList<IPreferenceNode>();
            boolean nonExisted = false;
            for (String childId : childrenIds) {
                IPreferenceNode removedNode = findNodeMatching.remove(childId);
                if (removedNode != null) {
                    recordRemovedNodes.add(removedNode);
                } else {
                    nonExisted = true;
                    break;
                }
            }
            // add back the delete nodes
            if (nonExisted) {
                for (IPreferenceNode node : recordRemovedNodes) {
                    findNodeMatching.add(node);
                }
                return false;
            } else {
                this.getTreeViewer().refresh();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean openPage(String nodeId, Object data) {
        if (this instanceof IWorkbenchPreferenceContainer) {
            ((IWorkbenchPreferenceContainer) this).openPage(nodeId, data);
        } else { // impl by self
            final IPreferenceNode node = findNodeMatching(nodeId);
            if (node != null) {
                getTreeViewer().setSelection(new StructuredSelection(node));
                showPage(node);
            }
            IPreferencePage page = getCurrentPage();
            if (page instanceof PreferencePage) {
                ((PreferencePage) page).applyData(data);
            }
            return true;
        }
        return false;
    }

    public static boolean isInReopen() {
        return isInReopen;
    }

    public static void setInReopen(boolean isInReopen) {
        ProjectSettingsPreferenceDialog.isInReopen = isInReopen;
    }
}
