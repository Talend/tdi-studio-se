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
package org.talend.repository.model.migration.spark;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.impl.AdditionalInfoMapImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 *
 * @author ametivier
 *
 * to keep retro compatibility with some components that used specific RDD implementation that don't behave the same with
 * dataset API in spark, migrated job from previous versions will by default use RDD implementation instead of dataset
 * according to spark version value.
 *
 */
public class SetDatasetCheckBoxGlobalOption extends AbstractJobMigrationTask {
    
    private String CHECKBOX_DATASET = "USE_DATASET_API"; //$NON-NLS-1$
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 11, 25, 10, 0, 0);
        return gc.getTime();
    }
    
    @Override
    public List<ERepositoryObjectType> getTypes() {
        return Arrays.asList(ERepositoryObjectType.PROCESS_MR);
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        List<AdditionalInfoMapImpl> properties = item.getProperty().getAdditionalProperties();
        String talendVersionUsedToCreateJob = null;
        String lastImportedVersion = "0";
        for (AdditionalInfoMapImpl property : properties) {
        	if ("created_product_version".equals(property.getKey().toString())) {
        		talendVersionUsedToCreateJob = property.getValue().toString();
        	} else if ("import_product_version".equals(property.getKey().toString())) {
        		// for some reason since 7.3 the import version is prefixed by time Big Data Platform- so we remove it 
        		// to keep it as something like 7.3.1.20200831_1017-patch
        		lastImportedVersion = property.getValue().toString().replaceAll("time Big Data Platform-", "");
        	}
        }
        try {
    		setGlobalOption(processType, item, talendVersionUsedToCreateJob, lastImportedVersion);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }
    
    private void setGlobalOption(ProcessType processType, Item item, String talendStudioVersion, String lastImportedVersion) throws PersistenceException {
    	ElementParameterType propertyFalse = TalendFileFactory.eINSTANCE.createElementParameterType();
    	propertyFalse.setName(CHECKBOX_DATASET); //$NON-NLS-1$
        propertyFalse.setField("CHECK"); //$NON-NLS-1$
        propertyFalse.setValue("false"); //$NON-NLS-1$
        ElementParameterType propertyTrue = TalendFileFactory.eINSTANCE.createElementParameterType();
        propertyTrue.setName(CHECKBOX_DATASET); //$NON-NLS-1$
        propertyTrue.setField("CHECK"); //$NON-NLS-1$
        propertyTrue.setValue("true"); //$NON-NLS-1$
        boolean isParameterAlreadyAdded = processType.getParameters().getElementParameter().stream().anyMatch(x -> "USE_DATASET_API".equals(((ElementParameterTypeImpl) x).getName()));
        if (!isParameterAlreadyAdded && lastImportedVersion.compareTo("7.3.1") > 0) {
        	processType.getParameters().getElementParameter().add(propertyTrue);
        } else if (!isParameterAlreadyAdded && talendStudioVersion.compareTo("7.3.1") < 0) {
        	processType.getParameters().getElementParameter().add(propertyFalse);
        }
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        factory.save(item, true);
    }
}
