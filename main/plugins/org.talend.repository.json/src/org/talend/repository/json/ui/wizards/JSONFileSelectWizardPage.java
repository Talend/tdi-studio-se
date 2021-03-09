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
package org.talend.repository.json.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.model.json.JSONFileConnection;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONFileSelectWizardPage extends JSONFileWizardPage {

    private JSONFileWizardPage JSONFileInputStep1;

    private JSONFileWizardPage JSONFileInputStep2;

    private JSONFileWizardPage JSONFileInputStep3;

    private JSONFileOutputWizardPage JSONFileOutputStep1;

    private JSONFileOutputWizardPage JSONFileOutputStep2;

    private JSONFileOutputWizardPage JSONFileOutputStep3;

    private Label label;

    private Button inputModeButton;

    private Button outputModeButton;

    private boolean isInputModel = true;

    public JSONFileSelectWizardPage(boolean creation, ConnectionItem connectionItem, boolean isRepositoryObjectEditable,
            String[] existingNames) {
        super(creation, connectionItem, isRepositoryObjectEditable, existingNames);
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        layout.numColumns = 1;
        composite.setLayout(layout);

        label = new Label(composite, SWT.NONE);
        GridData labelData = new GridData();
        labelData.verticalSpan = 8;
        label.setLayoutData(labelData);
        label.setText("Select one model to create Json metadata");

        boolean inputModel = ((JSONFileConnection) connectionItem.getConnection()).isInputModel();
        inputModeButton = new Button(composite, SWT.RADIO);
        inputModeButton.setText("Input Json");
        if (creation) {
            inputModeButton.setSelection(isInputModel);
        } else {
            inputModeButton.setSelection(inputModel);
        }
        inputModeButton.setEnabled(creation);

        outputModeButton = new Button(composite, SWT.RADIO);
        outputModeButton.setText("Output Json");
        if (creation) {
            outputModeButton.setSelection(!isInputModel);
        } else {
            outputModeButton.setSelection(!inputModel);
        }
        outputModeButton.setEnabled(creation);
        setControl(composite);
        addListeners();
    }

    private void addListeners() {
        inputModeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                isInputModel = true;
                ((JSONFileConnection) connectionItem.getConnection()).setInputModel(true);
            }

        });
        outputModeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                isInputModel = false;
                ((JSONFileConnection) connectionItem.getConnection()).setInputModel(false);
            }

        });
    }

    @Override
    public IWizardPage getNextPage() {
        generateDynamicWizardPage();
        return super.getNextPage();
    }

    private void generateDynamicWizardPage() {
        List<IWizardPage> wizardPages = new ArrayList<IWizardPage>();
        boolean inputModel = false;
        if (creation) {
            inputModel = isInputModel;
        } else {
            inputModel = ((JSONFileConnection) connectionItem.getConnection()).isInputModel();
        }
        if (inputModel) {
            JSONFileInputStep1 = new JSONFileWizardPage(creation, 1, connectionItem, isRepositoryObjectEditable, existingNames);
            JSONFileInputStep1.setTitle("File - Step" + " 3 " + "of" + " 5");
            JSONFileInputStep1
                    .setDescription("Add a Metadata File on repository\nDefine the path of the file and the format settings");
            wizardPages.add(JSONFileInputStep1);
            JSONFileInputStep1.setPageComplete(!creation);
            JSONFileInputStep1.setWizard(getWizard());

            JSONFileInputStep2 = new JSONFileWizardPage(creation, 2, connectionItem, isRepositoryObjectEditable, existingNames);
            JSONFileInputStep2.setTitle("File - Step" + " 4 " + "of" + " 5");
            JSONFileInputStep2.setDescription("Add a Metadata File on repository\nDefine the setting of the parse job");
            wizardPages.add(JSONFileInputStep2);
            JSONFileInputStep2.setPageComplete(!creation);
            JSONFileInputStep2.setWizard(getWizard());

            if (creation) {
                JSONFileInputStep3 = new JSONFileWizardPage(creation, 3, connectionItem, isRepositoryObjectEditable, null);
                JSONFileInputStep3.setTitle("File - Step" + " 5 " + "of"

                + " 5");
                JSONFileInputStep3.setDescription("Add a Schema on repository\nDefine the Schema");
                wizardPages.add(JSONFileInputStep3);
                JSONFileInputStep3.setPageComplete(!creation);
                JSONFileInputStep3.setWizard(getWizard());
            }

        } else {
            JSONFileOutputStep1 = new JSONFileOutputWizardPage(creation, 1, connectionItem, isRepositoryObjectEditable,
                    existingNames);
            JSONFileOutputStep1.setTitle("File - Step" + " 3 " + "of" + " 5");
            JSONFileOutputStep1.setDescription("Select create manually or from a file\nDefine the output file");
            wizardPages.add(JSONFileOutputStep1);
            JSONFileOutputStep1.setPageComplete(!creation);
            JSONFileOutputStep1.setWizard(getWizard());

            JSONFileOutputStep2 = new JSONFileOutputWizardPage(creation, 2, connectionItem, isRepositoryObjectEditable,
                    existingNames);
            JSONFileOutputStep2.setTitle("File - Step" + " 4 " + "of" + " 5");
            JSONFileOutputStep2.setDescription("Add or edit schema and tree\nDrag&Drop to define columns");
            wizardPages.add(JSONFileOutputStep2);
            JSONFileOutputStep2.setPageComplete(!creation);
            JSONFileOutputStep2.setWizard(getWizard());

            if (creation) {
                JSONFileOutputStep3 = new JSONFileOutputWizardPage(creation, 3, connectionItem, isRepositoryObjectEditable,
                        existingNames);
                JSONFileOutputStep3.setTitle("File - Step" + " 5 " + "of" + " 5");
                JSONFileOutputStep2.setDescription("Add a Schema on repository\nDefine the Schema");
                wizardPages.add(JSONFileOutputStep3);
                JSONFileOutputStep3.setPageComplete(!creation);
                JSONFileOutputStep3.setWizard(getWizard());
            }
        }
        ((JSONWizard) getWizard()).setDynamicWizardPages(wizardPages);
    }
}
