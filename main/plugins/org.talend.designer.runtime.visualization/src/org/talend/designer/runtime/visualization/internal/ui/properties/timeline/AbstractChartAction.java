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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.internal.ui.IConstants;
import org.talend.designer.runtime.visualization.views.RuntimeGraphcsComposite;

/**
 * created by ldong on Apr 7, 2015 Detailled comment
 *
 */
public class AbstractChartAction extends Action implements IConstants {

    /** The predefined overview chart set. */
    static final String OVERVIEW_CHART_SET = "Overview"; //$NON-NLS-1$

    /** The predefined memory chart set. */
    static final String MEMORY_CHART_SET = "Memory"; //$NON-NLS-1$

    RuntimeGraphcsComposite graphComposite;

    /**
     * The constructor.
     *
     * @param section The property section
     */
    public AbstractChartAction(Composite composite) {
        setId(getClass().getName());
        this.graphComposite = (RuntimeGraphcsComposite) composite;
    }

    /**
     * Gets the chart sets memento.
     *
     * @return The chart sets memento, or <tt>null</tt> if no chart sets are saved yet
     * @throws WorkbenchException
     * @throws IOException
     */
    IMemento getChartSetsMemento() throws WorkbenchException, IOException {
        String chartSetsString = Activator.getDefault().getPreferenceStore().getString(CHART_SETS);
        if (chartSetsString.isEmpty()) {
            return null;
        }
        return XMLMemento.createReadRoot(new StringReader(chartSetsString));
    }

    /**
     * Gets the chart sets stored as chart sets memento.
     *
     * @return The chart sets
     * @throws WorkbenchException
     * @throws IOException
     */
    List<String> getChartSets() throws WorkbenchException, IOException {
        List<String> elements = new ArrayList<String>();

        IMemento chartSetsMemento = getChartSetsMemento();
        if (chartSetsMemento == null) {
            return elements;
        }

        for (IMemento element : chartSetsMemento.getChildren(CHART_SET)) {
            elements.add(element.getID());
        }

        Collections.sort(elements);

        return elements;
    }

    /**
     * Gets the predefined chart sets.
     *
     * @return The predefined chart sets
     */
    List<String> getPredefinedChartSets() {
        return Arrays.asList(new String[] { OVERVIEW_CHART_SET, MEMORY_CHART_SET });
    }
}
