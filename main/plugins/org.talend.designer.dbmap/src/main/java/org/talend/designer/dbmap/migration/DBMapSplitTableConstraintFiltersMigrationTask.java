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
package org.talend.designer.dbmap.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.language.generation.DbMapSqlConstants;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DBMapSplitTableConstraintFiltersMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        ProcessType processType = getProcessType(item);
        boolean modified = false;
        if (processType != null) {
            for (Object obj : processType.getNode()) {
                NodeType nodeType = (NodeType) obj;
                AbstractExternalData externalData = nodeType.getNodeData();
                if (externalData instanceof DBMapData) {
                    DBMapData mapperData = (DBMapData) externalData;
                    for (OutputTable pTable : mapperData.getOutputTables()) {
                        int i = 0;
                        List<FilterEntry> needSplitFilters = new ArrayList<FilterEntry>();
                        List<FilterEntry> newFilters = new ArrayList<FilterEntry>();
                        for (FilterEntry pFilter : pTable.getFilterEntries()) {
                            String expression = pFilter.getExpression();
                            if (expression != null && !expression.trim().isEmpty()) {
                                if (!DbGenerationManager.containWith(expression, DbMapSqlConstants.GROUP_BY_PATTERN, false)
                                        && !DbGenerationManager
                                                .containWith(expression, DbMapSqlConstants.ORDER_BY_PATTERN, false)) {
                                    continue;
                                } else {
                                    // can not split the clause directly here, because clause like this(a = b GROUP BY
                                    // c) will be put at
                                    // the end of where clause
                                    needSplitFilters.add(pFilter);
                                }
                            }
                        }

                        if (!needSplitFilters.isEmpty()) {
                            EList<FilterEntry> entryList = pTable.getFilterEntries();
                            for (FilterEntry pFilter : needSplitFilters) {
                                String expression = pFilter.getExpression().trim();
                                int splitIndex = firstIndexInString(expression, DbMapSqlConstants.GROUP_BY_PATTERN);
                                int orderIndex = firstIndexInString(expression, DbMapSqlConstants.ORDER_BY_PATTERN);

                                if (splitIndex < 0 || (0 <= orderIndex && orderIndex < splitIndex)) {
                                    splitIndex = orderIndex;
                                }

                                if (splitIndex == 0) {
                                    // keep the order of "GROUP BY" and "ORDER BY"
                                    pFilter.setFilterKind(FilterTableEntry.OTHER_FILTER);
                                    entryList.remove(pFilter);
                                    newFilters.add(pFilter);
                                } else {
                                    String whereClause = expression.substring(0, splitIndex);
                                    if (!DbGenerationManager.containWith(expression, DbMapSqlConstants.OR + "\\b", true) //$NON-NLS-1$
                                            && !DbGenerationManager.containWith(expression, DbMapSqlConstants.AND + "\\b", true)) { //$NON-NLS-1$
                                        whereClause = DbMapSqlConstants.AND + " " + whereClause; //$NON-NLS-1$
                                    }
                                    pFilter.setExpression(whereClause);
                                    FilterEntry tFilter = DbmapFactory.eINSTANCE.createFilterEntry();
                                    tFilter.setName("newFilterSplited" + ++i); //$NON-NLS-1$
                                    tFilter.setExpression(expression.substring(splitIndex).trim());
                                    tFilter.setFilterKind(FilterTableEntry.OTHER_FILTER);
                                    entryList.remove(pFilter);
                                    entryList.add(pFilter);
                                    newFilters.add(tFilter);
                                }
                                modified = true;
                            }
                            if (!newFilters.isEmpty()) {
                                pTable.getFilterEntries().addAll(newFilters);
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        try {
            if (modified) {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private int firstIndexInString(String expression, String pattern) {
        Pattern regex = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = regex.matcher(expression);
        if (regexMatcher.find()) {
            return regexMatcher.start();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 1, 16, 13, 0);
        return gc.getTime();
    }

}
