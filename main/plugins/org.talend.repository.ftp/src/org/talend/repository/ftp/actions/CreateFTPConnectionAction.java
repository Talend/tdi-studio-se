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
package org.talend.repository.ftp.actions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.ftp.i18n.Messages;
import org.talend.repository.ftp.ui.wizards.FTPWizard;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class CreateFTPConnectionAction extends AbstractCreateAction {

    private static final String EDIT_LABEL = Messages.getString("CreateFTPConnectionAction_edit_ftp"); //$NON-NLS-1$

    private static final String OPEN_LABEL = Messages.getString("CreateFTPConnectionAction_open_ftp"); //$NON-NLS-1$

    private static final String CREATE_LABEL = Messages.getString("CreateFTPConnectionAction_create_ftp"); //$NON-NLS-1$

    ImageDescriptor defaultImage = ImageProvider.getImageDesc(ECoreImage.FTP_ICON);

    ImageDescriptor createImage = OverlayImageProvider.getImageWithNew(ImageProvider.getImage(ECoreImage.FTP_ICON));

    public CreateFTPConnectionAction() {
        super();

        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(defaultImage);
    }

    public CreateFTPConnectionAction(boolean isToolbar) {
        super();
        setToolbar(isToolbar);
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(defaultImage);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.actions.metadata.AbstractCreateAction#init(org.talend.repository.model.RepositoryNode)
     */
    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (ERepositoryObjectType.METADATA_FILE_FTP != null && !ERepositoryObjectType.METADATA_FILE_FTP.equals(nodeType)) {
            setEnabled(false);
            return;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
            if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                setEnabled(false);
                return;
            }
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            this.setText(CREATE_LABEL);
            this.setImageDescriptor(createImage);
            collectChildNames(node);
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject())) {
                this.setText(EDIT_LABEL);
                this.setImageDescriptor(defaultImage);
                collectSiblingNames(node);
            } else {
                this.setText(OPEN_LABEL);
                this.setImageDescriptor(defaultImage);
            }
            break;
        default:
            return;
        }
        setEnabled(true);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualAction#doRun()
     */
    @Override
    protected void doRun() {
        RepositoryNode dbConnectionNode = getCurrentRepositoryNode();

        if (isToolbar()) {
            if (dbConnectionNode != null && dbConnectionNode.getContentType() != ERepositoryObjectType.METADATA_FILE_FTP) {
                dbConnectionNode = null;
            }
            if (dbConnectionNode == null) {
                dbConnectionNode = getRepositoryNodeForDefault(ERepositoryObjectType.METADATA_FILE_FTP);
            }
        }
        RepositoryNode metadataNode = dbConnectionNode.getParent();
        if (metadataNode != null) {
            // Force focus to the repositoryView and open Metadata and DbConnection nodes
            IRepositoryView viewPart = getViewPart();
            if (viewPart != null) {
                viewPart.setFocus();
                viewPart.expand(metadataNode, true);
                viewPart.expand(dbConnectionNode, true);
            }
        }

        FTPConnection connection = null;
        IPath pathToSave = null;

        // Define the RepositoryNode, by default Metadata/DbConnection
        RepositoryNode node = dbConnectionNode;
        ISelection selection = null;
        // When the userSelection is an element of metadataNode, use it !
        if (!isToolbar()) {
            Object userSelection = ((IStructuredSelection) getSelection()).getFirstElement();
            if (userSelection instanceof RepositoryNode) {
                switch (((RepositoryNode) userSelection).getType()) {
                case REPOSITORY_ELEMENT:
                case SIMPLE_FOLDER:
                case SYSTEM_FOLDER:
                    node = (RepositoryNode) userSelection;
                    break;
                }
            }
            selection = getSelection();
        }
        boolean creation = false;
        // Define the repositoryObject DatabaseConnection and his pathToSave
        switch (node.getType()) {
        case REPOSITORY_ELEMENT:
            // pathToSave = null;
            connection = (FTPConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
            creation = false;
            break;
        case SIMPLE_FOLDER:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            connection = ConnectionFactory.eINSTANCE.createFTPConnection();
            creation = true;
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            connection = ConnectionFactory.eINSTANCE.createFTPConnection();
            creation = true;
            break;
        }

        // Init the content of the Wizard
        init(node);
        FTPWizard ftpWizard;
        if (isToolbar()) {
            ftpWizard = new FTPWizard(PlatformUI.getWorkbench(), creation, node, getExistingNames());
            ftpWizard.setToolBar(true);
        } else {
            ftpWizard = new FTPWizard(PlatformUI.getWorkbench(), creation, selection, getExistingNames());
        }

        // Open the Wizard
        WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), ftpWizard);
        wizardDialog.setPageSize(700, 550);
        wizardDialog.create();
        wizardDialog.open();

        IRepositoryView view = getViewPart();
        if (view != null) {
            view.expand(dbConnectionNode, true);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualAction#getClassForDoubleClick()
     */
    @Override
    public Class getClassForDoubleClick() {
        return FTPConnectionItem.class;
    }

}
