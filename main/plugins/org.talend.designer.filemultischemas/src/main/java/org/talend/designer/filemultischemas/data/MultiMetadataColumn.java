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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiMetadataColumn extends MultiSchemasMetadataColumn {

    private String recordType;

    private SchemasKeyData container;

    private List<MultiSchemasMetadataColumn> dataColumns = new ArrayList<MultiSchemasMetadataColumn>();

    public MultiMetadataColumn(String recordType) {
        super();
        this.recordType = recordType;
    }

    public String getRecordType() {
        return this.recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public void addDataColumns(MultiSchemasMetadataColumn column) {
        if (column == null) {
            return;
        }
        //
        this.dataColumns.add(column);
        column.setParent(this);
        // type
        final String talendType = column.getTalendType();
        if (getTalendType() == null) {
            setTalendType(talendType);
        } else {
            if (talendType != null) {
                String newType = null;
                if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                    newType = JavaDataTypeHelper.getCommonType(talendType, getTalendType());
                } else {
                    newType = PerlDataTypeHelper.getNewCommonType(talendType, getTalendType());
                }
                setTalendType(newType);
            }
        }
        // length
        final Integer length = column.getLength();
        if (getLength() == null || (length != null && length > getLength())) {
            setLength(length);
        }

    }

    public List<MultiSchemasMetadataColumn> getDataColumns() {
        return this.dataColumns;
    }

    public SchemasKeyData getContainer() {
        return this.container;
    }

    public void setContainer(SchemasKeyData container) {
        this.container = container;
    }

    public int getContainerTagLevel() {
        if (this.container != null) {
            return this.container.getTagLevel();
        }
        return 0;
    }
}
