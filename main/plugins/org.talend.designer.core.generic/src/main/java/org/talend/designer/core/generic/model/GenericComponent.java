// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.model;

import org.talend.commons.exception.BusinessException;
import org.talend.components.api.component.ComponentDefinition;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class GenericComponent extends Component {

    private String name;

    public GenericComponent(ComponentDefinition componentDefinition, String paletteType, String name) throws BusinessException {
        super(componentDefinition, paletteType);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
