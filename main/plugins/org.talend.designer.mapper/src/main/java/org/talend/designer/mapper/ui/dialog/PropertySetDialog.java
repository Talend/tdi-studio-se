// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.swt.formtools.LabelledDirectoryField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.MapperSettingsManager;
import org.talend.designer.mapper.model.MapperSettingModel;
import org.talend.designer.mapper.ui.listener.CommonListener;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class PropertySetDialog extends Dialog {

    private MapperManager mapperManager;

    private MapperSettingsManager settingsManager;

    private Button dieOnErrorButton;

    private Button lookupInParallelButton;

    private Button enableAutoConvertTypeBtn;

    private LabelledDirectoryField directoryField;

    private LabelledText sizeField;

    public static final String QUOTATION_MARK = "\""; //$NON-NLS-1$

    private final Color color = new Color(Display.getDefault(), 238, 238, 0);

    /**
     * Create the dialog
     * 
     * @param parentShell
     */
    public PropertySetDialog(Shell parentShell, MapperManager mapperManager) {
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

        dieOnErrorButton = new Button(container, SWT.CHECK);
        dieOnErrorButton.setText("Die on error");//$NON-NLS-1$

        lookupInParallelButton = new Button(container, SWT.CHECK);
        lookupInParallelButton.setText("Lookup in parallel");//$NON-NLS-1$
        lookupInParallelButton.setEnabled(true);
        IComponent tempNode = ComponentsFactoryProvider.getInstance().get(
                "tParallelize", ComponentCategory.CATEGORY_4_DI.getName());//$NON-NLS-1$
        if (tempNode == null) {
            lookupInParallelButton.setVisible(false);
        }

        enableAutoConvertTypeBtn = new Button(container, SWT.CHECK);
        enableAutoConvertTypeBtn.setText(Messages.getString("PropertySetDialog.Button.enable"));//$NON-NLS-1$

        final Group storeOnDiskGroup = new Group(container, SWT.NONE);
        storeOnDiskGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        storeOnDiskGroup.setText("Store on disk");
        storeOnDiskGroup.setLayout(new GridLayout(3, false));

        directoryField = new LabelledDirectoryField(storeOnDiskGroup, "Temp data directory path:");
        sizeField = new LabelledText(storeOnDiskGroup, "Max buffer size(nb of rows):");

        Label label = new Label(storeOnDiskGroup, SWT.NONE);
        label.setText("*");
        label.setToolTipText("Required filed.");

        init();
        addListener();
        updateStatus();
        //
        return container;
    }

    private void init() {
        MapperSettingModel currnentModel = settingsManager.getCurrnentModel();
        dieOnErrorButton.setSelection(currnentModel.isDieOnError());
        lookupInParallelButton.setSelection(currnentModel.isLookInParallel());
        enableAutoConvertTypeBtn.setSelection(currnentModel.isEnableAutoConvertType());
        directoryField.setText(StringUtils.trimToEmpty(currnentModel.getTempDataDir()));
        sizeField.setText(StringUtils.trimToEmpty(currnentModel.getRowBufferSize()));
    }

    private void addListener() {
        dieOnErrorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStatus();
            }
        });

        lookupInParallelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // shouldn't set value in here,need set value to component when close mapper ui,see TDI-17704
                updateStatus();
            }
        });

        directoryField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateStatus();
            }
        });

        sizeField.addListener(SWT.Verify, CommonListener.getInstance().getNumberListener());
        sizeField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateStatus();
            }
        });

    }

    private void updateStatus() {
        final MapperSettingModel defaultModel = settingsManager.getDefaultModel();

        if (defaultModel.isDieOnError() == dieOnErrorButton.getSelection()) {
            dieOnErrorButton.setBackground(null);
        } else {
            dieOnErrorButton.setBackground(color);
        }

        if (defaultModel.isLookInParallel() == lookupInParallelButton.getSelection()) {
            lookupInParallelButton.setBackground(null);
        } else {
            lookupInParallelButton.setBackground(color);
        }
        if (defaultModel.isEnableAutoConvertType() == enableAutoConvertTypeBtn.getSelection()) {
            enableAutoConvertTypeBtn.setBackground(null);
        } else {
            enableAutoConvertTypeBtn.setBackground(color);
        }

        if (defaultModel.getTempDataDir().equals(directoryField.getText())) {
            directoryField.getTextControl().setBackground(null);
            directoryField.setToolTipText(null);
        } else {
            directoryField.getTextControl().setBackground(color);
            directoryField.setToolTipText("Default is empty.");
        }

        if (defaultModel.getRowBufferSize().equals(sizeField.getText())) {
            sizeField.getTextControl().setBackground(null);
            sizeField.setToolTipText(null);
        } else {
            sizeField.getTextControl().setBackground(color);
            sizeField.setToolTipText("Default is 2000000.");
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
        // bug TDI-19070
        String directory = directoryField.getText();
        if (!ContextParameterUtils.isContainContextParam(directory)) {
            if (directory != null && StringUtils.trimToNull(directory) != null
                    && (!directory.startsWith(QUOTATION_MARK) || !directory.endsWith(QUOTATION_MARK))) {
                directory = TalendQuoteUtils.addQuotesIfNotExist(directory);
            }
        }
        MapperSettingModel currentModel = settingsManager.getCurrnentModel();
        currentModel.setDieOnError(dieOnErrorButton.getSelection());
        currentModel.setLookInParallel(lookupInParallelButton.getSelection());
        currentModel.setEnableAutoConvertType(enableAutoConvertTypeBtn.getSelection());
        currentModel.setTempDataDir(directory);
        currentModel.setRowBufferSize(sizeField.getText());

        if (dieOnErrorButton.getSelection()) {
            mapperManager.removeRejectOutput();
        } else {
            if (!mapperManager.hasRejectOutput(mapperManager.getOutputTables())) {
                mapperManager.addRejectOutput();
            }
        }

        super.okPressed();
    }

}
