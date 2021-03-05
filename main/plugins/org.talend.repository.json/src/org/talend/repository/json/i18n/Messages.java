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
package org.talend.repository.json.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.talend.repository.json.messages"; //$NON-NLS-1$

    public static String JSONLimitToolTip;

    public static String CreateJSONAction_CREATE_JSON;

    public static String CreateJSONAction_EDIT_JSON;

    public static String CreateJSONAction_OPEN_JSON;

    public static String CreateJSONSchemaAction_EDIT_SCHEMA;

    public static String CreateJSONSchemaAction_RETRIEVE_SCHEMA;

    public static String EditJSONPropertiesAction_EDIT_PROPERTIES;

    public static String JSONDragAndDropHandler_dataViewer_warning_title;

    public static String JSONDragAndDropHandler_dataViewer_warning_message;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
