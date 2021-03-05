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
package org.talend.repository.resource.editors.input;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.talend.core.model.properties.Item;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.resource.ui.util.RouteResourceEditorUtil;
import org.talend.repository.resource.util.JobResourceUtil;

/**
 * @author xpli
 *
 */
public class RouteResourceInput extends RepositoryEditorInput {

	private IResourceChangeListener listener;

	protected RouteResourceInput(IFile file, Item item) {
		super(file, item);
	}

	/**
	 * Create instance
	 *
	 * @param item
	 * @return
	 */
    public static RouteResourceInput createInput(IRepositoryNode node, ResourceItem item) {
		RouteResourceInput routeResourceInput = new RouteResourceInput(
                JobResourceUtil.getSourceFile(item), item);
		if(node!=null) {
			routeResourceInput.setRepositoryNode(node);
			routeResourceInput.setReadOnly(RouteResourceEditorUtil.isReadOnly(node));
		}
		return routeResourceInput;
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(IResourceChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * @return the listener
	 */
	public IResourceChangeListener getListener() {
		return listener;
	}

	public String getName() {
		String label = (getItem() == null ? "" : getItem().getProperty()
				.getLabel());
		String version = (getItem() == null ? "0.1" : getItem().getProperty()
				.getVersion());
		return label + " " + version+ (isReadOnly()?" (ReadOnly)":"");
	}
}
