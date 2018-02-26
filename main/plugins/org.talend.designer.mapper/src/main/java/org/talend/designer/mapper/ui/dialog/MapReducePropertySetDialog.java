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
package org.talend.designer.mapper.ui.dialog;

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
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.MapperSettingsManager;
import org.talend.designer.mapper.model.MapperSettingModel;

/**
 * Sets global properties for the MapReduce Mapper component (currently only whether the join is replicated or not).
 */
public class MapReducePropertySetDialog extends Dialog {

    private MapperManager mapperManager;

    private MapperSettingsManager settingsManager;

    private Button replicatedJoinButton;

    public static final String QUOTATION_MARK = "\""; //$NON-NLS-1$

    private final Color color = new Color(Display.getDefault(), 238, 238, 0);

    /**
     * Create the dialog
     * 
     * @param parentShell
     */
    public MapReducePropertySetDialog(Shell parentShell, MapperManager mapperManager) {
        super(parentShell);
        this.mapperManager = mapperManager;
        settingsManager = MapperSettingsManager.getInstance(mapperManager);
    }

    /**
     * Create contents of the dialog
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = 10;
        gridLayout.marginTop = 10;
        gridLayout.marginHeight = 10;
        container.setLayout(gridLayout);

        replicatedJoinButton = new Button(container, SWT.CHECK);
        replicatedJoinButton.setText("Use replicated join (when all lookup tables can fit in memory).");

        init();
        addListener();
        updateStatus();
        return container;
    }

    private void init() {
        MapperSettingModel currentModel = settingsManager.getCurrnentModel();
        replicatedJoinButton.setSelection(currentModel.isReplicatedJoin());
    }

    private void addListener() {
        replicatedJoinButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStatus();
            }
        });
    }

    private void updateStatus() {
        final MapperSettingModel defaultModel = settingsManager.getDefaultModel();

        if (defaultModel.isReplicatedJoin() == replicatedJoinButton.getSelection()) {
            replicatedJoinButton.setBackground(null);
        } else {
            replicatedJoinButton.setBackground(color);
        }
    }

    /**
     * Create contents of the button bar
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(600, 350);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Property Settings");
    }

    @Override
    protected void okPressed() {
        MapperSettingModel currentModel = settingsManager.getCurrnentModel();
        currentModel.setReplicatedJoin(replicatedJoinButton.getSelection());
        super.okPressed();
    }

}
