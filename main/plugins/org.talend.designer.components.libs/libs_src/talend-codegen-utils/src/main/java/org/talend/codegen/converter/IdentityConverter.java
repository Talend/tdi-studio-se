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
package org.talend.codegen.converter;

import org.apache.avro.Schema;
import org.talend.daikon.avro.converter.AbstractAvroConverter;

/**
 * Doesn't convert data. It returns data unchanged. It is used for following avro data types:
 * <ol>
 * <li>array</li>
 * <li>boolean</li>
 * <li>bytes</li>
 * <li>int</li>
 * <li>long</li>
 * <li>float</li>
 * <li>double</li>
 * <li>String</li>
 * <li>Logical Time</li>
 * <li>...</li>
 * </ol>
 *
 */
public class IdentityConverter extends AbstractAvroConverter<Object, Object> {

    /**
     * Reference to itself for Singleton pattern implementation
     */
    private static IdentityConverter instance;

    /**
     * Private constructor for Singleton pattern implementation
     * Sets both Di {@link Class} and Avro {@link Schema} null, as this converter supports multiple
     * types
     * Corresponding methods {@link this#getDatumClass()} and {@link this#getSchema()} are not supported
     */
    private IdentityConverter() {
        super(null, null);
    }

    /**
     * Returns instance of this class
     *
     * @return instance of this class
     */
    public static IdentityConverter getInstance() {
        if (instance == null) {
            instance = new IdentityConverter();
        }
        return instance;
    }

    /**
     * It is supposed this method won't be called.
     * If client code will require this method, this class may be extended
     *
     * @throws {@link UnsupportedOperationException}
     */
    @Override
    public Schema getSchema() {
        throw new UnsupportedOperationException();
    }

    /**
     * It is supposed this method won't be called.
     * If client code will require this method, this class may be extended
     *
     * @throws {@link UnsupportedOperationException}
     */
    @Override
    public Class<Object> getDatumClass() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns data value unchanged
     *
     * @param value Avro data value
     * @return unchanged data
     */
    @Override
    public Object convertToDatum(Object value) {
        return value;
    }

    /**
     * Returns data value unchanged
     *
     * @param value DI data value
     * @return unchanged data
     */
    @Override
    public Object convertToAvro(Object value) {
        return value;
    }

}
