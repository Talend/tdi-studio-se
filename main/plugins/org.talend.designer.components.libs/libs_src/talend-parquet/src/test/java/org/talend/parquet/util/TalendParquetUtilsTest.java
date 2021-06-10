package org.talend.parquet.util;

import static org.apache.parquet.schema.MessageTypeParser.parseMessageType;
import static org.apache.parquet.schema.OriginalType.DECIMAL;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY;
import static org.apache.parquet.schema.Type.Repetition.REQUIRED;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.parquet.schema.DecimalMetadata;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Type;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.talend.parquet.data.Group;
import org.talend.parquet.data.simple.SimpleGroup;
import org.talend.parquet.utils.TalendParquetUtils;

public class TalendParquetUtilsTest {

	@Test
	public void testGetStringList() {
		MessageType schema = parseMessageType("message Schema { " //
				+ "  optional int64 fieldo; " //
				+ "  optional group field1 { " //
				+ "    repeated binary field2 (UTF8); " //
				+ "  } " //
				+ "}"); //

		Group group = new SimpleGroup(schema.getType(1).asGroupType());
		group.add(0, "element 1");
		group.add(0, "element 2");
		group.add(0, "element 3");
		group.add(0, "element 4");

		List<Object> values = TalendParquetUtils.groupFieldValueToList(group);
		MatcherAssert.assertThat("", values, Matchers.contains("element 1", "element 2", "element 3", "element 4"));

	}

	@Test
	public void testGetIntList() {
		MessageType schema = parseMessageType("message Schema { " //
				+ "  optional int64 fieldo; " //
				+ "  optional group field1 { " //
				+ "    repeated int32 field2 ; " //
				+ "  } " //
				+ "}"); //

		Group group = new SimpleGroup(schema.getType(1).asGroupType());
		group.add(0, 123);
		group.add(0, 345);
		group.add(0, 431);

		List<Object> values = TalendParquetUtils.groupFieldValueToList(group);
		MatcherAssert.assertThat("", values, Matchers.contains(123, 345, 431));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNestGroupList() {
		MessageType schema = parseMessageType("message Schema { " //
				+ "  optional int64 fieldo; " //
				+ "  optional group field1 { " //
				+ "    repeated group field2 {" //
				+ "      repeated double field3;" //
				+ "    } " //
				+ "  } " //
				+ "}"); //

		Group group = new SimpleGroup(schema.getType(1).asGroupType());

		Group nest1 = new SimpleGroup(schema.getType(1).asGroupType().getType(0).asGroupType());
		nest1.add(0, 123.0);
		nest1.add(0, 345.0);
		nest1.add(0, 431.0);

		Group nest2 = new SimpleGroup(schema.getType(1).asGroupType().getType(0).asGroupType());
		nest2.add(0, 2123.0);
		nest2.add(0, 2345.0);
		nest2.add(0, 2431.0);

		group.add(0, nest1);
		group.add(0, nest2);

		List<Object> values = TalendParquetUtils.groupFieldValueToList(group);

		MatcherAssert.assertThat("", (List<Object>) values.get(0), Matchers.contains(123.0, 345.0, 431.0));
		MatcherAssert.assertThat("", (List<Object>) values.get(1), Matchers.contains(2123.0, 2345.0, 2431.0));

	}

	@Test
	public void testNullGroupList() {
		List<Object> values = TalendParquetUtils.groupFieldValueToList(null);
		Assert.assertNull(values);
	}

	@Test
	public void testCreateGroupElementType() {
		Type emptyElement = TalendParquetUtils.createGroupElementType("field0", null);
		Assert.assertEquals(PrimitiveTypeName.BINARY, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", "1");
		Assert.assertEquals(PrimitiveTypeName.BINARY, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", 1.0);
		Assert.assertEquals(PrimitiveTypeName.DOUBLE, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", 1.0f);
		Assert.assertEquals(PrimitiveTypeName.FLOAT, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", 1);
		Assert.assertEquals(PrimitiveTypeName.INT32, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", 1L);
		Assert.assertEquals(PrimitiveTypeName.INT64, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupElementType("field0", true);
		Assert.assertEquals(PrimitiveTypeName.BOOLEAN, emptyElement.asPrimitiveType().getPrimitiveTypeName());

		// Nest group
		MessageType schema = parseMessageType("message Schema { " //
				+ "  optional group field1 { " //
				+ "    repeated group field2 {" //
				+ "      repeated double field3;" //
				+ "    } " //
				+ "  } " //
				+ "}"); //
		Group group = new SimpleGroup(schema.getType(0).asGroupType());
		Group nest1 = new SimpleGroup(schema.getType(0).asGroupType().getType(0).asGroupType());
		nest1.add(0, 123.0);
		nest1.add(0, 345.0);
		nest1.add(0, 431.0);
		Group nest2 = new SimpleGroup(schema.getType(0).asGroupType().getType(0).asGroupType());
		nest2.add(0, 2123.0);
		nest2.add(0, 2345.0);
		nest2.add(0, 2431.0);

		group.add(0, nest1);
		group.add(0, nest2);
		Assert.assertFalse("Should be group type", group.getType().isPrimitive());

		Assert.assertEquals(2, group.getFieldRepetitionCount(0));

		emptyElement = TalendParquetUtils.createGroupElementType("field0", group);
		Assert.assertFalse("Should be group type", emptyElement.isPrimitive());
		Assert.assertEquals(schema.getType(0).asGroupType(), emptyElement);
	}

	@Test
	public void testCreateGroupType() {
		GroupType emptyElement = TalendParquetUtils.createGroupType("field0", true, null);
		Assert.assertEquals(OriginalType.LIST, emptyElement.asGroupType().getOriginalType());
		Assert.assertEquals(OriginalType.UTF8, emptyElement.getType(0).asPrimitiveType().getOriginalType());

		emptyElement = TalendParquetUtils.createGroupType("field0", true, 2);
		Assert.assertEquals(OriginalType.LIST, emptyElement.asGroupType().getOriginalType());
		Assert.assertEquals(PrimitiveTypeName.INT32, emptyElement.getType(0).asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupType("field0", true, Byte.valueOf("1"));
		Assert.assertEquals(OriginalType.LIST, emptyElement.asGroupType().getOriginalType());
		Assert.assertEquals(OriginalType.INT_8, emptyElement.getType(0).asPrimitiveType().getOriginalType());
		Assert.assertEquals(PrimitiveTypeName.INT32, emptyElement.getType(0).asPrimitiveType().getPrimitiveTypeName());

		emptyElement = TalendParquetUtils.createGroupType("field0", true, Short.valueOf("1"));
		Assert.assertEquals(OriginalType.LIST, emptyElement.asGroupType().getOriginalType());
		Assert.assertEquals(OriginalType.INT_16, emptyElement.getType(0).asPrimitiveType().getOriginalType());
		Assert.assertEquals(PrimitiveTypeName.INT32, emptyElement.getType(0).asPrimitiveType().getPrimitiveTypeName());
	}

	@Test
	public void testWriteGroupField() {
		Group group = null;
		MessageType schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated int32 array;" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		List<?> values = Arrays.asList(1, 2, 3);
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(3, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated int32 array(INT_8);" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(Byte.valueOf("1"), Byte.valueOf("2"));
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(2, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated int32 array(INT_16);" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(Short.valueOf("1"));
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(1, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated int64 array;" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(1L, 2L, 3L);
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(3, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated double array;" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(1.0, 2.0, 3.0);
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(3, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated float array;" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(1.0f, 2.0f, 3.0f);
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(3, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated binary array (UTF8);" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList("element 1", "element 2");
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(2, group.getFieldRepetitionCount(0));

		schema = parseMessageType("message Schema { " //
				+ "    optional group field0 (LIST) {" + "    repeated boolean array ;" + "  } " //
				+ "}"); //
		group = new SimpleGroup(schema.getType(0).asGroupType());
		values = Arrays.asList(true, false);
		TalendParquetUtils.writeGroupField(group, values);
		Assert.assertEquals(2, group.getFieldRepetitionCount(0));
	}

	@Test
	public void testDecimalAnnotation() {
		Group group = null;
		MessageType schema = new MessageType("DecimalMessage", new PrimitiveType(REQUIRED, FIXED_LEN_BYTE_ARRAY, 16,
				"aDecimal", DECIMAL, new DecimalMetadata(38, 2), null));
		BigDecimal decimalValue = new BigDecimal("1234423199.9999");

		group = new SimpleGroup(schema);
		group.append("aDecimal", TalendParquetUtils.decimalToBinary(decimalValue, 5));
		Assert.assertEquals(decimalValue.setScale(5), TalendParquetUtils.binaryToDecimal(group.getBinary(0, 0), 38, 5));

		group = new SimpleGroup(schema);
		group.append("aDecimal", TalendParquetUtils.decimalToBinary(decimalValue, 4));
		Assert.assertEquals(decimalValue, TalendParquetUtils.binaryToDecimal(group.getBinary(0, 0), 38, 4));

		decimalValue = new BigDecimal("1234");
		group = new SimpleGroup(schema);
		group.append("aDecimal", TalendParquetUtils.decimalToBinary(decimalValue, 0));
		Assert.assertEquals(decimalValue, TalendParquetUtils.binaryToDecimal(group.getBinary(0, 0), 10, 0));

	}

}
