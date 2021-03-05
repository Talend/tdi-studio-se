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
package org.talend.designer.dbmap.external.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class ExternalDbMapDataTest {



    @Test
    public void testEquals() {
        ExternalDbMapData mapper1 = new ExternalDbMapData();
        ExternalDbMapData mapper2 = new ExternalDbMapData();
        assertTrue(mapper1.equals(mapper2));
        test1();
        test2();
        test3();
        test4();
    }

    private void test1(){
        ExternalDbMapData mapper1 = createExternalDbMapData("inDiff", "outDiff", "varDiff");
        ExternalDbMapData mapper2 = createExternalDbMapData("inDiff", "outDiff", "varDiff");
        assertTrue(mapper1.equals(mapper2));
    }

    private void test2(){
        ExternalDbMapData mapper1 = createExternalDbMapData("inDiff", "outDiff", "varDiff");
        ExternalDbMapData mapper2 = createExternalDbMapData("inDiff1", "outDiff", "varDiff");
        assertFalse(mapper1.equals(mapper2));
    }

    private void test3(){
        ExternalDbMapData mapper1 = createExternalDbMapData("inDiff", "outDiff", "varDiff");
        ExternalDbMapData mapper2 = createExternalDbMapData("inDiff", "outDiff1", "varDiff");
        assertFalse(mapper1.equals(mapper2));
    }

    private void test4(){
        ExternalDbMapData mapper1 = createExternalDbMapData("inDiff", "outDiff", "varDiff");
        ExternalDbMapData mapper2 = createExternalDbMapData("inDiff", "outDiff", "varDiff1");
        assertFalse(mapper1.equals(mapper2));
    }

    private ExternalDbMapData createExternalDbMapData(String inDiff, String outDiff, String varDiff){
        ExternalDbMapData externalData = new ExternalDbMapData();

        List<ExternalDbMapTable> externalTables = new ArrayList<ExternalDbMapTable>();

        // input
        ExternalDbMapTable externalTable = new ExternalDbMapTable();
        externalTable.setName("pTableName");
        externalTable.setMinimized(true);
        externalTable.setAlias("pTableAlias");
        externalTable.setJoinType("JoinType");
        externalTable.setTableName("pTableTableName");
        List<ExternalDbMapEntry> entities = new ArrayList<ExternalDbMapEntry>();
        ExternalDbMapEntry entity = new ExternalDbMapEntry();
        entity.setExpression(inDiff);
        entity.setJoin(true);
        entity.setName("pEntityName");
        entity.setOperator(inDiff);
        entities.add(entity);
        externalTable.setMetadataTableEntries(entities);
        externalTables.add(externalTable);
        externalData.setInputTables(externalTables);

        // output
        externalTables = new ArrayList<ExternalDbMapTable>();
        externalTable = new ExternalDbMapTable();
        externalTable.setName("pTableName");
        externalTable.setMinimized(false);
        externalTable.setTableName(outDiff);
        entities = new ArrayList<ExternalDbMapEntry>();
        entity = new ExternalDbMapEntry();
        entity.setExpression(outDiff);
        entity.setName("pEntitytName");
        entities.add(entity);
        externalTable.setMetadataTableEntries(entities);

        // filters
        entities = new ArrayList<ExternalDbMapEntry>();
        List<ExternalDbMapEntry> otherFilterEntities = new ArrayList<ExternalDbMapEntry>();
        entity = new ExternalDbMapEntry();
        entity.setExpression(outDiff);
        entity.setName("pFilterName");
        entities.add(entity);
        externalTable.setCustomWhereConditionsEntries(entities);
        externalTable.setCustomOtherConditionsEntries(otherFilterEntities);
        externalTables.add(externalTable);
        externalData.setOutputTables(externalTables);

        // var
        externalTables = new ArrayList<ExternalDbMapTable>();
        externalTable = new ExternalDbMapTable();
        externalTable.setName("pTableName");
        externalTable.setMinimized(true);
        externalTable.setAlias("pTableAlias");
        externalTable.setJoinType("JoinType");
        externalTable.setTableName("pTableTableName");
        entities = new ArrayList<ExternalDbMapEntry>();
        entity = new ExternalDbMapEntry();
        entity.setExpression(varDiff);
        entity.setJoin(true);
        entity.setName("pEntityName");
        entity.setOperator(varDiff);
        entities.add(entity);
        externalTable.setMetadataTableEntries(entities);
        externalTables.add(externalTable);
        externalData.setVarsTables(externalTables);

        return externalData;
    }

    @Test
    public void testEquals2() {
        ExternalDbMapData mapper1 = new ExternalDbMapData();

        List<ExternalDbMapTable> externalTables = new ArrayList<ExternalDbMapTable>();

        // input
        ExternalDbMapTable externalTable = new ExternalDbMapTable();
        externalTable.setName("pTableName");
        externalTable.setMinimized(true);
        externalTable.setAlias("pTableAlias");
        externalTable.setJoinType("JoinType");
        externalTable.setTableName("pTableTableName");
        externalTable.setMetadataTableEntries(new ArrayList<ExternalDbMapEntry>());
        externalTable.setCustomOtherConditionsEntries(new ArrayList<ExternalDbMapEntry>());
        externalTable.setCustomWhereConditionsEntries(new ArrayList<ExternalDbMapEntry>());
        externalTables.add(externalTable);
        mapper1.setInputTables(externalTables);


        ExternalDbMapData mapper2 = new ExternalDbMapData();

        List<ExternalDbMapTable> externalTables2 = new ArrayList<ExternalDbMapTable>();

        // input
        ExternalDbMapTable externalTable2 = new ExternalDbMapTable();
        externalTable2.setName("pTableName");
        externalTable2.setMinimized(true);
        externalTable2.setAlias("pTableAlias");
        externalTable2.setJoinType("JoinType");
        externalTable2.setTableName("pTableTableName");
        externalTables2.add(externalTable2);
        mapper2.setInputTables(externalTables2);

        assertTrue(mapper1.equals(mapper2));
    }

}
