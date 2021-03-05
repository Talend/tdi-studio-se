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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.sqlbuilder.Messages;

/**
 * This class is used for creating a dialog for saving current editor's sql texts. <br/>
 *
 * $Id: SaveSQLDialog.java,v 1.0 2006/11/15 05:38:28 ftang Exp $
 *
 * @author ftang
 */
public class SQLPropertyDialog extends TitleAreaDialog {

    private static String dialogTitle = ""; //$NON-NLS-1$

    private Text commentText;

    private Text nameText;

    private List<String> names;

    private Query query;

    private String sql;

    private boolean ifcontext; // add by hyWang

    private boolean showQueryProperty;

    public void setIfcontext(boolean ifcontext) {
        this.ifcontext = ifcontext;
    }

    /**
     * Sets the sql.
     *
     * @param sql the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * Getter for query.
     *
     * @return the query
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Sets the query.
     *
     * @param query the query to set
     */
    public void setQuery(Query query) {
        this.query = query;
        if (query != null && !showQueryProperty) {
            names.remove(query.getLabel());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        final Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(Messages.getString("SQLEditor.SQLPropertyDialog.Name")); //$NON-NLS-1$

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label commentLabel = new Label(container, SWT.NONE);
        commentLabel.setText(Messages.getString("SQLEditor.SQLPropertyDialog.Comment")); //$NON-NLS-1$

        commentText = new Text(container, SWT.BORDER);
        commentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        if (showQueryProperty && query != null) {
            this.setMessage(
                    Messages.getString("SQLEditor.SQLPropertyDialog.ShowQueryProperty.message"), IMessageProvider.INFORMATION);//$NON-NLS-1$
            nameText.setText(query.getLabel());
            nameText.setEditable(false);
            commentText.setText(query.getComment());
            // commentText.setEditable(false);
        } else {
            this.setTitle(Messages.getString("SQLEditor.Actions.SaveSQL")); //$NON-NLS-1$
            this.setMessage(Messages.getString("SQLEditor.Actions.InputMessage"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        }
        this.addListener();
        return area;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (query == null) {
            query = ConnectionFactory.eINSTANCE.createQuery();
            query.setValue(""); //$NON-NLS-1$
        }
        query.setComment(this.commentText.getText());
        query.setLabel(this.nameText.getText());
        query.setContextMode(ifcontext); // add by hyWang
        if (sql != null) {
            query.setValue(sql);
        }
        super.okPressed();
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.getString("SQLEditor.Actions.SaveMessage"), true); //$NON-NLS-1$
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     *
     * For fixing bug TUP-19612, hide this method to use the default size rather than to give the dialog an initial size.
     */
//    @Override
//    protected Point getInitialSize() {
//        return new Point(500, 242);
//    }

    /**
     * SaveSQLDialog constructor.
     *
     * @param shell
     * @param string
     */
    public SQLPropertyDialog(Shell shell, List<String> existentNames) {
        super(shell);
        this.names = existentNames;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX);
    }

    /**
     * ShowQueryProperty constructor.
     *
     * @param shell
     * @param string
     */
    public SQLPropertyDialog(Shell shell, boolean showQueryProperty) {
        super(shell);
        this.showQueryProperty = showQueryProperty;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        // Set the title bar text
        if (showQueryProperty) {
            shell.setText(Messages.getString("SQLEditor.SQLPropertyDialog.ShowQueryProperty.title")); //$NON-NLS-1$
        } else {
            shell.setText(dialogTitle);
        }
    }

    /**
     * Adds modify listener.
     */
    /**
     * DOC dev Comment method "addListener".
     */
    /**
     * DOC dev Comment method "addListener".
     */
    private void addListener() {
        nameText.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
            public void modifyText(ModifyEvent e) {
                String result = validate(nameText.getText());
                if (result == null) {
                    SQLPropertyDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(true);
                    SQLPropertyDialog.this.setMessage(Messages.getString("SQLEditor.Actions.InputMessage"), //$NON-NLS-1$
                            IMessageProvider.INFORMATION);
                } else {
                    SQLPropertyDialog.this.setMessage(result, IMessageProvider.ERROR);
                    SQLPropertyDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }

            /**
             * Validates input text.
             *
             * @param text
             * @return a string
             */
            private String validate(String text) {
                if (nameText.getText().length() == 0) {
                    return Messages.getString("SQLEditor.Actions.EmptyNameMessage"); //$NON-NLS-1$
                } else if (names.contains(text)) {
                    // Checks if name is existing.
                    return Messages.getString("SQLEditor.Actions.DuplicateNameMessage"); //$NON-NLS-1$
                }
                return null;
            }
        });

        commentText.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
            public void modifyText(ModifyEvent e) {
                if (commentText.getText().length() == 0) {
                    SQLPropertyDialog.this.setMessage(Messages.getString("SQLEditor.Actions.EmptyCommentMessage"), //$NON-NLS-1$
                            IMessageProvider.INFORMATION);
                } else if (showQueryProperty) {
                    SQLPropertyDialog.this.setMessage(
                            Messages.getString("SQLEditor.SQLPropertyDialog.ShowQueryProperty.message"), //$NON-NLS-1$
                            IMessageProvider.INFORMATION);
                    SQLPropertyDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
    }

    public static void setDialogTitle(String dialogTitle) {
        if (dialogTitle.substring(0, 1).equals("*")) { //$NON-NLS-1$
            dialogTitle = dialogTitle.substring(1);
        }
        SQLPropertyDialog.dialogTitle = "\"" + dialogTitle + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
        SQLPropertyDialog.dialogTitle += Messages.getString("SQLEditor.SaveSQLDialog.Title"); //$NON-NLS-1$
    }
}
