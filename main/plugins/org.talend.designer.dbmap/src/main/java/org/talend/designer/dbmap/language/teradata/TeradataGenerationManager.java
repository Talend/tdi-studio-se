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
package org.talend.designer.dbmap.language.teradata;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: GenerationManager.java 1299 2007-01-05 14:53:10Z amaumont $
 *
 */
public class TeradataGenerationManager extends DbGenerationManager {

    public TeradataGenerationManager() {
        super(new TeradataLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    protected ExternalDbMapTable removeUnmatchingEntriesWithColumnsOfMetadataTable(ExternalDbMapTable externalDbMapTable,
            IMetadataTable metadataTable) {
        return externalDbMapTable; // keep original, don't change
    }

    @Override
    protected boolean isColumnChanged(ExternalDbMapEntry dbMapEntry, String expression) {
        boolean columnChanged = false;
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(language);
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        if (tableEntriesLocationsSources.length > 1) {
            columnChanged = true;
        } else {
            for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
                TableEntryLocation location = tableEntriesLocationsSource;
                String entryName = getAliasOf(dbMapEntry.getName());
                if (location != null && entryName != null && !entryName.startsWith("_") //$NON-NLS-1$
                        && !entryName.equals(location.columnName)) {
                    columnChanged = true;
                }
            }
        }
        return columnChanged;
    }
}
