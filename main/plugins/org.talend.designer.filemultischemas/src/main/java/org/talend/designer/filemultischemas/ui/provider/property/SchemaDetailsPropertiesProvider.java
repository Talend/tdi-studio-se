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
package org.talend.designer.filemultischemas.ui.provider.property;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.MultiSchemasMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;
import org.talend.designer.filemultischemas.managers.UIManager;
import org.talend.designer.filemultischemas.ui.provider.SchemaDetailsProvider;

/**
 * cLi class global comment. Detailled comment
 */
public class SchemaDetailsPropertiesProvider extends SchemaDetailsProvider {

    // hywang add for feature 7373
    private UIManager uiManager;

    public SchemaDetailsPropertiesProvider(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            return ((List) inputElement).toArray();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof MultiMetadataColumn) {
            MultiMetadataColumn column = (MultiMetadataColumn) parentElement;
            return column.getDataColumns().toArray();
        }
        if (parentElement instanceof MultiSchemasMetadataColumn) {
            return null;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof MultiSchemasMetadataColumn) {
            MultiSchemasMetadataColumn column = (MultiSchemasMetadataColumn) element;
            return column.getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        final Object[] children = getChildren(element);
        return children != null && children.length > 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 2) { // key
            if (element instanceof MultiMetadataColumn) {
                MultiMetadataColumn multiMetadataColumn = (MultiMetadataColumn) element;
                // first column(record type)
                SchemasKeyData container = multiMetadataColumn.getContainer();
                if (container != null && container.getMetadataColumnsInModel().indexOf(multiMetadataColumn) == 0) {
                    return null;
                }
                if (multiMetadataColumn.isKey()) {
                    // return ImageProvider.getImage(EImage.CHECKED_ICON);
                } else {
                    // return ImageProvider.getImage(EImage.UNCHECKED_ICON);
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (columnIndex == 0) { // column name
            if (element instanceof MultiMetadataColumn) {
                return ((MultiMetadataColumn) element).getLabel();
            }
            if (element instanceof MultiSchemasMetadataColumn) {
                return ((MultiSchemasMetadataColumn) element).getData();
            }
        } else if (columnIndex == 1) { // level
            if (element instanceof MultiMetadataColumn) {
                MultiMetadataColumn multiMetadataColumn = (MultiMetadataColumn) element;
                return validateValue(multiMetadataColumn.getContainerTagLevel());
            }
        } else if (columnIndex == 2) { // key
            if (element instanceof MultiMetadataColumn) {
                MultiMetadataColumn multiMetadataColumn = (MultiMetadataColumn) element;
                // first column(record type)
                SchemasKeyData container = multiMetadataColumn.getContainer();
                if (container != null
                        && container.getMetadataColumnsInModel().indexOf(multiMetadataColumn) == uiManager
                                .getSelectedColumnIndex()) {
                    return null;
                }
                return validateValue(multiMetadataColumn.isKey());
            }
        } else {
            if (element instanceof MultiSchemasMetadataColumn) {
                MultiSchemasMetadataColumn column = (MultiSchemasMetadataColumn) element;
                switch (columnIndex) {
                case 3: // type
                    return getTypeLabel(column.getTalendType());
                case 4: // length
                    return validateValue(column.getLength());
                    // case 4: // card
                    // return column.getCard();
                case 5: // pattern
                    return column.getPattern();
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    public Color getBackground(Object element, int columnIndex) {
        if (element instanceof MultiMetadataColumn) {
            return Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
     */
    public Font getFont(Object element, int columnIndex) {
        return null;
    }

}
