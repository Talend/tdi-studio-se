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
package org.talend.repository.generic.ui.context.handler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.GenericConnParamName;
import org.talend.repository.generic.handler.IContextHandler;
import org.talend.repository.generic.i18n.Messages;

/**
 * created by ycbai on 2015年11月20日 Detailled comment
 *
 */
public class GenericContextHandler implements IContextHandler {

    private List<IElementParameter> parameters;

    @Override
    public boolean exportContext(ConnectionItem connectionItem) {
        boolean isContextMode = connectionItem.getConnection().isContextMode();
        if (isContextMode) {
            ConnectionContextHelper.openInConetxtModeDialog();
        } else {
            String defaultContextName = ConnectionContextHelper.convertContextLabel(connectionItem.getProperty().getLabel());
            if (StringUtils.isEmpty(defaultContextName)) {
                MessageDialog.openError(DisplayUtils.getDefaultShell(), Messages.getString("GenericContextHandler.wizardTitle"), Messages.getString("GenericContextHandler.defineConnectionName")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            Set<IConnParamName> contextParams = getContextParams();
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> variableModels = ConnectionContextHelper.exportAsContext(
                    defaultContextName, connectionItem, contextParams);
            if (variableModels != null) {
                Iterator<ContextItem> contextItor = variableModels.keySet().iterator();
                while (contextItor.hasNext()) {
                    ContextItem contextItem = contextItor.next();
                    List<ConectionAdaptContextVariableModel> apaptModels = variableModels.get(contextItem);
                    if (contextItem != null && apaptModels.size() == 0) { // create
                        if (ConnectionContextHelper.contextManager instanceof JobContextManager) {
                            Map<String, String> map = ((JobContextManager) ConnectionContextHelper.contextManager).getNameMap();
                            // set properties for context mode
                            ConnectionContextHelper.setPropertiesForContextMode(defaultContextName, connectionItem, contextItem,
                                    contextParams, map);
                        }
                    } else {
                        // set properties for exist context
                        ConnectionContextHelper.setPropertiesForExistContextMode(connectionItem, contextParams, variableModels);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean revertContext(ConnectionItem connectionItem) {
        boolean isContextMode = connectionItem.getConnection().isContextMode();
        if (isContextMode) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(PlatformUI.getWorkbench().getDisplay()
                    .getActiveShell(), connectionItem.getConnection(), true);
            if (contextType != null) {
                ConnectionContextHelper.revertPropertiesForContextMode(connectionItem, contextType);
            }
            return true;
        } else {
            ConnectionContextHelper.openOutConetxtModeDialog();
        }
        return false;
    }

    @Override
    public Set<IConnParamName> getContextParams() {
        Set<IConnParamName> contextParams = new HashSet<>();
        if (parameters != null) {
            for (IElementParameter param : parameters) {
                if (param instanceof GenericElementParameter) {
                    GenericElementParameter genericElementParameter = (GenericElementParameter) param;
                    if (genericElementParameter.isSupportContext()) {
                        GenericConnParamName connParamName = new GenericConnParamName();
                        String paramName = genericElementParameter.getName();
                        connParamName.setName(paramName);
                        connParamName.setContextVar(getValidContextVarName(paramName));
                        contextParams.add(connParamName);
                    }
                }
            }
        }
        return contextParams;
    }

    private String getValidContextVarName(String paramName) {
        return paramName.replace(IGenericConstants.EXP_SEPARATOR, IGenericConstants.UNDERLINE_SEPARATOR);
    }

    @Override
    public void setParameters(List<IElementParameter> parameters) {
        this.parameters = parameters;
    }

}
