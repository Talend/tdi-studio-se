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
package org.talend.sqlbuilder.ui.proposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.swt.colorstyledtext.jedit.Mode;
import org.talend.commons.ui.swt.colorstyledtext.jedit.Modes;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.util.QueryTokenizer;

/**
 * dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorProposalUtil.java,v 1.19 2006/11/08 10:03:04 qiang.zhang Exp $
 *
 * @author qiang.zhang
 */
public class SQLEditorProposalUtil {

    private RepositoryNode session;

    private List<IContentProposal> proposals;

    private List<String> allString;

    private String languageMode;

    private int position;

    private String[] contents;

    private Map<String, List<String>> tableAndColumns;

    private String dbType;

    private List<String> aliasList = new ArrayList<String>();

    public SQLEditorProposalUtil(RepositoryNode session, String language) {
        super();
        contents = new String[2];
        this.session = session;
        this.languageMode = language;
        allString = new ArrayList<String>();
        if (allString != null && !allString.isEmpty()) {
            allString.clear();
        }
        dbType = SQLBuilderRepositoryNodeManager.getDbTypeFromRepositoryNode(session);
        getAllProposalString();
    }

    /**
     * dev Comment method "getSQLEditorContentProposals".
     *
     * @param content editor has input all String
     * @param position edit current position
     * @return ContentProposal array.
     */
    public IContentProposal[] getSQLEditorContentProposals(String content, int positionInt) {
        this.position = positionInt;
        proposals = new ArrayList<IContentProposal>();
        if (content.length() < positionInt) {
            return new IContentProposal[0];
        }
        contents[0] = content.substring(0, positionInt);
        contents[1] = content.substring(positionInt);
        proposals = new ArrayList<IContentProposal>();
        List<String> queryStrings = getAllSqlQuery(content);

        try {
            String[] curSql = getCurrentSqlQuery(content);

            String currentSql = curSql[0] + curSql[1];

            this.findAlias(currentSql);

            // When alias like this: alias1.columnname1, alias2.columnname2, gets all alias;

            int indexOfFrom = -1;
            int indexOfWhere = -1;

            indexOfFrom = getIndex(currentSql, "from"); //$NON-NLS-1$
            indexOfWhere = getIndex(currentSql, "where"); //$NON-NLS-1$

            // Checks if string between "from" and "where".
            if ((indexOfFrom != -1) && (indexOfWhere != -1)) {
                String strBetweenFromAndWhere = currentSql.substring(indexOfFrom + 4, indexOfWhere).trim();
                String[] splitArray = strBetweenFromAndWhere.split(","); //$NON-NLS-1$
                handleStrBetweenFromAndWhere(splitArray);
            }

            if (queryStrings.isEmpty() || (curSql[1].length() == 0 && curSql[0].length() == 0)) {
                for (String string : allString) {
                    proposals.add(createAllProposal("", string)); //$NON-NLS-1$
                }
            } else {
                while (!queryStrings.isEmpty()) {
                    hasSQLQueryProposal(queryStrings, curSql);
                }
            }
        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("SQLEditorProposalUtil.logMessage1"), e); //$NON-NLS-1$
        }
        IContentProposal[] res = new IContentProposal[proposals.size()];
        res = proposals.toArray(res);
        return res;

    }

    /**
     * ftang Comment method "handleStrBetweenFromAndWhere".
     *
     * @param splitArray
     */
    private void handleStrBetweenFromAndWhere(String[] splitArray) {
        for (String string : splitArray) {
            int indexOfWhiteSpace = string.trim().indexOf(" "); //$NON-NLS-1$
            // Checks if has white space, that mean has alias.
            if (indexOfWhiteSpace != -1) {
                handleAliasBetweenFromAndWhere(string, indexOfWhiteSpace);
            }
        }
    }

    /**
     * ftang Comment method "handleAliasBetweenFromAndWhere".
     *
     * @param string
     * @param indexOfWhiteSpace
     */
    private void handleAliasBetweenFromAndWhere(String string, int indexOfWhiteSpace) {
        String originalStr = string.substring(0, indexOfWhiteSpace + 1).trim();
        String aliasStr = string.substring(indexOfWhiteSpace + 1).trim();
        List<String> needChangedList = new ArrayList<String>();
        addChangedEntry(originalStr, aliasStr, needChangedList);
        allString.addAll(0, needChangedList);
    }

    /**
     * ftang Comment method "addChangedEntry".
     *
     * @param originalStr
     * @param aliasStr
     * @param needChangedList
     */
    private void addChangedEntry(String originalStr, String aliasStr, List<String> needChangedList) {
        // Put all alias into needChangedList;
        if (allString.contains(aliasStr)) {
            allString.remove(aliasStr);
        }

        for (Object object : allString) {
            String str = (String) object;
            if (isTable(str, originalStr)) {

                String tempStr = ""; //$NON-NLS-1$
                if (originalStr.endsWith("\"")) { //$NON-NLS-1$
                    tempStr = str.replace(originalStr.replaceAll("\"", ""), aliasStr); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    tempStr = str.replace(originalStr, aliasStr);
                }
                needChangedList.add(Messages.getString("SQLEditorProposalUtil.needChanged", tempStr, str)); //$NON-NLS-1$

                // Sets key and values into tablesAndColums.
                String newElement = ""; //$NON-NLS-1$
                List<String> newColumnList = new ArrayList<String>();
                Set<String> keySet = tableAndColumns.keySet();
                for (Iterator iter = keySet.iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    String element = key;
                    element = handleKeyStr(element);
                    if (element.equals(originalStr.replaceAll("\"", ""))) { //$NON-NLS-1$ //$NON-NLS-2$
                        newElement = tempStr;
                        List<String> columnList = tableAndColumns.get(key);
                        for (String string2 : columnList) {
                            if (string2.indexOf(originalStr) != -1) {
                                newColumnList.add(string2.replaceFirst(originalStr.replaceAll("\"", ""), aliasStr)); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
                if (!newElement.equals("") && (newColumnList.size() != 0)) { //$NON-NLS-1$
                    this.tableAndColumns.put(newElement, newColumnList);
                }
            }
        }
    }

    /**
     * ftang Comment method "handleKeyStr".
     *
     * @param element
     * @return
     */
    private String handleKeyStr(String element) {
        element = element.substring(element.indexOf(".") + 1); //$NON-NLS-1$
        element = element.startsWith("\"") || element.startsWith("'") ? element.substring(1) : element; //$NON-NLS-1$ //$NON-NLS-2$
        int elementLength = element.length();
        element = element.endsWith("\"") || element.endsWith("'") ? element.substring(0, elementLength - 1) : element; //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

    /**
     * ftang Comment method "getIndex".
     *
     * @param beforeCursorSql
     * @param indexStr
     * @return
     */
    private int getIndex(String beforeCursorSql, String indexStr) {

        int indexOfLowerCase = beforeCursorSql.indexOf(indexStr.toLowerCase());
        int indexOfUpperCase = beforeCursorSql.indexOf(indexStr.toUpperCase());
        if (indexOfLowerCase != -1) {
            return indexOfLowerCase;
        } else if (indexOfUpperCase != -1) {
            return indexOfUpperCase;
        }
        return -1;
    }

    /**
     * ftang Comment method "isTable".
     *
     * @param str
     * @param originalStr
     * @return
     */
    private boolean isTable(String str, String originalStr) {
        int firstindexOf = str.indexOf("."); //$NON-NLS-1$
        int lastIndexOf = str.lastIndexOf("."); //$NON-NLS-1$
        if (firstindexOf == -1 || firstindexOf != lastIndexOf) {
            return false;
        }
        String tableName = str.substring(firstindexOf + 1);

        return tableName.replaceAll("\"", "").equals(originalStr.replaceAll("\"", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    /**
     * ftang Comment method "findAlias".
     *
     * @param input
     */
    private void findAlias(String currentSql) {

        // in case of : alias1.columnname1, alias2.columnname2
        String str1 = currentSql.replaceAll(",", " "); //$NON-NLS-1$ //$NON-NLS-2$
        String[] splitStr1 = str1.split(" "); //$NON-NLS-1$
        if (splitStr1 == null || splitStr1.length == 0) {
            return;
        }
        for (String string : splitStr1) {

            int firstIndexOfDot = string.indexOf("."); //$NON-NLS-1$
            int lastIndexOfDot = string.lastIndexOf("."); //$NON-NLS-1$

            if (firstIndexOfDot != -1 && lastIndexOfDot == firstIndexOfDot) {
                String aliasStr = string.substring(0, firstIndexOfDot).trim();
                if (aliasStr != null && aliasStr.length() != 0 && !isExisting(aliasStr)) {
                    allString.add(aliasStr);
                    if (!aliasList.contains(aliasStr)) {
                        aliasList.add(aliasStr);
                    }
                }
            }
        }

        // // /: in case of: columnname1 as alias1, column2 as alias2
        // int indexOfFrom = this.getIndex(currentSql, "from");
        // if (indexOfFrom != -1) {
        // splitStr1 = currentSql.split(",");
        // for (String string : splitStr1) {
        // String aliasStr = this.findAliasWithAs(string);
        // if (aliasStr != null && aliasStr.length() != 0 && !isExisting(aliasStr)) {
        // allString.add(aliasStr);
        // if (!aliasList.contains(aliasStr)) {
        // aliasList.add(aliasStr);
        // }
        // }
        // }
        // }

    }

    // // select column1 as alias1, column2 as alias2 from table1 as t1, table2 as t2 where t1.alias1 = t2.alias2;
    // private String findAliasWithAs(String splitStr) {
    // String aliasStr = splitStr.substring(this.getIndex(splitStr, "as") + 2);
    // return aliasStr;
    // }

    /**
     * ftang Comment method "isExisting".
     *
     * @param str
     * @return
     */
    private boolean isExisting(String str) {
        String compareStr1 = "'" + str + "'"; //$NON-NLS-1$ //$NON-NLS-2$
        String compareStr2 = "\"" + str + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 0; i < allString.size(); i++) {
            String strFromAllString = allString.get(i);
            if (TextUtil.isDoubleQuotesNeededDbType(this.dbType) && strFromAllString.indexOf(str + ".") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            } else if (strFromAllString.equalsIgnoreCase(str) || strFromAllString.indexOf(compareStr1) != -1
                    || strFromAllString.indexOf(compareStr2) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * dev Comment method "getAllQqlQuery". s
     *
     * @param content editor has input all String
     * @return list of all SQL Query.
     */
    private List<String> getAllSqlQuery(String content) {
        Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.QUERY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.ALTERNATE_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.COMMENT_DELIMITER);

        QueryTokenizer qt = new QueryTokenizer(content, queryDelimiter, alternateDelimiter, commentDelimiter);
        List<String> queryStringsTmp = new ArrayList<String>();
        while (qt.hasQuery()) {
            String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) { //$NON-NLS-1$
                queryStringsTmp.add(querySql);
            }
        }
        List<String> queryStrings = new ArrayList<String>();
        while (!queryStringsTmp.isEmpty()) {
            String temp = (String) queryStringsTmp.remove(0);
            String querySql = temp.replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
            queryStrings.add(querySql);
        }
        return queryStrings;
    }

    /**
     * Get Current SQL Query.
     *
     * @param content editor content.
     * @return current SQL Query String
     */
    private String[] getCurrentSqlQuery(String content) {
        String[] curSql = new String[2];
        String before = content.substring(0, position);
        String after = content.substring(position);
        if (before != null && before.trim().length() != 0) {
            if (before.lastIndexOf(";") == -1) { //$NON-NLS-1$
                curSql[0] = before.replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                curSql[0] = before.substring(before.lastIndexOf(";") + 1, position).replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } else {
            curSql[0] = ""; //$NON-NLS-1$
        }
        if (after != null && after.trim().length() != 0) {
            if (after.lastIndexOf(";") == -1) { //$NON-NLS-1$
                curSql[1] = after.replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                curSql[1] = after.substring(0, after.indexOf(";")).replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } else {
            curSql[1] = ""; //$NON-NLS-1$
        }

        return curSql;
    }

    /**
     * dev Comment method "getAllKeywords".
     *
     * @return all Key words of SQL.
     */
    private String[] getAllKeywords() {
        Mode mode = Modes.getMode(languageMode + XmlUtil.FILE_XML_SUFFIX);
        String[] keywords = mode.getDefaultRuleSet().getKeywords().get("KEYWORD1"); //$NON-NLS-1$
        return keywords;
    }

    /**
     * dev Comment method "getAllProposalString".
     */
    private void getAllProposalString() {
        tableAndColumns = SQLBuilderRepositoryNodeManager.getAllNamesByRepositoryNode(session);

        Set<String> alltablenames = tableAndColumns.keySet();
        List<String> tcs = new ArrayList<String>();
        for (String tableName : alltablenames) {
            List<String> columns = tableAndColumns.get(tableName);
            for (String string : columns) {
                tcs.add(string);
            }
            tcs.add(tableName);
        }

        String[] allTablesAndColumns = new String[tcs.size()];
        allTablesAndColumns = (String[]) tcs.toArray(allTablesAndColumns);
        Arrays.sort(allTablesAndColumns);

        for (String tablename : allTablesAndColumns) {
            allString.add(tablename);
        }
        String[] allKeywords = getAllKeywords();
        Arrays.sort(allKeywords);
        for (String keyword : allKeywords) {
            allString.add(keyword.toLowerCase());
        }
    }

    String hasInput = ""; //$NON-NLS-1$

    /**
     * dev Comment method "hasSQLQueryProposal".
     *
     * @param queryStrings all Sql Query String.
     * @param curSql current Sql Query String.
     */
    private void hasSQLQueryProposal(List<String> queryStrings, String[] curSql) {
        String querySql = (String) queryStrings.remove(0);
        if ((curSql[0] + curSql[1]).trim().startsWith(querySql.trim())) {
            int seqIndex = checkTableName(curSql); //$NON-NLS-1$
            int dotIndex = curSql[0].lastIndexOf("."); //$NON-NLS-1$
            List<String> list = new ArrayList<String>();
            if (seqIndex > -1 && dotIndex > seqIndex + 1) {
                final String substring = curSql[0].substring(seqIndex, dotIndex);
                String tableName = TalendTextUtils.removeQuotesForField(substring, dbType);
                List<String> columns = getColumnsByTableName(tableName);
                if (columns != null) {
                    list.addAll(columns);
                }
            } else {
                list.addAll(allString);
            }
            createProposal(hasInput, list);
        }
    }

    /**
     * qzhang Comment method "getLastIndex".
     *
     * @param curSql
     * @return
     */
    private int checkTableName(String[] curSql) {
        int seqIndex = curSql[0].lastIndexOf(" "); //$NON-NLS-1$
        int quoteIndex = curSql[0].lastIndexOf(","); //$NON-NLS-1$
        if (quoteIndex > seqIndex) {
            seqIndex = quoteIndex;
        }

        String quote = TalendTextUtils.getQuoteByDBType(dbType, false);
        if (curSql[0].contains(quote) && seqIndex < curSql[0].lastIndexOf(quote)) {
            int i = curSql[0].lastIndexOf(quote) - 1;
            quote = TalendTextUtils.getQuoteByDBType(dbType, true);
            int lquote = curSql[0].substring(0, i).lastIndexOf(quote);
            if (lquote > -1 && lquote < seqIndex) {
                seqIndex = lquote;
                hasInput = curSql[0].substring(seqIndex);

            } else {
                seqIndex += 1;
            }
        } else {
            if (seqIndex > -1) {
                hasInput = curSql[0].substring(seqIndex + 1);
                seqIndex += 1;
            } else {
                hasInput = curSql[0];
            }
        }
        return seqIndex;
    }

    /**
     * dev Comment method "createProposal".
     *
     * @param hasInput
     * @param list
     */
    private void createProposal(String hasInput, List<String> list) {
        if (list != null) {
            String newHasInput = hasInput; //$NON-NLS-1$ //$NON-NLS-2$
            int hasIndex = newHasInput.indexOf("."); //$NON-NLS-1$
            for (String string : list) {
                int index = string.indexOf("."); //$NON-NLS-1$
                int index2 = string.lastIndexOf("."); //$NON-NLS-1$
                String tmp2 = ""; //$NON-NLS-1$
                String column = ""; //$NON-NLS-1$
                if (index > -1) {
                    tmp2 = string.substring(index + 2, string.length() - 1).replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    if (index2 > index) {
                        column = string.substring(index2 + 2, string.length() - 1).replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        if (hasIndex == -1) {
                            tmp2 = ""; //$NON-NLS-1$
                        }
                    }
                } else {
                    tmp2 = string;
                }
                int index3 = string.indexOf("alias: "); //$NON-NLS-1$
                if (index3 != -1) {
                    String string2 = string.substring(index3 + 7, string.indexOf("\n")); //$NON-NLS-1$
                    index = string2.indexOf("."); //$NON-NLS-1$
                    tmp2 = string2.substring(index + 2, string2.length() - 1).replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }
                final String toLowerCase = TalendTextUtils.removeQuotesForField(newHasInput, dbType).toLowerCase();
                if (tmp2.toLowerCase().startsWith(toLowerCase) || column.toLowerCase().startsWith(toLowerCase)) {
                    proposals.add(createAllProposal(hasInput, string));
                }
            }
        }
    }

    /**
     * dev Comment method "createAllProposal".
     *
     * @param hasInput
     * @param string
     * @return
     */
    private SQLEditorAllProposal createAllProposal(String hasInput, String string) {
        SQLEditorAllProposal p = new SQLEditorAllProposal(hasInput, string, position, contents, dbType);
        return p;
    }

    /**
     * dev Comment method "getColumnsByTableName".
     *
     * @param tableName
     * @return
     */
    private List<String> getColumnsByTableName(String tableName) {
        Set<String> alltablenames = tableAndColumns.keySet();
        Map<String, String> tables = new HashMap<String, String>();
        for (String string : alltablenames) {
            tables.put(string.substring(string.indexOf(".") + 2, string.length() - 1), string); //$NON-NLS-1$
        }
        List<String> columns = tableAndColumns.get(tables.get(tableName.replaceAll("\"", ""))); //$NON-NLS-1$ //$NON-NLS-2$
        return columns;
    }
}
