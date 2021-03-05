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
package org.talend.designer.scd.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.model.SurrogateCreationType;
import org.talend.designer.scd.model.SurrogateKey;
import org.talend.designer.scd.util.DragDropManager;
import org.talend.designer.scd.util.SWTResourceManager;
import org.talend.designer.scd.util.SurrogateKeyManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class SurrogateSection extends ScdSection {

    private static final int SURROGATE_FIELD_WIDTH = 150;

    private static final Color ERROR_COLOR = SWTResourceManager.getColor(IColorConstants.RED);

    private final SurrogateKeyManager surrogateManager;

    private DragDropManager dragDropManager;

    private Text nameText;

    private CCombo creationCombo;

    private SurrogateKey key;

    private Label inputFieldLabel;

    private Text routineText;

    private Text dbSequenceText;

    private Runnable creationSwitch;

    private Composite dbSequenceComp;

    /**
     * DOC hcw SurrogateSection constructor comment.
     *
     * @param parent
     * @param width
     * @param height
     */
    public SurrogateSection(Composite parent, ScdManager scdManager) {
        super(parent, scdManager, false);
        surrogateManager = new SurrogateKeyManager();
    }

    @Override
    protected void createContents(Composite parent) {
        dragDropManager = new DragDropManager();
        key = new SurrogateKey();

        final Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        parent.setBackground(white);
        Composite composite = new Composite(parent, SWT.NONE);

        composite.setBackground(white);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        // row 1
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("name"); //$NON-NLS-1$
        nameLabel.setBackground(white);
        GridDataFactory.swtDefaults().applyTo(nameLabel);
        nameText = new Text(composite, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(SURROGATE_FIELD_WIDTH, SWT.DEFAULT).applyTo(nameText);

        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (StringUtils.isEmpty(nameText.getText())) {
                    nameText.setBackground(ERROR_COLOR);
                } else {
                    nameText.setBackground(white);
                }
                // update model value
                key.setColumn(nameText.getText());
            }
        });

        // row 2
        Label creationLabel = new Label(composite, SWT.NONE);
        creationLabel.setText("creation"); //$NON-NLS-1$
        creationLabel.setBackground(white);
        GridDataFactory.swtDefaults().applyTo(creationLabel);

        creationCombo = new CCombo(composite, SWT.READ_ONLY | SWT.BORDER);
        creationCombo.setItems(getScdManager().getSurrogateCreationTypeNames());
        GridDataFactory.swtDefaults().hint(SURROGATE_FIELD_WIDTH + 3, SWT.DEFAULT).applyTo(creationCombo);

        // row 3
        Label complementLabel = new Label(composite, SWT.NONE);
        complementLabel.setText("complement"); //$NON-NLS-1$
        complementLabel.setBackground(white);
        GridDataFactory.swtDefaults().applyTo(complementLabel);

        final Composite complementComp = new Composite(composite, SWT.NONE);
        GridDataFactory.swtDefaults().applyTo(complementComp);
        final StackLayout stack = new StackLayout();
        complementComp.setLayout(stack);

        final Composite inputFieldComp = new Composite(complementComp, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(inputFieldComp);
        inputFieldLabel = new Label(inputFieldComp, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).hint(100, SWT.DEFAULT).applyTo(inputFieldLabel);

        // add drag and drop support for input field
        IDragDropDelegate delegate = createDragDropDelegate(inputFieldLabel);
        dragDropManager.addDragSupport(inputFieldLabel, delegate);
        dragDropManager.addDropSupport(inputFieldLabel, delegate);

        // show DB_SEQUENCE in OraleSCD
        // final boolean enableOracle = getScdManager().enableOracle();
        // if (enableOracle) {
        dbSequenceComp = new Composite(complementComp, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(dbSequenceComp);
        dbSequenceText = new Text(dbSequenceComp, SWT.BORDER);
        dbSequenceText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (StringUtils.isEmpty(dbSequenceText.getText())) {
                    dbSequenceText.setBackground(ERROR_COLOR);
                } else {
                    dbSequenceText.setBackground(white);
                }
                if (key.getCreation() == SurrogateCreationType.DB_SEQUENCE) {
                    key.setComplement(dbSequenceText.getText());
                }
            }
        });
        GridDataFactory.fillDefaults().hint(SURROGATE_FIELD_WIDTH, SWT.DEFAULT).applyTo(dbSequenceText);
        TalendProposalUtils.installOn(dbSequenceText, null);
        // }

        final Composite routineFieldComp = new Composite(complementComp, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(routineFieldComp);
        routineText = new Text(routineFieldComp, SWT.BORDER);
        routineText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (StringUtils.isEmpty(routineText.getText())) {
                    routineText.setBackground(ERROR_COLOR);
                } else {
                    routineText.setBackground(white);
                }
                if (key.getCreation() == SurrogateCreationType.ROUTINE) {
                    // update model value
                    key.setComplement(routineText.getText());
                }
            }
        });
        GridDataFactory.fillDefaults().hint(SURROGATE_FIELD_WIDTH, SWT.DEFAULT).applyTo(routineText);

        // add content proposal for routine
        TalendProposalUtils.installOn(routineText, null);

        final Label emptyLabel = new Label(complementComp, SWT.NONE);
        emptyLabel.setBackground(white);
        stack.topControl = emptyLabel;

        // when combo is changed, also change the control in complement
        creationSwitch = new Runnable() {

            public void run() {
                String diaplayName = creationCombo.getText();
                // SurrogateCreationType type = SurrogateCreationType.getTypeByIndex(index + 1);
                SurrogateCreationType type = SurrogateCreationType.getTypeByName(diaplayName);
                key.setCreation(type);
                if (type == SurrogateCreationType.ROUTINE) {
                    stack.topControl = routineFieldComp; // routineText;
                    if (StringUtils.isEmpty(routineText.getText())) {
                        routineText.setBackground(ERROR_COLOR);
                        key.setComplement(""); //$NON-NLS-1$
                    } else {
                        routineText.setBackground(white);
                    }
                    inputFieldLabel.setText(""); //$NON-NLS-1$
                    // if (enableOracle) {
                    dbSequenceText.setText("");//$NON-NLS-1$
                    // }
                } else if (type == SurrogateCreationType.INPUT_FIELD) {
                    stack.topControl = inputFieldComp; // inputFieldLabel;
                    if (StringUtils.isEmpty(inputFieldLabel.getText())) {
                        inputFieldLabel.setBackground(ERROR_COLOR);
                        key.setComplement(""); //$NON-NLS-1$
                    } else {
                        inputFieldLabel.setBackground(null);
                    }
                    routineText.setText(""); //$NON-NLS-1$
                    // if (enableOracle) {
                    dbSequenceText.setText("");//$NON-NLS-1$
                    // }
                } else if (type == SurrogateCreationType.DB_SEQUENCE) {
                    // else if (type == SurrogateCreationType.DB_SEQUENCE && enableOracle) {
                    stack.topControl = dbSequenceComp; // dbSequenceText;
                    if (StringUtils.isEmpty(dbSequenceText.getText())) {
                        dbSequenceText.setBackground(ERROR_COLOR);
                        key.setComplement(""); //$NON-NLS-1$
                    } else {
                        dbSequenceText.setBackground(white);
                    }
                    inputFieldLabel.setText(""); //$NON-NLS-1$
                    routineText.setText("");
                } else {
                    stack.topControl = emptyLabel;
                    routineText.setText(""); //$NON-NLS-1$
                    inputFieldLabel.setText(""); //$NON-NLS-1$
                    // if (enableOracle) {
                    dbSequenceText.setText("");//$NON-NLS-1$
                    // }
                    key.setComplement(""); //$NON-NLS-1$
                }
                scdManager.fireFieldChange();
                complementComp.layout();

            }

        };
        creationCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                creationSwitch.run();
            }
        });

    }

    public List<SurrogateKey> getTableData() {
        List<SurrogateKey> tableModel = new ArrayList<SurrogateKey>();
        tableModel.add(key);
        return tableModel;
    }

    public void setTableInput(List<SurrogateKey> input) {
        if (input == null || input.isEmpty()) {
            key = new SurrogateKey();
        } else {
            key = input.get(0);
        }
        nameText.setText(key.getColumn());

        if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
            inputFieldLabel.setText(key.getComplement());
        } else if (key.getCreation() == SurrogateCreationType.ROUTINE) {
            routineText.setText(key.getComplement());
        } else if (key.getCreation() == SurrogateCreationType.DB_SEQUENCE) {
            dbSequenceText.setText(key.getComplement());
        }
        creationCombo.select(getIndexByCreationType(key.getCreation()));
        // activate event to switch component in stack layout
        creationSwitch.run();
    }

    /**
     * Get the index for creation combo by <code>SurrogateCreationType</code>. Created by Marvin Wang on May 16, 2012.
     *
     * @param type
     * @return
     */
    private int getIndexByCreationType(SurrogateCreationType type) {
        int index = 0;
        String[] items = creationCombo.getItems();
        if (items != null && items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                if (type.getName().equals(items[i])) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    @Override
    public List<String> getUsedFields() {
        List<String> fields = new ArrayList<String>();

        if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
            if (StringUtils.isNotEmpty(key.getComplement())) {
                fields.add(key.getComplement());
            }
        }

        return fields;
    }

    private IDragDropDelegate createDragDropDelegate(final Label text) {
        return new IDragDropDelegate() {

            public String getDragItemsAsText() {
                return "1|" + text.getText(); //$NON-NLS-1$
            }

            public void onDropItems(String data, Point position) {
                String[] items = data.split("\\|"); //$NON-NLS-1$
                text.setText(items[1]);
                key.setComplement(items[1]);
                text.setBackground(null);
            }

            public void removeDragItems(String data) {
                text.setText(""); //$NON-NLS-1$
                key.setComplement(""); //$NON-NLS-1$
                // display as error status, this field must not be null
                text.setBackground(ERROR_COLOR);
                // don't remove table item here, it can be removed by the delete
                // button
            }

            public boolean isDropAllowed(String data) {
                // only allow single selection
                return StringUtils.isEmpty(key.getComplement()) && data.startsWith("1|"); //$NON-NLS-1$
            }

        };

    }

    /**
     * DOC chuang Comment method "addContextHelp".
     *
     * @param scdDialog
     */
    public void addContextHelp(AbstractScdDialog scdDialog) {
        scdDialog.addContextHelp(getControl(), "org.talend.designer.scd.surrogateKey"); //$NON-NLS-1$
    }

}
