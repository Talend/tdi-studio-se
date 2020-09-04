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
package org.talend.designer.unifiedcomponent.unifier.jdbc;

import org.talend.designer.unifiedcomponent.unifier.AbstractComponentsUnifier;

/**
 * created by wchen on Dec 1, 2017 Detailled comment
 *
 */
public class JDBCComponentsUnifier extends AbstractComponentsUnifier implements IDynamicJDBCUnifier {

    private String displayName = "JDBC";

    private String componentKey = "JDBC";
    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier#getDatabase()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * the fake component name like tJDBCInput
     */
    @Override
    public String getComponentName() {
        return super.getComponentName();

    }

    public String getComponentKey() {
        return componentKey;
    }

    public void setComponentKey(String componentKey) {
        this.componentKey = componentKey;
    }

    // the real component like tDeltaLakeInput
    public String getDispalyComponentName() {
        // tJDBCInput ==> tDeltaLakeInput
        String componentName = super.getComponentName().replaceFirst("JDBC", getComponentKey());
        return componentName;
    }

}
