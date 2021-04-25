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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AbstractAvroConverter;

/**
 * Converts DI char to Avro string and vice versa
 */
public class CharStringConverter extends AbstractAvroConverter<Character, String> {

    /**
     * Constructor sets {@link Character} class, which is type of DI data and string {@link Schema} as Avro type
     */
    public CharStringConverter() {
        super(Character.class, AvroUtils._string());
    }

    /**
     * Converts Avro string value to DI char value
     */
    @Override
    public Character convertToDatum(String value) {
        return value.charAt(0);
    }

    /**
     * Converts char DI value to Avro string value
     */
    @Override
    public String convertToAvro(Character value) {
        return value.toString();
    }

}
