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
package org.talend.designer.runprocess.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class Log4jPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private StyledText templateTxt;

    private boolean reset = false;

    public Log4jPreferencePage() {
        super(FLAT);
        setPreferenceStore(RunProcessPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite p) {
        Composite parent = (Composite) super.createContents(p);

        Label headLabel = new Label(parent, SWT.NONE);
        headLabel.setText(Messages.getString("Log4jPreferencePage.title")); //$NON-NLS-1$

        templateTxt = new StyledText(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        templateTxt.setLayoutData(layoutData);
        templateTxt.setText(getPreferenceStore().getString(RunProcessPrefsConstants.LOG4J_PROPERTIES_TEMPLATE));

        return parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
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
        if (templateTxt != null && !templateTxt.isDisposed()) {
            reset = true;
            templateTxt.setText(getPreferenceStore().getDefaultString(RunProcessPrefsConstants.LOG4J_PROPERTIES_TEMPLATE));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (templateTxt != null && !templateTxt.isDisposed()) {
            if (reset) {
                getPreferenceStore().setToDefault(RunProcessPrefsConstants.LOG4J_PROPERTIES_TEMPLATE);
            } else {
                getPreferenceStore().setValue(RunProcessPrefsConstants.LOG4J_PROPERTIES_TEMPLATE, templateTxt.getText());
            }
            reset = false;
        }
        return ok;
    }

}
