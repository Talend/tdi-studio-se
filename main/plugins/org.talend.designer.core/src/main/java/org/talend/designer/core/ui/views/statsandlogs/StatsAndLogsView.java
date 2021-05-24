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
package org.talend.designer.core.ui.views.statsandlogs;

import org.eclipse.draw2d.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.talend.core.model.process.EComponentCategory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * class global comment. Detailed comment <br/>
 *
 * @deprecated remove to job settings
 */
public class StatsAndLogsView extends ViewPart {

    public static final String ID = "org.talend.designer.core.ui.views.statsandlogs.statsAndLogsView"; //$NON-NLS-1$

    private static final String VIEW_NAME = "Stats/Logs";; //$NON-NLS-1$

    private Process process;

    private String title;

    private Composite parent;

    private StatsAndLogsComposite statsAndLogsViewComposite;

    private boolean isEmptyComposite;

    /**
     * Default StatsAndLogsView constructor.
     */
    public StatsAndLogsView() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        this.getCurrentJob();
        if (this.process != null) {
            createStatsAndLogsView();
        } else {
            this.createEmptyPartControl(parent);
        }
    }

    /**
     * Creates a empty composite if no job opened.
     *
     * @param parent
     */
    private void createEmptyPartControl(Composite parent) {

        if (parent != null && !parent.isDisposed()) {
            Control[] children = parent.getChildren();
            for (Control control : children) {
                control.dispose();
            }
        }

        Composite alertComposite = new Composite(parent, SWT.NONE);
        alertComposite.setLayout(new GridLayout());
        alertComposite.setLayoutData(new GridData());
        Text alertText = new Text(alertComposite, SWT.NONE);
        alertText.setText(Messages.getString("StatsAndLogsView.dataNotAvailable")); //$NON-NLS-1$
        alertText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        parent.layout();
    }

    /**
     * Creates a full composite if job is opened.
     *
     * @param parent
     */
    private void createFullPartControl(Composite parentComposite, Process process) {

        if (parentComposite != null && !parentComposite.isDisposed()) {
            Control[] children = parentComposite.getChildren();
            for (Control control : children) {
                control.dispose();
            }
        }

        statsAndLogsViewComposite = new StatsAndLogsComposite(parentComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS,
                EComponentCategory.STATSANDLOGS, process);
        statsAndLogsViewComposite.refresh();
    }

    /**
     * ftang Comment method "createStatsAndLogsView".
     *
     */
    public void createStatsAndLogsView() {
        IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
        createFullPartControl(this.parent, (Process) talendEditor.getProcess());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.parent.setFocus();
    }

    /**
     * Gets current opened job.
     */
    private void getCurrentJob() {
        final IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (activeEditor != null && activeEditor instanceof AbstractMultiPageTalendEditor) {
            AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
            Process process = (Process) talendEditor.getProcess();
            if (process != null) {
                this.process = process;
                this.title = talendEditor.getTitle();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.ViewPart#setPartName(java.lang.String)
     */
    public void setPartName(String title) {
        String viewName = VIEW_NAME;
        if (!title.equals("")) { //$NON-NLS-1$
            viewName = viewName + "(" + title + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        super.setPartName(viewName);
    }

    /**
     * ftang Comment method "refresh".
     */
    public void refresh() {
        if (statsAndLogsViewComposite == null || statsAndLogsViewComposite.isDisposed()) {
            createStatsAndLogsView();
        }
        statsAndLogsViewComposite.refresh();
    }

    /**
     * Empty view if no job is opened.
     */
    public void emptyView() {
        createEmptyPartControl(parent);
        process = null;
    }

    /**
     * ftang Comment method "refreshView".
     */
    public void refreshView() {
        if (!this.isEmptyComposite) {
            refresh();
        }
    }

}
