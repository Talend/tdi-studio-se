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
package org.talend.designer.core.ui.editor.properties.connections;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.metadata.dialog.CustomTableManager;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * Main Section of the property for the connections. <br/>
 *
 * $Id$
 *
 */
public class MainConnectionSection extends DynamicTabbedPropertySection {

    private MetadataTableEditorView metadataTableEditorView;

    private MetadataTableEditor metadataTableEditor;

    public MainConnectionSection() {
        super(EComponentCategory.MAIN);
    }

    @Override
    public void addComponents(boolean forceRedraw) {
        if (conSchema()) {
            disposeChildren();
            curRowSize = 0;

            List<? extends IElementParameter> listParam = ((Connection) elem).getSource().getElementParameters();

            generator.initController(this);
            for (IElementParameter cur : listParam) {
                if ((cur.getFieldType() == EParameterFieldType.SCHEMA_TYPE || cur.getFieldType() == EParameterFieldType.SCHEMA_REFERENCE)
                        && (cur.getContext().equals(((Connection) elem).getConnectorName()))) {
                    AbstractElementPropertySectionController contorller = generator.getController(
                            EParameterFieldType.SCHEMA_TYPE, this);
                    contorller.createControl(composite, cur, 0, 0, 0, null);
                }
            }

            FormData data = new FormData();
            data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
            data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            data.top = new FormAttachment(0, curRowSize + ITabbedPropertyConstants.VSPACE);
            data.width = 300; // to correct bug of table growing indefinitly

            IMetadataTable outputMetaTable = ((Connection) elem).getMetadataTable();
            if (outputMetaTable != null) {
                // Composite compositeEditorView = new Composite(composite, SWT.BORDER);
                // compositeEditorView.setLayoutData(data);

                metadataTableEditor = new MetadataTableEditor(outputMetaTable, "Schema from " //$NON-NLS-1$
                        + ((Connection) elem).getSource().getElementName() + " output "); //$NON-NLS-1$
                metadataTableEditorView = new MetadataTableEditorView(composite, SWT.NONE, metadataTableEditor, true, false,
                        true, false);
                MetadataDialog.initializeMetadataTableView(metadataTableEditorView, ((Connection) elem).getSource(),
                        outputMetaTable);
                metadataTableEditorView.initGraphicComponents();
                metadataTableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
                CustomTableManager.addCustomManagementToTable(metadataTableEditorView, true);
                Composite compositeEditorView = metadataTableEditorView.getMainComposite();
                compositeEditorView.setLayoutData(data);
                // compositeEditorView.getParent().layout();

                Table table = metadataTableEditorView.getTable();
                int currentHeightEditor = table.getHeaderHeight() + outputMetaTable.getListColumns().size()
                        * table.getItemHeight() + table.getItemHeight() + 50;
                curRowSize = currentHeightEditor + ITabbedPropertyConstants.VSPACE + curRowSize;

            }
            super.addComponents(forceRedraw, false);
        } else if (conIf()) {
            super.addComponents(forceRedraw);
        } else {
            disposeChildren();
        }
    }

    private void disposeChildren() {
        // Empty the composite before use (kinda refresh) :
        Control[] ct = composite.getChildren();
        for (Control element : ct) {
            element.dispose();
        }
    }

    private boolean conIf() {
        Connection connection = (Connection) elem;
        return connection.getLineStyle() == EConnectionType.RUN_IF || connection.getLineStyle() == EConnectionType.ROUTE_WHEN;
    }

    private boolean conSchema() {
        Connection connection = (Connection) elem;
        return connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA);
    }
}
