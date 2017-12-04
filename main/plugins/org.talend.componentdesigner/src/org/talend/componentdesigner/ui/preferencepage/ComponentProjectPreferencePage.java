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
package org.talend.componentdesigner.ui.preferencepage;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class ComponentProjectPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private DirectoryFieldEditor filePathTemp;

    /**
     * DOC slanglois ComponentProjectPreferencePage constructor comment.
     */
    public ComponentProjectPreferencePage() {
        super(GRID);
        setPreferenceStore(ComponentDesigenerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    protected void performApply() {
        super.performApply();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    public void createFieldEditors() {
        Label l = new Label(getFieldEditorParent(), SWT.NONE);
        l.setText(Messages.getString("ComponentProjectPreferencePage.ChooseProject")); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        l.setLayoutData(gd);
        filePathTemp = new DirectoryFieldEditor(PluginConstant.PROJECT_URL,
                Messages.getString("ComponentProjectPreferencePage.ComponentProject"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(filePathTemp);
        // addModifyListener for the filePath text
        filePathTemp.getTextControl(getFieldEditorParent()).addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String newPath = filePathTemp.getTextControl(getFieldEditorParent()).getText();
                File file = new File(newPath);
                if (!file.exists() && !"".equals(newPath)) {
                    filePathTemp.showErrorMessage();
                    setValid(false);
                } else {
                    setValid(true);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        MessageDialog warningMessageDialog = new MessageDialog(getFieldEditorParent().getShell(),
                Messages.getString("ComponentProjectPreferencePage.Warning"), null, //$NON-NLS-1$
                Messages.getString("ComponentProjectPreferencePage.WarningMSG"), MessageDialog.WARNING, //$NON-NLS-1$
                new String[] { Messages.getString("ComponentProjectPreferencePage.ButtonLabel0") }, 0); //$NON-NLS-1$
        warningMessageDialog.open();
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
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    public boolean performOk() {
        ComponentDesigenerPlugin.getDefault().creatComponentProj(filePathTemp.getStringValue());
        return super.performOk();
    }

}
