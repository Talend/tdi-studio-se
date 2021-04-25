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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.utils.generation.CodeGenerationUtils;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: GenerationManager.java 1299 2007-01-05 14:53:10Z amaumont $
 *
 */
public class JavaGenerationManager extends GenerationManager {

    private Map<String, HashedMetadataTable> hashMetadataTable = new HashMap<String, HashedMetadataTable>();

    public JavaGenerationManager(ILanguage language) {
        super(language);
    }

    /**
     * DOC amaumont Comment method "buildLookupDataInstance".
     *
     * @param name
     *
     * @param tableName
     * @param keysValues
     * @param keysValues2
     * @param indent
     * @return
     */
    public String buildLookupDataInstance(String uniqueNameComponent, String name, String[] keysNames,
            String[] keysValues, int indent, boolean writeCommentedFieldKeys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keysNames.length; i++) {

            String key = CodeGenerationUtils.buildProblemKey(uniqueNameComponent,
                    JavaGenerationManager.PROBLEM_KEY_FIELD.METADATA_COLUMN.toString(), name, keysNames[i]);
            if (writeCommentedFieldKeys) {
                sb.append("\n").append(CodeGenerationUtils.buildJavaStartFieldKey(key)); //$NON-NLS-1$
            }

            String expression = indent(indent) + name + "HashKey." + keysNames[i] + " = " + keysValues[i] + ";"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            sb.append("\n").append(expression); //$NON-NLS-1$

            if (writeCommentedFieldKeys) {
                sb.append("\n").append(CodeGenerationUtils.buildJavaEndFieldKey(key)); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }

    /**
     * DOC amaumont Comment method "buildConditions".
     *
     * @param gm
     *
     * @param constraintTableEntries
     * @param expressionParser
     * @return
     */
    public String buildConditions(List<ExternalMapperTableEntry> constraintTableEntries,
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

            if (lstSize > 1) {
                stringBuilder.append(" ( " + constraintExpression + " ) "); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                stringBuilder.append(constraintExpression);
            }

            if (and == null) {
                and = language.getAndCondition();
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {

        JavaGenerationManager manager = new JavaGenerationManager(LanguageProvider.getJavaLanguage());
        // System.out.println(manager.insertFieldKey("table:column", "\nligne1\nligne2\nligne3"));
    }

    public void hashMetadataTable(IMetadataTable metadataTable) {
        hashMetadataTable.put(metadataTable.getTableName(), new HashedMetadataTable(metadataTable));
    }

    public HashedMetadataTable getHashedMetadataTable(String key) {
        return hashMetadataTable.get(key);
    }

    /**
     *
     * DOC amaumont MapperManager class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    public enum PROBLEM_KEY_FIELD {
        GLOBAL_MAP("globalMap variable"), //$NON-NLS-1$
        METADATA_COLUMN("column"), //$NON-NLS-1$
        FILTER("filter"), //$NON-NLS-1$
        ;

        private String label;

        private PROBLEM_KEY_FIELD(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

}
