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
package org.talend.designer.core.model.components;

import org.talend.core.model.process.IElementParameterDefaultValue;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ElementParameterDefaultValue implements IElementParameterDefaultValue {

    Object defaultValue;

    String ifCondition;

    String notIfCondition;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#getDefaultValue()
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#getIfCondition()
     */
    public String getIfCondition() {
        return this.ifCondition;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#setIfCondition(java.lang.String)
     */
    public void setIfCondition(String ifCondition) {
        this.ifCondition = ifCondition;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#getNotIfCondition()
     */
    public String getNotIfCondition() {
        return this.notIfCondition;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IElementParameterDefaultValue#setNotIfCondition(java.lang.String)
     */
    public void setNotIfCondition(String notIfCondition) {
        this.notIfCondition = notIfCondition;
    }
}
