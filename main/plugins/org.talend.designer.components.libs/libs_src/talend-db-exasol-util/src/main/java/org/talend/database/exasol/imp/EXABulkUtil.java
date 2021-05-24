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

/**
 * Executes EXASol bulk import and export commands
 * @author jlolling jan.lolling@cimt-ag.de
 */
package org.talend.database.exasol.imp;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class provides an IMPORT command for the EXASol database.
 * @author Jan Lolling, jan.lolling@cimt-ag.de
 */
public class EXABulkUtil {

	private static Logger logger = LoggerFactory.getLogger(EXABulkUtil.class);
	public static final String CSV = "CSV";
	public static final String FBV = "FBV";
	public static final String ORA = "ORA";
	public static final String EXA = "EXA";
	public static final String JDBC = "JDBC";
	public static final String TRIM = "TRIM";
	public static final String LTRIM = "LTRIM";
	public static final String RTRIM = "RTRIM";
	private String table = null;
	private boolean transferSecure = false;
	private String fileType = CSV;
	private String localFilePath = null;
	private String fileOptEncoding = "UTF8";
	private Integer fileOptSkip = null;
	private String fileOptNullIdentifier = null;
	private String fileOptRowSeparator = null;
	private String fileOptColumnSeparator = null;
	private String fileOptColumnEnclosure = null;
	private String fileOptTrim = null;
	private String defaultDateFormat = "YYYY-MM-DD";
	private String defaultTimestampFormat = "YYYY-MM-DD HH:MI:SS.ff3";
	private String defaultNumericCharacters = ",."; // first is group separator, second is decimal point
	private String dbmsSourceType = null;
	private List<BulkExecStatement> statements = new ArrayList<BulkExecStatement>();
	private List<Column> targetColumns = new ArrayList<Column>();
	private List<Column> remoteSourceColumns = new ArrayList<Column>();
	private Connection connection;
	private int countAffectedRows = 0;
	private String errorTable = null;
	private boolean errorTableWithCurrentTimestamp = false;
	private String localErrorFile = null;
	private boolean localErrorFileWithCurrentTimestamp = false;
	private Integer errorRejectLimit = null;
	private String remoteExistingFileConnectionName = null;
	private String remoteExistingDBConnectionName = null;
	private String remoteJdbcDriverClass = null;
	private String remoteDbmsUrl = null;
	private String remoteFileUrl = null;
	private String remoteFileName = null;
	private String remoteFileUrlParameters = null;
	private String remoteUser = null;
	private String remotePassword = null;
	private String remoteSourceTable = null;
	private String remoteSourceSelect = null;
	private int sourceIdentifierCase = 0; // 0 = unchanged, 1 = lower case, 2 = upper case
	private boolean onlyBuildSQLCode = false;

	private String createNumberFormat(Integer length, Integer precision, boolean hasGroups) {
        if (length != null && length.intValue() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = length - 1; i >= 0; i--) {
            	if(hasGroups && i < length - 1 && i > 0 && (i % 3 == 2)) {
            		sb.append("G");
            	}
            	sb.append("9");
            }
            if (precision != null && precision.intValue() > 0) {
                sb.append("D");
                for (int i = 0; i < precision.intValue(); i++) {
                    sb.append("9");
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public void addCSVNumberColumn(String dbColumnName, Integer sourceIndex, Integer length, Integer precision, String format) {
        addCSVNumberColumn(dbColumnName, sourceIndex, length, precision, format, true);
    }

    public void addCSVNumberColumn(String dbColumnName, Integer sourceIndex, Integer length, Integer precision, String format, boolean hasGroups) {
        if (sourceIndex == null) {
            sourceIndex = targetColumns.size();
        }
        if (format == null || format.trim().isEmpty()) {
            format = createNumberFormat(length, precision, hasGroups);
        }
        Column c = Column.getCSVColumn(dbColumnName, sourceIndex, format);
        targetColumns.add(c);
    }

	public void addCSVDateColumn(String dbColumnName, Integer sourceIndex, String format) {
		if (sourceIndex == null) {
			sourceIndex = targetColumns.size();
		}
		Column c = Column.getCSVColumn(dbColumnName, sourceIndex, translateDateFormat(format));
		targetColumns.add(c);
	}

	public void addCSVColumn(String dbColumnName, Integer sourceIndex) {
		if (sourceIndex == null) {
			sourceIndex = targetColumns.size();
		}
		Column c = Column.getCSVColumn(dbColumnName, sourceIndex, null);
		targetColumns.add(c);
	}

	public void addRemoteSourceTableColumn(String targetColumnName, String sourceColumnName, Integer sourceIndex) {
		if (sourceIndex == null) {
			sourceIndex = remoteSourceColumns.size();
		}
		if (isEmpty(sourceColumnName)) {
			sourceColumnName = targetColumnName;
		}
		switch (sourceIdentifierCase) {
		case 1:
			sourceColumnName = sourceColumnName.toLowerCase();
			break;
		case 2:
			sourceColumnName = sourceColumnName.toUpperCase();
			break;
		}
		Column cs = Column.getDbmsColumn(sourceColumnName, sourceIndex);
		remoteSourceColumns.add(cs);
		Column ct = Column.getDbmsColumn(targetColumnName, sourceIndex);
		targetColumns.add(ct);
	}

	private void addStatement(String sql, boolean isDML) {
		if (isNotEmpty(sql)) {
			statements.add(new BulkExecStatement(sql, isDML));
			if (onlyBuildSQLCode && logger.isDebugEnabled()) {
				logger.debug("SQL: " + sql);
			}
		}
	}

	public int executeImport() throws Exception {
		generateImportStatements();
		if (onlyBuildSQLCode == false) {
			if (connection == null) {
				throw new IllegalStateException("Connection not set!");
			}
			for (BulkExecStatement bs : statements) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Execute: " + bs.getSql());
					}
					Statement stat = connection.createStatement();
					stat.execute(bs.getSql());
					if (bs.isDMLStatement()) {
						countAffectedRows = countAffectedRows + stat.getUpdateCount();
					}
					stat.close();
				} catch (SQLException sqle) {
					throw new Exception("Execute statement:\n" + bs.getSql() + "\nfailed: " + sqle.getMessage(), sqle);
				}
			}
			return countAffectedRows;
		} else {
			return 0;
		}
	}

	/**
	 * generates the statements necessary to run an IMPORT
	 */
	private void generateImportStatements() {
		if (onlyBuildSQLCode && logger.isDebugEnabled()) {
			logger.debug("-- ############## begin script ##################");
		}
		buildNLSFormatStatement();
		buildImportStatement();
		if (onlyBuildSQLCode && logger.isDebugEnabled()) {
			logger.debug("-- ############## end script ##################\n\n");
		}
	}

	private void buildImportStatement() {
		if (targetColumns.isEmpty() == false) {
			// sort the column list according to the index in the source file
			Collections.sort(targetColumns);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("IMPORT INTO ");
		if (table == null) {
			throw new IllegalStateException("No target table set!");
		}
		sql.append(table);
		sql.append(" ");
		sql.append(buildTargetDBColumnList());
		sql.append("\nFROM ");
		if (localFilePath != null) {
			// read the file from the local file system
			sql.append(buildLocalFile());
		} else if (remoteFileUrl != null || remoteExistingFileConnectionName != null) {
			// read the file from a web service
			sql.append(buildRemoteFile());
		} else if (dbmsSourceType != null || remoteExistingDBConnectionName != null) {
			// use an remote dbms as source
			sql.append(buildRemoteDbmsSource());
		} else {
			throw new IllegalStateException("No data source set. You have to setup as source local or remote fil or a remote database!");
		}
		sql.append(buildErrorDestination());
		sql.append(buildErrorLimit());
		addStatement(sql.toString(), true);
	}

	private String buildRemoteFile() {
		// build an remote connection
		StringBuilder sql = new StringBuilder();
		sql.append(CSV);
		sql.append(" AT ");
		if (remoteExistingFileConnectionName != null) {
			sql.append(remoteExistingFileConnectionName);
		} else {
			sql.append("'");
			sql.append(remoteFileUrl);
			sql.append("'");
		}
		// credentials can also applied to existing connections
		if (remoteUser != null) {
			sql.append(" USER '");
			sql.append(remoteUser);
			sql.append("' ");
			if (remotePassword != null) {
				sql.append("IDENTIFIED BY '");
				sql.append(remotePassword);
				sql.append("' ");
			} else {
				throw new IllegalStateException("Password not set for remote connection!");
			}
		}
		if (remoteFileName == null) {
			throw new IllegalStateException("Remote file name not set!");
		}
		sql.append("\nFILE '");
		sql.append(remoteFileName);
		if (remoteFileUrlParameters != null) {
			sql.append("?");
			sql.append(remoteFileUrlParameters);
		}
		sql.append("' ");
		sql.append(buildFileColumnList());
		sql.append(buildFileOpts());
		return sql.toString();
	}

	private String buildRemoteDbmsSource() {
		// build an remote connection
		StringBuilder sql = new StringBuilder();
		if (EXA.equalsIgnoreCase(dbmsSourceType)) {
			sql.append(EXA);
			sql.append(" AT ");
			if (remoteExistingDBConnectionName != null) {
				sql.append(remoteExistingDBConnectionName);
			} else {
				if (remoteDbmsUrl == null) {
					throw new IllegalStateException("URL for remote database not set!");
				}
				sql.append("'");
				sql.append(remoteDbmsUrl);
				sql.append("'");
			}
		} else if (ORA.equalsIgnoreCase(dbmsSourceType)) {
			sql.append(ORA);
			sql.append(" AT ");
			if (remoteExistingDBConnectionName != null) {
				sql.append(remoteExistingDBConnectionName);
			} else {
				if (remoteDbmsUrl == null) {
					throw new IllegalStateException("URL for remote database not set!");
				}
				sql.append("'");
				sql.append(remoteDbmsUrl);
				sql.append("'");
			}
		} else if (JDBC.equalsIgnoreCase(dbmsSourceType)) {
			sql.append(JDBC);
			if (remoteJdbcDriverClass != null) {
				sql.append(" DRIVER='");
				sql.append(remoteJdbcDriverClass);
				sql.append("'");
			}
			sql.append(" AT ");
			if (remoteExistingDBConnectionName != null) {
				sql.append(remoteExistingDBConnectionName);
			} else {
				if (remoteDbmsUrl == null) {
					throw new IllegalStateException("URL for remote database not set!");
				}
				sql.append("'");
				sql.append(remoteDbmsUrl);
				sql.append("'");
			}
		}
		// credentials can also applied to existing connections
		if (remoteUser != null) {
			sql.append(" USER '");
			sql.append(remoteUser);
			sql.append("'");
			if (remotePassword != null) {
				sql.append(" IDENTIFIED BY '");
				sql.append(remotePassword);
				sql.append("'");
			} else {
				throw new IllegalStateException("Password not set for remote connection!");
			}
		}
		if (remoteSourceSelect != null) {
			sql.append(" STATEMENT '");
			sql.append(remoteSourceSelect); // escape the single quotas
			sql.append("'");
		} else if (remoteSourceTable != null) {
			sql.append(" TABLE ");
			sql.append(remoteSourceTable);
			sql.append(" ");
			sql.append(buildSourceDBColumnList());
		} else {
			throw new IllegalStateException("You use a remote database as source and have not set a source table or query!");
		}
		return sql.toString();
	}

	private String buildTargetDBColumnList() {
		if (targetColumns.isEmpty() == false) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < targetColumns.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(targetColumns.get(i).getName());
			}
			sb.append(") ");
			return sb.toString();
		} else {
			return "";
		}
	}

	private String buildSourceDBColumnList() {
		if (remoteSourceColumns.isEmpty() == false) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < remoteSourceColumns.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(remoteSourceColumns.get(i).getName());
			}
			sb.append(") ");
			return sb.toString();
		} else {
			return "";
		}
	}

	private String buildFileColumnList() {
		if (targetColumns.isEmpty() == false) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < targetColumns.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				Column c = targetColumns.get(i);
				sb.append(c.getSourceIndex() + 1);
				if (c.getFormat() != null) {
					sb.append(" FORMAT='");
					sb.append(c.getFormat());
					sb.append("'");
				}
			}
			sb.append(")\n");
			return sb.toString();
		} else {
			return "";
		}
	}

	private void buildNLSFormatStatement() {
		if (defaultNumericCharacters != null) {
			StringBuilder sql = new StringBuilder();
			sql.append("alter session set NLS_NUMERIC_CHARACTERS='");
			sql.append(defaultNumericCharacters);
			sql.append("'");
			addStatement(sql.toString(), false);
		}
		if (defaultTimestampFormat != null) {
			StringBuilder sql = new StringBuilder();
			sql.append("alter session set NLS_TIMESTAMP_FORMAT='");
			sql.append(defaultTimestampFormat);
			sql.append("'");
			addStatement(sql.toString(), false);
		}
		if (defaultDateFormat != null) {
			StringBuilder sql = new StringBuilder();
			sql.append("alter session set NLS_DATE_FORMAT='");
			sql.append(defaultDateFormat);
			sql.append("'");
			addStatement(sql.toString(), false);
		}
	}

	private String buildLocalFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("LOCAL ");
		if (transferSecure) {
			sb.append("SECURE ");
		}
		if (fileType.equals(CSV)) {
			sb.append(CSV);
			sb.append(" ");
		} else if (fileType.equals(FBV)) {
			sb.append(FBV);
			sb.append(" ");
		} else {
			throw new IllegalStateException("Unknown file type: " + fileType);
		}
		sb.append("FILE ");
		if (fileType == null) {
			throw new IllegalStateException("File type not set. CSV or FBV expected!");
		}
		if (localFilePath == null) {
			throw new IllegalStateException("Local file path must set!");
		}
		sb.append("'");
		sb.append(localFilePath);
		sb.append("' ");
		sb.append(buildFileColumnList());
		sb.append(buildFileOpts());
		return sb.toString();
	}

	private String buildFileOpts() {
		StringBuilder sb = new StringBuilder();
		if (fileOptEncoding != null) {
			sb.append("ENCODING='");
			sb.append(fileOptEncoding);
			sb.append("'\n");
		}
		if (fileOptSkip != null) {
			sb.append("SKIP=");
			sb.append(String.valueOf(fileOptSkip));
			sb.append("\n");
		}
		if (fileOptNullIdentifier != null) {
			sb.append("NULL='");
			sb.append(fileOptNullIdentifier);
			sb.append("'\n");
		}
		if (fileOptRowSeparator != null) {
			sb.append("ROW SEPARATOR='");
			sb.append(fileOptRowSeparator);
			sb.append("'\n");
		}
		if (fileOptColumnSeparator != null) {
			sb.append("COLUMN SEPARATOR='");
			sb.append(fileOptColumnSeparator);
			sb.append("'\n");
		}
		if (fileOptColumnEnclosure != null) {
			sb.append("COLUMN DELIMITER='");
			sb.append(fileOptColumnEnclosure);
			sb.append("'\n");
		}
		if (fileOptTrim != null) {
			sb.append(fileOptTrim);
			sb.append("\n");
		}
		return sb.toString();
	}

	private String buildErrorDestination() {
		if (localErrorFile != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("ERRORS INTO LOCAL ");
			if (transferSecure) {
				sb.append("SECURE ");
			}
			sb.append("CSV FILE '");
			sb.append(localErrorFile);
			sb.append("'\n");
			return sb.toString();
		} else if (errorTable != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("ERRORS INTO ");
			sb.append(errorTable);
			sb.append(" ");
			if (errorTableWithCurrentTimestamp) {
				sb.append("(CURRENT_TIMESTAMP) ");
			}
			sb.append("\n");
			return sb.toString();
		} else {
			return "";
		}
	}

	private String buildErrorLimit() {
		StringBuilder sb = new StringBuilder();
		if (errorRejectLimit != null && errorRejectLimit > 0) {
			sb.append("REJECT LIMIT ");
			sb.append(errorRejectLimit);
			sb.append(" ");
		}
		return sb.toString();
	}

	public String getTable() {
		return table;
	}

	private boolean isNotEmpty(String value) {
		return (value != null) && (value.trim().isEmpty() == false) && ("\"\"".equals(value) == false) && ("null".equals(value) == false);
	}

	private boolean isEmpty(String value) {
		return isNotEmpty(value) == false;
	}

	/*
	 * check if the string is ""
	 */
	private boolean isEmptyString(String value) {
	    return value!=null && "".equals(value);
	}

	/**
	 * set the schema.table name here.
	 * @param table
	 */
	public void setTable(String table) {
		if (isNotEmpty(table)) {
			this.table = table.trim();
		} else {
			throw new IllegalArgumentException("Table name cannot be empty of null!");
		}
	}

	public boolean isTransferSecure() {
		return transferSecure;
	}

	/**
	 * is true the transfer will be done in secure way
	 * @param transferSecure
	 */
	public void setTransferSecure(Boolean transferSecure) {
		if (transferSecure != null) {
			this.transferSecure = transferSecure.booleanValue();
		}
	}

	public String getFileType() {
		return fileType;
	}

	/**
	 * Set the file type here: CSV and FBV are allowed here
	 * @param fileType
	 */
	public void setFileType(String fileType) {
		if (isNotEmpty(fileType)) {
			this.fileType = fileType;
		}
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	/**
	 * Local file path for import or export
	 * The path will be used from the JDBC driver on the local file system
	 * @param localFilePath
	 */
	public void setLocalFilePath(String localFilePath) throws Exception {
		if (isNotEmpty(localFilePath)) {
			File f = new File(localFilePath);
			if (f.exists()) {
				this.localFilePath = localFilePath;
			} else {
				throw new Exception("Local file: " + localFilePath + " does not exists!");
			}
		}
	}

	/**
	 * Set here the character set used to encode/decode the text files
	 * @param fileOptEncoding
	 */
	public void setFileOptEncoding(String fileOptEncoding) {
		if (isNotEmpty(fileOptEncoding)) {
			this.fileOptEncoding = fileOptEncoding;
		}
	}

	/**
	 * Set here the number of lines in the file to skip
	 * @param fileOptSkip
	 */
	public void setFileOptSkip(Integer fileOptSkip) {
		this.fileOptSkip = fileOptSkip;
	}

	public void setFileOptNullIdentifier(String fileOptNullIdentifier) {
		if (isNotEmpty(fileOptNullIdentifier)) {
			this.fileOptNullIdentifier = fileOptNullIdentifier;
		}
	}

	public String getFileOptRowSeparator() {
		return fileOptRowSeparator;
	}

	/**
	 * Set here the row separator.
	 * The typical java escaped chars are translated into the EXASol abbreviations.
	 * If not set UNIX format is used.
	 * @param fileOptRowSeparator
	 */
	public void setFileOptRowSeparator(String fileOptRowSeparator) {
		if (isNotEmpty(fileOptRowSeparator)) {
			fileOptRowSeparator = fileOptRowSeparator.trim();
			if ("\n".equals(fileOptRowSeparator)) {
				fileOptRowSeparator = "LF";
			} else if ("\t".equals(fileOptRowSeparator)) {
				fileOptRowSeparator = "TAB";
			} else if ("\r\n".equals(fileOptRowSeparator)) {
				fileOptRowSeparator = "CRLF";
			} else if ("\r".equals(fileOptRowSeparator)) {
				fileOptRowSeparator = "CR";
			}
			this.fileOptRowSeparator = fileOptRowSeparator;
		}
	}

	public String getFileOptColumnSeparator() {
		return fileOptColumnSeparator;
	}

	public void setFileOptColumnSeparator(String fileOptColumnSeparator) {
		if (isNotEmpty(fileOptColumnSeparator)) {
			this.fileOptColumnSeparator = fileOptColumnSeparator;
		}
	}

	public String getFileOptColumnEnclosure() {
		return fileOptColumnEnclosure;
	}

	public void setFileOptColumnEnclosure(String fileOptColumnEnclosure) {
		if (isEmptyString(fileOptColumnEnclosure) || isNotEmpty(fileOptColumnEnclosure)) {
			this.fileOptColumnEnclosure = fileOptColumnEnclosure.trim();
		}
	}

	public String getFileOptTrim() {
		return fileOptTrim;
	}

	public void setFileOptTrim(String fileOptTrim) {
		if (isNotEmpty(fileOptTrim)) {
			this.fileOptTrim = fileOptTrim.trim();
		}
	}

	public String getDefaultDateFormat() {
		return defaultDateFormat;
	}

	private String translateDateFormat(String dateFormat) {
		if (dateFormat != null) {
			dateFormat = dateFormat.replace("yyyy", "YYYY");
			dateFormat = dateFormat.replace("HH24", "HH");
			dateFormat = dateFormat.replace("HH", "HH24");
			dateFormat = dateFormat.replace("dd", "DD");
			dateFormat = dateFormat.replace("mm", "MI");
			dateFormat = dateFormat.replace("ss", "SS");
			dateFormat = dateFormat.replace("SSS", "FF3");
			dateFormat = dateFormat.replace("SSSSSS", "FF6");
			return dateFormat.trim();
		} else {
			return null;
		}
	}

	/**
	 * Set here the default date format.
	 * We need here the SQL standard formats.
	 * Any Java formats will be translated into the SQL format (for year, day)
	 * @param defaultDateFormat
	 */
	public void setDefaultDateFormat(String defaultDateFormat) {
		if (isNotEmpty(defaultDateFormat)) {
			this.defaultDateFormat = translateDateFormat(defaultDateFormat);
		}
	}

	public String getDefaultTimestampFormat() {
		return defaultTimestampFormat;
	}

	/**
	 * Set here the default timestamp format.
	 * We need here the SQL standard formats.
	 * Any Java formats will be translated into the SQL format (for year, day, minute, second and millisecond)
	 * @param defaultDateFormat
	 */
	public void setDefaultTimestampFormat(String defaultTimestampFormat) {
		if (isNotEmpty(defaultTimestampFormat)) {
			this.defaultTimestampFormat = translateDateFormat(defaultTimestampFormat);
		}
	}

	public String getDefaultNumericCharacters() {
		return defaultNumericCharacters;
	}

	/**
	 * Set here the numeric characters for group separator and decimal point as one String
	 * @param defaultDecimalPoint
	 * @param defaultGroupSeparator
	 */
	public void setDefaultNumericCharacters(String defaultDecimalPoint, String defaultGroupSeparator) {
		if (isNotEmpty(defaultGroupSeparator)) {
			this.defaultNumericCharacters = defaultGroupSeparator.trim();
		} else {
			this.defaultNumericCharacters = ",";
		}
		if (isNotEmpty(defaultDecimalPoint)) {
			this.defaultNumericCharacters += defaultDecimalPoint.trim();
		} else {
			this.defaultNumericCharacters += ".";
		}
	}

	public String getDbmsSourceType() {
		return dbmsSourceType;
	}

	public void setDbmsSourceType(String dbmsSourceType) {
		if (isNotEmpty(dbmsSourceType)) {
			dbmsSourceType = dbmsSourceType.trim();
			if (EXA.equalsIgnoreCase(dbmsSourceType) || ORA.equalsIgnoreCase(dbmsSourceType) || JDBC.equalsIgnoreCase(dbmsSourceType)) {
				this.dbmsSourceType = dbmsSourceType;
			} else {
				throw new IllegalArgumentException("Invalid source database type: " + dbmsSourceType + ", only EXA, ORA or JDBC are allowed");
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null!");
		}
		this.connection = connection;
	}

	public String getErrorTable() {
		return errorTable;
	}

	public void setErrorTable(String errorTable) {
		if (isNotEmpty(errorTable)) {
			this.errorTable = errorTable.trim().toUpperCase();
		}
	}

	public String getLocalErrorFile() {
		return localErrorFile;
	}

	public void setLocalErrorFile(String errorFile) {
		if (isNotEmpty(errorFile)) {
			errorFile = errorFile.trim();
			if (localErrorFileWithCurrentTimestamp) {
				int pos = errorFile.lastIndexOf(".");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				if (pos > 0) {
					this.localErrorFile = errorFile.substring(0, pos) + "_" + sdf.format(new Date()) + errorFile.substring(pos);
				} else {
					this.localErrorFile = errorFile + "_" + sdf.format(new Date());
				}
			} else {
				this.localErrorFile = errorFile;
			}
		}
	}

	public Integer getErrorRejectLimit() {
		return errorRejectLimit;
	}

	public void setErrorRejectLimit(Integer errorRejectLimit) {
		this.errorRejectLimit = errorRejectLimit;
	}

	public boolean isErrorTableWithCurrentTimestamp() {
		return errorTableWithCurrentTimestamp;
	}

	public void setErrorTableWithCurrentTimestamp(
			Boolean errorTableWithCurrentTimestamp) {
		if (errorTableWithCurrentTimestamp != null) {
			this.errorTableWithCurrentTimestamp = errorTableWithCurrentTimestamp;
		}
	}

	public boolean isLocalErrorFileWithCurrentTimestamp() {
		return localErrorFileWithCurrentTimestamp;
	}

	public void setLocalErrorFileWithCurrentTimestamp(
			Boolean localErrorFileWithCurrentTimestamp) {
		if (localErrorFileWithCurrentTimestamp != null) {
			this.localErrorFileWithCurrentTimestamp = localErrorFileWithCurrentTimestamp;
		}
	}

	public int getCountAffectedRows() {
		return countAffectedRows;
	}

	public void commitAndClose() {
		if (connection != null) {
			try {
				connection.commit();
			} catch (SQLException e1) {
				// ignore
			}
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public void rollbackAndClose() {
		if (connection != null) {
			try {
				if (connection.isClosed() == false) {
					connection.rollback();
				}
			} catch (SQLException e) {
				// ignore
			}
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public String getRemoteJdbcDriverClass() {
		return remoteJdbcDriverClass;
	}

	public void setRemoteJdbcDriverClass(String remoteJdbcDriverClass) {
		if (isNotEmpty(remoteJdbcDriverClass)) {
			this.remoteJdbcDriverClass = remoteJdbcDriverClass.trim();
		}
	}

	public String getRemoteDbmsUrl() {
		return remoteDbmsUrl;
	}

	public void setRemoteDbmsUrl(String remoteDbmsUrl) {
		if (isNotEmpty(remoteDbmsUrl)) {
			this.remoteDbmsUrl = remoteDbmsUrl.trim();
		}
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		if (isNotEmpty(remoteUser)) {
			this.remoteUser = remoteUser.trim();
		}
	}

	public String getRemotePassword() {
		return remotePassword;
	}

	public void setRemotePassword(String remotePassword) {
		if (isNotEmpty(remotePassword)) {
			this.remotePassword = remotePassword.trim();
		}
	}

	public String getRemoteExistingFileConnectionName() {
		return remoteExistingFileConnectionName;
	}

	public void setRemoteExistingFileConnectionName(String remoteConnectionName) {
		if (isNotEmpty(remoteConnectionName)) {
			this.remoteExistingFileConnectionName = remoteConnectionName.trim();
		}
	}

	public String getRemoteExistingDBConnectionName() {
		return remoteExistingDBConnectionName;
	}

	public void setRemoteExistingDBConnectionName(String remoteConnectionName) {
		if (isNotEmpty(remoteConnectionName)) {
			this.remoteExistingDBConnectionName = remoteConnectionName.trim();
		}
	}

	public String getRemoteSourceTable() {
		return remoteSourceTable;
	}

	public void setRemoteSourceTable(String remoteSchema, String remoteSourceTable) {
		String schema = null;
		if (isNotEmpty(remoteSchema)) {
			schema = remoteSchema.trim();
		}
		switch (sourceIdentifierCase) {
		case 1:
			remoteSourceTable = remoteSourceTable.toLowerCase();
			schema = schema.toLowerCase();
			break;
		case 2:
			remoteSourceTable = remoteSourceTable.toUpperCase();
			schema = schema.toUpperCase();
			break;
		}
		if (isNotEmpty(remoteSourceTable)) {
			if (schema != null) {
				this.remoteSourceTable = schema + "." + remoteSourceTable.trim();
			} else {
				this.remoteSourceTable = remoteSourceTable.trim();
			}
		}
	}

	public String getRemoteSourceSelect() {
		return remoteSourceSelect;
	}

	public void setRemoteSourceSelect(String remoteSourceSelect) {
		if (isNotEmpty(remoteSourceSelect)) {
			this.remoteSourceSelect = remoteSourceSelect.trim().replace("'", "''");
		}
	}

	public String getRemoteFileUrl() {
		return remoteFileUrl;
	}

	public void setRemoteFileUrl(String remoteFileUrl) {
		if (isNotEmpty(remoteFileUrl)) {
			this.remoteFileUrl = remoteFileUrl.trim();
		}
	}

	public String getRemoteFileUrlParameters() {
		return remoteFileUrlParameters;
	}

	public void setRemoteFileUrlParameters(String remoteFileUrlParameters) {
		if (isNotEmpty(remoteFileUrlParameters)) {
			this.remoteFileUrlParameters = remoteFileUrlParameters.trim();
		}
	}

	public String getRemoteFileName() {
		return remoteFileName;
	}

	public void setRemoteFileName(String remoteFileName) {
		if (isNotEmpty(remoteFileName)) {
			this.remoteFileName = remoteFileName.trim();
		}
	}

	public void convertRemoteIdentifiersInUpperCase() {
		sourceIdentifierCase = 2;
	}

	public void convertRemoteIdentifiersInLowerCase() {
		sourceIdentifierCase = 1;
	}

	public void doNotConvertRemoteIdentifiers() {
		sourceIdentifierCase = 0;
	}

	public boolean isOnlyBuildSQLCode() {
		return onlyBuildSQLCode;
	}

	public void setOnlyBuildSQLCode(boolean onlyBuildSQLCode) {
		this.onlyBuildSQLCode = onlyBuildSQLCode;
	}

	public void deleteErrorLogFileIfEmpty() {
		if (localErrorFile != null) {
			File f = new File(localErrorFile);
			if (f.exists() && f.length() == 0) {
				f.delete();
			}
		}
	}

}