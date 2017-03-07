// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.maven.tools.creator.CreateMavenStandardJobOSGiPom;
import org.talend.designer.runprocess.IProcessor;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobOSGiBundleBuildProvider extends RepositoryObjectTypeBuildProvider {

    @Override
    public boolean valid(Map<String, Object> parameters) {
        // return super.valid(parameters);
        return false; // PTODO tmp disable OSGi, because it's not finished yet.
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

        CreateMavenStandardJobOSGiPom osgiPomCreator = new CreateMavenStandardJobOSGiPom((IProcessor) processor, (IFile) pomFile);

        osgiPomCreator.setArgumentsMap((Map<String, Object>) argumentsMap);
        osgiPomCreator.setOverwrite(Boolean.parseBoolean(overwrite.toString()));

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

    @Override
    public IBuildExportHandler createBuildExportHandler(Map<String, Object> parameters) {
        // PTODO not impl yet.
        return null;
    }

}
