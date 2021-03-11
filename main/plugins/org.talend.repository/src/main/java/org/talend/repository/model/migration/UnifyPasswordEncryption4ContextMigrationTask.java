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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

/**
 * created by ggu on Aug 21, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4ContextMigrationTask extends UnifyPasswordEncryption4ItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof ContextItem) {
            List<ContextType> contextTypeList = ((ContextItem) item).getContext();

            boolean modify = false;
            if (contextTypeList != null) {
                for (ContextType type : contextTypeList) {
                    List<ContextParameterType> paramTypes = type.getContextParameter();
                    if (paramTypes != null) {
                        for (ContextParameterType param : paramTypes) {
                            String value = param.getValue();
                            if (value != null && PasswordEncryptUtil.isPasswordType(param.getType())) {
                                value = cleanPassword(value);
                                String rawPassword;
                                try {
                                    rawPassword = PasswordEncryptUtil.decryptPassword(value);
                                    param.setRawValue(rawPassword);
                                    modify = true;
                                } catch (Exception e) {
                                    param.setRawValue(value);
                                    modify = true;
                                }
                            }
                        }
                    }
                }

            }
            if (modify) {
                try {
                    factory.save(item, true);
                    return ExecutionResult.SUCCESS_NO_ALERT;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
