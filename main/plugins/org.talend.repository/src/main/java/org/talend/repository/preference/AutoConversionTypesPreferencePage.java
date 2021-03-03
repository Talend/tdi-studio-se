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
package org.talend.repository.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryPreferenceStore;

/**
 *
 * created by hcyi on Aug 18, 2016 Detailled comment
 *
 */
public class AutoConversionTypesPreferencePage extends FieldEditorPreferencePage {

    public AutoConversionTypesEditor editor = null;

    public AutoConversionTypesPreferencePage() {
        super(FLAT);
    }

    @Override
    protected void createFieldEditors() {
        editor = new AutoConversionTypesEditor(AutoConversionTypesEditor.ID, getFieldEditorParent());
        addField(editor);
    }

    @Override
    protected void initialize() {
        try {
            super.initialize();
        } catch (RuntimeException e) {
            ExceptionHandler.process(e);
            setErrorMessage(e.getMessage());
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

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        RepositoryPreferenceStore preferenceStore = new RepositoryPreferenceStore(ProxyRepositoryFactory.getInstance());
        try {
            preferenceStore.load();
        } catch (PersistenceException e) {
            String detailError = e.getMessage();
            new ErrorDialogWidthDetailArea(DisplayUtils.getDefaultShell(false), RepositoryPlugin.PLUGIN_ID,
                    Messages.getString("CommonWizard.persistenceException"), detailError); //$NON-NLS-1$
        }
        return preferenceStore;
    }
}