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
package org.talend.designer.runprocess.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.INode;
import org.talend.designer.runprocess.IESBRunContainerService;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.repository.ProjectManager;

/**
 * DOC yyan class global comment. Detailled comment <br/>
 * For tracing ESB route running information from runtime by JMX
 *
 */
public class JMXRunStatManager {

    private static JMXRunStatManager manager;

    private static int limitSize = 5;

    private List<RunProcessContext> contextList;

    private Map<String, String> targetNodeToConnectionMap = new HashMap(); // processorId, conn

    private JMXRunStat jmxRunStat;

    private List<JMXPerformanceChangeListener> listeners;

    private String projectLabel;

    public JMXRunStatManager() {
        contextList = new ArrayList();
        listeners = new ArrayList();
        // local_project.let1-local_project
        projectLabel = ProjectManager.getInstance().getCurrentProject().getLabel().toLowerCase();
    }

    private void updateConnectionMap(RunProcessContext esbProcessContext, boolean isAdded) {

        if (esbProcessContext.getProcess() != null) {
            for (INode node : esbProcessContext.getProcess().getGraphicalNodes()) {
                if (node.isActivate()) {
                    for (org.talend.core.model.process.IConnection conn : node.getIncomingConnections()) {
                        if (isAdded) {
                            targetNodeToConnectionMap.put(esbProcessContext.getProcess().getLabel() + "_" + node.getUniqueName(),
                                    conn.getUniqueName());
                        } else {
                            targetNodeToConnectionMap.remove(
                                    esbProcessContext.getProcess().getLabel() + "_" + node.getUniqueName(), conn.getUniqueName());
                        }
                    }
                }
            }
        }
    }

    public static JMXRunStatManager getInstance() {
        if (manager == null) {
            manager = new JMXRunStatManager();
        }
        return manager;
    }

    public void addProcessContext(RunProcessContext context) {
        contextList.add(context);
        updateConnectionMap(context, true);
    }

    public void removeProcessContext(RunProcessContext context) {
        contextList.remove(context);
        updateConnectionMap(context, false);
    }

    public void addTracing(RunProcessContext context) {
        if (jmxRunStat == null || jmxRunStat.stopTracing) {
            jmxRunStat = new JMXRunStat();
            Thread thread = new Thread(jmxRunStat, "JMX_Run_Stat");
            thread.start();
        }
        addProcessContext(context);
    }

    public void stopTracing(RunProcessContext context) {
        removeProcessContext(context);
        if (jmxRunStat != null && contextList.size() == 0) {
            jmxRunStat.stopTracing();
        }
    }

    public void addPerformancesChangeListener(JMXPerformanceChangeListener performanceChangeListener) {
        listeners.add(performanceChangeListener);
    }

    public void removePerformancesChangeListener(JMXPerformanceChangeListener performanceChangeListener) {
        listeners.remove(performanceChangeListener);
    }

    class JMXRunStat implements Runnable {

        private boolean stopTracing = false;

        private IESBRunContainerService service;

        public JMXRunStat() {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBRunContainerService.class)) {
                IESBRunContainerService runContainerService = (IESBRunContainerService) GlobalServiceRegister.getDefault()
                        .getService(IESBRunContainerService.class);
                if (runContainerService != null) {
                    this.service = runContainerService;
                }
            }
        }

        @Override
        public void run() {
            stopTracing = false;
            MBeanServerConnection jmxConn = service.getJMXServerConnection();
            Map<String, Integer> completedHistory = new HashMap();
            final String query = "org.apache.camel:context=" + projectLabel + ".*,type=processors,name=*";
            // org.apache.camel:context=local_project.let1-local_project.let1_0_1.let1,type=processors,name="let1_cLog_1"
            while (service != null && !stopTracing) {
                if (listeners.size() > 0) {
                    if (jmxConn == null) {
                        jmxConn = service.getJMXServerConnection();
                    } else {
                        try {
                            // for camel route
                            Set<ObjectName> components = jmxConn.queryNames(new ObjectName(query), null);
                            for (ObjectName component : components) {
                                int completed = Integer.parseInt(String.valueOf(jmxConn.getAttribute(component,
                                        "ExchangesCompleted")));

                                String camelID = String.valueOf(jmxConn.getAttribute(component, "CamelId"));
                                String processorId = String.valueOf(jmxConn.getAttribute(component, "ProcessorId"));

                                for (JMXPerformanceChangeListener listener : listeners) {
                                    if (listener.getProcessName().equals(camelID.substring(camelID.lastIndexOf('.') + 1))) {
                                        if (completedHistory.get(processorId) != null
                                                && completed == completedHistory.get(processorId)) {
                                            continue;
                                        } else {
                                            completedHistory.put(processorId, completed);
                                            listener.performancesChanged(targetNodeToConnectionMap.get(processorId), completed);
                                        }
                                    }
                                }
                            }
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            completedHistory.clear();
            completedHistory = null;
        }

        /**
         * JMX run stat only stop with stop tracing
         */
        public void stopTracing() {
            stopTracing = true;
        }
    }
}
