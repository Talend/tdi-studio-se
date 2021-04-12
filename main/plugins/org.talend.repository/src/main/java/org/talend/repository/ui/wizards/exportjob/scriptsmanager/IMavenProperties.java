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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import org.talend.commons.utils.generation.JavaUtils;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public interface IMavenProperties {

    static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    static final String LIBRARY_FOLDER_NAME = JavaUtils.JAVA_LIB_DIRECTORY;

    static final String PROVIDED_LIB_FOLDER_NAME = "provided-lib"; //$NON-NLS-1$

    static final String LIB_PATH = "${lib.path}" + PATH_SEPARATOR; //$NON-NLS-1$

    static final String PROVIDED_LIB_PATH = "${provided.lib.path}" + PATH_SEPARATOR;//$NON-NLS-1$

    static final String SRC_PATH = "src" + PATH_SEPARATOR; //$NON-NLS-1$

    static final String MAIN_RESOURCES_PATH = SRC_PATH + "main/resources/"; //$NON-NLS-1$

    static final String MAIN_JAVA_PATH = SRC_PATH + "main/java/"; //$NON-NLS-1$

    static final String MAIN_RESOURCES_LIB_PATH = MAIN_RESOURCES_PATH + LIBRARY_FOLDER_NAME + PATH_SEPARATOR;

    static final String MAIN_RESOURCES_PROVIDED_LIB_PATH = MAIN_RESOURCES_PATH + PROVIDED_LIB_FOLDER_NAME + PATH_SEPARATOR;

    /*
     *
     */
    static final String ELE_GROUP_ID = "groupId"; //$NON-NLS-1$

    static final String ELE_ARTIFACT_ID = "artifactId"; //$NON-NLS-1$

    static final String ELE_PARENT = "parent"; //$NON-NLS-1$

    static final String ELE_PROPERTIES = "properties"; //$NON-NLS-1$

    static final String ELE_MODULES = "modules"; //$NON-NLS-1$

    static final String ELE_MODULE = "module"; //$NON-NLS-1$

    static final String ELE_DEPENDENCIES = "dependencies"; //$NON-NLS-1$

    static final String ELE_DEPENDENCY = "dependency"; //$NON-NLS-1$

    static final String ELE_VERSION = "version"; //$NON-NLS-1$

    static final String ELE_SCOPE = "scope"; //$NON-NLS-1$

    static final String ELE_SYSTEMPATH = "systemPath"; //$NON-NLS-1$

}
