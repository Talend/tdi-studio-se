/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.internal.tools;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.IHost;
import org.talend.designer.runtime.visualization.IJvmAttachHandler;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.tools.Activator;

/**
 * The JVM attach handler that contributes to the extension point <tt>org.jvmmonitor.core.jvmAttachHandler</tt>.
 */
public class JvmAttachHandler implements IJvmAttachHandler, IPropertyChangeListener, IConstants {

    /** The local host. */
    private IHost localhost;

    /** The timer. */
    Timer timer;

    /*
     * @see IJvmAttachHandler#setHost(IHost)
     */
    @Override
    public void setHost(IHost host) {
        this.localhost = host;

        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);

        startMonitoring();
    }

    /*
     * @see IJvmAttachHandler#hasValidJdk()
     */
    @Override
    public boolean hasValidJdk() {
        return Tools.getInstance().isReady();
    }

    /*
     * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        startMonitoring();
    }

    /**
     * Starts monitoring.
     */
    private void startMonitoring() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    updatesActiveJvms();
                } catch (Throwable t) {
                	// for bug TDI-32970 
                    Activator.log(IStatus.WARNING, Messages.updateTimerCanceledMsg, t);
                    timer.cancel();
                }
            }
        };

        long period = Activator.getDefault().getPreferenceStore().getLong(IConstants.UPDATE_PERIOD);
        if (period == 0) {
            period = 1000;
        }
        timer.scheduleAtFixedRate(timerTask, 0, period);
    }

    /**
     * Updates the active JVMs.
     * 
     * @throws JvmCoreException
     */
    public void updatesActiveJvms() throws JvmCoreException {

        Object monitoredHost = Tools.getInstance().invokeGetMonitoredHost(IHost.LOCALHOST);

        Set<Integer> activeJvms = Tools.getInstance().invokeActiveVms(monitoredHost);

        // add JVMs
        List<IActiveJvm> previousVms = localhost.getActiveJvms();
        for (int pid : activeJvms) {
            if (containJvm(previousVms, pid)) {
                continue;
            }

            addActiveJvm(pid, monitoredHost);
        }

        // remove JVMs
        for (IActiveJvm jvm : previousVms) {
            Integer pid = jvm.getPid();
            if (!activeJvms.contains(pid)) {
                localhost.removeJvm(pid);
            }
        }
    }

    /**
     * Checks if the given list of JVMs contains the given pid.
     * 
     * @param jvms The list of active JVMs
     * @param pid The pid
     * @return True if the given list of JVMs contains the given pid
     */
    private static boolean containJvm(List<IActiveJvm> jvms, int pid) {
        for (IActiveJvm jvm : jvms) {
            if (jvm.getPid() == pid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the active JVM.
     * 
     * @param pid The pid
     * @param monitoredHost The monitored host
     */
    private void addActiveJvm(int pid, Object monitoredHost) {
        String vmId = String.format(IConstants.VM_IDENTIFIRER, pid);
        Tools tools = Tools.getInstance();

        Object monitoredVm = null;
        try {
            monitoredVm = tools.invokeGetMonitoredVm(monitoredHost, tools.invokeVmIdentifier(vmId));
        } catch (JvmCoreException e) {
            Activator.log(IStatus.ERROR, Messages.getMonitoredJvmFailedMsg, e);
        }

        String mainClass = null;
        String localConnectorAddress = null;
        String stateMessage = null;
        if (monitoredVm != null) {
            mainClass = getMainClass(monitoredVm, pid);
            try {
                localConnectorAddress = getLocalConnectorAddress(monitoredVm, pid);
            } catch (JvmCoreException e) {
                stateMessage = e.getMessage();
                String message = NLS.bind(Messages.getLocalConnectorAddressFailedMsg, pid);
                Activator.log(IStatus.WARNING, message, e);
            }
        }

        try {
            localhost.addLocalActiveJvm(pid, mainClass, localConnectorAddress, stateMessage);
        } catch (JvmCoreException e) {
            String message = NLS.bind(Messages.connectTargetJvmFailedMsg, pid);
            Activator.log(IStatus.WARNING, message, e);
        }
    }

    /**
     * Gets the main class name.
     * 
     * @param monitoredVm The monitored JVM.
     * @param pid The pid
     * @return The main class name.
     */
    private static String getMainClass(Object monitoredVm, int pid) {
        String javaCommand;
        try {
            Tools tools = Tools.getInstance();
            Object monitor = tools.invokeFindByName(monitoredVm, IConstants.JAVA_COMMAND_KEY);
            if (monitor == null) {
                return ""; //$NON-NLS-1$
            }

            javaCommand = tools.invokeGetValue(monitor).toString();
            if (javaCommand == null) {
                return ""; //$NON-NLS-1$
            }
        } catch (JvmCoreException e) {
            String message = NLS.bind(Messages.getMainClassNameFailed, pid);
            Activator.log(IStatus.ERROR, message, e);
            return ""; //$NON-NLS-1$
        }

        /*
         * javaCommand contains Java executable options that are sorted so that the main class or jar comes first.
         */
        String[] elements = javaCommand.split(IConstants.JAVA_OPTIONS_DELIMITER);
        String mainClass;
        if (elements.length > 0) {
            mainClass = elements[0];
        } else {
            mainClass = javaCommand;
        }
        return mainClass;
    }

    /**
     * Gets the local connector address.
     * 
     * @param monitoredVm The monitored JVM
     * @param pid The process ID
     * 
     * @return The local connector address
     * @throws JvmCoreException
     */
    private static String getLocalConnectorAddress(Object monitoredVm, int pid) throws JvmCoreException {
        String url = null;

        Tools tools = Tools.getInstance();
        Object virtualMachine = null;
        try {
            virtualMachine = tools.invokeAttach(pid);

            String javaHome = ((Properties) tools.invokeGetSystemProperties(virtualMachine))
                    .getProperty(IConstants.JAVA_HOME_PROPERTY_KEY);

            File file = new File(javaHome + IConstants.MANAGEMENT_AGENT_JAR);

            if (!file.exists()) {
                String message = NLS.bind(Messages.fileNotFoundMsg, file.getPath());
                throw new JvmCoreException(IStatus.ERROR, message, new Exception());
            }

            tools.invokeLoadAgent(virtualMachine, file.getAbsolutePath(), IConstants.JMX_REMOTE_AGENT);

            Properties props = tools.invokeGetAgentProperties(virtualMachine);
            url = (String) props.get(LOCAL_CONNECTOR_ADDRESS);
        } finally {
            if (virtualMachine != null) {
                try {
                    tools.invokeDetach(virtualMachine);
                } catch (JvmCoreException e) {
                    // ignore
                }
            }
        }
        return url;
    }
}
