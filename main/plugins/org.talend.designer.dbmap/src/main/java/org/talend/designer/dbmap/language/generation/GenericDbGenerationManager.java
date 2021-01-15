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
package org.talend.designer.dbmap.language.generation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.MappingTypeRetriever;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.language.GenericDbLanguage;

/**
 * wzhang class global comment. Detailled comment
 */
public class GenericDbGenerationManager extends DbGenerationManager {

    public GenericDbGenerationManager() {
        super(new GenericDbLanguage());
    }

    @Override
    public String buildSqlSelect(DbMapComponent component, String outputTableName) {
        return super.buildSqlSelect(component, outputTableName);
    }

    @Override
    protected String getExpressionTableName(DbMapComponent component, IConnection conn, String name, String quote) {
        if (!isRefTableConnection(conn) && isSnowflakeUseDelimitedIdentifiers(component)) {
            if (name.contains(".") && name != null) { //$NON-NLS-1$
                MapExpressionParser mapParser2 = new MapExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
                List<Map<String, String>> tableNameList = mapParser2.parseInTableEntryLocations(name);
                for (Map<String, String> tableNameMap : tableNameList) {
                    Set<Entry<String, String>> setTable = tableNameMap.entrySet();
                    Iterator<Entry<String, String>> iteTable = setTable.iterator();
                    while (iteTable.hasNext()) {
                        Entry<String, String> tableEntry = iteTable.next();
                        String tableValue = tableEntry.getKey();
                        String schemaValue = tableEntry.getValue();
                        String handledTableName = null;
                        if (StringUtils.isNotBlank(schemaValue)) {
                            handledTableName = getNameWithDelimitedIdentifier(schemaValue, quote) + "."; //$NON-NLS-1$
                        }
                        return handledTableName + getNameWithDelimitedIdentifier(tableValue, quote);
                    }
                }
            }
            return getNameWithDelimitedIdentifier(name, quote);
        }
        return name;
    }

    @Override
    protected String getTableName(DbMapComponent component, IConnection conn, String name, String quote) {
        if (!isRefTableConnection(conn) && isSnowflakeUseDelimitedIdentifiers(component)) {
            return getHandledTableName(component, name, "");//$NON-NLS-1$
        } else {
            return name;
        }
    }

    @Override
    protected String getQuote(DbMapComponent component) {
        String quote = TalendQuoteUtils.QUOTATION_MARK;
        IElementParameter mappingPara = component.getElementParameter(EParameterName.MAPPING.getName());
        if (mappingPara == null) {
            return quote;
        }
        String mapping = (String) mappingPara.getValue();
        if (mapping == null) {
            return quote;
        }
        MappingTypeRetriever mappingTypeRetriever = MetadataTalendType.getMappingTypeRetriever(mapping);
        if (mappingTypeRetriever == null) {
            return quote;
        }
        Dbms dbms = mappingTypeRetriever.getDbms();
        String product = dbms.getProduct();
        if (isSnowflakeUseDelimitedIdentifiers(component)) {
            return quote;
        }
        EDatabaseTypeName type = EDatabaseTypeName.getTypeFromProductName(product);
        return TalendQuoteUtils.getQuoteByDBType(type);
    }

    @Override
    protected String getReplaceHandledName(DbMapComponent component, String handledName) {
        if (isSnowflakeUseDelimitedIdentifiers(component)) {
            handledName = TalendTextUtils.removeQuotes(handledName);
            return "\"\\" + "\"" + handledName + "\\" + "\"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        return handledName;
    }

    private String getDbType(DbMapComponent component) {
        IElementParameter mappingPara = component.getElementParameter(EParameterName.MAPPING.getName());
        if (mappingPara == null) {
            return null;
        }
        String mapping = (String) mappingPara.getValue();
        if (mapping == null) {
            return null;
        }
        MappingTypeRetriever mappingTypeRetriever = MetadataTalendType.getMappingTypeRetriever(mapping);
        if (mappingTypeRetriever == null) {
            return null;
        }
        Dbms dbms = mappingTypeRetriever.getDbms();
        if (dbms == null) {
            return null;
        }
        return dbms.getProduct();
    }

    private boolean isSnowflakeUseDelimitedIdentifiers(DbMapComponent component) {
        return isUseDelimitedIdentifiers() && ExtractMetaDataUtils.SNOWFLAKE.equalsIgnoreCase(getDbType(component));
    }

    @Override
    protected String getHandledTableName(DbMapComponent component, String tableName, String alias) {
        if (isSnowflakeUseDelimitedIdentifiers(component) && alias == null) {
            return super.getHandledTableName(component, tableName, "");//$NON-NLS-1$
        } else {
            return super.getHandledTableName(component, tableName, alias);
        }
    }
}
