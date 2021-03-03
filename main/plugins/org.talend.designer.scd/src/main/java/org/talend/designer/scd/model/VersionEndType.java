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
package org.talend.designer.scd.model;

import java.util.HashMap;
import java.util.Map;

import org.talend.designer.scd.ScdParameterConstants;

/**
 * DOC hcw class global comment. Detailled comment
 */
public enum VersionEndType {
    NULL(0, "NULL", ScdParameterConstants.NULL_VALUE), //$NON-NLS-1$
    FIXED_YEAR(1, "Fixed year value", ScdParameterConstants.FIXED_VALUE); //$NON-NLS-1$

    int index;

    String name; // for display

    String value; // for code generation parameter

    static String[] allTypeNames;

    static Map<Integer, VersionEndType> indexTypeMap;

    static Map<String, VersionEndType> valueTypeMap;

    VersionEndType(int i, String t, String v) {
        index = i;
        name = t;
        value = v;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static VersionEndType getTypeByValue(String v) {
        if (valueTypeMap == null) {
            valueTypeMap = new HashMap<String, VersionEndType>();
            for (VersionEndType t : values()) {
                valueTypeMap.put(t.getValue(), t);
            }
        }
        return valueTypeMap.get(v);
    }

    public static VersionEndType getTypeByIndex(int i) {
        if (indexTypeMap == null) {
            indexTypeMap = new HashMap<Integer, VersionEndType>();
            for (VersionEndType t : values()) {
                indexTypeMap.put(t.getIndex(), t);
            }
        }
        return indexTypeMap.get(i);
    }

    public static String[] getAllTypeNames() {
        if (allTypeNames == null) {
            allTypeNames = new String[values().length];
            int i = 0;
            for (VersionEndType t : values()) {
                allTypeNames[i++] = t.name;
            }
        }
        return allTypeNames;
    }

}
