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

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.properties.Status;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryPreferenceStore;

/**
 * Preference for the status values.
 *
 * $Id$
 *
 * @deprecated
 */
public class StatusPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    protected static Logger log = Logger.getLogger(StatusPreferencePage.class);

    /**
     * StatusListEditor.
     *
     * $Id$
     *
     */

    public StatusPreferencePage() {
        super(FLAT);
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
            log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return preferenceStore;
    }

    @Override
    protected void createFieldEditors() {
        addField(new StatusEditor(Status.TECHNICAL_STATUS,
                Messages.getString("StatusPreferencePage.technicalStatusLabel"), getFieldEditorParent())); //$NON-NLS-1$
        addField(new StatusEditor(Status.DOCUMENTATION_STATUS,
                Messages.getString("StatusPreferencePage.docStatusLabel"), getFieldEditorParent())); //$NON-NLS-1$
    }

    @Override
    protected void initialize() {
        try {
            super.initialize();
        } catch (RuntimeException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            setErrorMessage(e.getMessage());
            log.error(e);
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

    public void init(IWorkbench workbench) {
    }

}
