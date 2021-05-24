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
package org.talend.designer.documentation.utils;

import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public final class DocumentationUtil {

    public static void setPartItemId(WebBrowserEditor editor, String objId, ERepositoryObjectType objType) {
        if (objId != null && editor != null) {
            String id = objId;
            if (objType != null
                    && (objType.equals(ERepositoryObjectType.PROCESS) || objType.equals(ERepositoryObjectType.JOBLET))) {
                id += DocumentationConstants.ID_SUFFIX; // get the doc id
            }
            editor.setPartProperty(DocumentationConstants.KEY_ITEM_ID, id);
        }
    }
}
