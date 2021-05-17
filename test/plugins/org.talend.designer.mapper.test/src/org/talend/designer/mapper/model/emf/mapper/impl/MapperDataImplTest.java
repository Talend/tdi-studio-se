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
package org.talend.designer.mapper.model.emf.mapper.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.model.emf.mapper.SizeState;
import org.talend.designer.mapper.model.emf.mapper.VarTable;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class MapperDataImplTest {

    @Test
    public void testEquals() {
        test1();
        test2();
        test3();
        test4();
        test5();
        test6();
    }

    private void test1(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final InputTable persistentTable = MapperFactory.eINSTANCE.createInputTable();
        persistentTable.setActivateCondensedTool(true);
        persistentTable.setActivateExpressionFilter(true);
        persistentTable.setActivateColumnNameFilter(true);
        persistentTable.setExpressionFilter("expressionFilter1");
        persistentTable.setMinimized(false);
        persistentTable.setName("tableName");
        persistentTable.setSizeState(SizeState.MAXIMIZED);
        persistentTable.setLookupMode("LookupMode");
        persistentTable.setMatchingMode("MatchingMode");
        persistentTable.setInnerJoin(true);
        persistentTable.setPersistent(false);
        persistentTable.setId("id1");

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName");
        emfMapperTableEntry.setNullable(false);
        emfMapperTableEntry.setType("type1");
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getMapperTableEntries().add(emfMapperTableEntry);

        final MapperTableEntry mapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        mapperTableEntry.setExpression("expression1");
        mapperTableEntry.setName("entityName");
        mapperTableEntry.setNullable(true);
        mapperTableEntry.setType("type1");
        mapperTableEntry.setOperator("operator1");
        persistentTable.getGlobalMapKeysValues().add(emfMapperTableEntry);

        mapData1.getInputTables().add(persistentTable);

        final InputTable persistentTable2 = MapperFactory.eINSTANCE.createInputTable();
        persistentTable2.setActivateCondensedTool(true);
        persistentTable2.setActivateExpressionFilter(true);
        persistentTable2.setActivateColumnNameFilter(true);
        persistentTable2.setExpressionFilter("expressionFilter1");
        persistentTable2.setMinimized(false);
        persistentTable2.setName("tableName");
        persistentTable2.setSizeState(SizeState.MAXIMIZED);
        persistentTable2.setLookupMode("LookupMode");
        persistentTable2.setMatchingMode("MatchingMode");
        persistentTable2.setInnerJoin(true);
        persistentTable2.setPersistent(false);
        persistentTable2.setId("id1");

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName");
        emfMapperTableEntry2.setNullable(false);
        emfMapperTableEntry2.setType("type1");
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getMapperTableEntries().add(emfMapperTableEntry2);

        final MapperTableEntry mapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        mapperTableEntry2.setExpression("expression1");
        mapperTableEntry2.setName("entityName");
        mapperTableEntry2.setNullable(true);
        mapperTableEntry2.setType("type1");
        mapperTableEntry2.setOperator("operator1");
        persistentTable2.getGlobalMapKeysValues().add(emfMapperTableEntry2);

        mapData2.getInputTables().add(persistentTable2);

        assertTrue(mapData1.equals(mapData2));

    }

    private void test2(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final InputTable persistentTable = MapperFactory.eINSTANCE.createInputTable();
        persistentTable.setActivateCondensedTool(true);
        persistentTable.setActivateExpressionFilter(true);
        persistentTable.setActivateColumnNameFilter(true);
        persistentTable.setExpressionFilter("expressionFilter1");
        persistentTable.setMinimized(false);
        persistentTable.setName("tableName");
        persistentTable.setSizeState(SizeState.MAXIMIZED);
        persistentTable.setLookupMode("LookupMode");
        persistentTable.setMatchingMode("MatchingMode");
        persistentTable.setInnerJoin(true);
        persistentTable.setPersistent(false);
        persistentTable.setId("id1");

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName");
        emfMapperTableEntry.setNullable(false);
        emfMapperTableEntry.setType("type1");
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getMapperTableEntries().add(emfMapperTableEntry);

        final MapperTableEntry mapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        mapperTableEntry.setExpression("expression1");
        mapperTableEntry.setName("entityName");
        mapperTableEntry.setNullable(true);
        mapperTableEntry.setType("type1");
        mapperTableEntry.setOperator("operator1");
        persistentTable.getGlobalMapKeysValues().add(emfMapperTableEntry);

        mapData1.getInputTables().add(persistentTable);

        final InputTable persistentTable2 = MapperFactory.eINSTANCE.createInputTable();
        persistentTable2.setActivateCondensedTool(true);
        persistentTable2.setActivateExpressionFilter(true);
        persistentTable2.setActivateColumnNameFilter(true);
        persistentTable2.setExpressionFilter("exprefssionFilter1");
        persistentTable2.setMinimized(false);
        persistentTable2.setName("tableName");
        persistentTable2.setSizeState(SizeState.MAXIMIZED);
        persistentTable2.setLookupMode("LookupMode");
        persistentTable2.setMatchingMode("MatchiggngMode");
        persistentTable2.setInnerJoin(true);
        persistentTable2.setPersistent(false);
        persistentTable2.setId("id1");

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName");
        emfMapperTableEntry2.setNullable(false);
        emfMapperTableEntry2.setType("type1");
        emfMapperTableEntry2.setOperator("operat");
        persistentTable.getMapperTableEntries().add(emfMapperTableEntry2);

        final MapperTableEntry mapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        mapperTableEntry2.setExpression("expression1");
        mapperTableEntry2.setName("entityName");
        mapperTableEntry2.setNullable(true);
        mapperTableEntry2.setType("typge1");
        mapperTableEntry2.setOperator("opgerator1");
        persistentTable.getGlobalMapKeysValues().add(emfMapperTableEntry2);

        mapData2.getInputTables().add(persistentTable2);

        assertFalse(mapData1.equals(mapData2));

    }

    private void test3(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final OutputTable persistentTable = MapperFactory.eINSTANCE.createOutputTable();
        persistentTable.setActivateCondensedTool(true);
        persistentTable.setActivateExpressionFilter(true);
        persistentTable.setActivateColumnNameFilter(true);
        persistentTable.setExpressionFilter("expressionFilter1");
        persistentTable.setMinimized(false);
        persistentTable.setName("tableName");
        persistentTable.setSizeState(SizeState.MAXIMIZED);
        persistentTable.setId("id1");
        persistentTable.setReject(true);
        persistentTable.setRejectInnerJoin(false);
        persistentTable.setIsErrorRejectTable(false);
        persistentTable.setIsJoinTableOf("join");

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName");
        emfMapperTableEntry.setNullable(false);
        emfMapperTableEntry.setType("type1");
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getOutputTables().add(persistentTable);

        final OutputTable persistentTable2 = MapperFactory.eINSTANCE.createOutputTable();
        persistentTable2.setActivateCondensedTool(true);
        persistentTable2.setActivateExpressionFilter(true);
        persistentTable2.setActivateColumnNameFilter(true);
        persistentTable2.setExpressionFilter("expressionFilter1");
        persistentTable2.setMinimized(false);
        persistentTable2.setName("tableName");
        persistentTable2.setSizeState(SizeState.MAXIMIZED);
        persistentTable2.setId("id1");
        persistentTable2.setReject(true);
        persistentTable2.setRejectInnerJoin(false);
        persistentTable2.setIsErrorRejectTable(false);
        persistentTable2.setIsJoinTableOf("join");

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName");
        emfMapperTableEntry2.setNullable(false);
        emfMapperTableEntry2.setType("type1");
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getOutputTables().add(persistentTable2);

        assertTrue(mapData1.equals(mapData2));

    }

    private void test4(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final OutputTable persistentTable = MapperFactory.eINSTANCE.createOutputTable();
        persistentTable.setActivateCondensedTool(true);
        persistentTable.setActivateExpressionFilter(true);
        persistentTable.setActivateColumnNameFilter(true);
        persistentTable.setExpressionFilter("expressionFilter1");
        persistentTable.setMinimized(false);
        persistentTable.setName("tableName");
        persistentTable.setSizeState(SizeState.MAXIMIZED);
        persistentTable.setId("id1");
        persistentTable.setReject(true);
        persistentTable.setRejectInnerJoin(false);
        persistentTable.setIsErrorRejectTable(false);
        persistentTable.setIsJoinTableOf("join");

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName");
        emfMapperTableEntry.setNullable(false);
        emfMapperTableEntry.setType("type1");
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getOutputTables().add(persistentTable);

        final OutputTable persistentTable2 = MapperFactory.eINSTANCE.createOutputTable();
        persistentTable2.setActivateCondensedTool(true);
        persistentTable2.setActivateExpressionFilter(true);
        persistentTable2.setActivateColumnNameFilter(true);
        persistentTable2.setExpressionFilter("expressionhhFilter1");
        persistentTable2.setMinimized(false);
        persistentTable2.setName("tablebbName");
        persistentTable2.setSizeState(SizeState.MAXIMIZED);
        persistentTable2.setId("id2");
        persistentTable2.setReject(true);
        persistentTable2.setRejectInnerJoin(false);
        persistentTable2.setIsErrorRejectTable(false);
        persistentTable2.setIsJoinTableOf("jonnin");

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expggression1");
        emfMapperTableEntry2.setName("entityName");
        emfMapperTableEntry2.setNullable(false);
        emfMapperTableEntry2.setType("tyggpe1");
        emfMapperTableEntry2.setOperator("opeggrator1");
        persistentTable2.getMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getOutputTables().add(persistentTable2);

        assertFalse(mapData1.equals(mapData2));
    }

    private void test5(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final VarTable persistentVarTable = MapperFactory.eINSTANCE.createVarTable();
        persistentVarTable.setMinimized(false);
        persistentVarTable.setName("name1");
        persistentVarTable.setSizeState(SizeState.INTERMEDIATE);

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("ss");
        emfMapperTableEntry.setNullable(true);
        emfMapperTableEntry.setType("dd");
        emfMapperTableEntry.setOperator("oo");
        persistentVarTable.getMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getVarTables().add(persistentVarTable);

        final VarTable persistentVarTable2 = MapperFactory.eINSTANCE.createVarTable();
        persistentVarTable2.setMinimized(false);
        persistentVarTable2.setName("name1");
        persistentVarTable2.setSizeState(SizeState.INTERMEDIATE);

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("ss");
        emfMapperTableEntry2.setNullable(true);
        emfMapperTableEntry2.setType("dd");
        emfMapperTableEntry2.setOperator("oo");
        persistentVarTable2.getMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getVarTables().add(persistentVarTable2);
        assertTrue(mapData1.equals(mapData2));
    }

    private void test6(){
        MapperDataImpl mapData1 = new MapperDataImpl();
        MapperDataImpl mapData2 = new MapperDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final VarTable persistentVarTable = MapperFactory.eINSTANCE.createVarTable();
        persistentVarTable.setMinimized(false);
        persistentVarTable.setName("name1");
        persistentVarTable.setSizeState(SizeState.INTERMEDIATE);

        final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("ss");
        emfMapperTableEntry.setNullable(true);
        emfMapperTableEntry.setType("dvvd");
        emfMapperTableEntry.setOperator("ovvo");
        persistentVarTable.getMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getVarTables().add(persistentVarTable);

        final VarTable persistentVarTable2 = MapperFactory.eINSTANCE.createVarTable();
        persistentVarTable2.setMinimized(false);
        persistentVarTable2.setName("name1");
        persistentVarTable2.setSizeState(SizeState.INTERMEDIATE);

        final MapperTableEntry emfMapperTableEntry2 = MapperFactory.eINSTANCE.createMapperTableEntry();
        emfMapperTableEntry2.setExpression("expreshsion1");
        emfMapperTableEntry2.setName("ss");
        emfMapperTableEntry2.setNullable(true);
        emfMapperTableEntry2.setType("dvd");
        emfMapperTableEntry2.setOperator("oov");
        persistentVarTable2.getMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getVarTables().add(persistentVarTable2);
        assertFalse(mapData1.equals(mapData2));
    }
}
