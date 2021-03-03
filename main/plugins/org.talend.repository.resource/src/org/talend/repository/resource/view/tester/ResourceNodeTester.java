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
package org.talend.repository.resource.view.tester;

import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.tester.AbstractNodeTypeTester;

/**
 *
 * DOC jding class global comment. Detailled comment
 */
public class ResourceNodeTester extends AbstractNodeTypeTester {

    @SuppressWarnings("serial")
    private static final Map<String, ERepositoryObjectType> PROPERTY_MAPPING = new HashMap<String, ERepositoryObjectType>() {
        {
            put("isRouteResourceNode", ERepositoryObjectType.RESOURCES); //$NON-NLS-1$
        }
    };

    @Override
    protected Map<String, ERepositoryObjectType> getPropertyMapping() {
        return PROPERTY_MAPPING;
    }

}
