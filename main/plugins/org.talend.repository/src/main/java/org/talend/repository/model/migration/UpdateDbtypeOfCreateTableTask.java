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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class UpdateDbtypeOfCreateTableTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);

        if (getProject().getLanguage().equals(ECodeLanguage.JAVA) && processType != null) {
            final Map<String, String> dbtypes = new HashMap<String, String>();
            dbtypes.put("Access", "ACCESS"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("AS400", "AS400"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("DB2", "DB2"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("FireBird", "FIREBIRD"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Hsql", "HSQLDB"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Informix", "INFORMIX"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Ingres", "INGRES"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Interbase", "INTERBASE"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("JavaDb", "JAVADB"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("MSSQLServer", "MSSQL"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Oracle", "DBORACLE"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Postgre", "POSTGRE"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("SQLite", "SQLITE"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("Sybase", "SYBASE"); //$NON-NLS-1$ //$NON-NLS-2$
            dbtypes.put("ODBC", "ODBC"); //$NON-NLS-1$ //$NON-NLS-2$

            final String name = "tCreateTable"; //$NON-NLS-1$
            final String property = "DBTYPE"; //$NON-NLS-1$

            IComponentFilter filert = new IComponentFilter() {

                public boolean accept(NodeType node) {

                    boolean toReturn = (name == null ? true : acceptS(node));
                    if (toReturn) {
                        String pValue = ComponentUtilities.getNodePropertyValue(node, property);
                        toReturn = !dbtypes.values().contains(pValue);
                    }
                    return toReturn;
                }

                public boolean acceptS(NodeType node) {
                    return node.getComponentName().equals(name);
                }
            };

            IComponentConversion conversion = new IComponentConversion() {

                public void transform(NodeType node) {
                    String pValue = ComponentUtilities.getNodePropertyValue(node, property);
                    String value = dbtypes.get(pValue);
                    if (value != null) {
                        ComponentUtilities.setNodeValue(node, property, value);
                    } else {
                        ComponentUtilities.setNodeValue(node, property, "MYSQL"); //$NON-NLS-1$
                    }
                }
            };
            List<IComponentConversion> cons = new ArrayList<IComponentConversion>();
            cons.add(conversion);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filert, cons);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
            // do nothing
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
