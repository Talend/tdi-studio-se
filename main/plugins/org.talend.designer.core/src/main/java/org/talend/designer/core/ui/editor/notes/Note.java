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
package org.talend.designer.core.ui.editor.notes;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.TalendEditor;

/**
 *
 */
public class Note extends Element {

    private Point location;

    private Dimension size = new Dimension(TalendEditor.GRID_SIZE * 3, TalendEditor.GRID_SIZE * 2);

    private boolean opaque = true;

    private String text = Messages.getString("Note.DefaultText"); //$NON-NLS-1$

    private boolean readOnly;

    private List elementParameter;

    private String noteColor = "255;255;203"; //$NON-NLS-1$

    private String textColor = "0;0;0"; //$NON-NLS-1$

    private String noteLineColor = "237;201;122"; //$NON-NLS-1$

    private IProcess2 process;

    /**
     * tang Note constructor comment.
     */
    public Note() {
        super();
        ElementParameter param = new ElementParameter(this);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTE_COLOR.getName());
        param.setValue(noteColor); // default note color
        param.setDisplayName(EParameterName.NOTE_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.COLOR);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(true);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_COLOR.getName());
        param.setValue(textColor); // default note color
        param.setDisplayName(EParameterName.NOTETXT_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.COLOR);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(3);
        param.setReadOnly(false);
        param.setRequired(true);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_LEFT.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.NOTETXT_LEFT.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_RIGHT.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.NOTETXT_RIGHT.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_CENTER.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.NOTETXT_CENTER.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTELABEL_CENTER.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.NOTELABEL_CENTER.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_TOP.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.NOTETXT_TOP.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTETXT_BOTTOM.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.NOTETXT_BOTTOM.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
        param = new ElementParameter(this);
        param.setName(EParameterName.NOTE_FONT.getName());
        param.setValue("Time New Roman"); //$NON-NLS-1$
        param.setDisplayName(EParameterName.NOTE_FONT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.FONT_SIZE.getName());
        param.setValue("10"); //$NON-NLS-1$
        param.setDisplayName(EParameterName.FONT_SIZE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.FONT_BOLD.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.FONT_BOLD.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.FONT_ITALIC.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.FONT_ITALIC.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.NOTE_LINECOLOR.getName());
        param.setValue(noteLineColor);
        param.setDisplayName(EParameterName.FONT_ITALIC.getDisplayName());
        param.setFieldType(EParameterFieldType.COLOR);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

    }

    @Override
    public String getElementName() {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
        firePropertyChange("", null, location); //$NON-NLS-1$
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
        firePropertyChange("", null, size); //$NON-NLS-1$
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        // autoAdjustFigureSize(text);
        this.text = text;
        firePropertyChange("", null, text); //$NON-NLS-1$
    }

    /**
     * It is used for auto-adjust the size of fingure to adapt the text modification.
     *
     * @param text
     */
    private void autoAdjustFigureSize(String text) {
        int length = text.length() - this.text.length();
        int adjustLength = TalendEditor.GRID_SIZE / 4;
        if (length < 0) {
            adjustLength = -TalendEditor.GRID_SIZE / 4;
        }
        if (length != 0 && text.length() != 0) {
            this.size.width = this.size.width + length * 3 + adjustLength;
        } else {
            this.size = new Dimension(TalendEditor.GRID_SIZE * 3, TalendEditor.GRID_SIZE * 2);
        }
    }

    public boolean isOpaque() {
        return opaque;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
        firePropertyChange("", null, opaque); //$NON-NLS-1$
    }

    /**
     * Getter for elementParameter.
     *
     * @return the elementParameter
     */
    public List getElementParameter() {
        return this.elementParameter;
    }

    /**
     * Sets the elementParameter.
     *
     * @param elementParameter the elementParameter to set
     */
    public void setElementParameter(List elementParameter) {
        this.elementParameter = elementParameter;
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    // public IProcess2 getProcess() {
    // return this.process;
    // }
    public void refresh() {
        firePropertyChange("", null, ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public IProcess2 getProcess() {
        return this.process;
    }

    public void setProcess(IProcess2 process) {
        this.process = process;
    }

}
