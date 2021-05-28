// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.visualmap.table;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.tableeditor.TableEditorContent;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: CheckboxTableEditorContent.java 7183 2007-11-23 11:03:36Z amaumont $
 *
 */
public class CheckboxTableEditorContentElt extends TableEditorContent {

    public static final boolean CHECKED = true;

    public static final boolean UNCHECKED = false;

    private String toolTipText;

    private static Color bgColorCheck;

    private Point checkSize;

    private Button check;


    /**
     * DOC amaumont CheckboxTableEditorContent constructor comment.
     */
    public CheckboxTableEditorContentElt() {
        super();
    }

    /*
     *
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.tableviewer.TableEditorContent#initialize(org.eclipse.swt.widgets.Table,
     * org.eclipse.swt.custom.TableEditor, org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn,
     * java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Control initialize(final Table table, final TableEditor tableEditor, final TableViewerCreatorColumn currentColumn,
            final Object currentRowObject, final Object currentCellValue) {

        /*
         * Do not set check control as field because one instance of CheckboxTableEditorContent is shared by all checks
         * of a same column
         */

        final TableViewerCreator<Object> tableViewerCreator = currentColumn.getTableViewerCreator();
        check = new Button(table, SWT.CHECK);
        // System.out.println("Intitlalize:" + CheckboxTableEditorContent.this + " with check " + check.hashCode());
        boolean enabled = currentColumn.isModifiable()
                && (tableViewerCreator.getCellModifier() == null || tableViewerCreator.getCellModifier().canModify(
                        currentRowObject, currentColumn.getId()));

        if (!enabled) { // performance purpose
            check.setEnabled(enabled);
        }
        if ("ID_EXPLICIT_JOIN".equals(currentColumn.getId())) {
            if (currentRowObject instanceof InputColumnTableEntry) {
                InputColumnTableEntry column = (InputColumnTableEntry) currentRowObject;
                check.setData("ID_EXPLICIT_JOIN", "ID_EXPLICIT_JOIN");
                check.setData("columnName", column.getMetadataColumn().getOriginalDbColumnName());
            }
        }
        if (toolTipText != null && toolTipText.length() > 0) { // performance purpose
            check.setToolTipText(toolTipText);
        }
        // check.setBackground(table.getBackground()); // performance purpose
        if ("true".equals(String.valueOf(currentCellValue))) { // performance purpose
            check.setSelection(true);
        }
        if (bgColorCheck == null) {
            bgColorCheck = new Color(check.getDisplay(), 28, 81, 128);
        }

        final SelectionListener selectionListener = new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent e) {
                // System.out.println("Checking " + check.hashCode());
                tableViewerCreator.getCellModifier().modify(tableEditor.getItem(), currentColumn.getId(),
                        ((Button) e.getSource()).getSelection() ? CHECKED : UNCHECKED);
                tableViewerCreator.getTableViewer().setSelection(new StructuredSelection(currentRowObject));
            }

        };
        check.addSelectionListener(selectionListener);

        final IModifiedBeanListener<Object> modifiedBeanListener = new IModifiedBeanListener<Object>() {

            public void handleEvent(ModifiedBeanEvent<Object> event) {
                if (check.isDisposed()) {
                    tableViewerCreator.removeModifiedBeanListener(this);
                    return;
                }
                Object beanValue = tableViewerCreator.getBeanValue(currentColumn, currentRowObject);
                if (beanValue instanceof Boolean) {
                    check.setSelection(((Boolean) beanValue).booleanValue());
                }
            }

        };
        tableViewerCreator.addModifiedBeanListener(modifiedBeanListener);

        final FocusListener focusListener = new FocusListener() {

            public synchronized void focusGained(final FocusEvent e) {
                check.setBackground(bgColorCheck);
            }

            public void focusLost(FocusEvent e) {
                check.setBackground(table.getBackground());
            }

        };
        check.addFocusListener(focusListener);

        // fix bug 11810, by xtan.
        final MouseListener mouseListener = new MouseAdapter() {

            public void mouseDown(MouseEvent e) {
                // System.out.println("checkbox - mouseDown Event");
                // convert axis from button(x,y) to table(x1,y1)
                Point map = e.display.map((Control) e.widget, table, new Point(e.x, e.y));

                // build a common Event from MouseEvent, @see:MouseEvent(Event e)
                Event event = new Event();
                // super(e);
                event.x = map.x;
                event.y = map.y;
                event.button = e.button;
                event.stateMask = e.stateMask;
                // event.count = e.count; only for paint event and not supported in RAP
                // super(e.widget);
                event.display = e.display;
                event.widget = table;
                event.time = e.time;
                event.data = e.data;
                // event.widget = e.source;

                // the purpose is to send a MouseDown event to Table...like TextCellEditor/ColorCellEditor in SWT
                table.notifyListeners(SWT.MouseDown, event);
            }
        };
        check.addMouseListener(mouseListener);

        check.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                // System.out.println("Disposing:" + CheckboxTableEditorContent.this + " with check " +
                // check.hashCode());
                check.removeFocusListener(focusListener);
                tableViewerCreator.removeModifiedBeanListener(modifiedBeanListener);
                check.removeSelectionListener(selectionListener);
                check.removeMouseListener(mouseListener);
            }

        });

        // Set attributes of the editor
        tableEditor.horizontalAlignment = SWT.CENTER;

        if (checkSize == null) { // performance purpose
            checkSize = check.computeSize(SWT.DEFAULT, table.getItemHeight());
        }

        tableEditor.minimumHeight = checkSize.y;
        tableEditor.minimumWidth = checkSize.x;
        return check;
    }

    /**
     * Sets the toolTipText.
     *
     * @param toolTipText the toolTipText to set
     */
    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }

    public void setCheckVisible(boolean visible) {
        if (check != null) {
            check.setVisible(visible);
        }
    }

    public Button getCheck() {
        return check;
    }

}
