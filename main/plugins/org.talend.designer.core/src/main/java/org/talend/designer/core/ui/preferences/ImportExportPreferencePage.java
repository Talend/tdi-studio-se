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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

public class ImportExportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ImportExportPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        CheckBoxFieldEditor isUsedCheckButton = new CheckBoxFieldEditor(IRepositoryPrefConstants.USE_EXPORT_SAVE,
                Messages.getString("ImportExportPreferencePage.exportJobScript"), getFieldEditorParent()); //$NON-NLS-1$
        addField(isUsedCheckButton);

        CheckBoxFieldEditor addClasspathJarButton = new CheckBoxFieldEditor(IRepositoryPrefConstants.ADD_CLASSPATH_JAR,
                Messages.getString("ImportExportPreferencePage.addClasspathJar"), getFieldEditorParent()); //$NON-NLS-1$
        addField(addClasspathJarButton);

        CheckBoxFieldEditor needRebuild = new CheckBoxFieldEditor(IRepositoryPrefConstants.REBUILD_RELATIONSHIPS,
                Messages.getString("ImportExportPreferencePage.rebuildRelationships"), getFieldEditorParent()); //$NON-NLS-1$
        addField(needRebuild);
    }

    @Override
    public void init(IWorkbench workbench) {

    }

}
