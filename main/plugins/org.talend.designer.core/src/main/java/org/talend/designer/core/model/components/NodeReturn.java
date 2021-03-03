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

import java.util.List;

import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeReturn;
import org.talend.designer.core.i18n.Messages;

/**
 * Return parameters for each component. These returns are described in the xml file of the component. <br/>
 *
 * $Id$
 *
 */
public class NodeReturn implements INodeReturn {

    private String name;

    private String displayName;

    private String type = "String"; //$NON-NLS-1$

    private String availability;

    private String varName;

    private String showIf;

    protected final static String UNIQUE_NAME = "__UNIQUE_NAME__"; //$NON-NLS-1$

    /*
     *
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#getAvailability()
     */
    public String getAvailability() {
        return this.availability;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#setAvailability(java.lang.String)
     */
    public void setAvailability(final String availability) {
        this.availability = Messages.getString("NodeReturn.Availability." + availability); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#getCode()
     */
    public String getVarName() {
        switch (LanguageManager.getCurrentLanguage()) {
        case PERL:
            return "$_globals{" + UNIQUE_NAME + "}{" + varName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        case JAVA:
            return "((" + getDisplayType() + ")globalMap.get(\"" + UNIQUE_NAME + "_" + name + "\"))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        default:
            return "$_globals{" + UNIQUE_NAME + "}{" + varName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#setCode(java.lang.String)
     */
    public void setVarName(final String variableName) {
        varName = variableName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#getDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#setDisplayName(java.lang.String)
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#setName(java.lang.String)
     */
    public void setName(final String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#getType()
     */
    public String getType() {
        return this.type;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.INodeReturn#setType(org.talend.core.model.metadata.EMetadataType)
     */
    public void setType(final String type) {
        this.type = type;
    }

    public String getDisplayType() {
        switch (LanguageManager.getCurrentLanguage()) {
        case PERL:
            return type;
        case JAVA:
            String displayType;
            if ((type.compareTo("String") == 0) || (type.compareTo("Integer") == 0)) { //$NON-NLS-1$ //$NON-NLS-2$
                type = "id_" + type; //$NON-NLS-1$
            }
            try {
                displayType = JavaTypesManager.getTypeToGenerate(type, true);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    displayType = type;//$NON-NLS-1$
                } else {
                    displayType = "String"; //$NON-NLS-1$
                }
            }
            return displayType;
        default:
            return type;
        }
    }

    public void setShowIf(String showIf) {
        this.showIf = showIf;
    }

    public boolean isShow(List<? extends IElementParameter> listParam) {
        if (showIf == null || "".equals(showIf)) {
            return true;
        } else {
            return Expression.evaluate(showIf, listParam);
        }
    }
}
