// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * created by xqliu on 2014-8-7 Detailled comment
 * 
 */
public class UpdateTMultiPatternCheckTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 11, 0, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tMultiPatternCheck"); //$NON-NLS-1$
            IComponentConversion addRegexInvalidityMessageColumn = new AddRegexInvalidityMessageColumn();
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(addRegexInvalidityMessageColumn));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class AddRegexInvalidityMessageColumn implements IComponentConversion {

        @Override
        @SuppressWarnings("unchecked")
        public void transform(NodeType node) {
            EList metadatas = node.getMetadata();
            if (metadatas != null) {
                for (Object objMetadata : metadatas) {
                    if (objMetadata != null && objMetadata instanceof MetadataType) {
                        MetadataType metadataType = (MetadataType) objMetadata;
                        if ("ROW_PATTERN_KO".equals(metadataType.getName())) { //$NON-NLS-1$
                            boolean regexInvalidityMessageExist = false;
                            EList columns = metadataType.getColumn();
                            if (columns != null) {
                                for (Object objColumn : columns) {
                                    if (objColumn != null && objColumn instanceof ColumnType) {
                                        ColumnType columnType = (ColumnType) objColumn;
                                        if ("REGEX_INVALIDITY_MESSAGE".equals(columnType.getName())) { //$NON-NLS-1$
                                            regexInvalidityMessageExist = true;
                                            break;
                                        }
                                    }
                                }
                                if (!regexInvalidityMessageExist) {
                                    ColumnType columnType = TalendFileFactory.eINSTANCE.createColumnType();
                                    columnType.setName("REGEX_INVALIDITY_MESSAGE"); //$NON-NLS-1$
                                    columnType.setType("id_String"); //$NON-NLS-1$
                                    columns.add(columnType);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
