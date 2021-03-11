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
package org.talend.designer.mapper.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
import org.talend.core.PluginChecker;
import org.talend.designer.mapper.i18n.Messages;
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

    private Group autoMapGroup;

    private Scale scale;

    private Slider levenshteinSlider;

    private Label levenshteinWeightLabel;

    private Slider jaccardSlider;

    private Label jaccardWeightLabel;

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

        createAutoMapGroup(container);
        updateStatus();
        return container;
    }

    private void createAutoMapGroup(Composite container) {
        if (!PluginChecker.isTIS()) {
            return;
        }
        autoMapGroup = new Group(container, SWT.NONE);
        autoMapGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        autoMapGroup.setText(Messages.getString("PropertySetDialog.AutoMap.GroupName"));

        FormLayout formLayout = new FormLayout();
        autoMapGroup.setLayout(formLayout);

        FormData formdata = new FormData();
        formdata.height = 35;
        formdata.width = 650;

        scale = new Scale(autoMapGroup, SWT.HORIZONTAL);
        scale.setMaximum(4);
        scale.setMinimum(0);
        scale.setPageIncrement(1);
        scale.setLayoutData(formdata);
        scale.getDisplay();

        Label label1 = new Label(autoMapGroup, SWT.NONE);
        label1.setText("Exact Match");
        formdata = new FormData();
        label1.setLayoutData(formdata);
        formdata.top = new FormAttachment(scale, 5);

        Label label2 = new Label(autoMapGroup, SWT.NONE);
        label2.setText("Simple Match");
        formdata = new FormData();
        label2.setLayoutData(formdata);
        formdata.top = new FormAttachment(scale, 5);

        formdata.left = new FormAttachment(label1,
                162 - label1.computeSize(SWT.DEFAULT, SWT.DEFAULT).x - label2.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2);

        Label label3 = new Label(autoMapGroup, SWT.NONE);
        label3.setText("Full Levenshtein");
        formdata = new FormData();
        label3.setLayoutData(formdata);
        formdata.top = new FormAttachment(scale, 5);
        formdata.left = new FormAttachment(label1,
                324 - label1.computeSize(SWT.DEFAULT, SWT.DEFAULT).x - label3.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2);

        Label label4 = new Label(autoMapGroup, SWT.NONE);
        label4.setText("Full Jaccard");
        formdata = new FormData();
        label4.setLayoutData(formdata);
        formdata.top = new FormAttachment(scale, 5);
        formdata.left = new FormAttachment(label1,
                482 - label1.computeSize(SWT.DEFAULT, SWT.DEFAULT).x - label4.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2);

        Label label5 = new Label(autoMapGroup, SWT.NONE);
        label5.setText("Super Fuzzy");
        formdata = new FormData();
        label5.setLayoutData(formdata);
        formdata.top = new FormAttachment(scale, 5);
        formdata.left = new FormAttachment(label1,
                650 - label1.computeSize(SWT.DEFAULT, SWT.DEFAULT).x - label5.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

        Label levenshteinLabel = new Label(autoMapGroup, SWT.NONE);
        levenshteinLabel.setText("Levenshtein");
        formdata = new FormData();
        levenshteinLabel.setLayoutData(formdata);
        formdata.top = new FormAttachment(label1, 5);

        levenshteinSlider = new Slider(autoMapGroup, SWT.HORIZONTAL);
        levenshteinSlider.setSize(200, 25);
        levenshteinSlider.setMaximum(101);
        levenshteinSlider.setMinimum(0);
        levenshteinSlider.setThumb(1);
        formdata = new FormData();
        levenshteinSlider.setLayoutData(formdata);
        formdata.top = new FormAttachment(label1, 5);
        formdata.left = new FormAttachment(levenshteinLabel, 10);

        levenshteinWeightLabel = new Label(autoMapGroup, SWT.NONE);
        formdata = new FormData();
        levenshteinWeightLabel.setLayoutData(formdata);
        formdata.top = new FormAttachment(label1, 5);
        formdata.left = new FormAttachment(levenshteinSlider, 10);

        Label jaccardLabel = new Label(autoMapGroup, SWT.NONE);
        jaccardLabel.setText("Jaccard");
        formdata = new FormData();
        jaccardLabel.setLayoutData(formdata);
        formdata.top = new FormAttachment(levenshteinWeightLabel, 5);

        jaccardSlider = new Slider(autoMapGroup, SWT.HORIZONTAL);
        jaccardSlider.setSize(200, 25);
        jaccardSlider.setMaximum(101);
        jaccardSlider.setMinimum(0);
        jaccardSlider.setThumb(1);
        formdata = new FormData();
        jaccardSlider.setLayoutData(formdata);
        formdata.top = new FormAttachment(levenshteinWeightLabel, 5);
        formdata.left = new FormAttachment(levenshteinLabel, 10);

        jaccardWeightLabel = new Label(autoMapGroup, SWT.NONE);
        formdata = new FormData();
        jaccardWeightLabel.setLayoutData(formdata);
        formdata.top = new FormAttachment(levenshteinWeightLabel, 5);
        formdata.left = new FormAttachment(jaccardSlider, 10);

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

        MapperSettingModel currnentModel = settingsManager.getCurrnentModel();
        levenshteinWeightLabel.setText(String.valueOf(currnentModel.getLevenshteinWeight()));
        levenshteinSlider.setSelection(currnentModel.getLevenshteinWeight());
        jaccardWeightLabel.setText(String.valueOf(currnentModel.getJaccardWeight()));
        jaccardSlider.setSelection(currnentModel.getJaccardWeight());

        updateScale();
    }

    private void init() {
        MapperSettingModel currentModel = settingsManager.getCurrnentModel();
        replicatedJoinButton.setSelection(currentModel.isReplicatedJoin());

        /*
         * levenshteinWeightLabel.setText(String.valueOf(currentModel.getLevenshteinWeight()));
         * levenshteinSlider.setSelection(currentModel.getLevenshteinWeight());
         * jaccardWeightLabel.setText(String.valueOf(currentModel.getJaccardWeight()));
         * jaccardSlider.setSelection(currentModel.getJaccardWeight());
         */
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

    private void updateAutoMapLabel() {
        levenshteinWeightLabel.setText(String.valueOf(levenshteinSlider.getSelection()));
        jaccardWeightLabel.setText(String.valueOf(jaccardSlider.getSelection()));
        autoMapGroup.layout();
    }

    private void updateScale() {
        if ((levenshteinSlider.getSelection() <= 20) && (jaccardSlider.getSelection() <= 20)) {// exact match
            scale.setSelection(0);
        } else if ((levenshteinSlider.getSelection() <= 20) && (jaccardSlider.getSelection() >= 70)) {// full jaccard
            scale.setSelection(3);
        } else if ((levenshteinSlider.getSelection() >= 70) && ((jaccardSlider.getSelection()) <= 20)) {// full
            scale.setSelection(2);
        } else if ((levenshteinSlider.getSelection() >= 70) && (jaccardSlider.getSelection() >= 70)) {// supper fuzzy
            scale.setSelection(4);
        } else {
            scale.setSelection(1);
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
        return new Point(700, 420);
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
        currentModel.setLevenshteinWeight(Integer.valueOf(levenshteinSlider.getSelection()));
        currentModel.setJaccardWeight(Integer.valueOf(jaccardSlider.getSelection()));
        super.okPressed();
    }

}
