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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.util.DiagramFileCreator;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.util.EditorUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.ui.editor.RepositoryEditionManager;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.BusinessProcess;
import org.talend.designer.business.model.business.diagram.edit.parts.BusinessProcessEditPart;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditorUtil;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramFileCreator;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DiagramResourceManager {

    private IWorkbenchPage page;

    private IProgressMonitor progressMonitor;

    public DiagramResourceManager(IWorkbenchPage page, IProgressMonitor progressMonitor) {
        this.page = page;
        this.progressMonitor = progressMonitor;
    }

    public DiagramResourceManager() {
    }

    public IFile createDiagramFile() {
        DiagramFileCreator fileCreator = BusinessDiagramFileCreator.getInstance();
        IPath temporaryFilePath = RepositoryEditionManager.getInstance().getTemporaryFilePath();
        IPath containerFullPath = temporaryFilePath.removeLastSegments(1);
        String fileName = temporaryFilePath.lastSegment();
        InputStream initialContents = EditorUtil.getInitialContents();
        String diagramKind = BusinessProcessEditPart.MODEL_ID;
        Shell shell = page.getWorkbenchWindow().getShell();

        IFile file = BusinessDiagramEditorUtil.createNewDiagramFile(fileCreator, containerFullPath, fileName, initialContents,
                diagramKind, shell, progressMonitor);

        return file;
    }

    public void updateFromResource(BusinessProcessItem businessProcessItem, IFile file) {
        Resource resource = createResource(file);

        BusinessProcess semantic = (BusinessProcess) EcoreUtil.getObjectByType(resource.getContents(), BusinessPackage.eINSTANCE
                .getBusinessProcess());
        Diagram notation = (Diagram) EcoreUtil.getObjectByType(resource.getContents(), NotationPackage.eINSTANCE.getDiagram());

        businessProcessItem.setSemantic(semantic);
        businessProcessItem.setNotation(notation);
    }

    public void updateResource(BusinessProcessItem businessProcessItem, IFile file) {
        Resource resource = createResource(file);

        // don't seems to be usefull
        // EcoreUtil.resolveAll(businessProcessItem.eResource());

        resource.getContents().clear();
        resource.getContents().add(EcoreUtil.copy(businessProcessItem.getSemantic()));
        resource.getContents().add(businessProcessItem.getNotation());

        try {
            resource.save(null);
        } catch (IOException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    public IEditorPart openEditor(BusinessProcessItem businessProcessItem, IFile file, boolean forceReadOnly) {
        return RepositoryEditionManager.getInstance().openEditor(page, file, businessProcessItem, forceReadOnly);
    }

    private Resource createResource(IFile file) {
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(file.getFullPath().toString()), true);
        return resource;
    }
}
