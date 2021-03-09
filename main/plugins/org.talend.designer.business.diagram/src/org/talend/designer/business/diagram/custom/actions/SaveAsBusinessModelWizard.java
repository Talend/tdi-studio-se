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
package org.talend.designer.business.diagram.custom.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessProcess;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditor;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;

/**
 * Wizard for the creation of a new project. <br/>
 *
 * $Id: NewProcessWizard.java 46332 2010-08-05 06:48:56Z cli $
 *
 */
public class SaveAsBusinessModelWizard extends Wizard {

    /** Main page. */
    private SimpleBusinessCreationWizardPage mainPage;

    /** Created project. */
    private BusinessProcessItem businessProcessItem;

    private Property property;

    private IPath path;

    private IProxyRepositoryFactory repositoryFactory;

    private RepositoryEditorInput repositoryEditorInput;

    private IFile file;

    private Property oldProperty;

    private boolean isUpdate;

    private BusinessProcessItem oldBusinessProcessItem;

    public SaveAsBusinessModelWizard(EditorPart editorPart) {

        this.repositoryEditorInput = ((BusinessDiagramEditor) editorPart).getDiagramEditorInput();

        RepositoryNode repositoryNode = repositoryEditorInput.getRepositoryNode();
        // see: RepositoryEditorInput.setRepositoryNode(IRepositoryNode repositoryNode)
        if (repositoryNode == null) {
            repositoryNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService()
                    .getRepositoryNode(repositoryEditorInput.getItem().getProperty().getId(), false);
        }

        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        this.path = service.getRepositoryPath((RepositoryNode) repositoryNode);

        oldBusinessProcessItem = (BusinessProcessItem) repositoryEditorInput.getItem();
        oldProperty = oldBusinessProcessItem.getProperty();

        this.property = PropertiesFactory.eINSTANCE.createProperty();

        assginVlaues(this.property, oldProperty);

        businessProcessItem = PropertiesFactory.eINSTANCE.createBusinessProcessItem();

        businessProcessItem.setProperty(property);

        repositoryFactory = service.getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    public void addPages() {
        mainPage = new SimpleBusinessCreationWizardPage(property, path);
        mainPage.initializeSaveAs(oldProperty.getLabel(), oldProperty.getVersion(), true);

        // overwrite it.
        mainPage.setTitle("Save As");
        mainPage.setDescription("Save as another new business model.");

        addPage(mainPage);
        setWindowTitle("Save As");
    }

    public boolean performFinish() {

        boolean ok = false;
        try {

            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            DiagramResourceManager diagramResourceManager = new DiagramResourceManager(page, new NullProgressMonitor());
            file = diagramResourceManager.createDiagramFile();

            isUpdate = isUpdate();

            if (isUpdate) {
                update();
            } else {
                property.setId(repositoryFactory.getNextId());

                diagramResourceManager.updateFromResource(businessProcessItem, file);

                BusinessProcess businessProcess = BusinessFactory.eINSTANCE.createBusinessProcess();
                businessProcessItem.setSemantic(businessProcess);

                // don't set these values directly
                // businessProcessItem.setSemantic(oldItem.getSemantic());
                // businessProcessItem.setNotation(oldItem.getNotation());
                // businessProcessItem.setNotationHolder(oldItem.getNotationHolder());

                repositoryFactory.create(businessProcessItem, mainPage.getDestinationPath());
            }
            ok = true;

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error", "Business model could not be saved" + " : " + e.getMessage());
            ExceptionHandler.process(e);
        }

        return ok;
    }

    private void update() {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    assginVlaues(oldProperty, property);

                    repositoryFactory.save(oldBusinessProcessItem);
                    // assign value
                    businessProcessItem = oldBusinessProcessItem;
                } catch (PersistenceException pe) {
                    throw new CoreException(new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
                            "persistance error", pe)); //$NON-NLS-1$
                }
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();
            // the update the project files need to be done in the workspace runnable to avoid all notification
            // of changes before the end of the modifications.
            workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            MessageBoxExceptionHandler.process(e.getCause());
        }
    }

    public BusinessProcessItem getBusinessProcessItem() {
        return this.businessProcessItem;
    }

    public IFile getTempFile() {
        return this.file;
    }

    // left = right
    private void assginVlaues(Property leftProperty, Property rightProperty) {
        // 6 fields, don't contains the "locker" and "path". and author , they are the same.
        leftProperty.setLabel(rightProperty.getLabel());
        leftProperty.setPurpose(rightProperty.getPurpose());
        leftProperty.setDescription(rightProperty.getDescription());
        // same author as old one.
        leftProperty.setAuthor(rightProperty.getAuthor());
        leftProperty.setVersion(rightProperty.getVersion());
        leftProperty.setStatusCode(rightProperty.getStatusCode());
    }

    // if name is different, it will create a new job, if name is the same, means to update the job(version or
    // description...)
    private boolean isUpdate() {
        if (oldProperty.getLabel().trim().equalsIgnoreCase(property.getLabel().trim())) {
            return true;
        } else {
            return false;
        }
    }
}
