// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ImportProjectBean {

    public final org.talend.core.model.general.Project newProject;

    public final org.talend.core.model.general.Project oldProject;

    public ImportProjectBean(org.talend.core.model.properties.Project newProject,
            org.talend.core.model.properties.Project oldProject) {
        this(new org.talend.core.model.general.Project(newProject), new org.talend.core.model.general.Project(oldProject));
    }

    public ImportProjectBean(org.talend.core.model.general.Project newProject, org.talend.core.model.general.Project oldProject) {
        this.newProject = newProject;
        this.oldProject = oldProject;
    }
}
