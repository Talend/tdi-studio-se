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
package org.talend.designer.filemultischemas.data;

/**
 * cLi class global comment. Detailled comment
 */
public enum EPropertyName {
    NAME("Name"),
    TAGLEVEL("TagLevel"),
    KEY("Key"),
    TYPE("Type"),
    // NULL("Null"),
    LENGTH("Length"),
    // PRECISION("Precision"),
    // CARD("Card"),
    PATTERN("Pattern"),

    ;

    private String name;

    EPropertyName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static int indexOf(EPropertyName property) {
        EPropertyName[] values = EPropertyName.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == property) {
                return i;
            }
        }
        return -1;
    }
}
