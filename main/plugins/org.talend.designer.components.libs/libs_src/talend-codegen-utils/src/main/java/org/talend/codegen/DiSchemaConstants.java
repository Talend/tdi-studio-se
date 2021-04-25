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
package org.talend.codegen;

import org.apache.avro.Schema;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Constants that can be used as keys in an Avro {@link Schema} properties in order to remain feature-equivalent to the
 * existing Talend 6 IMetadataTable.
 *
 * Values that are null are always omitted from the properties. The type of the value is always String.
 */
public interface DiSchemaConstants {

    /*
     * Can apply to table and columns. --------------------------------------
     */

    String TALEND6_ID = "di.column.id"; //$NON-NLS-1$

    String TALEND6_COMMENT = "di.table.comment"; //$NON-NLS-1$

    String TALEND6_LABEL = "di.table.label"; //$NON-NLS-1$

    /** Property is present if readonly, otherwise not present. */
    String TALEND6_IS_READ_ONLY = SchemaConstants.TALEND_IS_LOCKED; // $NON-NLS-1$

    /** The key will have this as a prefix, pointing to the value. */
    String TALEND6_ADDITIONAL_PROPERTIES = "di.prop."; //$NON-NLS-1$

    /*
     * Table custom properties. ---------------------------------------------
     */

    String TALEND6_TABLE_NAME = "di.table.name"; //$NON-NLS-1$

    String TALEND6_TABLE_TYPE = "di.table.type"; //$NON-NLS-1$

    /*
     * Column custom properties. --------------------------------------------
     */

    /** Property is present if key, otherwise not present. */
    String TALEND6_COLUMN_IS_KEY = SchemaConstants.TALEND_COLUMN_IS_KEY; // $NON-NLS-1$

    String TALEND6_COLUMN_SOURCE_TYPE = SchemaConstants.TALEND_COLUMN_DB_TYPE;

    String TALEND6_COLUMN_TALEND_TYPE = "di.column.talendType"; //$NON-NLS-1$

    String TALEND6_COLUMN_PATTERN = SchemaConstants.TALEND_COLUMN_PATTERN;

    /** String representation of an int. */
    String TALEND6_COLUMN_LENGTH = SchemaConstants.TALEND_COLUMN_DB_LENGTH;

    /** String representation of an int. */
    String TALEND6_COLUMN_ORIGINAL_LENGTH = "di.column.originalLength"; //$NON-NLS-1$

    /** Property is present if nullable, otherwise not present. */
    String TALEND6_COLUMN_IS_NULLABLE = "di.column.isNullable"; //$NON-NLS-1$

    /** String representation of an int. */
    String TALEND6_COLUMN_PRECISION = SchemaConstants.TALEND_COLUMN_PRECISION;

    /** String representation of an int. */
    String TALEND6_COLUMN_SCALE = SchemaConstants.TALEND_COLUMN_SCALE;

    String TALEND6_COLUMN_DEFAULT = SchemaConstants.TALEND_COLUMN_DEFAULT;

    /** cf TDKN-36. to link to the custom fields of the studio */
    String TALEND6_COLUMN_CUSTOM = "di.column.custom"; //$NON-NLS-1$

    String TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME = SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME;

    String TALEND6_COLUMN_RELATED_ENTITY = "di.column.relatedEntity"; //$NON-NLS-1$

    String TALEND6_COLUMN_RELATIONSHIP_TYPE = "di.column.relationshipType"; //$NON-NLS-1$

    String TALEND6_DYNAMIC_COLUMN_POSITION = "di.dynamic.column.position"; //$NON-NLS-1$

    String TALEND6_DYNAMIC_COLUMN_NAME = "di.dynamic.column.name"; //$NON-NLS-1$

    String TALEND6_DYNAMIC_COLUMN_ID = "di.dynamic.column.id"; //$NON-NLS-1$

    String TALEND6_DYNAMIC_COLUMN_COMMENT = "di.dynamic.column.comment"; //$NON-NLS-1$

    /** Property is present if readonly, otherwise not present. */
    String TALEND6_DYNAMIC_IS_READ_ONLY = "di.dynamic.readOnly"; //$NON-NLS-1$

    /** The key will have this as a prefix, pointing to the value. */
    String TALEND6_DYNAMIC_ADDITIONAL_PROPERTIES = "di.dynamic.prop."; //$NON-NLS-1$

    /** tell Avro converter how to process Original Avro logical type time
     * if value is "TALEND_DATE", it mean use Talend Date, if not, will use Talend Integer like before
     * we add this only one purpose : for the old job, we keep Talend Integer, for new job, we use Talend Date
     * */
    public static final String LOGICAL_TIME_TYPE_AS = "LOGICAL_TIME_TYPE_AS";

    public static final String AS_TALEND_DATE = "TALEND_DATE";

}
