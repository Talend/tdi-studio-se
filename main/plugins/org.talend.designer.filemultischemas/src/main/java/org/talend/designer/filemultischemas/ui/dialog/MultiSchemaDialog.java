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
package org.talend.designer.filemultischemas.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.filemultischemas.MultiSchemasMain;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasUIProperties;
import org.talend.designer.filemultischemas.managers.UIManager;
import org.talend.designer.filemultischemas.ui.MultiSchemasUI;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemaDialog extends Dialog implements MultiSchemaEventListener {

    private String title;

    private Rectangle size;

    private Image icon;

    private boolean maximized;

    private MultiSchemasMain multiSchemasMain;

    private MultiSchemasUI multiSchemaUI;

    public MultiSchemaDialog(Shell parentShell, MultiSchemasMain multiSchemasMain) {
        super(parentShell);
        this.multiSchemasMain = multiSchemasMain;
        setShellStyle(ExternalMultiSchemasUIProperties.DIALOG_STYLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(panel);

        multiSchemaUI = new MultiSchemasUI(panel, this.multiSchemasMain.getMultiSchemaManager());
        multiSchemaUI.addListener(this);
        multiSchemaUI.init();

        return panel;
    }

    public MultiSchemasUI getMultiSchemaUI() {
        return this.multiSchemaUI;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (title != null) {
            newShell.setText(title);
        }
        if (icon != null) {
            newShell.setImage(icon);
        }
        if (maximized) {
            newShell.setMaximized(true);
            newShell.setBounds(size);
        } else {
            newShell.setBounds(size);
        }
    }

    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the size.
     *
     * @param size the size to set
     */
    public void setSize(Rectangle size) {
        this.size = size;
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(Image icon) {
        this.icon = icon;
    }

    /**
     * Sets the maximizedSize.
     *
     * @param maximizedSize the maxmimizedSize to set
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    private UIManager getUIManager() {
        return multiSchemasMain.getMultiSchemaManager().getUIManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        getMultiSchemaUI().saveProperties();
        getMultiSchemaUI().prepareClosing(SWT.OK);
        super.okPressed();

    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        getUIManager().setDialogResponse(SWT.CANCEL);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.designer.filemultischemas.ui.dialog.MultiSchemaEventListener#checkPerformed(org.talend.designer.
     * filemultischemas.ui.dialog.MultiSchemaDialog)
     */
    public void checkPerformed(boolean enable) {
        final Button okBtn = getButton(IDialogConstants.OK_ID);
        if (okBtn != null) {
            okBtn.setEnabled(enable);
        }
    }

}
