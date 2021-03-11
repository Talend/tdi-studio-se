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
package org.talend.designer.core.generic.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.migration.IMigrationTask.ExecutionResult;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class DriverJarMigration extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 12, 19, 15, 0, 0).getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        boolean modified = false;
        if(processType.getParameters() != null){
            List elementParameter = processType.getParameters().getElementParameter();
            for (Object object : elementParameter) {
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    if(parameterType.getName().equals(EParameterName.DRIVER_JAR.getName()) || parameterType.getName().equals("DRIVER_JAR_IMPLICIT_CONTEXT")){
                        List eValue = parameterType.getElementValue();
                        if(eValue != null){
                            for(Object obj : eValue){
                                if(obj instanceof ElementValueType){
                                    ((ElementValueType)obj).setElementRef("drivers");
                                    modified = true;
                                }
                            }
                        }
                    }
//                    else if(PasswordEncryptUtil.isPasswordField(parameterType.getField())) {
//                        String pass = parameterType.getRawValue();
//                        if (pass != null && pass.length() > 0){
//                            String encryptValue = CryptoHelper.getDefault().encrypt(TalendQuoteUtils.removeQuotesIfExist(pass));
//                            parameterType.setValue(TalendQuoteUtils.addQuotesIfNotExist(encryptValue));
//                            modified = true;
//                        }
//
//                    }

                }
            }
        }
        if (modified) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
