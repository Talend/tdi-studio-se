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
package org.talend.repository.resource.ui.wizards;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.metadata.managment.ui.wizard.AbstractNamedWizardPage;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;
import org.talend.repository.resource.i18n.Messages;

/**
 *
 * New Route Resource Wizard Page
 *
 * @author xpli
 *
 */
public class NewRouteResourceWizardPage extends PropertiesWizardPage {

	private Button browseBtn;
	private Text filenameText;
	private URL url;

	private List<IRepositoryViewObject> listExistingResources;

	/**
	 * Constructs a new NewProjectWizardPage.
	 *
	 */
	public NewRouteResourceWizardPage(Property property, IPath destinationPath) {
		super("WizardPage", property, destinationPath); //$NON-NLS-1$

        setTitle(Messages.getString("NewRouteResourceWizardPage.update.title")); //$NON-NLS-1$
		setDescription(Messages.getString("NewRouteResourceWizardPage.desc")); //$NON-NLS-1$
	}

	public NewRouteResourceWizardPage(String pageName, Property property,
			IPath destinationPath, boolean readOnly, boolean editPath,
			String lastVersionFound) {
		super(pageName, property, destinationPath, readOnly, editPath, lastVersionFound);
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		if(browseBtn != null && filenameText != null){
			browseBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
					String filename = dlg.open();
					if (filename != null) {
						filenameText.setText(filename);
					}
				}
			});
			filenameText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					File file = new File(filenameText.getText());
					String fileName = file.getName();
					if (nameText.getText().isEmpty()) {
						nameText.setText(fileName);
					}
					evaluateFields();
				}
			});
		}
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		if(!isEditPath()){
			super.createControl(container);
		}else{
			// Source file
			Label filenameLab = new Label(container, SWT.NONE);
			filenameLab.setText(Messages.getString("NewRouteResourceWizardPage.sourceFile")); //$NON-NLS-1$

			Composite filenameContainer = new Composite(container, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = layout.numColumns - 1;
			filenameContainer.setLayoutData(data);
			GridLayout filenameLayout = new GridLayout(2, false);
			filenameLayout.marginHeight = 0;
			filenameLayout.marginWidth = 0;
			filenameContainer.setLayout(filenameLayout);

			filenameText = new Text(filenameContainer, SWT.BORDER);
			data = new GridData(GridData.FILL_HORIZONTAL);
			filenameText.setLayoutData(data);

			browseBtn = new Button(filenameContainer, SWT.PUSH);
			browseBtn.setText(Messages.getString("NewRouteResourceWizardPage.browse")); //$NON-NLS-1$
			browseBtn.setToolTipText(Messages.getString("NewRouteResourceWizardPage.browseTip")); //$NON-NLS-1$

			super.createControl(container);

		}
		setControl(container);
		updateContent();
		addListeners();
		setPageComplete(false);

		nameText.selectAll();
		nameText.setFocus();
	}

	@Override
	protected void evaluateTextField() {

		String trimName = nameText.getText().trim();
		//Check name is a valid file name
		nameStatus = ResourcesPlugin.getWorkspace().validateName(trimName, IResource.FILE);
		if(!nameStatus.isOK()){
			updatePageStatus();
			return;
		} else {
			nameStatus = AbstractNamedWizardPage.createOkStatus();
			// Check name is existing or not
			if (!isValidResourceName(trimName)) {
				nameStatus = createStatus(IStatus.ERROR, Messages.getString("NewRouteResourceWizardPage.itemAlreadyExist")); //$NON-NLS-1$
				updatePageStatus();
				return;
			}
			// consider it's a valid name
			else {
				if (property != null && nameStatus.getSeverity() == IStatus.OK) {
					property.setLabel(getPropertyLabel(trimName));
					property.setDisplayName(trimName);
					property.setModificationDate(new Date());
				}
			}
		}

		if(filenameText != null){
			//Check the specified file path is valid or not
			String filePath = filenameText.getText().trim();
			// An empty file, allowed
			if (filePath != null && !filePath.isEmpty()) {
				File file = new File(filePath);
				if (!file.exists()) {
					try {
						url = new URL(filePath);
						InputStream is = url.openStream();
						if (is == null) {
							url = null;
						}
						if (is != null) {
							is.close();
						}
					} catch (Exception e) {
						url = null;
					}
				} else {
					try {
						url = file.toURI().toURL();
					} catch (Exception e) {
						url = null;
					}
				}
				if (url == null) {
					nameStatus = createStatus(IStatus.ERROR,
							Messages.getString("NewRouteResourceWizardPage.errorLoadFile") + filePath + "."); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		updatePageStatus();

	}

	private boolean isValidResourceName(String itemName){
		if(listExistingResources == null){
			listExistingResources = getListExistingObjects();
		}
		if(listExistingResources == null){
			try {
				listExistingResources = loadRepositoryViewObjectList();
			} catch (PersistenceException e) {
				listExistingResources = Collections.emptyList();
			}
		}
		for(IRepositoryViewObject object: listExistingResources){
			if(object.getProperty() == getProperty()){
				continue;
			}
			String p = object.getProperty().getItem().getState().getPath();
			if(p == null){
				continue;
			}
			if(!p.equalsIgnoreCase(getDestinationPath().toPortableString())){
				continue;
			}
			if(itemName.equalsIgnoreCase(object.getProperty().getLabel())){
				return false;
			}
		}
		return true;
	}

	public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.RESOURCES;
	}

	public URL getUrl() {
		return url;
	}

}
