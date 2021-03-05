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
package org.talend.designer.core.utils;

import java.util.List;
import java.util.Set;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Temporary class done for a migration linked with the feature 3310. This class could be deleted later.
 */
public final class DowngradeParameterHelper {

    public static final String EMPTY = ""; //$NON-NLS-1$

    public static final String COLON = ":"; //$NON-NLS-1$

    public static final String SEPARATOR = " - "; //$NON-NLS-1$

    public static final String PROPERTY = "PROPERTY"; //$NON-NLS-1$

    public static final String QUERYSTORE = "QUERYSTORE"; //$NON-NLS-1$

    public static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$

    public static final String PROCESS = "PROCESS"; //$NON-NLS-1$

    /**
     *
     * ggu Comment method "upgradeItem".
     *
     * upgrade the item parameters.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static boolean downgradeItem(Item item) {
        if (item == null) {
            return false;
        }
        boolean changed = false;
        if (item instanceof ProcessItem) {
            // job
            ProcessType process = ((ProcessItem) item).getProcess();
            if (process != null) {
                changed = changed | downgradeNodes(process.getNode());
            }
        } else if (item instanceof JobletProcessItem) {
            // Joblet
            JobletProcess jobletProcess = ((JobletProcessItem) item).getJobletProcess();
            if (jobletProcess != null) {
                changed = changed | downgradeNodes(jobletProcess.getNode());
            }
        }
        return changed;
    }

    /**
     *
     * ggu Comment method "upgradeNodes".
     *
     * upgrade the nodes parameters.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static boolean downgradeNodes(List<NodeType> nodesList) {
        if (nodesList == null || nodesList.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (NodeType node : nodesList) {
            if (downgradeNodeParameters(node.getElementParameter())) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     *
     * ggu Comment method "upgradeParameters".
     *
     * process the node parameters.
     */
    private static boolean downgradeNodeParameters(List<ElementParameterType> elemParamList) {
        if (elemParamList == null || elemParamList.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (ElementParameterType elemParam : elemParamList) {
            if (downgradeNodeParameter(elemParam, elemParamList)) {
                changed = true;
            }
        }
        return changed;
    }

    private static boolean downgradeNodeParameter(ElementParameterType elemParam, List<ElementParameterType> elemParamList) {
        if (elemParam == null) {
            return false;
        }
        String name = elemParam.getName();
        if (name == null) {
            return false;
        }
        String parentName = null;
        String relatedName = null;
        ERepositoryObjectType type = null;

        // REPOSITORY_QUERYSTORE_TYPE
        if (name.contains(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
            parentName = QUERYSTORE;
            relatedName = EParameterName.QUERYSTORE_TYPE.getName();
            type = ERepositoryObjectType.METADATA_CON_QUERY;

        } else
        // REPOSITORY_SCHEMA_TYPE
        if (name.contains(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {
            parentName = SCHEMA;
            relatedName = EParameterName.SCHEMA_TYPE.getName();
            type = ERepositoryObjectType.METADATA_CON_TABLE;

        }
        if (parentName != null) { // above
            if (hasParent(name)) {
                // only update value
                return downgradeParameterValue(elemParam, relatedName, parentName, elemParamList, type);
            } else {
                // no parent, upgrade the paramters
                return downgradeNodeRelatedParameters(elemParam, relatedName, parentName, elemParamList, type);
            }
        }
        return false;
    }

    private static boolean downgradeNodeRelatedParameters(ElementParameterType repositoryParam, final String typeParamName,
            final String parentName, List<ElementParameterType> elemParamList, ERepositoryObjectType type) {
        return downgradeRelatedParameters(repositoryParam, null, typeParamName, null, parentName, elemParamList, type);
    }

    /**
     *
     * ggu Comment method "upgradeRelatedParameters".
     *
     * "type =null" is only for property type parameter.
     */
    private static boolean downgradeRelatedParameters(ElementParameterType repositoryParam, String repositoryParamName,
            final String oleTypeParamName, String newTypeParamName, final String parentName,
            List<ElementParameterType> elemParamList, ERepositoryObjectType type) {
        if (repositoryParam == null || oleTypeParamName == null || parentName == null || elemParamList == null
                || elemParamList.isEmpty()) {
            return false;
        }
        if (repositoryParamName == null) {
            repositoryParamName = repositoryParam.getName();
        }
        if (newTypeParamName == null) {
            newTypeParamName = oleTypeParamName;
        }

        // related paramter
        // such as PROPERTY_TYPE, QUERYSTORE_TYPE, SCHEMA_TYPE
        ElementParameterType typeParam = findParameter(oleTypeParamName, elemParamList);
        if (typeParam != null) {
            repositoryParam.setName(parentName + COLON + repositoryParamName);
            repositoryParam.setField(EParameterFieldType.TECHNICAL.getName());

            typeParam.setName(parentName + COLON + newTypeParamName);
            typeParam.setField(EParameterFieldType.TECHNICAL.getName());
            // update the value
            downgradeParameterValue(repositoryParam, typeParam, parentName, type);

            return true;
        }
        return false;
    }

    /**
     *
     * ggu Comment method "updateParameterValue".
     *
     * only update the value for existed parent
     */
    private static boolean downgradeParameterValue(ElementParameterType repositoryParam, final String typeParamName,
            final String parentName, List<ElementParameterType> elemParamList, ERepositoryObjectType type) {

        ElementParameterType typeParam = findParameter(typeParamName, parentName, elemParamList);

        return downgradeParameterValue(repositoryParam, typeParam, parentName, type);
    }

    /**
     *
     * ggu Comment method "updateParameterValue".
     *
     * update the related paramters value.
     */
    private static boolean downgradeParameterValue(ElementParameterType repositoryParam, ElementParameterType typeParam,
            final String parentName, ERepositoryObjectType type) {
        if (repositoryParam == null || parentName == null) {
            return false;
        }
        if (type == null) { // the property type is null and anothers
            return false;
        }
        if (hasParent(repositoryParam.getName(), parentName)) {
            String id = repositoryParam.getValue();
            if (id != null && !id.equals("")) { //$NON-NLS-1$
                String label = getOldLabelFromId(id, type);
                if (label != null) {
                    repositoryParam.setValue(label);
                    return true;
                }
            }
            repositoryParam.setValue(EMPTY);
            if (typeParam != null) {
                typeParam.setValue(EmfComponent.BUILTIN);
            }
            return true;
        }
        return false;
    }

    /**
     * DOC nrousseau Comment method "getOldLabelFromId".
     *
     * @param id
     * @param type
     * @return
     */
    private static String getOldLabelFromId(String id, ERepositoryObjectType type) {
        final IProxyRepositoryFactory proxyRepositoryFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();

        try {
            ERepositoryObjectType repositoryConnectionsNeeded;
            if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
                repositoryConnectionsNeeded = ERepositoryObjectType.METADATA_CONNECTIONS;
            } else {
                repositoryConnectionsNeeded = ERepositoryObjectType.METADATA;
            }

            List<IRepositoryViewObject> objects = proxyRepositoryFactory.getAll(repositoryConnectionsNeeded);
            ConnectionItem connectionItem = null;
            for (IRepositoryViewObject object : objects) {
                connectionItem = (ConnectionItem) object.getProperty().getItem();

                if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
                    QueriesConnection queries = connectionItem.getConnection().getQueries();
                    if (queries == null) {
                        continue;
                    }
                    for (Query query : (List<Query>) queries.getQuery()) {
                        if (query.getId().equals(id)) {
                            return connectionItem.getProperty().getId() + SEPARATOR + query.getLabel();
                        }
                    }
                } else if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                    for (MetadataTable table : (Set<MetadataTable>) ConnectionHelper.getTables(connectionItem.getConnection())) {
                        if (table.getId().equals(id)) {
                            return connectionItem.getProperty().getId() + SEPARATOR + table.getLabel();
                        }
                    }
                }

            }
        } catch (PersistenceException e) {
            return null;
        }
        return null;
    }

    /*
     * check the parent form name
     */
    private static boolean hasParent(final String name, final String parent) {
        if (name == null) {
            return false;
        }
        if (parent != null) {
            final String parentExp = parent + COLON;
            if (name.contains(parentExp)) {
                Perl5Matcher matcher = new Perl5Matcher();
                Perl5Compiler compiler = new Perl5Compiler();
                Pattern pattern;

                try {
                    pattern = compiler.compile("\\b(" + parentExp + ")\\b"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (matcher.contains(name, pattern)) {
                        return true;
                    }
                } catch (MalformedPatternException e) {
                    //
                }
            }
        }
        return false;
    }

    private static boolean hasParent(final String name) {
        if (name == null) {
            return false;
        }
        if (name.contains(COLON)) {
            return true;
        }
        return false;
    }

    /**
     *
     * ggu Comment method "findParameter".
     *
     * find the name of parameter for parent name.
     */
    private static ElementParameterType findParameter(String paramName, String parentName,
            List<ElementParameterType> elemParamList) {
        if (parentName == null) {
            parentName = EMPTY;
        } else {
            parentName = parentName + COLON;
        }
        if (paramName != null) {
            paramName = parentName + paramName;
        }
        return findParameter(paramName, elemParamList);

    }

    /*
     * find the name of parameter.
     */
    private static ElementParameterType findParameter(final String paramName, List<ElementParameterType> elemParamList) {
        if (paramName == null || elemParamList == null || elemParamList.isEmpty()) {
            return null;
        }
        for (ElementParameterType param : elemParamList) {
            if (paramName.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }
}
