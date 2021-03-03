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
package org.talend.designer.runprocess.ui;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FindDialog extends Dialog {

    /**
     * DOC Administrator findDialog constructor comment.
     *
     * @param parentShell
     */
    public FindDialog(Shell parentShell) {
        super(parentShell);

    }

    protected void createButtonsForButtonBar(Composite parent) {
    }

    protected int getShellStyle() {
        return super.getShellStyle();
    }

    protected Point getInitialSize() {
        return new Point(240, 200);
    }

    // the console output text composite
    StyledText consoleText;

    // the newest searched location
    int selectionLoaction = 0;;

    // record the founded location used for backward search
    ArrayList locationRecord = new ArrayList();

    boolean firstSearch = true;

    public void setConsoleText(StyledText consoleText) {
        this.consoleText = consoleText;
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(3, false);
        container.setLayout(layout);

        Label label = new Label(container, SWT.CENTER);
        final Text text = new Text(container, SWT.BORDER);

        final Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
        group.setText("Direction");
        group.setLayout(new RowLayout(SWT.VERTICAL));
        final Button forwardButton = new Button(group, SWT.RADIO);
        forwardButton.setText("Forward");
        forwardButton.setSelection(true);

        final Button backwardButton = new Button(group, SWT.RADIO);
        backwardButton.setText("Backward");
        final Label tempLabel = new Label(container, SWT.NONE);
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                selectionLoaction = 0;
                tempLabel.setText("");
                locationRecord.clear();
                firstSearch = true;
            }

        });
        forwardButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                tempLabel.setText("");

            }

        });
        backwardButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                tempLabel.setText("");
            }

        });
        Button findButton = new Button(container, SWT.NONE);
        findButton.setText("Find");
        findButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                if (consoleText.getText().length() > 0) {
                    this.searchString(text.getText());
                } else
                    tempLabel.setText("String not found");

            }

            private void searchString(String parameter) {
                if (!parameter.equals("")) {
                    if (firstSearch == true) {
                        findAll(parameter);
                        firstSearch = false;
                    }
                    selectionLoaction = consoleText.getSelection().x;
                    boolean selected = false;
                    if (backwardButton.getSelection() == true) {
                        if (locationRecord.size() > 0) {
                            for (int i = locationRecord.size() - 1; i >= 0; i--) {
                                if (Integer.parseInt(locationRecord.get(i).toString()) - parameter.length() < selectionLoaction) {
                                    selectionLoaction = Integer.parseInt(locationRecord.get(i).toString());
                                    consoleText.setSelection(selectionLoaction - parameter.length(), selectionLoaction);
                                    selected = true;
                                    break;
                                }
                                selected = false;
                            }
                            if (selected == false)
                                tempLabel.setText("String not found");

                        } else
                            tempLabel.setText("String not found");
                    } else {
                        if (locationRecord.size() > 0) {
                            for (int i = 0; i < locationRecord.size(); i++) {
                                if (Integer.parseInt(locationRecord.get(i).toString()) - parameter.length() > selectionLoaction) {
                                    selectionLoaction = Integer.parseInt(locationRecord.get(i).toString());
                                    consoleText.setSelection(selectionLoaction - parameter.length(), selectionLoaction);
                                    selected = true;
                                    break;
                                }
                                selected = false;
                            }
                            if (selected == false)
                                tempLabel.setText("String not found");
                        } else
                            tempLabel.setText("String not found");
                    }
                }
            }

            private void findAll(String parameter) {
                if (!parameter.equals("")) {
                    int location = -1;
                    do {
                        location = consoleText.getText().substring(selectionLoaction).indexOf(parameter);
                        if (location > -1) {
                            selectionLoaction = selectionLoaction + location + parameter.length();
                            locationRecord.add(selectionLoaction);
                        } else
                            break;
                    } while ((selectionLoaction + parameter.length()) <= consoleText.getText().length());
                }
            }
        });

        GridData data = new GridData(GridData.CENTER);
        data.horizontalSpan = 1;
        label.setText("Find: ");
        label.setLayoutData(data);

        GridData data1 = new GridData(GridData.CENTER);
        data1.horizontalSpan = 2;
        data1.widthHint = 180;
        data1.grabExcessVerticalSpace = true;
        text.setLayoutData(data1);

        GridData data2 = new GridData(GridData.CENTER);
        data2.horizontalSpan = 3;
        data2.widthHint = 215;
        data2.grabExcessVerticalSpace = true;
        group.setLayoutData(data2);

        GridData data3 = new GridData(GridData.CENTER);
        data3.horizontalSpan = 2;
        data3.widthHint = 150;
        tempLabel.setLayoutData(data3);

        GridData data4 = new GridData(GridData.CENTER);
        data4.horizontalSpan = 1;
        data4.widthHint = 65;
        findButton.setLayoutData(data4);
        return container;
    }
}
