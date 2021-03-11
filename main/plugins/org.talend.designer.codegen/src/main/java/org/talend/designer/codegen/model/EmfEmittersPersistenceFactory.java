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
package org.talend.designer.codegen.model;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.CodeGenPlugin;
import org.talend.core.language.ECodeLanguage;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public final class EmfEmittersPersistenceFactory {

    private static Logger log = Logger.getLogger(EmfEmittersPersistenceFactory.class);

    private static EmfEmittersPersistence singleton = null;

    private EmfEmittersPersistenceFactory() {
    }

    public static EmfEmittersPersistence getInstance(ECodeLanguage language) {
        if (singleton == null || !singleton.getLanguage().equals(language)) {

            final IProject project = getJetProject();
            IFile iFile = null;
            if (project != null) {
                iFile = project.getFile("JetPersistence" + language); //$NON-NLS-1$
            }
            File file = iFile.getLocation().toFile();

            singleton = new EmfEmittersPersistence(language, file);
        }

        return singleton;
    }

    /**
     * DOC mhirt Comment method "getJetProject".
     *
     * @return
     * @throws CoreException
     */
    private static IProject getJetProject() {
        IProject project = null;
        try {
            IProgressMonitor monitor = new NullProgressMonitor();
            IProgressMonitor progressMonitor = new SubProgressMonitor(monitor, 1);

            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            project = workspace.getRoot().getProject(".JETEmitters"); //$NON-NLS-1$
            if (!project.exists()) {
                project.create(new SubProgressMonitor(progressMonitor, 1));
                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreatingProject_message", //$NON-NLS-1$
                        new Object[] { project.getName() }));
                project.open(new SubProgressMonitor(progressMonitor, 1));
            }
        } catch (CoreException e) {
            log.error(e.getMessage(), e);
            project = null;
        }
        return project;
    }

}
