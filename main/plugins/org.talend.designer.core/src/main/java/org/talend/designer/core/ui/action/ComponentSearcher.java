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
package org.talend.designer.core.ui.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.editor.IJobEditorHandler;
import org.talend.core.ui.editor.JobEditorHandlerManager;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.JobSearchResultProcessor;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class ComponentSearcher {

    private String componentName;

    private Shell shell;

    public ComponentSearcher(String componentName, Shell shell) {
        this.componentName = componentName;
        this.shell = shell;
    }

    public void run() {
        final List<IRepositoryViewObject> found = new ArrayList<IRepositoryViewObject>();

        IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                search(monitor, componentName, found);
            }

        };
        try {
            new ProgressMonitorDialog(shell).run(true, true, op);
        } catch (Exception e) {
            // ignore me
        }

        if (found.size() > 0) {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    final RepositoryReviewDialog dialog = new RepositoryReviewDialog(shell, new JobSearchResultProcessor(found),
                            ERepositoryObjectType.PROCESS);
                    if (dialog.open() == RepositoryReviewDialog.OK) {
                        RepositoryNode result = dialog.getResult();
                        openEditorOperation(result);
                    }
                }
            });
        } else {
            MessageDialog
                    .openInformation(
                            shell,
                            Messages.getString("ComponentSearcher.searchResult", componentName), Messages.getString("ComponentSearcher.noJobsFound")); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    public void openEditorOperation(RepositoryNode repositoryNode) {
        if (repositoryNode != null && repositoryNode.getObject() != null && repositoryNode.getObject().getProperty() != null
                && repositoryNode.getObject().getProperty().getItem() != null) {
            Item item = repositoryNode.getObject().getProperty().getItem();
            try {
                ERepositoryObjectType repObjType = ERepositoryObjectType.getItemType(item);
                IJobEditorHandler editorInputFactory = JobEditorHandlerManager.getInstance()
                        .extractEditorInputFactory(repObjType.getType());
                editorInputFactory.openJobEditor(editorInputFactory.createJobEditorInput(item, true));
            } catch (PartInitException e) {
                ExceptionHandler.process(e);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * DOC hcw Comment method "search".
     *
     * @param monitor
     * @param nodeName
     * @param found
     */
    protected void search(IProgressMonitor monitor, String nodeName, List<IRepositoryViewObject> found) {
        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();

        try {
            List<IRepositoryViewObject> repositoryObjectList = factory.getAll(ERepositoryObjectType.PROCESS, false);
            repositoryObjectList.addAll(factory.getAll(ERepositoryObjectType.PROCESS_MR, false));
            if (ProjectManager.getInstance().getReferencedProjects(ProjectManager.getInstance().getCurrentProject()).size() > 0) {
                for (Project refProject : ProjectManager.getInstance().getReferencedProjects(
                        ProjectManager.getInstance().getCurrentProject())) {
                    repositoryObjectList.addAll(factory.getAll(refProject, ERepositoryObjectType.PROCESS, false));
                    repositoryObjectList.addAll(factory.getAll(refProject, ERepositoryObjectType.PROCESS_MR, false));
                }
            }
            monitor.beginTask("Searching Component in Jobs ", repositoryObjectList.size()); //$NON-NLS-1$
            for (IRepositoryViewObject rObject : repositoryObjectList) {
                if (monitor.isCanceled()) {
                    break;
                }
                monitor.setTaskName("Search " + rObject.getLabel()); //$NON-NLS-1$
                monitor.worked(1);
                Item item = rObject.getProperty().getItem();
                if (item instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) item;
                    IProcess process = designerCoreService.getProcessFromProcessItem(processItem);
                    List<INode> nodes = (List<INode>) process.getGraphicalNodes();
                    for (INode node : nodes) {
                        if (node.getComponent().getName().equals(nodeName)) {
                            found.add(rObject);
                            break;
                        }
                    }
                }
            }
            monitor.done();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        // JobletEditorInput fileEditorInput = new JobletEditorInput(processItem, true);
        // IEditorPart editorPart = page.findEditor(fileEditorInput);
    }

}
