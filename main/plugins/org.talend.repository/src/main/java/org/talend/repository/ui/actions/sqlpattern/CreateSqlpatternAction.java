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
package org.talend.repository.ui.actions.sqlpattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.wizards.sqlpattern.NewSqlpatternWizard;

/**
 * Action that will edit routines.
 *
 * $Id: EditRoutinesAction.java 906 2006-12-08 02:18:54 +0000 (ven., 08 déc. 2006) rli $
 *
 */
public class CreateSqlpatternAction extends AbstractSqlpatternAction {

    boolean isFromSqlPatternComposite = false;

    String sqlPatternPath;

    public CreateSqlpatternAction() {
        super();

        setText("Create SQLTemplate"); //$NON-NLS-1$
        setToolTipText("Create SQLtempalte"); //$NON-NLS-1$

        Image folderImg = ImageProvider.getImage(ECoreImage.METADATA_SQLPATTERN_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    public CreateSqlpatternAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    public CreateSqlpatternAction(String path, boolean isFromSqlPatternComposite) {
        this();
        this.sqlPatternPath = path;
        this.isFromSqlPatternComposite = isFromSqlPatternComposite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case SIMPLE_FOLDER:
                if (!isUnderUserDefined(node)) {
                    canWork = false;
                }
                break;
            case SYSTEM_FOLDER:
                Object obj = node.getProperties(EProperties.LABEL);
                if (obj instanceof String) {
                    String nodeLabel = (String) obj;
                    if (!nodeLabel.equals(RepositoryConstants.USER_DEFINED)) {
                        canWork = false;
                    }
                    break;
                }
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
        RepositoryNode sqlPatternNode = null;
        if (!isFromSqlPatternComposite) {
            sqlPatternNode = getCurrentRepositoryNode();
        }
        if (isToolbar()) {
            if (sqlPatternNode != null && sqlPatternNode.getContentType() != ERepositoryObjectType.SQLPATTERNS) {
                sqlPatternNode = null;
            }
            if (sqlPatternNode == null) {
                sqlPatternNode = getRepositoryNodeForDefault(ERepositoryObjectType.SQLPATTERNS);
            }
        }
        RepositoryNode node = null;
        IPath path;

        if (this.sqlPatternPath != null) {
            path = new Path(this.sqlPatternPath);
        }

        else if (isToolbar()) {
            path = RepositoryNodeUtilities.getPath(sqlPatternNode);

        } else {
            ISelection selection = getSelection();
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            node = (RepositoryNode) obj;
            path = RepositoryNodeUtilities.getPath(node);
        }

        NewSqlpatternWizard routineWizard = new NewSqlpatternWizard(path);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), routineWizard);

        if (dlg.open() == Window.OK) {
            try {
                openSQLPatternEditor(routineWizard.getSQLPattern(), false);
            } catch (PartInitException e) {
                MessageBoxExceptionHandler.process(e);
            } catch (SystemException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
    }

}
