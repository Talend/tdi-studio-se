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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.BidiMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ITargetExecutionConfig;
import org.talend.core.model.process.ReplaceNodesInProcessProvider;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionTrace;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.actions.SaveJobBeforeRunAction;
import org.talend.designer.runprocess.ui.views.ProcessView;
import org.talend.designer.runprocess.ui.views.ProcessViewSelectionProvider;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.IHost;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.JvmModel;
import org.talend.designer.runtime.visualization.ReportMessageManager;
import org.talend.designer.runtime.visualization.views.AbstractRuntimeGraphcsComposite;
import org.talend.designer.runtime.visualization.views.RuntimeGraphcsComposite;

import routines.TalendDate;

/**
 * created by ldong on Apr 3, 2015 Detailled comment
 *
 */
public class MemoryRuntimeComposite extends ScrolledComposite implements IDynamicProperty {

    private Button runtimeButton;

    private Combo periodCombo;

    private ComboViewer contextCombo;

    private Button gcCheckButton;

    private RunProcessContext processContext;

    private ProcessView viewPart;

    private IActiveJvm currentJvm;

    private Composite monitorComposite;

    private AbstractRuntimeGraphcsComposite chartComposite;

    private PropertyChangeListener propertyChangeListener;

    private ProcessManager processManager;

    private  ReportMessageManager messageManager;

    private static boolean isGCSelected = false;

    private static int periodComboSelectionIndex = 0;

    private static Timer timer = new Timer();

    private static boolean lock = false;

    private boolean isRemoteRun = false;

    private boolean isCommandlineRun = false;

    private boolean isRemoteMonitoring = false;

	private boolean isReadyToStart = false;

    public MemoryRuntimeComposite(ProcessView viewPart, Composite parent, RunProcessContext processContext, int style) {
        super(parent, style);
        this.setMinSize(new Point(500, 450));
        this.viewPart = viewPart;
        this.processContext = processContext;
        if (this.processContext != null) {
            getRemoteStatus();
        }
        this.processManager = ProcessManager.getInstance();
        this.messageManager = ReportMessageManager.getInstance();
        init(parent);
        // CSS
        CoreUIPlugin.setCSSClass(this, this.getClass().getSimpleName());
    }

	private void getRemoteStatus() {
		ITargetExecutionConfig config = processContext.getSelectedTargetExecutionConfig();
        if (config != null) {
        	isRemoteRun = config.isRemote();
            isCommandlineRun = false;
        	this.isRemoteMonitoring = config.isUseJMX();
        }
	}

    private void init(final Composite parent) {
        initGraphicComponents(parent);
//        addListeners();
    }

    /**
     * DOC ldong Comment method "initGraphicComponents".
     *
     * @param parent
     */
    private void initGraphicComponents(Composite parent) {
        initRuntimeGraphs(parent);
        addListeners();
        connectToJvm();
    }

    private void initRuntimeGraphs(final Composite parent) {
        ISelection processContextSelection = viewPart.getSite().getSelectionProvider() == null ? null : viewPart.getSelection();
        setExpandHorizontal(true);
        setExpandVertical(true);
        FormData layouData = new FormData();
        layouData.left = new FormAttachment(0, 0);
        layouData.right = new FormAttachment(100, 0);
        layouData.top = new FormAttachment(0, 0);
        layouData.bottom = new FormAttachment(100, 0);
        this.setBackground(parent.getBackground());
        this.setLayout(new FormLayout());
        setLayoutData(layouData);

        monitorComposite = new Composite(this, SWT.NULL);
        FormData baseData = new FormData();
        baseData.left = new FormAttachment(0, 0);
        baseData.right = new FormAttachment(100, 0);
        baseData.top = new FormAttachment(0, 0);
        baseData.bottom = new FormAttachment(100, 0);
        monitorComposite.setLayout(new FormLayout());
        monitorComposite.setLayoutData(layouData);
        setContent(monitorComposite);

        Group topGroup = createTopGroup(monitorComposite);
        runtimeButton = new Button(topGroup, SWT.PUSH);
        if(processContext!=null){
        	setRuntimeButtonByStatus(!processContext.isMonitoring());
        }else{
        	setRuntimeButtonByStatus(true);
        }
        runtimeButton.setEnabled(true);
        GridData runButtonData = new GridData();
        Point execSize = null;
        execSize = computeSize(runtimeButton.getText());
        runButtonData.widthHint = execSize.x + 70;
        runtimeButton.setLayoutData(runButtonData);

        gcCheckButton = new Button(topGroup,SWT.CHECK);
        GridData gcCheckButtonData = new GridData();
        gcCheckButtonData.grabExcessHorizontalSpace = false;
        gcCheckButton.setLayoutData(gcCheckButtonData);
        gcCheckButton.setEnabled(true);
        gcCheckButton.setSelection(isGCSelected);

        Label periodLabel = new Label(topGroup, SWT.NULL);
        periodLabel.setText(Messages.getString("ProcessView.moniorPeriod")); //$NON-NLS-1$
        periodLabel.setBackground(getBackground());
        GridData periodLabelData = new GridData();
        execSize = computeSize(periodLabel.getText());
        periodLabelData.widthHint = execSize.x;
        periodLabelData.grabExcessHorizontalSpace = false;
        periodLabelData.horizontalAlignment = GridData.BEGINNING;
        periodLabel.setLayoutData(periodLabelData);

        periodCombo = new Combo(topGroup, SWT.READ_ONLY);
        GridData periodData = new GridData();
        execSize = computeSize("Select");
        periodData.widthHint = execSize.x;
        periodData.grabExcessHorizontalSpace = false;
        periodData.horizontalAlignment = GridData.BEGINNING;
        periodData.minimumWidth = execSize.x;
        periodCombo.setLayoutData(periodData);
		periodCombo.setItems(new String[] {
				"Select", "30 sec", "60 sec", "120 sec" });	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        periodCombo.setEnabled(isGCSelected);
        if(isGCSelected){
        	periodCombo.select(periodComboSelectionIndex);
        }else{
        	periodCombo.select(0);
        	periodComboSelectionIndex = 0;
        }

        contextCombo = new ComboViewer(topGroup, SWT.BORDER | SWT.READ_ONLY);
        contextCombo.getCombo().setLayout(new FormLayout());
        GridData contextComboData = new GridData();
        contextComboData.grabExcessHorizontalSpace = true;
        contextComboData.horizontalAlignment = GridData.END;
        execSize = computeSize("Default");
        contextComboData.minimumWidth = execSize.x;
        contextCombo.getCombo().setLayoutData(contextComboData);
        contextCombo.setContentProvider(ArrayContentProvider.getInstance());
        contextCombo.setLabelProvider(new ContextNameLabelProvider());
        initContextInput();

        chartComposite = new RuntimeGraphcsComposite(monitorComposite, processContextSelection, SWT.NULL);
        FormLayout rgcLayout = new FormLayout();
        FormData charLayData = new FormData();
        charLayData.left = new FormAttachment(0, 10);
        charLayData.right = new FormAttachment(100, 0);
        charLayData.top = new FormAttachment(topGroup, 60, SWT.BOTTOM);
        charLayData.bottom = new FormAttachment(100, 0);
        chartComposite.setLayout(rgcLayout);
        chartComposite.setLayoutData(charLayData);
    }

	@SuppressWarnings("unchecked")
	private void initContextInput() {
		List<IContext> contextList = (List<IContext>) viewPart.getContextComposite().getContextComboInput();
        if(contextList!=null){
        	contextCombo.setSelection(new StructuredSelection(contextList.get(0)));
        	contextCombo.setInput(contextList);
        	ComboViewer processContextComboViewer = viewPart.getContextComposite().getContextComboViewer();
        	IContext selectedContext = (IContext) ((IStructuredSelection) processContextComboViewer.getSelection()).getFirstElement();
        	for(int i=0;i<contextList.size();i++){
        		if(contextList.get(i).getName().equals(selectedContext.getName())){
        			contextCombo.getCombo().select(i);
        		}
        	}
        	contextCombo.addSelectionChangedListener(new ISelectionChangedListener() {
        		@Override
        		public void selectionChanged(SelectionChangedEvent event) {
        			viewPart.getContextComposite().runSelectionChange(event);
        		}
        	});
        }else{
        	contextCombo.getCombo().setEnabled(false);
        }
	}

    private void connectToJvm() {
        if (currentJvm != null && !currentJvm.isConnected() && isEnabled()) {
            try {
                currentJvm.connect(1000);
            } catch (JvmCoreException e) {
                Activator.log(NLS.bind("Jvm failed", currentJvm.getPid()), e);	//$NON-NLS-1$
            }
        }
    }

    private Group createTopGroup(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NULL);
        FormLayout topLayout = new FormLayout();
        FormData topData = new FormData();
        topData.left = new FormAttachment(0, 10);
        topData.right = new FormAttachment(100, 0);
        topData.top = new FormAttachment(0, 0);
        topComposite.setLayout(topLayout);
        topComposite.setLayoutData(topData);

        Group topGroup = new Group(topComposite, SWT.NULL);
        topGroup.setText("Monitor Control"); //$NON-NLS-1$
        GridLayout groupLayout = new GridLayout(5,false);
        FormData groupData = new FormData();
        groupData.left = new FormAttachment(0, 0);
        groupData.right = new FormAttachment(100, 0);
        groupData.top = new FormAttachment(0, 0);
        groupData.bottom = new FormAttachment(100, 0);
        topGroup.setLayout(groupLayout);
        topGroup.setLayoutData(groupData);

        return topGroup;
    }

    private Point computeSize(String text) {
        GC gc = new GC(runtimeButton.getDisplay());
        final Point p = gc.textExtent(text);
        gc.dispose();
        return p;
    }

    private void addListeners() {
        runtimeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                getRemoteStatus();

                boolean isRunButtonPressed = runtimeButton.getText().equals(Messages.getString("ProcessComposite.exec"));
                boolean isKillButtonPressed = runtimeButton.getText().equals(Messages.getString("ProcessComposite.kill"));

                if (lock && isRunButtonPressed) { // $NON-NLS-1$
                    MessageDialog.openWarning(getShell(), "Warning", Messages.getString("ProcessView.anotherJobMonitoring")); //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                if (isCommandlineRun) {
                    MessageDialog.openWarning(getShell(), "Warning", Messages.getString("ProcessView.commandlineForbidden"));   //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }

                if (isRemoteRun && !isRemoteMonitoring) {
                    MessageDialog.openWarning(getShell(), "Warning", Messages.getString("ProcessView.remoteMonitoringUnavailable"));    //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                if (processContext != null && !processContext.isRunning() && isRunButtonPressed) { // $NON-NLS-1$
                    runtimeButton.setEnabled(false);
                    isReadyToStart = true;
                    exec();
                }
                if (processContext != null && (processContext.isRunning() || isReadyToStart)) {
                    if (isRunButtonPressed) {
                        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                        try {
                            progressService.run(false, true, new IRunnableWithProgress() {

                                @Override
                                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                    final IProgressMonitor progressMonitor = new EventLoopProgressMonitor(monitor);
                                    progressMonitor.beginTask(Messages.getString("Processor.memoryRun.searchJvmInfo"), 1);
                                    if (!acquireJVM()) {
                                        isReadyToStart = false;
                                        runtimeButton.setEnabled(true);
                                        if (isRemoteRun) {
                                            MessageDialog.openWarning(getShell(), "Warning", //$NON-NLS-1$
                                                    Messages.getString("ProcessView.connectToMonitorServerFailed")); //$NON-NLS-1$
                                        } else {
                                            MessageDialog.openWarning(getShell(), "Warning", //$NON-NLS-1$
                                                    Messages.getString("ProcessView.noJobRunning")); //$NON-NLS-1$
                                        }
                                        return;
                                    }
                                    progressMonitor.subTask(Messages.getString("Processor.memoryRun.jvmInfo"));
                                    processContext.setTracPause(false);
                                    processContext.setMemoryRunning(false);

                                    initMonitoringModel();
                                    refreshMonitorComposite();
                                    processContext.setMonitoring(true);
                                    AbstractRuntimeGraphcsComposite.setMonitoring(true);
                                    setRuntimeButtonByStatus(false);
                                    isReadyToStart = false;

                                    if (periodCombo.isEnabled() && periodCombo.getSelectionIndex() != 0) {
                                        startCustomerGCSchedule();
                                    }
                                    String content = getExecutionInfo("Start"); //$NON-NLS-1$
                                    messageManager.setStartMessage(content, getDisplay().getSystemColor(SWT.COLOR_BLUE),
                                            getDisplay().getSystemColor(SWT.COLOR_WHITE));
                                    ((RuntimeGraphcsComposite) chartComposite).displayReportField();
                                    lock = true;
                                    Thread.sleep(1000);
                                    progressMonitor.done();
                                }

                            });
                        } catch (InvocationTargetException | InterruptedException e) {
                            ExceptionHandler.process(e);
                        }
                    } else if (isKillButtonPressed) { // $NON-NLS-1$
                        processContext.kill();
                    }
                } else {
                    MessageDialog.openWarning(getShell(), "Warning", Messages.getString("ProcessView.noJobRunning")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                runtimeButton.setEnabled(true);
            }
        });

        gcCheckButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				isGCSelected = gcCheckButton.getSelection();
				periodCombo.setEnabled(isGCSelected);
				if(!isGCSelected){
					periodCombo.select(0);
					if(processContext!=null && processContext.isRunning()){
						// cancel GC timer task during job running.
						doScheduledGc(0);
					}
				}
			}
		});

        periodCombo.addSelectionListener(new SelectionAdapter() {

        	@Override
			public void widgetSelected(SelectionEvent e) {
        		startCustomerGCSchedule();
			}

		});

        propertyChangeListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                runProcessContextChanged(evt);
            }
        };

    }

    private void startCustomerGCSchedule() {
    	periodComboSelectionIndex =  periodCombo.getSelectionIndex();
    	if(processContext != null && processContext.isRunning()){
    		String item = periodCombo.getItem(periodComboSelectionIndex);
    		if(periodComboSelectionIndex != 0){
    			int interval = Integer.valueOf(item.split(" ")[0]); //$NON-NLS-1$
    			System.out.println("set new interval: " + interval); //$NON-NLS-1$
    			doScheduledGc(interval);
    		}
    	}
	}

	protected void doScheduledGc(int interval) {
		timer.cancel();
		if(interval == 0){
			return;
		}
		timer = null;
		timer = new Timer();
		TimerTask gcTask = new TimerTask() {
			@Override
			public void run() {
				if(processContext!= null && processContext.isRunning()){
					if(currentJvm != null){
						try {
							currentJvm.getMBeanServer().runGarbageCollector();
							System.out.println("GC executed at " + TalendDate.getDate("HH:mm:ss")); //$NON-NLS-1$ //$NON-NLS-2$
						} catch (JvmCoreException e) {
							//do nothing.
						}
					}
				}else{
					timer.cancel();
					System.out.println("timer cancelled at " + TalendDate.getDate("HH:mm:ss")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		};
		timer.schedule(gcTask, interval*1000, interval*1000);
	}

	private boolean acquireJVM() {
		long startTime = System.currentTimeMillis();
		long endTime;
        String remoteHost = null;
        int remotePort = -1;
        ITargetExecutionConfig targExecConfig = processContext.getSelectedTargetExecutionConfig();
        // normally this value is always null in TOS
        if (targExecConfig != null) {
            remoteHost = targExecConfig.getHost();
            remotePort = targExecConfig.getRemotePort();
        }
		while(true){
			if ((processContext != null && !processContext.isRunning()) && !isReadyToStart) {
				return false;
			}
			System.out.println("background thread searching..."); //$NON-NLS-1$
			if(initCurrentActiveJobJvm(remoteHost, remotePort)){
				return true;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			endTime = System.currentTimeMillis();
			if(endTime - startTime > 10*1000){
				return false;
			}
		}
	}

	private boolean initCurrentActiveJobJvm(String remoteHost, int remotePort) {
		boolean isJvmFound = false;
		JvmModel jvmModel = JvmModel.getInstance();
		if (isRemoteRun) {
			if (isRemoteMonitoring && remotePort != -1) {
				try {

					if (currentJvm == null) {
						currentJvm = jvmModel.addRemoteHostAndJvm(remoteHost, remotePort);
						return true;
					}

					if(remotePort == currentJvm.getPort() && currentJvm.isConnected()) {
						return true;
					}

					if(currentJvm.isConnected()) {
						currentJvm.disconnect();
					}

					currentJvm = jvmModel.addRemoteHostAndJvm(remoteHost, remotePort);
					return true;
				} catch (JvmCoreException e) {
					ExceptionHandler.process(e);
					return false;
				}
	    	}
		} else {
            String jobClassName = JavaResourcesHelper.getJobClassName(processContext.getProcess());
            IActiveJvm activeJvm = null;
            try {
                activeJvm = JvmModel.getInstance().getActiveJvmByMainClass(jobClassName, IHost.LOCALHOST);
            } catch (JvmCoreException e) {
                ExceptionHandler.process(e);
            }
            if (activeJvm != null) {
                currentJvm = activeJvm;
                isJvmFound = true;
            }

    	}
        return isJvmFound;
    }

    private void initMonitoringModel() {
        final List<IActiveJvm> contructSelections = new ArrayList<IActiveJvm>();
        contructSelections.add(currentJvm);
        if (currentJvm != null) {
            viewPart.getSite()
                    .setSelectionProvider(new ProcessViewSelectionProvider(new StructuredSelection(contructSelections)));
        }
    }

    public void refreshMonitorComposite() {
        if (monitorComposite != null && !monitorComposite.isDisposed()) {
            monitorComposite.dispose();
        }
        initGraphicComponents(this);
        monitorComposite.layout();
    }

    private String getExecutionInfo(String prefix){
    	return prefix + " time : "+ TalendDate.getDate("hh:mm:ss a MM/dd/YYYY") + System.getProperty("line.separator"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void runProcessContextChanged(final PropertyChangeEvent evt) {
        if (isDisposed()) {
            return;
        }
        String propName = evt.getPropertyName();

        if (RunProcessContext.MEMORY_MONITOR.equals(propName)) {
            getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    boolean monitoring = ((Boolean) evt.getNewValue()).booleanValue();
                    if (runtimeButton != null && !runtimeButton.isDisposed()) {
                    	setRuntimeButtonByStatus(processContext != null && processContext.isRunning());
                    	if (processContext == null) {
                    		setRuntimeButtonByStatus(false);
                    	} else if(processContext.isRunning()) {
                    		setRuntimeButtonByStatus(false);
                    	} else {
                    		disconnectJVM();
                    		setRuntimeButtonByStatus(true);
                    		processContext.setMonitoring(false);
                    		if (AbstractRuntimeGraphcsComposite.isMonitoring()) {
                    			AbstractRuntimeGraphcsComposite.setMonitoring(false);
                    			String content = getExecutionInfo("End"); //$NON-NLS-1$
                    			messageManager.setEndMessage(content, getDisplay().getSystemColor(SWT.COLOR_BLUE), getDisplay().getSystemColor(SWT.COLOR_WHITE));
                    		}
                    		((RuntimeGraphcsComposite)chartComposite).displayReportField();
                    		lock = false;
                    	}
                    }
                    if (!monitoring && chartComposite != null && chartComposite.isDisposed()) {
                        viewPart.setSelection(null);
                        refreshMonitorComposite();
                    }
                }
            });
        }

    }

    public void setProcessContext(RunProcessContext processContext) {

        if (this.processContext != null) {
            this.processContext.removePropertyChangeListener(propertyChangeListener);
        }
        this.processContext = processContext;
        if (processContext != null) {
            processContext.addPropertyChangeListener(propertyChangeListener);
        }

        if (runtimeButton != null && !runtimeButton.isDisposed()) {
        	runtimeButton.setEnabled(processContext != null);
        	//imporve this logic later
        	if(processContext == null){
        		setRuntimeButtonByStatus(true);
        	}else if(!processContext.isRunning()){
        		setRuntimeButtonByStatus(true);
        	}else if(processContext.isMonitoring()){
        		setRuntimeButtonByStatus(false);
        	}else{
        		setRuntimeButtonByStatus(true);
        	}
        }

        if (gcCheckButton != null && !gcCheckButton.isDisposed()) {
        	gcCheckButton.setEnabled(processContext != null);
        	gcCheckButton.setSelection(isGCSelected);
        }

        if (periodCombo != null && !periodCombo.isDisposed()) {
            periodCombo.setEnabled(processContext != null && gcCheckButton.getSelection());
        }


        if (contextCombo !=null && !contextCombo.getCombo().isDisposed()){
        	if(processContext == null){
        		contextCombo.setInput(null);
        	}else{
        		initContextInput();
        	}
    		contextCombo.getCombo().setEnabled(processContext != null);
        }
        if(processContext != null && processContext.isMonitoring()){
        	((RuntimeGraphcsComposite)chartComposite).displayReportField();
        }else{
        	chartComposite.dispose();
        }
        
        if(processContext != null) {
            processContext.setMonitorTrace(false);
            org.talend.core.model.process.IProcess process = processContext.getProcess();
            List<INode> nodeList = (List<INode>) process.getGraphicalNodes();
            for (INode node : nodeList) {
                for (Connection connection : (List<Connection>) node.getOutgoingConnections()) {
                    if(connection != null) {
                        ConnectionTrace traceNode = connection.getConnectionTrace();
                        if (traceNode == null) {
                            continue;
                        }
                        traceNode.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), false);
                        if (connection.checkTraceShowEnable()) {
                        	connection.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), false);
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getCurRowSize()
     */
    @Override
    public int getCurRowSize() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getElement()
     */
    @Override
    public Element getElement() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getHashCurControls()
     */
    @Override
    public BidiMap getHashCurControls() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getPart()
     */
    @Override
    public IMultiPageTalendEditor getPart() {
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
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getSection()
     */
    @Override
    public EComponentCategory getSection() {
        return EComponentCategory.MEMORYRUN;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        if (!isDisposed()) {
            getParent().layout();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#setCurRowSize(int)
     */
    @Override
    public void setCurRowSize(int i) {

    }

    /**
     * LabelProvider for a context combo. <br/>
     *
     * $Id$
     *
     */
    private static class ContextNameLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(final Object element) {
            IContext context = (IContext) element;
            return context.getName();
        }
    }

    private void setRuntimeButtonByStatus(boolean isRunning){
    	if(isRunning){
    		runtimeButton.setText(Messages.getString("ProcessComposite.exec")); //$NON-NLS-1$
    		runtimeButton.setImage(ImageProvider.getImage(ERunprocessImages.RUN_PROCESS_ACTION));
    	}else{
    		runtimeButton.setText(Messages.getString("ProcessComposite.kill")); //$NON-NLS-1$
    		runtimeButton.setImage(ImageProvider.getImage(ERunprocessImages.KILL_PROCESS_ACTION));
    	}
    }

    private void exec() {

        if (processContext instanceof IProcess2) {
            ReplaceNodesInProcessProvider.beforeRunJobInGUI(processContext.getProcess());
        }
        CorePlugin.getDefault().getRunProcessService().saveJobBeforeRun(processContext.getProcess());
        if (processContext.isClearBeforeExec()) {
            processContext.clearMessages();
        }
        processContext.setMonitorTrace(false);
        processContext.setWatchAllowed(processManager.getExecTime());
        processContext.setMonitorPerf(processManager.getStat());

        if (processContext.isSaveBeforeRun()) {
            SaveJobBeforeRunAction action = new SaveJobBeforeRunAction(processContext.getProcess());
            action.run();
        }

        processContext.setSelectedContext(processManager.getSelectContext());
        processContext.setMemoryRunning(true);
        processContext.exec(processManager.getProcessShell());

        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunch[] launches = manager.getLaunches();
        manager.removeLaunches(launches);

    }

    private void disconnectJVM () {

    	final Job disconnectJVM = new Job("disconnect JVM") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	if (isRemoteMonitoring) {
					if(currentJvm != null && currentJvm.isConnected()) {
						currentJvm.disconnect();
					}
				}
                return Status.OK_STATUS;
            }
        };
        disconnectJVM.schedule();
    }

}
