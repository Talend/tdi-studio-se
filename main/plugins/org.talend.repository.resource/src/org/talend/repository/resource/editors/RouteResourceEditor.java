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
package org.talend.repository.resource.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.resource.editors.input.RouteResourceInput;

/**
 * @author xpli
 *
 */
public class RouteResourceEditor extends TextEditor {

    public static final String ID = "org.talend.repository.resource.editors.RouteResourceEditor";

	private RouteResourceInput rrInput;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Item item = rrInput.getItem();
		String displayName = item.getProperty().getDisplayName();
		String version = item.getProperty().getVersion();
		String partName = "Resource " + displayName + " " + version;
		setPartName(partName);
		setTitleToolTip(partName);
		getSourceViewer().setEditable(!rrInput.isReadOnly());
	}

	@Override
	public boolean isEditable() {
		return !rrInput.isReadOnly();
	}

	@Override
	public void dispose() {
		super.dispose();

		try {
			ProxyRepositoryFactory.getInstance().unlock(rrInput.getItem());
		} catch (Exception e) {
		    ExceptionHandler.process(e);
		}
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);

        ResourceItem item = (ResourceItem) rrInput.getItem();
        saveProcess(item, progressMonitor);
	}

    private static boolean saveProcess(ResourceItem item, final IProgressMonitor monitor) {
        try {
            if (monitor != null) {
                monitor.beginTask("save process", 100); //$NON-NLS-1$
            }
            IRepositoryService service = CoreRuntimePlugin.getInstance().getRepositoryService();
            final IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            RepositoryWorkUnit rwu = new RepositoryWorkUnit("save process : ") {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    factory.save(item);
                }
            };
            rwu.setAvoidUnloadResources(false);
            rwu.setAvoidSvnUpdate(false);
            rwu.setAvoidUpdateLocks(false);
            factory.executeRepositoryWorkUnit(rwu);
            rwu.throwPersistenceExceptionIfAny();
            if (monitor != null) {
                monitor.worked(50);
            }
            if (monitor != null) {
                monitor.worked(10);
            }
            return true;
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
            if (monitor != null) {
                monitor.setCanceled(true);
            }
            return false;
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		this.rrInput = (RouteResourceInput) input;
	}

	/*
	 * Change the background if readonly
	 */
	@Override
	protected void initializeViewerColors(ISourceViewer viewer) {
		super.initializeViewerColors(viewer);
		if(!isEditable()){
			StyledText textWidget = viewer.getTextWidget();
			textWidget.setBackground(textWidget.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
	}
}
