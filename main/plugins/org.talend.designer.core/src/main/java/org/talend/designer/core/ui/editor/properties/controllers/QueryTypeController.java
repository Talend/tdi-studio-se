// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeQueryCommand;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class QueryTypeController extends AbstractRepositoryController {

    private static final String GUESS_QUERY_NAME = "Guess Query"; //$NON-NLS-1$

    /**
     * DOC nrousseau QueryTypeController constructor comment.
     * 
     * @param dp
     */
    public QueryTypeController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createControl(org.eclipse
     * .swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);

        IElementParameter queryStoreTypeParameter = param.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName());
        if (queryStoreTypeParameter != null) {
            String queryStoreType = (String) queryStoreTypeParameter.getValue();
            if (queryStoreType != null && queryStoreType.equals(EmfComponent.BUILTIN)) {
                lastControlUsed = addGuessQueryButton(subComposite, param, lastControlUsed, numInRow, top);
            }
        }

        return lastControlUsed;
    }

    private Control addGuessQueryButton(Composite subComposite, IElementParameter param, Control lastControl, int numInRow,
            int top) {
        final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return new Button(parent, style);
            }
        });
        Button guessQueryButton = null;
        Control buttonControl = dField1.getLayoutControl();
        guessQueryButton = (Button) dField1.getControl();
        guessQueryButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        buttonControl.setBackground(subComposite.getBackground());
        guessQueryButton.setEnabled(!param.isReadOnly() || !ExtractMetaDataUtils.getInstance().haveLoadMetadataNode());
        guessQueryButton.setData(NAME, GUESS_QUERY_NAME);
        guessQueryButton.setData(PARAMETER_NAME, param.getName());
        guessQueryButton.setText(GUESS_QUERY_NAME);

        FormData data1 = new FormData();
        data1.left = new FormAttachment(lastControl, 0);
        data1.top = new FormAttachment(0, top);
        data1.height = STANDARD_HEIGHT + 2;

        buttonControl.setLayoutData(data1);
        guessQueryButton.addSelectionListener(listenerSelection);
        return buttonControl;
    }

    private IElementParameter getQueryTextElementParameter(IElement elem) {
        for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                return param;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createButtonCommand(org
     * .eclipse.swt.widgets.Button)
     */
    @Override
    protected Command createButtonCommand(Button button) {
        if (button.getData(NAME).equals(GUESS_QUERY_NAME)) {
            return getGuessQueryCommand();
        }
        if (button.getData(NAME).equals(REPOSITORY_CHOICE)) {
            RepositoryReviewDialog dialog = new RepositoryReviewDialog(button.getShell(),
                    ERepositoryObjectType.METADATA_CON_QUERY, null);
            if (dialog.open() == RepositoryReviewDialog.OK) {
                RepositoryNode node = dialog.getResult();
                while (node.getObject().getProperty().getItem() == null
                        || (!(node.getObject().getProperty().getItem() instanceof ConnectionItem))) {
                    node = node.getParent();
                }
                String id = node.getObject().getProperty().getId();
                String name = dialog.getResult().getObject().getLabel();
                String paramName = (String) button.getData(PARAMETER_NAME);
                String value = id + " - " + name; //$NON-NLS-1$

                Query query = MetadataToolHelper.getQueryFromRepository(value);
                if (query != null) {
                    IElementParameter queryText = getQueryTextElementParameter(elem);
                    if (queryText != null) {
                        return new RepositoryChangeQueryCommand(elem, query, EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(),
                                value);
                    }
                }

            }
        }
        return null;
    }

    /**
     * DOC nrousseau Comment method "getGuessQueryCommand".
     * 
     * @return
     */
    private QueryGuessCommand getGuessQueryCommand() {
        // Map<String, IMetadataTable> repositoryTableMap = null;
        IMetadataTable newRepositoryMetadata = null;
        String realTableName = null;
        String realTableId = null;
        String schemaName = "";

        // Only for getting the real table name.
        if (elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName()).equals(EmfComponent.REPOSITORY)) {

            IElementParameter repositorySchemaTypeParameter = elem.getElementParameter(EParameterName.REPOSITORY_SCHEMA_TYPE
                    .getName());

            if (repositorySchemaTypeParameter != null) {
                final Object value = repositorySchemaTypeParameter.getValue();
                if (elem instanceof Node) {
                    /* value can be devided means the value like "connectionid - label" */
                    String[] keySplitValues = value.toString().split(" - ");
                    if (keySplitValues.length > 1) {

                        String connectionId = value.toString().split(" - ")[0];
                        String tableLabel = value.toString().split(" - ")[1];
                        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        Item item = null;
                        try {
                            IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                            if (repobj != null) {
                                Property property = repobj.getProperty();
                                if (property != null) {
                                    item = property.getItem();
                                }
                            }
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                        if (item != null && item instanceof ConnectionItem) {
                            Connection connection = ((ConnectionItem) item).getConnection();
                            for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                                    .getTables(connection)) {
                                // bug 20365
                                if (table.getLabel().equals(tableLabel)) {
                                    IMetadataTable repositoryMetadata = ConvertionHelper.convert(table);
                                    realTableName = repositoryMetadata.getTableName();
                                    realTableId = repositoryMetadata.getId();
                                    // if (table.eContainer() != null && table.eContainer() instanceof SchemaImpl) {
                                    // SchemaImpl schemaImpl = (SchemaImpl) table.eContainer();
                                    // schemaName = schemaImpl.getName();
                                    // dynamicProperty.getTableIdAndDbSchemaMap().put(realTableId, schemaName);
                                    // }
                                    break;
                                }
                            }
                        }
                    }
                    // if (repositoryTableMap.containsKey(value)) {
                    // IMetadataTable repositoryMetadata = repositoryTableMap.get(value);
                    // realTableName = repositoryMetadata.getTableName();
                    // realTableId = repositoryMetadata.getId();
                    // }
                }
            }
            // }
            // }
        } // Ends

        Connection repositoryConnection = null;
        boolean useExisting = false;
        IElementParameter elementParameter = elem.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.name());
        if (elem instanceof Node) {
            IProcess process = ((Node) elem).getProcess();
            if (elementParameter != null && Boolean.valueOf(String.valueOf(elementParameter.getValue()))) {
                String connName = (String) elem.getPropertyValue("CONNECTION");
                for (INode node : process.getGraphicalNodes()) {
                    if (node.getElementName().equals(connName)) {
                        useExisting = true;
                        final Object propertyValue = node.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                        if (propertyValue != null) {
                            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                            Item item = null;
                            try {
                                IRepositoryViewObject repobj = factory.getLastVersion(propertyValue.toString());
                                if (repobj != null) {
                                    Property property = repobj.getProperty();
                                    if (property != null) {
                                        item = property.getItem();
                                    }
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                            if (item != null && item instanceof ConnectionItem) {
                                repositoryConnection = ((ConnectionItem) item).getConnection();
                            } else {
                                initConnectionParameters();
                                repositoryConnection = TracesConnectionUtils.createConnection(connParameters);

                            }
                        }
                        break;
                    }
                }
            }
        }
        if (!useExisting && elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName()).equals(EmfComponent.REPOSITORY)) {
            final Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
            if (propertyValue != null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                Item item = null;
                try {
                    IRepositoryViewObject repobj = factory.getLastVersion(propertyValue.toString());
                    if (repobj != null) {
                        Property property = repobj.getProperty();
                        if (property != null) {
                            item = property.getItem();
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                if (item != null && item instanceof ConnectionItem) {
                    repositoryConnection = ((ConnectionItem) item).getConnection();
                }
            }
        } else {
            initConnectionParameters();
            repositoryConnection = TracesConnectionUtils.createConnection(connParameters);
        }

        QueryGuessCommand cmd = null;
        INode node = null;
        if (elem instanceof INode) {
            node = (INode) elem;
        } else { // else instanceof Connection
            node = ((IConnection) elem).getSource();
        }

        List<IMetadataTable> metadataList = node.getMetadataList();
        newRepositoryMetadata = metadataList.get(0);
        // for tInformixRow
        if (newRepositoryMetadata.getListColumns().size() == 0 && metadataList.size() > 1) {
            newRepositoryMetadata = metadataList.get(1);
        }

        if (newRepositoryMetadata == null) {
            String schemaSelected = (String) node.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            if (schemaSelected != null) {
                // repositoryMetadata = repositoryTableMap.get(schemaSelected);
            } else if (newRepositoryMetadata == null) {
                MessageDialog
                        .openWarning(
                                new Shell(),
                                Messages.getString("QueryTypeController.alert"), Messages.getString("QueryTypeController.nothingToGuess")); //$NON-NLS-1$ //$NON-NLS-2$
                return cmd;
            }
        }
        cmd = new QueryGuessCommand(node, newRepositoryMetadata, repositoryConnection);

        cmd.setMaps(dynamicProperty.getTableIdAndDbTypeMap(), dynamicProperty.getTableIdAndDbSchemaMap(), null);
        String type = getValueFromRepositoryName("TYPE"); //$NON-NLS-1$
        if ("Oracle".equalsIgnoreCase(type)) {
            type = EDatabaseTypeName.ORACLEFORSID.getDisplayName();
        }
        cmd.setParameters(realTableId, realTableName, type);
        return cmd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createComboCommand(org
     * .eclipse.swt.custom.CCombo)
     */
    @Override
    protected Command createComboCommand(CCombo combo) {
        String paramName = (String) combo.getData(PARAMETER_NAME);

        IElementParameter param = elem.getElementParameter(paramName);
        IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        String name = param.getName();
        String value = combo.getText();

        for (int j = 0; j < param.getListItemsValue().length; j++) {
            if (combo.getText().equals(param.getListItemsDisplayName()[j])) {
                value = (String) param.getListItemsValue()[j];
            }
        }
        if (name.equals(EParameterName.QUERYSTORE_TYPE.getName())) {
            if (elem instanceof Node) {
                String querySelected;
                Query repositoryQuery = null;
                // Map<String, Query> repositoryQueryStoreMap = this.dynamicProperty.getRepositoryQueryStoreMap();
                IElementParameter repositoryParam = param.getParentParameter().getChildParameters()
                        .get(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
                querySelected = (String) repositoryParam.getValue();

                /* value can be devided means the value like "connectionid - label" */
                String[] keySplitValues = querySelected.toString().split(" - ");
                if (keySplitValues.length > 1) {

                    String connectionId = querySelected.split(" - ")[0];
                    String queryLabel = querySelected.split(" - ")[1];
                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    Item item = null;
                    try {
                        IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                        if (repobj != null) {
                            Property property = repobj.getProperty();
                            if (property != null) {
                                item = property.getItem();
                            }
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                    if (item != null && item instanceof DatabaseConnectionItem) {
                        Connection conn = ((DatabaseConnectionItem) item).getConnection();
                        if (conn instanceof DatabaseConnection) {
                            DatabaseConnection dbconn = (DatabaseConnection) conn;
                            QueriesConnection queryConnection = dbconn.getQueries();
                            for (Query query : queryConnection.getQuery()) {
                                if (query.getLabel().equals(queryLabel)) {
                                    repositoryQuery = query;
                                    break;
                                }
                            }
                        }
                    }
                }

                // if (repositoryQueryStoreMap.containsKey(querySelected)) {
                // repositoryQuery = repositoryQueryStoreMap.get(querySelected);
                // }
                /*
                 * else if (dynamicProperty.getRepositoryQueryStoreMap().size() > 0) { repositoryQuery = (Query)
                 * dynamicProperty.getRepositoryQueryStoreMap().values().toArray()[0]; }
                 */

                if (switchParam != null) {
                    switchParam.setValue(Boolean.FALSE);
                }

                if (repositoryQuery != null) {
                    return new RepositoryChangeQueryCommand(elem, repositoryQuery, name, value);
                } else {
                    return new PropertyChangeCommand(elem, name, value);
                }

            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#getRepositoryChoiceParamName
     * ()
     */
    @Override
    protected String getRepositoryChoiceParamName() {
        return EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#getRepositoryTypeParamName
     * ()
     */
    @Override
    protected String getRepositoryTypeParamName() {
        return EParameterName.QUERYSTORE_TYPE.getName();
    }

    // @Override
    // protected String getDisplayNameFromValue(IElementParameter param, String value) {
    // super.getDisplayNameFromValue(param, value);
    // if (param == null || value == null || value.equals("")) { //$NON-NLS-1$
    // return null;
    // }
    // if (!param.getName().equals(getRepositoryChoiceParamName())) {
    // return null;
    // }
    // Item item = param.getLinkedRepositoryItem();
    // Query query = null;
    // if (item != null) {
    // // item not match
    // query = UpdateRepositoryUtils.getQueryById(item, value);
    // }
    // if (item == null || query == null) {
    // // research
    // item = UpdateRepositoryUtils.getConnectionItemByChildId(dynamicProperty.getRepositoryConnectionItemMap(), value);
    // if (item != null) {
    // query = UpdateRepositoryUtils.getQueryById(item, value);
    // }
    // }
    //
    // if (query != null && item != null && item instanceof ConnectionItem) {
    // return dynamicProperty.getRepositoryAliasName((ConnectionItem) item) + ":" //$NON-NLS-1$
    // + item.getProperty().getLabel() + " - " + query.getLabel(); //$NON-NLS-1$
    // }
    // return null;
    // }

}
