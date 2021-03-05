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
package org.talend.designer.core.ui.editor.properties.controllers;

public enum DataTypeEnum {

    DateTime("Timestamp"),
    Text("String"),
    Decimal("Double"),
    SingleAssociation("Long"),
    MultiAssociation("List"),
    FileData("String"),
    Enum("String");

    protected String typeName;

    DataTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public static String compareAndRetrieveTypeName(String typeName) {
        String returnValue = typeName;
        if (typeName.equals(SingleAssociation.name()) || typeName.equals(MultiAssociation.name())) {
            returnValue = SingleAssociation.typeName;
        } else if (typeName.equals(DateTime.name())) {
            returnValue = DateTime.typeName;
        } else if (typeName.equals(Text.name())) {
            returnValue = Text.typeName;
        } else if (typeName.equals(Decimal.name())) {
            returnValue = Decimal.typeName;
        } else if (typeName.equals(FileData.name())) {
            returnValue = FileData.typeName;
        } else if (typeName.equals(Enum.name())) {
            returnValue = Enum.typeName;
        }
        return returnValue;
    }
}
