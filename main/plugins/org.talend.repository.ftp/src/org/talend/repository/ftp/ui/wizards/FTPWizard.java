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
package org.talend.repository.ftp.ui.wizards;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;
import org.talend.metadata.managment.ui.wizard.RepositoryWizard;
import org.talend.metadata.managment.ui.wizard.metadata.connection.Step0WizardPage;
import org.talend.repository.ftp.i18n.Messages;
import org.talend.repository.ftp.ui.wizards.pags.FTPPage;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class FTPWizard extends RepositoryWizard implements INewWizard {

    private final RepositoryNode node;

    private FTPConnection connection;

    private PropertiesWizardPage propertiesWizardPage;

    private Property connectionProperty;

    private FTPPage ftpPage;

    private ConnectionItem connectionItem;

    private boolean isToolBar;

    private String originaleObjectLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    /**
     * DOC Administrator FTPWizard constructor comment.
     *
     * @param workbench
     * @param creation
     */
    public FTPWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        super(workbench, creation);
        this.existingNames = existingNames;
        this.node = node;
        setNeedsProgressMonitor(true);
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = ConnectionFactory.eINSTANCE.createFTPConnection();
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty
                    .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                            .getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = PropertiesFactory.eINSTANCE.createFTPConnectionItem();
            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;

        case REPOSITORY_ELEMENT:
            connection = (FTPConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
            connectionProperty = node.getObject().getProperty();
            connectionItem = (ConnectionItem) node.getObject().getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        if (!creation) {
            this.originaleObjectLabel = this.connectionItem.getProperty().getDisplayName();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
        }
        // initialize the context mode
        ConnectionContextHelper.checkContextMode(connectionItem);
    }

    public FTPWizard(IWorkbench workbench, boolean creation, ISelection selection, String[] existingNames) {
        super(workbench, creation);
        this.selection = selection;
        this.existingNames = existingNames;
        setNeedsProgressMonitor(true);

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        node = (RepositoryNode) obj;
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }

        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = ConnectionFactory.eINSTANCE.createFTPConnection();
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty
                    .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                            .getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = PropertiesFactory.eINSTANCE.createFTPConnectionItem();
            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;

        case REPOSITORY_ELEMENT:
            connection = (FTPConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
            connectionProperty = node.getObject().getProperty();
            connectionItem = (ConnectionItem) node.getObject().getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        if (!creation) {
            this.originaleObjectLabel = this.connectionItem.getProperty().getDisplayName();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
        }
        // initialize the context mode
        ConnectionContextHelper.checkContextMode(connectionItem);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */

    @Override
    public void addPages() {
        String windowTitle = Messages.getString("FTPWizard.CreateNewFTPWizard");
        if (!creation) {
            windowTitle = Messages.getString("FTPWizard.EditFTPWizard");
        }
        setWindowTitle(windowTitle);//$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.UNKNOWN));
        if (isToolBar) {
            pathToSave = null;
        }
        propertiesWizardPage = new Step0WizardPage(connectionProperty, pathToSave, ERepositoryObjectType.METADATA_FILE_FTP,
                !isRepositoryObjectEditable(), creation);
        propertiesWizardPage.setTitle("Talend FTP"); //$NON-NLS-1$
        propertiesWizardPage.setDescription(Messages.getString("FTPWizard_create_ftp_conn")); //$NON-NLS-1$

        ftpPage = new FTPPage(connectionItem, isRepositoryObjectEditable(), existingNames);

        addPage(propertiesWizardPage);
        addPage(ftpPage);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */

    @Override
    public boolean canFinish() {
        if (ftpPage != null && ftpPage.isPageComplete()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        // TODO Auto-generated method stub
        return super.getNextPage(page);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.IWizard#performCancel()
     */

    @Override
    public boolean performCancel() {
        if (!creation) {
            connectionItem.getProperty().setVersion(this.originalVersion);
            connectionItem.getProperty().setDisplayName(this.originaleObjectLabel);
            connectionItem.getProperty().setDescription(this.originalDescription);
            connectionItem.getProperty().setPurpose(this.originalPurpose);
            connectionItem.getProperty().setStatusCode(this.originalStatus);
        }
        return super.performCancel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */

    @Override
    public boolean performFinish() {
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            if (creation) {
                String nextId = factory.getNextId();
                connectionProperty.setId(nextId);
                factory.create(connectionItem, propertiesWizardPage.getDestinationPath());
            } else {
                RepositoryUpdateManager.updateFileConnection(connectionItem);
                boolean isModified = propertiesWizardPage.isNameModifiedByUser();
                if (isModified) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                        IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                                IDesignerCoreService.class);
                        if (service != null) {
                            service.refreshComponentView(connectionItem);
                        }
                    }
                }
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRunnable operation = new IWorkspaceRunnable() {

                    public void run(IProgressMonitor monitor) throws CoreException {
                        try {
                            factory.save(connectionItem);
                            closeLockStrategy();
                        } catch (PersistenceException e) {
                            throw new CoreException(new Status(IStatus.ERROR, "", "", e));
                        }
                    }
                };
                workspace.run(operation, null);
            }
        } catch (Exception e) {
            String detailError = e.toString();
            new ErrorDialogWidthDetailArea(getShell(), PID, "", //$NON-NLS-1$
                    detailError);
            return false;
        }
        return true;
    }

    public void setToolBar(boolean isToolbar) {
        this.isToolBar = isToolbar;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }
}
