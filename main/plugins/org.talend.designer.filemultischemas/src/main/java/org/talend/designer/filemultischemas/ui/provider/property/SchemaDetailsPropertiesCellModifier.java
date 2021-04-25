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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.designer.filemultischemas.data.EPropertyName;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;
import org.talend.designer.filemultischemas.managers.UIManager;
import org.talend.designer.filemultischemas.ui.provider.SchemaDetailsCellModifier;

/**
 * cLi class global comment. Detailled comment
 */
public class SchemaDetailsPropertiesCellModifier extends SchemaDetailsCellModifier {

    // hywang add for feature 7373
    private UIManager uiManager;

    public SchemaDetailsPropertiesCellModifier(TreeViewer schemaDetailsViewer, UIManager uiManager) {
        super(schemaDetailsViewer);
        this.uiManager = uiManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
        if (element instanceof MultiMetadataColumn) {
            MultiMetadataColumn column = (MultiMetadataColumn) element;
            SchemasKeyData container = column.getContainer();
            if (container == null) {
                return false;
            }
            List<MultiMetadataColumn> metadataColumnsInModel = container.getMetadataColumnsInModel();
            /*
             * ingore record type(first column);
             *
             * if existed key column, should not edit other key.
             */
            if (EPropertyName.KEY.name().equals(property)
                    && (metadataColumnsInModel.indexOf(column) == uiManager.getSelectedColumnIndex() || UIManager
                            .existedKeyColumn(metadataColumnsInModel, column))) {
                return false;
            }
            if (!EPropertyName.TAGLEVEL.name().equals(property)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object element, String property) {
        if (element instanceof MultiMetadataColumn) {
            MultiMetadataColumn column = (MultiMetadataColumn) element;
            if (EPropertyName.NAME.name().equals(property)) {
                return MultiSchemasUtil.validateValue(column.getLabel());
            } else if (EPropertyName.TAGLEVEL.name().equals(property)) {
                // if (column.getTagLevel() != null) {
                // return column.getTagLevel().toString();
                // } else {
                // return "";
                // }
            } else if (EPropertyName.KEY.name().equals(property)) {
                return column.isKey();
            } else if (EPropertyName.TYPE.name().equals(property)) {
                return MultiSchemasUtil.getTalendTypeIndex(column.getTalendType());
            } else if (EPropertyName.LENGTH.name().equals(property)) {
                return MultiSchemasUtil.getAndCheckIntgerValue(column.getLength());
                // } else if (EPropertyName.CARD.name().equals(property)) {
                // return MultiSchemasUtil.validateValue(column.getCard());
            } else if (EPropertyName.PATTERN.name().equals(property)) {
                return MultiSchemasUtil.validateValue(column.getPattern());
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
        if (element instanceof TreeItem) {
            final Object data = ((TreeItem) element).getData();
            if (data != null && data instanceof MultiMetadataColumn) {
                MultiMetadataColumn column = (MultiMetadataColumn) data;
                if (EPropertyName.NAME.name().equals(property)) {
                    if (value != null
                            && UIManager.checkSchemaDetailsValue(getSchemaDetailsViewer(), null, EPropertyName.NAME, value)) {
                        column.setLabel((String) value);
                    }
                } else if (EPropertyName.TAGLEVEL.name().equals(property)) {
                    // column.setTagLevel(Integer.parseInt((String) value));
                } else if (EPropertyName.KEY.name().equals(property)) {
                    column.setKey((Boolean) value);
                } else if (EPropertyName.TYPE.name().equals(property)) {
                    final String talendType = MultiSchemasUtil.getTalendTypeByIndex((Integer) value);
                    if (talendType != null) {
                        column.setTalendType(talendType);
                    }
                } else if (EPropertyName.LENGTH.name().equals(property)) {
                    if (!"".equals(value)) { //$NON-NLS-1$
                        if (UIManager.checkSchemaDetailsValue(getSchemaDetailsViewer(), null, EPropertyName.LENGTH, value)) {
                            column.setLength(Integer.parseInt((String) value));
                        }// else, don't change it.
                    } else {
                        column.setLength(null);
                    }
                    // } else if (EPropertyName.CARD.name().equals(property)) {
                    // column.setCard((String) value);
                } else if (EPropertyName.PATTERN.name().equals(property)) {
                    column.setPattern((String) value);
                }
                this.getSchemaDetailsViewer().update(column, null);
            }
        }
    }

}
