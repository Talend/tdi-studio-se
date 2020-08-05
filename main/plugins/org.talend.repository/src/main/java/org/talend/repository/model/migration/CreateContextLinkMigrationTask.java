// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.link.ContextLinkService;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

public class CreateContextLinkMigrationTask extends AbstractItemMigrationTask {

    private static final Logger LOGGER = Logger.getLogger(CreateContextLinkMigrationTask.class);

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess2());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfTestContainer());
        toReturn.addAll(getAllMetaDataType());
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        boolean modified = false;
        try {
            List<ContextType> contextTypeList = ContextUtils.getAllContextType(item);
            if (contextTypeList != null && contextTypeList.size() > 0) {
                Map<String, Item> idToItemMap = new HashMap<String, Item>();
                for (ContextType contextType : contextTypeList) {
                    for (Object obj : contextType.getContextParameter()) {
                        if (obj instanceof ContextParameterType) {
                            ContextParameterType paramType = (ContextParameterType) obj;
                            if (!ContextUtils.isBuildInParameter(paramType)) {
                                String repoId = paramType.getRepositoryContextId();
                                Item repoItem = idToItemMap.get(repoId);
                                if (repoItem == null) {
                                    repoItem = ContextUtils.getRepositoryContextItemById(repoId);
                                    idToItemMap.put(repoId, repoItem);
                                }
                                if (repoItem != null) {
                                    if (!(repoItem instanceof ContextItem)) {
                                        ContextType repoContextType = ContextUtils.getContextTypeByName(repoItem,
                                                contextType.getName());
                                        if (repoContextType != null) {
                                            ContextParameterType repoParamType = ContextUtils
                                                    .getContextParameterTypeByName(repoContextType, paramType.getName());
                                            if (repoParamType != null) {
                                                paramType.setInternalId(repoParamType.getInternalId());
                                            } else {
                                                LOGGER.warn("Can't find context repository parameter type repo:" + repoId
                                                        + " parameter name:" + paramType.getName());
                                            }
                                        } else {
                                            LOGGER.warn("Can't find context repository context type repo:" + repoId
                                                    + " context type name:" + contextType.getName());
                                        }
                                    }
                                } else {
                                    LOGGER.warn("Can't find context repository item:" + repoId);
                                }
                            } else {
                                if (StringUtils.isEmpty(paramType.getInternalId())) {
                                    paramType.setInternalId(factory.getNextId());
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }
            boolean hasLinkFile = ContextLinkService.getInstance().saveContextLink(item);
            modified = modified || hasLinkFile;
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
            return ExecutionResult.FAILURE;
        }

        if (modified) {
            try {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private List<ERepositoryObjectType> getAllMetaDataType() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>();
        ERepositoryObjectType[] allTypes = (ERepositoryObjectType[]) ERepositoryObjectType.values();
        for (ERepositoryObjectType object : allTypes) {
            if (object.isChildTypeOf(ERepositoryObjectType.METADATA)) {
                list.add(object);
            }
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 04, 02, 12, 0, 0);
        return gc.getTime();
    }
}
