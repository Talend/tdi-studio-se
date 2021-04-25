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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.PluginChecker;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC achen class global comment. Detailled comment
 */
public class RepositoryPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {

        if (PluginChecker.isRefProjectLoaded()) {

            addField(new BooleanFieldEditor(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT,
                    Messages.getString("PerformancePreferencePage.RepositoryPreferencePage.ReferenceProjectMerged"),//$NON-NLS-1$
                    getFieldEditorParent()));
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(RepositoryManager.getRepositoryPreferenceStore());
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

}
