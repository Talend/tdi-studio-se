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

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.RGB;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.ISharedImages;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.MBean.IMonitoredMXBeanAttribute;
import org.talend.designer.runtime.visualization.MBean.IMonitoredMXBeanGroup;
import org.talend.designer.runtime.visualization.MBean.IMonitoredMXBeanGroup.AxisUnit;
import org.talend.designer.runtime.visualization.views.RuntimeGraphcsComposite;

/**
 * created by ldong on Apr 7, 2015 Detailled comment
 *
 */
public class ConfigChartAction extends Action {

    /** The chart. */
    private TimelineChart chart;

    /** The property section. */
    private RuntimeGraphcsComposite runtimeComposite;

    /**
     * The constructor.
     *
     * @param chart The chart
     * @param section The property section
     */
    public ConfigChartAction(TimelineChart chart, RuntimeGraphcsComposite composite) {
        this.chart = chart;
        this.runtimeComposite = composite;

        setText(Messages.configureChartLabel);
        setImageDescriptor(Activator.getImageDescriptor(ISharedImages.CONFIGURE_IMG_PATH));
        setId(getClass().getName());
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        IActiveJvm jvm = runtimeComposite.getJvm();
        if (jvm == null) {
            return;
        }

        String title = chart.getSection().getText();
        IMonitoredMXBeanGroup group = chart.getAttributeGroup();
        AxisUnit unit = group.getAxisUnit();
        List<MBeanAttribute> attributes = getAttributes();

        ConfigureChartDialog dialog = new ConfigureChartDialog(chart.getShell(), title, unit, attributes, jvm, true);

        if (dialog.open() == Window.OK) {
            performConfiguration(dialog.getChartTitle(), dialog.getAxisUnit(), dialog.getAttributes(),
                    dialog.getRemovedAttributes());
        }
    }

    /**
     * Performs the configuration.
     *
     * @param chartTitle The chart title
     * @param axisUnit The axis unit
     * @param attributes The attributes
     * @param removedAttributes The removed attributes
     */
    private void performConfiguration(String chartTitle, AxisUnit axisUnit, List<MBeanAttribute> attributes,
            List<MBeanAttribute> removedAttributes) {
        IMonitoredMXBeanGroup group = chart.getAttributeGroup();

        group.setName(chartTitle);
        group.setAxisUnit(axisUnit);

        for (MBeanAttribute attribute : attributes) {
            ObjectName objectName = attribute.getObjectName();
            String attributeName = attribute.getAttributeName();
            IMonitoredMXBeanAttribute monitoredAttribute = group.getAttribute(objectName, attributeName);
            RGB rgb = attribute.getRgb();
            if (monitoredAttribute == null) {
                try {
                    group.addAttribute(objectName.getCanonicalName(), attributeName, new int[] { rgb.red, rgb.green, rgb.blue });
                } catch (JvmCoreException e) {
                    Activator.log(Messages.addAttributeFailedMsg, e);
                }
            } else {
                monitoredAttribute.setRGB(rgb.red, rgb.green, rgb.blue);
            }
        }

        for (MBeanAttribute removedAttribute : removedAttributes) {
            group.removeAttribute(removedAttribute.getObjectName().getCanonicalName(), removedAttribute.getAttributeName());
        }
        chart.refresh();
    }

    /**
     * Gets the attributes.
     *
     * @return The attributes
     */
    private List<MBeanAttribute> getAttributes() {
        List<MBeanAttribute> attributes = new ArrayList<MBeanAttribute>();
        for (IMonitoredMXBeanAttribute attribute : chart.getAttributeGroup().getAttributes()) {
            ObjectName objectName = attribute.getObjectName();
            String attributeName = attribute.getAttributeName();
            int[] rgb = attribute.getRGB();
            attributes.add(new MBeanAttribute(objectName, attributeName, new RGB(rgb[0], rgb[1], rgb[2])));
        }
        return attributes;
    }

}
