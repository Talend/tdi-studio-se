package org.talend.database.exasol;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.talend.database.exasol.imp.EXABulkUtil;

public class TestEXABulkUtil {
	
	private Connection getConnection() throws Exception {
		String url = "jdbc:exa:172.16.214.128:8563;schema=test_data";
		java.lang.Class.forName("com.exasol.jdbc.EXADriver");
		return DriverManager.getConnection(url, "sys", "exasol");
	}
	
	@Before
	public void setup() {
		System.out.println("Setup Log4J");
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		System.out.println("########################");
	}
	
	
	@Test
	public void testImportLocalFile() throws Exception {
		System.out.println("testImportLocalFile");
		String table = "TEST_DATA.TEST_DATA_LOAD";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement truncateStat = connection.createStatement();
		truncateStat.execute("truncate table " + table);
		truncateStat.execute("truncate table " + errorTable);
		truncateStat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setTransferSecure(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setLocalFilePath("/Volumes/Data/Talend/testdata/exasol/test_data_load.csv");
		u.setFileOptColumnSeparator("|");
		u.setFileOptEncoding("UTF8");
		u.setFileOptRowSeparator("\n");
		u.setFileOptTrim("TRIM");
		u.setFileOptSkip(1);
		u.setFileOptNullIdentifier("NULL");
		u.setDefaultDateFormat("yyyy-MM-dd");
		u.addCSVColumn("ID", null);
		u.addCSVColumn("FIRSTNAME", null);
		u.addCSVColumn("LASTNAME", null);
		u.addCSVDateColumn("BIRTHDATE", null, "dd.MM.yyyy HH:mm:ss");
		u.addCSVColumn("CITY", null);
		u.addCSVColumn("GENDER", null);
		u.addCSVNumberColumn("AGE", null, null, null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(3);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 999998);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteFile() throws Exception {
		System.out.println("testImportRemoteFile");
		String table = "TEST_DATA.TEST_DATA_LOAD_3";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement truncateStat = connection.createStatement();
		try {
			truncateStat.execute("truncate table " + table);
			truncateStat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		truncateStat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setRemoteFileUrl("sftp://172.16.214.132/home/tisadmin/Downloads/");
		u.setRemoteFileName("test_data_load.csv");
		u.setRemoteFileUrlParameters("open=true;test=1");
		u.setRemoteUser("tisadmin");
		u.setRemotePassword("tisadmin");
		u.setFileOptColumnSeparator("|");
		u.setFileOptEncoding("UTF8");
		u.setFileOptRowSeparator("\n");
		u.setFileOptTrim("TRIM");
		u.setFileOptSkip(1);
		u.setFileOptNullIdentifier("NULL");
		u.setDefaultDateFormat("yyyy-MM-dd");
		u.addCSVColumn("ID", null);
		u.addCSVColumn("FIRSTNAME", null);
		u.addCSVColumn("LASTNAME", null);
		u.addCSVDateColumn("BIRTHDATE", null, "dd.MM.yyyy HH:mm:ss");
		u.addCSVColumn("CITY", null);
		u.addCSVColumn("GENDER", null);
		u.addCSVNumberColumn("AGE", null, null, null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(3);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 999998);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteMysql() throws Exception {
		System.out.println("testImportRemoteMysql");
		String table = "TEST_DATA.TEST_DATA_LOAD_1";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement truncateStat = connection.createStatement();
		try {
			truncateStat.execute("truncate table " + table);
			truncateStat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		truncateStat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setDbmsSourceType("JDBC");
		u.setRemoteDbmsUrl("jdbc:mysql://172.16.214.132:3306/mysql");
		u.setRemoteUser("tisadmin");
		u.setRemotePassword("tisadmin");
		u.setRemoteSourceTable(null, "\"test_data_load_1\"");
		u.addRemoteSourceTableColumn("ID", "id", null);
		u.addRemoteSourceTableColumn("firstname", null, null);
		u.addRemoteSourceTableColumn("lastname", null, null);
		u.addRemoteSourceTableColumn("birthdate", null, null);
		u.addRemoteSourceTableColumn("city", null, null);
		u.addRemoteSourceTableColumn("gender", null, null);
		u.addRemoteSourceTableColumn("age", null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 1000000);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteOracle() throws Exception {
		System.out.println("testImportRemoteOracle");
		String table = "TEST_DATA.TEST_DATA_LOAD_6";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement truncateStat = connection.createStatement();
		try {
			truncateStat.execute("truncate table " + table);
			truncateStat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		truncateStat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setDbmsSourceType("ORA");
		u.setRemoteDbmsUrl("jdbc:oracle:thin:@//172.16.214.132:1521/XE");
		u.setRemoteUser("DWH_STAGE");
		u.setRemotePassword("DWH");
		u.setRemoteSourceTable(null, "\"test_data_load\"");
		u.addRemoteSourceTableColumn("ID", "id", null);
		u.addRemoteSourceTableColumn("firstname", null, null);
		u.addRemoteSourceTableColumn("lastname", null, null);
		u.addRemoteSourceTableColumn("birthdate", null, null);
		u.addRemoteSourceTableColumn("city", null, null);
		u.addRemoteSourceTableColumn("gender", null, null);
		u.addRemoteSourceTableColumn("age", null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 1000000);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteMysqlConnection() throws Exception {
		System.out.println("testImportRemoteMysqlConnection");
		String table = "TEST_DATA.TEST_DATA_LOAD_5";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement stat = connection.createStatement();
		stat.execute("create or replace connection mysql_mysql to 'jdbc:mysql://172.16.214.132/mysql' user 'tisadmin' identified by 'tisadmin'");
		try {
			stat.execute("truncate table " + table);
			stat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		stat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setDbmsSourceType("JDBC");
		u.setRemoteExistingDBConnectionName("mysql_mysql");
		u.setRemoteSourceTable(null, "\"v_test_data_load\"");
		u.addRemoteSourceTableColumn("ID", "id", null);
		u.addRemoteSourceTableColumn("firstname", "first_name", null);
		u.addRemoteSourceTableColumn("lastname", "last_name", null);
		u.addRemoteSourceTableColumn("birthdate", null, null);
		u.addRemoteSourceTableColumn("city", null, null);
		u.addRemoteSourceTableColumn("gender", null, null);
		u.addRemoteSourceTableColumn("age", null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 1000000);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteMysqlConnectionAndStatement() throws Exception {
		System.out.println("testImportRemoteMysqlConnectionAndStatement");
		String table = "TEST_DATA.TEST_DATA_LOAD_4";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement stat = connection.createStatement();
		stat.execute("create or replace connection mysql_mysql to 'jdbc:mysql://172.16.214.132/mysql' user 'tisadmin' identified by 'tisadmin'");
		try {
			stat.execute("truncate table " + table);
			stat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		stat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setDbmsSourceType("JDBC");
		u.setRemoteExistingDBConnectionName("MYSQL_MYSQL");
		u.setRemoteSourceSelect("select\n"
			    + "id,\n"
			    + "firstname,\n"
			    + "lastname,\n"
			    + "birthdate,\n"
			    + "city,\n"
			    + "gender,\n"
			    + "age\n"
			    + "from test_data_load_1\n"
			    + "limit 10000");
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 10000);
		connection.close();
		System.out.println("########################");
	}

	@Test
	public void testImportRemoteExasol() throws Exception {
		System.out.println("testImportRemoteExasol");
		String table = "TEST_DATA.TEST_DATA_LOAD_2";
		String errorTable = table + "_ERROR_LOG";
		Connection connection = getConnection();
		Statement stat = connection.createStatement();
		try {
			stat.execute("truncate table " + table);
			stat.execute("truncate table " + errorTable);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		stat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setDbmsSourceType("EXA");
		u.setRemoteDbmsUrl("172.16.214.128");
		u.setRemoteUser("sys");
		u.setRemotePassword("exasol");
		u.setRemoteSourceTable("TEST_DATA", "TEST_DATA_LOAD_1");
		u.addRemoteSourceTableColumn("id", null, null);
		u.addRemoteSourceTableColumn("firstname", null, null);
		u.addRemoteSourceTableColumn("lastname", null, null);
		u.addRemoteSourceTableColumn("birthdate", null, null);
		u.addRemoteSourceTableColumn("city", null, null);
		u.addRemoteSourceTableColumn("gender", null, null);
		u.addRemoteSourceTableColumn("age", null, null);
		u.setErrorTable(errorTable);
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 1000000);
		connection.close();
		System.out.println("########################");
	}

}