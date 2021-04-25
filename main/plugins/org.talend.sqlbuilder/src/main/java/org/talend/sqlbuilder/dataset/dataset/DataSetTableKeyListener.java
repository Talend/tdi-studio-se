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
package org.talend.sqlbuilder.dataset.dataset;

import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbdetail.IDetailTab;

/**
 * Provides keyboard features for DataSetTable.
 * <ul>
 * <li>F5: refresh table</li>
 * <li>CTRL-C: copy active cell</li>
 * <li>CTRL-F: column name finder assistant (use F3 to skip to next match) </li>
 * </ul>
 *
 * @author Davy Vanherbergen
 */
public class DataSetTableKeyListener implements KeyListener {

    private IDetailTab ptab = null;

    private Composite pparent = null;

    private Table ptable = null;

    private TableCursor pcursor = null;

    private Shell ppopup = null;

    private static final int CTRL_C = 3;

    private static final int CTRL_F = 6;

    private static final int ENTER = 13;

    private String plastNameSearched = null;

    private int plastColumnIndex = 0;


    /**
     * Create new keylistener.
     *
     * @param parent
     * @param table
     * @param cursor
     * @param tab
     */
    public DataSetTableKeyListener(Composite parent, Table table, TableCursor cursor) {

        ptable = table;
        pparent = parent;
        pcursor = cursor;

        Object o = pparent.getData("IDetailTab"); //$NON-NLS-1$
        if (o != null) {
            ptab = (IDetailTab) o;
        }

    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {

        switch (e.character) {

        case CTRL_C:
            // copy cell content to clipboard

            try {

                Clipboard clipBoard = new Clipboard(Display.getCurrent());
                TextTransfer textTransfer = TextTransfer.getInstance();

                TableItem[] items = ptable.getSelection();

                if (items == null || items.length == 0) {
                    return;
                }

                int columnIndex = pcursor.getColumn();
                clipBoard.setContents(new Object[] { items[0].getText(columnIndex) }, new Transfer[] { textTransfer });

            } catch (Exception ex) {
                SqlBuilderPlugin.log(Messages.getString("DataSetTableKeyListener.logMessage1"), ex); //$NON-NLS-1$
            }
            break;

        case CTRL_F:
            // column name typeahead
            createPopup();
            break;
        default:
            return;

        }

    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {

        switch (e.keyCode) {

        case SWT.F5:
            // refresh tab
            if (ptab != null) {
                ptab.refresh();
            }
            disposePopup();

            // refresh SQL Results
            try {
                Object o = pparent.getData("parenttab"); //$NON-NLS-1$
                if (o != null) {
                    SQLExecution sqlExec = (SQLExecution) ((TabItem) o).getData();
                    if (sqlExec != null) {
                        sqlExec.startExecution();
                    }
                }
            } catch (Exception e1) {
                SqlBuilderPlugin.log(Messages.getString("DataSetTableKeyListener.logMessage2"), e1); //$NON-NLS-1$
            }

            break;

        case SWT.ESC:
            disposePopup();
            break;
        default:
            return;

        }

    }


    /**
     * Display column finder popup.
     */

    private void createPopup() {

        plastNameSearched = null;

        // recycle old popup
        if (ppopup != null && !ppopup.isDisposed()) {
            if (!ppopup.isVisible()) {
                ppopup.open();
            }
            return;
        }

        // find out where to put the popup on screen
        Point popupLocation = ptable.toDisplay(10, 40);

        // create new shell
        ppopup = new Shell(pparent.getShell(), SWT.BORDER | SWT.ON_TOP);
        ppopup.setBackground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        ppopup.setForeground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        ppopup.setSize(250, 50);
        ppopup.setLocation(popupLocation);
        ppopup.setLayout(new GridLayout());

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        // add 'find:' label
        Label label = new Label(ppopup, SWT.NULL);
        label.setText(Messages.getString("DataSetTable.PopUp.Find")); //$NON-NLS-1$
        label.setBackground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        // add input field for search text
        final Text input = new Text(ppopup, SWT.SINGLE | SWT.FILL);
        input.setLayoutData(gridData);
        input.setBackground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));


        // scroll columns whenever something is typed in input field.
        input.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {

                Text t = (Text) e.widget;
                String text = t.getText();

                // locate column and show if found
                if (jumpToColumn(text)) {
                    input.setForeground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                } else {
                    // give some subtle feedback to user that column doesn't exist..
                    input.setForeground(pparent.getDisplay().getSystemColor(SWT.COLOR_RED));
                }
            }

        });


        // add listener so that we can jump to next column match when
        // user hits enter..
        input.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {

                if (e.character == ENTER) {
                    // scroll to next match
                    if (jumpToColumn(null)) {
                        input.setForeground(pparent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                    } else {
                        // give some subtle feedback to user that column doesn't exist..
                        input.setForeground(pparent.getDisplay().getSystemColor(SWT.COLOR_RED));
                    }
                }
            }
        });

        // close popup when user is no longer in inputfield
        input.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                disposePopup();
            }

        });


        // activate popup
        ppopup.open();
        ppopup.forceActive();

    }


    /**
     * Close column finder popup.
     */
    private void disposePopup() {

        if (ppopup != null && !ppopup.isDisposed()) {
            ppopup.close();
            ppopup.dispose();
            ppopup = null;
        }
    }


    /**
     * Jump to next availabel column with header name.
     * If the same name is processed again, we jump to
     * the next column with the same name.  If no further columns
     * are available, we jump to first available column again.
     *
     * @param name of column to jump to.
     * @return true if a matching column was found
     */
    private boolean jumpToColumn(String name) {

        String text = null;

        if (name != null) {
            // use input to find column
            text = name.toLowerCase().trim();
            plastNameSearched = text;
            plastColumnIndex = 0;

        } else {
            // use previous name to search
            text = plastNameSearched;
            plastColumnIndex += 1;

        }

        if (text == null) {
            text = ""; //$NON-NLS-1$
        }


        TableColumn[] columns = ptable.getColumns();
        if (columns == null || plastColumnIndex >= columns.length) {

            // no columns or we searched them all..
            plastColumnIndex = 0;
            return false;
        }

        boolean columnFound = false;

        // find column
        for (int i = plastColumnIndex; i < columns.length; i++) {

            TableColumn column = columns[i];

            if (column.getText().toLowerCase().startsWith(text)) {

                columnFound = true;

                // first scroll all the way to right
                ptable.showColumn(columns[columns.length - 1]);

                // now back to the column we want, this way it should be
                // the first column visible in most cases
                ptable.showColumn(column);

                // move cursor to found column
                if (ptable.getItemCount() > 0) {
                    pcursor.setSelection(0, i);
                    pcursor.setVisible(true);
                }

                // store column index so we can pickup where we left of
                // in case of repeated search
                plastColumnIndex = i;

                break;

            }
        }

        // reset search to start from start again
        if (!columnFound) {
            plastColumnIndex = 0;
        }


        return columnFound;
    }
}
