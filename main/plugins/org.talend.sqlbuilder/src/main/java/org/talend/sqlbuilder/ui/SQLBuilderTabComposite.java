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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.QueryRepositoryObject;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.editors.SQLBuilderEditorInput;
import org.talend.sqlbuilder.editors.SQLBuilderEditorSite;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;

/**
 * This class is responsible for creating tab composite. <br/>
 *
 * $Id: SQLBuilderTabComposite.java,v 1.23 2006/11/09 08:40:43 tangfn Exp $
 *
 */
public class SQLBuilderTabComposite extends Composite {

    private CTabFolder tabFolder;

    private ISQLBuilderDialog dialog;

    private boolean readOnly = false;

    public SQLBuilderTabComposite(Composite parent, int style, ISQLBuilderDialog d) {
        super(parent, style);
        this.dialog = d;
        this.setLayout(new FillLayout());

    }

    /**
     * Opens an new sql editor.
     *
     * @param node RepositoryNode with the DatabaseConnection
     * @param repositoryNames all the repositories' name
     * @param connParam ConnectionParameters
     * @param isDefaultEditor whether is the DefaultEditor
     */
    public void openNewEditor(RepositoryNode node, List<String> repositories, ConnectionParameters connParam,
            boolean isDefaultEditor) {
        Assert.isNotNull(node, Messages.getString("SQLBuilderTabComposite.assertMessage")); //$NON-NLS-1$
        createTabFolder();
        try {
            createTabItem(node, connParam, isDefaultEditor);
        } catch (Exception e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }
    }

    public void openNewEditor(RepositoryNode node, List<String> repositories, ConnectionParameters connParam,
            boolean isDefaultEditor, List<RepositoryNode> nodesSel) {

    }

    /**
     *
     * Creates tab folder.
     *
     */
    private void createTabFolder() {

        if (tabFolder == null || tabFolder.isDisposed()) {

            clearParent();

            // create tab folder for different sessions
            tabFolder = new CTabFolder(this, SWT.NULL | SWT.BORDER);
            tabFolder.setSimple(false);
            tabFolder.setToolTipText(Messages.getString("SQLBuilderTabComposite.toolTipText")); //$NON-NLS-1$
            this.layout();
            this.redraw();
        }
    }

    /**
     * Dispose all parent Controls.
     *
     */
    private void clearParent() {

        Control[] children = this.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
        }
    }

    /**
     * Creates tab item. Changed by Marvin Wang on Feb. 24, 2012 for bug TDI-7643, for all SQLBuilderEditorComposite
     * when using <code>editorComposite.getConnParam().getQueryObject()</code> to get the query object, the query every
     * time get is the same object. Caz all <code>SQLBuilderEditorComposite</code>s use the same connection parameter.
     *
     * @param node
     * @param connParam
     * @param isDefaultEditor
     */
    private void createTabItem(RepositoryNode node, ConnectionParameters connParam, boolean isDefaultEditor) {
        String queryStr = ""; //$NON-NLS-1$
        if (node != null) {
            CTabItem[] tabItems = tabFolder.getItems();
            for (int i = 0; i < tabItems.length; i++) {
                SQLBuilderEditorComposite editorComposite = (SQLBuilderEditorComposite) (((CTabFolder) tabItems[i].getControl())
                        .getItems()[0]).getControl();
                // To get the different query object for each SQLBuilderEditorComposite, use the following method. The
                // queryObject is stored in <code>SQLBuilderEditorComposite.doSaveSQL()</code>
                Query query2 = editorComposite.getQueryObject();
                if ((RepositoryNodeType) node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.QUERY) {
                    Query query = ((QueryRepositoryObject) node.getObject()).getQuery();
                    if (query2 == null && tabItems[i].getData() instanceof Query) {
                        query2 = (Query) tabItems[i].getData();
                    }
                    if (query2 != null && query.getLabel().equals(query2.getLabel())) {
                        if ("".equals(editorComposite.getEditorContent())) { //$NON-NLS-1$
                            editorComposite.setEditorContent(query.getValue());
                        }
                        tabFolder.setSelection(i);
                        return;
                    }
                    connParam.setQueryObject(query);
                    queryStr = query.getValue();
                }
            }
        }

        CTabItem tabItem = null;
        if (connParam.isFromDBNode()) {
            tabItem = new CTabItem(tabFolder, SWT.NULL);
        } else {
            tabItem = new CTabItem(tabFolder, SWT.CLOSE);
        }
        node = SQLBuilderRepositoryNodeManager.getRoot(node);

        // SQLBuilderEditorComposite builderEditorComposite = new SQLBuilderEditorComposite(tabFolder, tabItem,
        // isDefaultEditor,
        // connParam, node, dialog);
        // builderEditorComposite.setEditorContent(connParam);
        // builderEditorComposite.setRepositoryNode(node);
        // builderEditorComposite.setQueryObject(dialog.getConnParameters().getQueryObject());
        // builderEditorComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        if (!"".equals(queryStr)) { //$NON-NLS-1$
            try {
                nodesSel = EMFRepositoryNodeManager.getInstance().parseSqlStatement(queryStr, node);
            } catch (Exception e) {
                MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                        Messages.getString("SQLBuilderTabComposite.Notice.title"), Messages //$NON-NLS-1$
                        .getString("SQLBuilderTabComposite.Notice.Info")); //$NON-NLS-1$
            }
        }
        MultiPageSqlBuilderEditor builderEditor = new MultiPageSqlBuilderEditor(nodesSel, tabItem, isDefaultEditor, connParam,
                node, dialog);
        builderEditor.setReadOnly(readOnly);
        try {
            builderEditor.init(new SQLBuilderEditorSite(), new SQLBuilderEditorInput());
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        }
        builderEditor.createPartControl2(tabFolder);
        tabItem.setControl(builderEditor.getContainer());
        tabItem.setData(TextUtil.KEY, builderEditor);
        if (connParam.isFromRepository() && connParam.getQueryObject() != null) {
            queryStr = connParam.getQueryObject().getValue();
        }
        builderEditor.setSqlText(queryStr);
        if (connParam.isShowDesignerPage()) {
            builderEditor.showDesignerPage();
        }
        // set new tab as the active one.
        tabFolder.setSelection(tabFolder.getItemCount() - 1);

        // refresh view
        tabFolder.layout();
        tabFolder.redraw();
    }

    private List<IRepositoryNode> nodesSel = new ArrayList<IRepositoryNode>();

    /**
     *
     * Gets default tab page's text.
     *
     * @return a string representing sql text.
     */
    public String getDefaultTabSql() {
        SQLBuilderEditorComposite editorComposite = (SQLBuilderEditorComposite) (((CTabFolder) tabFolder.getItems()[0]
                .getControl()).getItems()[0]).getControl();

        return editorComposite.getSQLToBeExecuted();
    }

    /**
     * method "getCurrentTabSql" : Get Cuurent Tab page (Used tab) 's Text.
     *
     * @return a string representing sql text.
     */
    public String getCurrentTabSql() {
        if (tabFolder.getSelection() != null) {
            Control control = (((CTabFolder) tabFolder.getSelection().getControl())).getSelection().getControl();
            if (control instanceof SQLBuilderDesignerComposite) {
                return ((SQLBuilderDesignerComposite) control).getSQLToBeExecuted();
            } else if (control instanceof SQLBuilderEditorComposite) {
                SQLBuilderEditorComposite editorComposite = (SQLBuilderEditorComposite) control;
                return editorComposite.getSQLToBeExecuted();
            }
        }
        return ""; //$NON-NLS-1$
    }

    public boolean isIfcontext() {
        try {
            if (tabFolder.getSelection() != null) {
                Control control = (((CTabFolder) tabFolder.getSelection().getControl())).getSelection().getControl();
                if (control instanceof AbstractSQLEditorComposite) {
                    return ((AbstractSQLEditorComposite) control).isIfcontext();
                }
            }
            return false;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    public CTabFolder getTabFolder() {
        return this.tabFolder;
    }

    public List<IRepositoryNode> getNodesSel() {
        return this.nodesSel;
    }

    public void setNodesSel(List<IRepositoryNode> nodesSel) {
        this.nodesSel = nodesSel;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
