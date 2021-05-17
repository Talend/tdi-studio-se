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
package org.talend.repository.json.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.metadata.managment.ui.i18n.Messages;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.DBConnectionContextUtils.EDBParamName;
import org.talend.metadata.managment.ui.utils.FileConnectionContextUtils.EFileParamName;
import org.talend.metadata.managment.ui.utils.OtherConnectionContextUtils.EParamName;
import org.talend.metadata.managment.ui.wizard.context.ContextModeWizard;
import org.talend.metadata.managment.ui.wizard.metadata.ContextSetsSelectionDialog;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;

/**
 * ggu class global comment. Detailled comment
 */
public final class JSONConnectionContextHelper {

    private static final ECodeLanguage LANGUAGE = LanguageManager.getCurrentLanguage();

    public static final String LINE = "_"; //$NON-NLS-1$

    public static final String EMPTY = ""; //$NON-NLS-1$

    public static IContextManager contextManager;

    /**
     *
     * ggu Comment method "exportAsContext".
     *
     */
    public static Map<ContextItem, List<ConectionAdaptContextVariableModel>> exportAsContext(ConnectionItem connItem,
            Set<IConnParamName> paramSet) {

        if (connItem == null) {
            return null;
        }
        List<IContextParameter> varList = createContextParameters(connItem, paramSet);
        if (varList == null || varList.isEmpty()) {
            return null;
        }

        String contextName = convertContextLabel(connItem.getProperty().getLabel());

        ISelection selection = getRepositoryContext(contextName, false);

        if (selection == null) {
            return null;
        }
        Map<ContextItem, List<ConectionAdaptContextVariableModel>> variableContextMap = new HashMap();
        List<ConectionAdaptContextVariableModel> models = new ArrayList<ConectionAdaptContextVariableModel>();

        Set<String> connectionVaribles = getConnVariables(connItem, paramSet);
        ContextModeWizard contextWizard = new ContextModeWizard(contextName, selection.isEmpty(), selection, varList,
                connectionVaribles);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), contextWizard);
        if (dlg.open() == Window.OK) {
            ContextItem contextItem = contextWizard.getContextItem();
            models = contextWizard.getAdaptModels();
            if (contextItem != null) {
                variableContextMap.put(contextItem, models);
            }
            contextManager = contextWizard.getContextManager();
            if (contextItem != null) {
                contextItem.getProperty().setLabel(contextName);
            }
            return variableContextMap;
        }
        return null;
    }

    public static void openInConetxtModeDialog() {
        MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                Messages.getString("ConnectionContextHelper.ContextTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionContextHelper.InConextMessages")); //$NON-NLS-1$
    }

    public static void openOutConetxtModeDialog() {
        MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                Messages.getString("ConnectionContextHelper.ContextTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionContextHelper.OutConextMessages")); //$NON-NLS-1$
    }

    /**
     *
     * change the mode of lebel to Context mode
     *
     * @param label which you want to convert(it is the name of connection normal)
     * @return Context mode label
     */
    public static String convertContextLabel(String label) {
        if (label != null) {
            String newLabel = label.replaceAll("[\\.\\-\\ \\(\\)\\[\\]=]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            Pattern pattern = Pattern.compile("^[0-9]+.*$"); //$NON-NLS-1$
            Matcher m = pattern.matcher(newLabel);
            if (m.matches()) {
                newLabel = "_" + newLabel; //$NON-NLS-1$
            }
            //
            try {
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                List<IRepositoryViewObject> contextObjectList = factory.getAll(ERepositoryObjectType.CONTEXT, true);

                int i = 1;
                String tmpLabel = newLabel;
                while (!isValidContextName(contextObjectList, tmpLabel)) {
                    tmpLabel = newLabel + i;
                    i++;
                }
                return tmpLabel;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
            return newLabel;
        }
        return null;
    }

    private static boolean isValidContextName(List<IRepositoryViewObject> contextObjectList, String name) {
        if (contextObjectList != null) {
            for (IRepositoryViewObject object : contextObjectList) {
                Item item = object.getProperty().getItem();
                if (item.getProperty().getLabel().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<IContextParameter> createContextParameters(ConnectionItem connectionItem, Set<IConnParamName> paramSet) {
        if (connectionItem == null) {
            return null;
        }
        final String label = convertContextLabel(connectionItem.getProperty().getLabel());
        Connection conn = connectionItem.getConnection();

        List<IContextParameter> varList = null;
        if (conn instanceof JSONFileConnection) {
            varList = getJSONFileVariables(label, (JSONFileConnection) conn);
        }

        return varList;
    }

    private static Set<String> getConnVariables(ConnectionItem connectionItem, Set<IConnParamName> paramSet) {
        if (connectionItem == null) {
            return null;
        }

        Set<String> varList = new HashSet<String>();

        Iterator<IConnParamName> paramIt = paramSet.iterator();
        while (paramIt.hasNext()) {
            Object param = paramIt.next();
            if (param instanceof EDBParamName) {
                varList.add(((EDBParamName) param).name());
            }
            if (param instanceof EFileParamName) {
                varList.add(((EFileParamName) param).name());
            }
            if (param instanceof EParamName) {
                varList.add(((EParamName) param).name());
            }
        }
        return varList;
    }

    public static void setPropertiesForContextMode(ConnectionItem connectionItem, ContextItem contextItem,
            Set<IConnParamName> paramSet, Map<String, String> map) {
        if (connectionItem == null || contextItem == null) {
            return;
        }

        final String label = contextItem.getProperty().getLabel();
        Connection conn = connectionItem.getConnection();

        if (conn instanceof JSONFileConnection) {
            setJSONFilePropertiesForContextMode(label, (JSONFileConnection) conn);
        }
        // set connection for context mode
        connectionItem.getConnection().setContextMode(true);
        connectionItem.getConnection().setContextId(contextItem.getProperty().getId());
        connectionItem.getConnection().setContextName(contextItem.getDefaultContext());

    }

    public static void setPropertiesForExistContextMode(ConnectionItem connectionItem, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> map) {
        if (connectionItem == null) {
            return;
        }
        ContextItem selItem = null;
        if (map.keySet().size() == 1) {
            selItem = map.keySet().iterator().next();
        }
        Connection conn = connectionItem.getConnection();

        if (conn instanceof JSONFileConnection) {
            setJSONFilePropertiesForExistContextMode((JSONFileConnection) conn, paramSet, map);
        }
        // set connection for context mode
        connectionItem.getConnection().setContextMode(true);
        connectionItem.getConnection().setContextId(selItem.getProperty().getId());
        connectionItem.getConnection().setContextName(selItem.getDefaultContext());

    }

    static void createParameters(List<IContextParameter> varList, String paramName, String value) {
        createParameters(varList, paramName, value, null);
    }

    static void createParameters(List<IContextParameter> varList, String paramName, String value, JavaType type) {
        if (varList == null || paramName == null) {
            return;
        }

        if (value == null) {
            value = EMPTY;
        }

        JobContextParameter contextParam = new JobContextParameter();
        contextParam.setName(paramName);

        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            if (type == null) {
                contextParam.setType(MetadataTalendType.getDefaultTalendType());
            } else {
                contextParam.setType(type.getId());
            }
            break;
        case PERL:
        default:
            if (type != null) {
                if (type == JavaTypesManager.DIRECTORY) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_DIRECTORY);
                    break;
                } else if (type == JavaTypesManager.FILE) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_FILE);
                    break;
                } else if (type == JavaTypesManager.INTEGER) {
                    contextParam.setType(MetadataTalendType.getPerlTypes()[3]); // int
                    break;
                } else if (type == JavaTypesManager.PASSWORD) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_PASSWORD);
                    break;
                }
            }
            contextParam.setType(MetadataTalendType.getPerlTypes()[5]); // string
            break;
        }

        contextParam.setPrompt(paramName + "?"); //$NON-NLS-1$
        if (value != null) {
            contextParam.setValue(value);
        }
        contextParam.setComment(EMPTY);
        varList.add(contextParam);
    }

    private static ISelection getRepositoryContext(final String contextNameOrId, boolean isId) {
        if (contextNameOrId == null || "".equals(contextNameOrId.trim())) { //$NON-NLS-1$
            return null;
        }
        if (isId && RepositoryNode.NO_ID.equals(contextNameOrId.trim())) {
            return null;
        }
        IRepositoryViewObject contextObject = null;
        try {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            List<IRepositoryViewObject> contextObjectList = factory.getAll(ERepositoryObjectType.CONTEXT, true);
            if (contextObjectList != null) {
                for (IRepositoryViewObject object : contextObjectList) {
                    Item item = object.getProperty().getItem();
                    if (item != null && item instanceof ContextItem) {
                        String itemNameOrId = null;
                        if (isId) {
                            itemNameOrId = item.getProperty().getId();
                        } else {
                            itemNameOrId = item.getProperty().getLabel();
                        }
                        if (contextNameOrId.equals(itemNameOrId)) {
                            contextObject = object;
                            break;
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            //
        }

        StructuredSelection selection = new StructuredSelection();
        if (contextObject != null) {
            RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(contextObject);
            if (repositoryNode != null) {
                selection = new StructuredSelection(repositoryNode);
            }
        }
        return selection;
    }

    public static void checkAndAddContextVariables(ContextItem item, IContextManager contextManager, Set<String> addedVars,
            Set<String> contextGoupNameSet) {
        EList context = item.getContext();
        Map<String, List<ContextParameterTypeImpl>> map = new HashMap<String, List<ContextParameterTypeImpl>>();
        Iterator iterator = context.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof ContextTypeImpl) {
                ContextTypeImpl contextTypeImpl = (ContextTypeImpl) obj;
                String name = contextTypeImpl.getName();
                EList contextParameters = contextTypeImpl.getContextParameter();
                Iterator contextParas = contextParameters.iterator();
                List<ContextParameterTypeImpl> list = new ArrayList<ContextParameterTypeImpl>();
                while (contextParas.hasNext()) {
                    ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                    list.add(contextParameterType);
                }
                map.put(name, list);
            }
        }
        if (map.isEmpty()) {
            return;
        }
        String defaultContextName = item.getDefaultContext();
        Set<String> existGroupNameSet = new HashSet<String>();
        for (IContext con : contextManager.getListContext()) {
            existGroupNameSet.add(con.getName());
        }
        if (contextGoupNameSet.isEmpty()) {
            contextGoupNameSet.add(defaultContextName);
        }
        Set<String> alreadyUpdateNameSet = new HashSet<String>();
        for (String key : map.keySet()) {
            for (String groupName : contextGoupNameSet) {
                boolean isExtraGroup = false;
                for (String existGroup : existGroupNameSet) {
                    if (key.equals(existGroup)) {
                        isExtraGroup = true;
                        alreadyUpdateNameSet.add(existGroup);
                        break;
                    }
                }
                if (key.equals(groupName) || isExtraGroup) {
                    List<ContextParameterTypeImpl> list = map.get(key);
                    JobContext jobContext = new JobContext(key);
                    boolean isExistContext = false;
                    if (isExtraGroup) {
                        for (IContext con : contextManager.getListContext()) {
                            if (key.equals(con.getName())) {
                                if (con instanceof JobContext) {
                                    jobContext = (JobContext) con;
                                    isExistContext = true;
                                    break;
                                }
                            }
                        }
                    }
                    setContextParameter(item, addedVars, list, jobContext);
                    if (!isExistContext) {
                        contextManager.getListContext().add(jobContext);
                    }
                    break;
                }
            }
        }
        // if job context group is not in current add's context,then update context group value to default group value
        existGroupNameSet.removeAll(alreadyUpdateNameSet);
        List<ContextParameterTypeImpl> list = map.get(defaultContextName);
        if (list == null) {
            return;
        }
    }

    public static void setContextParameter(ContextItem item, Set<String> addedVars, List<ContextParameterTypeImpl> list,
            JobContext jobContext) {
        for (ContextParameterTypeImpl contextImpl : list) {
            for (String var : addedVars) {
                if (var.equals(contextImpl.getName())) {
                    JobContextParameter contextParam = new JobContextParameter();
                    ContextUtils.updateParameter(contextImpl, contextParam);
                    contextParam.setSource(item.getProperty().getId());
                    contextParam.setContext(jobContext);
                    jobContext.getContextParameterList().add(contextParam);
                }
            }
        }
    }

    /**
     *
     * wzhang Comment method "checkAndAddContextsVarDND".
     *
     * @param item
     * @param contextManager
     */
    public static void checkAndAddContextsVarDND(ContextItem item, IContextManager contextManager) {
        EList context = item.getContext();
        Map<String, List<ContextParameterTypeImpl>> map = new HashMap<String, List<ContextParameterTypeImpl>>();
        Iterator iterator = context.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof ContextTypeImpl) {
                ContextTypeImpl contextTypeImpl = (ContextTypeImpl) obj;
                String name = contextTypeImpl.getName();
                EList contextParameters = contextTypeImpl.getContextParameter();
                Iterator contextParas = contextParameters.iterator();
                List<ContextParameterTypeImpl> list = new ArrayList<ContextParameterTypeImpl>();
                while (contextParas.hasNext()) {
                    ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                    list.add(contextParameterType);
                }
                map.put(name, list);
            }
        }
        if (map.isEmpty()) {
            return;
        }
        contextManager.getListContext().clear();
        String defaultContextName = item.getDefaultContext();

        for (String key : map.keySet()) {
            List<ContextParameterTypeImpl> list = map.get(key);
            JobContext jobContext = new JobContext(key);
            for (ContextParameterTypeImpl contextImpl : list) {
                JobContextParameter contextParam = new JobContextParameter();
                ContextUtils.updateParameter(contextImpl, contextParam);
                contextParam.setSource(item.getProperty().getId());
                contextParam.setContext(jobContext);
                jobContext.getContextParameterList().add(contextParam);
            }
            contextManager.getListContext().add(jobContext);
            if (key.equals(defaultContextName)) {
                contextManager.setDefaultContext(jobContext);
            }
        }

    }

    public static ContextType getContextTypeForContextMode(Shell shell, Connection connection, boolean canCancel) {
        return getContextTypeForContextMode(shell, connection, null, false, canCancel);
    }

    /**
     *
     * ggu Comment method "getContextTypeForContextMode".
     *
     * if connection is in context mode,choose the context. if return null, the connection is not in context mode.
     *
     * if canCancel is true, the selecting cotnext sets dialog will can be cancel.
     */
    private static ContextType getContextTypeForContextMode(Shell shell, Connection connection, String selectedContext,
            boolean defaultContext, boolean canCancel) {
        if (connection == null) {
            return null;
        }
        ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
        if (contextItem != null && connection.isContextMode()) {
            if (defaultContext) {
                selectedContext = contextItem.getDefaultContext();
            } else if (selectedContext == null) {
                if (contextItem.getContext().size() > 1) {
                    final ContextSetsSelectionDialog setsDialog = new ContextSetsSelectionDialog(shell, contextItem, canCancel);
                    Display.getDefault().syncExec(new Runnable() {// launch the dialog box in the UI thread beacause the

                                // method may be called from other threads.

                                @Override
                                public void run() {
                                    setsDialog.open();
                                }
                            });
                    selectedContext = setsDialog.getSelectedContext();
                    connection.setContextName(selectedContext);
                } else {
                    selectedContext = contextItem.getDefaultContext();
                }
            }
            // if can cancel, can't return the default contex by auto.
            return ContextUtils.getContextTypeByName(contextItem, selectedContext, !canCancel);
        }
        return null;
    }

    /**
     *
     * ggu Comment method "getOriginalValue".
     *
     * if value is context mode, return original value.
     */
    @SuppressWarnings("unchecked")
    public static String getOriginalValue(ContextType contextType, final String value) {
        if (value == null) {
            return EMPTY;
        }
        if (contextType != null && ContextParameterUtils.isContainContextParam(value)) {
            String var = ContextParameterUtils.getVariableFromCode(value);
            if (var != null) {
                ContextParameterType param = null;
                for (ContextParameterType paramType : (List<ContextParameterType>) contextType.getContextParameter()) {
                    if (paramType.getName().equals(var)) {
                        param = paramType;
                        break;
                    }
                }
                if (param != null) {
                    String value2 = param.getRawValue();
                    if (value2 != null) {
                        // return TalendTextUtils.removeQuotes(value2); //some value can't be removed for quote
                        return value2;
                    }
                }
                return EMPTY;
            }
        }
        return value;
    }

    public static void revertPropertiesForContextMode(ConnectionItem connItem, ContextType contextType) {
        if (connItem == null || contextType == null) {
            return;
        }
        Connection conn = connItem.getConnection();
        if (conn instanceof JSONFileConnection) {
            revertJSONFilePropertiesForContextMode((JSONFileConnection) conn, contextType);
        }
        // set connection for context mode
        conn.setContextMode(false);
        conn.setContextId(EMPTY);
    }

    static List<IContextParameter> getJSONFileVariables(String prefixName, JSONFileConnection conn) {

        if (conn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + LINE;
        String paramName = null;
        if (!conn.isInputModel()) {
            String outputFilePath = conn.getOutputFilePath();
            paramName = prefixName + EParamName.OutputFilePath;
            createParameters(varList, paramName, outputFilePath, JavaTypesManager.FILE);
        } else {

            String jsonFilePath = conn.getJSONFilePath();
            String encoding = conn.getEncoding();

            if (LANGUAGE.equals(ECodeLanguage.PERL)) {
                jsonFilePath = TalendQuoteUtils.addQuotes(jsonFilePath);
                encoding = TalendQuoteUtils.addQuotes(encoding);
            }
            paramName = prefixName + EParamName.FilePath;
            createParameters(varList, paramName, jsonFilePath, JavaTypesManager.FILE);

            paramName = prefixName + EParamName.Encoding;
            createParameters(varList, paramName, encoding);

            EList schema = conn.getSchema();
            if (schema != null) {
                Object object = schema.get(0);
                if (object instanceof JSONXPathLoopDescriptor) {
                    JSONXPathLoopDescriptor loopDesc = (JSONXPathLoopDescriptor) object;
                    paramName = prefixName + EParamName.XPathQuery;
                    String absoluteXPathQuery = loopDesc.getAbsoluteXPathQuery();
                    if (LANGUAGE.equals(ECodeLanguage.PERL)) {
                        absoluteXPathQuery = TalendQuoteUtils.addQuotes(absoluteXPathQuery);
                    }
                    createParameters(varList, paramName, absoluteXPathQuery);
                }
            }
        }

        return varList;
    }

    public static String getConnectionXPathQuery(JSONFileConnection conn) {
        EList schema = conn.getSchema();
        if (schema != null) {
            Object object = schema.get(0);
            if (object instanceof JSONXPathLoopDescriptor) {
                JSONXPathLoopDescriptor loopDesc = (JSONXPathLoopDescriptor) object;
                String absoluteXPathQuery = loopDesc.getAbsoluteXPathQuery();
                return absoluteXPathQuery;
            }
        }
        return null;
    }

    public static void setConnectionXPathQuery(JSONFileConnection conn, String newValue) {
        EList schema = conn.getSchema();
        if (schema != null) {
            Object object = schema.get(0);
            if (object instanceof JSONXPathLoopDescriptor) {
                JSONXPathLoopDescriptor loopDesc = (JSONXPathLoopDescriptor) object;
                loopDesc.setAbsoluteXPathQuery(newValue);
            }
        }
    }

    static void setJSONFilePropertiesForContextMode(String prefixName, JSONFileConnection conn) {
        if (conn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + LINE;
        String paramName = null;

        if (conn.isInputModel()) {
            paramName = prefixName + EParamName.FilePath;
            conn.setJSONFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            paramName = prefixName + EParamName.Encoding;
            conn.setEncoding(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            EList schema = conn.getSchema();
            if (schema != null) {
                if (schema.get(0) instanceof JSONXPathLoopDescriptor) {
                    JSONXPathLoopDescriptor descriptor = (JSONXPathLoopDescriptor) schema.get(0);
                    paramName = prefixName + EParamName.XPathQuery;
                    descriptor.setAbsoluteXPathQuery(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                }
            }
        } else {
            paramName = prefixName + EParamName.OutputFilePath;
            conn.setOutputFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        }

    }

    static void setJSONFilePropertiesForExistContextMode(JSONFileConnection jsonConn, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> map) {
        if (jsonConn == null) {
            return;
        }
        String jsonVariableName = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof EParamName) {
                EParamName jsonParam = (EParamName) param;
                if (map != null && map.size() > 0) {
                    for (Map.Entry<ContextItem, List<ConectionAdaptContextVariableModel>> entry : map.entrySet()) {
                        List<ConectionAdaptContextVariableModel> modelList = entry.getValue();
                        for (ConectionAdaptContextVariableModel model : modelList) {
                            if (model.getValue().equals(jsonParam.name())) {
                                jsonVariableName = model.getName();
                                break;
                            }
                        }
                    }
                }
                if (jsonConn.isInputModel()) {
                    switch (jsonParam) {
                    case FilePath:
                        jsonConn.setJSONFilePath(ContextParameterUtils.getNewScriptCode(jsonVariableName, LANGUAGE));
                        break;
                    case Encoding:
                        jsonConn.setEncoding(ContextParameterUtils.getNewScriptCode(jsonVariableName, LANGUAGE));
                        break;
                    case XPathQuery:
                        EList schema = jsonConn.getSchema();
                        if (schema != null) {
                            if (schema.get(0) instanceof JSONXPathLoopDescriptor) {
                                JSONXPathLoopDescriptor descriptor = (JSONXPathLoopDescriptor) schema.get(0);
                                descriptor.setAbsoluteXPathQuery(ContextParameterUtils.getNewScriptCode(jsonVariableName,
                                        LANGUAGE));
                            }
                        }
                    default:
                    }

                } else {
                    if (jsonParam.equals(EParamName.OutputFilePath)) {
                        jsonConn.setOutputFilePath(ContextParameterUtils.getNewScriptCode(jsonVariableName, LANGUAGE));
                    }
                }
            }
        }
    }

    static void revertJSONFilePropertiesForContextMode(JSONFileConnection conn, ContextType contextType) {
        if (conn == null || contextType == null) {
            return;
        }
        if (!conn.isInputModel()) {
            String outputFilePath = getOriginalValue(contextType, conn.getOutputFilePath());
            outputFilePath = TalendQuoteUtils.removeQuotes(outputFilePath);
            conn.setOutputFilePath(outputFilePath);
        } else {
            String filePath = getOriginalValue(contextType, conn.getJSONFilePath());
            String encoding = getOriginalValue(contextType, conn.getEncoding());

            filePath = TalendQuoteUtils.removeQuotes(filePath);
            conn.setJSONFilePath(filePath);
            encoding = TalendQuoteUtils.removeQuotes(encoding);
            conn.setEncoding(encoding);
            EList schema = conn.getSchema();
            if (schema != null) {
                if (schema.get(0) instanceof JSONXPathLoopDescriptor) {
                    JSONXPathLoopDescriptor descriptor = (JSONXPathLoopDescriptor) schema.get(0);
                    String xPahtQuery = getOriginalValue(contextType, descriptor.getAbsoluteXPathQuery());
                    xPahtQuery = TalendQuoteUtils.removeQuotes(xPahtQuery);
                    descriptor.setAbsoluteXPathQuery(xPahtQuery);
                }
            }

        }
    }

}
