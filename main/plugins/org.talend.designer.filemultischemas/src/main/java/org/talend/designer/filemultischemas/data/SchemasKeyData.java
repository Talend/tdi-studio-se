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

import org.talend.commons.utils.data.list.UniqueStringGenerator;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * cLi class global comment. Detailled comment
 *
 * for schema tree node.
 */
public class SchemasKeyData {

    private SchemasKeyData parent;

    private String recordType;

    private String uniqueRecord;

    private List<SchemasKeyData> children = new ArrayList<SchemasKeyData>();

    private List<MultiSchemaRowData> rowsData = new ArrayList<MultiSchemaRowData>();

    private List<MultiMetadataColumn> metadataColumns = new ArrayList<MultiMetadataColumn>();

    private MultiMetadataColumn addedColumn;;

    private static final String defaultCard = TalendTextUtils.addQuotes(""); //$NON-NLS-1$

    private String card = defaultCard;

    private String separator;

    public SchemasKeyData(String recordType) {
        super();
        this.recordType = recordType;
    }

    public String getRecordType() {
        return this.recordType;
    }

    public String getUniqueRecord() {
        return this.uniqueRecord;
    }

    public void setUniqueRecord(String uniqueRecord) {
        this.uniqueRecord = uniqueRecord;
    }

    public String getSeparator() {
        return this.separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setParent(SchemasKeyData parent) {
        this.parent = parent;
    }

    public SchemasKeyData getParent() {
        return this.parent;
    }

    public String getCard() {
        if (this.getTagLevel() == 0) { // if no parent, will be no card.
            return defaultCard;
        }
        return this.card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void addRowsData(MultiSchemaRowData rowData) {
        if (rowData != null) {
            getRowsData().add(rowData);
            rowData.setParent(this);
        }
    }

    public List<MultiSchemaRowData> getRowsData() {
        return this.rowsData;
    }

    public void addChild(SchemasKeyData child) {
        addChild(-1, child); // add in to end
    }

    public void addChild(int index, SchemasKeyData child) {
        if (child != null && child != this) {
            // have contain in current
            if (getChildren().contains(child)) {
                return;
            }
            // delete from parent
            SchemasKeyData p = child.getParent();
            if (p != null) {
                p.getChildren().remove(child);
            }
            // avoid to recycle, child is parent of current before.
            if (isParentOfParent(this.getParent(), child)) {
                updateRecycleChildren(child);

            }
            // add to current
            if (index == -1 || index > getChildren().size()) {
                getChildren().add(child);
            } else {
                getChildren().add(index, child);
            }
            child.setParent(this);
        }
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    private void updateRecycleChildren(SchemasKeyData current) {
        SchemasKeyData p = current.getParent();
        for (SchemasKeyData tmpChild : current.getChildren()) {
            tmpChild.setParent(p);
            if (p != null) {
                p.getChildren().add(tmpChild);
            }
        }
        // not child for current.
        current.getChildren().clear();

    }

    private boolean isParentOfParent(SchemasKeyData parent, SchemasKeyData child) {
        if (parent == null) {
            return false;
        }
        if (parent == child) {
            return true;
        }
        return isParentOfParent(parent.getParent(), child);
    }

    public List<SchemasKeyData> getChildren() {
        return this.children;
    }

    public int getTagLevel() {
        int level = -1;
        SchemasKeyData p = this;
        while ((p = p.getParent()) != null) {
            level++;
        }
        return level;
    }

    public List<MultiMetadataColumn> getMetadataColumns() {
        return this.metadataColumns;
    }

    public void setMetadataColumns(List<MultiMetadataColumn> metadataColumns) {
        this.metadataColumns.clear();
        if (metadataColumns != null) {
            this.metadataColumns = metadataColumns;
        }
    }

    public List<MultiMetadataColumn> getMetadataColumnsInModel() {
        List<MultiMetadataColumn> modelColumns = new ArrayList<MultiMetadataColumn>(getMetadataColumns());
        generateAddedColumn();
        if (getTagLevel() > 0 && addedColumn != null) {
            modelColumns.add(addedColumn);
        }
        return modelColumns;
    }

    public MultiMetadataColumn getAddedColumn() {
        generateAddedColumn();
        return this.addedColumn;
    }

    public void setAddedColumn(MultiMetadataColumn addedColumn) {
        this.addedColumn = addedColumn;
    }

    public void generateAddedColumn() {
        String baseName = null;
        boolean needed = false;
        if (this.addedColumn != null) {
            // check name unique

            for (MultiMetadataColumn col : this.getMetadataColumns()) {
                if (col.getLabel().equals(this.addedColumn.getLabel())) {
                    needed = true;
                    baseName = this.addedColumn.getLabel();
                    break;
                }
            }
        } else {
            needed = true;
            // default
            baseName = ExternalMultiSchemasUIProperties.DEFAULT_COLUMN_NAME;
            this.addedColumn = new MultiMetadataColumn("Test"); //$NON-NLS-1$
            this.addedColumn.setContainer(this);
            this.addedColumn.setTalendType(JavaTypesManager.STRING.getId());
        }
        if (needed) {
            UniqueStringGenerator<MultiMetadataColumn> generator = new UniqueStringGenerator<MultiMetadataColumn>(baseName, this
                    .getMetadataColumns()) {

                @Override
                protected String getBeanString(MultiMetadataColumn bean) {
                    return bean.getLabel();
                }

            };
            this.addedColumn.setLabel(generator.getUniqueString());
        }
    }
}
