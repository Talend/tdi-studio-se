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
package org.talend.repository.model;

import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.utils.ResourceModelHelper;

/**
 * Provides utilities methods relative to model on IResource. <br/>
 *
 * $Id: ResourceModelUtils.java 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ResourceModelUtils {

    /**
     * Load a project (IResource speaking) in the current workspace from a project (model speaking).
     *
     * @param project - the project to retrieve
     * @return the IProject matching the project
     * @throws PersistenceException if the IProject cannot be retrieve
     */
    public static IProject getProject(Project project) throws PersistenceException {
        return ResourceModelHelper.getProject(project);
    }
}
