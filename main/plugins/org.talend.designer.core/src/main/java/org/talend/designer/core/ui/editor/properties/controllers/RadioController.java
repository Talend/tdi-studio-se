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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusElementCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.DesignerUtilities;

/**
 * yzhang class global comment. Detailled comment
 */
public class RadioController extends AbstractElementPropertySectionController {

    /**
     * yzhang RadioButtonController constructor comment.
     *
     * @param dp
     */
    public RadioController(IDynamicProperty dp) {
        super(dp);
    }

    private List<Command> createCommand(SelectionEvent event) {
        Set<String> elementsName;
        Control ctrl = (Control) event.getSource();

        elementsName = hashCurControls.keySet();

        List<Command> commands = new ArrayList<Command>();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }
                if (ctrl.equals(event.getSource())) {
                    if (ctrl instanceof Button) {
                        // only for checkbox, other buttons must be checked
                        // before
                        if (!elem.getPropertyValue(name).equals(new Boolean(((Button) ctrl).getSelection()))) {
                            Command cmd = null;
                            Boolean value = new Boolean(((Button) ctrl).getSelection());
                            if (name.equals(EParameterName.ACTIVATE.getName())) {
                                if (elem instanceof Node) {
                                    List<Node> nodeList = new ArrayList<Node>();
                                    nodeList.add((Node) elem);
                                    List<Connection> connList = new ArrayList<Connection>();
                                    cmd = new ChangeActivateStatusElementCommand(value, nodeList, connList);
                                    commands.add(cmd);
                                }
                            } else {
                                IElementParameter param = elem.getElementParameter(name);
                                if (Boolean.TRUE.equals(value)) {
                                    List<IElementParameter> parameters = DesignerUtilities.findRadioParamInSameGroup(elem.getElementParameters(), param);
                                    for (IElementParameter currentRadioParam : parameters) {
                                        if (Boolean.TRUE.equals(currentRadioParam.getValue())) {
                                            cmd = new PropertyChangeCommand(elem, currentRadioParam.getName(), Boolean.FALSE);
                                            commands.add(cmd);
                                        }
                                    }
                                }

                                cmd = new PropertyChangeCommand(elem, name, value);
                                commands.add(cmd);

                                String groupName = param.getGroup();
                                if (groupName != null && elem.getElementParameter(groupName) != null) {
                                    Command cmd2 = new PropertyChangeCommand(elem, groupName, name);
                                    commands.add(cmd2);
                                }
                            }
                            return commands;
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl(org.eclipse.swt.widgets.Composite,
     * org.talend.core.model.process.IElementParameter, int, int, int, org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, final IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {

        Button buttonRadio = new Button(subComposite, SWT.RADIO);
        buttonRadio.setText(param.getDisplayName());

        buttonRadio.setBackground(subComposite.getBackground());

        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        buttonRadio.setLayoutData(data);

        hashCurControls.put(param.getName(), buttonRadio);
        buttonRadio.setEnabled(!param.isReadOnly());
        buttonRadio.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            buttonRadio.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        Point initialSize = buttonRadio.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return buttonRadio;

    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent event) {
            List<Command> cmds = createCommand(event);
            if (cmds == null) {
                return;
            }
            for (Command cmd : cmds) {
                executeCommand(cmd);
            }
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize(org.eclipse.swt.widgets.Composite,
     * org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, final IElementParameter param) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.RADIO);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org.talend.core.model.process.IElementParameter,
     * boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        Button radioButton = (Button) hashCurControls.get(param.getName());
        Object value = param.getValue();
        if (radioButton == null || radioButton.isDisposed()) {
            return;
        }

        if (value instanceof String) {
            radioButton.setSelection(Boolean.valueOf((String) value));
        } else {
            radioButton.setSelection((Boolean) value);
        }

        if (param.isContextMode()) {
            radioButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
            radioButton.setEnabled(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {

    }

}
