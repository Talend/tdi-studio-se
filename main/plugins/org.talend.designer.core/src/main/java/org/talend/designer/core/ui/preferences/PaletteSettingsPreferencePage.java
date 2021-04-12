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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.UpdateHelpIndexJob;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class PaletteSettingsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    protected IntegerFieldEditor recentlyUsedListSize;

    protected CheckBoxFieldEditor shouldSearchFromHelp;

    protected IntegerFieldEditor resultLimitFromHelp;

    protected static final int RECENTLY_USED_LIST_SIZE_DEFAULT = 12;

    protected static final boolean SEARCH_FROM_HELP_DEFAULT = true;

    protected static final int SEARCH_RESULT_LIMIT_FROM_HELP_DEFAULT = 10;

    protected boolean originalSearchFromHelpValue;

    public PaletteSettingsPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
        originalSearchFromHelpValue = getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP);
    }

    public static int getPaletteSearchResultLimitFromHelp() {
        return DesignerPlugin.getDefault().getPreferenceStore()
                .getInt(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_RESULT_LIMIT_FROM_HELP);
    }

    public static boolean isPaletteSearchFromHelp() {
        return DesignerPlugin.getDefault().getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP);
    }

    public static int getPaletteRencentlyUsedListSize() {
        return DesignerPlugin.getDefault().getPreferenceStore()
                .getInt(TalendDesignerPrefConstants.PALETTE_SETTINGS_RECENTLY_USED_LIST_SIZE);
    }

    @Override
    protected void createFieldEditors() {
        recentlyUsedListSize = new IntegerFieldEditor(TalendDesignerPrefConstants.PALETTE_SETTINGS_RECENTLY_USED_LIST_SIZE,
                Messages.getString("PaletteSettingsPreferencePage.paletteSettingsRecentlyUsedListSize"), //$NON-NLS-1$
                getFieldEditorParent());
        recentlyUsedListSize.setValidRange(1, 1000);
        addField(recentlyUsedListSize);

        shouldSearchFromHelp = new CheckBoxFieldEditor(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP,
                Messages.getString("PaletteSettingsPreferencePage.paletteSettingsSearchFromHelp"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(shouldSearchFromHelp);

        resultLimitFromHelp = new IntegerFieldEditor(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_RESULT_LIMIT_FROM_HELP,
                Messages.getString("PaletteSettingsPreferencePage.paletteSettingsSearchResultLimitFromHelp"), //$NON-NLS-1$
                getFieldEditorParent());
        resultLimitFromHelp.setValidRange(1, 1000);
        addField(resultLimitFromHelp);

        initStatus();
        initListeners();
    }

    @Override
    protected void performApply() {
        super.performApply();
        if (originalSearchFromHelpValue == false
                && getPreferenceStore().getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP)) {
            new UpdateHelpIndexJob().schedule();
        }
    }

    @Override
    public boolean performOk() {
        boolean performResult = super.performOk();
        if (originalSearchFromHelpValue == false
                && getPreferenceStore().getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP)) {
            new UpdateHelpIndexJob().schedule();
        }
        return performResult;
    }

    protected void initStatus() {
        resultLimitFromHelp.setEnabled(
                getPreferenceStore().getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP),
                getFieldEditorParent());
    }

    protected void initListeners() {
        shouldSearchFromHelp.getButton().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                resultLimitFromHelp.setEnabled(shouldSearchFromHelp.getBooleanValue(), getFieldEditorParent());
            }
        });
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        resultLimitFromHelp.setEnabled(
                getPreferenceStore().getBoolean(TalendDesignerPrefConstants.PALETTE_SETTINGS_SEARCH_FROM_HELP),
                getFieldEditorParent());
    }

}
