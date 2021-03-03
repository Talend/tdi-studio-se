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

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.resource.editors.input.RouteResourceInput;

/**
 * @author xpli
 *
 */
public class ResourceEditorListener implements IPartListener2 {

	private RouteResourceInput editorInput;

	private IWorkbenchPage page;

	public ResourceEditorListener(RouteResourceInput input, IWorkbenchPage page) {
		this.editorInput = input;
		this.page = page;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);

		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof RouteResourceInput) {
				Item item = ((RouteResourceInput) input).getItem();
				if (item.getProperty().getId()
						.equals(editorInput.getItem().getProperty().getId())) {
					try {
						ProxyRepositoryFactory.getInstance().unlock(item);
						page.getWorkbenchWindow().getPartService().removePartListener(this);
						IResourceChangeListener l = editorInput.getListener();
						if (null != l) {
                            ResourcesPlugin.getWorkspace().removeResourceChangeListener(l);
						}
					} catch (Exception e) {
					    ExceptionHandler.process(e);
					}
				}
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);

		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof RouteResourceInput) {
				Item item = ((RouteResourceInput) input).getItem();
				try {
					ProxyRepositoryFactory.getInstance().lock(item);
				} catch (Exception e) {
				    ExceptionHandler.process(e);
				}
			}
		}

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

}
