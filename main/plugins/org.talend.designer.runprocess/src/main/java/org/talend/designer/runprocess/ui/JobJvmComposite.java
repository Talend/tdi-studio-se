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
package org.talend.designer.runprocess.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JobJvmComposite extends Composite {

    private static ProcessManager manager = ProcessManager.getInstance();

    /**
     * DOC Administrator JobJvmComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    private static RunProcessContext processContext;

    private Composite execComposite;

    public JobJvmComposite(Composite parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub

        this.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);
        Composite targetExecutionComposite = createTargetExecutionComposite(this);
        // targetExecutionComposite.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        // CSS
        CoreUIPlugin.setCSSClass(this, JobJvmComposite.class.getSimpleName());
    }

    protected Composite createTargetExecutionComposite(Composite parent) {
        execComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        execComposite.setLayout(layout);

        StyledText text = new StyledText(execComposite, SWT.NONE);
        text.setText(Messages.getString("ProcessComposite.targetExecutionTabTooltipAvailable")); //$NON-NLS-1$
        text.setWordWrap(true);
        text.setEditable(false);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        return execComposite;
    }

    protected void addListeners() {

    }

    public void setProcessContext(RunProcessContext processContext) {
        this.processContext = processContext;
        if (execComposite != null) {
            for (Control control : execComposite.getChildren()) {
                control.setEnabled(processContext != null);
            }
        }
    }

    public void debug() {

    }

    public void exec() throws PersistenceException, BusinessException{

    }

    public void kill() {

    }

    protected void setRunnable(boolean runnable) {

    }

    protected boolean checkKillAllowed() {
        return true;
    }

    public void setDebugEnabled(Boolean enable) {

    }

    protected static RunProcessContext getProcessContext() {
        return manager.getProcessContext();
    }

}
