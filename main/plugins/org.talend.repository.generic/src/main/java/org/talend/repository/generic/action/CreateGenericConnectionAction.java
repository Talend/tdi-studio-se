package org.talend.repository.generic.action;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.generic.i18n.Messages;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.ui.GenericConnWizard;
import org.talend.repository.generic.ui.common.GenericWizardDialog;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * 
 * created by ycbai on 2015年9月14日 Detailled comment
 *
 */
public class CreateGenericConnectionAction extends AbstractCreateAction {

    private static final int DEFAULT_WIZARD_WIDTH = 700;

    private static final int DEFAULT_WIZARD_HEIGHT = 400;

    private boolean creation = true;

    private ERepositoryObjectType repObjType;

    public CreateGenericConnectionAction() {
        super();
    }

    @Override
    protected void doRun() {
        IWizard wizard = new GenericConnWizard(PlatformUI.getWorkbench(), creation, repositoryNode, getExistingNames());
        WizardDialog wizardDialog = new GenericWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                wizard, new GenericWizardInternalService().getComponentService());
        if (Platform.getOS().equals(Platform.OS_LINUX)) {
            wizardDialog.setPageSize(getWizardWidth(), getWizardHeight() + 80);
        }
        wizardDialog.create();
        wizardDialog.open();
    }

    @Override
    protected void init(RepositoryNode node) {
        if (!isGenericConnection(node)) {
            setEnabled(false);
            return;
        }
        repositoryNode = getCurrentRepositoryNode();
        repObjType = repositoryNode.getObjectType();
        if(repObjType == null){
            repObjType =  (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
        }
        this.setText(getCreateLabel());
        this.setToolTipText(getEditLabel());
        Image nodeImage = getNodeImage();
        if (nodeImage != null) {
            this.setImageDescriptor(ImageDescriptor.createFromImage(nodeImage));
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                setEnabled(false);
                return;
            }
            this.setText(getCreateLabel());
            collectChildNames(node);
            creation = true;
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject()) && isLastVersion(node)) {
                this.setText(getEditLabel());
                collectSiblingNames(node);
            } else {
                this.setText(getOpenLabel());
            }
            creation = false;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    protected int getWizardWidth() {
        return DEFAULT_WIZARD_WIDTH;
    }

    protected int getWizardHeight() {
        return DEFAULT_WIZARD_HEIGHT;
    }

    protected String getCreateLabel() {
        return Messages.getString("CreateGenericConnectionAction.createLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getEditLabel() {
        return Messages.getString("CreateGenericConnectionAction.editLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getOpenLabel() {
        return Messages.getString("CreateGenericConnectionAction.openLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getNodeLabel() {
        return repObjType.getLabel();
    }

    protected Image getNodeImage() {
        if (isGenericConnection(repositoryNode)) {
            return GenericWizardServiceFactory.getGenericWizardService().getNodeImage(repObjType.getType());
        }
        return null;
    }

    private boolean isGenericConnection(RepositoryNode node) {
        ERepositoryObjectType contentType = node.getObjectType();
        if(contentType == null){
            contentType =  (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        }
        boolean isGenericType = GenericWizardServiceFactory.getGenericWizardService().isGenericType(contentType);
        return isGenericType;
    }

    @Override
    public Class getClassForDoubleClick() {
        return GenericConnectionItem.class;
    }

}
