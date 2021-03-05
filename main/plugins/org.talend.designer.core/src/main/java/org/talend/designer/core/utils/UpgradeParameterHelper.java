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
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.builder.connection.Connection;
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
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public final class UpgradeParameterHelper {

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
    public static boolean upgradeItem(Item item) {
        if (item == null) {
            return false;
        }
        boolean changed = false;
        if (item instanceof ProcessItem) {
            // job
            ProcessType process = ((ProcessItem) item).getProcess();
            if (process != null) {
                ParametersType parameters = process.getParameters();
                if (parameters != null) {
                    changed = upgradeMainParameters(parameters.getElementParameter());
                }
                changed = changed | upgradeNodes(process.getNode());
            }
        } else if (item instanceof JobletProcessItem) {
            // Joblet
            JobletProcess jobletProcess = ((JobletProcessItem) item).getJobletProcess();
            if (jobletProcess != null) {
                ParametersType parameters = jobletProcess.getParameters();
                if (parameters != null) {
                    changed = upgradeMainParameters(parameters.getElementParameter());
                }
                changed = changed | upgradeNodes(jobletProcess.getNode());
            }
        }
        return changed;
    }

    /**
     *
     * ggu Comment method "upgradeMainParameters".
     *
     * only process the job main parameters.
     */
    private static boolean upgradeMainParameters(List<ElementParameterType> elemParamList) {
        if (elemParamList == null || elemParamList.isEmpty()) {
            return false;
        }
        final String propertyType = EParameterName.PROPERTY_TYPE.getName();
        final String repositoryType = EParameterName.REPOSITORY_PROPERTY_TYPE.getName();

        boolean changed = false;

        for (ElementParameterType param : elemParamList) {
            final String name = param.getName();
            // REPOSITORY_PROPERTY_TYPE
            if (name.contains(repositoryType) && !hasParent(name)) {
                // job settings /extra
                if (JobSettingsConstants.getExtraParameterName(repositoryType).equals(name)) {
                    final String extraPropertyParameterName = JobSettingsConstants.getExtraParameterName(propertyType);
                    changed = upgradeRelatedParameters(param, repositoryType, extraPropertyParameterName, propertyType,
                            extraPropertyParameterName, elemParamList, null);

                } else
                // job settings /stats & logs
                if (repositoryType.equals(name)) {
                    changed = upgradeRelatedParameters(param, repositoryType, propertyType, null, propertyType, elemParamList,
                            null);

                }

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
    private static boolean upgradeNodes(List<NodeType> nodesList) {
        if (nodesList == null || nodesList.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (NodeType node : nodesList) {
            if (upgradeNodeParameters(node.getElementParameter())) {
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
    private static boolean upgradeNodeParameters(List<ElementParameterType> elemParamList) {
        if (elemParamList == null || elemParamList.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (ElementParameterType elemParam : elemParamList) {
            if (upgradeNodeParameter(elemParam, elemParamList)) {
                changed = true;
            }
        }
        return changed;
    }

    private static boolean upgradeNodeParameter(ElementParameterType elemParam, List<ElementParameterType> elemParamList) {
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

        // REPOSITORY_PROPERTY_TYPE
        if (name.contains(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
            parentName = PROPERTY;
            relatedName = EParameterName.PROPERTY_TYPE.getName();
            type = null;

        } else
        // PROCESS_TYPE_PROCESS
        if (name.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            parentName = PROCESS;
            relatedName = EParameterName.PROCESS_TYPE_CONTEXT.getName();
            type = ERepositoryObjectType.PROCESS;

        }
        // disable the changes done for the query & schema
        /*
         * else // REPOSITORY_QUERYSTORE_TYPE if (name.contains(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
         * parentName = QUERYSTORE; relatedName = EParameterName.QUERYSTORE_TYPE.getName(); type =
         * ERepositoryObjectType.METADATA_CON_QUERY; } else // REPOSITORY_SCHEMA_TYPE if
         * (name.contains(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) { parentName = SCHEMA; relatedName =
         * EParameterName.SCHEMA_TYPE.getName(); type = ERepositoryObjectType.METADATA_CON_TABLE; }
         */
        if (parentName != null) { // above
            if (hasParent(name)) {
                // only update value
                return updateParameterValue(elemParam, relatedName, parentName, elemParamList, type);
            } else {
                // no parent, upgrade the paramters
                return upgradeNodeRelatedParameters(elemParam, relatedName, parentName, elemParamList, type);
            }
        }
        return false;
    }

    private static boolean upgradeNodeRelatedParameters(ElementParameterType repositoryParam, final String typeParamName,
            final String parentName, List<ElementParameterType> elemParamList, ERepositoryObjectType type) {
        return upgradeRelatedParameters(repositoryParam, null, typeParamName, null, parentName, elemParamList, type);
    }

    /**
     *
     * ggu Comment method "upgradeRelatedParameters".
     *
     * "type =null" is only for property type parameter.
     */
    private static boolean upgradeRelatedParameters(ElementParameterType repositoryParam, String repositoryParamName,
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
            updateParameterValue(repositoryParam, typeParam, parentName, type);

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
    private static boolean updateParameterValue(ElementParameterType repositoryParam, final String typeParamName,
            final String parentName, List<ElementParameterType> elemParamList, ERepositoryObjectType type) {

        ElementParameterType typeParam = findParameter(typeParamName, parentName, elemParamList);

        return updateParameterValue(repositoryParam, typeParam, parentName, type);
    }

    /**
     *
     * ggu Comment method "updateParameterValue".
     *
     * update the related paramters value.
     */
    private static boolean updateParameterValue(ElementParameterType repositoryParam, ElementParameterType typeParam,
            final String parentName, ERepositoryObjectType type) {
        if (repositoryParam == null || parentName == null) {
            return false;
        }
        if (type == null) { // the property type is null and anothers
            return false;
        }
        if (hasParent(repositoryParam.getName(), parentName)) {
            String id = getValueId(repositoryParam.getValue());
            if (id != null) {
                if (type == ERepositoryObjectType.PROCESS) { // special process type
                    // id is label
                    String theId = getIdFormLabel(id, type);
                    if (theId != null) {
                        repositoryParam.setValue(theId);
                    } else {
                        repositoryParam.setValue(EMPTY);
                    }
                    return true;
                } else {
                    String label = getValueLabel(repositoryParam.getValue());
                    if (label != null) {
                        String newId = getChildIdFormParent(id, label, type);
                        if (newId != null) {
                            repositoryParam.setValue(newId);
                            return true;
                        }
                    }
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

    /*
     * get the id form value
     */
    private static String getValueId(final String value) {
        if (value == null) {
            return null;
        }
        if (hasChildValue(value)) {
            return value.split(SEPARATOR)[0];
        } else {
            return value;
        }
    }

    /*
     * get the child label form value
     */
    private static String getValueLabel(final String value) {
        if (value == null) {
            return null;
        }
        if (hasChildValue(value)) {
            return value.split(SEPARATOR)[1];
        } else {
            return null;
        }
    }

    /*
     * check the child from value
     */
    private static boolean hasChildValue(final String value) {
        if (value == null) {
            return false;
        }
        if (value.contains(SEPARATOR)) {
            return true;
        }
        return false;
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

    /**
     *
     * ggu Comment method "getChildIdFormParent".
     *
     *
     */
    private static String getChildIdFormParent(final String parentId, final String childName, final ERepositoryObjectType type) {
        if (parentId == null || childName == null || type == null) {
            return null;
        }
        final IProxyRepositoryFactory proxyRepositoryFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        try {
            final IRepositoryViewObject lastVersion = proxyRepositoryFactory.getLastVersion(parentId);
            if (lastVersion != null) {
                final Item item = lastVersion.getProperty().getItem();
                if (item != null) {
                    if (item instanceof ConnectionItem) {
                        return getChildIdFromParentConnection(((ConnectionItem) item).getConnection(), childName, type);

                    }
                }
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static String getChildIdFromParentConnection(final Connection connection, final String childName,
            final ERepositoryObjectType type) {
        if (connection == null || childName == null || type == null) {
            return null;
        }
        // query
        if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
            final QueriesConnection queryConn = connection.getQueries();
            if (queryConn != null) {
                final EList queries = queryConn.getQuery();
                if (queries != null) {
                    for (Query query : (List<Query>) queries) {
                        if (childName.equals(query.getLabel())) {
                            return query.getId();
                        }
                    }
                }

            }
        } else
        // schema
        if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            final Set tables = ConnectionHelper.getTables(connection);
            if (tables != null) {
                for (MetadataTable table : (Set<MetadataTable>) tables) {
                    if (childName.equals(table.getLabel())) {
                        return table.getId();
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * ggu Comment method "getIdFormLabel".
     *
     * get label of id by type.
     */
    private static String getIdFormLabel(final String label, ERepositoryObjectType type) {
        if (label == null || type == null) {
            return null;
        }
        final IProxyRepositoryFactory proxyRepositoryFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        try {
            List<IRepositoryViewObject> allRepositoryObject = proxyRepositoryFactory.getAll(type, true);
            for (IRepositoryViewObject repObject : allRepositoryObject) {
                Item item = repObject.getProperty().getItem();
                if (item != null && label.equals(item.getProperty().getLabel())) {
                    return item.getProperty().getId();
                }
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

}
