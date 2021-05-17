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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.navigator.RepoViewCommonNavigator;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class TalendWizardArchiveFileResourceExportPage2 extends WizardArchiveFileResourceExportPage1 {

    /**
     * DOC guanglong.du TalendWizardArchiveFileResourceExportPage2 constructor comment.
     *
     * @param selection
     */
    public TalendWizardArchiveFileResourceExportPage2(IStructuredSelection selection) {
        super(selection);
        // TODO Auto-generated constructor stub
    }

    /**
     * DOC guanglong.du TalendWizardArchiveFileResourceExportPage2 constructor comment.
     *
     * @param name
     * @param selection
     */
    public TalendWizardArchiveFileResourceExportPage2(String name, IStructuredSelection selection) {
        super(name, selection);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected boolean saveDirtyEditors() {
        boolean result = false;
        IRepositoryView repView = RepositoryManagerHelper.findRepositoryView();
        if (repView instanceof RepoViewCommonNavigator) {
            ((RepoViewCommonNavigator) repView).setShouldCheckRepositoryDirty(false);
            result = super.saveDirtyEditors();
            ((RepoViewCommonNavigator) repView).setShouldCheckRepositoryDirty(true);
        } else {
            return super.saveDirtyEditors();
        }

        return result;
    }

    @Override
    public boolean finish() {

        if (!ensureTargetIsValid()) {
            return false;
        }

        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        final List<Boolean> results = new ArrayList<Boolean>(1);
        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().executeRepositoryWorkUnit(new RepositoryWorkUnit("refresh") {

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
                Display.getCurrent().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        List resourcesToExport = getWhiteCheckedResources();
                        boolean r = executeExportOperation(new ArchiveFileExportOperation(null, resourcesToExport,
                                getDestinationValue()));
                        results.add(r);
                    }
                });
            }

        });

        return results.size() == 1 && results.get(0) == true;
    }
}
