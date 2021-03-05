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
package org.talend.designer.core.ui;

import org.eclipse.ui.IViewPart;
import org.talend.core.model.process.IProcess2;

/**
 * The standard job hierarchy view presents a job hierarchy for a given input Job. Visually, this view consists of a
 * pair of viewers, one showing the job hierarchy, the other showing the members of the job selected in the first.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IJobHierarchyViewPart extends IViewPart {

    /**
     * Constant used for the vertical view layout.
     *
     */
    public static final int VIEW_LAYOUT_VERTICAL = 0;

    /**
     * Constant used for the horizontal view layout.
     *
     */
    public static final int VIEW_LAYOUT_HORIZONTAL = 1;

    /**
     * Constant used for the single view layout (no members view)
     *
     */
    public static final int VIEW_LAYOUT_SINGLE = 2;

    /**
     * Constant used for the automatic view layout.
     *
     */
    public static final int VIEW_LAYOUT_AUTOMATIC = 3;

    /**
     * Constant used for the 'classic' type hierarchy mode.
     *
     */
    public static final int HIERARCHY_MODE_CLASSIC = 2;

    /**
     * Constant used for the super types hierarchy mode.
     *
     */
    public static final int HIERARCHY_MODE_SUPERTYPES = 1;

    /**
     * Constant used for the sub types hierarchy mode.
     *
     */
    public static final int HIERARCHY_MODE_SUBTYPES = 0;

    /**
     * Sets the hierarchy mode. Valid modes are {@link #HIERARCHY_MODE_SUBTYPES}, {@link #HIERARCHY_MODE_SUPERTYPES} and
     * {@link #HIERARCHY_MODE_CLASSIC}.
     *
     * @param mode The hierarchy mode to set
     *
     */
    public void setHierarchyMode(int mode);

    /**
     * Returns the currently configured hierarchy mode. Possible modes are {@link #HIERARCHY_MODE_SUBTYPES},
     * {@link #HIERARCHY_MODE_SUPERTYPES} and {@link #HIERARCHY_MODE_CLASSIC} but clients should also be able to handle
     * yet unknown modes.
     *
     * @return The hierarchy mode currently set
     *
     */
    public int getHierarchyMode();

    public void setInputProcess(IProcess2 process);

    public IProcess2 getInputProcess();

    /**
     * DOC bqian Comment method "setViewLayout".
     *
     * @param actionOrientation
     */
    public void setViewLayout(int actionOrientation);

}
