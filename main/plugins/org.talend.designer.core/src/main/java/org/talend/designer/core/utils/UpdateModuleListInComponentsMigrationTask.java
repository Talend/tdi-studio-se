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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.utils.Hex;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
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
                if (updateConnectionItem((ConnectionItem) item)) {
                    modified = true;
                }
            } else if (item instanceof ProcessItem) {
                ProcessItem processItem = (ProcessItem) item;
                if (updateProcessItem(item, processItem.getProcess())) {
                    modified = true;
                }
            } else if (item instanceof JobletProcessItem) {
                JobletProcessItem jobletItem = (JobletProcessItem) item;
                if (updateProcessItem(item, jobletItem.getJobletProcess())) {
                    modified = true;
                }
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

    @Override
    public ExecutionResult execute(Project project) {
        ExecutionResult result = super.execute(project);
        if (result == ExecutionResult.FAILURE) {
            return result;
        }

        result = this.migrateProjectSettings(project);

        return result;
    }

    @Override
    public ExecutionResult execute(Project project, boolean doSave) {
        ExecutionResult result = super.execute(project, doSave);
        if (result == ExecutionResult.FAILURE) {
            return result;
        }

        result = this.migrateProjectSettings(project);

        return result;
    }

    @Override
    public ExecutionResult execute(Project project, Item item) {

        ExecutionResult result = super.execute(project, item);
        if (result == ExecutionResult.FAILURE) {
            return result;
        }
        result = migrateProjectSettings(project);
        return result;
    }

    protected ExecutionResult migrateProjectSettings(Project project) {
        boolean modified = false;
        org.talend.core.model.properties.Project emfProject = project.getEmfProject();
        StatAndLogsSettings statAndLogs = emfProject.getStatAndLogsSettings();
        if (statAndLogs != null && statAndLogs.getParameters() != null) {
            ParametersType parameters = statAndLogs.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    if (updateParam(parameterType, null)) {
                        modified = true;
                    }
                }
            }
        }

        ImplicitContextSettings ctxSettings = emfProject.getImplicitContextSettings();
        if (ctxSettings != null && ctxSettings.getParameters() != null) {
            ParametersType parameters = ctxSettings.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    if (updateParam(parameterType, null)) {
                        modified = true;
                    }
                }
            }
        }

        if (modified) {
            try {
                factory.saveProject(project);
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
            List ctxs = null;
            ContextItem contextItem = ContextUtils.getContextItemById2(dbConnection.getContextId());
            if (contextItem != null) {
                ctxs = contextItem.getContext();
            }
            String[] jars = driverJar.split(";");
            StringBuffer sb = new StringBuffer();
            for (String jar : jars) {
                String uri = getMavenUriForJar(jar, ctxs);
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(uri);
            }

            dbConnection.setDriverJarPath(sb.toString());
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
                    if (updateParam(param, processType)) {
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    protected boolean checkNodesFromEmf(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        for (Object nodeObject : processType.getNode()) {
            NodeType nodeType = (NodeType) nodeObject;
            boolean isConfig = nodeType.getComponentName().equals("cConfig");
            for (Object paramObjectType : nodeType.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObjectType;
                if (isConfig ? updateParamForcConfig(param, processType) : updateParam(param, processType)) {
                    modified = true;
                }
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
                boolean isConfig = nodeType.getComponentName().equals("cConfig");
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
                        if (isConfig ? updateParamForcConfig(param, processType) : updateParam(param, processType)) {
                            modified = true;
                        }
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

    private boolean updateParam(ElementParameterType param, ProcessType processType) {
        boolean modified = false;
        List ctxs = processType == null ? null : processType.getContext();
        if (param.getField() != null) {
            if (param.getField().equals(EParameterFieldType.MODULE_LIST.name()) && param.getValue() != null) {
                String jarUri = getMavenUriForJar(param.getValue(), ctxs);
                param.setValue(jarUri);
                modified = true;
            } else if (("DRIVER_JAR".equals(param.getName()) || "DRIVER_JAR_IMPLICIT_CONTEXT".equals(param.getName()))
                    && param.getElementValue() != null) {

                EList<?> elementValues = param.getElementValue();
                for (Object ev : elementValues) {
                    ElementValueType evt = (ElementValueType) ev;
                    String jarUri = getMavenUriForJar(evt.isHexValue(), evt.getValue(), ctxs);
                    if (!StringUtils.equals(jarUri, evt.getValue())) {
                        evt.setValue(jarUri);
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    private boolean updateParamForcConfig(ElementParameterType param, ProcessType processType) {
        boolean modified = false;
        List ctxs = processType == null ? null : processType.getContext();
        if (param.getField() != null) {
            if (param.getField().equals(EParameterFieldType.MODULE_LIST.name()) && param.getValue() != null) {
                String jarUri = getMavenUriForJar(param.getValue(), ctxs);
                param.setValue(jarUri);
                modified = true;
            } else if (("DRIVER_JAR".equals(param.getName()) || "DRIVER_JAR_IMPLICIT_CONTEXT".equals(param.getName()))
                    && param.getElementValue() != null) {

                EList<?> elementValues = param.getElementValue();

                ElementValueType jn = null;

                for (Object ev : elementValues) {
                    ElementValueType evt = (ElementValueType) ev;
                    switch (evt.getElementRef()) {
                        case "JAR_NAME": {
                            String jarUri = getMavenUriForJar(evt.getValue(), ctxs);
                            if (!StringUtils.equals(jarUri, evt.getValue())) {
                                jn = evt;
                                modified = true;
                            }
                            break;
                        }
                        case "JAR_NEXUS_VERSION": {
                            if (StringUtils.isNotBlank(evt.getValue()) && jn != null) {
                                String mvnURI = "mvn:org.talend.libraries/" + FilenameUtils.getBaseName(jn.getValue()) + "/"
                                        + evt.getValue() + "/jar";
                                jn.setValue(mvnURI);
                                jn = null;
                                modified = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return modified;
    }

    public String getMavenUriForJar(String jarName, List ctxs) {
        return getMavenUriForJar(false, jarName, ctxs);
    }
    
    public String getMavenUriForJar(boolean isHexValue, String jarName, List ctxs) {
        jarName = TalendTextUtils.removeQuotes(jarName);
        
        if(isHexValue) {
            jarName = parseJarNameForHexVaue(jarName);
        }
        
        boolean containContext = containContext(jarName, ctxs);
        if (!StringUtils.isEmpty(jarName) && !MavenUrlHelper.isMvnUrl(jarName) && !containContext && !isHexValue) {
            ModuleNeeded mod = new ModuleNeeded(null, jarName, null, true);
            if (!StringUtils.isEmpty(mod.getCustomMavenUri())) {
                return mod.getCustomMavenUri();
            }
            return mod.getMavenUri();
        }
        return jarName;
    }

    protected String parseJarNameForHexVaue(String jarName) {
        if(!jarName.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            try {
                byte[] decodeBytes = Hex.decodeHex(jarName.toCharArray());
                String elemValue = new String(decodeBytes, "UTF-8");
                if(!elemValue.startsWith(MavenUrlHelper.MVN_PROTOCOL) && !ContextParameterUtils.isContainContextParam(elemValue)) {
                    ModuleNeeded mod = new ModuleNeeded(null, elemValue, null, true);
                    if (!StringUtils.isEmpty(mod.getCustomMavenUri())) {
                        elemValue = mod.getCustomMavenUri();
                    } else {
                        elemValue = mod.getMavenUri();
                    }
                    
                    jarName = Hex.encodeHexString(elemValue.getBytes());
                }
            } catch (UnsupportedEncodingException e) {
                ExceptionHandler.process(e);
            }
        }
        
        return jarName;
    }

    public boolean containContext(String jarName, List ctxs) {
        if (ctxs == null) {
            // for project settings
            return ContextParameterUtils.isContainContextParam(jarName);
        }
        // check for job and connections
        for (Object ct : ctxs) {
            if (ContextParameterUtils.isContextParamOfContextType((ContextType) ct, jarName)) {
                return true;
            }
        }
        return false;
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
