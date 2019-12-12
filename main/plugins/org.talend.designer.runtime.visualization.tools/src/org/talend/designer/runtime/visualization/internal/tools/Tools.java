/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.internal.tools;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.talend.designer.runtime.visualization.IHost;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.tools.Activator;

/**
 * The class enabling to invoke the APIs in <tt>tools.jar</tt>.
 */
public class Tools implements IPropertyChangeListener, IConstants {

    /** The shared instance of this class. */
    private static Tools tools;

    /** The state indicating if ready to use. */
    private boolean isReady;

    /**
     * The constructor.
     */
    private Tools() {
        /*
         * In case of Mac, the required APIs are provided by classes.jar instead of tools.jar, and the additional class
         * path and library path don't have to be set.
         */
        isReady = validateClassPathAndLibraryPath();

        if (!isReady) {
            // Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
            configureClassPathAndLibraryPath();
            isReady = validateClassPathAndLibraryPath();
        }
    }

    /**
     * Gets the shared instance of this class.
     * 
     * @return The shared instance
     */
    public static synchronized Tools getInstance() {
        if (tools == null) {
            tools = new Tools();
        }
        return tools;
    }

    /*
     * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (IConstants.JDK_ROOT_DIRECTORY.equals(event.getProperty())) {
            configureClassPathAndLibraryPath();
        }
    }

    /**
     * Gets the state indicating if it is ready to use.
     * 
     * @return <tt>true</tt> if it is ready to use
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Invokes the getAgentProperties method of VirtualMachine
     * 
     * @param virtualMachine The virtual machine
     * @return The agent properties
     * @throws JvmCoreException
     */
    protected Properties invokeGetAgentProperties(Object virtualMachine) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(GET_AGENT_PROPERTIES_METHOD);
            return (Properties) method.invoke(virtualMachine);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the getMonitoredHost method of MonitoredHost with reflection.
     * 
     * @param name The host name
     * @return The monitored host
     * @throws JvmCoreException
     */
    synchronized protected Object invokeGetMonitoredHost(String name) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(MONITORED_HOST_CLASS);
            Method method = clazz.getDeclaredMethod(GET_MONITORED_HOST_CLASS, new Class[] { String.class });
            return method.invoke(null, name);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the activeVms method of MonitoredHost with reflection.
     * 
     * @param monitoredHost The monitored host
     * @return The active VMs.
     * @throws JvmCoreException
     */
    @SuppressWarnings("unchecked")
    protected Set<Integer> invokeActiveVms(Object monitoredHost) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(MONITORED_HOST_CLASS);
            Method method = clazz.getDeclaredMethod(ACTIVE_VMS_METHOD);
            return (Set<Integer>) method.invoke(monitoredHost);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the constructor of VmIdentifier with reflection.
     * 
     * @param vmId The VM id.
     * @return the VM identifier
     * @throws JvmCoreException
     */
    protected Object invokeVmIdentifier(String vmId) throws JvmCoreException {
        try {
            Constructor<?> clazz = Class.forName(VM_IDENTIFIER_CLASS).getConstructor(new Class[] { String.class });
            return clazz.newInstance(vmId);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the getMonitoredVm of MonitoredHost with reflection.
     * 
     * @param monitoredHost The monitored host
     * @param vmIdentifier The VM identifier
     * @return The monitored VM
     * @throws JvmCoreException
     */
    synchronized protected Object invokeGetMonitoredVm(Object monitoredHost, Object vmIdentifier) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(MONITORED_HOST_CLASS);
            Class<?> clazz2 = Class.forName(VM_IDENTIFIER_CLASS);
            Method method = clazz.getDeclaredMethod(GET_MONITORED_VM_METHOD, new Class[] { clazz2 });
            return method.invoke(monitoredHost, vmIdentifier);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the findByName method of MonitoredVm with reflection.
     * 
     * @param monitoredVm The monitored VM
     * @param name The name
     * @return The monitor
     * @throws JvmCoreException
     */
    protected Object invokeFindByName(Object monitoredVm, String name) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(MONITORED_VM_CLASS);
            Method method = clazz.getDeclaredMethod(FIND_BY_NAME_METHOD, new Class[] { String.class });
            return method.invoke(monitoredVm, name);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the getValue method of Monitor with reflection.
     * 
     * @param monitor The monitor
     * @return The value
     * @throws JvmCoreException
     */
    protected Object invokeGetValue(Object monitor) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(MONITOR_CLASS);
            Method method = clazz.getDeclaredMethod(GET_VALUE_METHOD);
            return method.invoke(monitor);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the attach method of VirtualMachine with reflection.
     * 
     * @param pid The process ID
     * @return The virtual machine
     * @throws JvmCoreException
     */
    protected Object invokeAttach(int pid) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(ATTACH_METHOD, new Class[] { String.class });
            return method.invoke(null, String.valueOf(pid));
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getCause().getMessage(), t);
        }
    }

    /**
     * Invokes the detach method of VirtualMachine with reflection.
     * 
     * @param vm The virtual machine
     * @throws JvmCoreException
     */
    protected void invokeDetach(Object vm) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(DETACH_METHOD);
            method.invoke(vm);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the getSystemProperties method of VirtualMachine with reflection.
     * 
     * @param vm The virtual machine
     * @return The system properties
     * @throws JvmCoreException
     */
    protected Object invokeGetSystemProperties(Object vm) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(GET_SYSTEM_PROPERTIES_METHOD);
            return method.invoke(vm);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Invokes the loadAgent method of VirtualMachine with reflection.
     * 
     * @param virtualMachine The virtual machine
     * @param path The path for agent jar file
     * @param options The options given to agent
     * @throws JvmCoreException
     */
    protected void invokeLoadAgent(Object virtualMachine, String path, String options) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(LOAD_AGENT_METHOD, new Class[] { String.class, String.class });
            method.invoke(virtualMachine, path, options);
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null) {
                Throwable cause = t.getCause();
                while (cause != null) {
                    message = cause.getMessage();
                    if (message != null) {
                        break;
                    }
                    cause = cause.getCause();
                }
            }
            throw new JvmCoreException(IStatus.ERROR, message, t);
        }
    }
    
    protected Object invokeStartLocalManagementAgent(Object virtualMachine) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(START_LOCAL_MANAGEMENT_AGENT);
            return method.invoke(virtualMachine);
        } catch (Throwable t) {
            String message = t.getMessage();
            if (message == null) {
                Throwable cause = t.getCause();
                while (cause != null) {
                    message = cause.getMessage();
                    if (message != null) {
                        break;
                    }
                    cause = cause.getCause();
                }
            }
            throw new JvmCoreException(IStatus.ERROR, message, t);
        }
    }

    /**
     * Invokes the heapHisto method of HotSpotVirtualMachine with reflection.
     * 
     * @param virtualMachine The virtual machine
     * @param isLive True to dump only live objects
     * @return The input stream of heap histo
     * @throws JvmCoreException
     */
    protected InputStream invokeHeapHisto(Object virtualMachine, boolean isLive) throws JvmCoreException {
        try {
            Class<?> clazz = Class.forName(HOT_SPOT_VIRTUAL_MACHINE_CLASS);
            Method method = clazz.getDeclaredMethod(HEAP_HISTO_METHOD, new Class[] { Object[].class });
            Object[] arg = new Object[] { isLive ? HEAP_HISTO_LIVE_OPTION : HEAP_HISTO_ALL_OPTION };
            return (InputStream) method.invoke(virtualMachine, (Object) arg);
        } catch (Throwable t) {
            throw new JvmCoreException(IStatus.ERROR, t.getMessage(), t);
        }
    }

    /**
     * Validates the JDK root directory.
     * 
     * @param jdkRootDirectory The JDK root directory
     * @return The error message or <tt>null</tt> if not found
     */
    protected String validateJdkRootDirectory(String jdkRootDirectory) {

        // check if directory exists
        File directory = new File(jdkRootDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            return Messages.directoryNotExistMsg;
        }

        // check if tools.jar exists
        File toolsJarFile = new File(jdkRootDirectory + TOOLS_JAR);
        if (!toolsJarFile.exists()) {
            return Messages.notJdkRootDirectoryMsg;
        }

        // checks if "attach" shared library exist
        String libraryPath = getJreLibraryPath(jdkRootDirectory);
        if (libraryPath != null) {
            return null;
        }

        return Messages.notJdkRootDirectoryMsg;
    }

    /**
     * Searches the JDK root directory.
     * 
     * @return The JDK root directory, or empty string if not found
     */
    private String searchJdkRootDirectory() {

        // search from the JREs that are specified on preference page
        for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
            for (IVMInstall install : type.getVMInstalls()) {
                String jdkRootDirectory = install.getInstallLocation().getPath();
                if (null == validateJdkRootDirectory(jdkRootDirectory)) {
                    Activator.log(IStatus.INFO, NLS.bind(Messages.jdkRootDirectoryFoundMsg, jdkRootDirectory), new Exception());
                    return jdkRootDirectory;
                }
            }
        }

        // search at the same directory as current JRE
        String javaHome = System.getProperty(JAVA_HOME_PROPERTY_KEY);
        for (File directory : getPossibleJdkRootDirectory(javaHome)) {
            String path = directory.getPath();
            if (null == validateJdkRootDirectory(path)) {
                Activator.log(IStatus.INFO, NLS.bind(Messages.jdkRootDirectoryFoundMsg, path), new Exception());
                return path;
            }
        }

        Activator.log(IStatus.WARNING, Messages.jdkRootDirectoryNotFoundMsg, new Exception());
        return ""; //$NON-NLS-1$
    }

    /**
     * Gets the directories that could be JDK root directory.
     * 
     * @param javaHome The java home path
     * @return The directories that could be JDK root directory.
     */
    private static List<File> getPossibleJdkRootDirectory(String javaHome) {
        List<File> dirs = new ArrayList<File>();

        /*
         * On Mac, java home path can be for example:
         * /Library/Java/JavaVirtualMachines/jdk1.7.0_13.jdk/Contents/Home/jre
         */
        if (Util.isMac()) {
            int index = javaHome.indexOf(IConstants.JAVA_INSTALLATION_DIR_ON_MAC);
            if (index == -1) {
                return dirs;
            }

            String javaVirtualMachinesPath = javaHome.substring(0, index + IConstants.JAVA_INSTALLATION_DIR_ON_MAC.length());
            File dir = new File(javaVirtualMachinesPath);

            collectDirs(dirs, dir, 3);
            return dirs;
        }

        File parentDir = new File(javaHome + File.separator + ".."); //$NON-NLS-1$
        if (parentDir.exists()) {
            for (File file : parentDir.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                }
            }
        }

        return dirs;
    }

    /**
     * Collects the directories which are within given depth from given base directory.
     * 
     * @param dirs The directories to store result
     * @param dir The directory to search
     * @param depth The depth to search
     */
    private static void collectDirs(List<File> dirs, File dir, int depth) {
        if (depth > 0) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                    collectDirs(dirs, file, depth - 1);
                }
            }
        }
    }

    /**
     * Validates the class path and library path.
     * 
     * @return <tt>true</tt> if tools.jar (or classses.jar in Mac) can be found in class path, and the required shared
     * library can be also found.
     */
    private boolean validateClassPathAndLibraryPath() {
        try {
            invokeGetMonitoredHost(IHost.LOCALHOST);
        } catch (JvmCoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Configures the class path and library path.
     */
    private void configureClassPathAndLibraryPath() {
        String jdkRootDirectory = Activator.getDefault().getPreferenceStore().getString(IConstants.JDK_ROOT_DIRECTORY);
        if (jdkRootDirectory.isEmpty()) {
            jdkRootDirectory = searchJdkRootDirectory();
            Activator.getDefault().getPreferenceStore().setValue(IConstants.JDK_ROOT_DIRECTORY, jdkRootDirectory);
        }

        if (validateJdkRootDirectory(jdkRootDirectory) != null) {
            return;
        }

        try {
            addClassPath(jdkRootDirectory);
        } catch (Throwable t) {
            Activator.log(IStatus.ERROR, Messages.addingClassPathFailedMsg, t);
            return;
        }

        try {
            addLibraryPath(jdkRootDirectory);
        } catch (Throwable t) {
            Activator.log(IStatus.ERROR, Messages.addingLibraryPathFailedMsg, t);
            return;
        }
    }

    /**
     * Gets the JRE library path.
     * 
     * @param jdkRootDirectory The JDK root directory
     * @return The JRE library path or <tt>null</tt> it not found
     */
    private static String getJreLibraryPath(String jdkRootDirectory) {
        for (String path : LIBRARY_PATHS) {
            File attachLibraryFile = new File(jdkRootDirectory + path + File.separator + System.mapLibraryName(ATTACH_LIBRARY));
            if (attachLibraryFile.exists()) {
                return jdkRootDirectory + path;
            }
        }
        return null;
    }

    /**
     * Adds the class path to the system class loader.
     * 
     * @param jdkRootDirectory The JDK root directory
     * @throws Throwable
     */
    private static void addClassPath(String jdkRootDirectory) throws Throwable {
        File file = new File(jdkRootDirectory + TOOLS_JAR);
        URL toolsJarUrl = file.toURI().toURL();

        Class<URLClassLoader> clazz = URLClassLoader.class;
        Method method = clazz.getDeclaredMethod(ADD_URL_METHOD, new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { toolsJarUrl });

        Activator.log(IStatus.INFO, NLS.bind(Messages.classPathAddedMsg, file.getPath()), new Exception());
    }

    /**
     * Adds the library path to the system class loader.
     * 
     * @param jdkRootDirectory The JDK root directory
     * @throws Throwable
     */
    private static void addLibraryPath(String jdkRootDirectory) throws Throwable {
        String libraryPath = System.getProperty(JAVA_LIBRARY_PATH);
        String jreLibraryPath = getJreLibraryPath(jdkRootDirectory);

        System.setProperty(JAVA_LIBRARY_PATH, libraryPath + File.pathSeparator + jreLibraryPath);

        /*
         * clear the sys_paths field so that the library path is reset with system property "java.library.path" when
         * loading a library next time.
         */
        Class<ClassLoader> clazz = ClassLoader.class;
        Field field = clazz.getDeclaredField(SYS_PATHS_FIELD);
        field.setAccessible(true);
        field.set(clazz, null);
    }
}
