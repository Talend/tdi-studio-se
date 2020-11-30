package org.talend.designer.dbmap.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.language.mysql.MysqlLanguage;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;

public class DataMapExpressionParserTest {

    private DataMapExpressionParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new DataMapExpressionParser(new MysqlLanguage());
    }

    @Test
    public void testParseTableEntryLocations() {
        // test context and built-in expression
        verifyParseResult("context.schema.context.table.column");
        verifyParseResult("context.schema.table.column");
        verifyParseResult("schema.context.table.column");
        verifyParseResult("schema.table.column");
        verifyParseResult("table.column");
        // test expression with blanks
        verifyParseResult(" context .  schema. context . table.column");
        verifyParseResult(" schema . context .table .column");
        verifyParseResult("schema. table . column");
        verifyParseResult("((String)globalMap.get(\"schema\")). ((String)globalMap.get(\"main_table\")).column ");
        verifyParseResult("schema. ((String)globalMap.get(\"main_table\")).column ");
        verifyParseResult("((String)globalMap.get(\"schema\")). table.column ");
        verifyParseResult("((String)globalMap.get(\"main_table\")).column ");
        
        verifyIdentifiersResult("table.\\\"column\\\"");
        verifyIdentifiersResult("Case NVL(table.\\\"column\\\",-99) When -99 Then 'N' Else 'Y' End");
    }
    
    private void verifyIdentifiersResult(String expression) {
    	TableEntryLocation[] locations = parser.parseTableEntryLocations(expression);
        for (TableEntryLocation location : locations) {
            assertEquals("table", location.tableName);
            assertEquals("column", location.columnName);
        }
    }

    private void verifyParseResult(String expression) {
        String expectColumnName = expression.substring(expression.lastIndexOf(".") + 1).trim();
        String expectTableName = expression.substring(0, expression.lastIndexOf("."));
        String[] fragments = expectTableName.split("\\.");
        expectTableName = "";
        for (int i = 0; i < fragments.length; i++) {
            String fragment = fragments[i].trim();
            if (i < fragments.length - 1) {
                fragment = fragment + ".";
            }
            expectTableName = expectTableName + fragment;
        }
        TableEntryLocation[] locations = parser.parseTableEntryLocations(expression);
        for (TableEntryLocation location : locations) {
            assertEquals(expectTableName, location.tableName);
            assertEquals(expectColumnName, location.columnName);
        }
    }

    public static void main(String[] args) throws Exception {

        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();
        // String PATTERN_STR = "\\s*(\\w+)\\s*(\\.\\s*(\\w+)\\s*)+"; // can't get correct group count.
        String PATTERN_STR = "(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)"
                + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)"
                + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)" + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)";
        String expression = "context. schema.  context  .table.column";
        // String expression = "context.schema.table.column";
        // String expression = "schema.table.column";
        // String expression = "table.column";
        matcher.setMultiline(true);
        PatternMatcherInput patternMatcherInput = new PatternMatcherInput(expression);
        Pattern pattern = compiler.compile(PATTERN_STR);
        while (matcher.contains(patternMatcherInput, pattern)) {
            MatchResult matchResult = matcher.getMatch();
            System.out.println("group count:" + matchResult.groups());
            for (int i = 1; i <= matchResult.groups(); i++) {
                System.out.println("group[" + i + "] content:" + matchResult.group(i));
            }
        }
    }

    @Test
    public void testGetGlobalMapSet() {
        String sqlQuery = "select * from myTable";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 0);

        sqlQuery = "\"SELECT \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".id,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".age, \""
                + "+((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n" + "FROM"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \" , "
                + "\" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 3);
        List<String> list = new ArrayList<String>(globalList);
        java.util.Collections.sort(list);
        Assert.assertEquals(list.get(0), "((String)globalMap.get(\"lookup_table\"))");
        Assert.assertEquals(list.get(1), "((String)globalMap.get(\"main_table\"))");
        Assert.assertEquals(list.get(2), "((String)globalMap.get(\"schema\"))");

        sqlQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"#main_table%\"))+ \".id, \" +((String)globalMap.get(\"#main_table%\"))+ \".name,"
                + " \" +((String)globalMap.get(\"#main_table%\"))+ \".age, \" +((String)globalMap.get(\"@lookup_table*\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"#main_table%\"))+ \" , \" +((String)globalMap.get(\"@lookup_table*\"))";

        globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 2);
        list = new ArrayList<String>(globalList);
        java.util.Collections.sort(list);
        Assert.assertEquals(list.get(0), "((String)globalMap.get(\"#main_table%\"))");
        Assert.assertEquals(list.get(1), "((String)globalMap.get(\"@lookup_table*\"))");

    }

    @Test
    public void testGetGlobalMapSet4ExpressionString1() {
        String sqlQuery = "((String)globalMap.get(\"TECH_SYSTEM_SOURCE\"))";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 1);
        List<String> list = new ArrayList<String>(globalList);
        Assert.assertEquals(list.get(0), "((String)globalMap.get(\"TECH_SYSTEM_SOURCE\"))");
    }

    @Test
    public void testGetGlobalMapSet4ExpressionString2() {
        String sqlQuery = "\"+((String)globalMap.get(\"TECH_SYSTEM_SOURCE\"))+\"";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 1);
        List<String> list = new ArrayList<String>(globalList);
        Assert.assertEquals(list.get(0), "((String)globalMap.get(\"TECH_SYSTEM_SOURCE\"))");
    }

    @Test
    public void testGetGlobalMapSet4ExpressionString3() {
        String sqlQuery = "globalMap.get(\"TECH_SYSTEM_SOURCE\")";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 0);
    }

    @Test
    public void testGetGlobalMapSet4ExpressionInteger1() {
        String sqlQuery = "((Integer)globalMap.get(\"TECH_SYSTEM_SOURCE\"))";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 1);
        List<String> list = new ArrayList<String>(globalList);
        Assert.assertEquals(list.get(0), "((Integer)globalMap.get(\"TECH_SYSTEM_SOURCE\"))");
    }

    @Test
    public void testGetGlobalMapSet4ExpressionInteger2() {
        String sqlQuery = "\"+((Integer)globalMap.get(\"TECH_SYSTEM_SOURCE\"))+\"";
        Set<String> globalList = parser.getGlobalMapSet(sqlQuery);
        Assert.assertEquals(globalList.size(), 1);
        List<String> list = new ArrayList<String>(globalList);
        Assert.assertEquals(list.get(0), "((Integer)globalMap.get(\"TECH_SYSTEM_SOURCE\"))");
    }

    @Test
    public void testReplaceLocation() {
        String expression = "((String)globalMap.get(\"schema\")). ((String)globalMap.get(\"main_table\")).column ";
        String expectedExp = "((String)globalMap.get(\"schema_1\")). ((String)globalMap.get(\"main_table_1\")).column ";
        TableEntryLocation oldLocation = new TableEntryLocation();
        oldLocation.tableName = "((String)globalMap.get(\"schema\")). ((String)globalMap.get(\"main_table\"))";
        oldLocation.columnName = "column";
        TableEntryLocation newLocation = new TableEntryLocation();
        newLocation.tableName = "((String)globalMap.get(\"schema_1\")). ((String)globalMap.get(\"main_table_1\"))";
        newLocation.columnName = "column";
        String replaceLocation = parser.replaceLocation(expression, oldLocation, newLocation);
        Assert.assertEquals(expectedExp, replaceLocation);

        expression = "schema. ((String)globalMap.get(\"main_table\")).column ";
        expectedExp = "schema_1. ((String)globalMap.get(\"main_table_1\")).column ";
        oldLocation = new TableEntryLocation();
        oldLocation.tableName = "schema. ((String)globalMap.get(\"main_table\"))";
        oldLocation.columnName = "column";
        newLocation = new TableEntryLocation();
        newLocation.tableName = "schema_1. ((String)globalMap.get(\"main_table_1\"))";
        newLocation.columnName = "column";
        replaceLocation = parser.replaceLocation(expression, oldLocation, newLocation);
        Assert.assertEquals(expectedExp, replaceLocation);

        expression = "((String)globalMap.get(\"schema\")). table.column ";
        expectedExp = "((String)globalMap.get(\"schema_1\")). table_1.column ";
        oldLocation = new TableEntryLocation();
        oldLocation.tableName = "((String)globalMap.get(\"schema\")). table";
        oldLocation.columnName = "column";
        newLocation = new TableEntryLocation();
        newLocation.tableName = "((String)globalMap.get(\"schema_1\")). table_1";
        newLocation.columnName = "column";
        replaceLocation = parser.replaceLocation(expression, oldLocation, newLocation);
        Assert.assertEquals(expectedExp, replaceLocation);

        expression = "((String)globalMap.get(\"main_table\")).column ";
        expectedExp = "main_table.column ";
        oldLocation = new TableEntryLocation();
        oldLocation.tableName = "((String)globalMap.get(\"main_table\"))";
        oldLocation.columnName = "column";
        newLocation = new TableEntryLocation();
        newLocation.tableName = "main_table";
        newLocation.columnName = "column";
        replaceLocation = parser.replaceLocation(expression, oldLocation, newLocation);
        Assert.assertEquals(expectedExp, replaceLocation);
    }

    @Test
    public void testGetGlobalMapExpressionRegexAndReplacement() {
        IDbLanguage language = new MysqlLanguage();
        DataMapExpressionParser paser = new DataMapExpressionParser(language);
        // test 1
        String exp1 = "((String)globalMap.get(\"\\source*test{\"))";
        String globalMapReplaceExpression = paser.getGlobalMapExpressionRegex(exp1);
        // test expression
        Assert.assertEquals(globalMapReplaceExpression, "\\(\\(String\\)globalMap\\.get\\(\"\\\\source\\*test\\{\"\\)\\)");
        // test replacement
        String replacement = paser.getGlobalMapReplacement(exp1);
        exp1 = exp1.replaceAll(globalMapReplaceExpression, "\"+" + replacement + "+\"");
        Assert.assertEquals(exp1, "\"+((String)globalMap.get(\"\\source*test{\"))+\"");

        // test 2
        exp1 = "((String)globalMap.get(\"\\\\source*test{\"))";
        globalMapReplaceExpression = paser.getGlobalMapExpressionRegex(exp1);
        // test expression
        Assert.assertEquals(globalMapReplaceExpression, "\\(\\(String\\)globalMap\\.get\\(\"\\\\\\\\source\\*test\\{\"\\)\\)");
        // test replacement
        replacement = paser.getGlobalMapReplacement(exp1);
        exp1 = exp1.replaceAll(globalMapReplaceExpression, "\"+" + replacement + "+\"");
        Assert.assertEquals(exp1, "\"+((String)globalMap.get(\"\\\\source*test{\"))+\"");

        // test 3
        exp1 = "((String)globalMap.get(\"\\\\source\\*test\\{\"))";
        globalMapReplaceExpression = paser.getGlobalMapExpressionRegex(exp1);
        // test expression
        Assert.assertEquals(globalMapReplaceExpression,
                "\\(\\(String\\)globalMap\\.get\\(\"\\\\\\\\source\\\\\\*test\\\\\\{\"\\)\\)");
        // test replacement
        replacement = paser.getGlobalMapReplacement(exp1);
        exp1 = exp1.replaceAll(globalMapReplaceExpression, "\"+" + replacement + "+\"");
        Assert.assertEquals(exp1, "\"+((String)globalMap.get(\"\\\\source\\*test\\{\"))+\"");

        // test 4
        exp1 = "((String)globalMap.get(\"#main\\$table)\"))";
        globalMapReplaceExpression = paser.getGlobalMapExpressionRegex(exp1);
        // test expression
        Assert.assertEquals(globalMapReplaceExpression, "\\(\\(String\\)globalMap\\.get\\(\"#main\\\\\\$table\\)\"\\)\\)");
        // test replacement
        replacement = paser.getGlobalMapReplacement(exp1);
        exp1 = exp1.replaceAll(globalMapReplaceExpression, "\"+" + replacement + "+\"");
        Assert.assertEquals(exp1, "\"+((String)globalMap.get(\"#main\\$table)\"))+\"");

    }

}
