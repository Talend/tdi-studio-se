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
package org.talend.designer.dbmap.model.emf.dbmap.impl;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class DBMapDataImplTest {

    @Test
    public void testEquals() {
        test1();
        test2();
        test3();
        test4();
    }

    private void test1(){
        DBMapDataImpl mapData1 = new DBMapDataImpl();
        DBMapDataImpl mapData2 = new DBMapDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final InputTable persistentTable = DbmapFactory.eINSTANCE.createInputTable();
        persistentTable.setMinimized(true);
        persistentTable.setName("table1");
        persistentTable.setAlias("alias1");
        persistentTable.setJoinType("type1");
        persistentTable.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName1");
        emfMapperTableEntry.setJoin(true);
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getDBMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getInputTables().add(persistentTable);

        final InputTable persistentTable2 = DbmapFactory.eINSTANCE.createInputTable();
        persistentTable2.setMinimized(true);
        persistentTable2.setName("table1");
        persistentTable2.setAlias("alias1");
        persistentTable2.setJoinType("type1");
        persistentTable2.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry2 = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName1");
        emfMapperTableEntry2.setJoin(true);
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getDBMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getInputTables().add(persistentTable2);
        assertTrue(mapData1.equals(mapData2));
    }

    private void test2(){
        DBMapDataImpl mapData1 = new DBMapDataImpl();
        DBMapDataImpl mapData2 = new DBMapDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final InputTable persistentTable = DbmapFactory.eINSTANCE.createInputTable();
        persistentTable.setMinimized(true);
        persistentTable.setName("table1");
        persistentTable.setAlias("alias1");
        persistentTable.setJoinType("type1");
        persistentTable.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName1");
        emfMapperTableEntry.setJoin(true);
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getDBMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getInputTables().add(persistentTable);

        final InputTable persistentTable2 = DbmapFactory.eINSTANCE.createInputTable();
        persistentTable2.setMinimized(true);
        persistentTable2.setName("table1");
        persistentTable2.setAlias("alias1");
        persistentTable2.setJoinType("type1");
        persistentTable2.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry2 = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression_");
        emfMapperTableEntry2.setName("entityName1");
        emfMapperTableEntry2.setJoin(true);
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getDBMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getInputTables().add(persistentTable2);
        assertFalse(mapData1.equals(mapData2));
    }

    private void test3(){
        DBMapDataImpl mapData1 = new DBMapDataImpl();
        DBMapDataImpl mapData2 = new DBMapDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final OutputTable persistentTable = DbmapFactory.eINSTANCE.createOutputTable();
        persistentTable.setMinimized(true);
        persistentTable.setName("table1");
        persistentTable.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName1");
        emfMapperTableEntry.setJoin(true);
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getDBMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getOutputTables().add(persistentTable);

        final OutputTable persistentTable2 = DbmapFactory.eINSTANCE.createOutputTable();
        persistentTable2.setMinimized(true);
        persistentTable2.setName("table1");
        persistentTable2.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry2 = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName1");
        emfMapperTableEntry2.setJoin(true);
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getDBMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getOutputTables().add(persistentTable2);
        assertTrue(mapData1.equals(mapData2));
    }

    private void test4(){
        DBMapDataImpl mapData1 = new DBMapDataImpl();
        DBMapDataImpl mapData2 = new DBMapDataImpl();
        assertTrue(mapData1.equals(mapData2));

        final OutputTable persistentTable = DbmapFactory.eINSTANCE.createOutputTable();
        persistentTable.setMinimized(true);
        persistentTable.setName("table1");
        persistentTable.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry.setExpression("expression1");
        emfMapperTableEntry.setName("entityName1");
        emfMapperTableEntry.setJoin(true);
        emfMapperTableEntry.setOperator("operator1");
        persistentTable.getDBMapperTableEntries().add(emfMapperTableEntry);

        mapData1.getOutputTables().add(persistentTable);

        final InputTable persistentTable2 = DbmapFactory.eINSTANCE.createInputTable();
        persistentTable2.setMinimized(true);
        persistentTable2.setName("table1");
        persistentTable2.setTableName("tableName");

        final DBMapperTableEntry emfMapperTableEntry2 = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        emfMapperTableEntry2.setExpression("expression1");
        emfMapperTableEntry2.setName("entityName");
        emfMapperTableEntry2.setJoin(true);
        emfMapperTableEntry2.setOperator("operator1");
        persistentTable2.getDBMapperTableEntries().add(emfMapperTableEntry2);

        mapData2.getOutputTables().add(persistentTable);
        assertFalse(mapData1.equals(mapData2));
    }
}
