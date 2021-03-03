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
package org.talend.sqlbuilder.erdiagram.ui.nodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;

/**
 *  qzhang class global comment. Detailled comment <br/>
 *
 * $Id: Column.java 2006-12-25 下午03:03:20 +0000 (2006-12-25) qzhang $
 *
 */
public class Column extends Element {

    /**
     *
     */
    private static final long serialVersionUID = 0L;

    public static final String PROP_SELECTED = "selected"; //$NON-NLS-1$

    public static final String PROP_TALBE = "table"; //$NON-NLS-1$

    public static final String PROP_INPUTS = "INPUTS"; //$NON-NLS-1$

    public static final String PROP_OUTPUTS = "OUTPUTS"; //$NON-NLS-1$

    private Table table;

    private boolean selected;

    private List<Relation> inputs;

    private List<Relation> outputs;

    private MetadataColumn metadataColumn;

    private String columnName;

    private Dimension size = new Dimension();

    /**
     *  yzhang Column constructor comment.
     */
    public Column() {
        inputs = new ArrayList<Relation>();
        outputs = new ArrayList<Relation>();
    }

    /**
     *  yzhang Comment method "setSelected".
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        fireStructureChange(PROP_SELECTED, this.selected);
    }

    /**
     *  yzhang Comment method "isSelected".
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     *  yzhang Comment method "getTable".
     *
     * @return
     */
    public Table getTable() {
        return this.table;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuider.erdiagram.model.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return columnName;
    }

    public void setElementName(String name) {
        this.columnName = name;
        size.height = 23;

        size.width = Math.round(columnName.length() * 5.2f) + 45;
    }

    public void setMetadataColumn(MetadataColumn metadataColumn) {
        this.metadataColumn = metadataColumn;
        if (this.metadataColumn != null) {
            setElementName(this.metadataColumn.getOriginalField());
        }
    }

    public void setTable(Table table) {
        this.table = table;
        fireStructureChange(PROP_TALBE, this.table);
    }

    public List<Relation> getInputs() {
        return this.inputs;
    }

    public List<Relation> getOutputs() {
        return this.outputs;
    }

    public void addInputRelation(Relation relation) {
        this.inputs.add(relation);
        fireStructureChange(PROP_INPUTS, this.inputs);
    }

    public void removeInputRelation(Relation relation) {
        this.inputs.remove(relation);
        fireStructureChange(PROP_INPUTS, this.inputs);
    }

    public void addOutputRelation(Relation relation) {
        this.outputs.add(relation);
        fireStructureChange(PROP_OUTPUTS, this.outputs);
    }

    public void removeOutputRelation(Relation relation) {
        this.outputs.remove(relation);
        fireStructureChange(PROP_OUTPUTS, this.outputs);
    }

    public Dimension getSize() {
        return this.size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }
}
