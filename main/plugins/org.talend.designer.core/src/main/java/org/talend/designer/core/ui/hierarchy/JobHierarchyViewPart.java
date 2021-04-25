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
package org.talend.designer.core.ui.hierarchy;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.viewsupport.SelectionProviderMediator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.IJobHierarchyViewPart;
import org.talend.repository.ProjectManager;

/**
 * view showing the super jobs/sub jobs of its input.
 */
public class JobHierarchyViewPart extends ViewPart implements IJobHierarchyViewPart {

    public static final String ID = "org.talend.designer.core.ui.hierarchy.JobHierarchyViewPart"; //$NON-NLS-1$

    private static final String GROUP_FOCUS = "group.focus"; //$NON-NLS-1$

    private static final String DIALOGSTORE_VIEWLAYOUT = "TypeHierarchyViewPart.orientation"; //$NON-NLS-1$

    // input job or null
    private IProcess2 inputProcess;

    private IDialogSettings fDialogSettings;

    private ToggleViewAction[] fViewActions;

    private int fCurrentViewerIndex;

    private PageBook fPagebook;

    private Label fNoHierarchyShownLabel;

    private SashForm fTypeMethodsSplitter;

    private ViewForm fTypeViewerViewForm;

    private ViewForm dependencyViewerViewForm;

    private CLabel dependencyViewerPaneLabel;

    private PageBook fViewerbook;

    private JobHierarchyViewer[] fAllViewers;

    private Label fEmptyTypesViewer;

    private Composite fParent;

    private TableViewer dependencyViewer;

    private JobHierarchyLifeCycle fHierarchyLifeCycle;

    private ISelectionChangedListener fSelectionChangedListener;

    private ToggleOrientationAction[] fToggleOrientationActions;

    private CompositeActionGroup fActionGroups;

    private FocusOnJobAction focusOnTypeAction = null;

    /**
     * Constructor
     */
    public JobHierarchyViewPart() {
        fHierarchyLifeCycle = new JobHierarchyLifeCycle();
        fViewActions = new ToggleViewAction[] { new ToggleViewAction(this, IJobHierarchyViewPart.HIERARCHY_MODE_SUBTYPES),
                new ToggleViewAction(this, IJobHierarchyViewPart.HIERARCHY_MODE_SUPERTYPES) };

        fSelectionChangedListener = new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                doSelectionChanged(event);
            }
        };
        fDialogSettings = DesignerPlugin.getDefault().getDialogSettings();
        fToggleOrientationActions = new ToggleOrientationAction[] { new ToggleOrientationAction(this, VIEW_LAYOUT_VERTICAL),
                new ToggleOrientationAction(this, VIEW_LAYOUT_HORIZONTAL),
                new ToggleOrientationAction(this, VIEW_LAYOUT_AUTOMATIC), new ToggleOrientationAction(this, VIEW_LAYOUT_SINGLE) };

        focusOnTypeAction = new FocusOnJobAction(this);
    }

    String showEmptyLabel = Messages.getString("JobHierarchyViewPart.showDecription"); //$NON-NLS-1$

    private int fCurrentLayout;

    private boolean fInComputeLayout;

    private SelectionProviderMediator fSelectionProviderMediator;

    @Override
    public void createPartControl(Composite container) {
        fParent = container;
        addResizeListener(fParent);
        fPagebook = new PageBook(container, SWT.NONE);
        // page 1 of page book (no hierarchy label)

        fNoHierarchyShownLabel = new Label(fPagebook, SWT.TOP + SWT.LEFT + SWT.WRAP);
        fNoHierarchyShownLabel.setText(showEmptyLabel);

        // page 2 of page book (viewers)
        fTypeMethodsSplitter = new SashForm(fPagebook, SWT.VERTICAL);
        fTypeMethodsSplitter.setVisible(false);

        fTypeViewerViewForm = new ViewForm(fTypeMethodsSplitter, SWT.NONE);

        Control typeViewerControl = createTypeViewerControl(fTypeViewerViewForm);
        fTypeViewerViewForm.setContent(typeViewerControl);

        dependencyViewerViewForm = new ViewForm(fTypeMethodsSplitter, SWT.NONE);
        fTypeMethodsSplitter.setWeights(new int[] { 65, 35 });

        Control dependencyViewerPart = createMethodViewerControl(dependencyViewerViewForm);
        dependencyViewerViewForm.setContent(dependencyViewerPart);

        dependencyViewerPaneLabel = new CLabel(dependencyViewerViewForm, SWT.NONE);
        dependencyViewerViewForm.setTopLeft(dependencyViewerPaneLabel);

        ToolBar methodViewerToolBar = new ToolBar(dependencyViewerViewForm, SWT.FLAT | SWT.WRAP);
        dependencyViewerViewForm.setTopCenter(methodViewerToolBar);

        initDragAndDrop();

        MenuManager menu = new MenuManager();
        menu.add(focusOnTypeAction);
        fNoHierarchyShownLabel.setMenu(menu.createContextMenu(fNoHierarchyShownLabel));
        fPagebook.showPage(fNoHierarchyShownLabel);

        int layout;
        try {
            layout = fDialogSettings.getInt(DIALOGSTORE_VIEWLAYOUT);
            if (layout < 0 || layout > 3) {
                layout = VIEW_LAYOUT_AUTOMATIC;
            }
        } catch (NumberFormatException e) {
            layout = VIEW_LAYOUT_AUTOMATIC;
        }
        // force the update
        fCurrentLayout = -1;
        // will fill the main tool bar
        setViewLayout(layout);

        // set the filter menu items
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager viewMenu = actionBars.getMenuManager();
        // for (int i = 0; i < fViewActions.length; i++) {
        // ToggleViewAction action = fViewActions[i];
        // viewMenu.add(action);
        // action.setEnabled(false);
        // }
        // viewMenu.add(new Separator());

        // IMenuManager layoutSubMenu = new MenuManager(TypeHierarchyMessages.TypeHierarchyViewPart_layout_submenu);
        IMenuManager layoutSubMenu = new MenuManager(Messages.getString("FocusOnJobAction.TypeHierarchyViewPart_layout_submenu")); //$NON-NLS-1$
        viewMenu.add(layoutSubMenu);
        for (int i = 0; i < fToggleOrientationActions.length; i++) {
            layoutSubMenu.add(fToggleOrientationActions[i]);
        }
        viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        // selection provider
        int nHierarchyViewers = fAllViewers.length;
        StructuredViewer[] trackedViewers = new StructuredViewer[nHierarchyViewers + 1];
        for (int i = 0; i < nHierarchyViewers; i++) {
            trackedViewers[i] = fAllViewers[i];
        }
        trackedViewers[nHierarchyViewers] = dependencyViewer;
        fSelectionProviderMediator = new SelectionProviderMediator(trackedViewers, getCurrentViewer());
        getSite().setSelectionProvider(fSelectionProviderMediator);
        ActionGroup[] actionGroups = new ActionGroup[] { new JobActionGroup() };
        fActionGroups = new CompositeActionGroup(actionGroups);

        fActionGroups.fillActionBars(actionBars);
    }

    private Control createMethodViewerControl(Composite parent) {
        dependencyViewer = new TableViewer(parent);

        DependencyViewerProvider provider = new DependencyViewerProvider(this.fHierarchyLifeCycle);
        dependencyViewer.setContentProvider(provider);
        dependencyViewer.setLabelProvider(provider);
        Control control = dependencyViewer.getTable();

        String popupId = "DependencyViewer_ContextMenu"; //$NON-NLS-1$
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager menu) {
                fillDependencyViewerContextMenu(menu);
            }
        });
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
        getSite().registerContextMenu(popupId, menuMgr, dependencyViewer);

        return control;
    }

    /**
     * bqian Comment method "createTypeViewerControl".
     *
     * @param typeViewerViewForm
     * @return
     */
    private Control createTypeViewerControl(Composite parent) {
        fViewerbook = new PageBook(parent, SWT.NULL);

        // Create the viewers
        JobHierarchyViewer superTypesViewer = new SuperJobHierarchyViewer(fViewerbook, fHierarchyLifeCycle, this);
        initializeTypesViewer(superTypesViewer);
        JobHierarchyViewer subTypesViewer = new SubJobHierarchyViewer(fViewerbook, fHierarchyLifeCycle, this);
        initializeTypesViewer(subTypesViewer);
        fAllViewers = new JobHierarchyViewer[2];
        fAllViewers[HIERARCHY_MODE_SUBTYPES] = subTypesViewer;
        fAllViewers[HIERARCHY_MODE_SUPERTYPES] = superTypesViewer;

        int currViewerIndex;
        try {
            // TODO
            // currViewerIndex = fDialogSettings.getInt(DIALOGSTORE_HIERARCHYVIEW);
            currViewerIndex = HIERARCHY_MODE_SUBTYPES;
            if (currViewerIndex < 0 || currViewerIndex > 1) {
                currViewerIndex = HIERARCHY_MODE_SUBTYPES;
            }
        } catch (NumberFormatException e) {
            currViewerIndex = HIERARCHY_MODE_SUBTYPES;
        }

        fEmptyTypesViewer = new Label(fViewerbook, SWT.TOP | SWT.LEFT | SWT.WRAP);

        for (int i = 0; i < fAllViewers.length; i++) {
            fAllViewers[i].setInput(fAllViewers[i]);
        }

        // force the update
        fCurrentViewerIndex = -1;
        setHierarchyMode(currViewerIndex);

        return fViewerbook;
    }

    @Override
    public void setFocus() {
        fPagebook.setFocus();

    }

    private JobHierarchyViewer getCurrentViewer() {
        return fAllViewers[fCurrentViewerIndex];
    }

    public void setHierarchyMode(int viewerIndex) {
        Assert.isNotNull(fAllViewers);
        if (viewerIndex < fAllViewers.length && fCurrentViewerIndex != viewerIndex) {
            fCurrentViewerIndex = viewerIndex;

            updateHierarchyViewer(true);
            if (inputProcess != null) {
                ISelection currSelection = getCurrentViewer().getSelection();
                if (currSelection == null || currSelection.isEmpty()) {
                    internalSelectType(inputProcess, false);
                    currSelection = getCurrentViewer().getSelection();
                }
            }
            updateTitle();

            // fDialogSettings.put(DIALOGSTORE_HIERARCHYVIEW, viewerIndex);
            getCurrentViewer().getTree().setFocus();
        }
        for (int i = 0; i < fViewActions.length; i++) {
            ToggleViewAction action = fViewActions[i];
            action.setChecked(fCurrentViewerIndex == action.getViewerIndex());
        }
    }

    private void internalSelectType(IProcess2 process, boolean reveal) {
        JobHierarchyViewer viewer = getCurrentViewer();
        // viewer.removePostSelectionChangedListener(fSelectionChangedListener);
        // viewer.setSelection(elem != null ? new StructuredSelection(elem) : StructuredSelection.EMPTY, reveal);
        // viewer.addPostSelectionChangedListener(fSelectionChangedListener);
    }

    /*
     * When the input changed or the hierarchy pane becomes visible, <code>updateHierarchyViewer<code> brings up the
     * correct view and refreshes the current tree
     */
    private void updateHierarchyViewer(final boolean doExpand) {
        if (inputProcess == null) {
            fNoHierarchyShownLabel.setText(showEmptyLabel);
            fPagebook.showPage(fNoHierarchyShownLabel);
        } else {
            if (getCurrentViewer().containsElements() != null) {
                Runnable runnable = new Runnable() {

                    public void run() {
                        getCurrentViewer().updateContent(doExpand); // refresh
                    }
                };
                BusyIndicator.showWhile(getDisplay(), runnable);
                if (!isChildVisible(fViewerbook, getCurrentViewer().getControl())) {
                    setViewerVisibility(true);
                }
            } else {
                fEmptyTypesViewer.setText(Messages.getString("JobHierarchyViewPart.reason")); //$NON-NLS-1$
                setViewerVisibility(false);
            }
        }
    }

    /*
     * Toggles between the empty viewer page and the hierarchy
     */
    private void setViewerVisibility(boolean showHierarchy) {
        if (showHierarchy) {
            fViewerbook.showPage(getCurrentViewer().getControl());
        } else {
            fViewerbook.showPage(fEmptyTypesViewer);
        }
    }

    public IProcess2 getInputProcess() {
        return inputProcess;
    }

    public void setInputProcess(IProcess2 process) {
        // TODO FOR HISTORY
        // if (element != null && !element.equals(fInputElement)) {
        // addHistoryEntry(element);
        // }
        updateInput(process);
    }

    public int getHierarchyMode() {
        return fCurrentViewerIndex;
    }

    private Display getDisplay() {
        if (fPagebook != null && !fPagebook.isDisposed()) {
            return fPagebook.getDisplay();
        }
        return null;
    }

    private boolean isChildVisible(Composite pb, Control child) {
        Control[] children = pb.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == child && children[i].isVisible())
                return true;
        }
        return false;
    }

    /*
     * Changes the input to a new type
     *
     * @param inputElement
     */
    private void updateInput(IProcess2 newProcess) {
        IProcess2 prevInput = inputProcess;

        // synchronized (this) {
        // if (fRestoreStateJob != null) {
        // fRestoreStateJob.cancel();
        // try {
        // fRestoreStateJob.join();
        // } catch (InterruptedException e) {
        // // ignore
        // } finally {
        // fRestoreStateJob = null;
        // }
        // }
        // }

        // Make sure the UI got repainted before we execute a long running
        // operation. This can be removed if we refresh the hierarchy in a
        // separate thread.
        // Work-around for http://dev.eclipse.org/bugs/show_bug.cgi?id=30881
        // processOutstandingEvents();
        if (newProcess == null) {
            clearInput();
        } else {
            inputProcess = newProcess;
            fNoHierarchyShownLabel.setText(Messages.getString("JobHierarchyMessages.JobHierarchyViewPart_createinput", //$NON-NLS-1$
                    getJobLabel()));
            try {
                fHierarchyLifeCycle.ensureRefreshedTypeHierarchy(inputProcess, PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow());
                // fHierarchyLifeCycle.ensureRefreshedTypeHierarchy(inputElement, getSite().getWorkbenchWindow());
            } catch (InvocationTargetException e) {
                org.talend.commons.ui.runtime.exception.ExceptionHandler.process(e);
                clearInput();
                return;
            } catch (InterruptedException e) {
                fNoHierarchyShownLabel.setText(showEmptyLabel);
                return;
            }

            // internalSelectType(null, false); // clear selection
            updateHierarchyViewer(true);
            internalSelectType(inputProcess, true);
            updateToolbarButtons();
            updateTitle();
            fPagebook.showPage(fTypeMethodsSplitter);
        }
    }

    private void clearInput() {
        inputProcess = null;
        fHierarchyLifeCycle.freeHierarchy();

        updateHierarchyViewer(false);
        updateToolbarButtons();
    }

    private void updateToolbarButtons() {
        // boolean isType = inputProcess instanceof IType;
        // for (int i = 0; i < fViewActions.length; i++) {
        // ToggleViewAction action = fViewActions[i];
        // if (action.getViewerIndex() == HIERARCHY_MODE_CLASSIC) {
        // action.setEnabled(fInputElement != null);
        // } else {
        // action.setEnabled(isType);
        // }
        // }
    }

    private void updateTitle() {
        String viewerTitle = getCurrentViewer().getTitle();

        String tooltip;
        String title;
        if (inputProcess != null) {
            String[] args = new String[] { viewerTitle, getJobLabel(), getProjectLabel() };
            title = Messages.getString("JobHierarchyMessages.JobHierarchyViewPart_title", args); //$NON-NLS-1$
            tooltip = Messages.getString("JobHierarchyMessages.JobHierarchyViewPart_tooltip", args); //$NON-NLS-1$

        } else {
            title = ""; //$NON-NLS-1$
            tooltip = viewerTitle;
        }
        setContentDescription(title);
        setTitleToolTip(tooltip);
    }

    private String getJobLabel() {
        return inputProcess.getLabel();
    }

    private String getProjectLabel() {
        org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(
                inputProcess.getProperty().getItem());
        return project.getTechnicalLabel();
    }

    private void initDragAndDrop() {
        for (int i = 0; i < fAllViewers.length; i++) {
            addDropAdapters(fAllViewers[i]);
        }

        // DND on empty hierarchy
        DropTarget dropTarget = new DropTarget(fPagebook, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
        dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getInstance() });
        dropTarget.addDropListener(new JobHierarchyTransferDropAdapter(this));
    }

    private void addDropAdapters(AbstractTreeViewer viewer) {
        Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getInstance() };
        int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT;
        DelegatingDropAdapter delegatingDropAdapter = new DelegatingDropAdapter();
        delegatingDropAdapter.addDropTargetListener(new JobHierarchyTransferDropAdapter(this));
        viewer.addDropSupport(ops, transfers, delegatingDropAdapter);
    }

    private void jobSelectionChanged(ISelection sel) {
        if (sel instanceof IStructuredSelection) {
            Object object = ((IStructuredSelection) sel).getFirstElement();
            if (object == null) {
                return;
            }
            IProcess2 process = (IProcess2) object;
            updateMethodViewer(process);
        }
    }

    protected void doSelectionChanged(SelectionChangedEvent e) {
        if (e.getSelectionProvider() == this.dependencyViewer) {

        } else {
            jobSelectionChanged(e.getSelection());
        }
    }

    private void updateMethodViewer(final IProcess2 input) {

        if (input == dependencyViewer.getInput()) {
            if (input != null) {
                Runnable runnable = new Runnable() {

                    public void run() {
                        dependencyViewer.refresh(); // refresh
                    }
                };
                BusyIndicator.showWhile(getDisplay(), runnable);
            }
        } else {
            if (input != null) {
                ILabelProvider provider = (ILabelProvider) getCurrentViewer().getLabelProvider();
                dependencyViewerPaneLabel.setText(provider.getText(input));
                dependencyViewerPaneLabel.setImage(provider.getImage(input));
            } else {
                dependencyViewerPaneLabel.setText(""); //$NON-NLS-1$
                dependencyViewerPaneLabel.setImage(null);
            }
            Runnable runnable = new Runnable() {

                public void run() {
                    dependencyViewer.setInput(input); // refresh
                }
            };
            BusyIndicator.showWhile(getDisplay(), runnable);
        }
    }

    public void setViewLayout(int layout) {
        if (fCurrentLayout != layout || layout == VIEW_LAYOUT_AUTOMATIC) {
            fInComputeLayout = true;
            try {
                boolean methodViewerNeedsUpdate = false;

                if (this.dependencyViewerViewForm != null && !dependencyViewerViewForm.isDisposed()
                        && fTypeMethodsSplitter != null && !fTypeMethodsSplitter.isDisposed()) {

                    boolean horizontal = false;
                    if (layout == VIEW_LAYOUT_SINGLE) {
                        dependencyViewerViewForm.setVisible(false);
                        // showMembersInHierarchy(false);
                        updateMethodViewer(null);
                    } else {
                        if (fCurrentLayout == VIEW_LAYOUT_SINGLE) {
                            dependencyViewerViewForm.setVisible(true);
                            methodViewerNeedsUpdate = true;
                        }
                        if (layout == VIEW_LAYOUT_AUTOMATIC) {
                            if (fParent != null && !fParent.isDisposed()) {
                                Point size = fParent.getSize();
                                if (size.x != 0 && size.y != 0) {
                                    // bug 185397 - Hierarchy View flips orientation multiple times on resize
                                    // Control viewFormToolbar = fTypeViewerViewForm.getTopLeft();
                                    // if (viewFormToolbar != null && !viewFormToolbar.isDisposed() &&
                                    // viewFormToolbar.isVisible()) {
                                    // size.y -= viewFormToolbar.getSize().y;
                                    // }
                                    horizontal = size.x > size.y;
                                }
                            }
                            if (fCurrentLayout == VIEW_LAYOUT_AUTOMATIC) {
                                boolean wasHorizontal = fTypeMethodsSplitter.getOrientation() == SWT.HORIZONTAL;
                                if (wasHorizontal == horizontal) {
                                    return; // no real change
                                }
                            }

                        } else if (layout == VIEW_LAYOUT_HORIZONTAL) {
                            horizontal = true;
                        }
                        fTypeMethodsSplitter.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
                    }
                    // updateMainToolbar(horizontal);
                    fTypeMethodsSplitter.layout();
                }
                if (methodViewerNeedsUpdate) {
                    jobSelectionChanged(getCurrentViewer().getSelection());
                }
                fDialogSettings.put(DIALOGSTORE_VIEWLAYOUT, layout);
                fCurrentLayout = layout;

                updateCheckedState();
            } finally {
                fInComputeLayout = false;
            }
        }
    }

    private void addResizeListener(Composite parent) {
        parent.addControlListener(new ControlListener() {

            public void controlMoved(ControlEvent e) {
            }

            public void controlResized(ControlEvent e) {
                if (getViewLayout() == VIEW_LAYOUT_AUTOMATIC && !fInComputeLayout) {
                    setViewLayout(VIEW_LAYOUT_AUTOMATIC);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.ui.ITypeHierarchyViewPart#getViewLayout()
     */
    public int getViewLayout() {
        return fCurrentLayout;
    }

    private void updateCheckedState() {
        for (int i = 0; i < fToggleOrientationActions.length; i++) {
            fToggleOrientationActions[i].setChecked(getViewLayout() == fToggleOrientationActions[i].getOrientation());
        }
    }

    private void initializeTypesViewer(final JobHierarchyViewer typesViewer) {
        // typesViewer.getControl().setVisible(false);
        typesViewer.initContextMenu(new IMenuListener() {

            public void menuAboutToShow(IMenuManager menu) {
                fillJobViewerContextMenu(typesViewer, menu);
            }
        }, getSite());

        typesViewer.addPostSelectionChangedListener(fSelectionChangedListener);
    }

    private void fillJobViewerContextMenu(JobHierarchyViewer viewer, IMenuManager menu) {
        // viewer entries
        // ISelection selection = viewer.getSelection();

        // if (focusOnSelectionAction.canActionBeAdded())
        // menu.appendToGroup(GROUP_FOCUS, focusOnSelectionAction);
        // menu.appendToGroup(GROUP_FOCUS, fFocusOnTypeAction);

        menu.add(focusOnTypeAction);

        fActionGroups.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
        fActionGroups.fillContextMenu(menu);
        fActionGroups.setContext(null);
    }

    /*
     * Creates the context menu for the method viewer
     */
    private void fillDependencyViewerContextMenu(IMenuManager menu) {
        // viewer entries
        fActionGroups.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
        fActionGroups.fillContextMenu(menu);
        fActionGroups.setContext(null);
    }

    public void dispose() {
        fHierarchyLifeCycle.freeHierarchy();

        if (fActionGroups != null)
            fActionGroups.dispose();

        super.dispose();
    }
}
