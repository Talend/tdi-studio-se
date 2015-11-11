package org.talend.database.exasol;

public class BulkExecStatement {
	
	private String sql;
	private boolean isDMLStatement;
	
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
