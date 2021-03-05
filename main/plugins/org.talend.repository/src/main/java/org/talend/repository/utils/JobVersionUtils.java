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
package org.talend.repository.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;

/**
 * ftang class global comment. Detailled comment <br/>
 *
 */
public class JobVersionUtils {

    /**
     * ftang Comment method "getCurrentJobVersion".
     *
     * @return
     */
    public static String getCurrentVersion(IRepositoryNode repositoryNode) {
        try {
            // alert for bug TDI-20132
            if (repositoryNode.getObject() != null
                    && !repositoryNode.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.FOLDER)) {
                return ProxyRepositoryFactory.getInstance().getLastVersion(repositoryNode.getId()).getVersion();
            }
            List<IRepositoryNode> nodeChildren = repositoryNode.getChildren();
            if (nodeChildren != null) {
                for (IRepositoryNode node : nodeChildren) {
                    if (node.getObject() != null
                            && node.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.FOLDER)) {
                        String currentVersion = getCurrentVersion(node);
                        if (currentVersion != null && !"".equals(currentVersion)) {
                            return currentVersion;
                        }
                    } else {
                        if (node.getObject() != null) {
                            return ProxyRepositoryFactory.getInstance().getLastVersion(node.getId()).getVersion();
                        }
                    }
                }
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return "";
    }

    /**
     * wfy Comment method "getCurrentID",fixed bug TDI-20132.
     *
     * @return
     */
    public static String getCurrentID(IRepositoryNode repositoryNode) {
        List<IRepositoryNode> nodeChildren = repositoryNode.getChildren();
        if (nodeChildren != null) {
            for (IRepositoryNode node : nodeChildren) {
                if (node.getObject() != null && node.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.FOLDER)) {
                    String currentID = getCurrentID(node);
                    if (currentID != null && !"".equals(currentID)) {
                        return currentID;
                    }
                } else {
                    if (node.getObject() != null) {
                        return node.getId();
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$

    }

    /**
     * ftang Comment method "getAllVersions".
     *
     * @return
     */
    public static String[] getAllVersions(IRepositoryNode repositoryNode) {
        List<String> versionList = new ArrayList<String>();
        String nodeID = "";
        if (repositoryNode.getObject() != null
                && !repositoryNode.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.FOLDER)) {
            nodeID = repositoryNode.getId();
        } else {
            nodeID = getCurrentID(repositoryNode);
        }
        try {
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                    repositoryNode.getRoot().getProject(), nodeID, false);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                versionList.add(repositoryObject.getVersion());
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return versionList.toArray(new String[versionList.size()]);
    }

    /**
     * DOC xlwang Comment method "getAllVersions".
     * @return
     */
    public static IRepositoryViewObject getAllVersions(IRepositoryNode repositoryNode, String version) {
        String nodeID = "";
        IRepositoryViewObject object = null;
        if (repositoryNode.getObject() != null
                && !repositoryNode.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.FOLDER)) {
            nodeID = repositoryNode.getId();
        } else {
            nodeID = getCurrentID(repositoryNode);
        }
        try {
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance()
                    .getAllVersion(repositoryNode.getRoot().getProject(), nodeID, false);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                if (version.equals(repositoryObject.getVersion())) {
                    object = repositoryObject;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return object;
    }
}
