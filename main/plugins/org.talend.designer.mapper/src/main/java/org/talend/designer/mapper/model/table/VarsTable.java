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
package org.talend.designer.mapper.model.table;

import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class VarsTable extends AbstractDataMapTable {

    private static final PatternCompiler COMPILER = new Perl5Compiler();

    public static final String PREFIX_VARS_TABLE_NAME = "Var"; //$NON-NLS-1$

    public static final String VALID_PATTERN_COLUMN_NAME = "^[a-zA-Z_][a-zA-Z_0-9]*$"; //$NON-NLS-1$

    public VarsTable(MapperManager mapperManager, String name) {
        super(mapperManager, name);
    }

    public void initFromExternalData(ExternalMapperTable externalMapperTable) {
        super.initFromExternalData(externalMapperTable);
        if (externalMapperTable != null) {
            List<ExternalMapperTableEntry> metadataTableEntries = externalMapperTable.getMetadataTableEntries();
            if (metadataTableEntries != null) {
                for (ExternalMapperTableEntry externalMapperTableEntry : metadataTableEntries) {
                    VarTableEntry varTableEntry = new VarTableEntry(this, externalMapperTableEntry.getName(),
                            externalMapperTableEntry.getExpression(), externalMapperTableEntry.getType());
                    // mapperManager.getProblemsManager().checkProblemsForTableEntry(varTableEntry, false);
                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        varTableEntry.setNullable(externalMapperTableEntry.isNullable());
                    }
                    addColumnEntry(varTableEntry);
                }
            }
        } else {
            setMinimized(true);
        }
    }

    /**
     * Manage to find a unique name with the given name.
     *
     * @param titleName
     */
    public String findUniqueColumnName(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException(Messages.getString("VarsTable.baseNameCannotNull")); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidColumnName(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    /**
     * Check if the given name will be unique in the process. If another link already exists with that name, false will
     * be returned.
     *
     * @param uniqueName
     * @return true if the name is unique
     */
    public boolean checkValidColumnName(String connectionName) {

        for (ITableEntry entry : dataMapTableEntries) {
            if (entry.getName().equals(connectionName)) {
                return false;
            }
        }

        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern;

        try {
            pattern = compiler.compile("^[A-Za-z_][A-Za-z0-9_]*$"); //$NON-NLS-1$
            if (!matcher.matches(connectionName, pattern)) {
                return false;
            }
        } catch (MalformedPatternException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     *
     * DOC amaumont Comment method "validateColumnName".
     *
     * @param columnName
     * @return true if columnName has a valid value
     */
    public String validateColumnName(String columnName, int beanPosition) {
        if (columnName == null) {
            return Messages.getString("VarsTable.columnNameIsNull"); //$NON-NLS-1$
        }
        Pattern validPatternColumnNameRegexp = null;
        if (validPatternColumnNameRegexp == null) {
            try {
                validPatternColumnNameRegexp = COMPILER.compile(VALID_PATTERN_COLUMN_NAME);
            } catch (MalformedPatternException e) {
                throw new RuntimeException(e);
            }
        }
        Perl5Matcher matcher = new Perl5Matcher();
        boolean match = matcher.matches(columnName, validPatternColumnNameRegexp);
        // System.out.println(columnName + " -> "+ match);
        if (!match) {
            return Messages.getString("VarsTable.columnNameTip") + columnName + Messages.getString("VarsTable.invalidTip"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int lstSize = dataMapTableEntries.size();
        for (int i = 0; i < lstSize; i++) {
            if (columnName.equals(dataMapTableEntries.get(i).getName()) && i != beanPosition) {
                return Messages.getString("VarsTable.columnNameTip") + columnName + Messages.getString("VarsTable.existTip"); //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
        return null;
    }

}
