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
package org.talend.spagic.engines.client.ui.preferences;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.core.model.properties.SpagoBiServer;
import org.talend.spagic.engines.client.Activator;

/**
 * Preference for the SpagoBiServer values.
 *
 * $Id: SpagoBiPreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
 *
 */
public class SpagicPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    protected static Logger log = Logger.getLogger(SpagicPreferencePage.class);

    protected CheckBoxFieldEditor spagicCheckButton;

    protected SpagicServerEditor editor;

    private Composite parent;

    /**
     * SpagoBiPreferencePage.
     *
     * $Id: SpagoBiPreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
     *
     */

    public SpagicPreferencePage() {
        super(FLAT);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /*
     * @Override protected IPreferenceStore doGetPreferenceStore() { // SpagoPreferenceStore preferenceStore = new
     * SpagoPreferenceStore(ProxyRepositoryFactory.getInstance()); // try { // preferenceStore.load(); // } catch
     * (PersistenceException e) { // String detailError = e.getMessage(); // new
     * ErrorDialogWidthDetailArea(DisplayUtils.getDefaultShell(false),
     * RepositoryPlugin.PLUGIN_ID, Messages // .getString("CommonWizard.persistenceException"), detailError);
     * //$NON-NLS-1$ // log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError);
     * //$NON-NLS-1$ // //$NON-NLS-2$ // } // return preferenceStore; return
     * Activator.getDefault().getPreferenceStore(); }
     */

    @Override
    protected void createFieldEditors() {
        parent = getFieldEditorParent();

        spagicCheckButton = new CheckBoxFieldEditor(SpagicPreferenceInitializer.SPAGIC_STATUS,
                "Enable/Disable Deploy on Spagic", parent); //$NON-NLS-1$
        editor = new SpagicServerEditor(SpagoBiServer.SPAGOBI_SERVER, "Spagic Server (Deprecated)", parent); //$NON-NLS-1$
        addField(editor);

        updateEnableStateFromPreferences();

        SelectionListener listener = new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                IPreferenceStore preferenceStore = getPreferenceStore();
                preferenceStore.setValue(SpagicPreferenceInitializer.SPAGIC_STATUS, ((Button) e.getSource()).getSelection());
                updateEnableStateFromDisplay();
            }
        };
        spagicCheckButton.getButton().addSelectionListener(listener);

        // disable the spagic feature
        // spagicCheckButton.setEnabled(false, parent);
    }

    private void updateEnableStateFromDisplay() {
        boolean spago = spagicCheckButton.getBooleanValue();
        editor.setEnabled(spago, parent);
    }

    private void updateEnableStateFromPreferences() {
        IPreferenceStore preferenceStore = getPreferenceStore();
        boolean spago = preferenceStore.getBoolean(SpagicPreferenceInitializer.SPAGIC_STATUS);
        spagicCheckButton.getButton().setSelection(spago);
        editor.setEnabled(spago, parent);
    }

    @Override
    protected void initialize() {
        try {
            super.initialize();
        } catch (RuntimeException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            setErrorMessage(e.getMessage());
            log.error(e);
        }
    }

    @Override
    protected void checkState() {
        if (getErrorMessage() == null) {
            super.checkState();
        } else {
            setValid(false);
        }
    }

    public void init(IWorkbench workbench) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        updateEnableStateFromDisplay();
    }

}
