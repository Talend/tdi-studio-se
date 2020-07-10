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
package org.talend.designer.core.build;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.core.service.IESBRouteService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobStandaloneBuildProvider extends RepositoryObjectTypeBuildProvider {

    private static final String REST_REQUEST = "tRESTRequest";

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider#getObjectType()
     */
    @Override
    protected ERepositoryObjectType getObjectType() {
        return ERepositoryObjectType.PROCESS;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider#valid(java.util.Map)
     */
    @Override
    public boolean valid(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }

        ERepositoryObjectType type = null;
        Property property = null;
        boolean isRestServiceProvider = false;

        Object object = parameters.get(PROCESS);
        if (object != null && object instanceof IProcess2) {

            for (INode node : ((IProcess2) object).getGraphicalNodes()) {
                if (node.isActivate() && REST_REQUEST.equals(node.getComponent().getName())) {
                    isRestServiceProvider = true;
                    break;
                }
            }

            property = ((IProcess2) object).getProperty();
            if (property != null) {
                type = ERepositoryObjectType.getType(property);
            }
        }

        if (type == null) {
            object = parameters.get(ITEM);
            if (object != null && object instanceof Item) {
                property = ((Item) object).getProperty();
                if (property != null) {
                    type = ERepositoryObjectType.getType(property);
                }
                if (object instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) object;
                    for (Object node : processItem.getProcess().getNode()) {
                        NodeType nodeType = (NodeType) node;
                        if (REST_REQUEST.equals(nodeType.getComponentName())) {
                            isRestServiceProvider = true;
                            break;
                        }
                    }
                }
            }
        }
        if (type == null) {
            object = parameters.get(REPOSITORY_OBJECT);
            if (object != null && object instanceof IRepositoryViewObject) {
                IRepositoryViewObject repObject = (IRepositoryViewObject) object;
                type = repObject.getRepositoryObjectType();
                property = repObject.getProperty();
            }
        }

        if (type != null && type.equals(getObjectType()) && !isRestServiceProvider && !isServiceOperation(property)) {
            return true;
        }

        return false;
    }

    @Override
    public IMavenPomCreator createPomCreator(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }

        final Object processor = parameters.get(PROCESSOR);
        if (processor == null || !(processor instanceof IProcessor)) {
            return null;
        }
        final Object pomFile = parameters.get(FILE_POM);
        if (pomFile == null || !(pomFile instanceof IFile)) {
            return null;
        }
        final Object item = parameters.get(ITEM);
        if (item == null || !(item instanceof Item)) {
            return null;
        }
        Object argumentsMap = parameters.get(ARGUMENTS_MAP);
        if (argumentsMap == null) {
            argumentsMap = Collections.emptyMap();
        }
        if (!(argumentsMap instanceof Map)) {
            return null;
        }
        Object overwrite = parameters.get(OVERWRITE_POM);
        if (overwrite == null) {
            overwrite = Boolean.FALSE;
        }

        final Object assemblyFile = parameters.get(FILE_ASSEMBLY);
        if (!ProcessorUtilities.isGeneratePomOnly() && (assemblyFile == null || !(assemblyFile instanceof IFile))) {
            return null;
        }
        final Object winClassPath = parameters.get(CP_WIN);
        if (winClassPath == null) {
            return null;
        }
        final Object linuxClassPath = parameters.get(CP_LINUX);
        if (linuxClassPath == null) {
            return null;
        }

        final Property itemProperty = ((Item) item).getProperty();

        CreateMavenJobPom creator = null;
        if ("ROUTE".equals(itemProperty.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {

            IESBRouteService routeService = null;

            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBRouteService.class)) {
                routeService = (IESBRouteService) GlobalServiceRegister.getDefault().getService(IESBRouteService.class);
            }

            if (routeService != null) {
                creator = (CreateMavenJobPom) routeService.createMavenJobPom((IProcessor) processor, (IFile) pomFile);
            } else {
                creator = new CreateMavenJobPom((IProcessor) processor, (IFile) pomFile);
            }

        } else {
            creator = new CreateMavenJobPom((IProcessor) processor, (IFile) pomFile);
        }

        creator.setUnixClasspath(linuxClassPath.toString());
        creator.setWindowsClasspath(winClassPath.toString());

        creator.setAssemblyFile((IFile) assemblyFile);
        creator.setArgumentsMap((Map<String, Object>) argumentsMap);
        creator.setOverwrite(Boolean.parseBoolean(overwrite.toString()));

        IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(itemProperty);
        IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(itemProperty);
        if (itemLocationPath != null && objectTypeFolder != null) {
            IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
            creator.setObjectTypeFolder(objectTypeFolder);
            creator.setItemRelativePath(itemRelativePath);
        }
        return creator;
    }

    @Override
    public IBuildExportHandler createBuildExportHandler(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        final Object item = parameters.get(ITEM);
        if (item == null || !(item instanceof ProcessItem)) {
            return null;
        }
        final Object version = parameters.get(VERSION);
        if (version == null) {
            return null;
        }
        final Object contextGroup = parameters.get(CONTEXT_GROUP);
        if (contextGroup == null) {
            return null;
        }
        Object choiceOption = parameters.get(CHOICE_OPTION);
        if (choiceOption == null) {
            choiceOption = Collections.emptyMap();
        }
        if (!(choiceOption instanceof Map)) {
            return null;
        }
        IBuildJobHandler buildHandler = new BuildJobHandler((ProcessItem) item, version.toString(), contextGroup.toString(),
                (Map<ExportChoice, Object>) choiceOption);
        return buildHandler;
    }
}
