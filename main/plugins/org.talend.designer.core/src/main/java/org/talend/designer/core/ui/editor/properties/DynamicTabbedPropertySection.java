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
package org.talend.designer.core.ui.editor.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.IExtendedTableCommand;
import org.talend.commons.utils.data.container.Content;
import org.talend.commons.utils.data.container.ContentList;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.HeaderFooterConnection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.outline.NodeReturnsTreeEditPart;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.views.properties.DynamicPropertyGenerator;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 * @deprecated use MultipleThreadDynamicComposite instead.
 */
@Deprecated
public class DynamicTabbedPropertySection extends AbstractPropertySection implements IDynamicProperty {

    protected AbstractMultiPageTalendEditor part;

    protected Element elem;

    protected Composite composite;

    protected BidiMap hashCurControls;

    protected String currentComponent;

    protected EComponentCategory section;

    protected int curRowSize;

    protected DynamicPropertyGenerator generator = new DynamicPropertyGenerator();

    private String oldProcessType;

    private final Map<String, IMetadataTable> repositoryTableMap;

    private final Map<String, ConnectionItem> repositoryConnectionItemMap;

    private Map<String, IRepositoryViewObject> processMap;

    private String oldPropertyType;

    private final Map<String, Query> repositoryQueryStoreMap;

    private String oldQueryStoreType;

    private static CommandStackEventListener commandStackEventListener;

    private Map<String, String> tableIdAndDbTypeMap;

    private Map<String, String> tableIdAndDbSchemaMap;

    private boolean forceRedraw;

    private static DynamicTabbedPropertySection lastPropertyUsed;

    private int lastCompositeSize = 0;

    private boolean propertyResized;

    public static final boolean DEBUG_TIME = false;

    // private final String extraPropertyTypeName;

    // private final String extraRepositoryPropertyTypeName;
    //
    private final String updataComponentParamName;

    /**
     * ftang Comment method "showQueryStoreRepositoryList".
     *
     * @param show
     */
    private void showQueryStoreRepositoryList(boolean show) {
        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
                param.setShow(show);
            }
        }
    }

    /**
     * ftang Comment method "showPropertyRepositoryList".
     *
     * @param show boolean
     */
    private void showPropertyRepositoryList(boolean show, boolean extra) {
        // for (int i = 0; i < elem.getElementParameters().size(); i++) {
        // IElementParameter param = elem.getElementParameters().get(i);
        // if (extra && param.getName().equals(extraRepositoryPropertyTypeName)) {
        // param.setShow(show);
        //
        // } else if (!extra && param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
        // param.setShow(show);
        // }
        // }
    }

    /**
     * ftang Comment method "updateProcessList".
     */
    private void updateProcessList() {
        List<String> processNameList = new ArrayList<String>();
        List<String> processValueList = new ArrayList<String>();
        processMap = new HashMap<String, IRepositoryViewObject>();

        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        try {
            RootContainer<String, IRepositoryViewObject> processContainer = factory.getMetadata(ERepositoryObjectType.PROCESS);
            ContentList<String, IRepositoryViewObject> processAbsoluteMembers = processContainer.getAbsoluteMembers();

            String currentProcess = part.getTalendEditor().getProcess().getLabel();
            for (Content<String, IRepositoryViewObject> object : processAbsoluteMembers.values()) {
                IRepositoryViewObject process = object.getContent();
                if (factory.getStatus(process) != ERepositoryStatus.DELETED && !currentProcess.equals(process.getLabel())) {
                    String path = object.getParent().getPath().toString();
                    String name;
                    if (path.equals("")) { //$NON-NLS-1$
                        name = IPath.SEPARATOR + process.getLabel();
                    } else {
                        name = IPath.SEPARATOR + path + IPath.SEPARATOR + process.getLabel();
                    }
                    processNameList.add(name);
                    processMap.put(name, process);
                }
            }
        } catch (PersistenceException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        List<String> tempFolderList = new ArrayList<String>();
        List<String> tempProcessNameList = new ArrayList<String>();
        tempProcessNameList.addAll(processNameList);

        for (String string : tempProcessNameList) {
            // Put jobs which in a folder into a new list.s
            if (string.lastIndexOf("/") != 0) { //$NON-NLS-1$
                tempFolderList.add(string);
                processNameList.remove(string);
            }
        }

        sortList(processNameList);
        sortList(tempFolderList);

        // Always put the jobs which in a folder on the top of the job list
        tempFolderList.addAll(processNameList);

        processNameList = tempFolderList;

        for (String name : processNameList) {
            IRepositoryViewObject process = processMap.get(name);
            processValueList.add(process.getLabel());
        }
        String[] processTableNameList = processNameList.toArray(new String[0]);
        String[] processTableValueList = processValueList.toArray(new String[0]);

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
                param.setListItemsDisplayName(processTableNameList);
                param.setListItemsValue(processTableValueList);
                if (elem instanceof Node) {
                    ((Node) elem).checkAndRefreshNode();
                }
            }
        }
    }

    /**
     * Sort the element order of the inputed list.
     *
     * @param compareList
     */
    private void sortList(List<String> compareList) {
        Collections.sort(compareList, new Comparator<String>() {

            @Override
            public int compare(String str1, String str2) {

                // For example: avoid job name "a_b_c" before "a_b" in the job
                // list.
                String newStr1 = str1.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
                String newStr2 = str2.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
                return newStr1.compareToIgnoreCase(newStr2);
            }
        });
    }

    /**
     * ftang Comment method "updateContextList".
     */
    private void updateContextList() {
        List<String> contextNameList = new ArrayList<String>();
        List<String> contextValueList = new ArrayList<String>();
        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();

        String selectedProcess = null;

        for (int i = 0; (i < elem.getElementParameters().size()) && (selectedProcess == null); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
                selectedProcess = (String) param.getValue();
            }
        }

        if (selectedProcess == null) {
            return;
        }

        try {
            List<IRepositoryViewObject> list = factory.getAll(ERepositoryObjectType.PROCESS);

            for (IRepositoryViewObject process : list) {
                String id = process.getLabel();
                if (selectedProcess.equals(id)) {
                    if (process.getProperty().getItem() instanceof ProcessItem) {
                        ProcessItem processItem = (ProcessItem) process.getProperty().getItem();

                        for (Object o : processItem.getProcess().getContext()) {
                            if (o instanceof ContextType) {
                                ContextType context = (ContextType) o;
                                contextNameList.add(context.getName());
                                contextValueList.add(context.getName());
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        String[] contextTableNameList = contextNameList.toArray(new String[0]);
        String[] contextTableValueList = contextValueList.toArray(new String[0]);

        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getName().equals(EParameterName.PROCESS_TYPE_CONTEXT.getName())) {
                param.setListItemsDisplayName(contextTableNameList);
                param.setListItemsValue(contextTableValueList);
                if (!contextValueList.contains(param.getValue())) {
                    if (contextTableNameList.length > 0) {
                        elem.setPropertyValue(EParameterName.PROCESS_TYPE_CONTEXT.getName(), contextTableValueList[0]);
                    }
                } else {
                    // force to store the value again to activate the code
                    // generation in Node.setPropertyValue
                    elem.setPropertyValue(EParameterName.PROCESS_TYPE_CONTEXT.getName(), param.getValue());
                }
            }
        }

    }

    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(connectionItem);
        String aliasName = repositoryObjectType.getAlias();
        Connection connection = connectionItem.getConnection();
        if (connection instanceof DatabaseConnection) {
            String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
            aliasName += " (" + currentDbType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return aliasName;
    }

    private final Map<String, List<String>> tablesMap = new HashMap<String, List<String>>();

    private final Map<String, List<String>> queriesMap = new HashMap<String, List<String>>();

    /**
     * ftang Comment method "updateRepositoryList".
     */
    @SuppressWarnings("unchecked")
    public void updateRepositoryList() {
        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        tableIdAndDbTypeMap = new HashMap<String, String>();
        tableIdAndDbSchemaMap = new HashMap<String, String>();
        List<ConnectionItem> metadataConnectionsItem = null;
        String[] repositoryTableNameList = new String[] {};
        String[] repositoryTableValueList = new String[] {};
        String[] repositoryConnectionNameList = new String[] {};
        String[] repositoryConnectionValueList = new String[] {};
        String[] repositoryQueryNameList = new String[] {};
        String[] repositoryQueryValueList = new String[] {};
        try {
            metadataConnectionsItem = factory.getMetadataConnectionsItem();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        if (metadataConnectionsItem != null) {
            repositoryTableMap.clear();
            repositoryQueryStoreMap.clear();
            repositoryConnectionItemMap.clear();

            tablesMap.clear();
            queriesMap.clear();
            List<String> tableNamesList = new ArrayList<String>();
            List<String> tableValuesList = new ArrayList<String>();
            List<String> queryStoreNameList = new ArrayList<String>();
            List<String> queryStoreValuesList = new ArrayList<String>();
            for (ConnectionItem connectionItem : metadataConnectionsItem) {
                Connection connection = connectionItem.getConnection();
                if (!connection.isReadOnly()) {
                    repositoryConnectionItemMap.put(connectionItem.getProperty().getId() + "", connectionItem); //$NON-NLS-1$
                    for (Object tableObj : ConnectionHelper.getTables(connection)) {
                        org.talend.core.model.metadata.builder.connection.MetadataTable table;

                        table = (org.talend.core.model.metadata.builder.connection.MetadataTable) tableObj;

                        if (factory.getStatus(connectionItem) != ERepositoryStatus.DELETED) {
                            if (!factory.isDeleted(table)) {
                                String name = getRepositoryAliasName(connectionItem) + ":" //$NON-NLS-1$
                                        + connectionItem.getProperty().getLabel() + " - " + table.getLabel(); //$NON-NLS-1$
                                String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$
                                IMetadataTable newTable = ConvertionHelper.convert(table);
                                repositoryTableMap.put(value, newTable);
                                if (connection instanceof DatabaseConnection) {
                                    String dbType = ((DatabaseConnection) connection).getDatabaseType();
                                    String schema = ((DatabaseConnection) connection).getUiSchema();
                                    tableIdAndDbTypeMap.put(newTable.getId(), dbType);
                                    if (schema != null && !schema.equals("")) { //$NON-NLS-1$
                                        tableIdAndDbSchemaMap.put(newTable.getId(), schema);
                                    }
                                }
                                addOrderDisplayNames(tableValuesList, tableNamesList, value, name);
                                // tableNamesList.add(name);
                                // tableValuesList.add(value);
                            }
                        }
                    }
                }
                tablesMap.put(connectionItem.getProperty().getId(), tableValuesList);
                if (connection instanceof DatabaseConnection && !connection.isReadOnly()) {
                    DatabaseConnection dbConnection = (DatabaseConnection) connection;
                    QueriesConnection queriesConnection = dbConnection.getQueries();
                    if (queriesConnection != null) {
                        List<Query> qs = queriesConnection.getQuery();
                        for (Query query : qs) {
                            String name = getRepositoryAliasName(connectionItem) + ":" //$NON-NLS-1$
                                    + connectionItem.getProperty().getLabel() + " - " + query.getLabel(); //$NON-NLS-1$
                            String value = connectionItem.getProperty().getId() + " - " + query.getLabel(); //$NON-NLS-1$
                            repositoryQueryStoreMap.put(value, query);
                            addOrderDisplayNames(queryStoreValuesList, queryStoreNameList, value, name);
                            // queryStoreNameList.add(name);
                            // queryStoreValuesList.add(value);
                        }
                    }
                }
                queriesMap.put(connectionItem.getProperty().getId(), queryStoreValuesList);
            }
            repositoryTableNameList = tableNamesList.toArray(new String[0]);
            repositoryTableValueList = tableValuesList.toArray(new String[0]);
            repositoryQueryNameList = queryStoreNameList.toArray(new String[0]);
            repositoryQueryValueList = queryStoreValuesList.toArray(new String[0]);
        }
        initMaps();
        for (int i = 0; i < elem.getElementParameters().size(); i++) {
            IElementParameter param = elem.getElementParameters().get(i);
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                IElementParameter repositorySchemaType = param.getChildParameters().get(
                        EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                repositorySchemaType.setListItemsDisplayName(repositoryTableNameList);
                repositorySchemaType.setListItemsValue(repositoryTableValueList);
                if (!repositoryTableMap.keySet().contains(repositorySchemaType.getValue())) {
                    List<String> list2 = tablesMap.get(elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()));
                    boolean isNeeded = list2 != null && !list2.isEmpty();
                    if (repositoryTableNameList.length > 0 && repositoryConnectionValueList.length > 0 && isNeeded) {
                        repositorySchemaType.setValue(getDefaultRepository(param, true, repositoryConnectionValueList[0]));
                        // elem.setPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName(),
                        // getDefaultRepository(
                        // true, repositoryConnectionValueList[0]));
                    }
                }
            }
            if (param.getName().equals(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
                param.setListItemsDisplayName(repositoryQueryNameList);
                param.setListItemsValue(repositoryQueryValueList);
                if (!repositoryQueryStoreMap.keySet().contains(param.getValue())) {
                    List<String> list2 = queriesMap.get(elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()));
                    boolean isNeeded = list2 != null && !list2.isEmpty();
                    if (repositoryQueryNameList.length > 0 && repositoryConnectionValueList.length > 0 && isNeeded) {
                        elem.setPropertyValue(
                                EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(),
                                getDefaultRepository(elem.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE), false,
                                        repositoryConnectionValueList[0]));
                    }
                }
            }
            if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
                updateRepositoryListExtra(param, repositoryConnectionNameList, repositoryConnectionValueList, false);
            }
            // // for job settings extra (feature 2710)
            // if (param.getName().equals(extraRepositoryPropertyTypeName)) {
            // updateRepositoryListExtra(param, repositoryConnectionNameList, repositoryConnectionValueList, true);
            // }
        }
        updateQuery();
    }

    private void updateRepositoryListExtra(IElementParameter param, String[] repositoryConnectionNameList,
            String[] repositoryConnectionValueList, boolean extra) {
        String paramName = EParameterName.PROPERTY_TYPE.getName();
        // if (extra) {
        // paramName = extraPropertyTypeName;
        // }
        String repositoryValue = elem.getElementParameter(paramName).getRepositoryValue();
        if (repositoryValue != null) {
            List<String> connectionNamesList = new ArrayList<String>();
            List<String> connectionValuesList = new ArrayList<String>();
            for (String key : repositoryConnectionItemMap.keySet()) {
                ConnectionItem connectionItem = repositoryConnectionItemMap.get(key);
                Connection connection = connectionItem.getConnection();
                String name = getRepositoryAliasName(connectionItem) + ":" //$NON-NLS-1$
                        + connectionItem.getProperty().getLabel();
                if ((connection instanceof DelimitedFileConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.DELIMITED.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof PositionalFileConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.POSITIONAL.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof RegexpFileConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.REGEX.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof XmlFileConnection) && (repositoryValue.equals(ERepositoryCategoryType.XML.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof FileExcelConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.EXCEL.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof GenericSchemaConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.GENERIC.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof LDAPSchemaConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.LDAP.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
                if ((connection instanceof SalesforceSchemaConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.SALESFORCE.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }

                if ((connection instanceof DatabaseConnection)
                        && (repositoryValue.startsWith(ERepositoryCategoryType.DATABASE.getName()))) {
                    String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
                    if (repositoryValue.contains(":")) { // database //$NON-NLS-1$
                        // is
                        // specified
                        // //$NON-NLS-1$
                        String neededDbType = repositoryValue.substring(repositoryValue.indexOf(":") + 1); //$NON-NLS-1$
                        if (neededDbType.equals(currentDbType)) {
                            addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                        }
                    } else {
                        addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                    }
                }
                if ((connection instanceof HeaderFooterConnection)
                        && (repositoryValue.equals(ERepositoryCategoryType.HEADERFOOTER.getName()))) {
                    addOrderDisplayNames(connectionValuesList, connectionNamesList, key, name);
                }
            }

            repositoryConnectionNameList = connectionNamesList.toArray(new String[0]);
            repositoryConnectionValueList = connectionValuesList.toArray(new String[0]);
        } else {
            List<String> connectionValuesList = new ArrayList<String>();
            List<String> connectionStringList = new ArrayList<String>();
            for (String key : repositoryConnectionItemMap.keySet()) {
                ConnectionItem connectionItem = repositoryConnectionItemMap.get(key);
                String name = connectionItem.getProperty().getLabel();
                addOrderDisplayNames(connectionValuesList, connectionStringList, key, name);
            }
            repositoryConnectionNameList = connectionStringList.toArray(new String[0]);
            repositoryConnectionValueList = connectionValuesList.toArray(new String[0]);
        }
        param.setListItemsDisplayName(repositoryConnectionNameList);
        param.setListItemsValue(repositoryConnectionValueList);
        if (!repositoryConnectionItemMap.keySet().contains(param.getValue())) {
            if (repositoryConnectionNameList.length > 0) {
                // paramName = EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
                // if (extra) {
                // paramName = extraRepositoryPropertyTypeName;
                // }
                elem.setPropertyValue(paramName, repositoryConnectionValueList[0]);
            }
        }
    }

    /**
     * qzhang Comment method "addOrderDisplayNames".
     *
     * @param connectionValuesList
     * @param connectionStringList
     * @param key
     * @param name
     */
    private void addOrderDisplayNames(List<String> connectionValuesList, List<String> connectionStringList, String key,
            String name) {
        int i = 0;

        for (; i < connectionStringList.size(); i++) {
            String string = connectionStringList.get(i);
            if (name.compareTo(string) < 0) {
                connectionStringList.add(i, name);
                connectionValuesList.add(i, key);
                break;
            }
        }
        if (connectionStringList.size() == 0 || i == connectionStringList.size()) {
            connectionStringList.add(name);
            connectionValuesList.add(key);
        }
    }

    /**
     * ftang Comment method "getElement".
     *
     * @return an instance of Element
     */
    @Override
    public Element getElement() {
        return elem;
    }

    /**
     *
     */
    private boolean checkErrorsWhenViewRefreshed;

    public void addComponents(boolean forceRedraw) {
        addComponents(forceRedraw, true);
    }

    /**
     * Initialize all components for the defined section for this node.
     */
    public void addComponents(boolean forceRedraw, boolean reInitialize) {
        registerListenerForRefreshingSection();
        checkErrorsWhenViewRefreshed = true;
        int heightSize = 0, maxRowSize = 0, nbInRow, numInRow;
        int maxRow;
        List<? extends IElementParameter> listParam = elem.getElementParameters();

        oldQueryStoreType = (String) elem.getPropertyValue(EParameterName.QUERYSTORE_TYPE.getName());
        if (oldQueryStoreType != null) {
            if (oldQueryStoreType.equals(EmfComponent.REPOSITORY)) {
                showQueryStoreRepositoryList(true);
                updateRepositoryList();
            } else {
                showQueryStoreRepositoryList(false);
            }
        }

        IElementParameter param = elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName());
        if (param != null) {
            oldPropertyType = (String) param.getValue();
            if (param.isShow(elem.getElementParameters())) {
                if (oldPropertyType.equals(EmfComponent.REPOSITORY)) {
                    showPropertyRepositoryList(true, false);
                    updateRepositoryList();
                } else {
                    showPropertyRepositoryList(false, false);
                }
            } else {
                showPropertyRepositoryList(false, false);
            }
        }
        // for job settings extra.(feature 2710)
        // param = elem.getElementParameter(extraPropertyTypeName);
        if (param != null) {
            oldPropertyType = (String) param.getValue();
            if (param.isShow(elem.getElementParameters())) {
                if (oldPropertyType.equals(EmfComponent.REPOSITORY)) {
                    showPropertyRepositoryList(true, true);
                    updateRepositoryList();
                } else {
                    showPropertyRepositoryList(false, true);
                }
            } else {
                showPropertyRepositoryList(false, true);
            }
        }
        oldProcessType = (String) elem.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
        if (oldProcessType != null) {
            String[] list = elem.getElementParameter(EParameterName.PROCESS_TYPE_PROCESS.getName()).getListItemsDisplayName();
            if ((oldProcessType.equals("NO_PROCESS") || (list.length == 0))) { //$NON-NLS-1$
                updateProcessList();
                updateContextList();
                if (elem instanceof Node) {
                    ((Node) elem).checkAndRefreshNode();
                }
            }
        }

        if (!forceRedraw) {
            boolean needRedraw = false;
            for (IElementParameter elementParameter : elem.getElementParametersWithChildrens()) {
                if (elementParameter.getCategory().equals(section)
                        && (elementParameter.getFieldType() != EParameterFieldType.SCHEMA_TYPE)
                        && (elementParameter.getFieldType() != EParameterFieldType.SCHEMA_REFERENCE)
                        && (elementParameter.getFieldType() != EParameterFieldType.QUERYSTORE_TYPE)) {
                    // if the component must be displayed, then check if the
                    // control exists or not.
                    boolean show = elementParameter.isShow(elem.getElementParameters());
                    Object control;
                    if (elementParameter.getParentParameter() == null) {
                        control = this.hashCurControls.get(elementParameter.getName());
                    } else {
                        control = this.hashCurControls.get(elementParameter.getParentParameter().getName() + ":" //$NON-NLS-1$
                                + elementParameter.getName());
                    }
                    if ((control == null && show) || (control != null && !show)) {
                        needRedraw = true;
                        break;
                        // System.out.println(elementParameter.getName() + "
                        // need redraw");
                    }
                }
            }
            if (!needRedraw) {
                // System.out.println("no need redraw");
                return;
            }
        }

        Control lastControl = null;
        if (reInitialize) {
            if (currentComponent != null && composite != null) {
                Control[] ct = composite.getChildren();
                for (Control element : ct) {
                    element.dispose();
                }
            }
        } else {
            // Control[] ct = composite.getChildren();
            // int curY = 0, maxY = 0;
            // for (int i = 0; i < ct.length; i++) {
            // curY = ct[i].getLocation().y + ct[i].computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            // if (curY > maxY) {
            // maxY = curY;
            // }
            // }
            heightSize = curRowSize;
        }

        hashCurControls = new DualHashBidiMap();

        maxRow = 0;
        for (int i = 0; i < listParam.size(); i++) {
            if (listParam.get(i).getCategory() == section) {
                if (listParam.get(i).getNumRow() > maxRow && listParam.get(i).isShow(listParam)) {
                    maxRow = listParam.get(i).getNumRow();
                }
            }
        }

        generator.initController(this);

        // System.out.println("********************** NEW ADDCOMPONENTS
        // **********************");
        TabbedPropertyComposite tabbedPropertyComposite = this.getTabbedPropertyComposite();
        int additionalHeightSize = 0;
        if (tabbedPropertyComposite != null && (!(elem instanceof org.talend.designer.core.ui.editor.connections.Connection))) {
            boolean hasDynamicRow = false;
            for (int i = 0; i < listParam.size(); i++) {
                IElementParameter curParam = listParam.get(i);
                if (curParam.getCategory() == section) {
                    if (curParam.getFieldType() != EParameterFieldType.TECHNICAL) {
                        if (curParam.isShow(listParam)) {
                            AbstractElementPropertySectionController controller = generator.getController(
                                    curParam.getFieldType(), this);

                            if (controller == null) {
                                continue;
                            }
                            if (controller.hasDynamicRowSize()) {
                                hasDynamicRow = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (hasDynamicRow) {
                additionalHeightSize = estimatePropertyHeightSize(maxRow, listParam, tabbedPropertyComposite);
            }
        }

        long lastTime = TimeMeasure.timeSinceBegin("DTP:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        curRowSize = 0;
        for (int curRow = 1; curRow <= maxRow; curRow++) {
            maxRowSize = 0;
            nbInRow = 0;
            for (int i = 0; i < listParam.size(); i++) {
                IElementParameter curParam = listParam.get(i);
                if (curParam.getCategory() == section) {
                    if (curParam.getNumRow() == curRow && curParam.isShow(listParam)
                            && (curParam.getFieldType() != EParameterFieldType.TECHNICAL)) {
                        nbInRow++;
                    }
                }
            }
            numInRow = 0;
            lastControl = null;
            for (int i = 0; i < listParam.size(); i++) {
                IElementParameter curParam = listParam.get(i);
                if (curParam.getCategory() == section) {
                    if (curParam.getNumRow() == curRow && (curParam.getFieldType() != EParameterFieldType.TECHNICAL)) {
                        // System.out.println("test:" + curParam.getName() + "
                        // field:"+curParam.getField());
                        if (curParam.isShow(listParam)) {
                            // System.out.println("show:" + curParam.getName()+
                            // " field:"+curParam.getField());
                            numInRow++;
                            AbstractElementPropertySectionController controller = generator.getController(
                                    curParam.getFieldType(), this);

                            if (controller == null) {
                                continue;
                            }
                            if (controller.hasDynamicRowSize()) {
                                controller.setAdditionalHeightSize(additionalHeightSize);
                            }

                            lastControl = controller.createControl(composite, curParam, numInRow, nbInRow, heightSize,
                                    lastControl);
                            lastTime = TimeMeasure.timeSinceBegin("DTP:refresh:" + getCurrentComponent()) - lastTime; //$NON-NLS-1$
                            if ((DynamicTabbedPropertySection.DEBUG_TIME) && !getCurrentComponent().equals("Job")) { //$NON-NLS-1$
                                System.out
                                        .println("DTP;create:" + curParam.getFieldType().getName() + ";" + getCurrentComponent() //$NON-NLS-1$ //$NON-NLS-2$
                                                + ";" + lastTime); //$NON-NLS-1$
                            }

                            // System.out.println("param:" + curParam.getName()
                            // + " - curRowSize:" + curRowSize);

                            if (curRowSize > maxRowSize) {
                                maxRowSize = curRowSize;
                            }
                        }
                    }
                }
            }
            heightSize += maxRowSize;

        }
        resizeScrolledComposite();
    }

    /**
     * DOC nrousseau Comment method "estimatePropertyHeightSize".
     *
     * @param maxRow
     * @param listParam
     * @param tabbedPropertyComposite
     */
    private int estimatePropertyHeightSize(int maxRow, List<? extends IElementParameter> listParam,
            TabbedPropertyComposite tabbedPropertyComposite) {
        int estimatedHeightSize = 0, estimatedMaxRowSize = 0;
        int additionalHeightSize = 0;
        int compositeHeight = tabbedPropertyComposite.getClientArea().height
                - tabbedPropertyComposite.getTitle().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        // System.out.println("size composite:" + compositeHeight);

        int nbDynamic = 0;
        for (int curRow = 1; curRow <= maxRow; curRow++) {
            estimatedMaxRowSize = 0;
            for (int i = 0; i < listParam.size(); i++) {
                IElementParameter curParam = listParam.get(i);
                if (curParam.getCategory() == section) {
                    if (curParam.getNumRow() == curRow && (curParam.getFieldType() != EParameterFieldType.TECHNICAL)) {
                        // System.out.println("test:" + curParam.getName() + "
                        // field:"+curParam.getField());
                        if (curParam.isShow(listParam)) {
                            // System.out.println("show:" + curParam.getName()+
                            // " field:"+curParam.getField());
                            AbstractElementPropertySectionController controller = generator.getController(
                                    curParam.getFieldType(), this);

                            if (controller == null) {
                                break;
                            }
                            int estimatedSize = controller.estimateRowSize(composite, curParam);
                            if (controller.hasDynamicRowSize()) {
                                nbDynamic++;
                            }
                            // System.out.println("param:" + curParam.getName()
                            // + " - estimatedSize:" + estimatedSize);

                            if (estimatedSize > estimatedMaxRowSize) {
                                estimatedMaxRowSize = estimatedSize;
                            }
                        }
                    }
                }
            }
            estimatedHeightSize += estimatedMaxRowSize;
        }
        // System.out.println("*** ESTIMATED SIZE:" + estimatedHeightSize + "
        // ***");
        int emptySpace = compositeHeight - estimatedHeightSize;
        // System.out.println("--- EMPTY SPACE:" + emptySpace);
        if (emptySpace > 0 && nbDynamic > 0) {
            additionalHeightSize = emptySpace / nbDynamic;
            // System.out.println("--- DIVIDED ADDITIONAL HEIGHT (for each
            // dynamic):" + additionalHeightSize);
        }
        return additionalHeightSize;
    }

    private TabbedPropertyComposite getTabbedPropertyComposite() {
        TabbedPropertyComposite tabbedPropertyComposite = null;
        Composite tmpComposite = composite;
        while (tabbedPropertyComposite == null) { // to retrieve the main
            // composite of the TabbedProperties
            if (tmpComposite == null || tmpComposite.isDisposed() || tmpComposite.getParent() == null
                    || tmpComposite.getParent().isDisposed()) {
                return null;
            }
            if (tmpComposite.getParent() instanceof TabbedPropertyComposite) {
                tabbedPropertyComposite = (TabbedPropertyComposite) tmpComposite.getParent();
            } else {
                tmpComposite = tmpComposite.getParent();
            }
        }
        return tabbedPropertyComposite;
    }

    /**
     * added to fix the bugs 1107 & 1434. This code is added because the function resizeScrolledComposite is set as
     * private in TabbedPropertySheetPage. So the code bellow will do the same operation but will access to specific
     * eclipse functions.
     */
    private void resizeScrolledComposite() {
        Point compositeSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        TabbedPropertyComposite tabbedPropertyComposite = getTabbedPropertyComposite();
        if (tabbedPropertyComposite != null) {
            tabbedPropertyComposite.getScrolledComposite().setExpandHorizontal(true);
            lastCompositeSize = tabbedPropertyComposite.getClientArea().height;
            compositeSize.y += tabbedPropertyComposite.getTitle().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            tabbedPropertyComposite.getScrolledComposite().setMinSize(compositeSize);
            tabbedPropertyComposite.getScrolledComposite().setExpandHorizontal(false);
        }
        propertyResized = true;
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#
     * createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
     */
    @Override
    public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        composite = parent;

        FormLayout layout = new FormLayout();
        layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
        layout.marginHeight = ITabbedPropertyConstants.VSPACE;
        layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
        composite.setLayout(layout);
    }

    @Override
    public void refresh() {
        TimeMeasure.display = false;
        TimeMeasure.measureActive = true;
        TimeMeasure.begin("DTP:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        if (elem == null) {
            return;
        }
        List<? extends IElementParameter> listParam = elem.getElementParameters();

        if (oldProcessType != null) {
            String newProcessType = (String) elem.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
            if (!oldProcessType.equals(newProcessType)) {
                updateProcessList();
                updateContextList();
                if (elem instanceof Node) {
                    ((Node) elem).checkAndRefreshNode();
                }
            }
        }

        Boolean updateNeeded = (Boolean) elem.getPropertyValue(updataComponentParamName);
        if (updateNeeded != null) {
            if (updateNeeded) {
                addComponents(forceRedraw);
                elem.setPropertyValue(updataComponentParamName, new Boolean(false));
            }
        }
        forceRedraw = false;

        for (int i = 0; i < listParam.size(); i++) {
            if (listParam.get(i).getCategory() == section) {
                if (listParam.get(i).isShow(listParam)) {
                    AbstractElementPropertySectionController controller = generator.getController(
                            listParam.get(i).getFieldType(), this);
                    if (controller != null) {
                        controller.refresh(listParam.get(i), checkErrorsWhenViewRefreshed);
                    }
                }
            }
        }
        if (propertyResized) {
            composite.getParent().layout(true, true);
            propertyResized = false;
        }
        checkErrorsWhenViewRefreshed = false;
        long time = TimeMeasure.timeSinceBegin("DTP:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        TimeMeasure.end("DTP:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        if ((DEBUG_TIME) && !getCurrentComponent().equals("Job")) { //$NON-NLS-1$
            System.out.println("DTP;total;" + getCurrentComponent() + ";" + time); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    // private ISelection lastSelection;
    /*
     * @Override (non-Java)
     *
     * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection# setInput(org.eclipse.ui.IWorkbenchPart,
     * org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setInput(final IWorkbenchPart workbenchPart, final ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        curRowSize = 0;
        Object input = ((IStructuredSelection) selection).getFirstElement();
        if (input instanceof NodeContainerPart) {
            NodeContainerPart nContainer = (NodeContainerPart) input;
            elem = (Element) nContainer.getParent().getModel();
        } else if (input instanceof NodeTreeEditPart) {
            NodeTreeEditPart nTreePart = (NodeTreeEditPart) input;
            elem = (Element) nTreePart.getModel();
        } else if (!(input instanceof NodeReturnsTreeEditPart)) {
            EditPart editPart = (EditPart) input;
            elem = (Element) editPart.getModel();
        }
        if (elem instanceof NodeLabel) {
            elem = ((NodeLabel) elem).getNode();
        }

        if (elem instanceof ConnectionLabel) {
            elem = ((ConnectionLabel) elem).getConnection();
        }

        if (workbenchPart instanceof AbstractMultiPageTalendEditor) {
            part = (AbstractMultiPageTalendEditor) workbenchPart;
        } else {
            if (elem instanceof INode) {
                part = (AbstractMultiPageTalendEditor) ((IProcess2) ((INode) elem).getProcess()).getEditor();
            } else if (elem instanceof IConnection) {
                IConnection conn;
                conn = (IConnection) elem;
                part = (AbstractMultiPageTalendEditor) ((IProcess2) conn.getSource().getProcess()).getEditor();
            } else {
                part = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getActiveEditor();
            }
        }
        super.setInput(part, selection);
        commandStackEventListener = null;

        if ((currentComponent == null) || (!currentComponent.equals(elem.getElementName()))) {
            forceRedraw = true;
            elem.setPropertyValue(updataComponentParamName, Boolean.TRUE);
        }
        currentComponent = elem.getElementName();
    }

    private static final Listener REFRESH_LISTENER = new Listener() {

        @Override
        public void handleEvent(Event event) {
            REFRESH_LIMITER.resetTimer();
            REFRESH_LIMITER.startIfExecutable(true, null);
        }
    };

    private static final ExecutionLimiter REFRESH_LIMITER = new ExecutionLimiter(250, true) {

        @Override
        public void execute(final boolean isFinalExecution, Object data) {
            Composite currentComposite = lastPropertyUsed.getComposite();
            if (currentComposite.isDisposed()) {
                return;
            }

            currentComposite.getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    TabbedPropertyComposite tabbedComposite = lastPropertyUsed.getTabbedPropertyComposite();
                    if (tabbedComposite != null) {
                        int currentSize = tabbedComposite.getClientArea().height;
                        if (lastPropertyUsed.getLastCompositeSize() != currentSize) {
                            lastPropertyUsed.addComponents(true);
                            lastPropertyUsed.refresh();
                        }
                    }
                }

            });

        }
    };

    /**
     * Set the section of the tabbed property.
     *
     * @param section
     */
    public DynamicTabbedPropertySection(final EComponentCategory section) {
        super();
        this.section = section;
        repositoryQueryStoreMap = new HashMap<String, Query>();
        repositoryConnectionItemMap = new HashMap<String, ConnectionItem>();
        repositoryTableMap = new HashMap<String, IMetadataTable>();
        hashCurControls = new DualHashBidiMap();
        // // for job settings extra (feature 2710)
        // extraPropertyTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName());
        // extraRepositoryPropertyTypeName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.REPOSITORY_PROPERTY_TYPE
        // .getName());
        // if (section == EComponentCategory.EXTRA) {
        // updataComponentParamName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName());
        // } else {
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        // }
    }

    /**
     * yzhang Comment method "setCurRowSize" Sets the curRowSize.
     *
     * @param curRowSize int
     */
    @Override
    public void setCurRowSize(int curRowSize) {
        this.curRowSize = curRowSize;
    }

    /**
     * dev Comment method "getRepositoryTableMap".
     *
     * @return Map
     */
    public Map<String, IMetadataTable> getRepositoryTableMap() {
        if (this.repositoryTableMap.keySet().isEmpty()) {
            updateRepositoryList();
        }
        return this.repositoryTableMap;
    }

    /**
     * dev Comment method "getRepositoryConnectionItemMap".
     *
     * @return Map
     */
    public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
        return this.repositoryConnectionItemMap;
    }

    /**
     * Getter for composite.
     *
     * @return the composite
     */
    @Override
    public Composite getComposite() {
        return this.composite;
    }

    /**
     * Getter for currentComponent.
     *
     * @return the currentComponent
     */
    public String getCurrentComponent() {
        return this.currentComponent;
    }

    /**
     * Getter for curRowSize.
     *
     * @return the curRowSize
     */
    @Override
    public int getCurRowSize() {
        return this.curRowSize;
    }

    /**
     * Getter for hashCurControls.
     *
     * @return the hashCurControls
     */
    @Override
    public BidiMap getHashCurControls() {
        return this.hashCurControls;
    }

    /**
     * Getter for part.
     *
     * @return the part
     */
    @Override
    public AbstractMultiPageTalendEditor getPart() {
        return this.part;
    }

    /**
     * Getter for section.
     *
     * @return the section
     */
    @Override
    public EComponentCategory getSection() {
        return this.section;
    }

    /**
     * Getter for repositoryQueryStoreMap.
     *
     * @return the repositoryQueryStoreMap
     */
    public Map<String, Query> getRepositoryQueryStoreMap() {
        return repositoryQueryStoreMap;
    }

    /**
     * Get the command stack of the Gef editor.
     *
     * @return
     */
    protected CommandStack getCommandStack() {
        AbstractTalendEditor talendEditor = part.getTalendEditor();
        Object adapter = talendEditor.getAdapter(CommandStack.class);
        return (CommandStack) adapter;
    }

    /**
     * amaumont Comment method "registerListenerForRefreshingSection".
     */
    public void registerListenerForRefreshingSection() {
        if (commandStackEventListener == null) {

            DynamicTabbedPropertySection.commandStackEventListener = new CommandStackEventListener() {

                @Override
                public void stackChanged(CommandStackEvent event) {
                    int detail = event.getDetail();
                    if (lastPropertyUsed != null) {
                        if ((lastPropertyUsed.getElement() instanceof org.talend.designer.core.ui.editor.connections.Connection)
                                && (event.getCommand() instanceof ChangeMetadataCommand)
                                && (0 != (detail & CommandStack.POST_EXECUTE) || 0 != (detail & CommandStack.POST_REDO) //
                                || 0 != (detail & CommandStack.POST_REDO))) {
                            lastPropertyUsed.addComponents(true);
                            lastPropertyUsed.refresh();
                        }
                        if (0 != (detail & CommandStack.POST_UNDO) || 0 != (detail & CommandStack.POST_REDO)) {
                            if (event.getCommand() instanceof IExtendedTableCommand) {
                                lastPropertyUsed.refresh();
                            }
                        }
                    }
                }

            };
            getCommandStack().addCommandStackEventListener(DynamicTabbedPropertySection.commandStackEventListener);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateQuery() {
        Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
        if (propertyValue == null || !(propertyValue instanceof String) || "".equals(propertyValue) //$NON-NLS-1$
                || elem.getPropertyValue(EParameterName.QUERYSTORE_TYPE.getName()).equals(EmfComponent.BUILTIN)) {
            return;
        }
        if (repositoryQueryStoreMap.containsKey(propertyValue)) {
            Query query = repositoryQueryStoreMap.get(propertyValue);
            for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                    elem.setPropertyValue(param.getName(), convertSQL(query.getValue()));
                    param.setRepositoryValueUsed(true);
                }
            }
        }
    }

    private String convertSQL(String sql) {

        if (sql.startsWith("'") || sql.startsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            return sql;
        }
        return TalendTextUtils.addQuotes(sql);
    }

    /**
     * qzhang Comment method "getDefaultRepository".
     *
     * @return
     */
    private String getDefaultRepository(IElementParameter baseParam, boolean istable, String defaultPropertyValue) {
        boolean metadataInput = false;
        if (((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) > 0
                || ((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.FLOW_REF) > 0
                || ((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.TABLE) > 0) {
            metadataInput = true;
        }

        if (metadataInput && istable) {
            return (String) baseParam.getChildParameters().get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName()).getValue();
        }
        Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if ((propertyValue == null || !(propertyValue instanceof String)) && defaultPropertyValue != null) {
            propertyValue = defaultPropertyValue;
        }
        if (propertyValue == null || propertyValue.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        if (istable) {
            List<String> list = tablesMap.get(propertyValue);
            if (list != null) {
                if (list.size() > 0) {
                    return tablesMap.get(propertyValue).get(0);
                }
            }
        } else {
            List<String> list = queriesMap.get(propertyValue);
            if (list != null) {
                if (queriesMap.get(propertyValue).size() > 0) {
                    return queriesMap.get(propertyValue).get(0);
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "initMaps".
     */
    private void initMaps() {
        for (String key : tablesMap.keySet()) {
            List<String> tablesName = new ArrayList<String>();
            List<String> queriesName = new ArrayList<String>();
            queriesName.addAll(queriesMap.get(key));
            tablesName.addAll(tablesMap.get(key));
            for (String string : tablesMap.get(key)) {
                if (!string.startsWith(key)) {
                    tablesName.remove(string);
                }
            }

            for (String string : queriesMap.get(key)) {
                if (!string.startsWith(key)) {
                    queriesName.remove(string);
                }
            }
            tablesMap.put(key, tablesName);
            queriesMap.put(key, queriesName);
        }
    }

    /**
     * Getter for tablesMap.
     *
     * @return the tablesMap
     */
    public Map<String, List<String>> getTablesMap() {
        initMaps();
        return this.tablesMap;
    }

    /**
     * Getter for queriesMap.
     *
     * @return the queriesMap
     */
    public Map<String, List<String>> getQueriesMap() {
        initMaps();
        return this.queriesMap;
    }

    /**
     * Getter for tableIdAndDbTypeMap.
     *
     * @return the tableIdAndDbTypeMap
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        return this.tableIdAndDbTypeMap;
    }

    /**
     * Getter for tableIdAndDbSchemaMap.
     *
     * @return the tableIdAndDbSchemaMap
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        return this.tableIdAndDbSchemaMap;
    }

    /**
     * Getter for lastCompositeSize.
     *
     * @return the lastCompositeSize
     */
    public int getLastCompositeSize() {
        return this.lastCompositeSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#aboutToBeShown()
     */
    @Override
    public void aboutToBeShown() {
        lastPropertyUsed = this;
        TabbedPropertyComposite tabbedPropertyComposite = getTabbedPropertyComposite();
        if (tabbedPropertyComposite != null) {
            tabbedPropertyComposite.getScrolledComposite().setAlwaysShowScrollBars(true);
            // tabbedPropertyComposite.getScrolledComposite().getHorizontalBar().setVisible(false);
            tabbedPropertyComposite.getScrolledComposite().setExpandHorizontal(true);
            tabbedPropertyComposite.removeListener(SWT.Resize, REFRESH_LISTENER);

            tabbedPropertyComposite.addListener(SWT.Resize, REFRESH_LISTENER);
        }
        super.aboutToBeShown();
    }

    /**
     * Getter for lastPropertyUsed.
     *
     * @return the lastPropertyUsed
     */
    public static DynamicTabbedPropertySection getLastPropertyUsed() {
        return lastPropertyUsed;
    }

    public void setComposite(Composite composite) {
        this.composite = composite;
    }

}
