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
package org.talend.designer.runtime.visualization.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.IJvmModelChangeListener;
import org.talend.designer.runtime.visualization.JvmModel;
import org.talend.designer.runtime.visualization.JvmModelEvent;
import org.talend.designer.runtime.visualization.JvmModelEvent.State;

/**
 * created by ldong on Apr 7, 2015 Detailled comment
 *
 */
public abstract class AbstractRuntimeGraphcsComposite extends Composite implements IJvmModelChangeListener {

    private Composite contentPage;

    private IActiveJvm jvm;

    private boolean suspendRefresh;

    protected boolean isSectionActivated;

    protected static boolean isMonitoring = false;

    /* the current activate job */
    private ISelection selection;

    AbstractRuntimeGraphcsComposite(Composite parent, ISelection selection, int style) {
        super(parent, style);
        initGraphcs();
    }

    private void initGraphcs() {
        // messagePage = createMessagePage(parent);

        contentPage = new Composite(this, SWT.NULL);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 0;
        formLayout.marginHeight = 0;
        contentPage.setLayout(formLayout);

        FormData pageData = new FormData();
        pageData.top = new FormAttachment(0, 0);
        pageData.bottom = new FormAttachment(100, 0);
        pageData.left = new FormAttachment(0, 0);
        pageData.right = new FormAttachment(100, 0);

        contentPage.setLayoutData(pageData);

        createControls(contentPage);

        contentPage.layout();

        contentPage.setVisible(true);

        JvmModel.getInstance().addJvmModelChangeListener(this);

        suspendRefresh = false;
    }

    public void setInput(ISelection selection) {

        if (!(selection instanceof StructuredSelection)) {
            return;
        }
        StructuredSelection structuredSelection = (StructuredSelection) selection;
        Object firstElement = structuredSelection.getFirstElement();

        IActiveJvm oldJvm = jvm;
        IActiveJvm newJvm = (IActiveJvm) firstElement;

        jvm = newJvm;
        if (oldJvm != newJvm || newJvm.isConnected()) {
            updatePage();
        }

        if (newJvm.isConnected()) {
            updatePage();
        }
        setInput(selection, newJvm, oldJvm);
    }

    /*
     * @see IJvmModelChangeListener#jvmModelChanged(JvmModelEvent)
     */
    @Override
    public void jvmModelChanged(final JvmModelEvent e) {
        if (e.state != State.JvmModified && e.state != State.JvmConnected && e.state != State.JvmDisconnected) {
            return;
        }

        if (e.state == State.JvmDisconnected) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                }
            });
        }

        if (jvm == null || e.jvm.getPid() != jvm.getPid()) {// contentPage.isDisposed() ||
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (e.state == State.JvmConnected) {
                    updatePage();
                }
                refresh();
            }
        });
    }

    abstract protected void refresh();

    public void suspendRefresh(boolean suspend) {
        suspendRefresh = suspend;
    }

    /**
     * Gets the active JVM.
     *
     * @return The active JVM
     */
    public IActiveJvm getJvm() {
        return jvm;
    }

    /**
     * Gets the state indicating if refresh is suspended.
     *
     * @return True if refresh is suspended
     */
    public boolean isRefreshSuspended() {
        return suspendRefresh;
    }

    /**
     * Refreshes the background of given control with given color.
     *
     * @param control The control
     * @param jvmConnected The state indicating if JVM is connected
     */
    public void refreshBackground(final Control control, boolean jvmConnected) {
        final int color;
        if (jvmConnected) {
            color = SWT.COLOR_LIST_BACKGROUND;
        } else {
            color = SWT.COLOR_WIDGET_BACKGROUND;
        }

        if (!control.isDisposed() && !(control instanceof Sash) && !(control instanceof Label)) {
            control.setBackground(Display.getDefault().getSystemColor(color));
        }
    }

    /**
     * Refreshes the background of given controls and its child controls with given color.
     *
     * @param controls The controls
     * @param jvmConnected The state indicating if JVM is connected
     */
    public void refreshBackground(Control[] controls, boolean jvmConnected) {
        for (Control control : controls) {
            if (control.isDisposed()) {
                continue;
            }
            refreshBackground(control, jvmConnected);
            if (control instanceof Composite) {
                refreshBackground(((Composite) control).getChildren(), jvmConnected);
            }
        }
    }

    public static boolean isMonitoring() {
		return isMonitoring;
	}

	public static void setMonitoring(boolean isMonitoring) {
		AbstractRuntimeGraphcsComposite.isMonitoring = isMonitoring;
	}

    /**
     * Adds the tool bar actions.
     *
     * @param manager The toolbar manager
     */
    protected void addToolBarActions(IToolBarManager manager) {
        // do nothing
    }

    /**
     * Removes the tool bar actions.
     *
     * @param manager The toolbar manager
     */
    protected void removeToolBarActions(IToolBarManager manager) {
        // do nothing
    }

    /**
     * Adds the local menus.
     *
     * @param manager The menu manager
     */
    protected void addLocalMenus(IMenuManager manager) {
        // do nothing
    }

    /**
     * Removes the local menus.
     *
     * @param manager The menu manager
     */
    protected void removeLocalMenus(IMenuManager manager) {
        // do nothing
    }

    /**
     * Updates the page.
     */
    protected void updatePage() {
        if (jvm == null) {
            return;
        }
    }

    /**
     * Sets the input.
     *
     * @param selection The selection
     * @param newJvm The active JVM
     * @param oldJvm The old active JVM
     */
    abstract protected void setInput(ISelection selection, IActiveJvm newJvm, IActiveJvm oldJvm);

    /**
     * Creates the controls.
     *
     * @param parent The parent
     */
    abstract protected void createControls(Composite parent);

}
