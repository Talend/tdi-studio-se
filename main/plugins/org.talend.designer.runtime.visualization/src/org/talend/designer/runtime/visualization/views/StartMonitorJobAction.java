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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.internal.ui.IConstants;

/**
 * created by ldong on Mar 24, 2015 Detailled comment
 *
 */
public class StartMonitorJobAction extends Action implements ISelectionChangedListener {

    /** The visibility. */
    private boolean visible;

    /** The active JVMs. */
    List<IActiveJvm> jvms;

    public StartMonitorJobAction() {
        jvms = new ArrayList<IActiveJvm>();
        visible = false;
    }

    /**
     * Refreshes the enable state.
     */
    public void refresh() {
        boolean enable = true;
        for (IActiveJvm jvm : jvms) {
            if (jvm.isConnected() || !jvm.isConnectionSupported()) {
                enable = false;
            }
        }
        setEnabled(enable);
    }

    /**
     * Gets the state indicating if this action is visible.
     *
     * @return <tt>true</tt> if this action is visible
     */
    protected boolean getVisible() {
        return visible;
    }

    @Override
    public void run() {
        new Job(Messages.startMonitoringJobLabel) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (final IActiveJvm jvm : jvms) {
                    /*
                     * show properties view before connecting to JVM, so that the properties view can react to the jvm
                     * connection event
                     */
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            showPropertiesView(jvm);
                        }
                    });

                    if (!jvm.isConnected() && isEnabled()) {
                        try {
                            int period = Activator.getDefault().getPreferenceStore().getInt(IConstants.UPDATE_PERIOD);
                            jvm.connect(period);
                        } catch (JvmCoreException e) {
                            Activator.log(NLS.bind(Messages.connectJvmFailedMsg, jvm.getPid()), e);
                            return Status.CANCEL_STATUS;
                        }
                    }
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void showPropertiesView(IActiveJvm jvm) {
        try {
            List<PropertySheet> views = getProperriesView();

            // check if there are no properties views
            if (views.size() == 0) {
                openPropertiesView(jvm);
                return;
            }

            // check if there is a corresponding properties view
            PropertySheet view = getPropertiesView(jvm, views);
            if (view != null) {
                brindPropertiesViewToFront(view);
                return;
            }

            // check if there is non-pinned properties view
            view = getNonPinnedPropertiesView(views);
            if (view != null) {
                brindPropertiesViewToFront(view);
                return;
            }

            openPropertiesView(jvm);

        } catch (PartInitException e) {
            Activator.log(IStatus.ERROR, Messages.bringPropertiesViewToFrontFailedMsg, e);
        }
    }

    /**
     * Gets the currently opened properties view.
     *
     * @return The properties views
     */
    private static List<PropertySheet> getProperriesView() {
        List<PropertySheet> list = new ArrayList<PropertySheet>();

        IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
        for (IViewReference viewReference : views) {
            if (IPageLayout.ID_PROP_SHEET.equals(viewReference.getId())) {
                list.add((PropertySheet) viewReference.getView(true));
            }
        }
        return list;
    }

    /**
     * Gets the properties view showing the given JVM from the given properties views.
     *
     * @param jvm The JVM
     * @param views The properties views
     * @return The properties view
     */
    private static PropertySheet getPropertiesView(IActiveJvm jvm, List<PropertySheet> views) {
        for (PropertySheet view : views) {
            ISelection selection = view.getShowInContext().getSelection();
            if (selection instanceof TreeSelection) {
                Object element = ((TreeSelection) selection).getFirstElement();
                if (jvm.equals(element)) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * Gets the non-pinned properties view.
     *
     * @param views The properties view
     * @return The non-pinned properties view
     */
    private static PropertySheet getNonPinnedPropertiesView(List<PropertySheet> views) {
        for (PropertySheet view : views) {
            if (!view.isPinned()) {
                return view;
            }
        }
        return null;
    }

    /**
     * Opens the properties view.
     *
     * @param jvm The JVM
     * @throws PartInitException
     */
    private static void openPropertiesView(IActiveJvm jvm) throws PartInitException {
        PropertySheet view = (PropertySheet) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .showView(IPageLayout.ID_PROP_SHEET, String.valueOf(new Date().getTime()), IWorkbenchPage.VIEW_ACTIVATE);

        // make sure to set the selection as an identifier of view
        PropertyShowInContext context = (PropertyShowInContext) view.getShowInContext();
        if (context.getSelection() == null) {
            RuntimeVisualizationView jvmExplorer = (RuntimeVisualizationView) context.getPart();
            if (jvmExplorer != null) {
                view.selectionChanged(jvmExplorer, jvmExplorer.getSelection());
            }
        }
    }

    /**
     * Brings the properties view to front.
     *
     * @param view The properties view
     * @throws PartInitException
     */
    private static void brindPropertiesViewToFront(PropertySheet view) throws PartInitException {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop(view);
        view.setPinned(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent
     * )
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (!(event.getSelection() instanceof TreeSelection)) {
            return;
        }

        jvms.clear();

        visible = false;
        for (Object element : ((TreeSelection) event.getSelection()).toArray()) {
            if (element instanceof IActiveJvm) {
                jvms.add((IActiveJvm) element);
                visible = true;
            } else {
                visible = false;
                jvms.clear();
                break;
            }
        }

        refresh();

    }
}
