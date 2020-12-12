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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
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
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.migration.EncryptPasswordInComponentsMigrationTask.FakeNode;

/**
 * created by bhe on Dec 12, 2020 Detailled comment
 *
 */
public class FixModuleListInBDJDBCMigrationTask extends AbstractItemMigrationTask {

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private static final Set<String> COMPONENT_NAMES = new HashSet<String>();

    static {
        COMPONENT_NAMES.add("tJDBCConfiguration");
        COMPONENT_NAMES.add("tJDBCInput");
        COMPONENT_NAMES.add("tJDBCOutput");
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
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
            if (item instanceof ProcessItem) {
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

    private boolean updateProcessItem(Item item, ProcessType processType) throws Exception {
        EmfHelper.visitChilds(processType);

        boolean modified = false;

        // nodes parameters
        if (checkNodes(item, processType)) {
            modified = true;
        }
        return modified;
    }

    protected boolean checkNodesFromEmf(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        for (Object nodeObject : processType.getNode()) {
            NodeType nodeType = (NodeType) nodeObject;
            boolean needFix = needFix(nodeType.getComponentName());
            for (Object paramObjectType : nodeType.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObjectType;
                if (needFix && updateParam(param)) {
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
                boolean needFix = needFix(nodeType.getComponentName());
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
                        if (needFix && updateParam(param)) {
                            modified = true;
                        }
                    }
                }
            }
        }
        return modified;
    }

    private static boolean updateParam(ElementParameterType param) {
        boolean modified = false;
        if (param.getField() != null) {
            if (param.getField().equals(EParameterFieldType.MODULE_LIST.name()) && param.getValue() != null) {
                String context = getFixedValue(param.getValue());
                if (context != null) {
                    param.setValue(context);
                    modified = true;
                }
            } else if (("DRIVER_JAR".equals(param.getName()) || "DRIVER_JAR_IMPLICIT_CONTEXT".equals(param.getName()))
                    && param.getElementValue() != null) {

                EList<?> elementValues = param.getElementValue();
                for (Object ev : elementValues) {
                    ElementValueType evt = (ElementValueType) ev;
                    String context = getFixedValue(evt.getValue());
                    if (context != null) {
                        evt.setValue(context);
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    private static boolean needFix(String componentName) {
        return COMPONENT_NAMES.contains(componentName);
    }

    private static String getFixedValue(String paramVal) {
        if (paramVal == null || StringUtils.isEmpty(paramVal)) {
            return null;
        }
        String val = TalendTextUtils.removeQuotes(paramVal);
        if (StringUtils.isEmpty(val) || !val.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            return null;
        }

        String[] vals = val.split("/");
        if (vals.length > 3 && vals[0].equals("mvn:org.talend.libraries") && vals[2].equals("6.0.0-SNAPSHOT")
                && (vals[1].equals("context") || vals[1].startsWith("((String)context"))
                && !vals[vals.length - 1].equals("jar")) {
            return vals[1] + "." + vals[vals.length - 1];
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 12, 12, 12, 0, 0);
        return gc.getTime();
    }
}
