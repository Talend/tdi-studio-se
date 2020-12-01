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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 *
 */
public abstract class ConfigExternalLibPage extends WizardPage {

    private IStructuredSelection selection;

    private IRepositoryViewObject object;

    /**
     * Getter for selection.
     *
     * @return the selection
     */
    public IStructuredSelection getSelection() {
        return this.selection;
    }

    /**
     * An abstract class for import external Lib.
     *
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConfigExternalLibPage(String name, IStructuredSelection selection) {
        super(name);
        this.selection = selection;
    }

    public boolean isReadOnly() {
        boolean readonly = false;
        IProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
        RepositoryNode selectedRepositoryNode = getSelectedRepositoryNode();
        ERepositoryStatus status = repFactory.getStatus(selectedRepositoryNode.getObject());
        Item item = selectedRepositoryNode.getObject().getProperty().getItem();
        ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
        // node can't be lock in offline mode , so use isOpendItemInEidtor instead.
        boolean isOpened = coreService.isOpenedItemInEditor(selectedRepositoryNode.getObject());
        boolean mainProject = ProjectManager.getInstance().isInCurrentMainProject(selectedRepositoryNode);
        boolean isSysRoutine = false;
        if (item instanceof RoutineItem) {
            if (((RoutineItem) item).isBuiltIn()) {
                isSysRoutine = true;
            }
        }
        boolean userReadOnlyOnCurrentProject = repFactory.isUserReadOnlyOnCurrentProject();
        // when routine is
        // 1. system routne
        // 2. in the ref project
        // 3. routine is locked (when offline mode use isOpened, when default mode use LOCK_BY_USER)
        // 4. user is readonly on current project
        // 5. Tag project
        // then set readonly
        boolean isOffline = getRepositoryContext().isOffline();
        if (isOnTag() || userReadOnlyOnCurrentProject || (isOffline && isOpened) || isSysRoutine || !mainProject
                || (!isOffline && (status == ERepositoryStatus.LOCK_BY_OTHER || status == ERepositoryStatus.LOCK_BY_USER))) {
            readonly = true;
        }
        return readonly;
    }

    private boolean isOnTag() {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        String branch = ProjectManager.getCurrentBranchLabel(currentProject);
        return isOnTagGit(branch) || isOnTagSvn(branch);
    }

    private boolean isOnTagGit(String branch) {
        if (branch == null) {
            return false;
        }
        return branch.startsWith(ProjectManager.NAME_TAGS + ProjectManager.SEP_CHAR);
    }

    private boolean isOnTagSvn(String branch) {
        if (branch == null) {
            return false;
        }
        return branch.startsWith(SVNConstant.SEP_CHAR + SVNConstant.NAME_TAGS + SVNConstant.SEP_CHAR)
                || branch.startsWith(SVNConstant.NAME_TAGS + SVNConstant.SEP_CHAR);
    }

    private RepositoryContext getRepositoryContext() {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        return (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
    }
    /**
     * Subclasses should implement this for its own business.
     *
     * @return
     */
    public boolean finish() {

        // maybe need process the pig udf and bean also. not only for routine.
//        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
//            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
//                    IRunProcessService.class);
//            ITalendProcessJavaProject talendProcessJavaProject = service.getTalendProcessJavaProject();
//            if (talendProcessJavaProject != null) {
//                talendProcessJavaProject.updateRoutinesPom(true, true);
//            }
//        }
        return true;
    }

    public void cancel() {

    }

    public RepositoryNode getSelectedRepositoryNode() {
        return (RepositoryNode) this.getSelection().getFirstElement();
    }

    public RoutineItem getSelectedRoutine() {
        RepositoryNode node = getSelectedRepositoryNode();
        Item item = node.getObject().getProperty().getItem();
        return (RoutineItem) item;
    }

}
