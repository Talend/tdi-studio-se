package org.talend.designer.business.diagram.custom.actions;

import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class CreateDiagramAction extends AContextualAction implements IIntroAction {

    private RepositoryNode repositoryNode;

    public CreateDiagramAction() {
        super();
        Image folderImg = ImageProvider.getImage(ECoreImage.BUSINESS_PROCESS_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
        setText(Messages.getString("CreateDiagramAction.CreateBusinessModel")); //$NON-NLS-1$
    }

    public CreateDiagramAction(boolean isToolbar) {
        super();
        setToolbar(isToolbar);
        Image folderImg = ImageProvider.getImage(ECoreImage.BUSINESS_PROCESS_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
        setText(Messages.getString("CreateDiagramAction.CreateBusinessModel")); //$NON-NLS-1$
    }

    @Override
    protected void doRun() {
        SimpleBusinessCreationWizard wizard = new SimpleBusinessCreationWizard(getActivePage().getWorkbenchWindow()
                .getWorkbench(), getPath());

        WizardDialog wizardDialog = new WizardDialog(DisplayUtils.getDefaultShell(false), wizard);
        wizardDialog.create();
        wizardDialog.open();
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        // PTODO mhelleboid refactor AContextualAction

        repositoryNode = getRepositoryNode(selection);
        boolean enabled = false;

        if (repositoryNode != null) {
            ERepositoryObjectType nodeType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
            if (repositoryNode.getType() == RepositoryNode.ENodeType.SYSTEM_FOLDER
                    || repositoryNode.getType() == RepositoryNode.ENodeType.SIMPLE_FOLDER) {
                if (nodeType == ERepositoryObjectType.BUSINESS_PROCESS) {
                    enabled = true;
                }
                if (repositoryNode.getObject() != null && repositoryNode.getObject().isDeleted()) {
                    enabled = false;
                }
            }
        }

        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            enabled = false;
        }
        if (enabled) {
            enabled = ProjectManager.getInstance().isInCurrentMainProject(repositoryNode);
        }

        setEnabled(enabled);
    }

    private RepositoryNode getRepositoryNode(IStructuredSelection selection) {
        if (!selection.isEmpty() && selection.size() == 1) {
            Object object = selection.getFirstElement();
            if (object instanceof RepositoryNode) {
                return (RepositoryNode) object;
            }
        }
        return null;
    }

    private IPath getPath() {
        IPath path;
        if (isToolbar()) {

            return null;

        } else {
            if (repositoryNode != null) {
                if (repositoryNode.getType() == ENodeType.SIMPLE_FOLDER || repositoryNode.getType() == ENodeType.SYSTEM_FOLDER) {
                    path = RepositoryNodeUtilities.getPath(repositoryNode);
                } else {
                    path = new Path(""); //$NON-NLS-1$
                }
                return path;
            }
        }
        return null;

    }

    /*
     * added by wchen for intro
     * 
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    @Override
    public void run(IIntroSite site, Properties params) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            MessageDialog.openWarning(null, "User Authority",
                    "Can't create Business Model! Current user is read-only on this project!");
        } else {
            PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
            setRepositoryNode(params);
            doRun();
        }
    }

    private void setRepositoryNode(Properties params) {

        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (null == workbenchWindow) {
            return;
        }
        IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
        if (null == workbenchPage) {
            return;
        }

        IPerspectiveDescriptor currentPerspective = workbenchPage.getPerspective();
        if (!IBrandingConfiguration.PERSPECTIVE_DI_ID.equals(currentPerspective.getId())) {
            // show di perspective
            try {
                workbenchWindow.getWorkbench().showPerspective(IBrandingConfiguration.PERSPECTIVE_DI_ID, workbenchWindow);
                workbenchPage = workbenchWindow.getActivePage();
            } catch (WorkbenchException e) {
                ExceptionHandler.process(e);
                return;
            }
        }

        // bug 16594
        IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        if (view != null) {

            Object type = params.get("type");
            if (ERepositoryObjectType.BUSINESS_PROCESS != null && ERepositoryObjectType.BUSINESS_PROCESS.name().equals(type)) {
                RepositoryNode processNode = ((ProjectRepositoryNode) view.getRoot())
                        .getRootRepositoryNode(ERepositoryObjectType.BUSINESS_PROCESS);
                final StructuredViewer viewer = view.getViewer();
                if (viewer instanceof TreeViewer) {
                    ((TreeViewer) viewer).expandToLevel(processNode, 1);
                }
                this.repositoryNode = processNode;
            }
        }
    }
}
