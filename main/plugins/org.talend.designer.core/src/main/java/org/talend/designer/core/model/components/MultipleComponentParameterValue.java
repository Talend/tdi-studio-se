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
package org.talend.designer.core.model.components;

import java.util.StringTokenizer;

import org.talend.core.model.components.IMultipleComponentParameter;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class MultipleComponentParameterValue implements IMultipleComponentParameter {

    String sourceComponent;

    String targetComponent;

    String sourceValue;

    String targetValue;

    public MultipleComponentParameterValue(String target, String value) {
        this(target, value, ".");
    }

    public MultipleComponentParameterValue(String target, String value, String seperator) {
        StringTokenizer token = new StringTokenizer(target, seperator);
        targetComponent = token.nextToken();
        targetValue = token.nextToken();

        sourceComponent = null;
        sourceValue = value;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getSourceComponent()
     */
    public String getSourceComponent() {
        return this.sourceComponent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getSourceValue()
     */
    public String getSourceValue() {
        return this.sourceValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getTargetComponent()
     */
    public String getTargetComponent() {
        return this.targetComponent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getTargetValue()
     */
    public String getTargetValue() {
        return this.targetValue;
    }
}
