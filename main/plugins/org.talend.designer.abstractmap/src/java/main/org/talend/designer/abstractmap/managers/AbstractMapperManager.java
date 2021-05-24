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
package org.talend.designer.abstractmap.managers;

import java.util.List;

import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.abstractmap.AbstractMapComponent;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public abstract class AbstractMapperManager {

    public static final String ERROR_REJECT = "ErrorReject";//$NON-NLS-1$

    public static final String ERROR_REJECT_MESSAGE = "errorMessage";

    public static final String ERROR_REJECT_STACK_TRACE = "errorStackTrace";

    private AbstractMapComponent mapperComponent;

    private boolean mapperChanged;

    private IExternalData data;

    /**
     * DOC amaumont AbstractMapperManager constructor comment.
     *
     * @param mapperComponent
     */
    public AbstractMapperManager(AbstractMapComponent mapperComponent) {
        super();
        this.mapperComponent = mapperComponent;
    }

    public Object getElementParameterValue(String parameterName) {
        List<? extends IElementParameter> elementParameters = mapperComponent.getElementParameters();
        for (IElementParameter parameter : elementParameters) {
            if (parameterName.equals(parameter.getName())) {
                return parameter.getValue();
            }
        }
        return null;
    }

    public AbstractMapComponent getAbstractMapComponent() {
        return this.mapperComponent;
    }

    public abstract ILinkManager getLinkManager();

    /**
     * Getter for mapperChanged.
     *
     * @return the mapperChanged
     */
    public abstract boolean isDataChanged();

    /**
     * DOC amaumont Comment method "setOriginalExternalData".
     *
     * @param data
     */
    public void setOriginalExternalData(IExternalData data) {
        this.data = data;
    }

    public IExternalData getOriginalExternalData() {
        return this.data;
    }

    public abstract AbstractUIManager getUiManager();
}
