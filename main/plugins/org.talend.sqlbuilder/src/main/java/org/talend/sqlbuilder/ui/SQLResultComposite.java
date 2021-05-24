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
package org.talend.sqlbuilder.ui;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.actions.CloseSQLResultTabAction;
import org.talend.sqlbuilder.actions.IResultDisplayer;
import org.talend.sqlbuilder.sqlcontrol.AbstractSQLExecution;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLResultComposite.java,v 1.18 2006/11/06 09:18:06 qiang.zhang Exp $
 *
 * @author qiang.zhang
 */
public class SQLResultComposite extends Composite implements IResultDisplayer {

    private static IResultDisplayer instance;

    private int lastTabNumber = 0;

    private CTabFolder tabFolder;

    private AbstractSQLExecution sqlExecution;

    /**
     *
     * DOC dev SQLResultComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public SQLResultComposite(Composite parent, int style) {
        super(parent, style);

        GridLayout gLay = new GridLayout();
        gLay.marginLeft = 0;
        gLay.marginRight = 0;
        gLay.marginTop = 0;
        gLay.marginBottom = 0;

        gLay.horizontalSpacing = 0;
        gLay.verticalSpacing = 0;
        gLay.marginWidth = 0;
        gLay.marginHeight = 0;
        setLayout(gLay);
        setDefaultMessage();
        instance = this;

    }

    /**
     *
     * DOC dev Comment method "addSQLExecution".
     *
     * @param sqlExe sql execution object
     * @throws Exception throw all exception
     */
    public void addSQLExecution(AbstractSQLExecution sqlExe) throws Exception {
        this.sqlExecution = sqlExe;
        createTabFolder();
        createTabItem();
    }

    /**
     * Getter for instance.
     *
     * @return the instance
     */
    public static IResultDisplayer getInstance() {
        return instance;
    }

    /**
     *
     * DOC dev Comment method "createTabFolder".
     *
     */
    private void createTabFolder() {

        if (tabFolder == null || tabFolder.isDisposed()) {

            clearParent();

            // create tab folder for different sessions
            tabFolder = new CTabFolder(this, SWT.NULL);
            tabFolder.setSimple(false);
            tabFolder.setToolTipText(Messages.getString("SQLResultComposite.SQLResults.ToolTip")); //$NON-NLS-1$
            GridData gd = new GridData(GridData.FILL_BOTH);
            tabFolder.setLayoutData(gd);

            this.layout();
            this.redraw();

        }
    }

    /**
     *
     * DOC dev Comment method "clearParent".
     */
    private void clearParent() {

        Control[] children = this.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
        }

        lastTabNumber = 0;
    }

    /**
     *
     * DOC dev Comment method "createTabItem".
     *
     * @throws Exception throw all exception
     */
    private void createTabItem() throws Exception {
        lastTabNumber = lastTabNumber + 1;
        final CTabItem tabItem = new CTabItem(tabFolder, SWT.NULL);
        String labelText = Messages.getString("SQLResultComposite.Result") + ": " + lastTabNumber;
        tabItem.setText(labelText);
        tabItem.setData("tabLabel", labelText); //$NON-NLS-1$
        tabItem.setToolTipText(TextUtil.getWrappedText(sqlExecution.getSqlStatement()));
        Composite composite = new Composite(tabFolder, SWT.NULL);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginLeft = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;

        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        tabItem.setControl(composite);
        tabItem.setData(sqlExecution);

        tabItem.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(final DisposeEvent e) {
                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

                    public void run() {
                        CTabItem tabItem = (CTabItem) e.getSource();
                        AbstractSQLExecution sqlExe = (AbstractSQLExecution) tabItem.getData();
                        sqlExe.stop();
                        tabItem.setData(null);
                        if (tabFolder != null && !tabFolder.isDisposed()) {

                            if (tabFolder.getItemCount() == 0) {
                                // this is last tab..
                                clearParent();
                                setDefaultMessage();
                            }

                        } else if (tabFolder.isDisposed()) {
                            clearParent();
                            setDefaultMessage();
                        }
                    }
                });
            }
        });

        createHeaderComposite(composite, tabItem);

        createDetailComposite(composite, tabItem);

        // refresh view
        composite.layout();
        tabFolder.layout();
        tabFolder.redraw();
    }

    /**
     * Set a default message, this method is called when no results are available for viewing.
     */
    private void setDefaultMessage() {

        clearParent();

        // add message
        String message = Messages.getString("SQLResultsView.NoResults"); //$NON-NLS-1$
        Label label = new Label(this, SWT.FILL);
        label.setText(message);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        this.layout();
        this.redraw();
    }

    /**
     *
     * DOC dev Comment method "createHeaderComposite".
     *
     * @param parent a TabItem 's Control
     * @param tabItem a TabItem
     */
    private void createHeaderComposite(Composite parent, CTabItem tabItem) {
        // add sql statement, first create temp label to calculate correct size

        // int labelStyle = SWT.WRAP | SWT.MULTI | SWT.V_SCROLL;
        //
        // Text tmpLabel = new Text(parent, labelStyle);
        // tmpLabel.setText(TextUtil.removeLineBreaks(sqlExecution.getSqlStatement()));
        // tmpLabel.setLayoutData(new FillLayout());
        // int parentWidth = this.getClientArea().width;
        // Point idealSize = tmpLabel.computeSize(parentWidth - 30, SWT.DEFAULT);
        //
        // if (idealSize.y > 60) {
        // // we need a scroll bar
        // labelStyle = SWT.WRAP | SWT.MULTI | SWT.V_SCROLL;
        // }
        //
        // tmpLabel.dispose();
        // now create real label
        // create spanned cell for table data
        Composite headerComposite = new Composite(parent, SWT.FILL);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout hLayout = new GridLayout();
        hLayout.numColumns = 2;
        hLayout.marginLeft = 0;

        hLayout.horizontalSpacing = 0;
        hLayout.verticalSpacing = 0;
        hLayout.marginWidth = 0;
        hLayout.marginHeight = 0;

        headerComposite.setLayout(hLayout);

        Text label = new Text(headerComposite, SWT.H_SCROLL);
        // label.setEnabled(false);
        label.setEditable(false);
        label.setBackground(this.getBackground());

        label.setText(TextUtil.removeLineBreaks(sqlExecution.getSqlStatement()));
        label.setToolTipText(TextUtil.getWrappedText(sqlExecution.getSqlStatement()));

        GridData labelGridData = new GridData(SWT.FILL, SWT.TOP, true, true);
        // labelGridData.heightHint = labelHeight;
        label.setLayoutData(labelGridData);
        label.setVisible(true);
        // add action bar

        ToolBarManager toolBarMgr = new ToolBarManager(SWT.FLAT);
        toolBarMgr.createControl(headerComposite);
        toolBarMgr.add(new CloseSQLResultTabAction(tabItem));
        toolBarMgr.update(true);
        GridData gid = new GridData();
        gid.horizontalAlignment = SWT.RIGHT;
        gid.verticalAlignment = SWT.TOP;
        toolBarMgr.getControl().setLayoutData(gid);

    }

    /**
     *
     * DOC dev Comment method "createDetailComposite".
     *
     * @param parent a CTabItem 's Control
     * @param tabItem a CTabItem
     */
    private void createDetailComposite(Composite parent, CTabItem tabItem) {
        // add sql execute result TableView composite to show progress bar and results
        Composite detailComposite = new Composite(parent, SWT.FILL);
        detailComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        sqlExecution.setComposite(detailComposite);
        sqlExecution.setParentTab(tabItem);
        sqlExecution.startExecution();
        // set new tab as the active one
        tabFolder.setSelection(tabFolder.getItemCount() - 1);
    }
}
