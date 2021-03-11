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
package org.talend.designer.core.generic.constants;

/**
 * created by ycbai on 2015年9月16日 Detailled comment
 *
 */
public interface IGenericConstants {

    public static final String PLUGIN_ID = "org.talend.designer.core.generic"; //$NON-NLS-1$

    public static final String REPOSITORY_PLUGIN_ID = "org.talend.repository.generic"; //$NON-NLS-1$

    public static final String REPOSITORY_LOCATION_SEPARATOR = "#"; //$NON-NLS-1$

    public static final String NAME_PROPERTY = "name"; //$NON-NLS-1$

    public static final String UNDERLINE_SEPARATOR = "_";//$NON-NLS-1$

    public static final String EXP_SEPARATOR = ".";//$NON-NLS-1$

    public static final String DIR_SEPARATOR = "/";//$NON-NLS-1$

    public static final String MAIN_CONNECTOR_NAME = "MAIN";//$NON-NLS-1$

    public static final String REJECT_CONNECTOR_NAME = "REJECT";//$NON-NLS-1$

    public static final String CONTEXT = "FLOW";//$NON-NLS-1$

    /**
     * Tag key of property's repository value.
     */
    public static final String REPOSITORY_VALUE = "REPOSITORY_VALUE";//$NON-NLS-1$

    /**
     * Tag key of property's availability value.
     */
    public static final String AVAILABILITY = "AVAILABILITY";//$NON-NLS-1$

    /**
     * Tag key which estimates whether need to add quotes for property value.
     */
    public static final String ADD_QUOTES = "ADD_QUOTES"; //$NON-NLS-1$

    /**
     * Tag key which records the context mode status of one property.
     */
    public static final String IS_CONTEXT_MODE = "IS_CONTEXT_MODE"; //$NON-NLS-1$

    public static final String IS_DYNAMIC = "IS_DYNAMIC"; //$NON-NLS-1$

    /**
     * Tag key estimates what would line separator in property would be replaced with.
     * The property is which value can include line separator
     */
    public static final String LINE_SEPARATOR_REPLACED_TO = "LINE_SEPARATOR_REPLACED_TO";//$NON-NLS-1$

    public static final String IS_PROPERTY_SHOW = "IS_PROPERTY_SHOW";//$NON-NLS-1$

    public static final String DYNAMIC_PROPERTY_VALUE = "DYNAMIC_PROPERTY_VALUE"; //$NON-NLS-1$
}
