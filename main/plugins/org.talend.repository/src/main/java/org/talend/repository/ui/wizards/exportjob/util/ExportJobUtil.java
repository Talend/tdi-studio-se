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
package org.talend.repository.ui.wizards.exportjob.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.utils.ZipToFile;

/**
 * created by ycbai on 2015年5月20日 Detailled comment
 *
 */
public class ExportJobUtil {

    /**
     *
     * Gets the set of current job's context.
     *
     * @return a List of context names.
     *
     */
    public static List<String> getJobContexts(ProcessItem processItem) {
        List<String> contextNameList = new ArrayList<String>();
        for (Object o : ((ProcessTypeImpl) processItem.getProcess()).getContext()) {
            if (o instanceof ContextType) {
                ContextType context = (ContextType) o;
                if (contextNameList.contains(context.getName())) {
                    continue;
                }
                contextNameList.add(context.getName());
            }
        }
        return contextNameList;
    }

    /**
     * DOC zli Comment method "getJobContextValues".
     *
     * @param processItem
     * @param contextName
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<ContextParameterType> getJobContextValues(ProcessItem processItem, String contextName) {
        if (contextName == null) {
            return null;
        }// else do next line
        List<ContextParameterType> list = new ArrayList<ContextParameterType>();
        EList contexts = ((ProcessTypeImpl) processItem.getProcess()).getContext();
        for (int i = 0; i < contexts.size(); i++) {
            Object object = contexts.get(i);
            if (object instanceof ContextType) {
                ContextType contextType = (ContextType) object;
                if (contextName.equals(contextType.getName())) {
                    EList contextParameter = contextType.getContextParameter();
                    for (int j = 0; j < contextParameter.size(); j++) {
                        Object object2 = contextParameter.get(j);
                        if (object2 instanceof ContextParameterType) {
                            ContextParameterType contextParameterType = (ContextParameterType) object2;
                            list.add(contextParameterType);
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    public static String getTmpFolder() {
        String tmp = getTmpFolderPath() + "/newjarFolder"; //$NON-NLS-1$
        tmp = tmp.replace('\\', '/');
        File f = new File(tmp);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmp;
    }

    public static String getTmpFolderPath() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        String tmpFolder;
        try {
            IProject physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder("temp").getLocation().toPortableString(); //$NON-NLS-1$
        } catch (Exception e) {
            tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        }
        return tmpFolder;
    }

    public static void deleteTempFiles() {
        String tmpFold = getTmpFolderPath() + "/newjarFolder"; //$NON-NLS-1$
        File file = new File(tmpFold);
        if (!file.exists() && !file.isDirectory()) {
            return;
        }
        ZipToFile.deleteDirectory(file);
    }

    public static ProcessItem getProcessItem(List<? extends IRepositoryNode> nodes) {
        ProcessItem processItem = null;
        for (IRepositoryNode node : nodes) {
            IRepositoryViewObject repositoryObject = node.getObject();
            if (repositoryObject != null) {
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                } else if (repositoryObject.getProperty().getItem() instanceof FolderItem) {
                    processItem = getProcessItemIfSelectFolder(repositoryObject);
                }
            } else {
                List<IRepositoryNode> nodesChildren = node.getChildren();
                processItem = getProcessItem(nodesChildren);
            }
            if (processItem != null) {
                return processItem;
            }
        }
        return null;
    }

    private static ProcessItem getProcessItemIfSelectFolder(IRepositoryViewObject repositoryObject) {
        ProcessItem processItem = null;
        List<IRepositoryNode> children = repositoryObject.getRepositoryNode().getChildren();
        for (IRepositoryNode object : children) {
            if (object.getObject().getProperty().getItem() instanceof FolderItem) {
                processItem = getProcessItemIfSelectFolder(object.getObject());
                if (processItem != null) {
                    return processItem;
                }
            } else if (object.getObject().getProperty().getItem() instanceof ProcessItem) {
                return (ProcessItem) object.getObject().getProperty().getItem();
            }
        }
        return processItem;
    }

}
