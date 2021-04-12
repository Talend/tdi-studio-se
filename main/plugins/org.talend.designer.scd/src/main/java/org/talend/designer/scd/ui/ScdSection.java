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
package org.talend.designer.scd.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.util.SWTResourceManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class ScdSection {

    protected Label title;

    private Composite composite;

    protected ScdManager scdManager;

    protected ToolBar toolBar;

    protected boolean toolbarNeeded;

    protected Composite headerComposite;

    protected ToolItem addEntryItem;

    protected ToolItem removeEntryItem;

    protected ToolItem moveUpEntryItem;

    protected ToolItem moveDownEntryItem;

    protected Font titleFont;

    public ScdSection(Composite parent, ScdManager scdManager, boolean toolbarNeeded) {
        this.toolbarNeeded = toolbarNeeded;
        this.scdManager = scdManager;
        // if (WindowSystem.isWIN32()) {
        // // composite = new Composite(parent, SWT.NONE);
        // composite = new Decorations(parent, SWT.ON_TOP);
        // // composite.setBackgroundMode(SWT.INHERIT_NONE);
        // // composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        // } else {
        // composite = new Composite(parent, SWT.NONE);
        // }
        composite = new Composite(parent, SWT.BORDER);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
        GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(composite);
        // SWTResourceManager.getFont("Times New Roman", 12, SWT.BOLD)
        titleFont = SWTResourceManager.getSystemFont(SWT.BOLD);
        init(composite);
    }

    /**
     * DOC hcw Comment method "init".
     *
     * @param width
     * @param composite
     */
    private void init(Composite composite) {

        if (toolbarNeeded) {
            headerComposite = new Composite(composite, SWT.NONE);
            GridData headerGridData = new GridData(GridData.FILL_HORIZONTAL);
            headerComposite.setLayoutData(headerGridData);
            GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).numColumns(2).applyTo(headerComposite);

            title = new Label(headerComposite, SWT.NONE);
            title.setAlignment(SWT.CENTER);
            title.setFont(titleFont);
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(title);

            createToolbar(headerComposite);
        } else {
            title = new Label(composite, SWT.NONE);
            title.setAlignment(SWT.CENTER);
            title.setFont(titleFont);
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(title);
        }
        createContents(composite);
    }

    /**
     * DOC chuang Comment method "createToolbar".
     *
     * @param headerComposite
     */
    protected void createToolbar(Composite headerComposite) {
        toolBar = new ToolBar(headerComposite, SWT.FLAT | SWT.RIGHT | SWT.NONE);
        addEntryItem = new ToolItem(toolBar, SWT.PUSH);
        addEntryItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.ADD_ICON)));
        toolBar.setBackground(SWTResourceManager.getColor(IColorConstants.YELLOW));

        removeEntryItem = new ToolItem(toolBar, SWT.PUSH);
        removeEntryItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.MINUS_ICON)));
        // removeEntryItem.setBackground(SWTResourceManager.getColor(IColorConstants.YELLOW));

        moveUpEntryItem = new ToolItem(toolBar, SWT.PUSH);
        moveUpEntryItem.setImage(ImageProvider.getImage(EImage.UP_ICON));

        moveDownEntryItem = new ToolItem(toolBar, SWT.PUSH);
        moveDownEntryItem.setImage(ImageProvider.getImage(EImage.DOWN_ICON));

        addToolbarListener();
    }

    /**
     * DOC chuang Comment method "addToolbarListener".
     */
    protected void addToolbarListener() {
    }

    public Composite getControl() {
        return composite;
    }

    /**
     * DOC hcw Comment method "createContents".
     *
     * @param composite
     */
    protected void createContents(Composite composite) {
        // TODO Auto-generated method stub

    }

    public void setTitle(String text, Color background) {
        title.setText(text);
        title.setBackground(background);
        // title.getParent().setBackground(background);
    }

    public void setScdManager(ScdManager scdManager) {
        this.scdManager = scdManager;
    }

    public ScdManager getScdManager() {
        return scdManager;
    }

    /**
     * DOC hcw Comment method "adjustIndicesDown".
     *
     * @param selectionIndices
     * @param tableModel2
     * @return
     */
    protected <T> int[] adjustIndicesDown(int[] selectionIndices, List<T> model, List<Integer> updateIndices) {
        if (model.size() < 2) {
            return new int[0];
        }

        Map<Integer, Integer> selectionMap = new HashMap<Integer, Integer>(); // old selected item index, new selected
        // item index
        for (int index : selectionIndices) {
            selectionMap.put(index, index);
        }

        Map<Integer, Integer> map = new HashMap<Integer, Integer>(); // new index , old index
        for (int i = selectionIndices.length - 1; i >= 0;) {
            int j = i - 1;
            // find continous block
            while (j >= 0 && selectionIndices[j] + 1 == selectionIndices[j + 1]) {
                j--;
            }

            if (selectionIndices[i] + 1 < model.size()) {
                // from j + 1 to i, all index add 1
                for (int k = j + 1; k <= i; k++) {
                    map.put(selectionIndices[k] + 1, selectionIndices[k]);
                    selectionMap.put(selectionIndices[k], selectionIndices[k] + 1);
                }

                map.put(selectionIndices[j + 1], selectionIndices[i] + 1);
            }
            i = j;
        }
        List<T> temp = new ArrayList<T>(model);
        for (Integer newIndex : map.keySet()) {
            Integer oldIndex = map.get(newIndex);
            model.set(newIndex, temp.get(oldIndex));
            updateIndices.add(newIndex);
        }
        return convertToArray(new ArrayList<Integer>(selectionMap.values()));
    }

    /**
     * DOC hcw Comment method "adjustIndicesUp".
     *
     * @param selectionIndices
     * @param tableModel2
     * @return
     */
    protected <T> int[] adjustIndicesUp(int[] selectionIndices, List<T> model, List<Integer> updateIndices) {
        if (model.size() < 2) {
            return new int[0];
        }

        Map<Integer, Integer> selectionMap = new HashMap<Integer, Integer>(); // old selected item index, new selected
        // item index
        for (int i : selectionIndices) {
            selectionMap.put(i, i);
        }

        Map<Integer, Integer> map = new HashMap<Integer, Integer>(); // new index , old index
        for (int i = selectionIndices.length - 1; i >= 0;) {
            int j = i - 1;
            // find continous block
            while (j >= 0 && selectionIndices[j] + 1 == selectionIndices[j + 1]) {
                j--;
            }

            if (selectionIndices[j + 1] - 1 >= 0) {
                // from j + 1 to i, all index minus 1
                for (int k = j + 1; k <= i; k++) {
                    map.put(selectionIndices[k] - 1, selectionIndices[k]);
                    selectionMap.put(selectionIndices[k], selectionIndices[k] - 1);
                }

                map.put(selectionIndices[i], selectionIndices[j + 1] - 1);
            }
            i = j;
        }
        List<T> temp = new ArrayList<T>(model);
        for (Integer newIndex : map.keySet()) {
            Integer oldIndex = map.get(newIndex);
            model.set(newIndex, temp.get(oldIndex));
            updateIndices.add(newIndex);
        }
        return convertToArray(new ArrayList<Integer>(selectionMap.values()));
    }

    /**
     * DOC hcw Comment method "convertToArray".
     *
     * @param collection
     * @return
     */
    protected int[] convertToArray(List<Integer> collection) {
        // convert to int array
        int[] array = new int[collection.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = collection.get(i);
        }
        return array;
    }

    public List<String> getUsedFields() {
        return null;
    }

    public void onUnusedFieldsChange(List<String> fields) {
    }

}
