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
package org.talend.designer.mapper.language.generation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.perl.PerlLanguage;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class PerlGenerationManager extends GenerationManager {

    public PerlGenerationManager(ILanguage language) {
        super(language);
    }

    /**
     * DOC amaumont Comment method "prefixEntryLocations".
     *
     * @param outputExpression
     * @param expressionParser
     * @return
     */
    public String prefixEntryLocationsForOutputExpression(String uniqueNameComponent, String outputExpression,
            DataMapExpressionParser expressionParser, TableType[] possibleSources) {
        outputExpression = tranformArraysExpressions(true, uniqueNameComponent, outputExpression, expressionParser,
                possibleSources);
        return tranformArraysExpressions(false, uniqueNameComponent, outputExpression, expressionParser,
                possibleSources);
    }

    /**
     * DOC amaumont Comment method "tranformArraysExpressions".
     *
     * @param uniqueNameComponent
     * @param outputExpression
     * @param expressionParser
     * @param possibleSources
     * @return
     */
    private String tranformArraysExpressions(boolean addArrayPointers, String uniqueNameComponent,
            String outputExpression, DataMapExpressionParser expressionParser, TableType[] possibleSources) {
        expressionParser.setLocationPattern(getPerlLanguage().getLocationPattern());
        TableEntryLocation[] allEntryLocations = expressionParser.parseTableEntryLocations(outputExpression);
        // expressionParser.setLocationPattern("\\$\\s*(\\w+)\\s*\\[\\s*(\\$?\\w+)\\s*\\]");
        TableEntryLocation[] validColumnEntryLocations = null;
        HashSet<TableEntryLocation> sValidColumnEntryLocations = new HashSet<TableEntryLocation>();
        if (!addArrayPointers) {
            expressionParser.setLocationPattern(getPerlLanguage().getLocationPatternValidColumnName());
            validColumnEntryLocations = expressionParser.parseTableEntryLocations(outputExpression);
            for (int i = 0; i < validColumnEntryLocations.length; i++) {
                sValidColumnEntryLocations.add(validColumnEntryLocations[i]);
            }
        }
        ArrayList<TableEntryLocation> listCoupleForAddTablePrefix = new ArrayList<TableEntryLocation>();
        ArrayList<TableEntryLocation> listCoupleForAddTablePrefixWithPrefixComponentName = new ArrayList<TableEntryLocation>();
        boolean possibleSourceInputs = false;
        boolean possibleSourceVars = false;
        for (int i = 0; i < possibleSources.length; i++) {
            TableType possibleSourceType = possibleSources[i];
            if (possibleSourceType == TableType.INPUT) {
                possibleSourceInputs = true;
            }
            if (possibleSourceType == TableType.VARS) {
                possibleSourceVars = true;
            }
        }

        for (TableEntryLocation location : allEntryLocations) {
            if (possibleSourceInputs && isInputTable(location.tableName)) {
                listCoupleForAddTablePrefix.add(location);
            } else if (possibleSourceVars && isVarsTable(location.tableName)) {
                listCoupleForAddTablePrefixWithPrefixComponentName.add(location);
            }
        }
        String outputExpressionToWrite = outputExpression;
        if (listCoupleForAddTablePrefix.size() > 0) {
            if (addArrayPointers) {
                outputExpressionToWrite = expressionParser.addRefArrayPointer(outputExpressionToWrite,
                        listCoupleForAddTablePrefix.toArray(new TableEntryLocation[0]));
            } else {
                outputExpressionToWrite = expressionParser.addTablePrefixToColumnName(uniqueNameComponent,
                        outputExpressionToWrite, listCoupleForAddTablePrefix.toArray(new TableEntryLocation[0]), false,
                        sValidColumnEntryLocations);
            }
        }
        if (listCoupleForAddTablePrefixWithPrefixComponentName.size() > 0) {
            if (addArrayPointers) {
                outputExpressionToWrite = expressionParser.addRefArrayPointer(outputExpressionToWrite,
                        listCoupleForAddTablePrefixWithPrefixComponentName.toArray(new TableEntryLocation[0]));
            } else {
                outputExpressionToWrite = expressionParser.addTablePrefixToColumnName(uniqueNameComponent,
                        outputExpressionToWrite, listCoupleForAddTablePrefixWithPrefixComponentName
                                .toArray(new TableEntryLocation[0]), true, sValidColumnEntryLocations);
            }
        }
        return outputExpressionToWrite;
    }

    /**
     * Build conditions for tMap before 2.1 .
     *
     * @param gm
     *
     * @param constraintTableEntries
     * @param expressionParser
     * @return
     */
    public String buildConditions(String uniqueNameComponent, List<ExternalMapperTableEntry> constraintTableEntries,
            DataMapExpressionParser expressionParser) {
        int lstSize = constraintTableEntries.size();
        StringBuilder stringBuilder = new StringBuilder();
        String and = null;
        for (int i = 0; i < lstSize; i++) {

            String constraintExpression = ((ExternalMapperTableEntry) constraintTableEntries.get(i)).getExpression();
            if (constraintExpression == null || constraintExpression.trim().equals("")) { //$NON-NLS-1$
                continue;
            }
            if (and != null && constraintExpression.trim().length() > 0) {
                stringBuilder.append(and);
            }
            String constraintExpressionToWrite = prefixEntryLocationsForOutputExpression(uniqueNameComponent,
                    constraintExpression, expressionParser, new TableType[] { TableType.INPUT, TableType.VARS });

            if (lstSize > 1) {
                stringBuilder.append(" ( " + constraintExpressionToWrite + " ) "); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                stringBuilder.append(constraintExpressionToWrite);
            }

            if (and == null) {
                and = language.getAndCondition();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * DOC amaumont Comment method "buildNewArrayDeclaration".
     *
     * @param name
     * @return
     */
    public String buildNewArrayDeclaration(String name, int indent) {
        return "my $" + name + " = [];"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * DOC amaumont Comment method "buildNewArrayDeclaration".
     *
     * @param name
     * @return
     */
    public String buildNewArrayDeclaration(String uniqueNameComponent, String name, int indent) {
        return "my $" + uniqueNameComponent + "__" + name + " = [];"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * DOC amaumont Comment method "buildNewArrayDeclaration".
     *
     * @param name
     * @return
     */
    public String buildNewArrayDeclarationWithKeyValue(String name, String[] keysValues, int indent) {
        String string = ""; //$NON-NLS-1$
        // string += "my @" + name + "ArrayFromLookup = $tHash_" + name + "{"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < keysValues.length; i++) {
            if (i > 0) {
                string += ".'|'."; //$NON-NLS-1$
            }
            string += keysValues[i]; //$NON-NLS-1$ //$NON-NLS-2$
        }
        // string += "};"; //$NON-NLS-1$
        return string;
    }

    public PerlLanguage getPerlLanguage() {
        return (PerlLanguage) language;
    }

}
