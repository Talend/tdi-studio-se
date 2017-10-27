// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.action;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.ui.composite.ILibListViewer;

/**
 * Abstract action that opens a dialog. Contains a prefix for dialog preference settings.
 */
public abstract class OpenDialogAction extends UseResourceAction {

    /**
     * Attribute name for the last path used to open a file/directory chooser dialog.
     */
    protected static final String LAST_PATH_SETTING = "LAST_PATH_SETTING"; //$NON-NLS-1$

    /**
     * Dialog settings prefix/qualifier.
     */
    private String fPrefix = null;

    /**
     * Constructs an action that opens a dialog.
     */
    public OpenDialogAction(String label, ILibListViewer viewer, String dialogSettingsPrefix) {
        super(label, viewer);
        fPrefix = dialogSettingsPrefix;
    }

    /**
     * Returns the prefix of the identifier used to store dialog settings for this action.
     */
    protected String getDialogSettingsPrefix() {
        return fPrefix;
    }

    /**
     * Returns the value of the dialog setting, associated with the given settingName, resolved by the dialog setting
     * prefix associated with this action.
     * 
     * @param settingName unqualified setting name
     * @return value or <code>null</code> if none
     */
    protected String getDialogSetting(String settingName) {
        return getDialogSettings().get(getDialogSettingsPrefix() + "." + settingName); //$NON-NLS-1$
    }

    /**
     * Sets the value of the dialog setting, associated with the given settingName, resolved by the dialog setting
     * prefix associated with this action.
     * 
     * @param settingName unqualified setting name
     * @return value or <code>null</code> if none
     */
    protected void setDialogSetting(String settingName, String value) {
        getDialogSettings().put(getDialogSettingsPrefix() + "." + settingName, value); //$NON-NLS-1$
    }

    /**
     * Returns this plug-in's dialog settings.
     * 
     * @return IDialogSettings
     */
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = ComponentDesigenerPlugin.getDefault().getDialogSettings();
        return settings;
    }

    protected int getActionType() {
        return ADD;
    }
}
