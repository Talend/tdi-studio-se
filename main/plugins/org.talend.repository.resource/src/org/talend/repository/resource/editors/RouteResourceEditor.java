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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
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
//		setPartName(partName);
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

		saveContentsToItem(rrInput);

	}

	/**
	 * Save the file content to EMF item.
	 * 
	 * @param rrInput
	 */
	public static void saveContentsToItem(RouteResourceInput rrInput) {

		try {
            ResourceItem item = (ResourceItem) rrInput.getItem();

			ReferenceFileItem refFile = (ReferenceFileItem) item
					.getReferenceResources().get(0);

			InputStream inputstream = rrInput.getFile().getContents();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputstream));
			String line = bufferedReader.readLine();
			StringBuffer sb = new StringBuffer();
			String lineSeparator = System.getProperty("line.separator");
			while (line != null) {
				sb.append(line).append(lineSeparator);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			inputstream.close();

			ByteArray content = refFile.getContent();
			content.setInnerContent(sb.toString().getBytes());
		} catch (Exception e) {
		    ExceptionHandler.process(e);
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
