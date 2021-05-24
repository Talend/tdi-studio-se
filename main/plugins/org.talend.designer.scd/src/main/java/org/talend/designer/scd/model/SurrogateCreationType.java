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
public enum SurrogateCreationType {
    AUTO_INCREMENT(0, "Auto increment", ScdParameterConstants.AUTO_INCREMENT), //$NON-NLS-1$
    INPUT_FIELD(1, "Input field", ScdParameterConstants.INPUT_FIELD), //$NON-NLS-1$
    ROUTINE(2, "Routine", ScdParameterConstants.ROUTINE), //$NON-NLS-1$
    TABLE_MAX(3, "Table max + 1", ScdParameterConstants.TABLE_MAX), //$NON-NLS-1$
    DB_SEQUENCE(4, "DB Sequence", ScdParameterConstants.DB_SEQUENCE); //$NON-NLS-1$

    int index;

    String name; // for display

    String value; // for code generation parameter

    static String[] allTypeNames;

    static Map<Integer, SurrogateCreationType> indexTypeMap;

    static Map<String, SurrogateCreationType> valueTypeMap;

    SurrogateCreationType(int i, String t, String v) {
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

    /**
     * Gets the type by the given name. Created by Marvin Wang on May 11, 2012.
     *
     * @param name
     * @return
     */
    public static SurrogateCreationType getTypeByName(String name) {
        SurrogateCreationType[] types = SurrogateCreationType.values();
        for (SurrogateCreationType type : types) {
            if (type.getName().equals(name))
                return type;
        }
        return null;
    }

    /**
     * Invokers must insure the given value <code>i</code> can map to the index of <code>SurrogateCreationType</code>.
     * If can not, please refer to the method {@link SurrogateCreationType#getTypeByName(String)}. Created by Marvin
     * Wang on May 11, 2012.
     *
     * @param i
     * @return
     */
    public static SurrogateCreationType getTypeByIndex(int i) {
        if (indexTypeMap == null) {
            indexTypeMap = new HashMap<Integer, SurrogateCreationType>();
            for (SurrogateCreationType t : values()) {
                indexTypeMap.put(t.getIndex(), t);
            }
        }
        return indexTypeMap.get(i);
    }

    public static SurrogateCreationType getTypeByValue(String v) {
        if (valueTypeMap == null) {
            valueTypeMap = new HashMap<String, SurrogateCreationType>();
            for (SurrogateCreationType t : values()) {
                valueTypeMap.put(t.getValue(), t);
            }
        }
        return valueTypeMap.get(v);
    }

    /**
     * Gets all the display code names like "AUTO_INCREMENT", "INPUT_FIELD" and so on.
     *
     * @return
     */
    public static String[] getAllDisplayCodeNames() {
        if (allTypeNames == null) {
            allTypeNames = new String[values().length];
            int i = 0;
            for (SurrogateCreationType t : values()) {
                allTypeNames[i++] = t.name();
            }
        }

        return allTypeNames;
    }

    public static String[] getAllTypeNames() {
        if (allTypeNames == null) {
            allTypeNames = new String[values().length];
            int i = 0;
            for (SurrogateCreationType t : values()) {
                allTypeNames[i++] = t.name;
            }
        }
        return allTypeNames;
    }

}
