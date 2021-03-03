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
package org.talend.repository.ui.actions.routines;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.SystemException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public abstract class AbstractRoutineAction extends AContextualAction {

    protected RepositoryNode repositoryNode;

    /**
     * DOC smallet Comment method "openRoutineEditor".
     *
     * @param routineItem
     * @throws SystemException
     * @throws PartInitException
     */
    protected IEditorPart openRoutineEditor(RoutineItem routineItem, boolean readOnly) throws SystemException, PartInitException {
        if (routineItem == null) {
            return null;
        }
        ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);

        ECodeLanguage lang = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLanguage();
        ITalendSynchronizer routineSynchronizer;
        switch (lang) {
        case JAVA:
            routineSynchronizer = service.createJavaRoutineSynchronizer();
            break;
        case PERL:
            routineSynchronizer = service.createPerlRoutineSynchronizer();
            break;
        default:
            throw new UnsupportedOperationException(Messages.getString("AbstractRoutineAction.unknowLanguage") + lang); //$NON-NLS-1$
        }

        // check if the related editor is open.
        IWorkbenchPage page = getActivePage();

        IEditorReference[] editorParts = page.getEditorReferences();
        String talendEditorID = "org.talend.designer.core.ui.editor.StandAloneTalend" + lang.getCaseName() + "Editor"; //$NON-NLS-1$ //$NON-NLS-2$
        boolean found = false;
        IEditorPart talendEditor = null;
        for (IEditorReference reference : editorParts) {
            IEditorPart editor = reference.getEditor(false);
            if (talendEditorID.equals(editor.getSite().getId())) {
                // TextEditor talendEditor = (TextEditor) editor;
                RepositoryEditorInput editorInput = (RepositoryEditorInput) editor.getEditorInput();
                if (editorInput.getItem().equals(routineItem)) {
                    page.bringToTop(editor);
                    found = true;
                    talendEditor = editor;
                    break;
                }
            }
        }

        if (!found) {
            IFile file = null;
            ProjectManager projectManager = ProjectManager.getInstance();
            boolean flag = projectManager.isInCurrentMainProject(routineItem);
            if (!flag) { // is ref project
                file = routineSynchronizer.getRoutinesFile(routineItem);
            } else {// need open from item file with multiple version
                boolean needForceRefresh = false;
                if (!ProxyRepositoryFactory.getInstance().isLocalConnectionProvider()) {
                    // in case only routine.item file modified, better refresh every time, especially for git mode
                    needForceRefresh = true;
                }
                routineSynchronizer.syncRoutine(routineItem, true, needForceRefresh);
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                String lastVersion = factory.getLastVersion(routineItem.getProperty().getId()).getVersion();
                String curVersion = routineItem.getProperty().getVersion();
                if (curVersion != null && curVersion.equals(lastVersion)) {
                    file = routineSynchronizer.getFile(routineItem);
                } else {
                    file = routineSynchronizer.getRoutinesFile(routineItem);
                }
            }
            if (file == null) {
                return null;
            }
            RepositoryEditorInput input = new RoutineEditorInput(file, routineItem);
            input.setReadOnly(readOnly);
            talendEditor = page.openEditor(input, talendEditorID);
        }

        return talendEditor;

    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        Object o = selection.getFirstElement();
        if (selection.isEmpty() || selection.size() != 1 || !(o instanceof RepositoryNode)) {
            return;
        }
        repositoryNode = (RepositoryNode) o;
    }

}
