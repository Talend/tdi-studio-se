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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
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

    private Slider levenshteinSlider;

    private Slider jaccardSlider;

    private Label levenshteinWeightLabel;

    private Label jaccardWeightLabel;

    public static final String QUOTATION_MARK = "\"";

    private Group autoMapGroup;

    private final Color color = new Color(Display.getDefault(), 238, 238, 0);

    private Scale scale;


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
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);

        dieOnErrorButton = new Button(container, SWT.CHECK);
        dieOnErrorButton.setText("Die on error");

        lookupInParallelButton = new Button(container, SWT.CHECK);
        lookupInParallelButton.setText("Lookup in parallel");
        lookupInParallelButton.setEnabled(true);
        IComponent tempNode = ComponentsFactoryProvider.getInstance().get(
                "tParallelize", ComponentCategory.CATEGORY_4_DI.getName());
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

        autoMapGroup = new Group(container, SWT.NONE);
        autoMapGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        autoMapGroup.setText(Messages.getString("PropertySetDialog.AutoMap.GroupName"));

        GridLayout AutogridLayout = new GridLayout(5, true);
        AutogridLayout.horizontalSpacing = 10;
        AutogridLayout.marginRight = 5;
        
        autoMapGroup.setLayout(AutogridLayout);
        scale = new Scale(autoMapGroup, SWT.CENTER);
        scale.setMaximum(4);
        scale.setMinimum(0);
        scale.setPageIncrement(1);

        GridData grid = new GridData(GridData.CENTER);
        grid.horizontalSpan = 5;
        grid.widthHint = this.getInitialSize().x * 38 / 100;
        grid.heightHint = 40;
        grid.horizontalAlignment = GridData.CENTER;
        scale.setLayoutData(grid);

        /**
         * Composite comp = new Composite(autoMapGroup, SWT.NONE); GridLayout compLayout = new GridLayout(5, true);
         * compLayout.horizontalSpacing = 34; comp.setLayout(compLayout); GridData grid1 = new
         * GridData(GridData.CENTER); grid1.horizontalSpan = 5;// grid1.widthHint = 642;
         * System.out.println(this.getInitialSize()); grid1.heightHint = 40; comp.setLayoutData(grid1);
         **/
        Label label1 = new Label(autoMapGroup, SWT.NONE);
        label1.setText("Exact Match");
        GridData lgd1 = new GridData();
        lgd1.horizontalSpan = 1;
        label1.setLayoutData(lgd1);
        lgd1.horizontalAlignment = SWT.CENTER;

        Label label2 = new Label(autoMapGroup, SWT.NONE);
        label2.setText("Simple Match");
        GridData lgd2 = new GridData();
        lgd2.horizontalSpan = 1;
        label2.setLayoutData(lgd2);
        lgd2.horizontalAlignment = SWT.CENTER;

        Label label3 = new Label(autoMapGroup, SWT.NONE);
        label3.setText("Full Levenshtein");
        GridData lgd3 = new GridData();
        lgd3.horizontalSpan = 1;
        label3.setLayoutData(lgd3);
        lgd3.horizontalAlignment = SWT.CENTER;

        Label label4 = new Label(autoMapGroup, SWT.NONE);
        label4.setText("Full Jaccard");
        GridData lgd4 = new GridData();
        lgd4.horizontalSpan = 1;
        label4.setLayoutData(lgd4);
        lgd4.horizontalAlignment = SWT.CENTER;

        Label label5 = new Label(autoMapGroup, SWT.NONE);
        label5.setText("Super Fuzzy");
        GridData lgd5 = new GridData();
        lgd5.horizontalSpan = 1;
        label5.setLayoutData(lgd5);
        lgd5.horizontalAlignment = SWT.CENTER;

        Composite comp = new Composite(autoMapGroup, SWT.NONE);
        GridLayout compLayout = new GridLayout(3, false);
        GridData gds1 = new GridData();
        gds1.horizontalSpan = 5;
        comp.setLayout(compLayout);
        comp.setLayoutData(gds1);

        Label levenshteinLabel = new Label(comp, SWT.NONE);
        levenshteinLabel.setText("Levenshtein");
        levenshteinSlider = new Slider(comp, SWT.HORIZONTAL);
        levenshteinSlider.setSize(200, 25);
        levenshteinSlider.setMaximum(101);
        levenshteinSlider.setMinimum(0);
        levenshteinSlider.setThumb(1);
        levenshteinWeightLabel = new Label(comp, SWT.NONE);


        Label jaccardLabel = new Label(comp, SWT.NONE);
        jaccardLabel.setText("Jaccard");
        jaccardSlider = new Slider(comp, SWT.HORIZONTAL);
        jaccardSlider.setSize(200, 25);
        jaccardSlider.setMaximum(101);
        jaccardSlider.setMinimum(0);
        jaccardSlider.setThumb(1);
        jaccardWeightLabel = new Label(comp, SWT.NONE);

        init();
        addListener();
        updateStatus();
        updateScale();
        return container;
    }


    private void init() {
        MapperSettingModel currnentModel = settingsManager.getCurrnentModel();
        dieOnErrorButton.setSelection(currnentModel.isDieOnError());
        lookupInParallelButton.setSelection(currnentModel.isLookInParallel());
        enableAutoConvertTypeBtn.setSelection(currnentModel.isEnableAutoConvertType());
        directoryField.setText(StringUtils.trimToEmpty(currnentModel.getTempDataDir()));
        sizeField.setText(StringUtils.trimToEmpty(currnentModel.getRowBufferSize()));

        levenshteinWeightLabel.setText(String.valueOf(currnentModel.getLevenshteinWeight()));
        levenshteinSlider.setSelection(currnentModel.getLevenshteinWeight());
        jaccardWeightLabel.setText(String.valueOf(currnentModel.getJaccardWeight()));
        jaccardSlider.setSelection(currnentModel.getJaccardWeight());
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

        levenshteinSlider.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateAutoMapLabel();
                updateScale();
            }
        });

        jaccardSlider.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateAutoMapLabel();
                updateScale();
            }
        });



        scale.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (scale.getSelection() == 0) {
                    levenshteinSlider.setSelection(0);
                    jaccardSlider.setSelection(0);
                    updateAutoMapLabel();
                } else if (scale.getSelection() == 1) {
                    levenshteinSlider.setSelection(40);
                    jaccardSlider.setSelection(40);
                    updateAutoMapLabel();
                } else if (scale.getSelection() == 2) {
                    levenshteinSlider.setSelection(100);
                    jaccardSlider.setSelection(0);
                    updateAutoMapLabel();
                } else if (scale.getSelection() == 3) {
                    levenshteinSlider.setSelection(0);
                    jaccardSlider.setSelection(100);
                    updateAutoMapLabel();
                } else if (scale.getSelection() == 4) {
                    levenshteinSlider.setSelection(100);
                    jaccardSlider.setSelection(100);
                    updateAutoMapLabel();
                }
            }
        });

    }

    /**
     * DOC xwen Comment method "updateScale".
     */
    private void updateScale() {
        if ((levenshteinSlider.getSelection() <= 20) && (jaccardSlider.getSelection() <= 20)) {// exact match
            scale.setSelection(0);
        } else if ((levenshteinSlider.getSelection() <= 20) && (jaccardSlider.getSelection() >= 70)) {// full jaccard
            scale.setSelection(3);
        } else if ((levenshteinSlider.getSelection() >= 70) && ((jaccardSlider.getSelection()) <= 20)) {// full
                                                                                                        // levenshtein
            scale.setSelection(2);
        } else if ((levenshteinSlider.getSelection() >= 70) && (jaccardSlider.getSelection() >= 70)) {// supper fuzzy
            scale.setSelection(4);
        } else {
            scale.setSelection(1);
        }
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

    private void updateAutoMapLabel() {
        levenshteinWeightLabel.setText(String.valueOf(levenshteinSlider.getSelection()));
        jaccardWeightLabel.setText(String.valueOf(jaccardSlider.getSelection()));
        autoMapGroup.layout();
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

    @Override
    protected Point getInitialSize() {
        return new Point(600, 380);
    }
     */
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
        currentModel.setLevenshteinWeight(Integer.valueOf(levenshteinSlider.getSelection()));
        currentModel.setJaccardWeight(Integer.valueOf(jaccardSlider.getSelection()));

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
