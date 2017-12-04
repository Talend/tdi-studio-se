// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.migration.IMigrationTask.ExecutionResult;


/**
 * Configure the new __INCLUDTRASHFILES__ property to 'true' in order to keep the same behavior as before.
 * 
 * @author ypiel
 * @since 6.5.1
 */
public class AddIncludTrashFilesOnGoogleDriveListTask extends AbstractJobMigrationTask {
    
    /**
     * The component to update. 
     */
    private final static String GOOGLE_DRIVE_LIST_NAME = "tGoogleDriveList";
    
    /**
     * The name of the new property.
     */
    private final static String INCLUDTRASHFILES_PROPERTY_NAME = "INCLUDTRASHFILES";

    /**
     * Type of the new property.
     */
    private final static String INCLUDTRASHFILES_PROPERTY_TYPE = "CHECK"; 

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 07, 07, 12, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        
        IComponentConversion addIncludTrashProperty = new IComponentConversion() {
            
            @Override
            public void transform(NodeType node) {
                ElementParameterType includeTrashFiles = ComponentUtilities.getNodeProperty(node, INCLUDTRASHFILES_PROPERTY_NAME);
                
                // If INCLUDTRASHFILES_PROPERTY_NAME property doesn't exist, create with true to keep the same behavior as before 
                if(includeTrashFiles == null) {
                    ComponentUtilities.addNodeProperty(node, INCLUDTRASHFILES_PROPERTY_NAME, INCLUDTRASHFILES_PROPERTY_TYPE);
                    ComponentUtilities.getNodeProperty(node, INCLUDTRASHFILES_PROPERTY_NAME).setValue("true");
                }
            }

        };
        
        IComponentFilter filter = new NameComponentFilter(GOOGLE_DRIVE_LIST_NAME);
        
        try {
            ModifyComponentsAction.searchAndModify(item,
                                                       processType,
                                                       filter, 
                                                       Arrays.<IComponentConversion> asList(addIncludTrashProperty));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        
        
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
