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
package org.talend.repository.ui;

import org.talend.commons.ui.runtime.image.IImage;
import org.talend.repository.RepositoryPlugin;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public enum ERepositoryImages implements IImage {
    IMPORT_PROJECTS_ACTION("/icons/import_projects_action.gif"), //$NON-NLS-1$
    NEW_PROJECT_ACTION("/icons/newProject.png"), //$NON-NLS-1$
    LICENSE_WIZ("/icons/license_wiz.png"), //$NON-NLS-1$
    REGISTER_WIZ("/icons/register_wiz.png"), //$NON-NLS-1$
    DELETE_PROJECT_ACTION("/icons/delete.gif"), //$NON-NLS-1$
    PROJECT_ICON("/icons/project.gif"), //$NON-NLS-1$
    FOLDER_ICON("/icons/folder.gif"), //$NON-NLS-1$
    OPEN_PROJECT_ICON("/icons/bullet_go.png"), //$NON-NLS-1$
    LOGIN_CRITICAL_ICON("/icons/messagebox_critical.png"), //$NON-NLS-1$
    LOGIN_WARNING_ICON("/icons/messagebox_warning.png"), //$NON-NLS-1$
    LOGIN_CORRECT_ICON("/icons/messagebox_greenCoche.png"), //
    ADD_ICON("/icons/add.gif"), //$NON-NLS-N$
    DEFAULT_COMPONENT_ICON("/icons/default-32.png"), //$NON-NLS-N$
    DEFAULT_PROCESS_ICON("/icons/process_icon.png"), //$NON-NLS-N$
    MODIFIED_ICON("/icons/refresh.gif"), //$NON-NLS-N$
    SCHEMA_ICON("/icons/table.gif"), //$NON-NLS-N$
    CONNECTOR_ICON("/icons/connection.gif"), //$NON-NLS-N$
    TALENDFORGE_ICON("/icons/talendforge.jpg"), //$NON-NLS-N$
    OPEN_ICON("/icons/open.gif"), //$NON-NLS-N$
    BACKGROUND_TASK_ICON("/icons/backgroundTasks.png"), // $NON-NLS-N$
    DOT_ICON("/icons/dot.jpg");//$NON-NLS-N$

    private String path;

    ERepositoryImages(String path) {
        this.path = path;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for clazz.
     *
     * @return the clazz
     */
    public Class getLocation() {
        return RepositoryPlugin.class;
    }

}
