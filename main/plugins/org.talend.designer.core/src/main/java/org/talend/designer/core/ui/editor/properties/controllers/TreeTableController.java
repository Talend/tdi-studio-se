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

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class TreeTableController extends AbstractElementPropertySectionController {

    /**
     *
     */
    private static final int MIN_NUMBER_ROWS = 10;

    /**
     * DOC hcyi TreeTableController constructor comment.
     *
     * @param dp
     */
    public TreeTableController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(final Composite parentComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, int top, final Control lastControlPrm) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        final Composite container = parentComposite;

        final TreeViewer viewer = new TreeViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        viewer.getTree().setHeaderVisible(true);
        // key column
        TreeColumn column = new TreeColumn(viewer.getTree(), SWT.LEFT);
        column.setText("Key");
        column.setWidth(150);

        // columns for values
        if (param.getValue() != null) {
            List<Map<String, Object>> values = (List<Map<String, Object>>) param.getValue();
            for (int i = 0; i < values.size(); i++) {
                column = new TreeColumn(viewer.getTree(), SWT.LEFT);
                column.setText("Value" + i);
                column.setWidth(100);
            }
        }
        viewer.setContentProvider(new MyTreeContentProvider(param));
        viewer.setLabelProvider(new MyTableLableProvider(param));
        viewer.setInput(param);

        CLabel labelLabel2 = getWidgetFactory().createCLabel(container, param.getDisplayName());
        FormData formData = new FormData();
        if (lastControlPrm != null) {
            formData.left = new FormAttachment(lastControlPrm, 0);
        } else {
            formData.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        formData.top = new FormAttachment(0, top);
        labelLabel2.setLayoutData(formData);
        if (numInRow != 1) {
            labelLabel2.setAlignment(SWT.RIGHT);
        }
        // *********************
        formData = new FormData();
        int currentLabelWidth2 = STANDARD_LABEL_WIDTH;
        GC gc2 = new GC(labelLabel2);
        Point labelSize2 = gc2.stringExtent(param.getDisplayName());
        gc2.dispose();

        boolean needOffset = true;
        if ((labelSize2.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth2) {
            currentLabelWidth2 = labelSize2.x + ITabbedPropertyConstants.HSPACE;
            needOffset = false;
        }

        int tableHorizontalOffset = -5;
        if (numInRow == 1) {
            if (lastControlPrm != null) {
                if (needOffset) {
                    formData.left = new FormAttachment(lastControlPrm, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    formData.left = new FormAttachment(lastControlPrm, currentLabelWidth2);
                }
            } else {
                if (needOffset) {
                    formData.left = new FormAttachment(0, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    formData.left = new FormAttachment(0, currentLabelWidth2);
                }
            }
        } else {
            formData.left = new FormAttachment(labelLabel2, 0 + tableHorizontalOffset, SWT.RIGHT);
        }
        formData.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        formData.top = new FormAttachment(0, top);

        int toolbarSize = 0;
        if (!param.isBasedOnSchema()) {
            Point size = viewer.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            toolbarSize = size.y - 95;
        }
        int currentHeightEditor = viewer.getTree().getHeaderHeight() + param.getListItemsValue().length
                * viewer.getTree().getItemHeight() + viewer.getTree().getItemHeight() + toolbarSize;
        int minHeightEditor = viewer.getTree().getHeaderHeight() + getNumberLines(param) * viewer.getTree().getItemHeight()
                + viewer.getTree().getItemHeight() + toolbarSize;
        int ySize2 = Math.max(currentHeightEditor, minHeightEditor);

        formData.bottom = new FormAttachment(0, top + ySize2);
        viewer.getTree().setLayoutData(formData);

        this.dynamicProperty.setCurRowSize(ySize2 + ITabbedPropertyConstants.VSPACE);

        top += this.dynamicProperty.getCurRowSize();

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent arg0) {

    }

    @Override
    public void refresh(IElementParameter param, boolean check) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

    private int getNumberLines(IElementParameter param) {
        int numlines = param.getNbLines();
        return numlines < MIN_NUMBER_ROWS ? MIN_NUMBER_ROWS : numlines;
    }

    /**
     *
     * DOC hcyi TreeTableController class global comment. Detailled comment
     */
    class MyTreeContentProvider implements ITreeContentProvider {

        final IElementParameter param;

        public MyTreeContentProvider(final IElementParameter param) {
            this.param = param;
        }

        public Object[] getChildren(Object parentElement) {
            final int length = param.getListItemsValue().length;
            if (parentElement instanceof IElementParameter && length > 0) {
                // from 1 to length
                Object[] children = Arrays
                        .copyOfRange(param.getListItemsValue(), 1, length, param.getListItemsValue().getClass());
                return children;
            } else {
                return null;
            }
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            return param.getListItemsValue().length > 0 && param.getListItemsValue()[0] == element;
        }

        public Object[] getElements(Object inputElement) {
            // only the first parameter as root
            return new Object[] { ((IElementParameter) param).getListItemsValue()[0] };
        }

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }
    }

    /**
     *
     * DOC hcyi TreeTableController class global comment. Detailled comment
     */
    class MyTableLableProvider implements ITableLabelProvider {

        final IElementParameter param;

        public MyTableLableProvider(final IElementParameter param) {
            this.param = param;
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IElementParameter) {
                IElementParameter testParam = (IElementParameter) element;
                if (columnIndex == 0) {
                    return testParam.getValue().toString();
                }

                List<Map<String, Object>> values = (List<Map<String, Object>>) param.getValue();
                Map<String, Object> line = null;
                if (values.size() > columnIndex) {
                    line = values.get(columnIndex);
                }
                if (line != null) {
                    Object object = line.get(testParam.getName());
                    return object != null ? object.toString() : "";
                }
            }
            return null;
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public void dispose() {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {

        }
    }
}
