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
package org.talend.designer.hl7.ui.header;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.hl7.managers.HL7Manager;

/**
 * DOC xzhang class global comment. Detailled comment
 */
public class HeaderComposite extends Composite {

    private LabelledFileField fileField;

    private LabelledText startChar;

    private String startCharValue;

    private String endCharValue;

    private LabelledText endChar;

    private String filePath;

    private HL7Manager hl7Manager;

    private List<String> msgContentList = new ArrayList<String>();

    private HL7Parse hl7Parse = new HL7Parse();

    private boolean msgIsChange;

    private boolean isRepository;

    public HeaderComposite(Composite parent, int style, String filePath, String startCharValue, String endCharValue,
            HL7Manager hl7Manager, boolean isRepository) {
        super(parent, style);
        this.filePath = filePath;
        this.startCharValue = startCharValue;
        this.endCharValue = endCharValue;
        this.hl7Manager = hl7Manager;
        this.isRepository = isRepository;
        createComponents();
    }

    private void createComponents() {
        this.setLayout(new GridLayout(3, false));
        GridData headerCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        this.setLayoutData(headerCompositeGridData);
        String[] extensions = new String[] { "*.*" }; //$NON-NLS-N$
        fileField = new LabelledFileField(this, "File path:", extensions, 1, SWT.BORDER) {

            protected void setFileFieldValue(String result) {
                if (result != null) {
                    getTextControl().setText(TalendTextUtils.addQuotes(PathUtils.getPortablePath(result)));
                }
            }
        };
        fileField.setText(filePath);
        fileField.setEditable(!isRepository);

        final Group group = Form.createGroup(this, 4, "File Setting", 30);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        group.setLayoutData(gridData);
        startChar = new LabelledText(group, "Start character");
        startChar.setText(startCharValue);
        startChar.setEditable(!isRepository);
        endChar = new LabelledText(group, "End character");
        endChar.setText(endCharValue);
        endChar.setEditable(!isRepository);
        addListeners();
    }

    private void addListeners() {
        fileField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                filePath = fileField.getText();
                hl7Parse.doParse(filePath, hl7Manager, msgContentList, startChar.getText(), endChar.getText());
            }
        });

        startChar.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                startCharValue = startChar.getText();
            }
        });

        endChar.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                endCharValue = endChar.getText();
            }
        });
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getEndCharValue() {
        return this.endCharValue;
    }

    public String getStartCharValue() {
        return this.startCharValue;
    }

    public List<String> getMsgContentList() {
        return this.msgContentList;
    }

    public boolean isMsgIsChange() {
        return this.msgIsChange;
    }

    public void setMsgIsChange(boolean msgIsChange) {
        this.msgIsChange = msgIsChange;
    }
}
