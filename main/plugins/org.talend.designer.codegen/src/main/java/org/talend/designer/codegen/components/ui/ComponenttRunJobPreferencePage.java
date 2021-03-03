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
package org.talend.designer.codegen.components.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 *
 * @author ftang, 17/08, 2007<br/>
 *
 * @deprecated moved to ComponentsPreferencePage
 *
 */
public class ComponenttRunJobPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CheckBoxFieldEditor doNotShowJobAfterDoubleClickCheckBoxField;

    public static final String IS_AVOID = "isAvoidShowJobAfterDoulbleClick"; //$NON-NLS-1$

    private static final String TITLE = "tRunJob"; //$NON-NLS-1$

    public ComponenttRunJobPreferencePage() {
        super(TITLE, GRID);
        setPreferenceStore(CodeGeneratorActivator.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {

        // doNotShowJobAfterDoubleClickCheckBoxField = new CheckBoxFieldEditor(IS_AVOID, Messages
        //                .getString("ComponenttRunJobPreferencePage.label"), getFieldEditorParent()); //$NON-NLS-1$
        doNotShowJobAfterDoubleClickCheckBoxField = new CheckBoxFieldEditor(IS_AVOID,
                Messages.getString("ComponenttRunJobPreferencePage.showCorrespondingJob"), getFieldEditorParent()); //$NON-NLS-1$
        addField(doNotShowJobAfterDoubleClickCheckBoxField);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
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
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        boolean ok = super.performOk();
        CodeGeneratorActivator.getDefault().savePluginPreferences();
        return ok;
    }

}
