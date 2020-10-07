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
package org.talend.designer.core.model.components;

import org.talend.designer.core.i18n.Messages;

/**
 * Enumeration that describes all the standard name used in the properties.
 *
 * $Id$
 *
 */
public enum EParameterName {
    PROJECT_TECHNICAL_NAME(Messages.getString("EParameterName.Project_Technical_Name")), //$NON-NLS-1$
    NAME(Messages.getString("EParameterName.Name")), //$NON-NLS-1$
    UNIQUE_NAME(Messages.getString("EParameterName.uniqueName")), //$NON-NLS-1$
    TRANSLATED_UNIQUE_NAME("Translated Unique Name"), //$NON-NLS-1$
    AUTHOR(Messages.getString("EParameterName.Author")), //$NON-NLS-1$
    PURPOSE(Messages.getString("EParameterName.Purpose")), //$NON-NLS-1$
    HELP(Messages.getString("EParameterName.help")), //$NON-NLS-1$
    START(Messages.getString("EParameterName.start")), //$NON-NLS-1$
    STARTABLE(Messages.getString("EParameterName.startable")), //$NON-NLS-1$
    STATUS(Messages.getString("EParameterName.Status")), //$NON-NLS-1$
    DESCRIPTION(Messages.getString("EParameterName.Description")), //$NON-NLS-1$
    VERSION(Messages.getString("EParameterName.Version")), //$NON-NLS-1$
    PLATFORM(Messages.getString("EParameterName.Platform")), //$NON-NLS-1$
    FAMILY(Messages.getString("EParameterName.Family")), //$NON-NLS-1$
    LOG(Messages.getString("EParameterName.Log")), //$NON-NLS-1$
    ACTIVATE(Messages.getString("EParameterName.Activate")), //$NON-NLS-1$
    LABEL(Messages.getString("EParameterName.LabelFormat")), //$NON-NLS-1$
    HINT(Messages.getString("EParameterName.HintFormat")), //$NON-NLS-1$
    CONNECTION_FORMAT(Messages.getString("EParameterName.ConnectionFormat")), //$NON-NLS-1$
    SHOW_HINT(Messages.getString("EParameterName.ShowHint")), //$NON-NLS-1$
    INFORMATION(Messages.getString("EParameterName.Information")), //$NON-NLS-1$
    COMMENT(Messages.getString("EParameterName.Comment")), //$NON-NLS-1$
    LOG_FILENAME(Messages.getString("EParameterName.FileName")), //$NON-NLS-1$
    LEVEL_LOG_TO_FILE(Messages.getString("EParameterName.Level.Log.File")), //$NON-NLS-1$
    LEVEL_LOG_TO_DB(Messages.getString("EParameterName.Level.Log.Db")), //$NON-NLS-1$
    LEVEL_LOG_TO_STDOUT(Messages.getString("EParameterName.Level.Log.Stdout")), //$NON-NLS-1$
    LOG_TO_FILE(Messages.getString("EParameterName.Log.File")), //$NON-NLS-1$
    LOG_TO_DB(Messages.getString("EParameterName.Log.Db")), //$NON-NLS-1$
    LOG_TO_STDOUT(Messages.getString("EParameterName.Log.Stdout")), //$NON-NLS-1$
    SCHEMA_TYPE(Messages.getString("EParameterName.schemaType")), //$NON-NLS-1$
    REPOSITORY_SCHEMA_TYPE("Repository"), //$NON-NLS-1$  Hidden parameter so no translation needed
    REPOSITORY_COPYBOOK_NOXC2JFILE("NO_X2CJ_FILE"), //$NON-NLS-1$
    QUERYSTORE_TYPE(Messages.getString("EParameterName.queryType")), //$NON-NLS-1$
    REPOSITORY_QUERYSTORE_TYPE("Repository"), //$NON-NLS-1$  Hidden parameter so no translation needed
    PROPERTY_TYPE(Messages.getString("EParameterName.propertyType")), //$NON-NLS-1$
    SK_CREATION(Messages.getString("EParameterName.SK_CREATION")), //$NON-NLS-1$
    REPOSITORY_PROPERTY_TYPE("Repository"), //$NON-NLS-1$  Hidden parameter so no translation needed
    CONDITION(Messages.getString("EParameterName.condition")), //$NON-NLS-1$
    ROUTETYPE(Messages.getString("EParameterName.routeType")), //$NON-NLS-1$
    ENDOFCHOICE(Messages.getString("EParameterName.endofChoice")), // TESB-8043 //$NON-NLS-1$
    USE_NAMESPACES(Messages.getString("EParameterName.useNamespaces")), //$NON-NLS-1$
    NAMESPACES(Messages.getString("EParameterName.namespaces")), //$NON-NLS-1$
    PREFIX(Messages.getString("EParameterName.xpathPrefix")), //$NON-NLS-1$
    URI(Messages.getString("EParameterName.xpathURI")), //$NON-NLS-1$
    EXCEPTIONLIST(Messages.getString("EParameterName.listExceptions")), //$NON-NLS-1$
    COMPONENT_NAME(Messages.getString("EParameterName.componentName")), //$NON-NLS-1$
    UPDATE_COMPONENTS("Update components"), //$NON-NLS-1$  Hidden parameter so no translation needed
    ICONSELECTION("Icon(32x32)"), //$NON-NLS-1$
    PROCESS_TYPE(Messages.getString("EParameterName.generateCode")), //$NON-NLS-1$
    PROCESS_TYPE_PROCESS(Messages.getString("EParameterName.process")), //$NON-NLS-1$
    PROCESS(Messages.getString("EParameterName.process")), //$NON-NLS-1$
    // added by hcyi for feature 19312
    USE_DYNAMIC_JOB(Messages.getString("EParameterName.Use.Dynamic.Job")), //$NON-NLS-1$
    CONTEXT_JOB(Messages.getString("EParameterName.Context.Job")), //$NON-NLS-1$
    PROCESS_TYPE_VERSION(Messages.getString("EParameterName.Version")), //$NON-NLS-1$
    PROCESS_TYPE_CONTEXT(Messages.getString("EParameterName.context")), //$NON-NLS-1$
    PREVIEW(Messages.getString("EParameterName.preview")), //$NON-NLS-1$
    COLUMN_LIST(Messages.getString("EParameterName.columnList")), //$NON-NLS-1$
    CONNECTION_LIST(Messages.getString("EParameterName.connectionList")), //$NON-NLS-1$
    PREV_COLUMN_LIST(Messages.getString("EParameterName.prevColumnList")), //$NON-NLS-1$
    LOOKUP_COLUMN_LIST(Messages.getString("EParameterName.lookupColumnList")), //$NON-NLS-1$
    TSTATCATCHER_STATS(Messages.getString("EParameterName.tStatCatcherStats")), //$NON-NLS-1$
    COMP_DEFAULT_FILE_DIR("COMP_DEFAULT_FILE_DIR"), //$NON-NLS-1$  Hidden parameter so no translation needed
    PRODUCT_ROOT_DIR("PRODUCT_ROOT_DIR"), //$NON-NLS-1$  for TDQ_INSTALL_DIR
    COMP_DEFAULT_PROJECT_DIR("COMP_DEFAULT_PROJECT_DIR"), //$NON-NLS-1$  Hidden parameter so no translation needed
    // For TDQ-11338 Add this path for tDqReportRun component on git remote project
    TDQ_DEFAULT_PROJECT_DIR("TDQ_DEFAULT_PROJECT_DIR"), //$NON-NLS-1$  Hidden parameter so no translation needed
    DQ_REPORTING_BUNDLE_DIR("DQ_REPORTING_BUNDLE_DIR"), //$NON-NLS-1$  Hidden parameter so no translation needed
    LOG4J_RUN_ACTIVATE("LOG4J_RUN_ACTIVATE"), //$NON-NLS-1$  Hidden parameter so no translation needed
    LOG4J_RUN_LEVEL("LOG4J_RUN_LEVEL"), //$NON-NLS-1$  Hidden parameter so no translation needed
    JOB_RUN_VM_ARGUMENTS("JOB_RUN_VM_ARGUMENTS"), //$NON-NLS-1$  Hidden parameter so no translation needed
    JOB_RUN_VM_ARGUMENTS_OPTION(Messages.getString("EParameterName.jvmArgTitle")), //$NON-NLS-1$
    REPOSITORY_ALLOW_AUTO_SWITCH("REPOSITORY_ALLOW_AUTO_SWITCH"), //$NON-NLS-1$  Hidden parameter so no translation needed
    ENCODING_TYPE(Messages.getString("EParameterName.encodingType")), //$NON-NLS-1$
    ENCODING(Messages.getString("EParameterName.encoding")), //$NON-NLS-1$
    CSV_OPTION(Messages.getString("EParameterName.csvOption")), //$NON-NLS-1$
    ESCAPE_CHAR(Messages.getString("EParameterName.escapeChar")), //$NON-NLS-1$
    TEXT_ENCLOSURE(Messages.getString("EParameterName.textEnclosure")), //$NON-NLS-1$
    COMPONENT_LIST(Messages.getString("EParameterName.componentList")), //$NON-NLS-1$
    MAPPING(Messages.getString("EParameterName.mapping")), //$NON-NLS-1$
    MAPPING_TYPE(Messages.getString("EParameterName.mapping")), //$NON-NLS-1$
    DUMMY("DUMMY"), //$NON-NLS-1$ Hidden parameter so no translation needed
    DBTABLE(Messages.getString("EParameterName.TableName")), //$NON-NLS-1$
    TABLE(Messages.getString("EParameterName.TableName")), //$NON-NLS-1$
    QUERY_CONDITION("Query Condition"), //$NON-NLS-1$
    MODULE_LIST(Messages.getString("EParameterName.ModuleList")), //$NON-NLS-1$
    CURRENT_OS(Messages.getString("EParameterName.CurrentOS")), //$NON-NLS-1$
    IREPORT_PATH("IREPORT_PATH"), //$NON-NLS-1$  Hidden parameter so no translation needed
    CONNECTION("CONNECTION"), //$NON-NLS-1$  Parameter without label displayed so no translation needed
    NOT_SYNCHRONIZED_SCHEMA("NOT_SYNCHRONIZED_SCHEMA"), //$NON-NLS-1$  not displayed so no translation needed
    SCHEMA_OPTIONS(Messages.getString("EParameterName.schemaOptions")), //$NON-NLS-1$
    COLLAPSED("COLLAPSED"), //$NON-NLS-1$  Hidden parameter so no translation needed
    SUBJOB_DISPLAYED("DISPLAY_SUBJOB"), //$NON-NLS-1$  Hidden parameter so no translation needed
    SHOW_SUBJOB_TITLE(Messages.getString("EParameterName.showSubjob2")), //$NON-NLS-1$
    SUBJOB_TITLE(Messages.getString("EParameterName.title")), //$NON-NLS-1$
    SUBJOB_TITLE_COLOR(Messages.getString("EParameterName.SubjobTitleColor")), //$NON-NLS-1$
    SUBJOB_COLOR(Messages.getString("EParameterName.SubjobColor")), //$NON-NLS-1$
    SQLPATTERN_DB_NAME("SQL Template DB"), //$NON-NLS-1$
    SQLPATTERN_VALUE("SQL Template"), //$NON-NLS-1$
    NOTE_COLOR(Messages.getString("EParameterName.NoteColor")), //$NON-NLS-1$
    NOTETXT_COLOR(Messages.getString("EParameterName.NoteTextColor")), //$NON-NLS-1$
    NOTETXT_LEFT(Messages.getString("EParameterName.NoteTextLeft")), //$NON-NLS-1$
    NOTETXT_RIGHT(Messages.getString("EParameterName.NoteTextRight")), //$NON-NLS-1$
    NOTETXT_CENTER(Messages.getString("EParameterName.NoteTextCenter")), //$NON-NLS-1$
    NOTETXT_TOP(Messages.getString("EParameterName.NoteTextTop")), //$NON-NLS-1$
    NOTETXT_BOTTOM(Messages.getString("EParameterName.NoteTextBottom")), //$NON-NLS-1$
    NOTELABEL_CENTER(Messages.getString("EParameterName.NoteLabelCenter")), //$NON-NLS-1$
    NOTE_FONT(Messages.getString("EParameterName.NoteFont")), //$NON-NLS-1$
    FONT_SIZE(Messages.getString("EParameterName.FontSize")), //$NON-NLS-1$
    FONT_BOLD(Messages.getString("EParameterName.FontBold")), //$NON-NLS-1$
    FONT_ITALIC(Messages.getString("EParameterName.FontItalic")), //$NON-NLS-1$
    NOTE_LINECOLOR(Messages.getString("EParameterName.NoteLineColor")), //$NON-NLS-1$
    SCHEMAS("Schema(s)"), //$NON-NLS-1$
    SCHEMA("Schema"), //$NON-NLS-1$
    DCSCHEMA("Schema"), //$NON-NLS-1$
    MONITOR_CONNECTION(Messages.getString("Connection.monitorConnection")), //$NON-NLS-1$
    LANGUAGEWARNING(Messages.getString("Connection.languageWarning")), //$NON-NLS-1$
    TRACES_CONNECTION_ENABLE(Messages.getString("EParameterName.TracesConnection")), //$NON-NLS-1$
    TRACES_CONNECTION_FILTER("Filter the traces"), //$NON-NLS-1$ Hidden parameter so no translation needed
    TRACES_SHOW_ENABLE("Traces show"), //$NON-NLS-1$ Hidden parameter so no translation needed
    VARIABLES("VARIABLES"), //$NON-NLS-1$
    USE_EXISTING_CONNECTION(Messages.getString("EParameterName.useExistConn")), //$NON-NLS-1$
    USE_TRANSACTION("use_transaction"), //$NON-NLS-1$

    // feature 13940
    HEADERFOOTER_HEADERID("headerfooter_headerid"), //$NON-NLS-1$
    HEADER_ENABLED("header_enabled"), //$NON-NLS-1$
    HEADER_CODE("header_code"), //$NON-NLS-1$
    HEADER_IMPORT("header_import"), //$NON-NLS-1$
    HEADER_LIBRARY("header_library"), //$NON-NLS-1$
    HEADERFOOTER_FOOTERID("headerfooter_footerid"), //$NON-NLS-1$
    FOOTER_ENABLED("footer_enabled"), //$NON-NLS-1$
    FOOTER_CODE("footer_code"), //$NON-NLS-1$
    FOOTER_IMPORT("footer_import"), //$NON-NLS-1$
    FOOTER_LIBRARY("footer_library"), //$NON-NLS-1$

    RESUMING_CHECKPOINT("Recovery Checkpoint"), //$NON-NLS-1$
    RESUMLABEL("Label"), //$NON-NLS-1$
    FAILURE_INSTRUCTIONS("Failure instructions"), //$NON-NLS-1$
    ACTIVEBREAKPOINT("Activate conditional breakpoint"), //$NON-NLS-1$
    // hywang add for feature7373
    COLUMNINDEX(Messages.getString("EParameterName.columnIndex")), //$NON-NLS-1$
    CDC_TYPE_MODE("CDC Type Mode"), //$NON-NLS-1$
    /**
     * For stats & logs parameters.
     */
    ON_STATCATCHER_FLAG(Messages.getString("EParameterName.UseStatistics")), //$NON-NLS-1$
    ON_LOGCATCHER_FLAG(Messages.getString("EParameterName.UseLogs")), //$NON-NLS-1$
    ON_METERCATCHER_FLAG(Messages.getString("EParameterName.UseVolumetrics")), //$NON-NLS-1$
    ON_CONSOLE_FLAG(Messages.getString("EParameterName.onConsoleFlag")), //$NON-NLS-1$
    ON_FILES_FLAG(Messages.getString("EParameterName.onFileFlag")), //$NON-NLS-1$
    FILE_PATH(Messages.getString("EParameterName.filePath")), //$NON-NLS-1$
    FILENAME(Messages.getString("EParameterName.FileName")), //$NON-NLS-1$
    FILENAME_STATS(Messages.getString("EParameterName.fileNameStats")), //$NON-NLS-1$
    FILENAME_LOGS(Messages.getString("EParameterName.fileNameLogs")), //$NON-NLS-1$
    FILENAME_METTER(Messages.getString("EParameterName.MeterFileName")), //$NON-NLS-1$
    ON_DATABASE_FLAG(Messages.getString("EParameterName.onDatabaseFlag")), //$NON-NLS-1$
    DB_TYPE(Messages.getString("EParameterName.dbType")), //$NON-NLS-1$
    CONNECTION_TYPE(Messages.getString("EParameterName.connectionType")), //$NON-NLS-1$
    HOST(Messages.getString("EParameterName.host")), //$NON-NLS-1$
    PORT(Messages.getString("EParameterName.port")), //$NON-NLS-1$
    DBNAME(Messages.getString("EParameterName.dbName")), //$NON-NLS-1$
    SCHEMA_DB(Messages.getString("EParameterName.schemaDb")), //$NON-NLS-1$
    SCHEMA_DB_ORACLE("SCHEMA_DB"), //$NON-NLS-1$
    SCHEMA_DB_DB2("TABLESCHEMA"), //$NON-NLS-1$
    SCHEMA_DB_MSSQL("DB_SCHEMA"), //$NON-NLS-1$
    SCHEMA_JOB("SCHEMA_DB"), //$NON-NLS-1$
    LOCAL_SERVICE_NAME(Messages.getString("EParameterName.service_name")), //$NON-NLS-1$
    USER(Messages.getString("EParameterName.user")), //$NON-NLS-1$
    PASS(Messages.getString("EParameterName.password")), //$NON-NLS-1$
    TABLE_STATS(Messages.getString("EParameterName.tableStats")), //$NON-NLS-1$
    TABLE_LOGS(Messages.getString("EParameterName.tableLogs")), //$NON-NLS-1$
    TABLE_METER(Messages.getString("EParameterName.tableMeter")), //$NON-NLS-1$
    CATCH_RUNTIME_ERRORS(Messages.getString("EParameterName.catchRuntimeErrors")), //$NON-NLS-1$
    CATCH_USER_ERRORS(Messages.getString("EParameterName.catchUserErrors")), //$NON-NLS-1$
    CATCH_USER_WARNING(Messages.getString("EParameterName.catchUserWarning")), //$NON-NLS-1$
    CATCH_REALTIME_STATS(Messages.getString("EParameterName.catchRealtimeStats")), //$NON-NLS-1$
    PROPERTIES(Messages.getString("EParameterName.additionParam")), //$NON-NLS-1$
    DBFILE(Messages.getString("EParameterName.database")), //$NON-NLS-1$
    DB_VERSION(Messages.getString("EParameterName.dbVersion")), //$NON-NLS-1$
    DATASOURCE(Messages.getString("EParameterName.dataSource")), //$NON-NLS-1$
    USE_SHARED_CONNECTION(Messages.getString("EParameterName.useSharedConnection")), //$NON-NLS-1$
    SHARED_CONNECTION_NAME(Messages.getString("EParameterName.sharedConnectionName")), //$NON-NLS-1$
    SAP_FUNCTION(Messages.getString("EParameterName.FunName")), //$NON-NLS-1$
    SAP_IDOC(Messages.getString("EParameterName.IDocName")), //$NON-NLS-1$

    URL(Messages.getString("EParameterName.jdbcURL")), //$NON-NLS-1$
    DRIVER_JAR(Messages.getString("EParameterName.driverJar")), //$NON-NLS-1$
    DRIVER_CLASS(Messages.getString("EParameterName.className")), //$NON-NLS-1$
    MAPPING_FILE(Messages.getString("EParameterName.mappingFile")), //$NON-NLS-1$

    IMPLICITE_JDBC_URL(Messages.getString("EParameterName.jdbcURL")), //$NON-NLS-1$
    IMPLICITE_DRIVER_JAR(Messages.getString("EParameterName.driverJar")), //$NON-NLS-1$
    IMPLICITE_DRIVER_CLASS(Messages.getString("EParameterName.className")), //$NON-NLS-1$
    IMPLICITE_MAPPING_FILE(Messages.getString("EParameterName.mappingFile")), //$NON-NLS-1$

    /**
     * For Job Settings extra parameters.
     */
    MULTI_THREAD_EXECATION(Messages.getString("EParameterName.MultiThread")), //$NON-NLS-1$
    IMPLICIT_TCONTEXTLOAD(Messages.getString("EParameterName.ImplicitContextLoad")), //$NON-NLS-1$
    IMPLICIT_TCONTEXTLOAD_FILE(Messages.getString("EParameterName.FromFile")), //$NON-NLS-1$
    FIELDSEPARATOR(Messages.getString("EParameterName.FieldSeparator")), //$NON-NLS-1$
    ROWSEPARATOR(Messages.getString("EParameterName.RowSeparator")), //$NON-NLS-1$
    FROM_FILE_FLAG(Messages.getString("EParameterName.fromFileFlag")), //$NON-NLS-1$
    OVERRIDE_ENCODING_FLAG(Messages.getString("EParameterName.OverrideEncodingFlag")), //$NON-NLS-1$
    // for override encoding (name should be diff from the encoding of Stats&Logs)
    OVERRIDE_ENCODING_IN_EXTRA("OVERRIDE_ENCODING_IN_EXTRA"), //$NON-NLS-1$
    FROM_DATABASE_FLAG(Messages.getString("EParameterName.fromDatabaseFlag")), //$NON-NLS-1$
    // implict tConextLoad parameters.
    LOAD_NEW_VARIABLE(Messages.getString("EParameterName.LoadNewVariableLabel")), //$NON-NLS-1$
    NOT_LOAD_OLD_VARIABLE(Messages.getString("EParameterName.NotLoadOldVariableLabel")), //$NON-NLS-1$
    PRINT_OPERATIONS(Messages.getString("EParameterName.PrintOperations")), //$NON-NLS-1$
    DISABLE_ERROR(Messages.getString("EParameterName.DisableErrors")), //$NON-NLS-1$
    DISABLE_INFO(Messages.getString("EParameterName.DisableInfos")), //$NON-NLS-1$
    DISABLE_WARNINGS(Messages.getString("EParameterName.DisableWarnings")), //$NON-NLS-1$

    // added by wchen for feature 6759
    USE_MULTISEPARATORS(Messages.getString("EParameterName.useMultiSaparators")), //$NON-NLS-1$
    MULTI_SEPARATORS(Messages.getString("EParameterName.multiSaparators")), //$NON-NLS-1$
    MULTI_KEYVALUES(Messages.getString("EParameterName.multiKeyValues")), //$NON-NLS-1$
    /*
     * PERL_ON_FILES_FLAG(Messages.getString("EParameterName.onFileFlag")), //$NON-NLS-1$
     * PERL_FILE_PATH(Messages.getString("EParameterName.filePath")), //$NON-NLS-1$
     * PERL_FILENAME_STATS(Messages.getString("EParameterName.fileNameStats")), //$NON-NLS-1$
     * PERL_FILENAME_LOGS(Messages.getString("EParameterName.fileNameLogs")), //$NON-NLS-1$
     * PERL_ON_DATABASE_FLAG(Messages.getString("EParameterName.onDatabaseFlag")), //$NON-NLS-1$
     * PERL_DB_TYPE(Messages.getString("EParameterName.dbType")), //$NON-NLS-1$
     * PERL_PROPERTY_TYPE(Messages.getString("EParameterName.propertyType")), //$NON-NLS-1$
     * PERL_REPOSITORY_PROPERTY_TYPE("Repository"), //$NON-NLS-1$ Hidden parameter so no translation needed
     * PERL_HOST(Messages.getString("EParameterName.host")), //$NON-NLS-1$
     * PERL_PORT(Messages.getString("EParameterName.port")), //$NON-NLS-1$
     * PERL_DBNAME(Messages.getString("EParameterName.dbName")), //$NON-NLS-1$
     * PERL_SCHEMA_DB(Messages.getString("EParameterName.schemaDb")), //$NON-NLS-1$
     * PERL_USER(Messages.getString("EParameterName.user")), //$NON-NLS-1$
     * PERL_PASS(Messages.getString("EParameterName.password")), //$NON-NLS-1$
     * PERL_TABLE_STATS(Messages.getString("EParameterName.tableStats")), //$NON-NLS-1$
     * PERL_TABLE_LOGS(Messages.getString("EParameterName.tableLogs")), //$NON-NLS-1$
     * PERL_CATCH_RUNTIME_ERRORS(Messages.getString("EParameterName.catchRuntimeErrors")), //$NON-NLS-1$
     * PERL_CATCH_USER_ERRORS(Messages.getString("EParameterName.catchUserErrors")), //$NON-NLS-1$
     * PERL_CATCH_USER_WARNING(Messages.getString("EParameterName.catchUserWarning")), //$NON-NLS-1$
     * PERL_CATCH_REALTIME_STATS(Messages.getString("EParameterName.catchRealtimeStats")), //$NON-NLS-1$
     *
     * JAVA_ON_FILES_FLAG(Messages.getString("EParameterName.onFileFlag")), //$NON-NLS-1$
     * JAVA_FILE_PATH(Messages.getString("EParameterName.filePath")), //$NON-NLS-1$
     * JAVA_FILENAME_STATS(Messages.getString("EParameterName.fileNameStats")), //$NON-NLS-1$
     * JAVA_FILENAME_LOGS(Messages.getString("EParameterName.fileNameLogs")), //$NON-NLS-1$
     * JAVA_ON_DATABASE_FLAG(Messages.getString("EParameterName.onDatabaseFlag")), //$NON-NLS-1$
     * JAVA_DB_TYPE(Messages.getString("EParameterName.dbType")), //$NON-NLS-1$
     * JAVA_PROPERTY_TYPE(Messages.getString("EParameterName.propertyType")), //$NON-NLS-1$
     * JAVA_REPOSITORY_PROPERTY_TYPE("Repository"), //$NON-NLS-1$ Hidden parameter so no translation needed
     * JAVA_HOST(Messages.getString("EParameterName.host")), //$NON-NLS-1$
     * JAVA_PORT(Messages.getString("EParameterName.port")), //$NON-NLS-1$
     * JAVA_DBNAME(Messages.getString("EParameterName.dbName")), //$NON-NLS-1$
     * JAVA_SCHEMA_DB(Messages.getString("EParameterName.schemaDb")), //$NON-NLS-1$
     * JAVA_USER(Messages.getString("EParameterName.user")), //$NON-NLS-1$
     * JAVA_PASS(Messages.getString("EParameterName.password")), //$NON-NLS-1$
     * JAVA_TABLE_STATS(Messages.getString("EParameterName.tableStats")), //$NON-NLS-1$
     * JAVA_TABLE_LOGS(Messages.getString("EParameterName.tableLogs")), //$NON-NLS-1$
     * JAVA_CATCH_RUNTIME_ERRORS(Messages.getString("EParameterName.catchRuntimeErrors")), //$NON-NLS-1$
     * JAVA_CATCH_USER_ERRORS(Messages.getString("EParameterName.catchUserErrors")), //$NON-NLS-1$
     * JAVA_CATCH_USER_WARNING(Messages.getString("EParameterName.catchUserWarning")), //$NON-NLS-1$
     * JAVA_CATCH_REALTIME_STATS(Messages.getString("EParameterName.catchRealtimeStats")), //$NON-NLS-1$
     */

    // main tab within job setting.
    JOB_MAIN_NAME(Messages.getString("EParameterName.name")), //$NON-NLS-1$
    JOB_MAIN_AUTHOR(Messages.getString("EParameterName.author")), //$NON-NLS-1$
    JOB_MAIN_VERSION(Messages.getString("EParameterName.jobMainversion")), //$NON-NLS-1$
    JOB_MAIN_PURPOSE(Messages.getString("EParameterName.purpose")), //$NON-NLS-1$
    JOB_MAIN_STATUS(Messages.getString("EParameterName.status")), //$NON-NLS-1$
    JOB_MAIN_DESCRIPTION(Messages.getString("EParameterName.description")), //$NON-NLS-1$
    JOB_MAIN_CREATION(Messages.getString("EParameterName.creation")), //$NON-NLS-1$
    JOB_MAIN_MODIFICATION(Messages.getString("EParameterName.modification")), //$NON-NLS-1$

    // version tab within job setting.
    JOB_VERSION(Messages.getString("EParameterName.jobVersion")), //$NON-NLS-1$

    PARALLELIZE(Messages.getString("EParameterName.parallelize")), //$NON-NLS-1$
    PARALLELIZE_NUMBER(Messages.getString("EParameterName.parallelizeNumber")), //$NON-NLS-1$
    PARALLELIZE_UNIT_SIZE(Messages.getString("EParameterName.parallelizeUnitSize")), //$NON-NLS-1$
    PARALLELIZE_KEEP_EMPTY(""), //$NON-NLS-1$

    // for process
    IMPLICITCONTEXT_USE_PROJECT_SETTINGS(Messages.getString("Extra.UseProjectSettings")), //$NON-NLS-1$
    STATANDLOG_USE_PROJECT_SETTINGS(Messages.getString("StatsAndLogs.UseProjectSettings")), //$NON-NLS-1$
    // for project
    STATS_DEFAULT_PROJECTSETTING(Messages.getString("EParameterName.StatDefaultProjectsetting")), //$NON-NLS-1$
    IMPLICT_DEFAULT_PROJECTSETTING(Messages.getString("EParameterName.ImplictDefaultProjectsetting")), //$NON-NLS-1$

    // BASED_ON_INPUT_SCHEMAS
    BASED_ON_INPUT_SCHEMAS("BASED_ON_INPUT_SCHEMAS"), //$NON-NLS-1$
    KEEP_CDC_DATAS(Messages.getString("EParameterName.KeepCDCDatasTitle")), //$NON-NLS-1$
    VALUES("VALUES"), //$NON-NLS-1$
    JAVA_LIBRARY_PATH("JAVA_LIBRARY_PATH"), //$NON-NLS-1$

    // for tloop
    FORLOOP("For"), //$NON-NLS-1$

    // for validation rule
    VALIDATION_RULES(Messages.getString("EParameterName.validationRules")), //$NON-NLS-1$
    VALIDATION_RULE_TYPE(Messages.getString("EParameterName.validationRuleType")), //$NON-NLS-1$
    REPOSITORY_VALIDATION_RULE_TYPE("Repository"), //$NON-NLS-1$ Hidden parameter so no translation needed

    // tAggregateRow
    GROUPBYS("GROUPBYS"), //$NON-NLS-1$
    OPERATIONS("OPERATIONS"), //$NON-NLS-1$

    // Route Resource Type
    ROUTE_RESOURCE_TYPE("ROUTE_RESOURCE_TYPE"), //$NON-NLS-1$
    ROUTE_RESOURCE_TYPE_VERSION("Version"), //$NON-NLS-1$

    // Route Component Type
    ROUTE_COMPONENT_TYPE("ROUTE_COMPONENT_TYPE"), //$NON-NLS-1$

    // Route Breakpoint Type
    ROUTE_BREAKPOINT_LANGUAGES("Languages"), //$NON-NLS-1$
    ROUTE_BREAKPOINT_EXPRESSION("Expression"), //$NON-NLS-1$
    ROUTE_BREAKPOINT_EXAMPLE("Example"), //$NON-NLS-1$
    
    // Automatic Parlization
    PARTITIONER(Messages.getString("Connection.paral_partitioner")), //$NON-NLS-1$
    DEPARTITIONER(Messages.getString("Connection.paral_departitioner")), //$NON-NLS-1$
    REPARTITIONER(Messages.getString("Connection.paral_repartitioner")), //$NON-NLS-1$
    NONE(Messages.getString("Connection.paral_none")), //$NON-NLS-1$
    QUEUE_SIZE(Messages.getString("Connection.paral_queuesize")), //$NON-NLS-1$
    NUM_PARTITIONS("NUM_PARTITIONS"), //$NON-NLS-1$
    DEPART_QUEUE_SIZE("DEPART_QUEUE_SIZE"), //$NON-NLS-1$
    PART_QUEUE_SIZE("PART_QUEUE_SIZE"), //$NON-NLS-1$

    SUBTREE_START("SUBTREE_START"), //$NON-NLS-1$
    END_OF_FLOW("END_OF_FLOW"), //$NON-NLS-1$

    // tFlowToIterator
    DEFAULT_MAP("DEFAULT_MAP"), //$NON-NLS-1$

    HADOOP_CUSTOM_JARS("HADOOP_CUSTOM_JARS"), //$NON-NLS-1$

    // Added for Hadoop properties
    USE_HADOOP_PROPERTIES("USE_HADOOP_PROPERTIES"), //$NON-NLS-1$
    HADOOP_ADVANCED_PROPERTIES("HADOOP_ADVANCED_PROPERTIES"), //$NON-NLS-1$

    // Added for spark properties
    USE_SPARK_ADVANCED_PROPERTIES("USE_SPARK_ADVANCED_PROPERTIES"), //$NON-NLS-1$
    SPARK_ADVANCED_PROPERTIES("SPARK_ADVANCED_PROPERTIES"), //$NON-NLS-1$

    // Added for log4j.xml
    LOG4J_ACTIVATE("LOG4J_ACTIVATE"), //$NON-NLS-1$
    // corresponds to whether log4j2 version is chosen or not
    LOG4J2_ACTIVATE("LOG4J2_ACTIVATE"), //$NON-NLS-1$
    HMAP_PATH("HMAP_PATH"), //$NON-NLS-1$

    WINDOW_DURATION("WINDOW_DURATION"), //$NON-NLS-1$
    DEFINE_SLIDE_DURATION("DEFINE_SLIDE_DURATION"), //$NON-NLS-1$
    DEFINE_DURATION("DEFINE_DURATION"), //$NON-NLS-1$
    STREAMING_DURATION("STREAMING_DURATION"), //$NON-NLS-1$
    STREAMING_BATCH_SIZE("STREAMING_BATCH_SIZE"), //$NON-NLS-1$

    REPAINT("REPAINT"), //$NON-NLS-1$

    // for automap algorithms
    LEVENSHTEIN("LEVENSHTEIN"), //$NON-NLS-1$
    JACCARD("JACCARD"), //$NON-NLS-1$

    UNIFIED_COMPONENTS("Database"),

    ACTIVE_DATABASE_DELIMITED_IDENTIFIERS("Active database delimited identifiers"), //$NON-NLS-1$

    // for tDataprepRun
    PREPARATION_ID("PREPARATION_ID");

    private String displayName;

    EParameterName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return this.toString();
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
