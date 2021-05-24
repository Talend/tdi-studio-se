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
package org.talend.designer.runtime.visualization.internal.ui.properties.timeline;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.MBean.IMBeanServer;
import org.talend.designer.runtime.visualization.MBean.IMonitoredMXBeanGroup;
import org.talend.designer.runtime.visualization.MBean.IMonitoredMXBeanGroup.AxisUnit;

/**
 * created by ldong on Apr 7, 2015 Detailled comment
 *
 */
public class LoadChartAction extends AbstractChartAction {

    /** The non heap memory pool MBeans. */
    private static final String[] NON_HEAP_MEMORYPOOL_MXBEANS = { "name=.*Code Cache", "name=.*Perm Gen" }; //$NON-NLS-1$ //$NON-NLS-2$

    /** The heap memory pool MBeans. */
    private static final String[] HEAP_MEMORYPOOL_MXBEANS = { "name=.*Eden Space", "name=.*Survivor Space", "name=.*Tenured Gen", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "name=.*Old Gen" }; //$NON-NLS-1$

    /**
     * The constructor.
     *
     * @param section The property section
     */
    public LoadChartAction(Composite composite) {
        super(composite);
        setText(Messages.loadChartSetLabel);
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        LoadChartSetDialog dialog;
        try {
            dialog = new LoadChartSetDialog(graphComposite.getShell(), getChartSets(), getDefaultChartSet(),
                    getPredefinedChartSets());
        } catch (WorkbenchException e) {
            Activator.log(IStatus.ERROR, Messages.openSaveChartSetAsDialogFailedMsg, e);
            return;
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, Messages.openSaveChartSetAsDialogFailedMsg, e);
            return;
        }

        if (dialog.open() == Window.OK) {
            try {
                saveChartSets(dialog.getChartSets());
                loadChartSet(dialog.getChartSet());
            } catch (WorkbenchException e) {
                Activator.log(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
            } catch (JvmCoreException e) {
                Activator.log(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
            }
            storeDefaultChartSet(dialog.getDefaultChartSet());
        }
    }

    /**
     * Loads the default chart set.
     *
     * @throws JvmCoreException
     */
    public void loadDefaultChartSet() throws JvmCoreException {
        try {
            loadChartSet(getDefaultChartSet());
        } catch (WorkbenchException e) {
            Activator.log(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
        }
    }

    /**
     * Stores the default chart set.
     *
     * @param defaultChartSet The default chart set
     */
    private static void storeDefaultChartSet(String defaultChartSet) {
        Activator.getDefault().getPreferenceStore().setValue(DEFAULT_CHART_SET, defaultChartSet);
    }

    /**
     * Gets the default chart set.
     *
     * @return The default chart set
     * @throws IOException
     * @throws WorkbenchException
     */
    private String getDefaultChartSet() throws WorkbenchException, IOException {
        String defaultChartSet = Activator.getDefault().getPreferenceStore().getString(DEFAULT_CHART_SET);
        List<String> chartSets = getChartSets();
        if (defaultChartSet.isEmpty() || !chartSets.contains(defaultChartSet)) {
            Activator.getDefault().getPreferenceStore().setDefault(DEFAULT_CHART_SET, OVERVIEW_CHART_SET);
            defaultChartSet = OVERVIEW_CHART_SET;
        }
        return defaultChartSet;
    }

    /**
     * Gets the chart sets including predefined chart sets.
     *
     * @return The chart sets
     * @throws WorkbenchException
     * @throws IOException
     */
    @Override
    List<String> getChartSets() throws WorkbenchException, IOException {
        List<String> elements = super.getChartSets();
        elements.add(0, OVERVIEW_CHART_SET);
        elements.add(1, MEMORY_CHART_SET);
        return elements;
    }

    /**
     * Loads the given chart set.
     *
     * @param chartSet The chart set
     * @throws JvmCoreException
     */
    private void loadChartSet(String chartSet) throws JvmCoreException {
        loadPredefinedChartSet(chartSet);

        IMemento chartSetsMemento;
        try {
            chartSetsMemento = getChartSetsMemento();
        } catch (WorkbenchException e) {
            throw new JvmCoreException(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
        } catch (IOException e) {
            throw new JvmCoreException(IStatus.ERROR, Messages.loadChartSetFailedMsg, e);
        }

        if (chartSetsMemento == null) {
            return;
        }
        IMemento[] mementos = chartSetsMemento.getChildren(CHART_SET);
        for (IMemento memento : mementos) {
            if (chartSet.equals(memento.getID())) {
                loadChartSet(memento);
                return;
            }
        }
    }

    /**
     * Loads the given memento of chart set.
     *
     * @param memento The memento
     * @throws JvmCoreException
     */
    private void loadChartSet(IMemento memento) throws JvmCoreException {
        IMBeanServer server = graphComposite.getJvm().getMBeanServer();
        server.getMonitoredAttributeGroups().clear();

        StringBuffer buffer = new StringBuffer();
        for (IMemento groupMemento : memento.getChildren(GROUP)) {
            IMonitoredMXBeanGroup group = server.addMonitoredAttributeGroup(groupMemento.getID(),
                    AxisUnit.valueOf(groupMemento.getString(UNIT)));
            for (IMemento attributeMemento : groupMemento.getChildren(ATTRIBUTE)) {
                String objectName = attributeMemento.getString(OBJECT_NAME);
                String attributeName = attributeMemento.getID();

                if (attributeExist(objectName, attributeName)) {
                    group.addAttribute(objectName, attributeName, getRGB(attributeMemento.getString(COLOR)));
                } else {
                    buffer.append('\n');
                    buffer.append(objectName + ':' + attributeName);
                }
            }
        }

        if (buffer.length() > 0) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.errorDialogTitle,
                    NLS.bind(Messages.attributeNotSupportedMsg, buffer.toString()));
        }
    }

    /**
     * Gets the state indicating if the given attribute exists.
     *
     * @param objectName The object name
     * @param attributeName The attribute name
     * @return <tt>true</tt> if the given attribute exists
     */
    private boolean attributeExist(String objectName, String attributeName) {
        IMBeanServer server = graphComposite.getJvm().getMBeanServer();
        try {
            server.getAttribute(ObjectName.getInstance(objectName), attributeName);
        } catch (MalformedObjectNameException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        } catch (JvmCoreException e) {
            return false;
        }
        return true;
    }

    /**
     * Loads the given predefined chat set. If the given chart set is not predefined one, does nothing.
     *
     * @param chartSet The chart set
     * @throws JvmCoreException
     */
    private void loadPredefinedChartSet(String chartSet) throws JvmCoreException {
        if (OVERVIEW_CHART_SET.equals(chartSet)) {
            loadOverviewChartSet();
        } else if (MEMORY_CHART_SET.equals(chartSet)) {
            loadMemoryChartSet();
        }
    }

    /**
     * Loads the overview chart set.
     *
     * @throws JvmCoreException
     */
    private void loadOverviewChartSet() throws JvmCoreException {
        final int[] blue = new int[] { 0, 0, 255 };
        final int[] red = new int[] { 255, 0, 0 };
        final int[] green = new int[] { 0, 255, 0 };
        final int[] orange = new int[] { 255, 128, 0 };
        IMBeanServer server = graphComposite.getJvm().getMBeanServer();
        server.getMonitoredAttributeGroups().clear();

        IMonitoredMXBeanGroup group = server.addMonitoredAttributeGroup("Used Heap Memory", AxisUnit.MBytes); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "HeapMemoryUsage.used", blue); //$NON-NLS-1$

        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "HeapMemoryUsage.maxsize", red); //$NON-NLS-1$

        //        group = server.addMonitoredAttributeGroup("Loaded Class Count", AxisUnit.Count); //$NON-NLS-1$
        //        group.addAttribute(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, "LoadedClassCount", blue); //$NON-NLS-1$

        //        group = server.addMonitoredAttributeGroup("Thread Count", AxisUnit.Count); //$NON-NLS-1$
        //        group.addAttribute(ManagementFactory.THREAD_MXBEAN_NAME, "ThreadCount", //$NON-NLS-1$
        // orange);

        group = server.addMonitoredAttributeGroup("CPU Usage", AxisUnit.Percent); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "ProcessCpuTime", green); //$NON-NLS-1$
    }

    /**
     * Loads the memory chart set.
     *
     * @throws JvmCoreException
     */
    private void loadMemoryChartSet() throws JvmCoreException {
        final int[] blue = new int[] { 0, 0, 255 };
        final int[] green = new int[] { 0, 255, 0 };
        IMBeanServer server = graphComposite.getJvm().getMBeanServer();
        server.getMonitoredAttributeGroups().clear();

        IMonitoredMXBeanGroup group = server.addMonitoredAttributeGroup("Heap Memory", AxisUnit.MBytes); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "HeapMemoryUsage.used", blue); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "HeapMemoryUsage.committed", green); //$NON-NLS-1$

        group = server.addMonitoredAttributeGroup("Non-Heap Memory", AxisUnit.MBytes); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "NonHeapMemoryUsage.used", blue); //$NON-NLS-1$
        group.addAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "NonHeapMemoryUsage.committed", green); //$NON-NLS-1$

        group = server.addMonitoredAttributeGroup("Heap Memory Pool", AxisUnit.MBytes); //$NON-NLS-1$
        for (String objectName : getMemoryPoolObjectNames(HEAP_MEMORYPOOL_MXBEANS)) {
            group.addAttribute(objectName, "Usage.used", blue); //$NON-NLS-1$
        }

        group = server.addMonitoredAttributeGroup("Non-Heap Memory Pool", AxisUnit.MBytes); //$NON-NLS-1$
        for (String objectName : getMemoryPoolObjectNames(NON_HEAP_MEMORYPOOL_MXBEANS)) {
            group.addAttribute(objectName, "Usage.used", blue); //$NON-NLS-1$
        }
    }

    /**
     * Gets the memory pool object names that contains the given filter text.
     *
     * @param filters The filters
     * @return The memory pool object names
     * @throws JvmCoreException
     */
    private Set<String> getMemoryPoolObjectNames(String[] filters) throws JvmCoreException {
        Set<String> objectNames = new HashSet<String>();
        try {
            for (ObjectName objectName : graphComposite.getJvm().getMBeanServer()
                    .queryNames(ObjectName.getInstance("java.lang:type=MemoryPool,name=*"))) { //$NON-NLS-1$
                String canonicalName = objectName.getCanonicalName();
                for (String filter : filters) {
                    if (Pattern.compile(filter).matcher(canonicalName).find()) {
                        objectNames.add(canonicalName);
                    }
                }
            }
        } catch (MalformedObjectNameException e) {
            Activator.log(IStatus.ERROR, Messages.getMemoryPoolAttributeFailedMsg, e);
        } catch (NullPointerException e) {
            Activator.log(IStatus.ERROR, Messages.getMemoryPoolAttributeFailedMsg, e);
        }
        return objectNames;
    }

    /**
     * Saves the given chart sets that can be subset of previous chart sets.
     *
     * @param chartSets The chart sets
     * @throws WorkbenchException
     * @throws IOException
     */
    private void saveChartSets(List<String> chartSets) throws WorkbenchException, IOException {
        IMemento oldChartSetsMemento = getChartSetsMemento();
        IMemento[] oldMementos;
        if (oldChartSetsMemento == null) {
            oldMementos = new IMemento[0];
        } else {
            oldMementos = oldChartSetsMemento.getChildren(CHART_SET);
        }

        XMLMemento chartSetsMemento = XMLMemento.createWriteRoot(CHART_SETS);
        for (String chartSet : chartSets) {
            for (IMemento memento : oldMementos) {
                if (chartSet.equals(memento.getID())) {
                    chartSetsMemento.createChild(CHART_SET).putMemento(memento);
                    break;
                }
            }
        }

        StringWriter writer = new StringWriter();
        chartSetsMemento.save(writer);
        Activator.getDefault().getPreferenceStore().setValue(CHART_SETS, writer.getBuffer().toString());
    }

    /**
     * Gets the RGB integer array corresponding to the given RGB string.
     *
     * @param rgbString The RGB string "r,g,b" (e.g. "225,225,0")
     * @return The RGB integer array
     */
    private static int[] getRGB(String rgbString) {
        String[] elements = rgbString.split(","); //$NON-NLS-1$
        int[] rgb = new int[3];

        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = Integer.valueOf(elements[i]);
        }

        return rgb;
    }
}
