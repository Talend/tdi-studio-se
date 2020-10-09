// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.ExtensionPointLimiterImpl;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.properties.tab.TalendPropertyTabDescriptor;
import org.talend.designer.core.ui.views.properties.EElementType;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.designer.runprocess.EDebugProcessType;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessContextManager;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.AdvanceSettingComposite;
import org.talend.designer.runprocess.ui.MemoryRuntimeComposite;
import org.talend.designer.runprocess.ui.ProcessComposite;
import org.talend.designer.runprocess.ui.ProcessContextComposite;
import org.talend.designer.runprocess.ui.ProcessManager;
import org.talend.designer.runprocess.ui.TargetExecComposite;
import org.talend.designer.runprocess.ui.TraceDebugProcessComposite;
import org.talend.designer.runprocess.ui.actions.ClearPerformanceAction;
import org.talend.repository.RepositoryPlugin;

/**
 * View showing the execution of a process. <br/>
 *
 * $Id$
 *
 */
public class ProcessView extends ViewPart implements PropertyChangeListener {

    public static final String ID = RunProcessPlugin.PLUGIN_ID + ".ui.views.processview"; //$NON-NLS-1$

    public static final int EXEC_ID = 21;

    public static final int PAUSE_ID = 22;

    public static final int RESUME_ID = 23;

    public static final int DEBUG_ID = 24;

    public static final int TRACEDEBUG_ID = 25;

    private static Logger log = Logger.getLogger(ProcessView.class);

    private Label processNameLab;

    private IDynamicProperty dc = null;

    private ProcessComposite processComposite;

    private TraceDebugProcessComposite debugTisProcessComposite;

    private AdvanceSettingComposite advanceComposite;

    private MemoryRuntimeComposite memoryRunComposite;

    private PropertyChangeListener contextManagerListener;

    private ClearPerformanceAction clearPerfAction;

    private HorizontalTabFactory tabFactory = null;

    private TalendPropertyTabDescriptor currentSelectedTab;

    private Element element;

    private Button moveButton;

    private ProcessManager rubjobManager;

    public RunAction runAction;

    private SashForm sash;

    private boolean selectedPrimary = true;

    Composite compositeall;

    private IProcessViewHelper processViewHelper;

    private static RunProcessContext processContext;

    private Map<EDebugProcessType, IDebugViewHelper> debugViewHelpers;

    private boolean canRun = true;

    private ProcessContextComposite contextComposite;

    private Composite parent;

    private static TargetExecComposite targetComposite;

    public static ProcessView findProcessView() {
        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (ww != null) {
            IWorkbenchPage activePage = ww.getActivePage();
            if (activePage != null) {
                IViewPart part = activePage.findView(ID);
                try {
                    if (part == null) {
                        part = activePage.showView(ID);
                    }
                } catch (Exception e) {
                    // do nothing
                }
                return (ProcessView) part;
            }
        }
        return null;
    }

    /**
     * Constructs a new ProcessView.
     */
    public ProcessView() {
        super();
        IExtensionPointLimiter extensionPointLimiter = new ExtensionPointLimiterImpl(
                "org.talend.designer.runprocess.runprocess_view_helper", "runprocess_view_helper"); //$NON-NLS-1$ //$NON-NLS-2$
        IExtensionPointLimiter debugextensionPointLimiter = new ExtensionPointLimiterImpl(
                "org.talend.designer.runprocess.debugprocess_view_helper", "debugprocess_view_helper");
        debugViewHelpers = new HashMap<EDebugProcessType, IDebugViewHelper>();
        ExtensionImplementationProvider.getInstance(debugextensionPointLimiter, null).forEach(e -> {
            if (e instanceof IDebugViewHelper) {
                IDebugViewHelper helper = (IDebugViewHelper) e;
                debugViewHelpers.put(helper.getDebugType(), helper);
            }
        });
        
        IProcessViewHelper processViewHelperPrm = ExtensionImplementationProvider.getSingleInstance(extensionPointLimiter, null);

        if (processViewHelperPrm == null) {
            processViewHelperPrm = new DefaultProcessViewHelper();
        }
        tabFactory = new HorizontalTabFactory();
        setProcessViewHelper(processViewHelperPrm);
        
        if(!debugViewHelpers.containsKey(EDebugProcessType.DI)) {
         // TOS Debug helper
            DefaultDebugviewHelper defaultDebugViewHelper = new DefaultDebugviewHelper();
            debugViewHelpers.put(EDebugProcessType.DI, defaultDebugViewHelper);
        }
        rubjobManager = ProcessManager.getInstance();
        ProxyRepositoryFactory.getInstance().addPropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        parent.setLayout(new FillLayout());

        sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        sash.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        sash.setLayout(layout);
        Composite left = new Composite(sash, SWT.NONE);

        left.setLayout(new FillLayout());

        Composite right = new Composite(sash, SWT.NONE);
        right.setLayout(new FormLayout());
        FormData layouDatag = new FormData();
        layouDatag.left = new FormAttachment(0, 0);
        layouDatag.width = 32;
        layouDatag.top = new FormAttachment(0, 0);
        layouDatag.bottom = new FormAttachment(100, 0);
        final Composite buttonComposite = new Composite(right, SWT.ERROR);
        buttonComposite.setLayoutData(layouDatag);
        buttonComposite.setLayout(new GridLayout());

        Composite cotextCom = new Composite(right, SWT.NONE);
        layouDatag = new FormData();
        layouDatag.left = new FormAttachment(0, 32);
        layouDatag.right = new FormAttachment(100, 0);
        layouDatag.top = new FormAttachment(0, 0);
        layouDatag.bottom = new FormAttachment(100, 0);
        cotextCom.setLayoutData(layouDatag);
        cotextCom.setLayout(new GridLayout());

        tabFactory.initComposite(left, false);
        moveButton = new Button(buttonComposite, SWT.PUSH);
        moveButton.setText(">>"); //$NON-NLS-1$
        moveButton.setToolTipText(Messages.getString("ProcessComposite.hideContext"));
        final GridData layoutData = new GridData();
        layoutData.verticalAlignment = GridData.CENTER;
        layoutData.horizontalAlignment = GridData.CENTER;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        moveButton.setLayoutData(layoutData);
        addListeners();
        sash.setSashWidth(5);
        sash.setWeights(new int[] { 18, 5 });

        contextComposite = new ProcessContextComposite(cotextCom, SWT.NONE);
        contextComposite.setBackground(right.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        // processComposite = new ProcessComposite(tabFactory.getTabComposite(), SWT.H_SCROLL | SWT.V_SCROLL |
        // SWT.NO_FOCUS);
        // dc = processComposite;
        // createBasicComposite(tabFactory.getTabComposite(), element, null);
        tabFactory.getTabComposite().layout();
        tabFactory.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                TalendPropertyTabDescriptor descriptor = (TalendPropertyTabDescriptor) selection.getFirstElement();

                if (descriptor == null) {
                    return;
                }

                if (currentSelectedTab != null && (currentSelectedTab.getCategory() != descriptor.getCategory())) {
                    for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                        curControl.dispose();
                    }
                }

                if (currentSelectedTab == null || currentSelectedTab.getCategory() != descriptor.getCategory() || selectedPrimary) {

                    currentSelectedTab = descriptor;
                    selectedPrimary = false;
                    createDynamicComposite(tabFactory.getTabComposite(), (Element) descriptor.getData(), descriptor.getCategory());
                }
            }
        });
        setElement();

        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        IHandler handler1;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (brandingService.getBrandingConfiguration().isAllowDebugMode()) {
            Action debugAction = new DebugAction();
            handler1 = new ActionHandler(debugAction);
            handlerService.activateHandler(debugAction.getActionDefinitionId(), handler1);
        }
        Action killAction = new KillAction();
        handler1 = new ActionHandler(killAction);
        handlerService.activateHandler(killAction.getActionDefinitionId(), handler1);

        FocusListener fl = new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                log.trace(Messages.getString("ProcessView.gainFocusLog")); //$NON-NLS-1$
                IContextService contextService = (IContextService) RunProcessPlugin.getDefault().getWorkbench()
                        .getAdapter(IContextService.class);
                ca = contextService.activateContext("talend.runProcess"); //$NON-NLS-1$
            }

            @Override
            public void focusLost(FocusEvent e) {
                log.trace(Messages.getString("ProcessView.lostFocusLog")); //$NON-NLS-1$
                if (ca != null) {
                    IContextService contextService = (IContextService) RunProcessPlugin.getDefault().getWorkbench()
                            .getAdapter(IContextService.class);
                    contextService.deactivateContext(ca);
                }
            }
        };

        addListenerOnChildren(parent, fl);
        rubjobManager.setProcessShell(getSite().getShell());

        contextManagerListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (RunProcessContextManager.PROP_ACTIVE.equals(evt.getPropertyName())) {
                    // rubjobManager.setBooleanTrace(false);
                    runningProcessChanged();
                }
            }
        };
        RunProcessPlugin.getDefault().getRunProcessContextManager().addPropertyChangeListener(contextManagerListener);
        runAction = new RunAction();
    }

    /**
     * DOC Administrator Comment method "addListeners".
     */
    private void addListeners() {
        // TODO Auto-generated method stub
        moveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (moveButton.getText().equals(">>")) { //$NON-NLS-1$
                    sash.setWeights(new int[] { 23, 1 });
                    moveButton.setToolTipText(Messages.getString("ProcessComposite.showContext"));
                    moveButton.setText("<<");
                } else if (moveButton.getText().equals("<<")) { //$NON-NLS-1$
                    sash.setWeights(new int[] { 18, 5 });
                    moveButton.setToolTipText(Messages.getString("ProcessComposite.hideContext"));//$NON-NLS-1$
                    moveButton.setText(">>");
                }
            }
        });
    }

    protected Composite createTargetExecutionComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        StyledText text = new StyledText(composite, SWT.NONE);
        text.setText(Messages.getString("ProcessComposite.targetExecutionTabTooltipAvailable")); //$NON-NLS-1$
        text.setWordWrap(true);
        text.setEditable(false);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    public void createDynamicComposite(Composite parent, Element element, EComponentCategory category) {

        contextComposite.setVisible(true);
        moveButton.setVisible(true);

    	if (moveButton.getText().equals(">>")) { //$NON-NLS-1$
            sash.setWeights(new int[] { 18, 5 });
        } else if (moveButton.getText().equals("<<")) { //$NON-NLS-1$
            sash.setWeights(new int[] { 23, 1 });
        }
        if (category == EComponentCategory.BASICRUN) {
            processComposite = new ProcessComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
            dc = processComposite;
        } else if (category == EComponentCategory.DEBUGRUN) {
            debugViewHelpers.values().forEach(helper -> {
                helper.getDebugComposite(parent).setVisible(false);
            });
            debugTisProcessComposite = lastDebugType != null && debugViewHelpers.get(lastDebugType) != null
                    ? debugViewHelpers.get(lastDebugType).getDebugComposite(parent)
                    : debugViewHelpers.get(EDebugProcessType.DI).getDebugComposite(parent);
            debugTisProcessComposite.setVisible(true);
            // CSS
            CoreUIPlugin.setCSSClass(debugTisProcessComposite, TraceDebugProcessComposite.class.getSimpleName());
            dc = debugTisProcessComposite;
        } else if (category == EComponentCategory.ADVANCESETTING) {
            advanceComposite = new AdvanceSettingComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
            dc = advanceComposite;
        } else if (category == EComponentCategory.TARGET) {
            targetComposite = new TargetExecComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
            dc = targetComposite;
        } else if (category == EComponentCategory.MEMORYRUN) {
            memoryRunComposite = new MemoryRuntimeComposite(findProcessView(), parent, processContext, SWT.H_SCROLL
                    | SWT.V_SCROLL | SWT.NO_FOCUS);
            dc = memoryRunComposite;
            contextComposite.setVisible(false);
            moveButton.setVisible(false);
            sash.setWeights(new int[]{90,1});
        }
        if (EComponentCategory.MAPREDUCE_JOB_CONFIG_FOR_HADOOP.equals(category)
                || EComponentCategory.STORM_JOB_CONFIG.equals(category) || EComponentCategory.SPARK_JOB_CONFIG.equals(category)) {
            if (processContext != null) {
                dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                        (Element) processContext.getProcess(), true, Display.getCurrent().getSystemColor(
                                SWT.COLOR_WIDGET_BACKGROUND));
                // CSS
                CoreUIPlugin.setCSSClass(dc, dc.getClass().getSimpleName());
            } else {
                dc = null;
            }
            sash.setWeights(new int[] { 24, 0 });
        }
        refresh();
        if (dc != null) {
            dc.refresh();
        }
    }

    public IDebugViewHelper getDebugViewHelper() {

        return lastDebugType != null && debugViewHelpers.get(lastDebugType) != null
                ? debugViewHelpers.get(lastDebugType)
                : debugViewHelpers.get(EDebugProcessType.DI);
    }

    public ProcessComposite getProcessComposite() {
        return processComposite;
    }

    public void setElement() {
        EComponentCategory[] categories = getCategories();
        final List<TalendPropertyTabDescriptor> descriptors = new ArrayList<TalendPropertyTabDescriptor>();
        for (EComponentCategory category : categories) {
            TalendPropertyTabDescriptor d = new TalendPropertyTabDescriptor(category);
            // d.setData(data);
            descriptors.add(d);
        }
        tabFactory.setInput(descriptors);
        tabFactory.setSelection(new IStructuredSelection() {

            @Override
            public Object getFirstElement() {
                return null;
            }

            @Override
            public Iterator iterator() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public Object[] toArray() {
                return null;
            }

            @Override
            public List toList() {
                List<TalendPropertyTabDescriptor> d = new ArrayList<TalendPropertyTabDescriptor>();

                if (descriptors.size() > 0) {
                    if (currentSelectedTab != null) {
                        for (TalendPropertyTabDescriptor ds : descriptors) {
                            if (ds.getCategory() == currentSelectedTab.getCategory()) {
                                d.add(ds);
                                return d;
                            }
                        }
                    }
                    d.add(descriptors.get(0));
                }
                return d;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

        });
    }

    private EComponentCategory[] getCategories() {
        EComponentCategory[] categories = EElementType.RUN_PROCESS.getCategories();
        if (processContext != null && processContext.getProcess() != null) {
            if (processContext.getProcess().getComponentsType().equals(ComponentCategory.CATEGORY_4_MAPREDUCE.getName())) {
                categories = (EComponentCategory[]) ArrayUtils.remove(categories,
                        ArrayUtils.indexOf(categories, EComponentCategory.DEBUGRUN));
                categories = (EComponentCategory[]) ArrayUtils.add(categories, 1,
                        EComponentCategory.MAPREDUCE_JOB_CONFIG_FOR_HADOOP);
            }
            if (processContext.getProcess().getComponentsType().equals(ComponentCategory.CATEGORY_4_STORM.getName())) {
                categories = (EComponentCategory[]) ArrayUtils.add(categories, 1, EComponentCategory.STORM_JOB_CONFIG);
            }

            if (processContext.getProcess().getComponentsType().equals(ComponentCategory.CATEGORY_4_SPARK.getName())
                    || processContext.getProcess().getComponentsType()
                            .equals(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName())) {
                categories = (EComponentCategory[]) ArrayUtils.add(categories, 1, EComponentCategory.SPARK_JOB_CONFIG);
            }
        }
        return categories;
    }

    private void addListenerOnChildren(Control parent, FocusListener focusListener) {
        parent.addFocusListener(focusListener);
        if (parent instanceof Composite) {
            for (Control child : ((Composite) parent).getChildren()) {
                addListenerOnChildren(child, focusListener);
            }
        }
    }

    private IContextActivation ca;

    private void fillActionBars() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        clearPerfAction = new ClearPerformanceAction();
        menuManager.add(clearPerfAction);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (contextManagerListener != null) {
            RunProcessPlugin.getDefault().getRunProcessContextManager().removePropertyChangeListener(contextManagerListener);
            contextManagerListener = null;
        }
        // processComposite.dispose();
        // processComposite = null;
        ProxyRepositoryFactory.getInstance().removePropertyChangeListener(this);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.parent.setFocus();
        // selectTab(EComponentCategory.BASICRUN);
        // processComposite.setFocus();
        //
        // IContextService contextService = (IContextService) RunProcessPlugin.getDefault().getWorkbench()
        // .getAdapter(IContextService.class);
        // contextService.activateContext("talend.runProcess");
    }

    private void runningProcessChanged() {
        refresh();
        if(dc != null && dc instanceof MemoryRuntimeComposite) {
        	((MemoryRuntimeComposite)dc).refreshMonitorComposite();
        }
    }

    EDebugProcessType lastDebugType = null;

    public void refresh() {
        refresh(false);
    }

    public void refresh(boolean force) {
        if (force) {
            if (currentSelectedTab != null) {
                EComponentCategory currentCategory = currentSelectedTab.getCategory();
                selectTab(EComponentCategory.BASICRUN);
                selectTab(currentCategory);
            }
        }
        RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();
        boolean disableAll = false;
        if (activeContext != null) {
            disableAll = activeContext.getProcess().disableRunJobView();
        }
        processContext = activeContext;
        rubjobManager.setProcessContext(processContext);
        // if (processContext != null) {
        // activeContext.setSaveBeforeRun(rubjobManager.getSaveJobBeforeRun());
        // activeContext.setWatchAllowed(rubjobManager.getExecTime());
        // activeContext.setMonitorPerf(rubjobManager.getStat());
        // activeContext.setClearBeforeExec(rubjobManager.getClearBeforeExec());
        // }
        if (contextComposite.isDisposed()) {
            return;
        }
        
        if (activeContext != null
                && !EDebugProcessType.findDebugType(activeContext.getProcess().getComponentsType()).equals(lastDebugType)) {
            lastDebugType = EDebugProcessType.findDebugType(activeContext.getProcess().getComponentsType());
            setElement();
        }
        contextComposite.setProcess(((activeContext != null) && !disableAll ? activeContext.getProcess() : null));
        // clearPerfAction.setProcess(activeContext != null ? activeContext.getProcess() : null);
        rubjobManager.setSelectContext(contextComposite.getSelectedContext());
        if (activeContext != null) {
            activeContext.setSelectedContext(contextComposite.getSelectedContext());
        }
        if (dc != null && dc == processComposite) {
            processComposite.setProcessContext(activeContext);
        } else if (dc != null && dc instanceof TraceDebugProcessComposite) {
            EDebugProcessType type = lastDebugType == null ? EDebugProcessType.DI : lastDebugType;
            if (!debugViewHelpers.get(type).getDebugComposite(parent).isVisible()) {
                debugViewHelpers.values().forEach(helper -> {
                    if (!helper.getDebugType().equals(type)) {
                        helper.getDebugComposite(parent).setVisible(false);
                    }
                });
                debugViewHelpers.get(type).getDebugComposite(parent).setVisible(true);
            }
            if (debugTisProcessComposite != null) {
                debugTisProcessComposite.setProcessContext(activeContext);
                debugTisProcessComposite.setContextComposite(this.contextComposite);
            }

        } else if (dc != null && dc == advanceComposite) {
            advanceComposite.setProcessContext(activeContext);
        } else if (dc != null && dc == targetComposite) {
            targetComposite.setProcessContext(activeContext);
        } else if (dc != null && dc == memoryRunComposite) {
            memoryRunComposite.setProcessContext(activeContext);
        } else if (dc != null) {
            if (processContext != null && processContext.getProcess() != null) {
                if (dc instanceof MultipleThreadDynamicComposite) {
                    if (dc.getElement() != processContext.getProcess()) {
                        for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                            curControl.dispose();
                        }
                        dc = new MultipleThreadDynamicComposite(tabFactory.getTabComposite(), SWT.H_SCROLL | SWT.V_SCROLL
                                | SWT.NO_FOCUS, currentSelectedTab.getCategory(), (Element) processContext.getProcess(), true,
                                Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                    }
                    dc.refresh();
                }
            }
            if (processContext == null || processContext.getProcess() == null) {
                for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                    curControl.dispose();
                }
            }
        }
        if (activeContext != null) {
            String jobName = Messages.getString("ProcessView.jobName"); //$NON-NLS-1$
            if (activeContext.getProcess().disableRunJobView()) { // ?? joblet
                jobName = "Joblet"; //$NON-NLS-1$
            }
            jobName = jobName + " " + activeContext.getProcess().getLabel(); //$NON-NLS-1$
            setTitleToolTip(jobName);
            setPartName(Messages.getString("ProcessView.title", jobName)); //$NON-NLS-1$
            // processNameLab.setText(jobName);
            tabFactory.setTitle(jobName, null);
        } else {
            setPartName(Messages.getString("ProcessView.titleEmpty")); //$NON-NLS-1$
            //processNameLab.setText(Messages.getString("ProcessView.subtitleEmpty")); //$NON-NLS-1$
            tabFactory.setTitle(Messages.getString("ProcessView.subtitleEmpty"), null);
        }

        // processNameLab.getParent().layout(true, true);
    }

    /**
     * DOC smallet ProcessView class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    public class RunAction extends Action {

        /**
         * DOC smallet RunAction constructor comment.
         */
        public RunAction() {
            super();
            this.setActionDefinitionId("runProcess"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            if (!canRun) {
                return;
            }
            canRun = false;
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            try {
                page.showView("org.talend.designer.runprocess.ui.views.processview"); //$NON-NLS-1$

                selectTab(EComponentCategory.BASICRUN);
                if (processComposite != null && !processComposite.isDisposed()) {
                    if (processComposite.hasProcess() && !processComposite.getProcess().disableRunJobView()) {
                        processComposite.errorMessMap.clear();
                        processComposite.setCurRunMode(EXEC_ID);
                        processComposite.exec();
                    }
                }
            } catch (PartInitException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } finally {
                canRun = true;
            }
        }

    }

    /**
     * DOC smallet ProcessView class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class DebugAction extends Action {

        /**
         * DOC smallet RunAction constructor comment.
         */
        public DebugAction() {
            super();
            this.setActionDefinitionId("debugProcess"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            // IWorkbench workbench = PlatformUI.getWorkbench();
            // IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            // try {
            //                page.showView("org.talend.designer.runprocess.ui.views.processview"); //$NON-NLS-1$
            // } catch (PartInitException e) {
            // // TODO Auto-generated catch block
            // // e.printStackTrace();
            // ExceptionHandler.process(e);
            // }
            selectTab(EComponentCategory.DEBUGRUN);
            if (debugTisProcessComposite.hasProcess()) {
                debugTisProcessComposite.errorMessMap.clear();
                debugTisProcessComposite.debug();
            }
        }

    }

    /**
     * DOC smallet ProcessView class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class KillAction extends Action {

        /**
         * DOC smallet RunAction constructor comment.
         */
        public KillAction() {
            super();
            this.setActionDefinitionId("killProcess"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            selectTab(EComponentCategory.DEBUGRUN);
            if (debugTisProcessComposite.hasProcess()) {
                debugTisProcessComposite.kill();
            }
        }

    }

    /**
     * Getter for processViewHelper.
     *
     * @return the processViewHelper
     */
    public IProcessViewHelper getProcessViewHelper() {
        return this.processViewHelper;
    }

    /**
     * Sets the processViewHelper.
     *
     * @param processViewHelper the processViewHelper to set
     */
    public void setProcessViewHelper(IProcessViewHelper processViewHelper) {
        this.processViewHelper = processViewHelper;
    }

    public void selectTab(final EComponentCategory category) {
        if (tabFactory.getSelection().getCategory().equals(category)) {
            return;
        }

        List<TalendPropertyTabDescriptor> allTabs = tabFactory.getInput();
        final List<TalendPropertyTabDescriptor> selection = new ArrayList<TalendPropertyTabDescriptor>();
        for (TalendPropertyTabDescriptor talendPropertyTabDescriptor : allTabs) {
            if (talendPropertyTabDescriptor.getCategory().equals(category)) {
                dc = new ProcessComposite(tabFactory.getTabComposite(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
                // createBasicComposite(tabFactory.getTabComposite(), element, null);
                refresh();
                selection.add(talendPropertyTabDescriptor);
            }
        }
        tabFactory.setSelection(new StructuredSelection() {

            @Override
            public List toList() {
                return selection;
            }
        });
    }

    public ISelection getSelection() {
        return getSite().getSelectionProvider().getSelection();
    }

    public void setSelection(ISelection sel) {
        getSite().getSelectionProvider().setSelection(sel);
    }

	public ProcessContextComposite getContextComposite() {
		return contextComposite;
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("view_refresh")) { //$NON-NLS-1$
            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurProcessView();
        }
    }
}
