// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.model.SegmentModel;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class HL7MultiSchemaUI extends HL7UI {

    private ComboViewer metaTableViewer;

    private Label schemaLabel;

    private static String LABEL_NAME = "Segment(As Schema)"; //$NON-NLS-N$

    public HL7MultiSchemaUI(Composite parent, HL7Manager hl7Manager) {
        super(parent, hl7Manager);
    }

    @Override
    protected void createCombo(Composite parent) {
        createLabel(parent);
        metaTableViewer = new ComboViewer(parent, SWT.NONE);
        metaTableViewer.setContentProvider(new ArrayContentProvider());
        metaTableViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof SegmentModel) {
                    return ((SegmentModel) element).getSeg().getName();
                }
                return super.getText(element);
            }

        });
        final Combo combo = metaTableViewer.getCombo();
        GridData data = new GridData();
        data.widthHint = 60;
        combo.setLayoutData(data);
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                updateCurrentMetadataTable();
                linker.removeAllLinks();
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }

        });

    }

    private MetadataTable getCurrentTable(String key, List<MetadataColumn> beans) {
        MetadataTable currentTable = null;
        if (beans != null) {
            MetadataColumn[] array = new MetadataColumn[beans.size()];
            int i = 0;
            for (MetadataColumn column : beans) {
                MetadataColumn newColumn = copyColumn(column);
                array[i] = newColumn;
                i++;
            }
            currentTable = buildCurrentTable(array, key);
        }
        return currentTable;
    }

    private MetadataTable buildCurrentTable(MetadataColumn[] beans, String schemaKey) {
        MetadataTable metatable = ConnectionFactory.eINSTANCE.createMetadataTable();
        String displayName = ""; //$NON-NLS-N$
        for (int i = 0; i < beans.length; i++) {
            MetadataColumn column = beans[i];
            String original = beans[i].getOriginalField();
            if (original != null && !"".equals(original)) {
                if (original.indexOf(TalendTextUtils.LBRACKET) > 0) {
                    original = original.substring(0, original.indexOf(TalendTextUtils.LBRACKET));
                }
            }
            if (i != beans.length - 1) {
                displayName = displayName + TalendTextUtils.QUOTATION_MARK + original + TalendTextUtils.QUOTATION_MARK + ",";
            } else {
                displayName = displayName + TalendTextUtils.QUOTATION_MARK + original + TalendTextUtils.QUOTATION_MARK;
            }
            if (column.getSourceType() == null) {
                column.setSourceType("id_String");
            }
            if (column.getTalendType() == null) {
                column.setTalendType("id_String");
            }
            // column.setLabel(column.getOriginalField()); // display user defined column name
            metatable.getColumns().add(column);
        }
        metatable.setLabel(schemaKey);
        return metatable;
    }

    public void initSchemaCombo(List<IMetadataTable> tables) {
        List<SegmentModel> segments = this.contentProvider.getAllSegmentsForMessage();
        metaTableViewer.setInput(segments);
        if (!isRepository) {
            initMappingMap(segments);
        }
        final Combo combo = metaTableViewer.getCombo();
        if (tables == null || tables.size() == 0) {
            combo.select(0);
        } else {
            IMetadataTable table = tables.get(0);
            String label = table.getLabel();
            int index = 0;
            for (int i = 0; i < segments.size(); i++) {
                String name = segments.get(i).getSeg().getName();
                if (label.equals(name)) {
                    index = i;
                    break;
                }
            }
            combo.select(index);
        }
        updateCurrentMetadataTable();

    }

    private void updateCurrentMetadataTable() {
        IStructuredSelection selection = (IStructuredSelection) metaTableViewer.getSelection();
        Object selectedObj = selection.getFirstElement();
        if (selectedObj != null) {
            String key = ((IModel) selectedObj).getDisplayName();
            Map m = hl7Manager.getSchemaRelationMap();
            List<MetadataColumn> beans = (List<MetadataColumn>) m.get(key);
            MetadataTable currentTable = getCurrentTable(key, beans);
            if (currentTable != null) {
                metadataEditor.setMetadataTable(currentTable);
                hl7SchemaEditorView.setExtendedTableModel(metadataEditor);
            } else {
                currentTable = ConnectionFactory.eINSTANCE.createMetadataTable();
                metadataEditor.setMetadataTable(currentTable);
            }
        }
    }

    private void initMappingMap(List<SegmentModel> segments) {
        for (SegmentModel sm : segments) {
            List<MetadataColumn> list = new ArrayList<MetadataColumn>();
            hl7Manager.getSchemaRelationMap().put(sm.getDisplayName(), list);
        }
    }

    private void createLabel(Composite parent) {
        schemaLabel = new Label(parent, SWT.NONE);
        schemaLabel.setText(LABEL_NAME);
    }

    public ComboViewer getMetaTableViewer() {
        return this.metaTableViewer;
    }

}
