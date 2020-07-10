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
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildType;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.tools.creator.CreateMavenStandardJobOSGiPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.constants.BuildJobConstants;
import org.talend.repository.ui.wizards.exportjob.handler.BuildOSGiBundleHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * Build Provider for OSGI build type (ESB bundle)
 */
public class StandardJobOSGiBundleBuildProvider extends RepositoryObjectTypeBuildProvider {

    private static final String OSGI = "OSGI";

    @Override
    public boolean valid(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return false;
        }
        
        Object object = parameters.get(PROCESS);
        if (object != null && object instanceof IProcess2) {
            Property property = ((IProcess2) object).getProperty();
            if (isOsgiBuildType(property)) {
                return true;
            }
            
            IProcess2 process = (IProcess2) object;
            for (INode node : process.getGraphicalNodes()) {
                if (node.isActivate() && BuildJobConstants.esbComponents.contains(node.getComponent().getName())) {
                    fixDefaultBuildType(property);
                    return true;
                }
            }
        }
        object = parameters.get(ITEM);
        if (object != null && object instanceof ProcessItem) {
            Property property = ((Item) object).getProperty();
            if (isOsgiBuildType(property)) {
                return true;
            }
            
            ProcessItem processItem = (ProcessItem) object;
            for (Object node : processItem.getProcess().getNode()) {
                NodeType nodeType = (NodeType) node;
                if (BuildJobConstants.esbComponents.contains(nodeType.getComponentName())) {
                    fixDefaultBuildType(property);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param property
     * @return true if the build type is OSGI
     */
    private boolean isOsgiBuildType(Property property) {
        return property != null && "OSGI".equals(property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE));
    }
    
    /**
     * Fix the default build type if it is a ESB job
     * @param property
     */
    @SuppressWarnings("unchecked")
    private void fixDefaultBuildType(Property property) {
        if(property != null && property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE) == null) {
            property.getAdditionalProperties().put(TalendProcessArgumentConstant.ARG_BUILD_TYPE, OSGI);
        }
    }

    @Override
    protected ERepositoryObjectType getObjectType() {
        return ERepositoryObjectType.PROCESS;
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
        Object assemblyFile = parameters.get(FILE_ASSEMBLY);

        CreateMavenStandardJobOSGiPom osgiPomCreator = new CreateMavenStandardJobOSGiPom((IProcessor) processor, (IFile) pomFile);

        osgiPomCreator.setArgumentsMap((Map<String, Object>) argumentsMap);
        osgiPomCreator.setOverwrite(Boolean.parseBoolean(overwrite.toString()));
        osgiPomCreator.setAssemblyFile((IFile) assemblyFile);

        final Property itemProperty = ((Item) item).getProperty();
        IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(itemProperty);
        IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(itemProperty);
        if (itemLocationPath != null && objectTypeFolder != null) {
            IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
            osgiPomCreator.setObjectTypeFolder(objectTypeFolder);
            osgiPomCreator.setItemRelativePath(itemRelativePath);
        }

        return osgiPomCreator;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.runtime.repository.build.AbstractBuildProvider#getBuildType()
     */
    @Override
    public BuildType getBuildType() {
        return super.getBuildType();
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
        IBuildJobHandler buildHandler = new BuildOSGiBundleHandler((ProcessItem) item, version.toString(),
                contextGroup.toString(), (Map<ExportChoice, Object>) choiceOption);
        return buildHandler;
    }
}
