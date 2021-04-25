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

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.preferences.HotKeyUtil;
import org.talend.commons.ui.swt.proposal.StyledTextContentAdapter;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.ui.proposal.SQLEditorLabelProvider;
import org.talend.sqlbuilder.ui.proposal.SQLEditorProposalAdapter;
import org.talend.sqlbuilder.ui.proposal.SQLEditorProposalProvider;

/**
 * This class is responsible for creating editor composite.<br/>
 *
 * $Id: SQLBuilderEditorComposite.java,v 1.51 2006/11/09 08:40:43 ftang Exp $
 *
 */
/**
 * dev class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 nicolas Exp $
 *
 */
public class SQLBuilderEditorComposite extends AbstractSQLEditorComposite {

    private Query queryObject;

    private RepositoryNode repositoryNode;

    private boolean ifLimit = true;

    private Text maxResultText;

    private StyledText colorText;

    private CTabItem tabItem;

    private boolean isDefaultEditor;

    private final String language = "tsql"; //$NON-NLS-1$

    /**
     * SQLBuilderEditorComposite constructor.
     *
     * @param parent
     * @param tabItem
     * @param isDefaultEditor
     * @param node
     * @param dialog
     * @param connParam2
     * @param style
     */
    public SQLBuilderEditorComposite(Composite parent, CTabItem tabItem, boolean isDefaultEditor, ConnectionParameters connParam,
            RepositoryNode node, ISQLBuilderDialog d, boolean readOnly) {
        super(parent, SWT.NONE, d, connParam);
        this.tabItem = tabItem;
        this.isDefaultEditor = isDefaultEditor;
        this.readOnly = readOnly;
        repositoryNode = node;
        initialContent(this);
        this.setRepositoryNode(node);
    }

    /**
     * Initializes UI compoents.
     *
     * @param parent
     */
    private void initialContent(Composite parent) {

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        this.setLayout(layout);
        createToolBar();
        createEditorArea(parent);
        createStatusArea(parent);
        adaptWidgetToReadOnly();
    }

    /**
     * Creates UI for editor.
     *
     * @param parent
     */
    private void createEditorArea(Composite parent) {

        // create divider line
        Composite div1 = new Composite(parent, SWT.NONE);
        GridData lgid = new GridData();
        lgid.grabExcessHorizontalSpace = true;
        lgid.horizontalAlignment = GridData.FILL;
        lgid.heightHint = 1;
        lgid.verticalIndent = 1;
        div1.setLayoutData(lgid);
        div1.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

        // create text viewer
        GridData gid = new GridData();
        gid.grabExcessHorizontalSpace = true;
        gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = GridData.FILL;
        gid.verticalAlignment = GridData.FILL;

        colorText = new ColorStyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, CorePlugin.getDefault()
                .getPreferenceStore(), language);
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        String fontType = preferenceStore.getString(TalendDesignerPrefConstants.MEMO_TEXT_FONT);
        FontData fontData = new FontData(fontType);
        Font font = new Font(null, fontData);
        addResourceDisposeListener(colorText, font);
        colorText.setFont(font);

        GridData gd = new GridData(GridData.FILL_BOTH);
        colorText.setLayoutData(gd);

        colorText.setText(this.connParam.getQuery());

        colorText.addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {

                if (event.stateMask == SWT.CTRL && event.keyCode == 13) {
                    event.doit = false;
                    execSQLAction.run();
                }
            }
        });
        colorText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                isModified = true;
            }

        });
    }

    /**
     *
     * When dispose the control, dispose resource at the same time. (bug 6916)
     */
    protected void addResourceDisposeListener(final Control parent, final Resource res) {
        if (parent != null) {
            parent.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    if (res != null && !res.isDisposed()) {
                        res.dispose();
                    }
                    parent.removeDisposeListener(this);
                }
            });
        }

    }

    /**
     * Creates proposal for editor.
     */
    private void createEditorProposal() {
        try {
            // create KeyStroke use Ctrl+Space as default
            KeyStroke keyStroke = HotKeyUtil.getHotKey(HotKeyUtil.contentAssist);
            IControlContentAdapter controlContentAdapter = new StyledTextContentAdapter();
            IContentProposalProvider contentProposalProvider = new SQLEditorProposalProvider(repositoryNode, language);

            SQLEditorProposalAdapter contentProposalAdapter = new SQLEditorProposalAdapter(colorText, controlContentAdapter,
                    contentProposalProvider, keyStroke, new char[] { ' ', '.' });
            contentProposalAdapter.setPropagateKeys(true);
            contentProposalAdapter.setFilterStyle(ContentProposalAdapter.FILTER_CUMULATIVE);
            contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
            contentProposalAdapter.setLabelProvider(new SQLEditorLabelProvider());
            contentProposalAdapter.setAutoActivationDelay(10);
            contentProposalAdapter.setPopupSize(new Point(300, 200));
        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("SQLBuilderEditorComposite.logMessage"), e); //$NON-NLS-1$
        }
    }

    /**
     * Creates UI for status bar.
     *
     * @param parent
     */
    private void createStatusArea(Composite parent) {
        // create bottom status bar
        Composite statusBar = new Composite(parent, SWT.NULL);

        GridLayout statusBarLayout = new GridLayout();
        statusBarLayout.numColumns = 3;
        statusBarLayout.verticalSpacing = 0;
        statusBarLayout.marginHeight = 0;
        statusBarLayout.marginWidth = 0;
        statusBarLayout.marginTop = 0;
        statusBarLayout.marginBottom = 0;
        statusBarLayout.marginRight = 5;
        statusBarLayout.horizontalSpacing = 5;
        statusBarLayout.verticalSpacing = 0;

        statusBar.setLayout(statusBarLayout);

        GridData statusBarGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        statusBarGridData.verticalIndent = 0;
        statusBarGridData.horizontalIndent = 0;
        statusBar.setLayoutData(statusBarGridData);

        // add status line manager
        StatusLineManager statusMgr = new StatusLineManager();
        statusMgr.createControl(statusBar);

        GridData c1Grid = new GridData();
        c1Grid.horizontalAlignment = SWT.FILL;
        c1Grid.verticalAlignment = SWT.BOTTOM;
        c1Grid.grabExcessHorizontalSpace = true;
        c1Grid.grabExcessVerticalSpace = false;
        statusMgr.getControl().setLayoutData(c1Grid);

        // add checkbox for limiting results
        GridData c2Grid = new GridData();
        c2Grid.horizontalAlignment = SWT.RIGHT;
        c2Grid.verticalAlignment = SWT.CENTER;
        c2Grid.grabExcessHorizontalSpace = false;
        c2Grid.grabExcessVerticalSpace = false;

        final Button limitResults = new Button(statusBar, SWT.CHECK);

        limitResults.setText(Messages.getString("SQL_Limit_Rows_2")); //$NON-NLS-1$
        limitResults.setSelection(true);
        limitResults.setLayoutData(c2Grid);

        // add input field for result limit
        GridData c3Grid = new GridData();
        c3Grid.horizontalAlignment = SWT.RIGHT;
        c3Grid.verticalAlignment = SWT.CENTER;
        c3Grid.grabExcessHorizontalSpace = false;
        c3Grid.grabExcessVerticalSpace = false;
        c3Grid.widthHint = 30;

        maxResultText = new Text(statusBar, SWT.BORDER | SWT.SINGLE);
        maxResultText.setText(IConstants.MAX_SQL_ROWS);
        maxResultText.setLayoutData(c3Grid);

        limitResults.addMouseListener(new MouseAdapter() {

            // enable/disable input field when checkbox is clicked
            @Override
            public void mouseUp(MouseEvent e) {
                maxResultText.setEnabled(limitResults.getSelection());
                ifLimit = limitResults.getSelection();
            }
        });
    }

    /**
     * Adds resize listener.
     *
     * @param listener
     */
    public void addResizeListener(ControlListener listener) {

        coolBar.addControlListener(listener);
    }

    /**
     * Refresh actions availability on the toolbar.
     */
    @Override
    public void refresh(final boolean sessionChanged) {

        this.getShell().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {

                if (sessionChanged) {

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
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getSessionTreeNode()
     */
    @Override
    public RepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getIfLimit()
     */
    @Override
    public boolean getIfLimit() {
        return ifLimit;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getMaxResult()
     */
    @Override
    public String getMaxResult() {
        return maxResultText.getText();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#setRepositoryNode(org.talend.repository.model.RepositoryNode)
     */
    @Override
    public void setRepositoryNode(RepositoryNode node) {
        Assert.isNotNull(node, Messages.getString("SQLBuilderEditorComposite.assertMessage")); //$NON-NLS-1$
        this.repositoryNode = node;
        guiModificationQueryAction.setCurrentNode(node);
        setEditorTitle(this.repositoryNode, connParam, tabItem);
        sessionSwitcher.refreshSelectedRepository();
        if (!readOnly) {
            createEditorProposal();
        }
    }

    /**
     * Sets tab title.
     *
     * @param node
     */

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getRepositoryName()
     */
    @Override
    public String getRepositoryName() {
        if (repositoryNode == null) {
            return ""; //$NON-NLS-1$
        }
        String repositoryName = ((SqlBuilderRepositoryObject) repositoryNode.getObject()).getRepositoryName();
        return repositoryName;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getSQLToBeExecuted()
     */
    @Override
    public String getSQLToBeExecuted() {
        updateParameters();
        return colorText.getText();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#setEditorContent(org.talend.sqlbuilder.util.ConnectionParameters)
     */
    public void setEditorContent(ConnectionParameters connectionParameters) {
        this.connParam = connectionParameters;
        setEditorContent(connectionParameters.getQuery());
    }

    private boolean isModified = false;

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#clearText()
     */
    @Override
    public void clearText() {
        this.colorText.setText(""); //$NON-NLS-1$
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#setEditorContent(java.lang.String)
     */
    @Override
    public void setEditorContent(String string) {
        this.colorText.setText(string);
    }

    public String getEditorContent() {
        updateParameters();
        return colorText.getText();
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.editor.ISQLEditor#getSessionTreeNode()
     */
    public SessionTreeNode getSessionTreeNode() {
        return null;
    }

    public void setRepositoryNode(SessionTreeNode node) {

    }

    @Override
    public boolean getDefaultEditor() {
        return this.isDefaultEditor;
    }

    /**
     * Getter for connParam.
     *
     * @return the connParam
     */
    @Override
    public ConnectionParameters getConnParam() {
        return this.connParam;
    }

    public Query getQueryObject() {
        return this.queryObject;
    }

    public void setQueryObject(Query queryObject) {
        this.queryObject = queryObject;
    }

    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Sets the isModified.
     *
     * @param isModified the isModified to set
     */
    public void setModified(boolean isModified) {
        this.isModified = isModified;
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
    @Override
    public MultiPageSqlBuilderEditor getMultiPageEditor() {
        return multiPageEditor;
    }

    @Override
    public StyledText getColorText() {
        return this.colorText;
    }

    @Override
    protected void adaptWidgetToReadOnly() {
        super.adaptWidgetToReadOnly();
        colorText.setEditable(!isReadOnly());
    }

    /**
     * For bug TDI-7643, every composite should has different query object, using <code>queryObject</code> to store the
     * object.
     */
    @Override
    public Query doSaveSQL(Query query2, boolean as) {
        queryObject = super.doSaveSQL(query2, as);
        this.setQueryObject(queryObject);
        return queryObject;
    }
}
