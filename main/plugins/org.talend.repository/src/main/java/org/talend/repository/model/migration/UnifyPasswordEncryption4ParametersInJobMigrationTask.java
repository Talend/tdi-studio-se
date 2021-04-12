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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.migration.EncryptPasswordInComponentsMigrationTask.FakeNode;

/**
 * created by ggu on Aug 25, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4ParametersInJobMigrationTask extends UnifyPasswordEncryption4ItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof ProcessItem) {
            ProcessType processType = ((ProcessItem) item).getProcess();
            return execute(item, processType);
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    protected ExecutionResult execute(Item item, ProcessType processType) {
        try {
            EmfHelper.visitChilds(processType);

            boolean modified = false;
            // context
            if (checkContext(item, processType)) {
                modified = true;
            }

            // job settings
            if (checkJobsettintsParameter(item, processType)) {
                modified = true;
            }

            // nodes parameters
            if (checkNodes(item, processType)) {
                modified = true;
            }

            if (modified) {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /**
     *
     * same as UnifyPasswordEncryption4ContextMigrationTask to change the encryption.
     */
    @SuppressWarnings("deprecation")
    protected boolean checkContext(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        // context
        EList contextList = processType.getContext();
        for (Object o : contextList) {
            if (o instanceof ContextType) {
                List<ContextParameterType> paramTypes = ((ContextType) o).getContextParameter();
                if (paramTypes != null) {
                    for (ContextParameterType param : paramTypes) {
                        String value = param.getValue();
                        if (value != null
                                && (PasswordEncryptUtil.isPasswordType(param.getType()) || PasswordEncryptUtil
                                        .isPasswordField(param.getName()))) {
                            value = cleanPassword(value);
                            try {
                                String rawPassword = PasswordEncryptUtil.decryptPassword(value);
                                param.setRawValue(rawPassword);
                            } catch (Exception e) {
                                param.setRawValue(value);
                            }
                            modified = true;
                        }
                    }
                }
            }
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
	                if ("PASS".equals(param.getName())) { //$NON-NLS-1$
	                    param.setField(EParameterFieldType.PASSWORD.getName());
	                    if (reencryptValueIfNeeded(param)) {
	                        modified = true;
	                    }
	                }

	                // variable name used for implicit context
	                if ("PASS_IMPLICIT_CONTEXT".equals(param.getName())) { //$NON-NLS-1$
	                    param.setField(EParameterFieldType.PASSWORD.getName());
	                    if (reencryptValueIfNeeded(param)) {
	                        modified = true;
	                    }
	                }
	            }
	        }
        }
        return modified;
    }

    protected boolean checkNodesFromEmf(Item item, ProcessType processType) throws Exception {
        boolean modified = false;
        for (Object nodeObject : processType.getNode()) {
            NodeType nodeType = (NodeType)nodeObject;
            for (Object paramObjectType : nodeType.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObjectType;
                if (param.getField() != null) {
                    if (EParameterFieldType.PASSWORD.getName().equals(param.getField()) && param.getValue() != null) {
                        if (reencryptValueIfNeeded(param)) {
                            modified = true;
                        }
                    }
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
                        if (EParameterFieldType.PASSWORD.equals(paramFromEmf.getFieldType()) && param.getValue() != null) {
                            param.setField(EParameterFieldType.PASSWORD.getName());
                            if (reencryptValueIfNeeded(param)) {
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        return modified;
    }

    @SuppressWarnings("deprecation")
    private boolean reencryptValueIfNeeded(ElementParameterType param) throws Exception {
        String value = param.getValue();
        value = cleanPassword(value);
        int index = value.lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
        if (index != -1) {
            value = new StringBuilder(value).replace(index, index + PasswordEncryptUtil.ENCRYPT_KEY.length(), "").toString(); //$NON-NLS-1$
            String rawValue = PasswordEncryptUtil.decryptPassword(value);
            value = rawValue;
        }
        param.setRawValue(value);
        return true;
    }

}
