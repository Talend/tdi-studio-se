/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.talend.designer.runtime.visualization.JvmModelEvent.State;
import org.talend.designer.runtime.visualization.internal.core.ActiveJvm;
import org.talend.designer.runtime.visualization.internal.core.Host;
import org.talend.designer.runtime.visualization.internal.core.Util;
import org.talend.designer.runtime.visualization.views.RuntimeGraphcsComposite;

/**
 * The JVM model that is singleton having hosts and JVMs.
 */
public class JvmModel {

    /** The extension ID for JVM attach handler. */
    private static final String JVM_ATTACH_HANDLER_EXTENSION_ID = Activator.PLUGIN_ID + ".jvmAttachHandler"; //$NON-NLS-1$ Activator.PLUGIN_ID + ".jvmAttachHandler"

    /** The extension ID for heap dump handler. */
    private static final String HEAP_DUMP_HANDLER_EXTENSION_ID = Activator.PLUGIN_ID + ".heapDumpHandler"; //$NON-NLS-1$

    /** The extension ID for agent load handler. */
    private static final String AGENT_LOAD_HANDLER_EXTENSION_ID = Activator.PLUGIN_ID + ".agentLoadHandler"; //$NON-NLS-1$

    /** The property name for class. */
    private static final String CLASS = "class"; //$NON-NLS-1$

    /** The configuration element name for handler. */
    private static final String HANDLER = "handler"; //$NON-NLS-1$

    /** The shared instance of JVM model. */
    private static JvmModel jvmModel;

    /** The hosts. */
    private List<IHost> hosts;

    /** The JVM model change listeners. */
    private List<IJvmModelChangeListener> listeners;

    /** The JVM attach handler. */
    private IJvmAttachHandler jvmAttachHandler;

    /** The heap dump handler. */
    private IHeapDumpHandler heapDumpHandler;

    /** The agent load handler. */
    private IAgentLoadHandler agentLoadHandler;

    /**
     * The constructor.
     */
    private JvmModel() {
        hosts = new CopyOnWriteArrayList<IHost>();
        listeners = new CopyOnWriteArrayList<IJvmModelChangeListener>();

        try {
            jvmAttachHandler = (IJvmAttachHandler) loadHandler(JVM_ATTACH_HANDLER_EXTENSION_ID);
            heapDumpHandler = (IHeapDumpHandler) loadHandler(HEAP_DUMP_HANDLER_EXTENSION_ID);
            agentLoadHandler = (IAgentLoadHandler) loadHandler(AGENT_LOAD_HANDLER_EXTENSION_ID);
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, e.getMessage(), e);
        }

        restoreHosts();
    }

    /**
     * Gets the shared instance of JVM model.
     * 
     * @return The shared instance of JVM model
     */
    public static synchronized JvmModel getInstance() {
        if (jvmModel == null) {
            jvmModel = new JvmModel();
        }
        return jvmModel;
    }

    /**
     * Gets the heap dump handler.
     * 
     * @return The heap dump handler, or <tt>null</tt> if not available
     */
    public IHeapDumpHandler getHeapDumpHandler() {
        return heapDumpHandler;
    }

    /**
     * Gets the agent load handler.
     * 
     * @return The agent load handler, or <tt>null</tt> if not available
     */
    public IAgentLoadHandler getAgentLoadHandler() {
        return agentLoadHandler;
    }

    /**
     * Gets the hosts.
     * 
     * @return the hosts
     */
    public List<IHost> getHosts() {
        return hosts;
    }

    /**
     * Gets the host corresponding to the given host name.
     * 
     * @param hostname The host name
     * @return The host, or <tt>null</tt> if not found
     */
    public IHost getHost(String hostname) {
        for (IHost host : hosts) {
            if (host.getName().equals(hostname)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Adds the host.
     * 
     * @param hostname The host name
     * @return The host
     */
    public IHost addHost(String hostname) {
        IHost host = getHost(hostname);
        if (host != null) {
            return host;
        }

        host = new Host(hostname);
        hosts.add(host);

        fireJvmModelChangeEvent(new JvmModelEvent(State.HostAdded, null));
        return host;
    }

    /**
     * Method for connecting to JobServer.
     * @param remoteHost
     * @throws JvmCoreException
     */
    public IActiveJvm addRemoteHostAndJvm(String remoteHost, int remotePort) throws JvmCoreException {
    	String url = "service:jmx:rmi:///jndi/rmi://" + remoteHost + ":" + remotePort + "/jmxrmi";
    	return addHostAndJvm(url, null, null, 1000);
    }
    
    /**
     * Adds the host and JVM. If the host corresponding to the given JMX URL already exists, only JVM will be added to
     * model.
     * 
     * @param url The JMX URL
     * @param userName The user name
     * @param password The password
     * @param updatePeriod The update period
     * @return The active JVM
     * @throws JvmCoreException if connecting to JVM fails
     */
    public IActiveJvm addHostAndJvm(String url, String userName, String password, int updatePeriod) throws JvmCoreException {
        IActiveJvm jvm = new ActiveJvm(url, userName, password, updatePeriod);

        IHost host = jvm.getHost();
        if (host == null) {
            return jvm;
        }

        if (!hosts.contains(host)) {
            hosts.add(host);
            fireJvmModelChangeEvent(new JvmModelEvent(State.HostAdded, null));
        }
        return jvm;
    }

    /**
     * Removes the host.
     * 
     * @param host The host
     */
    public void removeHost(IHost host) {
        hosts.remove(host);
        try {
            IPath hostDir = ((Host) host).getHostDir();
            Util.deleteDir(hostDir.toFile());
        } catch (JvmCoreException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.removeHostFailedMsg, host.getName()), e);
        }
        fireJvmModelChangeEvent(new JvmModelEvent(State.HostRemoved, null));
    }

    /**
     * Adds the JVM model change listener.
     * 
     * @param listener The JVM model change listener
     */
    public void addJvmModelChangeListener(IJvmModelChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the JVM model change listener.
     * 
     * @param listener The JVM model change listener
     */
    public void removeJvmModelChangeListener(IJvmModelChangeListener listener) {
		listeners.remove(listener);
    }

    /**
     * Gets the JVM model change listeners.
     * 
     * @return The JVM model change listeners
     */
    public List<IJvmModelChangeListener> getJvmModelChangeListeners() {
        return listeners;
    }

    /**
     * Gets the state indicating if valid JDK is available in a sense that JDK has tools.jar at expected location. This
     * method makes sense only for local host.
     * 
     * @return True if valid JDK is available
     */
    public boolean hasValidJdk() {
        if (jvmAttachHandler != null) {
            return jvmAttachHandler.hasValidJdk();
        }
        return false;
    }

    /**
     * Fires the JVM model change event.
     * 
     * @param e The JVM model changed event
     */
    public void fireJvmModelChangeEvent(JvmModelEvent e) {
        for (IJvmModelChangeListener listener : listeners) {
//        	System.out.println("from JVM:" + listeners.hashCode());
        	if(listener instanceof RuntimeGraphcsComposite){
        		if(((RuntimeGraphcsComposite) listener).isDisposed()){
        			listeners.remove(listener);
        		}
        	}
            listener.jvmModelChanged(e);
        }
    }

    public void fireJvmModelOpenEvent() {

    }

    /**
     * Restores the hosts.
     */
    private void restoreHosts() {
        IHost localhost = addHost(IHost.LOCALHOST);
        if (jvmAttachHandler != null) {
            jvmAttachHandler.setHost(localhost);
        }

        IPath baseDir = Activator.getDefault().getStateLocation();

        File[] files = baseDir.toFile().listFiles();
        if (files == null) {
            return;
        }

        for (File dir : files) {
            if (!dir.isDirectory()) {
                continue;
            }

            String hostDir = dir.getName();
            if (!hostDir.endsWith(Host.DIR_SUFFIX)) {
                continue;
            }

            IPath filePath = baseDir.append(File.separator + hostDir + File.separator + Host.PROPERTIES_FILE);
            if (!filePath.toFile().exists()) {
                Util.deleteDir(dir);
                continue;
            }

            Properties props = Util.loadProperties(filePath);
            if (props == null) {
                Util.deleteDir(dir);
                continue;
            }

            String hostname = props.getProperty(Host.HOST_PROP_KEY);
            if (hostname != null) {
                addHost(hostname);
            }
        }
    }

    public IActiveJvm getActiveJvmByMainClass(String mainClassName, String host) throws JvmCoreException {
        int nb = 0;
        while (true) {
            List<IActiveJvm> activeJvms = getHost(host).getActiveJvms();
            for (IActiveJvm activeJvm : activeJvms) {
                String jvmName = activeJvm.getMainClass();
                if (jvmName != null) {
                    jvmName = jvmName.trim();
                    if (mainClassName.equals(jvmName)) {
                        return activeJvm;
                    }
                }
            }
            jvmAttachHandler.updatesActiveJvms();
            if (nb > 3) {
                break;
            }
        }
        return null;
    }

    /**
     * Loads the handler for the given extension point.
     * 
     * @param extensionPoint The extension point
     * @return The handler
     * @throws CoreException if creating executable extension fails
     */
    private static Object loadHandler(String extensionPoint) throws CoreException {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(extensionPoint).getExtensions();

        for (IExtension extension : extensions) {
            IConfigurationElement[] elements = extension.getConfigurationElements();

            for (IConfigurationElement element : elements) {
                if (element.getName().equals(HANDLER)) {
                    return element.createExecutableExtension(CLASS);
                }
            }
        }

        return null;
    }
}
