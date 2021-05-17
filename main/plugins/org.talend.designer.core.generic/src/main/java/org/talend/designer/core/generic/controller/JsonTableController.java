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
package org.talend.designer.core.generic.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.generic.ui.JsonFieldModel;
import org.talend.designer.core.generic.ui.JsonTableHandler;
import org.talend.designer.core.generic.ui.JsonTableVO;
import org.talend.designer.core.generic.ui.JsonTableView;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 *
 * created by ycbai on 2016年9月5日 Detailled comment
 *
 */
public class JsonTableController extends AbstractElementPropertySectionController {

    private static final int MIN_NUMBER_ROWS = 1;

    private List<Map<String, Object>> rows = new ArrayList<>();

    private int rowsNum = 0;

    private List<String> titles = new ArrayList<>();

    private boolean readonly = true; // Set this to true for now.

    private boolean hideToolbar = true; // Set this to true for now.

    public JsonTableController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(final Composite parentComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        final Composite container = parentComposite;

        JsonTableView tableEditorView = generateTableEditorView(parentComposite, param);
        final Composite mainComposite = tableEditorView.getMainComposite();
        mainComposite.setData(PARAMETER_NAME, param.getName());

        String labelDisplayName = param.getDisplayName();
        CLabel label = getWidgetFactory().createCLabel(container, labelDisplayName);
        FormData formData = new FormData();
        if (lastControl != null) {
            formData.left = new FormAttachment(lastControl, 0);
        } else {
            formData.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        formData.top = new FormAttachment(0, top);
        label.setLayoutData(formData);
        formData = new FormData();

        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(label);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        if ((labelSize.x + (ITabbedPropertyConstants.HSPACE * 2)) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + (ITabbedPropertyConstants.HSPACE * 2);
        }
        if (numInRow == 1) {
            if (lastControl != null) {
                formData.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                formData.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            formData.left = new FormAttachment(label, 0, SWT.RIGHT);
        }
        formData.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        formData.top = new FormAttachment(0, top);
        int height = adjustTableHeight(tableEditorView, param);
        formData.bottom = new FormAttachment(0, top + height);
        mainComposite.setLayoutData(formData);

        hashCurControls.put(param.getName(), tableEditorView.getExtendedTableViewer().getTableViewerCreator());

        this.dynamicProperty.setCurRowSize(height + ITabbedPropertyConstants.VSPACE);

        if (isInWizard()) {
            label.setAlignment(SWT.RIGHT);
            if (lastControl != null) {
                formData.right = new FormAttachment(lastControl, 0);
            } else {
                formData.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
            formData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow),
                    currentLabelWidth + ITabbedPropertyConstants.HSPACE);

            formData = (FormData) label.getLayoutData();
            formData.right = new FormAttachment(mainComposite, 0);
            formData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), 0);

            return label;
        }

        return mainComposite;
    }

    private int adjustTableHeight(JsonTableView tableEditorView, IElementParameter param) {
        final Table table = tableEditorView.getTable();
        int toolbarSize = 0;
        if (!hideToolbar) {
            Point size = tableEditorView.getExtendedToolbar().getToolbar().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            toolbarSize = size.y + 5;
        }
        int currentHeightEditor = table.getHeaderHeight() + rowsNum * table.getItemHeight() + table.getItemHeight() + toolbarSize;
        int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + toolbarSize;
        int height = Math.max(currentHeightEditor, minHeightEditor);
        return height;
    }

    private JsonTableView generateTableEditorView(Composite parent, IElementParameter param) {
        JsonTableVO vo = null;
        Object value = param.getValue();
        if (value != null) {
            String valueStr = TalendQuoteUtils.removeQuotesIfExist(String.valueOf(value));
            if (StringUtils.isNotBlank(valueStr)) {
                vo = JsonTableHandler.getInstance().parse(valueStr);
            }
        }
        if (vo != null) {
            rows = vo.getData();
            if (rows != null) {
                rowsNum = rows.size();
            }
            titles = vo.getTitles();
        }
        JsonFieldModel tableEditorModel = new JsonFieldModel(rows);
        JsonTableView tableEditorView = new JsonTableView(parent, tableEditorModel, readonly, !hideToolbar) {

            @Override
            protected List<String> getColumnTitles() {
                return titles;
            }
        };
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        tableEditorView.setReadOnly(param.isReadOnly() || param.isRepositoryValueUsed());
        tableEditorModel.setModifiedBeanListenable(tableEditorView.getTableViewerCreator());

        final Table table = tableEditorView.getTable();

        table.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());

        // add listener to tableMetadata (listen the event of the toolbars)
        tableEditorView.getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

            @Override
            public void handleEvent(ListenableListEvent event) {
                if (elem instanceof Node) {
                    Node node = (Node) elem;
                    node.checkAndRefreshNode();
                }
            }
        });

        return tableEditorView;
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        JsonTableView tableEditorView = generateTableEditorView(subComposite, param);
        int height = adjustTableHeight(tableEditorView, param);
        return height + ITabbedPropertyConstants.VSPACE;
    }

    private int getNumberLines(IElementParameter param) {
        int numlines = param.getNbLines();
        return numlines < MIN_NUMBER_ROWS ? MIN_NUMBER_ROWS : numlines;
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
    }

}
