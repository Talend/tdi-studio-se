package org.talend.designer.dbmap.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;

public class DbMapServiceTest {

    private NodeType nodeType;

    private String oldValue = "table";

    private String newValue = "context.schema.context.table";
    
    private InputTable input;
    
    private DBMapperTableEntry outEntry;

    @Before
    public void setUp() throws Exception {
        nodeType = TalendFileFactory.eINSTANCE.createNodeType();
        DBMapData data = DbmapFactory.eINSTANCE.createDBMapData();
        nodeType.setNodeData(data);
        input = DbmapFactory.eINSTANCE.createInputTable();
        input.setName(oldValue);
        input.setTableName(oldValue);
        DBMapperTableEntry inputEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        inputEntry.setName("column");
        data.getInputTables().add(input);
        input.getDBMapperTableEntries().add(inputEntry);
        OutputTable out = DbmapFactory.eINSTANCE.createOutputTable();
        out.setName("output");
        out.setTableName("output");
        outEntry = DbmapFactory.eINSTANCE.createDBMapperTableEntry();
        outEntry.setName("column");
        outEntry.setExpression(oldValue + ".column");
        out.getDBMapperTableEntries().add(outEntry);
        data.getOutputTables().add(out);
    }

    @Test
    public void testUpdateEMFDBMapData() {
        DbMapService service = new DbMapService();
        service.updateEMFDBMapData(nodeType, oldValue, newValue);
        DBMapData data = (DBMapData) nodeType.getNodeData();
        InputTable input = data.getInputTables().get(0);
        assertEquals("context.schema.context.table", input.getName());
        assertEquals("context.schema.context.table", input.getTableName());
        OutputTable out = data.getOutputTables().get(0);
        // output connection name should not be updated.
        assertEquals("output", out.getName());
        DBMapperTableEntry outEntry = out.getDBMapperTableEntries().get(0);
        assertEquals("context.schema.context.table.column", outEntry.getExpression());
    }
    
    @Test
    public void testUpdateEMFDBMapDataWithAliasSameWithTable() {
        input.setAlias(oldValue);
        DbMapService service = new DbMapService();
        service.updateEMFDBMapData(nodeType, oldValue, newValue);
        DBMapData data = (DBMapData) nodeType.getNodeData();
        InputTable input = data.getInputTables().get(0);
        assertEquals("context.schema.context.table", input.getName());
        assertEquals("context.schema.context.table", input.getTableName());
        OutputTable out = data.getOutputTables().get(0);
        // output connection name should not be updated.
        assertEquals("output", out.getName());
        DBMapperTableEntry outEntry = out.getDBMapperTableEntries().get(0);
        assertEquals(oldValue + ".column", outEntry.getExpression());
    }
    
    @Test
    public void testUpdateEMFDBMapDataWithAlias() {
        input.setAlias("alias");
        outEntry.setExpression("alias.column");
        DbMapService service = new DbMapService();
        service.updateEMFDBMapData(nodeType, oldValue, newValue);
        DBMapData data = (DBMapData) nodeType.getNodeData();
        InputTable input = data.getInputTables().get(0);
        assertEquals("context.schema.context.table", input.getName());
        assertEquals("context.schema.context.table", input.getTableName());
        OutputTable out = data.getOutputTables().get(0);
        // output connection name should not be updated.
        assertEquals("output", out.getName());
        DBMapperTableEntry outEntry = out.getDBMapperTableEntries().get(0);
        assertEquals("alias.column", outEntry.getExpression());
    }

}
