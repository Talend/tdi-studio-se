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
package org.talend.designer.core.generic.utils;

import org.talend.core.model.components.filters.IComponentFactoryFilter;

/**
 * DOC talend  class global comment. Detailled comment
 */
public class TestComponentFactoryFilter implements IComponentFactoryFilter {

    private static String[] compList;


    public TestComponentFactoryFilter() {
    }

    @Override
    public boolean isAvailable(String componentName) {
       if(Boolean.getBoolean("talend.test.component.filter")){
           if(componentName.equals("tJIRAInput")){
               return false;
           }
       }
       return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.components.model.IComponentFactoryFilter#cleanCache()
     */
    @Override
    public void cleanCache() {
        compList = null;
    }

}
