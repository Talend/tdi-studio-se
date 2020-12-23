// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.components.IComponent;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.managers.MapperManager;
/**
 * 
 * created by hcyi on Nov 24, 2020 Detailled comment
 *
 */
public class PropertySetDialog extends Dialog {

    private MapperManager mapperManager;

    private DbGenerationManager generationManager;

    private Button delimitedIdentifiersButton;

    private Button useAliasButton;

    private final Color color = new Color(Display.getDefault(), 238, 238, 0);

    public PropertySetDialog(Shell parentShell, MapperManager mapperManager) {
        super(parentShell);
        this.mapperManager = mapperManager;
        generationManager = mapperManager.getComponent().getGenerationManager();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = 10;
        gridLayout.marginTop = 20;
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);

        delimitedIdentifiersButton = new Button(container, SWT.CHECK);
        delimitedIdentifiersButton.setText(Messages.getString("PropertySetDialog.delimitedIdentifiers.title")); //$NON-NLS-1$

        useAliasButton = new Button(container, SWT.CHECK);
        useAliasButton.setText(Messages.getString("PropertySetDialog.useAlias.title")); //$NON-NLS-1$

        init();
        addListener();
        updateStatus();
        return container;
    }

    private void init() {
        delimitedIdentifiersButton.setSelection(generationManager.isUseDelimitedIdentifiers());
        useAliasButton.setSelection(generationManager.isUseAliasInOutputTable());

        // Implement the column alias only for tELTTeradataMap now.
        boolean enabled = false;
        IComponent component = mapperManager.getComponent().getComponent();
        if (component != null && "tELTTeradataMap".equals(component.getName())) { //$NON-NLS-1$
            enabled = true;
        }
        useAliasButton.setVisible(enabled);
    }

    private void addListener() {
        delimitedIdentifiersButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                mapperManager.useDelimitedIdentifiers(delimitedIdentifiersButton.getSelection());
                updateStatus();
            }
        });
        useAliasButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                mapperManager.useAliasInOutputTable(useAliasButton.getSelection());
                updateStatus();
            }
        });
    }

    private void updateStatus() {
        if (generationManager.isUseDelimitedIdentifiers() == delimitedIdentifiersButton.getSelection()) {
            delimitedIdentifiersButton.setBackground(null);
        } else {
            delimitedIdentifiersButton.setBackground(color);
        }
        if (generationManager.isUseAliasInOutputTable() == useAliasButton.getSelection()) {
            useAliasButton.setBackground(null);
        } else {
            useAliasButton.setBackground(color);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 300);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("PropertySetDialog.title")); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        mapperManager.useDelimitedIdentifiers(delimitedIdentifiersButton.getSelection());
        mapperManager.useAliasInOutputTable(useAliasButton.getSelection());
        super.okPressed();
    }
}
