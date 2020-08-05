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
package org.talend.repository.ui.actions.importproject;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.repository.IRepositoryViewObject;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ImportProjectBean {

    public final org.talend.core.model.general.Project newProject;

    public final org.talend.core.model.general.Project oldProject;

    private List<IRepositoryViewObject> invalidViewObjectList = new ArrayList<IRepositoryViewObject>();

    public ImportProjectBean(org.talend.core.model.properties.Project newProject,
            org.talend.core.model.properties.Project oldProject) {
        this(new org.talend.core.model.general.Project(newProject), new org.talend.core.model.general.Project(oldProject));
    }

    public ImportProjectBean(org.talend.core.model.general.Project newProject, org.talend.core.model.general.Project oldProject) {
        this.newProject = newProject;
        this.oldProject = oldProject;
    }

    public List<IRepositoryViewObject> getInvalidViewObjectList() {
        return invalidViewObjectList;
    }

    public void setInvalidViewObjectList(List<IRepositoryViewObject> invalidViewObjectList) {
        this.invalidViewObjectList = invalidViewObjectList;
    }

}
