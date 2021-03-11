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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.utils.XmiResourceManager;

/**
 *
 */
public class ChangeXmiSerialization extends AbstractProjectMigrationTask {

    private XmiResourceManager xmiResourceManager;

    private Collection<Resource> modifiedResources = new ArrayList<Resource>();

    @Override
    public ExecutionResult execute(Project project) {
        try {
            if (!project.isLocal()) {
                return ExecutionResult.NOTHING_TO_DO;
            }

            IProject iProject = ResourceUtils.getProject(project);
            xmiResourceManager = new XmiResourceManager();
            xmiResourceManager.setUseOldProjectFile(true);
            if (!xmiResourceManager.hasTalendProjectFile(iProject)) {
                return ExecutionResult.NOTHING_TO_DO;
            }

            PropertiesResourcesCollector propertiesResourcesCollector = new PropertiesResourcesCollector();
            iProject.accept(propertiesResourcesCollector);
            Collection<IFile> propertiesResourcesFiles = propertiesResourcesCollector.getPropertiesResourcesFiles();

            for (IFile file : propertiesResourcesFiles) {
                Property property = xmiResourceManager.loadProperty(file);

                // we access the author (resolve proxy)
                boolean correctAuthor = false;
                try {
                    property.getAuthor();
                } catch (Exception e) {
                    // author is null : fix old bug
                    correctAuthor = true;
                }
                if (correctAuthor || property.getAuthor().getLogin() == null) {
                    property.setAuthor(project.getAuthor());
                }

                EcoreUtil.resolveAll(property.eResource());

                // if .properties is saved then migrated .item must also be saved
                if (property.getItem() instanceof BusinessProcessItem) {
                    BusinessProcessItem businessProcessItem = (BusinessProcessItem) property.getItem();
                    businessProcessItem.getNotation();
                }

                modifiedResources.add(property.eResource());
            }

            Resource projectResource = xmiResourceManager.loadProject(iProject).eResource();
            xmiResourceManager.setUseOldProjectFile(false);
            Resource newProjectResource = xmiResourceManager.createProjectResource(iProject);
            EObject[] objects = (EObject[]) projectResource.getContents().toArray();
            for (EObject object : objects) {
                newProjectResource.getContents().add(object);
            }

            modifiedResources.add(newProjectResource);

            for (Resource resource : modifiedResources) {
                try {
                    resource.setModified(true);
                    xmiResourceManager.saveResource(resource);
                } catch (PersistenceException e) {
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }
            }

            xmiResourceManager.deleteResource(projectResource);

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     *
     */
    private final class PropertiesResourcesCollector implements IResourceVisitor {

        private Collection<IFile> propertiesResourcesFiles = new ArrayList<IFile>();

        @Override
        public boolean visit(IResource resource) {
            if (resource.getType() == IResource.FILE) {
                IFile file = (IFile) resource;
                if (xmiResourceManager.isPropertyFile(file)) {
                    propertiesResourcesFiles.add(file);
                }
            }
            return true;
        }

        public Collection<IFile> getPropertiesResourcesFiles() {
            return propertiesResourcesFiles;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
