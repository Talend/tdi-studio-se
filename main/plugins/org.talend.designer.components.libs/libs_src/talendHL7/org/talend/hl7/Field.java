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
package org.talend.hl7;

public class Field implements Comparable<Field> {

    public int field;

    public int rep;

    public int component;

    public int subComponent;

    private String value = "";

    public Field(int field) {
        this(field, 0, 1, 1);
    }

    public Field(int field, int rep, int component, int subComponent) {
        this.field = field;
        this.rep = rep;
        this.component = component;
        this.subComponent = subComponent;
    }

    public void setValue(String value) {
        if (value == null) {
            return;
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public int compareTo(Field other) {
        if (this.field != other.field) {
            return this.field > other.field ? 1 : -1;
        }
        if (this.rep != other.rep) {
            return this.rep > other.rep ? 1 : -1;
        }
        if (this.component != other.component) {
            return this.component > other.component ? 1 : -1;
        }
        if (this.subComponent != other.subComponent) {
            return this.subComponent > other.subComponent ? 1 : -1;
        }
        return 0;
    }

}
