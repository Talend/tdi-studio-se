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
package org.talend.sqlbuilder;

/**
 * Put Constants here. <br/>
 *
 * $Id: IConstants.java,v 1.5 2006/11/07 09:48:06 peiqin.hou Exp $
 *
 */
public interface IConstants {

    String AUTO_COMMIT = "SQLEditor.AutoCommit"; //$NON-NLS-1$

    String AUTO_OPEN_EDITOR = "SQLEditor.AutoOpenEditor"; //$NON-NLS-1$

    String CLIP_EXPORT_COLUMNS = "SQLEditor.ClipExportColumns"; //$NON-NLS-1$

    String CLIP_EXPORT_SEPARATOR = "SQLEditor.ClipExportSeparator"; //$NON-NLS-1$

    String COMMIT_ON_CLOSE = "SQLEditor.CommitOnClose"; //$NON-NLS-1$

    String DATASETRESULT_DATE_FORMAT = "DataSetResult.DateFormat"; //$NON-NLS-1$

    String DATASETRESULT_FORMAT_DATES = "DataSetResult.FormatDates"; //$NON-NLS-1$

    String DEFAULT_DRIVER = "Drivers.DefaultDriverName"; //$NON-NLS-1$

    String FONT = "SQLEditor.Font"; //$NON-NLS-1$

    String HISTORY_AUTOSAVE_AFTER = "SQLHistory.AutoSaveAfterXXStatements"; //$NON-NLS-1$

    String INTERACTIVE_QUERY_TIMEOUT = "InteractiveConnection.QueryTimeOutSeconds"; //$NON-NLS-1$

    // String MAX_SQL_ROWS = "SQLEditor.MaxSQLRows";

    String MAX_SQL_ROWS = "100"; //$NON-NLS-1$

    String PRE_ROW_COUNT = "SQLEditor.PreRowCount"; //$NON-NLS-1$

    String SQL_ALT_QRY_DELIMITER = "SQLEditor.AltQueryDelimiter"; //$NON-NLS-1$

    String SQL_ASSIST = "SQLEditor.Assist"; //$NON-NLS-1$

    /** The color key for database tables column names. */
    String SQL_COLUMS = "SQLEditor.ColumnsColor"; //$NON-NLS-1$

    String SQL_COMMENT_DELIMITER = "SQLEditor.CommentDelimiter"; //$NON-NLS-1$

    /**
     * The color key for everthing in SQL code for which no other color is specified.
     */
    String SQL_DEFAULT = "SQLEditor.DefaultColor"; //$NON-NLS-1$

    /** The color key for SQL keywords in Java code. */
    String SQL_KEYWORD = "SQLEditor.KeywordColor"; //$NON-NLS-1$

    /** The color key for multi-line comments in Java code. */
    String SQL_MULTILINE_COMMENT = "SQLEditor.MultiLineCommentColor"; //$NON-NLS-1$

    String SQL_QRY_DELIMITER = "SQLEditor.QueryDelimiter"; //$NON-NLS-1$

    /** The color key for single-line comments in Java code. */
    String SQL_SINGLE_LINE_COMMENT = "SQLEditor.SingleLineCommentColor"; //$NON-NLS-1$

    /** The color key for string and character literals in Java code. */
    String SQL_STRING = "SQLEditor.StringColor"; //$NON-NLS-1$

    /** The color key for database tables names. */
    String SQL_TABLE = "SQLEditor.TableColor"; //$NON-NLS-1$

    String WARN_IF_LARGE_LIMIT = "SQLEditor.WarnIfLargeLimit"; //$NON-NLS-1$

    String WARN_LIMIT = "SQLEditor.WarnLimit"; //$NON-NLS-1$

    String WORD_WRAP = "SQLEditor.AutoWrap"; //$NON-NLS-1$

    public static final String BUILTIN = "BUILT_IN"; //$NON-NLS-1$

    public static final String REPOSITORY = "REPOSITORY"; //$NON-NLS-1$

    String QUERY_DELIMITER = "SQLEditor.QueryDelimiter"; //$NON-NLS-1$

    String ALTERNATE_DELIMITER = "SQLEditor.QueryDelimiter"; //$NON-NLS-1$

    String COMMENT_DELIMITER = "SQLEditor.CommentDelimiter"; //$NON-NLS-1$

    String LINE_DELIMITER = "SQLEditor.LineDelimiter"; //$NON-NLS-1$

    String WARN_RESEULTS = "SQLEditor.WarnResults"; //$NON-NLS-1$

    /**
     * DOC dev Comment method "unUse".
     */
    public void unUse();

    // String QUERY_DELIMITER = ";";

    // String ALTERNATE_DELIMITER = "";

    // String COMMENT_DELIMITER = "#";

    // char LINE_DELIMITER = '\n';

    // int WARN_RESEULTS = 5000;
}
