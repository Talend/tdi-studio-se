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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.CorePlugin;
import org.talend.core.i18n.Messages;
import org.talend.core.prefs.ITalendCorePrefConstants;

/**
 * @deprecated moved to AppearancePreferencePage
 */
@Deprecated
public class ContextPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ContextPreferencePage() {
        super(GRID);
        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(ITalendCorePrefConstants.CONTEXT_GROUP_BY_SOURCE,
                Messages.getString("CorePreferencePage.groupBySource"), //$NON-NLS-1$
                getFieldEditorParent()));

    }

    public void init(IWorkbench workbench) {

    }

    @Override
    public void dispose() {
        super.dispose();
        // TDI-21143 : Studio repository view : remove all refresh call to repo view
        // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
        // if (view != null) {
        // view.refresh();
        // }
    }

    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (ok) {
            CorePlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
        return ok;
    }
}
