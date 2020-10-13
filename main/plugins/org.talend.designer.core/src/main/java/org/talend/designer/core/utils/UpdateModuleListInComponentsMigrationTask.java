// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.migration.IMigrationTask.ExecutionResult;
import org.talend.repository.model.migration.EncryptPasswordInComponentsMigrationTask.FakeNode;

/**
 * created by bhe on Oct 13, 2020 Detailled comment
 *
 */
public class UpdateModuleListInComponentsMigrationTask extends AbstractItemMigrationTask {

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.addAll(getAllMetaDataType());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess2());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfTestContainer());
        toReturn.add(ERepositoryObjectType.JDBC);
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
            if (item instanceof ConnectionItem) {
                modified = modified || updateConnectionItem((ConnectionItem) item);
            } else if (item instanceof ProcessItem) {
                ProcessItem processItem = (ProcessItem) item;
                modified = modified || updateProcessItem(item, processItem.getProcess());
            } else if (item instanceof JobletProcessItem) {
                JobletProcessItem jobletItem = (JobletProcessItem) item;
                modified = modified || updateProcessItem(item, jobletItem.getJobletProcess());
            }
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
            return ExecutionResult.FAILURE;
        }

        if (modified) {
            try {
                factory.save(item, true);
                // regenerate poms for affected job
                CorePlugin.getDefault().getRunProcessService().generatePom(item);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean updateConnectionItem(ConnectionItem connectionItem) throws Exception {
        boolean modified = false;
        if (connectionItem != null && connectionItem.getConnection() != null && !connectionItem.getConnection().isContextMode()) {
            Connection connection = connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                modified = updateDatabaseConnection((DatabaseConnection) connection);
            }
        }
        return modified;
    }

    protected boolean updateDatabaseConnection(DatabaseConnection dbConnection) throws Exception {
        String driverJar = dbConnection.getDriverJarPath();
        if (driverJar != null) {
            String uri = getMavenUriForJar(driverJar);
            dbConnection.setDriverJarPath(uri);
            return true;
        }
        return false;
    }

    private boolean updateProcessItem(Item item, ProcessType processType) throws Exception {
        EmfHelper.visitChilds(processType);

        boolean modified = false;

        // job settings
        if (checkJobsettintsParameter(item, processType)) {
            modified = true;
        }

        // nodes parameters
        if (checkNodes(item, processType)) {
            modified = true;
        }
        return modified;
    }

    protected boolean checkJobsettintsParameter(Item item, ProcessType processType) throws Exception {
        boolean modified = false;

        ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            for (Object p : parameters.getElementParameter()) {
                if (p instanceof ElementParameterType) {
                    ElementParameterType param = (ElementParameterType) p;
                    // variable name used for Stat&Logs
                    modified = updateParam(param);
                }
            }
        }
        return modified;
    }

    protected boolean checkNodesFromEmf(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        for (Object nodeObject : processType.getNode()) {
            NodeType nodeType = (NodeType) nodeObject;
            if (nodeType.getComponentName().equals("cConfig")) {
                return false;
            }
            for (Object paramObjectType : nodeType.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObjectType;
                modified = modified || updateParam(param);
            }
        }
        return modified;
    }

    protected boolean checkNodes(Item item, ProcessType processType) throws Exception {
        boolean modified = checkNodesFromEmf(item, processType);

        if (!modified) {
            // some versions of the job doesn't have any field type saved in the job, so we will check from the existing
            // component field type
            ComponentCategory category = ComponentCategory.getComponentCategoryFromItem(item);
            for (Object nodeObjectType : processType.getNode()) {
                NodeType nodeType = (NodeType) nodeObjectType;
                if (nodeType.getComponentName().equals("cConfig")) {
                    return false;
                }
                IComponent component = ComponentsFactoryProvider.getInstance().get(nodeType.getComponentName(),
                        category.getName());
                if (component == null) {
                    continue;
                }
                FakeNode fNode = new FakeNode(component);
                for (Object paramObjectType : nodeType.getElementParameter()) {
                    ElementParameterType param = (ElementParameterType) paramObjectType;
                    IElementParameter paramFromEmf = fNode.getElementParameter(param.getName());
                    if (paramFromEmf != null) {
                        modified = modified || updateParam(param);
                    }
                }
            }
        }
        return modified;
    }

    private List<ERepositoryObjectType> getAllMetaDataType() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>();
        list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return list;
    }

    private static boolean updateParam(ElementParameterType param) {
        boolean modified = false;
        if (param.getField() != null) {
            if (param.getField().equals(EParameterFieldType.MODULE_LIST.name()) && param.getValue() != null) {
                String jarUri = getMavenUriForJar(param.getValue());
                param.setValue(jarUri);
                modified = true;
            }

            if ("DRIVER_JAR".equals(param.getName()) && param.getField().equals(EParameterFieldType.TABLE.name())
                    && param.getElementValue() != null) {

                List<ElementValueType> newListValue = new ArrayList();
                EList<?> elementValues = param.getElementValue();
                for (Object ev : elementValues) {
                    ElementValueType evt = (ElementValueType) ev;
                    String jarUri = getMavenUriForJar(evt.getValue());
                    evt.setValue(jarUri);
                    newListValue.add(evt);
                    modified = true;
                }

                if (modified) {
                    param.getElementValue().clear();
                    param.getElementValue().addAll(newListValue);
                }
            }
        }
        return modified;
    }

    public static String getMavenUriForJar(String jarName) {
        jarName = TalendTextUtils.removeQuotes(jarName);
        if (!StringUtils.isEmpty(jarName) && !jarName.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            ModuleNeeded mod = new ModuleNeeded(null, jarName, null, true);
            if (!StringUtils.isEmpty(mod.getCustomMavenUri())) {
                return mod.getCustomMavenUri();
            }
            return mod.getMavenUri();
        }
        return jarName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 10, 13, 12, 0, 0);
        return gc.getTime();
    }
}
