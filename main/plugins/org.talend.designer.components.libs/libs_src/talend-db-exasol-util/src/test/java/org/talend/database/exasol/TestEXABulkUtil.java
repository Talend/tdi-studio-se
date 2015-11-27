package org.talend.database.exasol;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.talend.database.exasol.EXABulkUtil;

public class TestEXABulkUtil {
	
	private Connection getConnection() throws Exception {
		String url = "jdbc:exa:172.16.173.128:8563;schema=test_data";
		java.lang.Class.forName("com.exasol.jdbc.EXADriver");
		return DriverManager.getConnection(url, "sys", "exasol");
	}
	
	@Test
	public void testImport() throws Exception {
		BasicConfigurator.configure();
		String table = "TEST_DATA.TEST_DATA_LOAD";
		Connection connection = getConnection();
		Statement truncateStat = connection.createStatement();
		truncateStat.execute("truncate table " + table);
		truncateStat.close();
		EXABulkUtil u = new EXABulkUtil();
		u.setDebug(true);
		u.setConnection(connection);
		u.setTable(table);
		u.setLocalFilePath("/Volumes/Data/Talend/TalendDev/testdata/test_data_load.csv");
		u.setFileOptColumnEnclosure("\"");
		u.setFileOptColumnSeparator(";");
		u.setFileOptEncoding("UTF8");
		u.setFileOptRowSeparator("\n");
		u.setFileOptTrim("TRIM");
		u.setFileOptSkip(10);
		u.setFileOptNullIdentifier("NULL");
		u.setDefaultDateFormat("yyyy-MM-dd");
		u.addCSVColumn("ID", null);
		u.addCSVColumn("FIRSTNAME", null);
		u.addCSVColumn("LASTNAME", null);
		u.addCSVDateColumn("BIRTHDATE", null, "yyyy-MM-dd");
		u.addCSVColumn("CITY", null);
		u.addCSVColumn("GENDER", null);
		u.addCSVNumberColumn("AGE", null, null, null, null);
		u.setErrorTable("TEST_DATA.TEST_DATA_LOAD_ERROR_LOG");
		u.setErrorTableWithCurrentTimestamp(true);
		u.setErrorRejectLimit(5);
		u.executeImport();
		int rowNum = u.getCountAffectedRows();
		assertTrue("Not correct row num:" + rowNum, rowNum == 1000000);
	}

}
