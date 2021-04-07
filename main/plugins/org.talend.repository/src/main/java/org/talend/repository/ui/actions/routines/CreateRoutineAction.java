// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions.routines;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.wizards.routines.NewInnerRoutineWizard;
import org.talend.repository.ui.wizards.routines.NewRoutineWizard;

/**
 * Action that will edit routines.
 *
 * $Id: EditRoutinesAction.java 906 2006-12-08 02:18:54 +0000 (ven., 08 déc. 2006) rli $
 *
 */
public class CreateRoutineAction extends AbstractRoutineAction {

    public CreateRoutineAction() {
        super();

        setText(Messages.getString("CreateRoutineAction.text.createRoutine")); //$NON-NLS-1$
        setToolTipText(Messages.getString("CreateRoutineAction.toolTipText.createRoutine")); //$NON-NLS-1$

        Image folderImg = ImageProvider.getImage(ECoreImage.ROUTINE_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    public CreateRoutineAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
            switch (node.getType()) {
            case SIMPLE_FOLDER:
            case SYSTEM_FOLDER:
                if (nodeType != ERepositoryObjectType.ROUTINES) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().isDeleted()) {
                    canWork = false;
                }
                break;
            case REPOSITORY_ELEMENT:
                if (nodeType != ERepositoryObjectType.ROUTINESJAR) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        // RepositoryNode codeNode = getViewPart().getRoot().getChildren().get(4);
        // RepositoryNode routineNode = codeNode.getChildren().get(0);
        RepositoryNode routineNode = getCurrentRepositoryNode();

        if (isToolbar()) {
            if (routineNode != null && routineNode.getContentType() != ERepositoryObjectType.ROUTINES) {
                routineNode = null;
            }
            if (routineNode == null) {
                routineNode = getRepositoryNodeForDefault(ERepositoryObjectType.ROUTINES);
            }
        }
        RepositoryNode node = null;
        IPath path = null;
        if (!isToolbar()) {
            ISelection selection = getSelection();
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            node = (RepositoryNode) obj;
            path = RepositoryNodeUtilities.getPath(node);
        }
        NewRoutineWizard routineWizard;
        if (ERepositoryObjectType.ROUTINESJAR == node.getParent().getContentType()) {
            routineWizard = new NewInnerRoutineWizard(node, path);
        } else {
            routineWizard = new NewRoutineWizard(path);
        }
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), routineWizard);

        if (dlg.open() == Window.OK) {
            try {
                RoutineItem routineItem = routineWizard.getRoutine();
                openRoutineEditor(routineItem, false);
                if (RoutinesUtil.isInnerCodes(routineItem.getProperty())) {
                    IRunProcessService.get()
                            .getTalendCodesJarJavaProject(CodesJarResourceCache.getCodesJarByInnerCode(routineItem))
                            .buildWholeCodeProject();
                } else {
                    IRunProcessService.get().getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES).buildWholeCodeProject();
                }
            } catch (PartInitException e) {
                MessageBoxExceptionHandler.process(e);
            } catch (SystemException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
    }

}
