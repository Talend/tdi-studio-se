package org.neo4j.talend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DummyTalendPojo {

    public String id;
    public String label;
    public String relType;

    public String propString;
    public Integer propInteger;
    public Long propLong;
    public Float propFloat;
    public Boolean propBoolean;
    public Date propDate;

    public static DummyTalendPojo getDummyTalendPojo() {
        return DummyTalendPojo.getDummyTalendPojo(UUID.randomUUID().toString());
    }

    public static DummyTalendPojo getDummyTalendPojo(Object id) {
        DummyTalendPojo pojo = new DummyTalendPojo();

        pojo.id = id.toString();
        pojo.label = Neo4jBatchUnitTest.LABEL_NAME;
        pojo.relType = Neo4jBatchUnitTest.REL_TYPE;

        pojo.propString = "A string";
        pojo.propInteger = 42;
        pojo.propLong = Long.valueOf(42);
        pojo.propFloat = Float.parseFloat("66.6");
        pojo.propBoolean = Boolean.TRUE;
        pojo.propDate = new Date();

        return pojo;
    }

    public static List<String> getColumnList() {
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("label");
        columns.add("relType");
        columns.add("propString");
        columns.add("propInteger");
        columns.add("propLong");
        columns.add("propFloat");
        columns.add("propBoolean");
        columns.add("propDate");

        return columns;
    }

}
