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
package org.talend.repository.json.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.json.node.JSONRepositoryNodeType;
import org.talend.repository.view.di.metadata.tester.CoMetadataNodeTester;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONMetadataNodeTester extends CoMetadataNodeTester {

    private static final String IS_JSON_CONNECTION = "isJSONConnection"; //$NON-NLS-1$

    @Override
    protected ERepositoryObjectType findType(String property) {
        if (property != null) {
            if (IS_JSON_CONNECTION.equals(property)) {
                return JSONRepositoryNodeType.JSON;
            }
        }
        return null;
    }

}
