// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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
package org.talend.database.exasol;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class EXABulkUtil {
	
	private static Logger logger = Logger.getLogger(EXABulkUtil.class);
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
	private String remoteFileUrl = null;
	private String remoteFileUser = null;
	private String remoteFilePassword = null;
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
	private List<Column> columns = new ArrayList<Column>();
	private Connection connection;
	private int countAffectedRows = 0;
	private String errorTable = null;
	private boolean errorTableWithCurrentTimestamp = false;
	private String localErrorFile = null;
	private boolean localErrorFileWithCurrentTimestamp = false;
	private Integer errorRejectLimit = null;
	
	public void setDebug(boolean debug) {
		if (debug) {
			logger.setLevel(Level.DEBUG);
		} else {
			logger.setLevel(Level.INFO);
		}
	}
	
	public void addCSVColumn(String dbColumnName, Integer sourceIndex, String format) {
		if (sourceIndex == null) {
			sourceIndex = columns.size();
		}
		Column c = Column.getCSVColumn(dbColumnName, sourceIndex, translateDateFormat(format));
		columns.add(c);
	}
	
	private void addStatement(String sql, boolean isDML) {
		if (isNotEmpty(sql)) {
			statements.add(new BulkExecStatement(sql, isDML));
		}
	}
	
	public int executeImport() throws Exception {
		generateImportStatements();
		if (connection == null) {
			throw new IllegalStateException("Connection not set!");
		}
		for (BulkExecStatement bs : statements) {
			try {
				Statement stat = connection.createStatement();
				if (logger.isDebugEnabled()) {
					logger.debug(bs.getSql());
				}
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
	}
	
	/**
	 * generates the statements necessary to run an IMPORT
	 */
	private void generateImportStatements() {
		buildNLSFormatStatement();
		buildImportStatement();
	}
	
	private void buildImportStatement() {
		if (columns.isEmpty() == false) {
			// sort the column list according to the index in the source file
			Collections.sort(columns);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("IMPORT INTO ");
		if (table == null) {
			throw new IllegalStateException("No table set!");
		}
		sql.append(table);
		sql.append(" ");
		sql.append(buildDBColumnList());
		sql.append("\nFROM ");
		if (localFilePath != null) {
			sql.append(buildLocalFile());
		} else if (remoteFileUrl != null) {
			// TODO
		} else if (dbmsSourceType != null) {
			// TODO
		}
		sql.append(buildErrorDestination());
		sql.append(buildErrorLimit());
		addStatement(sql.toString(), true);
	}
	
	private String buildDBColumnList() {
		if (columns.isEmpty() == false) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < columns.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(columns.get(i).getName());
			}
			sb.append(") ");
			return sb.toString();
		} else {
			return "";
		}
	}
	
	private String buildFileColumnList() {
		if (columns.isEmpty() == false) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < columns.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				Column c = columns.get(i);
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
		return (value != null) && (value.trim().isEmpty() == false);
	}

	/**
	 * set the schema.table name here.
	 * @param table
	 */
	public void setTable(String table) {
		if (isNotEmpty(table)) {
			this.table = table.trim().toUpperCase();
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
		if (isNotEmpty(fileOptColumnEnclosure)) {
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
			dateFormat = dateFormat.replace("dd", "DD");
			dateFormat = dateFormat.replace("mm", "MI");
			dateFormat = dateFormat.replace("ss", "SS");
			dateFormat = dateFormat.replace("sss", "ff3");
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
	 * @param defaultNumericCharacters
	 */
	public void setDefaultNumericCharacters(String defaultGroupSeparator, String defaultDecimalPoint) {
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
			this.dbmsSourceType = dbmsSourceType.trim();
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

	public String getRemoteFileUrl() {
		return remoteFileUrl;
	}

	public void setRemoteFileUrl(String remoteFileUrl) {
		this.remoteFileUrl = remoteFileUrl;
	}

	public String getRemoteFileUser() {
		return remoteFileUser;
	}

	public void setRemoteFileUser(String remoteFileUser) {
		this.remoteFileUser = remoteFileUser;
	}

	public String getRemoteFilePassword() {
		return remoteFilePassword;
	}

	public void setRemoteFilePassword(String remoteFilePassword) {
		this.remoteFilePassword = remoteFilePassword;
	}
	
}