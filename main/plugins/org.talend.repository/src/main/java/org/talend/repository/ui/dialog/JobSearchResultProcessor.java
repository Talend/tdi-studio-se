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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.processor.JobTypeProcessor;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class JobSearchResultProcessor extends JobTypeProcessor {

    private Set<String> jobIds;

    private Set<String> folders;

    public JobSearchResultProcessor(List<IRepositoryViewObject> jobs) {
        super(null);
        jobIds = new HashSet<String>();
        folders = new HashSet<String>();
        folders.contains(new Path("").toString()); //$NON-NLS-1$
        for (IRepositoryViewObject obj : jobs) {
            String path = obj.getProperty().getItem().getState().getPath();
            if (path != null && path.length() > 0) {
                addFolder(new Path(path));
            }
            jobIds.add(obj.getId());
        }
    }

    /**
     * DOC chuang Comment method "addFolder".
     *
     * @param path
     */
    private void addFolder(IPath path) {
        String lastSegment = path.lastSegment();
        if (lastSegment != null) {
            folders.add(path.toString());
            addFolder(path.removeLastSegments(1));
        }
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            try {
                if (node.getContentType().equals(ERepositoryObjectType.PROCESS)
                        || node.getContentType().equals(ERepositoryObjectType.PROCESS_MR)) {
                    return true;
                }
            } catch (Exception e) { // ignore
            }
            return false;
        } else if (node.getType() == ENodeType.SIMPLE_FOLDER) {
            try {
                String path = RepositoryNodeUtilities.getPath(node).toString();
                return folders.contains(path);
            } catch (Exception e) {
                return false;
            }
        } else if (node.getContentType().equals(ERepositoryObjectType.REFERENCED_PROJECTS)
                || node.getContentType().equals(ERepositoryObjectType.SVN_ROOT)) {
            return true;
        } else if (node.getObject() != null) {
            String id = node.getObject().getId();
            return jobIds.contains(id);
        }
        return false;
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        ERepositoryObjectType type = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (getTypes().contains(type)) {
            return true;
        }

        return false;
    }

    @Override
    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>(50);
        list.add(ERepositoryObjectType.PROCESS);
        list.add(ERepositoryObjectType.PROCESS_MR);
        return list;
    }
}
