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
package org.talend.repository.preference.export;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.repository.i18n.Messages;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public abstract class AbstractScriptPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private StyledText scriptTxt;

    private boolean isDefaultPresentedForScriptTxt = false;

    public AbstractScriptPreferencePage(IPreferenceStore preferenceStore) {
        super(FLAT);
        setPreferenceStore(preferenceStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite p) {
        Composite parent = (Composite) super.createContents(p);

        Label scriptLabel = new Label(parent, SWT.NONE);
        scriptLabel.setText(getHeadTitle());

        scriptTxt = new StyledText(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.heightHint = 450;
        layoutData.minimumHeight = 450;
        layoutData.widthHint = 550;
        layoutData.minimumWidth = 550;
        scriptTxt.setLayoutData(layoutData);
        scriptTxt.setText(getPreferenceStore().getString(getPreferenceKey()));

        return parent;
    }

    protected String getHeadTitle() {
        return Messages.getString("AbstractScriptPreferencePage_scriptTitle"); //$NON-NLS-1$
    }

    protected abstract String getPreferenceKey();

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
        if (scriptTxt != null && !scriptTxt.isDisposed()) {
            isDefaultPresentedForScriptTxt = true;
            scriptTxt.setText(getPreferenceStore().getDefaultString(getPreferenceKey()));
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
        if (scriptTxt != null && !scriptTxt.isDisposed()) {
            if (isDefaultPresentedForScriptTxt) {
                getPreferenceStore().setToDefault(getPreferenceKey());
            } else {
                getPreferenceStore().setValue(getPreferenceKey(), scriptTxt.getText());
            }
            isDefaultPresentedForScriptTxt = false;
        }
        return ok;
    }

}
