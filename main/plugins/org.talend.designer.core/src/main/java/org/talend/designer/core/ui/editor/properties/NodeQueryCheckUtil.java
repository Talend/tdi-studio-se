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
package org.talend.designer.core.ui.editor.properties;

import java.util.regex.Matcher;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 */
public final class NodeQueryCheckUtil {

    public static final String SELECT = "SELECT"; //$NON-NLS-1$

    public static final String FROM = "FROM"; //$NON-NLS-1$

    private static final int REGX_FLAG = java.util.regex.Pattern.CANON_EQ | java.util.regex.Pattern.CASE_INSENSITIVE
            | java.util.regex.Pattern.UNICODE_CASE;

    private static final String NL_REGX_ONE = "(\\s)+"; //$NON-NLS-1$

    private static final String QUOTE = LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA ? TalendTextUtils.QUOTATION_MARK
            : TalendTextUtils.SINGLE_QUOTE;

    // reg: "(\s)*(select)(\s)+(.*?)(\s)+(from)(\s)+(.*)"
    private static final String SQL_REGX = "^" + QUOTE + "(\\s)*(" + SELECT + ")" + NL_REGX_ONE + "(.*?)" + NL_REGX_ONE + "(" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            + FROM + ")" + NL_REGX_ONE + "(.*)" + QUOTE + "$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final String NL_REGX = "(\\s)*"; //$NON-NLS-1$

    /*
     * add by wzhang
     */
    // field contains function
    private static final String SQL_FUNC_REGX = "(.+?)(\\s)*\\((\\s)*(.*)(\\s)*\\)(\\s)*(.*)"; //$NON-NLS-1$

    // split the function
    private static final String FUNC_SPLIT = "(\\s)*(\\w*)\\((.*?)\\)(\\s+\\w*){0,1}"; //$NON-NLS-1$

    private static final String COMMENT_REGX_PATTERN = ("(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/"); //$NON-NLS-1$

    private static boolean needMatchQuery;

    /**
     *
     * DOC ggu Comment method "checkQueryOK".
     *
     * @param node
     * @param sql
     * @return
     */
    public static boolean checkQueryOK(Node node, String sql) {
        setNeedMatchQuery(false);
        if (sql == null) {
            return false;
        }
        // replace the new line char
        sql = sql.replaceAll("\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
        sql = sql.replaceAll("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$

        // empty
        sql = sql.trim();
        if ("".equals(sql)) { //$NON-NLS-1$
            return false;
        }

        // match sql query: select x.a, x.b from x
        java.util.regex.Pattern sqlRegex = java.util.regex.Pattern.compile(SQL_REGX, REGX_FLAG);
        Matcher matcher = sqlRegex.matcher(sql);
        if (!matcher.find()) {
            // it isn't the select query, so not checked.
            return true;
        }

        // get the columns
        matcher.lookingAt();
        String columns = matcher.group(4).trim();
        if ("".equals(columns)) { //$NON-NLS-1$
            return false;
        }
        //
        if ("*".equals(columns)) { //$NON-NLS-1$
            return true;
        }
        boolean result = compareNodeTableColumns(node, columns);
        if (!result) {
            setNeedMatchQuery(!result);
        }
        return result;

    }

    /**
     *
     * DOC wzhang Comment method "compareNodeTableColumnsWithFunc".
     *
     * @param node
     * @param columns
     * @return
     */
    private static boolean compareNodeTableColumnsWithFunc(Node node, String columns) {
        String originalColumns = columns;
        if (node.getMetadataList().size() == 0) {
            return true;
        }
        IMetadataTable metaTable = node.getMetadataList().get(0);
        if (metaTable == null || metaTable.getListColumns() == null) {
            return true;
        }
        int originColumnSize = metaTable.getListColumns().size();
        // modified by wzhang. replace the field to one String if it contains function
        columns = columns.replaceAll(FUNC_SPLIT, "column"); //$NON-NLS-1$
        String[] columnArray = columns.split(","); //$NON-NLS-1$
        // columns not match
        if (columnArray.length != originColumnSize) {
            // if can not match , we should match the columns with function
            try {
                PatternCompiler pc = new Perl5Compiler();
                org.apache.oro.text.regex.Pattern pattern = null;
                pattern = pc.compile(SQL_FUNC_REGX, REGX_FLAG);
                PatternMatcher columnMatcher = new Perl5Matcher();
                if (columnMatcher.matches(originalColumns, pattern)) {
                    String columnWithFunc = columnMatcher.getMatch().group(4).trim();
                    if (columnWithFunc != null) {
                        String[] columnWithFuncArray = columnWithFunc.split(","); //$NON-NLS-1$
                        if (columnWithFuncArray.length > 1) {
                            originalColumns = originalColumns.replace(columnWithFunc, "columnWithFunction"); //$NON-NLS-1$
                            return compareNodeTableColumnsWithFunc(node, originalColumns);
                        }
                    }
                }
            } catch (MalformedPatternException e) {
                return false;
            }
            return false;
        }
        return true;
    }

    /**
     *
     * DOC wzhang Comment method "compareNodeTableColumnsNoFunc".
     *
     * @param node
     * @param columns
     * @return
     */
    private static boolean compareNodeTableColumnsNoFunc(Node node, String columns) {
        if (node.getMetadataList().size() == 0) {
            return true;
        }
        IMetadataTable metaTable = node.getMetadataList().get(0);
        if (metaTable == null || metaTable.getListColumns() == null) {
            return true;
        }
        int originColumnSize = metaTable.getListColumns().size();
        String[] columnArray = columns.split(","); //$NON-NLS-1$
        // columns not match
        if (columnArray.length != originColumnSize) {
            return false;
        }
        return true;
    }

    /**
     * See bug 5836. java.util.regex works too slow here. Use apache oro regex library instead.
     * <p>
     * DOC xye Comment method "apacheRegexMatch".
     *
     * @param patternString
     * @param flag
     * @param input
     * @return
     */
    private static boolean apacheRegexMatch(final String patternString, final int flag, final String input) {
        PatternCompiler pc = new Perl5Compiler();
        org.apache.oro.text.regex.Pattern pattern = null;
        try {
            pattern = pc.compile(patternString, flag);
            PatternMatcher columnMatcher = new Perl5Matcher();
            return columnMatcher.matches(input, pattern);
        } catch (MalformedPatternException e) {
            return false;
        }
    }

    public static String matchQueryComments(Node node, String sql) {
        if (sql == null) {
            return null;
        }
        if (sql.contains("--") || sql.contains("/*")) {//$NON-NLS-1$ //$NON-NLS-2$
            // match the query comments
            return sql.replaceAll(COMMENT_REGX_PATTERN, ""); //$NON-NLS-1$
        }
        return sql;
    }

    private static boolean compareNodeTableColumns(Node node, String columns) {
        boolean match = apacheRegexMatch(SQL_FUNC_REGX, REGX_FLAG, columns);
        if (!match) {
            // no function
            return compareNodeTableColumnsNoFunc(node, columns);
        } else {
            // contains function
            return compareNodeTableColumnsWithFunc(node, columns);
        }
    }

    public static boolean isNeedMatchQuery() {
        return needMatchQuery;
    }

    public static void setNeedMatchQuery(boolean matchQuery) {
        needMatchQuery = matchQuery;
    }
}
