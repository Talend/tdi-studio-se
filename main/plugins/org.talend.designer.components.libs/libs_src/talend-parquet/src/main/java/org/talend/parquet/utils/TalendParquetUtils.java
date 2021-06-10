package org.talend.parquet.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.DecimalMetadata;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Type;
import org.apache.parquet.schema.Type.Repetition;
import org.apache.parquet.schema.Types;
import org.apache.parquet.schema.Types.GroupBuilder;
import org.talend.parquet.data.Group;
import org.talend.parquet.data.simple.NanoTime;

public class TalendParquetUtils {

	public static final String ARRAY_FIELD_NAME = "array";

	public static PrimitiveType createPrimitiveType(String fieldName, boolean nullable, String primitiveType,
			String originalTypeName) {
		OriginalType originalType = null;
		if (originalTypeName != null) {
			originalType = OriginalType.valueOf(originalTypeName);
		}
		return new PrimitiveType((nullable ? Repetition.OPTIONAL : Repetition.REQUIRED),
				PrimitiveTypeName.valueOf(primitiveType), fieldName, originalType);
	}

	public static PrimitiveType createDecimalType(String fieldName, boolean nullable, int precision, int scale) {
		DecimalMetadata decimalMetadata = new DecimalMetadata(precision, scale);
		return new PrimitiveType((nullable ? Repetition.OPTIONAL : Repetition.REQUIRED),
				PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY, 16, fieldName, OriginalType.DECIMAL, decimalMetadata, null);
	}

	public static Type createGroupElementType(String fieldName, Object element) {
		if (element == null) {
			return Types.repeated(PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named(fieldName);
		}
		if (String.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named(fieldName);
		} else if (Double.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.DOUBLE).named(fieldName);
		} else if (Float.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.FLOAT).named(fieldName);
		} else if (Byte.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.INT32).as(OriginalType.INT_8).named(fieldName);
		} else if (Short.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.INT32).as(OriginalType.INT_16).named(fieldName);
		} else if (Integer.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.INT32).named(fieldName);
		} else if (Long.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.INT64).named(fieldName);
		} else if (Boolean.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.BOOLEAN).named(fieldName);
		} else if (Date.class.isInstance(element)) {
			return Types.repeated(PrimitiveTypeName.INT64).as(OriginalType.TIMESTAMP_MILLIS).named(fieldName);
		} else if (Group.class.isInstance(element)) {
			return ((Group) element).getType();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + element.getClass().getCanonicalName()
					+ " for group type field'" + fieldName + "'");
		}
	}

	public static GroupType createGroupType(String fieldName, boolean nullable, Object element) {
		GroupBuilder<GroupType> builder = null;
		if (nullable) {
			builder = Types.optionalGroup();
		} else {
			builder = Types.requiredGroup();
		}
		return builder.as(OriginalType.LIST).addField(createGroupElementType("array", element)).named(fieldName);
	}

	/*
	 * Here group only support List value with one field
	 */
	public static List<Object> groupFieldValueToList(Group group) {
		if (group == null) {
			return null;
		}
		List<Object> values = new ArrayList<>();
		int listSize = group.getFieldRepetitionCount(0);
		for (int elementIndex = 0; elementIndex < listSize; elementIndex++) {
			Type elelemntType = group.getType().getType(0);
			if (elelemntType.isPrimitive()) {
				PrimitiveType pType = elelemntType.asPrimitiveType();
				switch (pType.getPrimitiveTypeName()) {
				case INT64:
					if (OriginalType.TIMESTAMP_MILLIS == elelemntType.getOriginalType()) {
						values.add(new Date(group.getLong(0, elementIndex)));
					} else {
						values.add(group.getLong(0, elementIndex));
					}
					break;
				case INT32:
					values.add(group.getInteger(0, elementIndex));
					break;
				case BOOLEAN:
					values.add(group.getBoolean(0, elementIndex));
					break;
				case INT96:
					Binary value = group.getInt96(0, elementIndex);
					if (value != null) {
						NanoTime nanoTime = NanoTime.fromBinary(value);
						values.add(new Date(NanoTimeUtils.getTimestamp(nanoTime, false).getTime()));
					} else {
						values.add(value);
					}
					break;
				case FLOAT:
					values.add(group.getFloat(0, elementIndex));
					break;
				case DOUBLE:
					values.add(group.getDouble(0, elementIndex));
					break;
				default:
					values.add(group.getValueToString(0, elementIndex));
				}
			} else {
				values.add(groupFieldValueToList(group.getGroup(0, elementIndex)));
			}
		}
		return values;
	}

	public static void writeGroupField(Group nestGroup, List<?> values) {
		if (values == null || values.isEmpty()) {
			return;
		}
		// only support one field currently
		for (int i = 0; i < values.size(); i++) {
			Object element = values.get(i);
			if (String.class.isInstance(element)) {
				nestGroup.add(0, (String) element);
			} else if (Double.class.isInstance(element)) {
				nestGroup.add(0, (Double) element);
			} else if (Float.class.isInstance(element)) {
				nestGroup.add(0, (Float) element);
			} else if (Byte.class.isInstance(element)) {
				nestGroup.add(0, (Byte) element);
			} else if (Short.class.isInstance(element)) {
				nestGroup.add(0, (Short) element);
			} else if (Integer.class.isInstance(element)) {
				nestGroup.add(0, (Integer) element);
			} else if (Long.class.isInstance(element)) {
				nestGroup.add(0, (Long) element);
			} else if (Boolean.class.isInstance(element)) {
				nestGroup.add(0, (Boolean) element);
			} else if (Date.class.isInstance(element)) {
				nestGroup.add(0, ((Date) element).getTime());
			} else if (Group.class.isInstance(element)) {
				nestGroup.add(0, (Group) element);
			} else {
				throw new IllegalArgumentException("Unsupported type: " + element.getClass().getCanonicalName()
						+ " for group type field'" + nestGroup + "'");
			}
		}
	}

	public static BigDecimal binaryToDecimal(Binary value, int precision, int scale) {
		/*
		 * Precision <= 18 checks for the max number of digits for an unscaled long,
		 * else treat with big integer conversion
		 */
		if (precision <= 18) {
			ByteBuffer buffer = value.toByteBuffer();
			byte[] bytes = buffer.array();
			int start = buffer.arrayOffset() + buffer.position();
			int end = buffer.arrayOffset() + buffer.limit();
			long unscaled = 0L;
			int i = start;
			while (i < end) {
				unscaled = (unscaled << 8 | bytes[i] & 0xff);
				i++;
			}
			int bits = 8 * (end - start);
			long unscaledNew = (unscaled << (64 - bits)) >> (64 - bits);
			if (scale == 0 || unscaledNew <= -Math.pow(10, 18) || unscaledNew >= Math.pow(10, 18)) {
				return new BigDecimal(unscaledNew);
			} else {
				return BigDecimal.valueOf(unscaledNew / Math.pow(10, scale));
			}
		} else {
			return new BigDecimal(new BigInteger(value.getBytes()), scale);
		}
	}

	public static Binary decimalToBinary(BigDecimal decimalValue, int scale) {
		// First we need to make sure the BigDecimal matches our schema scale:
		decimalValue = decimalValue.setScale(scale, RoundingMode.HALF_UP);

		// Next we get the decimal value as one BigInteger (like there was no decimal
		// point)
		BigInteger unscaledDecimalValue = decimalValue.unscaledValue();

		// Finally we serialize the integer
		byte[] decimalBytes = unscaledDecimalValue.toByteArray();

		byte[] decimalBuffer = new byte[16];
		if (decimalBuffer.length >= decimalBytes.length) {
			// Because we set our fixed byte array size as 16 bytes, we need to
			// pad-left our original value's bytes with zeros
			int decimalBufferIndex = decimalBuffer.length - 1;
			for (int i = decimalBytes.length - 1; i >= 0; i--) {
				decimalBuffer[decimalBufferIndex] = decimalBytes[i];
				decimalBufferIndex--;
			}
		} else {
			throw new IllegalArgumentException(String.format("Decimal size: %d was greater than the allowed max: %d",
					decimalBytes.length, decimalBuffer.length));
		}
		return Binary.fromReusedByteArray(decimalBuffer);
	}

}
