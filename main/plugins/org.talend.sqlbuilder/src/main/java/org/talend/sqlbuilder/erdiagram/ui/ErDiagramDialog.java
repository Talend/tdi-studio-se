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
package org.talend.sqlbuilder.erdiagram.ui;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramDialog extends Dialog {

    private List<IRepositoryNode> nodes;

    /**
     * The progress indicator control.
     */
    protected ProgressIndicator progressIndicator;

    private ErDiagramComposite erComposite;

    private String title;

    /**
     * DOC admin ErDiagramDialog constructor comment.
     *
     * @param parentShell
     */
    public ErDiagramDialog(Shell parentShell, String title, RepositoryNode rootNode) {
        super(parentShell);
        this.title = title;
        this.rootNode = rootNode;
        setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.RESIZE | SWT.MIN | SWT.MAX);

    }

    private ISQLBuilderDialog dialog;

    public void setDialog(ISQLBuilderDialog dialog) {
        this.dialog = dialog;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ErDiagramDialog.textGenerateSelectStatement")); //$NON-NLS-1$
        if (title != null) {
            newShell.setText(title);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    protected Control createDialogArea(Composite parent) {
        // create a composite with standard margins and spacing
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginHeight = convertVerticalDLUsToPixels(0);
        layout.marginWidth = convertHorizontalDLUsToPixels(0);
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(container);
        erComposite = new ErDiagramComposite(container, SWT.VERTICAL);
        erComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        erComposite.setDialog(dialog);
        erComposite.setNodes(getNodes(), true);
        erComposite.setRootNode(rootNode);
        erComposite.setSqlText(sql);
        erComposite.setWeights(new int[] { 12, 3 });
        Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return container;

    }

    private RepositoryNode rootNode;

    private String sql;

    public void setSqlText(String sql) {
        this.sql = sql;
        if (erComposite != null) {
            erComposite.setSqlText(sql);
        }
    }

    /**
     * Return the initial size of the dialog.
     */
    protected Point getInitialSize() {
        return new Point(600, 450);
    }

    /**
     * Internal progress monitor implementation.
     */
    protected class ProgressMonitor implements IProgressMonitorWithBlocking {

        private boolean fIsCanceled;

        protected boolean forked = false;

        protected boolean locked = false;

        public void beginTask(String name, int totalWork) {
            if (progressIndicator.isDisposed()) {
                return;
            }
            if (totalWork == UNKNOWN) {
                progressIndicator.beginAnimatedTask();
            } else {
                progressIndicator.beginTask(totalWork);
            }
        }

        public void done() {
            if (!progressIndicator.isDisposed()) {
                progressIndicator.sendRemainingWork();
                progressIndicator.done();
            }
        }

        public void setTaskName(String name) {
        }

        public boolean isCanceled() {
            return fIsCanceled;
        }

        public void setCanceled(boolean b) {
            fIsCanceled = b;
            if (locked) {
                clearBlocked();
            }
        }

        public void subTask(String name) {
        }

        public void worked(int work) {
            internalWorked(work);
        }

        public void internalWorked(double work) {
            if (!progressIndicator.isDisposed()) {
                progressIndicator.worked(work);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
         */
        public void clearBlocked() {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
         */
        public void setBlocked(IStatus reason) {
            locked = true;
        }
    }

    /**
     * admin Comment method "getSql".
     *
     * @return
     */
    public String getSql() {
        return erComposite.getSqlText();
    }

    public List<IRepositoryNode> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<IRepositoryNode> nodes) {
        this.nodes = nodes;
    }

    public void setRootNode(RepositoryNode rootNode) {
        this.rootNode = rootNode;
        erComposite.setRootNode(rootNode);
    }
}
