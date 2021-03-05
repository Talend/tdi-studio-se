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

import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramComposite;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-3-7 上午09:35:17 (星期五, 29 九月 2006) qzhang $
 *
 */
public class SQLBuilderDesignerComposite extends AbstractSQLEditorComposite {

    private CTabItem tabItem;

    private boolean isDefaultEditor;

    private RepositoryNode repositoryNode;

    private ErDiagramComposite erDiagramComposite;

    private boolean ifLimit = true;

    /**
     * qzhang SQLBuilderDesignerComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public SQLBuilderDesignerComposite(Composite parent, CTabItem tabItem, boolean isDefaultEditor,
            ConnectionParameters connParam, RepositoryNode node, ISQLBuilderDialog d, List<IRepositoryNode> nodes,
            boolean readOnly) {
        super(parent, SWT.NONE, d, connParam);
        this.tabItem = tabItem;
        this.isDefaultEditor = isDefaultEditor;
        this.readOnly = readOnly;
        isOpen = isDefaultEditor;
        repositoryNode = node;
        initialContent(this, nodes);
        setRepositoryNode(node);
    }

    @Override
    protected void createGUIModificationQueryAction() {
        super.createGUIModificationQueryAction();
        guiModificationQueryAction.setDesigner(true);
    }

    /**
     * qzhang Comment method "initialContent".
     *
     * @param composite
     */
    private void initialContent(SQLBuilderDesignerComposite composite, List<IRepositoryNode> nodes) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        setLayout(layout);
        createToolBar();
        createDesignerArea(composite, nodes);
        adaptWidgetToReadOnly();
    }

    /**
     * qzhang Comment method "createDesignerArea".
     *
     * @param composite
     */
    private void createDesignerArea(SQLBuilderDesignerComposite composite, List<IRepositoryNode> nodes) {
        // create divider line
        Composite div1 = new Composite(composite, SWT.NONE);
        GridData lgid = new GridData();
        lgid.grabExcessHorizontalSpace = true;
        lgid.horizontalAlignment = GridData.FILL;
        lgid.heightHint = 1;
        lgid.verticalIndent = 1;
        div1.setLayoutData(lgid);
        div1.setBackground(composite.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

        erDiagramComposite = new ErDiagramComposite(composite, SWT.VERTICAL);

        erDiagramComposite.setDialog(dialog);
        erDiagramComposite.setRootNode(repositoryNode);
        erDiagramComposite.setNodes(nodes, connParam.isShowDesignerPage());
        erDiagramComposite.setWeights(new int[] { 12, 3 });
    }

    private boolean isOpen = true;

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#clearText()
     */
    public void clearText() {
        erDiagramComposite.clearAll();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getDefaultEditor()
     */
    public boolean getDefaultEditor() {
        return this.isDefaultEditor;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getIfLimit()
     */
    public boolean getIfLimit() {
        return ifLimit;
    }

    /**
     * Sets the ifLimit.
     *
     * @param ifLimit the ifLimit to set
     */
    public void setIfLimit(boolean ifLimit) {
        this.ifLimit = ifLimit;
    }

    private String maxResult;

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getMaxResult()
     */
    public String getMaxResult() {
        return maxResult;
    }

    public void setSqlText(String string) {
        erDiagramComposite.setSqlText(string);
    }

    /**
     * Sets the maxResult.
     *
     * @param maxResult the maxResult to set
     */
    public void setMaxResult(String maxResult) {
        this.maxResult = maxResult;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getRepositoryNode()
     */
    public RepositoryNode getRepositoryNode() {
        return repositoryNode;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getSQLToBeExecuted()
     */
    public String getSQLToBeExecuted() {
        return erDiagramComposite.getSqlText();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#refresh(boolean)
     */
    public void refresh(final boolean b) {
        this.getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {

                if (b) {

                    // reset actions
                    addDefaultActions();
                    defaultToolBarMgr.update(true);
                }

                // update session toolbar
                sessionToolBarMgr.update(true);

                coolBarMgr.update(true);
                coolBar.update();
            }
        });
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#setEditorContent(java.lang.String)
     */
    public void setEditorContent(String string) {
        try {
            if (isOpen) {
                isOpen = false;
            } else {
                EMFRepositoryNodeManager.getInstance().updateErDiagram(true, erDiagramComposite, string, repositoryNode);
            }
        } catch (Exception e) {
            SqlBuilderPlugin.log(string, e);
        }
    }

    public void setEditorContent(ConnectionParameters connectionParameters) {
        this.connParam = connectionParameters;
        setEditorContent(connectionParameters.getQuery());

    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#setRepositoryNode(org.talend.repository.model.RepositoryNode)
     */
    public void setRepositoryNode(RepositoryNode node) {
        Assert.isNotNull(node, Messages.getString("SQLBuilderEditorComposite.assertMessage")); //$NON-NLS-1$
        this.repositoryNode = node;
        guiModificationQueryAction.setCurrentNode(node);
        setEditorTitle(this.repositoryNode, connParam, tabItem);
        sessionSwitcher.refreshSelectedRepository();
        EMFRepositoryNodeManager.getInstance().setRoot(node);
    }

    public ErDiagramComposite getErDiagramComposite() {
        return this.erDiagramComposite;
    }

    private MultiPageSqlBuilderEditor multiPageEditor;

    /**
     * Sets the multiPageEditor.
     *
     * @param multiPageEditor the multiPageEditor to set
     */
    public void setMultiPageEditor(MultiPageSqlBuilderEditor multiPageEditor) {
        this.multiPageEditor = multiPageEditor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getMultiPageEditor()
     */
    public MultiPageSqlBuilderEditor getMultiPageEditor() {
        return multiPageEditor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.ui.AbstractSQLEditorComposite#getColorText()
     */
    @Override
    public StyledText getColorText() {
        return getErDiagramComposite().getStyledText();
    }

    @Override
    protected void adaptWidgetToReadOnly() {
        super.adaptWidgetToReadOnly();
        erDiagramComposite.setEnabled(!isReadOnly());
    }
}
