// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.resource.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.resource.editors.input.RouteResourceInput;

/**
 * @author xpli
 *
 */
public class RouteResoureChangeListener implements IResourceChangeListener {

    private static final String SAVING_RESOURCE = "Saving resource"; //$NON-NLS-1$

    private RouteResourceInput editorInput;

    private IFile editorFile;

    private String filePath;

    public RouteResoureChangeListener(RouteResourceInput editorInput) {
        this.editorInput = editorInput;
        this.editorInput.setListener(this);
        this.editorFile = editorInput.getFile();
        this.filePath = this.editorFile.getLocation().toPortableString();
    }

    /*
     * yyan:Updated to use work unit
     * 
     * @see
     * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent
     * )
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {

        IResourceDelta delta = event.getDelta();
        try {
            if (delta == null || editorFile == null) {
                return;
            }
            delta.accept(new IResourceDeltaVisitor() {

                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();
                    if (resource == null) {
                        return false;
                    }
                    if (resource.getType() != IResource.FILE) {
                        IPath location = resource.getLocation();
                        if (location == null || !filePath.startsWith(location.toPortableString())) {
                            return false;
                        }
                        return true;
                    }

                    if (resource.equals(editorFile)) {

                        final RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(SAVING_RESOURCE, this) {

                            @Override
                            protected void run() throws LoginException, PersistenceException {
                                RouteResourceEditor.saveContentsToItem(editorInput);
                            }
                        };
                        repositoryWorkUnit.setAvoidUpdateLocks(false);
                        repositoryWorkUnit.setAvoidUnloadResources(false);
                        repositoryWorkUnit.setUnloadResourcesAfterRun(false);
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                CoreRuntimePlugin.getInstance().getProxyRepositoryFactory()
                                        .executeRepositoryWorkUnit(repositoryWorkUnit);
                            }
                        });

                        return false;
                    }
                    return false;
                }
            }, IResource.FILE);
        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

}
