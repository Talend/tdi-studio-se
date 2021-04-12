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
package org.talend.designer.mapper.external.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class ExternalMapperDataTest {

    @Test
    public void testEquals2() {
        ExternalMapperData mapper1 = new ExternalMapperData();

        List<ExternalMapperTable> externalTables = new ArrayList<ExternalMapperTable>();
        // input
        ExternalMapperTable externalTable = new ExternalMapperTable();
        externalTable.setActivateCondensedTool(true);
        externalTable.setActivateExpressionFilter(false);
        externalTable.setActivateColumnNameFilter(true);
        externalTable.setExpressionFilter("ddd");
        externalTable.setSizeState("ddd");
        externalTable.setMinimized(false);
        externalTable.setName("ddd");

        externalTable.setId("id1");
        externalTable.setLookupMode("ddd");
        externalTable.setMatchingMode("ddd");
        externalTable.setInnerJoin(false);
        externalTable.setPersistent(true);

        externalTable.setGlobalMapKeysValues(new ArrayList<ExternalMapperTableEntry>());
        externalTable.setConstraintTableEntries(new ArrayList<ExternalMapperTableEntry>());
        externalTable.setMetadataTableEntries(new ArrayList<ExternalMapperTableEntry>());
        externalTables.add(externalTable);
        mapper1.setInputTables(externalTables);


        ExternalMapperData mapper2 = new ExternalMapperData();

        List<ExternalMapperTable> externalTables2 = new ArrayList<ExternalMapperTable>();
        // input
        ExternalMapperTable externalTable2 = new ExternalMapperTable();
        externalTable2.setActivateCondensedTool(true);
        externalTable2.setActivateExpressionFilter(false);
        externalTable2.setActivateColumnNameFilter(true);
        externalTable2.setExpressionFilter("ddd");
        externalTable2.setSizeState("ddd");
        externalTable2.setMinimized(false);
        externalTable2.setName("ddd");


        externalTable2.setId("id1");
        externalTable2.setLookupMode("ddd");
        externalTable2.setMatchingMode("ddd");
        externalTable2.setInnerJoin(false);
        externalTable2.setPersistent(true);


        externalTables2.add(externalTable2);
        mapper2.setInputTables(externalTables2);
        assertTrue(mapper1.equals(mapper2));
    }

    @Test
    public void testEquals() {
        ExternalMapperData mapper1 = new ExternalMapperData();
        ExternalMapperData mapper2 = new ExternalMapperData();
        assertTrue(mapper1.equals(mapper2));
        test1();
        test2();
        test3();
        test4();
    }

    private void test1(){
        ExternalMapperData mapper1 = createExternalMapperData("inDiff", "outDiff", "varDiff");
        ExternalMapperData mapper2 = createExternalMapperData("inDiff", "outDiff", "varDiff");
        assertTrue(mapper1.equals(mapper2));
    }

    private void test2(){
        ExternalMapperData mapper1 = createExternalMapperData("inDiff", "outDiff", "varDiff");
        ExternalMapperData mapper2 = createExternalMapperData("inDiff1", "outDiff", "varDiff");
        assertFalse(mapper1.equals(mapper2));
    }

    private void test3(){
        ExternalMapperData mapper1 = createExternalMapperData("inDiff", "outDiff", "varDiff");
        ExternalMapperData mapper2 = createExternalMapperData("inDiff", "outDiff1", "varDiff");
        assertFalse(mapper1.equals(mapper2));
    }

    private void test4(){
        ExternalMapperData mapper1 = createExternalMapperData("inDiff", "outDiff", "varDiff");
        ExternalMapperData mapper2 = createExternalMapperData("inDiff", "outDiff", "varDiff1");
        assertFalse(mapper1.equals(mapper2));
    }

    private ExternalMapperData createExternalMapperData(String inDiff, String outDiff, String varDiff){
        ExternalMapperData externalData = new ExternalMapperData();
        List<ExternalMapperTable> externalTables = new ArrayList<ExternalMapperTable>();
        // input
        ExternalMapperTable externalTable = new ExternalMapperTable();
        externalTable.setActivateCondensedTool(true);
        externalTable.setActivateExpressionFilter(false);
        externalTable.setActivateColumnNameFilter(true);
        externalTable.setExpressionFilter(inDiff);
        externalTable.setSizeState(inDiff);
        externalTable.setMinimized(false);
        externalTable.setName(inDiff);

        List<ExternalMapperTableEntry> entityList = new ArrayList<ExternalMapperTableEntry>();
        ExternalMapperTableEntry externalEntity = new ExternalMapperTableEntry();
        externalEntity.setExpression(inDiff);
        externalEntity.setName("fff");
        externalEntity.setNullable(false);
        externalEntity.setType(inDiff);
        externalEntity.setOperator(inDiff);
        entityList.add(externalEntity);

        externalTable.setMetadataTableEntries(entityList);
        externalTable.setId("id1");
        externalTable.setLookupMode(inDiff);
        externalTable.setMatchingMode(inDiff);
        externalTable.setInnerJoin(false);
        externalTable.setPersistent(true);

        entityList = new ArrayList<ExternalMapperTableEntry>();
        externalEntity = new ExternalMapperTableEntry();
        externalEntity.setExpression(inDiff);
        externalEntity.setName("fff");
        externalEntity.setNullable(false);
        externalEntity.setType(inDiff);
        externalEntity.setOperator(inDiff);
        entityList.add(externalEntity);

        externalTable.setGlobalMapKeysValues(entityList);
        externalTables.add(externalTable);
        externalData.setInputTables(externalTables);

        externalTables = new ArrayList<ExternalMapperTable>();
        externalTable = new ExternalMapperTable();
        externalTable.setActivateCondensedTool(true);
        externalTable.setActivateExpressionFilter(false);
        externalTable.setActivateColumnNameFilter(true);
        externalTable.setExpressionFilter(outDiff);
        externalTable.setSizeState(outDiff);
        externalTable.setMinimized(false);
        externalTable.setName(outDiff);

        entityList = new ArrayList<ExternalMapperTableEntry>();
        externalEntity = new ExternalMapperTableEntry();
        externalEntity.setExpression(outDiff);
        externalEntity.setName("fff");
        externalEntity.setNullable(false);
        externalEntity.setType(outDiff);
        externalEntity.setOperator(outDiff);
        entityList.add(externalEntity);

        externalTable.setMetadataTableEntries(entityList);
        externalTable.setId("id1");

        externalTable.setReject(false);
        externalTable.setRejectInnerJoin(true);
        externalTable.setErrorRejectTable(true);
        externalTable.setIsJoinTableOf(outDiff);
        externalTables.add(externalTable);
        externalData.setOutputTables(externalTables);
        // var tables
        externalTables = new ArrayList<ExternalMapperTable>();
        externalTable = new ExternalMapperTable();
        externalTable.setSizeState(varDiff);
        externalTable.setMinimized(true);
        externalTable.setName("varTableName");

        entityList = new ArrayList<ExternalMapperTableEntry>();
        externalEntity = new ExternalMapperTableEntry();
        externalEntity.setExpression(varDiff);
        externalEntity.setName("fff");
        externalEntity.setNullable(false);
        externalEntity.setType(varDiff);
        externalEntity.setOperator(varDiff);

        externalTable.setMetadataTableEntries(entityList);
        externalTables.add(externalTable);
        externalData.setVarsTables(externalTables);

        return externalData;
    }

}
