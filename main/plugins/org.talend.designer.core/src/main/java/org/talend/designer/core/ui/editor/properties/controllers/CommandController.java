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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.MultiDefaultValuesUtils;
import org.talend.designer.core.ui.editor.cmd.ExecuteSystemCommandCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 *
 * defined Commands in component. for example:<br/>
 *
 * in java:<br/>
 *
 * <PARAMETER NAME="COMMAND" FIELD="COMMAND" NUM_ROW="33">
 *
 * <DEFAULT IF="CURRENT_OS=='WINDOWS'">"cmd /c \"java -version\""</DEFAULT>
 *
 * <DEFAULT IF="(CURRENT_OS=='LINUX') or (CURRENT_OS=='UNIX')">"xterm java -version"</DEFAULT>
 *
 * <DEFAULT >"java -version"</DEFAULT>
 *
 * </PARAMETER>
 *
 * <br/>so, if the OS is MacOS, it will use the last "DEFAULT".<br/>
 *
 * in perl the commands as: 'cmd /c "java -version"' or 'cmd /c echo \'welcome\''.
 */
public class CommandController extends AbstractElementPropertySectionController {

    private static final String COMMANDS = "COMMANDS"; //$NON-NLS-1$

    private static final String LAUNCH = Messages.getString("CommandController.CommandLabel"); //$NON-NLS-1$

    /**
     * DOC ggu CommandController constructor comment.
     *
     * @param dp
     */
    public CommandController(IDynamicProperty dp) {
        super(dp);

    }

    @Override
    public Control createControl(Composite subComposite, final IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;
        // button
        final Button btnCmd = getWidgetFactory().createButton(subComposite, null, SWT.PUSH);
        btnCmd.setText(LAUNCH);

        data = new FormData();

        GC gc = new GC(btnCmd);
        Point labelSize = gc.stringExtent(LAUNCH);
        gc.dispose();
        int currentLabelWidth = STANDARD_BUTTON_WIDTH;
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE * 2) > STANDARD_BUTTON_WIDTH) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE * 2;
        }
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -currentLabelWidth);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT + 2;
        btnCmd.setLayoutData(data);
        btnCmd.setData(PARAMETER_NAME, param.getName());
        btnCmd.setData(NAME, COMMANDS);
        btnCmd.setData(COMMANDS, checkQuotes((String) param.getValue()));
        btnCmd.setEnabled(!param.isReadOnly());
        btnCmd.addSelectionListener(btnListenerSelection);

        // text
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        final Text commandText = (Text) dField.getControl();
        commandText.setData(PARAMETER_NAME, param.getName());
        cLayout.setBackground(subComposite.getBackground());
        commandText.setEditable(!param.isReadOnly());

        // init the commands from definded xml
        commandText.setText(checkQuotes((String) param.getValue()));
        commandText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String string = checkQuotes(commandText.getText());
                param.setValue(string);
                btnCmd.setData(COMMANDS, param.getValue());

            }
        });

        editionControlHelper.register(param.getName(), commandText);

        addDragAndDropTarget(commandText);
        if (elem instanceof Node) {
            commandText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        hashCurControls.put(param.getName(), commandText);

        // label
        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        currentLabelWidth = STANDARD_LABEL_WIDTH;
        gc = new GC(labelLabel);
        labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnCmd, 0);
        data.top = new FormAttachment(btnCmd, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return btnCmd;
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {

    }

    public void propertyChange(PropertyChangeEvent evt) {

    }

    private String checkQuotes(final String str) {
        if (str == null || "".equals(str)) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(str);
        }

        return str;
    }

    private SelectionListener btnListenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {

        }

        public void widgetSelected(SelectionEvent event) {
            Command command = createCommand(event);
            executeCommand(command);

        }

    };

    private Command createCommand(SelectionEvent event) {
        Control ctrl = (Control) event.getSource();
        if (ctrl instanceof Button) {
            Button btn = (Button) ctrl;
            Object valueObj = btn.getData(COMMANDS);
            if (valueObj != null && valueObj instanceof String) {
                List<String> commandsList = MultiDefaultValuesUtils.parserDefaultValues((String) ElementParameterParser.parse(
                        elem, (String) valueObj));
                if (!commandsList.isEmpty()) {
                    return new ExecuteSystemCommandCommand(commandsList);
                }
            }
        }
        return null;
    }

}
