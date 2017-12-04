// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;

/**
 * Login dialog. <br/>
 * 
 * $Id: LoginDialog.java,v 1.6 2007/04/04 11:12:12 pub Exp $
 * 
 */
public class ProjectSelectionDialog extends TitleAreaDialog {
	private Text directoryText;

	//    private Button fileButton;
	//
	//    private Button dbButton;

	/**
	 * Construct a new LoginDialog.
	 * 
	 * @param parentShell Parent shell.
	 */
	public ProjectSelectionDialog(Shell parentShell) {
		super(parentShell);
		//        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
		//                IBrandingService.class);
		//        ImageDescriptor imgDesc = brandingService.getLoginHImage();
		//        if (imgDesc != null) {
		//            setTitleImage(imgDesc.createImage());
		//        }
		// RGB rgb = parentShell.getBackground().getRGB();
		RGB rgb = new RGB(255, 255, 255);
		setTitleAreaColor(rgb);
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Component Designer"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		ComponentDesigenerPlugin.getDefault().getPreferenceStore().setValue(PluginConstant.PROJECT_URL, this.directoryText.getText());
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(final Composite parent) {
		Composite loginComposite = new Composite(parent, SWT.NONE);
		loginComposite.setLayout(new GridLayout(9, false));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 350;
		loginComposite.setLayoutData(data);

		Label label = new Label(loginComposite, SWT.None);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("ProjectSelectionDialog.ComponentProject")); //$NON-NLS-1$

		directoryText = new Text(loginComposite, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 6;
		directoryText.setLayoutData(gridData);

		Button browserButton = new Button(loginComposite, SWT.None);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		browserButton.setLayoutData(gridData);
		browserButton.setText(PluginConstant.BROWSER_LABEL);
		browserButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				directoryText.setText(getPathFromDialog());
			}
		});

		this.setTitle(Messages.getString("ProjectSelectionDialog.ChooseFolder")); //$NON-NLS-1$

		return loginComposite;
	}

	private String getPathFromDialog() {
		DirectoryDialog dialog = new DirectoryDialog(this.getShell());
		dialog.setMessage(Messages.getString("ProjectSelectionDialog.SelectPath")); //$NON-NLS-1$

		//		dialog.setFilterPath(dirName);
		String path = dialog.open();
		return path == null ? PluginConstant.EMPTY_STRING : path;
	}

}
