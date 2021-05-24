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
 * Class represents a statement which has to be executed within the bulk import
 * @author Jan Lolling jan.lolling@cimt-ag.de
 */
package org.talend.database.exasol.imp;

public class BulkExecStatement {

	private String sql;
	private boolean isDMLStatement;

	/**
	 * Create the statement
	 * @param sql SQL code
	 * @param isDML flag if the statement is a DML statement, this affects the retrieval counter method.
	 */
	public BulkExecStatement(String sql, boolean isDML) {
		if (sql == null || sql.trim().isEmpty()) {
			throw new IllegalArgumentException("sql cannot be null or emtpy!");
		}
		this.sql = sql;
		this.isDMLStatement = isDML;
	}

	public String getSql() {
		return sql;
	}

	public boolean isDMLStatement() {
		return isDMLStatement;
	}

}
