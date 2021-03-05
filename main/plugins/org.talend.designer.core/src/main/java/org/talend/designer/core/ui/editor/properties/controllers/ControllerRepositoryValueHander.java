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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.ITDQPatternService;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.metadata.RepositoryObjectHelper;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * created by wchen on Aug 23, 2016 Detailled comment
 *
 */
public class ControllerRepositoryValueHander {

    private static Map<EParameterFieldType, ControllerRepositoryValueHander> repositoryValueHandlerMap = new HashMap<EParameterFieldType, ControllerRepositoryValueHander>();

    protected ConnectionItem lastItemUsed;

    private FileItem lastFileItemUsed; // hywang add for 6484

    private LinkRulesItem lastLinkItem;

    public String getDisplayNameFromValue(IElementParameter param, String value) {
        // to load informations from repository only if needed.

        int index = param.getIndexOfItemFromList(value);
        IElementParameter infoObjectParam = param.getElement().getElementParameter("INFO_OBJECT_TYPE"); //$NON-NLS-1$
        if (infoObjectParam != null && infoObjectParam.getValue() != null) {
            index = -1;
        }
        if (index == -1) {

            fastInitializeRepositoryNames(param);
            // if even after the initialize there is nothing, just return an empty string
            if (param.getListItemsDisplayName().length == 0) {
                return ""; //$NON-NLS-1$
            }
            index = param.getIndexOfItemFromList(value);
            if (index == -1) {
                return ""; //$NON-NLS-1$
            }
        }

        return param.getListItemsDisplayName()[index];
    }

    protected void fastInitializeRepositoryNames(IElementParameter param) {
        lastItemUsed = null;

        if (param.getValue() == null || param.getValue().equals("")) {
            return;
        }
        if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
            fastRepositoryUpdateProperty(param);
        } else if (param.getName().equals(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {
            fastRepositoryUpdateSchema(param);
        } else if (param.getName().equals(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
            fastRepositoryUpdateQuery(param);
        }
    }

    private void fastRepositoryUpdateProperty(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
            String linkedRepository = (String) param.getValue();
            // value stored is the id, so we can get this item directly
            Item item;
            String displayName = "";
            try {
                IRepositoryViewObject object = factory.getLastVersion(linkedRepository.split(" - ")[0]);
                if (object == null) {
                    return;
                }
                item = object.getProperty().getItem();
                // Assert.isTrue(item instanceof ConnectionItem);
                IESBService service = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                    service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                }
                if (service != null && ERepositoryObjectType.getItemType(item) == service.getServicesType()) {
                    lastItemUsed = (ConnectionItem) item;
                    displayName = "Service:" + service.getServiceLabel(item, linkedRepository);
                } else if (item instanceof ConnectionItem) {
                    lastItemUsed = (ConnectionItem) item;
                    displayName = RepositoryObjectHelper.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                            + lastItemUsed.getProperty().getLabel();
                }
                if (item instanceof FileItem) { // hywang add for 6484
                    lastFileItemUsed = (FileItem) item;
                    if (lastFileItemUsed instanceof RulesItem) {
                        displayName = "Rules:" //$NON-NLS-1$
                                + lastFileItemUsed.getProperty().getLabel();
                    }
                }
                if (item instanceof LinkRulesItem) {
                    lastLinkItem = (LinkRulesItem) item;
                    displayName = "Rules:" //$NON-NLS-1$
                            + lastLinkItem.getProperty().getLabel();

                }
                if(StringUtils.isEmpty(displayName)){
                    ITDQPatternService tdqService = null;
                    try {
                        tdqService = (ITDQPatternService) GlobalServiceRegister.getDefault().getService(ITDQPatternService.class);
                        if(tdqService!=null){
                            displayName = tdqService.getPatternDisplayName(item);
                        }
                    } catch (RuntimeException e) {
                        // nothing to do
                    }

                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

            param.setListItemsDisplayName(new String[] { displayName });
            param.setListItemsValue(new String[] { (String) param.getValue() });
        }

    }

    private void fastRepositoryUpdateSchema(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            String queryIdAndName = (String) param.getValue();
            String[] names = queryIdAndName.split(" - "); //$NON-NLS-1$
            if (names.length < 2) {
                return;
            }
            String linkedRepository = names[0];
            String tableName = null;
            if (names.length == 2) {
                tableName = names[1];
            } else if (names.length > 2) {
                tableName = queryIdAndName.substring(linkedRepository.length() + 3);
            }

            if (lastItemUsed != null) {
                if (!linkedRepository.equals(lastItemUsed.getProperty().getId())) {
                    lastItemUsed = null;
                }
            }
            if (lastItemUsed == null) {
                IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
                Item item;
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object == null) {
                        return;
                    }
                    item = object.getProperty().getItem();
                    Assert.isTrue(item instanceof ConnectionItem);
                    lastItemUsed = (ConnectionItem) item;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }

            // EList<MetadataTable> tableList = lastItemUsed.getConnection().getTables();
            // for (MetadataTable table )

            String displayName = RepositoryObjectHelper.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                    + lastItemUsed.getProperty().getLabel() + " - " + tableName; //$NON-NLS-1$
            IElementParameter infoObjectTypeParam = param.getElement().getElementParameter("INFO_OBJECT_TYPE"); //$NON-NLS-1$
            if (infoObjectTypeParam != null) {
                String innerIOType = (String) infoObjectTypeParam.getValue();
                if (innerIOType != null) {
                    displayName = displayName + " (" + innerIOType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            param.setListItemsDisplayName(new String[] { displayName });
            param.setListItemsValue(new String[] { (String) param.getValue() });
        }
    }

    private void fastRepositoryUpdateQuery(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            String queryIdAndName = (String) param.getValue();
            String[] names = queryIdAndName.split(" - "); //$NON-NLS-1$
            if (names.length < 2) {
                return;
            }
            String linkedRepository = names[0];
            String queryName = null;
            if (names.length == 2) {
                queryName = names[1];
            } else if (names.length > 2) {
                queryName = queryIdAndName.substring(linkedRepository.length() + 3);
            }
            if (lastItemUsed != null) {
                if (!linkedRepository.equals(lastItemUsed.getProperty().getId())) {
                    lastItemUsed = null;
                }
            }
            if (lastItemUsed == null) {
                IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
                Item item;
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object == null) {
                        return;
                    }
                    item = object.getProperty().getItem();
                    lastItemUsed = (ConnectionItem) item;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
            Assert.isTrue(lastItemUsed instanceof DatabaseConnectionItem);
            QueriesConnection queriesConnection = ((DatabaseConnection) lastItemUsed.getConnection()).getQueries();
            EList<Query> queries = queriesConnection.getQuery();

            String repositoryAliasName = RepositoryObjectHelper.getRepositoryAliasName(lastItemUsed);
            for (Query currentQuery : queries) {
                if (currentQuery.getLabel().equals(queryName)) {
                    String displayName = repositoryAliasName + ":" //$NON-NLS-1$
                            + lastItemUsed.getProperty().getLabel() + " - " + currentQuery.getLabel(); //$NON-NLS-1$

                    param.setListItemsDisplayName(new String[] { displayName });
                    param.setListItemsValue(new String[] { (String) param.getValue() });
                    /* query cache should be deleted ,bug 16969 */
                    // dynamicProperty.getRepositoryQueryStoreMap().clear();
                    // dynamicProperty.getRepositoryQueryStoreMap().put((String) param.getValue(), currentQuery);
                }
            }
        }
    }

    /**
     * Getter for repositoryValueHandlerMap.
     *
     * @return the repositoryValueHandlerMap
     */
    public static Map<EParameterFieldType, ControllerRepositoryValueHander> getRepositoryValueHandlerMap() {
        return repositoryValueHandlerMap;
    }
}
