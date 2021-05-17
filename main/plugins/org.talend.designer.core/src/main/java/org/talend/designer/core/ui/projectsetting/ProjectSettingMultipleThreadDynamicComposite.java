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
package org.talend.designer.core.ui.projectsetting;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.repository.UpdateRepositoryUtils;

/**
 * cli class global comment. Detailled comment
 */
public class ProjectSettingMultipleThreadDynamicComposite extends MultipleThreadDynamicComposite {

    private CommandStack cs;

    private boolean isCommandExcute;

    private String repositoryPropertyName;

    private boolean connectionUpdated;

    public ProjectSettingMultipleThreadDynamicComposite(Composite parentComposite, int styles, EComponentCategory section,
            Element element, boolean isCompactView, String repositoryPropertyName) {
        super(parentComposite, styles, section, element, isCompactView);
        this.repositoryPropertyName = repositoryPropertyName;
    }

    @Override
    public CommandStack getCommandStack() {
        if (cs == null) { // fixed bug 12476
            cs = new CommandStack();
            cs.addCommandStackEventListener(new CommandStackEventListener() {

                public void stackChanged(CommandStackEvent event) {
                    if (event.getDetail() == CommandStack.POST_EXECUTE) {
                        isCommandExcute = true;
                        // when show connection param ,update it if needed
                        updateConnectionFromRepository();
                    }
                }
            });
        }
        return cs;
    }

    public boolean isCommandExcute() {
        return this.isCommandExcute;
    }

    private void updateConnectionFromRepository() {
        if (repositoryPropertyName == null || repositoryPropertyName.split(":").length != 2 || connectionUpdated) {
            return;
        }
        String[] split = repositoryPropertyName.split(":");
        String parentParamName = split[0];

        Element elementParams = elem;
        IElementParameter elementParameter = elementParams.getElementParameter(parentParamName);
        if (elementParameter != null && elementParameter.isShow(elem.getElementParameters())
                && elementParameter.getChildParameters() != null) {
            if (elementParameter.getChildParameters().get("PROPERTY_TYPE") != null
                    && !EmfComponent.BUILTIN.equals(elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue())) {
                DatabaseConnection connection = null;
                String id = (String) elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue();
                // bug 0018192
                String propertyType = (String) elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue();
                ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
                IRepositoryViewObject lastVersion = null;
                boolean connectionChanged = false;
                if (null != id && !"".equals(id)) {
                    try {
                        lastVersion = proxyRepositoryFactory.getLastVersion(id);
                        if (null == lastVersion && propertyType.equals(EmfComponent.REPOSITORY)) {
                            List<ConnectionItem> connectionItems = proxyRepositoryFactory.getMetadataConnectionsItem();
                            for (ConnectionItem cItem : connectionItems) {
                                if (cItem instanceof DatabaseConnectionItem) {
                                    if (isSupportDatabaseType(cItem)) {
                                        id = cItem.getProperty().getId();
                                        lastVersion = proxyRepositoryFactory.getLastVersion(id);
                                        elem.setPropertyValue("REPOSITORY_PROPERTY_TYPE", id);
                                        connectionChanged = true;
                                        break;
                                    }
                                }
                            }

                            if (connectionItems.isEmpty() || null == lastVersion) {
                                elem.setPropertyValue("REPOSITORY_PROPERTY_TYPE", "");
                                ChangeValuesFromRepository changeValuesFromRepository1 = new ChangeValuesFromRepository(elem,
                                        null, parentParamName + ":" + "PROPERTY_TYPE", EmfComponent.BUILTIN);
                                changeValuesFromRepository1.execute();
                            }

                        }
                    } catch (PersistenceException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        List<ConnectionItem> connectionItems = proxyRepositoryFactory.getMetadataConnectionsItem();
                        for (ConnectionItem cItem : connectionItems) {
                            if (cItem instanceof DatabaseConnectionItem) {
                                if (isSupportDatabaseType(cItem)) {
                                    lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(cItem.getProperty().getId());
                                    id = cItem.getProperty().getId();
                                    lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(id);
                                    elem.setPropertyValue("REPOSITORY_PROPERTY_TYPE", id);
                                    connectionChanged = true;
                                    break;
                                }
                            }
                        }
                    } catch (PersistenceException e) {
                        e.printStackTrace();
                    }
                }
                if (lastVersion != null && lastVersion.getProperty() != null) {
                    Item item = lastVersion.getProperty().getItem();
                    if (item instanceof DatabaseConnectionItem) {
                        DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                        connection = (DatabaseConnection) dbItem.getConnection();
                    }
                }

                if (connection != null) {
                    boolean sameValues = true;
                    for (IElementParameter param : elementParams.getElementParameters()) {
                        String repositoryValue = param.getRepositoryValue();
                        if (param.isShow(elementParams.getElementParameters()) && repositoryValue != null
                                && !param.getName().equals("PROPERTY_TYPE")) {
                            Object repValue = RepositoryToComponentProperty.getValue(connection, repositoryValue, null);
                            if (repValue == null) {
                                continue;
                            }
                            if (repositoryValue.equals(UpdatesConstants.TYPE)) { // datebase type
                                boolean found = false;
                                String[] list = param.getListRepositoryItems();
                                for (int i = 0; (i < list.length) && (!found); i++) {
                                    if (repValue.equals(list[i])) {
                                        found = true;
                                    }

                                }
                                if (!found) {
                                    sameValues = false;
                                    break;
                                }

                            } else {
                                // check the value
                                if (!param.getValue().equals(repValue)) {
                                    sameValues = false;
                                    break;
                                }
                            }

                        }
                    }
                    if (!sameValues || connectionChanged) {
                        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, connection,
                                repositoryPropertyName, id);
                        changeValuesFromRepository.execute();
                        connectionUpdated = true;
                    }

                }
                // bug 0018192
                // else {
                // change to build in
                // ChangeValuesFromRepository changeValuesFromRepository1 = new ChangeValuesFromRepository(elem,
                // null,
                // parentParamName + ":" + "PROPERTY_TYPE", EmfComponent.BUILTIN);
                // changeValuesFromRepository1.execute();
                // }

            }

        }

    }

    private boolean isSupportDatabaseType(ConnectionItem connectionItem) {
        Connection connection = connectionItem.getConnection();
        DatabaseConnection dbConnection = null;
        if (connection instanceof DatabaseConnection) {
            dbConnection = (DatabaseConnection) connection;
        }
        if (dbConnection == null || StringUtils.isBlank(dbConnection.getProductId())) {
            return false;
        }
        // JDBC databaseType with different productId like DeltaLake/SingleStore..
        String productId = dbConnection.getProductId();
        String[] supportDatabaseTypeNames = StatsAndLogsConstants.SUPPORT_PRODUCT_NAMES;
        boolean find = false;
        if ("ORACLE".equals(productId)) {
            find = Arrays.stream(StatsAndLogsConstants.DISPLAY_DBNAMES[1])
                    .anyMatch(typeName -> typeName.equals(connectionItem.getTypeName()));
        } else {
            find = Arrays.stream(supportDatabaseTypeNames).anyMatch(name -> name.equals(productId));
        }
        return find;
    }

}
