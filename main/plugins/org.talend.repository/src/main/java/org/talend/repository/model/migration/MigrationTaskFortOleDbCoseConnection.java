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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class MigrationTaskFortOleDbCoseConnection extends AbstractJobMigrationTask {


    @Override
    public ExecutionResult execute(final Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter filter = new NameComponentFilter("tOleDbInput"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        @Override
                        public void transform(NodeType node) {
  							ElementParameterType closeConnection = ComponentUtilities.getNodeProperty(node,
  									"CLOSE_CONNECTION"); //$NON-NLS-1$
  							if(closeConnection==null){
  							    ComponentUtilities.addNodeProperty(node, "CLOSE_CONNECTION", "CHECK");
  							    closeConnection = ComponentUtilities.getNodeProperty(node, "CLOSE_CONNECTION");
  							  closeConnection.setValue("false");
  							}
  						}

                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 11, 20, 10, 0, 0);
        return gc.getTime();
    }
}
