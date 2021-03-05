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
package org.talend.designer.abstractmap.ui.prefs;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.utils.workbench.preferences.ComboFieldEditor;
import org.talend.designer.abstractmap.MapPlugin;
import org.talend.designer.abstractmap.i18n.Messages;
import org.talend.designer.abstractmap.ui.properties.LINK_STYLE;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * @deprecated moved to ComponentsPreferencePage
 */
public class MapPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * This class exists to provide visibility to the <code>refreshValidState</code> method and to perform more
     * intelligent clearing of the error message.
     *
     *
     */
    protected class ConsoleIntegerFieldEditor extends IntegerFieldEditor {

        public ConsoleIntegerFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        /**
         * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
         */
        protected void refreshValidState() {
            super.refreshValidState();
        }

        /**
         * Clears the error message from the message line if the error message is the error message from this field
         * editor.
         */
        protected void clearErrorMessage() {
            if (canClearErrorMessage()) {
                super.clearErrorMessage();
            }
        }
    }

    // private BooleanFieldEditor2 fWrapEditor = null;
    private IntegerFieldEditor fClientComPortEditor = null;

    private ConsoleIntegerFieldEditor fTabSizeEditor = null;

    private Group clientGroup;

    private StringFieldEditor fClientHostEditor;

    private IntegerFieldEditor fClientStatPortEditor;

    private IntegerFieldEditor fClientTraceEditor;

    private Group remoteServersGroup;

    /**
     * Create the console page.
     */
    public MapPreferencePage() {
        super(GRID);
        // setDescription(Messages.getString("prefs.configuration.title"));
        setPreferenceStore(MapPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return MapPlugin.getDefault().getPreferenceStore();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(
        // getControl(),
        // IDebugHelpContextIds.CONSOLE_PREFERENCE_PAGE );
    }

    /**
     * Create all field editors for this page
     */
    public void createFieldEditors() {

        LINK_STYLE[] linkStyles = LINK_STYLE.values();
        String[][] strComboValues = new String[linkStyles.length][2];

        for (int i = 0; i < linkStyles.length; i++) {
            strComboValues[i][0] = linkStyles[i].getDisplayName();
            strComboValues[i][1] = linkStyles[i].getName();
        }

        ComboFieldEditor dbTypeField = new ComboFieldEditor(MapPrefsConstants.LINK_STYLE, Messages
                .getString("prefs.configuration.LINK_STYLE"), strComboValues, getFieldEditorParent()); //$NON-NLS-1$

        addField(dbTypeField);

    }

    protected void createSpacer(Composite composite, int columnSpan) {
        Label label = new Label(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = columnSpan;
        label.setLayoutData(gd);
    }

    /**
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        boolean ok = super.performOk();
        // update high water mark to be (about) 100 lines (100 * 80 chars) greater than low water mark
        // IPreferenceStore store = MapPlugin.getDefault().getPreferenceStore();
        // int low = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
        // int high = low + 8000;
        // store.setValue(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK, high);
        MapPlugin.getDefault().savePluginPreferences();
        return ok;
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
     */
    protected void initialize() {
        super.initialize();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        super.performDefaults();
    }

    protected boolean canClearErrorMessage() {
        // if (fClientComPortEditor.isValid() && fBufferSizeEditor.isValid()) {
        // return true;
        // }
        // return false;
        return true;
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getProperty().equals(FieldEditor.IS_VALID)) {
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            // If the new value is true then we must check all field editors.
            // If it is false, then the page is invalid in any case.
            if (newValue) {
                // if (fClientComPortEditor != null && event.getSource() != fClientComPortEditor) {
                // // fClientComPortEditor.refreshValidState();
                // }
                checkState();
            } else {
                super.propertyChange(event);
            }

        } else {
            super.propertyChange(event);
        }
    }

}
