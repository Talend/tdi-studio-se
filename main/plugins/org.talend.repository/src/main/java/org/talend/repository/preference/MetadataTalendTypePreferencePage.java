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
import org.talend.metadata.managment.ui.editor.MetadataTalendTypeEditor;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryPreferenceStore;

/**
 * Preference for the Metadata Talend type files.
 *
 * $Id: MetadataTalendTypePreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
 *
 */
public class MetadataTalendTypePreferencePage extends FieldEditorPreferencePage {

    /**
     * MetadataTalendTypePreferencePage.
     *
     * $Id: SpagoBiPreferencePage.java 2738 2007-04-26 13:12:27Z cantoine $
     *
     */

    public MetadataTalendTypePreferencePage() {
        super(FLAT);
    }

    @Override
    protected void createFieldEditors() {
        addField(new MetadataTalendTypeEditor(MetadataTalendTypeEditor.ID, "Talend Type Mapping Files", getFieldEditorParent())); //$NON-NLS-1$
    }

    @Override
    protected void initialize() {
        try {
            super.initialize();
        } catch (RuntimeException e) {
            // e.printStackTrace();
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
                    Messages
                    .getString("CommonWizard.persistenceException"), detailError); //$NON-NLS-1$
        }
        return preferenceStore;
    }
}
