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
package org.talend.designer.runprocess.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ReplaceNodesInProcessProvider;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.runprocess.Processor;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionTrace;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IProcessMessage;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.JobErrorsChecker;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;
import org.talend.designer.runprocess.ProcessMessageManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.RunprocessConstants;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.prefs.RunProcessPrefsConstants;
import org.talend.designer.runprocess.prefs.RunProcessTokenCollector;
import org.talend.designer.runprocess.ui.actions.ClearPerformanceAction;
import org.talend.designer.runprocess.ui.actions.ClearTraceAction;
import org.talend.designer.runprocess.ui.views.ProcessView;
import org.talend.utils.dates.DateUtils;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DebugProcessTosComposite extends TraceDebugProcessComposite {

    private final ToolBar toolBar;

    private final ToolItem itemDropDown;

    private MenuItem debugMenuItem;

    private final Button killBtn;

    private final ProcessManager manager = ProcessManager.getInstance();

    private ProcessContextComposite contextComposite;

    @Override
    public void setContextComposite(ProcessContextComposite contextComposite) {
        this.contextComposite = contextComposite;
    }

    private final Button clearTracePerfBtn;

    private static RunProcessContext processContext;

    private final StyledText consoleText;

    public HashMap<String, IProcessMessage> errorMessMap = new HashMap<String, IProcessMessage>();

    private boolean isAddedStreamListener;

    private final IStreamListener streamListener;

    private static final int MINIMUM_HEIGHT = 65;

    private static final int MINIMUM_WIDTH = 530;

    // private static final Control breakpoint = null;

    private boolean hideConsoleLine = false;

    private Button enableLineLimitButton;

    private Text lineLimitText;

    private final Menu menu;

    private final PropertyChangeListener pcl;

    private Double extend = new Double(0);

    private boolean isRuning = false;

    /**
     * DOC Administrator DebugProcessComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public DebugProcessTosComposite(Composite parent, int style) {
        super(parent, style);
        setExpandHorizontal(true);
        setExpandVertical(true);
        this.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        FormData layouData = new FormData();
        layouData.left = new FormAttachment(0, 0);
        layouData.right = new FormAttachment(100, 0);
        layouData.top = new FormAttachment(0, 0);
        layouData.bottom = new FormAttachment(100, 0);
        setLayoutData(layouData);

        this.setLayout(new FormLayout());
        final Composite panel = new Composite(this, SWT.NONE);
        setContent(panel);
        // panel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
        FormLayout layout = new FormLayout();
        layout.marginWidth = 5 + 2;
        layout.marginHeight = 4;
        layout.spacing = 6 + 1;
        panel.setLayout(layout);

        Group execGroup = new Group(panel, SWT.NONE);
        execGroup.setText("Debug"); //$NON-NLS-1$
        GridLayout layout5 = new GridLayout();
        layout5.marginHeight = 0;
        layout5.marginWidth = 0;
        execGroup.setLayout(layout5);

        FormData layouDatag = new FormData();
        layouDatag.left = new FormAttachment(0, 0);
        layouDatag.right = new FormAttachment(100, 0);
        layouDatag.top = new FormAttachment(0, 0);
        layouDatag.bottom = new FormAttachment(100, 0);
        execGroup.setLayoutData(layouDatag);

        ScrolledComposite execScroll = new ScrolledComposite(execGroup, SWT.V_SCROLL | SWT.H_SCROLL);
        execScroll.setExpandHorizontal(true);
        execScroll.setExpandVertical(true);
        execScroll.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite execContent = new Composite(execScroll, SWT.NONE);
        execContent.setLayout(new FormLayout());
        execScroll.setContent(execContent);

        Composite execHeader = new Composite(execContent, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 7;
        formLayout.marginHeight = 4;
        formLayout.spacing = 7;
        execHeader.setLayout(formLayout);

        FormData layoutData = new FormData();
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.top = new FormAttachment(0, 0);
        layoutData.bottom = new FormAttachment(0, 50);
        execHeader.setLayoutData(layoutData);

        Composite toolBarComposite = new Composite(execHeader, SWT.NONE);
        toolBarComposite.setLayout(new FillLayout());

        toolBar = new ToolBar(toolBarComposite, SWT.FLAT | SWT.RIGHT);

        itemDropDown = new ToolItem(toolBar, SWT.ARROW);
        itemDropDown.setText(Messages.getString("ProcessComposite.traceDebug"));//$NON-NLS-1$
        itemDropDown.setData(ProcessView.TRACEDEBUG_ID);
        itemDropDown.setToolTipText(Messages.getString("ProcessComposite.traceDebug"));//$NON-NLS-1$
        itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_TRACE_ACTION));

        menu = new Menu(execHeader);
        itemDropDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.detail == SWT.ARROW) {
                    Rectangle bounds = itemDropDown.getBounds();
                    Point point = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
                    menu.setLocation(point);
                    menu.setVisible(true);
                } else {
                    ToolItem item = (ToolItem) event.widget;
                    errorMessMap.clear();
                    if (item.getData().equals(ProcessView.DEBUG_ID)) {
                        debug();
                    } else /* if (item.getData().equals(ProcessView.TRACEDEBUG_ID)) */{
                        execButtonPressed();
                    }
                }
            }
        });

        // debug
        final MenuItem menuItem1 = new MenuItem(menu, SWT.PUSH);
        menuItem1.setText(" " + Messages.getString("ProcessDebugDialog.javaDebug"));//$NON-NLS-1$//$NON-NLS-2$
        menuItem1.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_PROCESS_ACTION));
        menuItem1.setData(ProcessView.DEBUG_ID);
        menuItem1.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (!itemDropDown.getData().equals(ProcessView.PAUSE_ID) && !itemDropDown.getData().equals(ProcessView.RESUME_ID)) {
                    itemDropDown.setText(menuItem1.getText());
                    itemDropDown.setData(ProcessView.DEBUG_ID);
                    itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_PROCESS_ACTION));
                    itemDropDown.setToolTipText(Messages.getString("ProcessDebugDialog.javaDebug"));//$NON-NLS-1$
                    toolBar.getParent().getParent().layout();
                    manager.setBooleanTrace(false);
                    addTrace(ProcessView.DEBUG_ID);
                }
            }
        });
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (brandingService.getBrandingConfiguration().isAllowDebugMode()) {
            // trace
            debugMenuItem = new MenuItem(menu, SWT.PUSH);
            debugMenuItem.setText(" " + Messages.getString("ProcessComposite.traceDebug")); //$NON-NLS-1$//$NON-NLS-2$
            debugMenuItem.setData(ProcessView.TRACEDEBUG_ID);
            debugMenuItem.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_TRACE_ACTION));
            debugMenuItem.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent event) {
                    if (!itemDropDown.getData().equals(ProcessView.PAUSE_ID)
                            && !itemDropDown.getData().equals(ProcessView.RESUME_ID)) {
                        itemDropDown.setText(debugMenuItem.getText());
                        itemDropDown.setData(ProcessView.TRACEDEBUG_ID);
                        itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_TRACE_ACTION));
                        itemDropDown.setToolTipText(Messages.getString("ProcessComposite.traceDebug"));//$NON-NLS-1$
                        toolBar.getParent().getParent().layout();
                        manager.setBooleanTrace(true);
                        addTrace(ProcessView.TRACEDEBUG_ID);

                    }

                }
            });
        }
        toolBar.setEnabled(false);
        // see the feature 6366,qli comment.
        // make a judge when the text change in diffrent languages.
        // changed to use FillLayout to resolve this problem, see TDI-28943
        if (brandingService.getBrandingConfiguration().isAllowDebugMode()) {
            itemDropDown.setText(debugMenuItem.getText());
        } else {
            itemDropDown.setText(menuItem1.getText());
        }

        killBtn = new Button(execHeader, SWT.PUSH);
        killBtn.setText(Messages.getString("ProcessComposite.kill")); //$NON-NLS-1$
        killBtn.setToolTipText(Messages.getString("ProcessComposite.killHint")); //$NON-NLS-1$
        killBtn.setImage(ImageProvider.getImage(ERunprocessImages.KILL_PROCESS_ACTION));
        // setButtonLayoutData(killBtn);
        killBtn.setEnabled(false);
        FormData formDatap = new FormData();
        formDatap.left = new FormAttachment(toolBarComposite, 0, SWT.RIGHT);
        formDatap.width = 80;
        formDatap.top = new FormAttachment(toolBarComposite, 0, SWT.CENTER);
        // formDatap.bottom = new FormAttachment(0, 30);
        formDatap.height = 30;
        killBtn.setLayoutData(formDatap);
        clearTracePerfBtn = new Button(execHeader, SWT.PUSH);
        clearTracePerfBtn.setText(Messages.getString("ProcessComposite.clear")); //$NON-NLS-1$
        clearTracePerfBtn.setToolTipText(Messages.getString("ProcessComposite.clearHint")); //$NON-NLS-1$
        clearTracePerfBtn.setImage(ImageProvider.getImage(RunProcessPlugin.imageDescriptorFromPlugin(RunProcessPlugin.PLUGIN_ID,
                "icons/process_stat_clear.gif"))); //$NON-NLS-1$
        clearTracePerfBtn.setEnabled(false);
        formDatap = new FormData();
        formDatap.left = new FormAttachment(killBtn, 0, SWT.RIGHT);
        formDatap.width = 80;
        formDatap.top = new FormAttachment(toolBarComposite, 0, SWT.CENTER);
        // formDatap.bottom = new FormAttachment(0, 30);
        formDatap.height = 30;
        clearTracePerfBtn.setLayoutData(formDatap);
        consoleText = new StyledText(execContent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        FormData formDataco = new FormData();
        formDataco.top = new FormAttachment(0, 50);
        formDataco.left = new FormAttachment(0, 10);
        formDataco.right = new FormAttachment(100, 0);
        formDataco.bottom = new FormAttachment(100, -30);
        consoleText.setLayoutData(formDataco);
        // feature 6875, add searching capability, nma
        consoleText.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent evt) {
                // select all
                if ((evt.stateMask == SWT.CTRL) && (evt.keyCode == 'a')) {
                    if (consoleText.getText().length() > 0) {
                        consoleText.setSelection(0, (consoleText.getText().length() - 1));
                    }
                }
                // search special string value
                else if ((evt.stateMask == SWT.CTRL) && (evt.keyCode == 'f')) {
                    FindDialog td = new FindDialog(Display.getCurrent().getActiveShell());
                    td.setConsoleText(consoleText);
                    td.setBlockOnOpen(true);
                    td.open();

                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {

            }
        });
        if (!setConsoleFont()) {
            Font font = new Font(parent.getDisplay(), "courier", 8, SWT.NONE); //$NON-NLS-1$
            consoleText.setFont(font);
        }

        // sash.setSashWidth(1);
        // sash.setWeights(new int[] { 7, 1, H_WEIGHT });

        pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                runProcessContextChanged(evt);
            }
        };
        streamListener = new IStreamListener() {

            @Override
            public void streamAppended(String text, IStreamMonitor monitor) {
                IProcessMessage message = new ProcessMessage(ProcessMessage.MsgType.STD_OUT, text);
                processContext.addDebugResultToConsole(message);
            }
        };
        addListeners();
        createLineLimitedControl(execContent);
    }

    private void addTrace(int itemId) {
        Boolean trace = false;
        if (itemId == ProcessView.TRACEDEBUG_ID) {
            trace = true;
        }
        processContext.setMonitorTrace(trace);
        org.talend.core.model.process.IProcess process = processContext.getProcess();
        List<INode> nodeList = (List<INode>) process.getGraphicalNodes();
        for (INode node : nodeList) {
            for (Connection connection : (List<Connection>) node.getOutgoingConnections()) {
                ConnectionTrace traceNode = connection.getConnectionTrace();
                if (traceNode == null) {
                    continue;
                }
                traceNode.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), trace);
                if (connection != null && connection.checkTraceShowEnable()) {
                    connection.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), trace);
                }
            }
        }
    }

    private void createLineLimitedControl(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        FormData layouData = new FormData();
        layouData.left = new FormAttachment(0, 10);
        layouData.right = new FormAttachment(100, 0);
        layouData.top = new FormAttachment(100, -30);
        layouData.bottom = new FormAttachment(100, -3);
        composite.setLayoutData(layouData);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 7;
        formLayout.marginHeight = 4;
        formLayout.spacing = 7;
        composite.setLayout(formLayout);

        enableLineLimitButton = new Button(composite, SWT.CHECK);
        enableLineLimitButton.setText(Messages.getString("ProcessComposite.lineLimited")); //$NON-NLS-1$
        FormData formData = new FormData();
        enableLineLimitButton.setLayoutData(formData);
        enableLineLimitButton.setEnabled(false);
        enableLineLimitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                lineLimitText.setEditable(enableLineLimitButton.getSelection());
                RunProcessPlugin.getDefault().getPluginPreferences()
                        .setValue(RunprocessConstants.ENABLE_CONSOLE_LINE_LIMIT, enableLineLimitButton.getSelection());
            }
        });

        lineLimitText = new Text(composite, SWT.BORDER);
        formData = new FormData();
        formData.width = 120;
        formData.left = new FormAttachment(enableLineLimitButton, 0, SWT.RIGHT);
        lineLimitText.setLayoutData(formData);
        lineLimitText.setEnabled(false);
        lineLimitText.addListener(SWT.Verify, new Listener() {

            // this text only receive number here.
            @Override
            public void handleEvent(Event e) {
                String s = e.text;
                if (!s.equals("")) { //$NON-NLS-1$
                    try {
                        Integer.parseInt(s);
                        RunProcessPlugin.getDefault().getPluginPreferences()
                                .setValue(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT, lineLimitText.getText() + s);
                    } catch (Exception ex) {
                        e.doit = false;
                    }
                }
            }
        });
        lineLimitText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                RunProcessPlugin.getDefault().getPluginPreferences()
                        .setValue(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT, lineLimitText.getText());
            }
        });

        boolean enable = RunProcessPlugin.getDefault().getPluginPreferences()
                .getBoolean(RunprocessConstants.ENABLE_CONSOLE_LINE_LIMIT);
        enableLineLimitButton.setSelection(enable);
        lineLimitText.setEditable(enable);
        String count = RunProcessPlugin.getDefault().getPluginPreferences()
                .getString(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT);
        if (count.equals("")) { //$NON-NLS-1$
            count = "100"; //$NON-NLS-1$
        }
        lineLimitText.setText(count);
    }

    @Override
    protected void addListeners() {
        clearTracePerfBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (processContext == null) {
                    clearTracePerfBtn.setEnabled(false);
                    return;
                }
                if (processContext.isRunning()) {
                    if (consoleText != null && !consoleText.isDisposed()) {
                        processContext.clearMessages();
                    }
                } else {
                    ClearPerformanceAction clearPerfAction = new ClearPerformanceAction();
                    clearPerfAction.setProcess(processContext.getProcess());
                    clearPerfAction.run();
                    ClearTraceAction clearTraceAction = new ClearTraceAction();
                    clearTraceAction.setProcess(processContext.getProcess());
                    clearTraceAction.run();
                    consoleText.setText(""); //$NON-NLS-1$
                    processContext.clearMessages();
                }
            }
        });

        killBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                kill();
            }
        });
    }

    public void execButtonPressed() {
        if (processContext == null) {
            toolBar.setEnabled(false);
            return;
        }
        menu.setVisible(false);
        if (itemDropDown.getData().equals(ProcessView.PAUSE_ID)) {
            pause(ProcessView.PAUSE_ID);
        } else if (itemDropDown.getData().equals(ProcessView.RESUME_ID)) {
            pause(ProcessView.RESUME_ID);
        } else if (itemDropDown.getData().equals(ProcessView.TRACEDEBUG_ID)) {
            // bug 18852 fixed :
            if (isRuning) {
                itemDropDown.setData(ProcessView.PAUSE_ID);
            } else {
                itemDropDown.setData(ProcessView.TRACEDEBUG_ID);
                addInHistoryRunningList();
                isRuning = true;
            }
        }
    }

    public void pause(int id) {
        boolean isPause = id == ProcessView.PAUSE_ID;
        processContext.setTracPause(isPause);
        setExecBtn(isPause);
        if (isPause) {
            itemDropDown.setText(Messages.getString("ProcessComposite.textContent")); //$NON-NLS-1$
            itemDropDown.setToolTipText(Messages.getString("ProcessComposite.tipTextContent")); //$NON-NLS-1$
            itemDropDown.setData(ProcessView.RESUME_ID);
            itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.RUN_PROCESS_ACTION));

        } else {
            itemDropDown.setData(ProcessView.PAUSE_ID);
        }
    }

    protected void addInHistoryRunningList() {
        if (processContext == null) {
            return;
        }
        exec();

    }

    public void exec() {

        setHideconsoleLine(false);
        if (getProcessContext() == null) {
            return;
        }
        if (getProcessContext().getProcess() instanceof IProcess2) {
            ReplaceNodesInProcessProvider.beforeRunJobInGUI(getProcessContext().getProcess());
        }
        CorePlugin.getDefault().getRunProcessService().saveJobBeforeRun(getProcessContext().getProcess());
        if (manager.getClearBeforeExec()) {
            processContext.clearMessages();
        }
        if (manager.getExecTime()) {
            processContext.switchTime();
        }
        processContext.setWatchAllowed(manager.getExecTime());
        processContext.setMonitorPerf(manager.getStat());
        // processContext.setMonitorTrace(traceBtn.getSelection());
        processContext.setNextBreakPoint(false);
        processContext.setSelectedContext(manager.getSelectContext());
        processContext.exec(manager.getProcessShell());

        checkSaveBeforeRunSelection();
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunch[] launches = manager.getLaunches();
        manager.removeLaunches(launches);

        // trace debug to collect when tos
        IPreferenceStore preferenceStore = RunProcessPlugin.getDefault().getPreferenceStore();
        int num = preferenceStore.getInt(RunProcessTokenCollector.TOS_COUNT_DEBUG_RUNS.getPrefKey());
        preferenceStore.setValue(RunProcessTokenCollector.TOS_COUNT_DEBUG_RUNS.getPrefKey(), num + 1);
    }

    protected static RunProcessContext getProcessContext() {
        return processContext;
    }

    @Override
    public void setProcessContext(RunProcessContext processContext) {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$
        if (this.processContext != null) {
            this.processContext.removePropertyChangeListener(pcl);
        }
        this.processContext = processContext;
        if (processContext != null) {
            processContext.addPropertyChangeListener(pcl);
        }

        boolean disableAll = false;
        if (processContext != null) {
            disableAll = processContext.getProcess().disableRunJobView();

        }
        if (processContext != null) {
            // Fix problem: when running Java Debug, switching different jobs or tabs will have display problem
            boolean isJavaDebuging = false;
            org.eclipse.debug.core.model.IProcess debugProcess = processContext.getDebugProcess();
            if (debugProcess != null) {
                try {
                    debugProcess.getExitValue();
                } catch (DebugException e) {
                    isJavaDebuging = true;
                }
            }
            if (isJavaDebuging == false) {
                processContext.setMonitorTrace(true);
                addTrace(ProcessView.TRACEDEBUG_ID);
            }
        }

        setRunnable(processContext != null && !processContext.isRunning() && !disableAll);
        killBtn.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // previousRow.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // nextRow.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // nextBreakPoint.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // clearBeforeExec.setEnabled(processContext != null);
        // clearBeforeExec.setSelection(processContext != null && processContext.isClearBeforeExec());
        // contextComposite.setProcess(((processContext != null) && !disableAll ? processContext.getProcess() : null));
        fillConsole(processContext != null ? processContext.getMessages() : new ArrayList<IProcessMessage>());
        if (processContext == null) {
            manager.setBooleanTrace(false);
            itemDropDown.setText(" " + Messages.getString("ProcessDebugDialog.javaDebug"));//$NON-NLS-1$//$NON-NLS-2$
            itemDropDown.setData(ProcessView.DEBUG_ID);
            itemDropDown.setToolTipText(Messages.getString("ProcessDebugDialog.javaDebug"));//$NON-NLS-1$
            itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_PROCESS_ACTION));
        }
    }

    @Override
    protected void fillConsole(Collection<IProcessMessage> messages) {
        consoleText.setText(""); //$NON-NLS-1$

        for (IProcessMessage processMessage : messages) {
            doAppendToConsole(processMessage);
        }
        scrollToEnd();
    }

    private void doAppendToConsole(final IProcessMessage message) {
        if (consoleText.isDisposed()) {
            return;
        }
        // see feature 0004895: Font size of the output console are very small
        setConsoleFont();

        String[] rows = message.getContent().split("\n"); //$NON-NLS-1$
        int rowLimit = getConsoleRowLimit();
        String content = null;
        if (rowLimit != SWT.DEFAULT) {
            int currentRows = consoleText.getLineCount();
            // if (consoleText.getText().equals("")) {
            currentRows--;
            // }
            if (currentRows >= rowLimit) {
                return;
            } else if (currentRows + rows.length <= rowLimit) {
                content = message.getContent();
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rowLimit - currentRows; i++) {
                    sb.append(rows[i]).append("\n"); //$NON-NLS-1$
                }
                content = sb.toString();
            }
        }

        if (content == null) {
            content = message.getContent();
        }

        StyleRange style = new StyleRange();
        style.start = consoleText.getText().length();

        String[] contents = content.split("\n");
        for (String content2 : contents) {
            if (isPattern(content2) || isPatternFor(content2)) {
                consoleText.append(""); //$NON-NLS-1$
                content = ""; //$NON-NLS-1$
            } else {
                consoleText.append(content2);
                consoleText.append("\n");
            }
        }
        style.length = content.length();
        if (message.getType() == MsgType.CORE_OUT || message.getType() == MsgType.CORE_ERR) {
            style.fontStyle = SWT.ITALIC;
        }
        Color color;
        switch ((MsgType) message.getType()) {
        case CORE_OUT:
            color = getDisplay().getSystemColor(SWT.COLOR_BLUE);
            break;
        case CORE_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
            break;
        case STD_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_RED);
            break;
        case STD_OUT:
        default:
            color = getDisplay().getSystemColor(SWT.COLOR_BLACK);
            break;
        }
        style.foreground = color;

        // added by hyWang for bug 0007411
        if ((style.start + style.length) > consoleText.getCharCount()) {
            style.length = consoleText.getCharCount() - style.start;
        }

        consoleText.setStyleRange(style);
    }

    private int getConsoleRowLimit() {
        if (!enableLineLimitButton.isDisposed()) {
            if (enableLineLimitButton.getSelection()) {
                try {
                    return Integer.parseInt(lineLimitText.getText());
                } catch (Exception e) {
                }
            }
        }
        return SWT.DEFAULT;
    }

    private boolean isPattern(String content) {
        Pattern pattern = Pattern.compile("\\$\\s*\\d+(\\.\\d*)?%"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    private boolean isPatternFor(String content) {
        Pattern pattern = Pattern.compile("\\[\\s*\\d+(\\.\\d*)?%\\]"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    private void scrollToEnd() {
        if (consoleText.isDisposed()) {
            return;
        }
        consoleText.setCaretOffset(consoleText.getText().length());
        consoleText.showSelection();
    }

    protected static void setButtonLayoutData(final Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        final int widthHint = 80;
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
    }

    @Override
    protected void appendToConsole(final IProcessMessage message) {
        manager.getProcessShell().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {

                if (message.getType() == MsgType.CORE_OUT || message.getType() == MsgType.CORE_ERR) {
                    // always display job end msgs
                    callViewToDisplayMsg(message);
                } else {
                    if (!isHideConsoleLine()) {
                        callViewToDisplayMsg(message);
                    }
                }
            }

            /**
             * DOC yexiaowei Comment method "callViewToDisplayMsg".
             *
             * @param message
             */
            private void callViewToDisplayMsg(final IProcessMessage message) {
                doAppendToConsole(message);
                scrollToEnd();
            }
        });
    }

    private void runProcessContextChanged(final PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        Display dis = Display.getCurrent();
        if (dis == null) {
            dis = Display.getDefault();
        }
        if (ProcessMessageManager.PROP_MESSAGE_ADD.equals(propName)
                || ProcessMessageManager.PROP_DEBUG_MESSAGE_ADD.equals(propName)) {
            IProcessMessage psMess = (IProcessMessage) evt.getNewValue();

            if (errorMessMap.size() <= CorePlugin.getDefault().getPreferenceStore()
                    .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT)) {
                if (!(LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL))) {
                    getAllErrorMess(psMess);
                } else {
                    addPerlMark(psMess);
                }
            }
            appendToConsole(psMess);
        } else if (ProcessMessageManager.PROP_MESSAGE_CLEAR.equals(propName)) {
            dis.asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!consoleText.isDisposed()) {
                        consoleText.setText(""); //$NON-NLS-1$
                    }
                }
            });
        } else if (RunProcessContext.PROP_MONITOR.equals(propName)) {
            // perfBtn.setSelection(((Boolean) evt.getNewValue()).booleanValue());
        } else if (RunProcessContext.TRACE_MONITOR.equals(propName)) {
            // traceBtn.setSelection(((Boolean) evt.getNewValue()).booleanValue());
        } else if (RunProcessContext.PROP_RUNNING.equals(propName)) {
            dis.asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    boolean running = ((Boolean) evt.getNewValue()).booleanValue();
                    setRunnable(!running);
                    if (!killBtn.isDisposed() && killBtn != null) {
                        killBtn.setEnabled(running);
                    }
                    isRuning = false;
                    // previousRow.setEnabled(running);
                    // nextRow.setEnabled(running);
                    // nextBreakPoint.setEnabled(running);
                }
            });
        }
    }

    private Point computeSize(String text) {
        GC gc = new GC(toolBar.getDisplay());
        final Point p = gc.textExtent(text);
        gc.dispose();
        return p;
    }

    @Override
    public boolean isHideConsoleLine() {
        return this.hideConsoleLine;
    }

    @Override
    public void setHideconsoleLine(boolean infoview) {
        this.hideConsoleLine = infoview;
    }

    private void checkSaveBeforeRunSelection() {

    }

    @Override
    protected void setRunnable(boolean runnable) {
        if (clearTracePerfBtn != null && !clearTracePerfBtn.isDisposed()) {
            IProcess2 iProcess = null;
            boolean enableClearBtn = true;
            if (processContext != null && (iProcess = processContext.getProcess()) != null) {
                if (iProcess.disableRunJobView()) {
                    enableClearBtn = false;
                }
            } else {
                enableClearBtn = false;
            }
            clearTracePerfBtn.setEnabled(enableClearBtn);
        }
        // previousRow.setEnabled(runnable);
        // nextRow.setEnabled(runnable);
        // nextBreakPoint.setEnabled(runnable);
        setExecBtn(runnable);
        // contextComposite.setEnabled(runnable);
        // if (argumentsComposite != null) {
        // argumentsComposite.setEnabled(runnable);
        // }
        if (enableLineLimitButton != null && !enableLineLimitButton.isDisposed()) {
            enableLineLimitButton.setEnabled(runnable);
        }
        if (lineLimitText != null && !lineLimitText.isDisposed()) {
            lineLimitText.setEnabled(runnable);
        }

    }

    @Override
    protected void setExecBtn(final boolean runnable) {
        if (!isDisposed()) {
            if (itemDropDown.getData().equals(ProcessView.DEBUG_ID)) {
                toolBar.setEnabled(runnable);
            }
            if (processContext != null) {
                if (processContext.isMonitorTrace()) {
                    boolean b = processContext != null;
                    if (!runnable && b) {
                        boolean isJobRunning = processContext.isRunning();
                        boolean isTracePause = processContext.isTracPause();
                        if (isJobRunning && isTracePause == false) {
                            itemDropDown.setText(" " + Messages.getString("ProcessComposite.pause")); //$NON-NLS-1$//$NON-NLS-2$
                            itemDropDown.setToolTipText(Messages.getString("ProcessComposite.pauseJob")); //$NON-NLS-1$
                            itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.PAUSE_PROCESS_ACTION));
                            itemDropDown.setData(ProcessView.PAUSE_ID);
                        } else if (isJobRunning && isTracePause) {
                            itemDropDown.setText(" " + Messages.getString("ProcessComposite.textContent")); //$NON-NLS-1$//$NON-NLS-2$
                            itemDropDown.setToolTipText(Messages.getString("ProcessComposite.tipTextContent")); //$NON-NLS-1$
                            itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.RUN_PROCESS_ACTION));
                            itemDropDown.setData(ProcessView.RESUME_ID);
                        }
                    } else {
                        itemDropDown.setText(" " + Messages.getString("ProcessComposite.traceDebug"));
                        itemDropDown.setData(ProcessView.TRACEDEBUG_ID);
                        itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_TRACE_ACTION));
                        itemDropDown.setToolTipText(Messages.getString("ProcessComposite.traceDebug"));
                    }
                    toolBar.getParent().getParent().layout();
                    toolBar.setEnabled(b);
                }
                // bug 18852
                // else {
                //
                // toolBar.setEnabled(runnable);
                // itemDropDown.setText(" " + Messages.getString("ProcessComposite.traceDebug"));
                // itemDropDown.setData(ProcessView.TRACEDEBUG_ID);
                // itemDropDown.setImage(ImageProvider.getImage(ERunprocessImages.DEBUG_TRACE_ACTION));
                // itemDropDown.setToolTipText(Messages.getString("ProcessComposite.traceDebug"));
                // toolBar.getParent().layout();
                // }
            }
        }
    }

    private boolean setConsoleFont() {
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        String fontType = preferenceStore.getString(RunProcessPrefsConstants.CONSOLE_FONT);
        if (StringUtils.isNotEmpty(fontType)) {
            FontData fontData = new FontData(fontType);
            if (consoleText.getFont() != null) {
                FontData oldFont = consoleText.getFont().getFontData()[0];
                // font is same
                if (oldFont.equals(fontData)) {
                    return false;
                }
            }
            Font font = new Font(this.getDisplay(), fontData);
            consoleText.setFont(font);
            return true;

        }
        return false;
    }

    @Override
    public void kill() {
        killBtn.setEnabled(false);
        // previousRow.setEnabled(false);
        // nextRow.setEnabled(false);
        // nextBreakPoint.setEnabled(false);
        setHideconsoleLine(true);
        processContext.kill();
    }

    boolean debugMode = false;

    @Override
    public void debug() {
        if (manager.getClearBeforeExec()) {
            processContext.clearMessages();
        }
        setHideconsoleLine(false);
        if ((processContext.getProcess()) instanceof org.talend.designer.core.ui.editor.process.Process) {
            ((org.talend.designer.core.ui.editor.process.Process) processContext.getProcess()).checkDifferenceWithRepository();
        }

        // final IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
        final IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
        final boolean oldValueConsoleOnOut = preferenceStore.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT);
        final boolean oldValueConsoleOnErr = preferenceStore.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR);

        preferenceStore.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, false);

        preferenceStore.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, false);
        // java debug to collect when tos
        int num = RunProcessPlugin.getDefault().getPreferenceStore()
                .getInt(RunProcessTokenCollector.TOS_COUNT_DEBUG_RUNS.getPrefKey());
        RunProcessPlugin.getDefault().getPreferenceStore()
                .setValue(RunProcessTokenCollector.TOS_COUNT_DEBUG_RUNS.getPrefKey(), num + 1);

        checkSaveBeforeRunSelection();

        if (contextComposite.promptConfirmLauch()) {
            setRunnable(false);
            final IContext context = contextComposite.getSelectedContext();

            IRunnableWithProgress worker = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) {
                    IProcessor processor = ProcessorUtilities.getProcessor(processContext.getProcess(), processContext
                            .getProcess().getProperty(), context);
                    monitor.beginTask("Launching debugger", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    try {
                        // use this function to generate childrens also.
                        ProcessorUtilities.generateCode(processContext.getProcess(), context, false, false, true, monitor);

                        ILaunchConfiguration config = ((Processor) processor).getDebugConfiguration(
                                processContext.getStatisticsPort(), processContext.getTracesPort(), null);

                        // see feature 0004820: The run job doesn't verify if
                        // code is correct before launching
                        if (!JobErrorsChecker.hasErrors(DebugProcessTosComposite.this.getShell())) {

                            if (config != null) {
                                IPreferenceStore debugUiStore = DebugUITools.getPreferenceStore();
                                debugUiStore.setValue(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, Boolean.FALSE);
                                DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);

                            } else {
                                MessageDialog.openInformation(getShell(), Messages.getString("ProcessDebugDialog.debugBtn"), //$NON-NLS-1$
                                        Messages.getString("ProcessDebugDialog.errortext")); //$NON-NLS-1$
                            }
                        }
                    } catch (ProcessorException e) {
                        IStatus status = new Status(IStatus.ERROR, RunProcessPlugin.PLUGIN_ID, IStatus.OK,
                                "Debug launch failed.", e); //$NON-NLS-1$
                        RunProcessPlugin.getDefault().getLog().log(status);
                        MessageDialog.openError(getShell(), Messages.getString("ProcessDebugDialog.debugBtn"), ""); //$NON-NLS-1$ //$NON-NLS-2$
                    } finally {
                        monitor.done();
                    }
                }
            };

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            try {
                progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), worker, ResourcesPlugin.getWorkspace()
                        .getRoot());
            } catch (InvocationTargetException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }

        debugMode = true;
        try {
            Thread thread = new Thread() {

                @Override
                public void run() {
                    while (debugMode) {
                        final IProcess process = DebugUITools.getCurrentProcess();
                        if (process != null && process.isTerminated()) {
                            Display dis = Display.getCurrent();
                            if (dis == null) {
                                dis = Display.getDefault();
                            }
                            dis.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    setRunnable(true);
                                    if (!killBtn.isDisposed() && killBtn != null) {
                                        killBtn.setEnabled(false);
                                    }
                                    preferenceStore.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, oldValueConsoleOnOut);

                                    preferenceStore.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, oldValueConsoleOnErr);

                                    if (isAddedStreamListener) {
                                        process.getStreamsProxy().getOutputStreamMonitor().removeListener(streamListener);
                                        isAddedStreamListener = false;

                                        if (processContext.isRunning()) {
                                            final String endingPattern = Messages.getString("ProcessComposite.endJobPattern"); //$NON-NLS-1$
                                            MessageFormat mf = new MessageFormat(endingPattern);
                                            try {
                                                String byeMsg = "\n" //$NON-NLS-1$
                                                		+ mf.format(new Object[] { processContext.getProcess().getName(), 
                                                		        DateUtils.getCurrentDate("HH:mm dd/MM/yyyy")});
                                                
                                                final String endExitPattern = Messages.getString("ProcessComposite.endExitCode"); //$NON-NLS-1$
                                                MessageFormat ef = new MessageFormat(endExitPattern);
                                                String endMsg = ef.format(new Object[] { " = " + new Integer(process.getExitValue()) }); //$NON-NLS-1$
                                                byeMsg = byeMsg + " [" + endMsg + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                                                
                                                processContext.addDebugResultToConsole(new ProcessMessage(MsgType.CORE_OUT,
                                                        byeMsg));
                                            } catch (DebugException e) {
                                                // e.printStackTrace();
                                                ExceptionHandler.process(e);
                                            }
                                            processContext.setRunning(false);
                                        }
                                    }
                                    debugMode = false;
                                }
                            });
                        } else {
                            if (process != null) { // (one at leat) process
                                // still running
                                Display dis = Display.getCurrent();
                                if (dis == null) {
                                    dis = Display.getDefault();
                                }
                                dis.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        setRunnable(false);
                                        if (!killBtn.isDisposed() && killBtn != null) {
                                            killBtn.setEnabled(true);
                                        }
                                        processContext.setRunning(true);
                                        processContext.setDebugProcess(process);
                                        if (!isAddedStreamListener) {
                                            process.getStreamsProxy().getOutputStreamMonitor().addListener(streamListener);
                                            // if (clearBeforeExec.getSelection()) {
                                            // processContext.clearMessages();
                                            // }
                                            // if (watchBtn.getSelection()) {
                                            // processContext.switchTime();
                                            // }

                                            ClearPerformanceAction clearPerfAction = new ClearPerformanceAction();
                                            clearPerfAction.setProcess(processContext.getProcess());
                                            clearPerfAction.run();

                                            ClearTraceAction clearTraceAction = new ClearTraceAction();
                                            clearTraceAction.setProcess(processContext.getProcess());
                                            clearTraceAction.run();
                                            isAddedStreamListener = true;

                                            final String startingPattern = Messages.getString("ProcessComposite.startJobPattern"); //$NON-NLS-1$
                                            MessageFormat mf = new MessageFormat(startingPattern);
                                            
                                            String welcomeMsg = mf.format(new Object[] { processContext.getProcess().getName(), 
                                                    DateUtils.getCurrentDate("HH:mm dd/MM/yyyy")});
                                            
                                            processContext.addDebugResultToConsole(new ProcessMessage(MsgType.CORE_OUT,
                                                    welcomeMsg + "\r\n"));//$NON-NLS-1$
                                        }
                                    }
                                });
                            } else { // no process running
                                Display dis = Display.getCurrent();
                                if (dis == null) {
                                    dis = Display.getDefault();
                                }
                                dis.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        setRunnable(true);
                                        if (!killBtn.isDisposed() && killBtn != null) {
                                            killBtn.setEnabled(false);
                                        }
                                    }
                                });
                            }
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // e.printStackTrace();
                            ExceptionHandler.process(e);
                        }
                    }
                }
            };
            thread.start();
        } catch (Exception e) {
            ExceptionHandler.process(e);
            processContext.addErrorMessage(e);
            kill();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getCurRowSize()
     */
    @Override
    public int getCurRowSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getElement()
     */
    @Override
    public Element getElement() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getHashCurControls()
     */
    @Override
    public BidiMap getHashCurControls() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getPart()
     */
    @Override
    public IMultiPageTalendEditor getPart() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.properties.tab.IDynamicProperty#getRepositoryAliasName(org.talend.core.model.properties.
     * ConnectionItem)
     */
    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* 16969 */
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryConnectionItemMap()
    // */
    // public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryQueryStoreMap()
    // */
    // public Map<String, Query> getRepositoryQueryStoreMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryTableMap()
     */
    // public Map<String, IMetadataTable> getRepositoryTableMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getSection()
     */
    @Override
    public EComponentCategory getSection() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
                getParent().layout();
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#setCurRowSize(int)
     */
    @Override
    public void setCurRowSize(int i) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getAllErrorMess(IProcessMessage psMess) {
        if (psMess.getType().equals(MsgType.STD_ERR)) {
            String mess = psMess.getContent();
            String[] linesMess = mess.split("\n");//$NON-NLS-1$
            StringBuffer currentMess = new StringBuffer();
            String currenctJobName = processContext.getProcess().getName();
            for (int i = 0; i < linesMess.length; i++) {
                String tRunJobName = currenctJobName;
                String linemess = linesMess[i].trim();
                Pattern pattern = Pattern.compile("^Exception\\s*in\\s*component\\s*(\\w)+_\\d$");//$NON-NLS-1$
                Matcher m = pattern.matcher(linemess);
                if (m.find()) {
                    List<Node> runjobList = getTRunjobList(processContext.getProcess());
                    String[] allwords = linemess.split("\\s"); //$NON-NLS-1$
                    String componentName = allwords[allwords.length - 1];
                    if (runjobList.size() > 0) {
                        int currentI = i;
                        if (currentI + 1 < linesMess.length - 1) {
                            // do {
                            // tRunJobName = linesMess[currentI++];
                            // } while ((tRunJobName.lastIndexOf("(") == -1 || tRunJobName.lastIndexOf(".java") == -1)
                            // && currentI < linesMess.length - 1);
                            for (int j = currentI + 1; j < linesMess.length - 1; j++) {
                                tRunJobName = linesMess[j];
                                if ((tRunJobName.contains(componentName))) {
                                    break;
                                }
                            }
                            if (tRunJobName.lastIndexOf("(") != -1 && tRunJobName.lastIndexOf(".java") != -1) {
                                tRunJobName = tRunJobName.substring(tRunJobName.lastIndexOf("(") + 1,
                                        tRunJobName.lastIndexOf(".java"));
                            } else {
                                tRunJobName = currenctJobName;
                            }
                        }
                    }

                    if (tRunJobName != null && tRunJobName.equals(currenctJobName)) {
                        if (i == 0) {
                            errorMessMap.put(componentName, psMess);
                        } else {
                            for (int j = i; j < linesMess.length; j++) {
                                currentMess.append(linesMess[j] + "\n"); //$NON-NLS-1$
                            }
                            IProcessMessage currentProMess = new ProcessMessage(MsgType.STD_ERR, currentMess.toString());
                            errorMessMap.put(componentName, currentProMess);
                        }
                    }
                    // break;
                }

            }
        }
        refreshNode(psMess);
    }

    @Override
    public void refreshNode(final IProcessMessage psMess) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (processContext == null) {
                    return;
                }
                org.talend.core.model.process.IProcess process = processContext.getProcess();
                if (process == null) {
                    return;
                }
                List<INode> nodeList = (List<INode>) process.getGraphicalNodes();
                for (INode inode : nodeList) {
                    if (!inode.isActivate()) {
                        continue;
                    }
                    String nodeUniqueName = inode.getUniqueName();
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL) && Problems.nodeList.size() > 0) {
                        errorMessMap.clear();
                    }
                    if (errorMessMap.get(nodeUniqueName) != null) {
                        if (inode instanceof Node) {
                            IProcessMessage messPro = errorMessMap.get(nodeUniqueName);
                            Node node = (Node) inode;
                            node.setErrorFlag(true);
                            node.setCompareFlag(false);
                            node.setErrorInfo(messPro.getContent());
                            node.getNodeError().updateState("UPDATE_STATUS", true); //$NON-NLS-1$
                            if (node.isFileScaleComponent()) {
                                refreshProgress(psMess, node, nodeUniqueName);
                            }
                            node.setErrorInfoChange("ERRORINFO", true); //$NON-NLS-1$
                        }
                    } else {
                        if (inode instanceof Node) {
                            Node node = (Node) inode;
                            if (Problems.nodeList.size() > 0) {
                                String befor = "Error in the component's properties:";
                                Iterator<Entry<Node, StringBuffer>> set = Problems.nodeList.entrySet().iterator();
                                while (set.hasNext()) {
                                    Entry<Node, StringBuffer> en = set.next();
                                    Node no = en.getKey();
                                    String des = en.getValue().toString();
                                    if (node == no) {
                                        node.setErrorFlag(true);
                                        node.setCompareFlag(false);
                                        node.setErrorInfo(befor + des);
                                        node.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                                        node.setErrorInfoChange("ERRORINFO", true);//$NON-NLS-1$
                                    }
                                }
                            } else {
                                if (node.isErrorFlag() == true) {
                                    node.setErrorFlag(false);
                                    node.setCompareFlag(false);
                                    node.setErrorInfo(null);
                                    node.getNodeError().updateState("UPDATE_STATUS", false); //$NON-NLS-1$
                                }

                                if (node.isFileScaleComponent()) {
                                    refreshProgress(psMess, node, nodeUniqueName);
                                }
                                if (node.isErrorFlag() == true) {
                                    node.setErrorInfoChange("ERRORINFO", false); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }

        });
    }

    @Override
    public void refreshProgress(IProcessMessage psMess, Node node, String nodeUniqueName) {
        String mess = ""; //$NON-NLS-1$
        String uniqueName = ""; //$NON-NLS-1$
        String[] message = psMess.getContent().split("\n");

        for (String element : message) {
            if (isPattern(element)) {

                int firIndex = element.indexOf("$"); //$NON-NLS-1$
                int secIndex = element.indexOf("%"); //$NON-NLS-1$
                uniqueName = element.substring(0, firIndex);
                mess = element.substring(firIndex + 1, secIndex);
            }

            Double extentPro = new Double(0);
            if ((!"".equals(mess)) && mess != null) { //$NON-NLS-1$
                extentPro = Double.parseDouble(mess);
            }

            if (((extend != extentPro) && nodeUniqueName.equals(uniqueName))) {
                node.getNodeProgressBar().updateState("UPDATE_STATUS", extentPro); //$NON-NLS-1$
                extend = extentPro;
            }

        }

    }

    private List<Node> getTRunjobList(org.talend.core.model.process.IProcess process) {
        List<Node> trunjobList = new ArrayList<Node>();
        if (!(process instanceof Process)) {
            return trunjobList;
        }
        List<INode> nodeList = (List<INode>) ((Process) process).getGraphicalNodes();
        for (INode node : nodeList) {
            if (node.getComponent().getName().equals("tRunJob")) {
                if (node instanceof Node) {
                    trunjobList.add((Node) node);
                }
            }
        }
        return trunjobList;
    }

    @Override
    protected void addPerlMark(IProcessMessage psMess) {
        if (psMess.getType().equals(MsgType.STD_ERR)) {
            String content = psMess.getContent();
            String path = null;
            String uniName = null;
            int lineNo = -1;
            Pattern errorPattern = Pattern.compile("(.*) at (\\S+) line (\\d+)[\\.,]");//$NON-NLS-1$
            Matcher m = errorPattern.matcher(content);
            String matchContent = null;

            while (m.find()) {
                path = m.group(2);
                lineNo = parseInt(m.group(3));

                matchContent = m.group();

                if ((!("".equals(path)) && path != null) && lineNo > 0) {//$NON-NLS-1$
                    uniName = Problems.setErrorMark(path, lineNo);
                }

                if (uniName != null) {
                    if (!errorMessMap.containsKey(uniName)) {
                        errorMessMap.put(uniName, new ProcessMessage(MsgType.STD_ERR, matchContent));
                    } else {
                        String uniMess = errorMessMap.get(uniName).getContent();
                        errorMessMap.put(uniName, new ProcessMessage(MsgType.STD_ERR, uniMess.concat(matchContent)));
                    }
                }

            }
        }
        refreshNode(psMess);
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public boolean hasProcess() {
        // TODO Auto-generated method stub
        return processContext != null;
    }
}
