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

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusElementCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: CheckController.java 1 2006-12-12 下午01:45:55 +0000 (下午01:45:55) yzhang $
 *
 */
public class CheckController extends AbstractElementPropertySectionController {

    /**
     * DOC yzhang CheckController constructor comment.
     *
     * @param parameterBean
     */
    public CheckController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    private Command createCommand(SelectionEvent event) {
        Control ctrl = (Control) event.getSource();
        String paramName = (String) ctrl.getData(PARAMETER_NAME);

        // only for checkbox, other buttons must be checked
        // before
        Command cmd = null;
        Boolean value = new Boolean(((Button) ctrl).getSelection());

        // Empty EParameterName.PROCESS_TYPE_PROCESS parameter if the select job is a M/R job(TDI-25914).
        if (value && paramName.equals(EParameterName.USE_DYNAMIC_JOB.getName())) {
            if (elem instanceof Node) {
                String processParamName = EParameterName.PROCESS_TYPE_PROCESS.getName();
                IElementParameter processParameter = elem.getElementParameter(processParamName);
                if (processParameter != null) {
                    String processId = String.valueOf(processParameter.getValue());
                    IRepositoryNode repNode = null;
                    if (StringUtils.isNotEmpty(processId)) {
                        repNode = RepositorySeekerManager.getInstance().searchRepoViewNode(processId);
                    }
                    if (repNode != null && ERepositoryObjectType.PROCESS_MR != null
                            && ERepositoryObjectType.PROCESS_MR.equals(repNode.getObjectType())) {
                        cmd = new PropertyChangeCommand(elem, processParamName, ""); //$NON-NLS-1$
                        executeCommand(cmd);
                    }
                }
            }
        }

        if (paramName.equals(EParameterName.ACTIVATE.getName())) {
            if (elem instanceof Node) {
                List<Node> nodeList = new ArrayList<Node>();
                nodeList.add((Node) elem);
                List<Connection> connList = new ArrayList<Connection>();
                cmd = new ChangeActivateStatusElementCommand(value, nodeList, connList);
            } else if (elem instanceof Connection) {
                List<Node> nodeList = new ArrayList<Node>();
                List<Connection> connList = new ArrayList<Connection>();
                connList.add((Connection) elem);
                cmd = new ChangeActivateStatusElementCommand(value, nodeList, connList);
            }
        } else {
            cmd = new PropertyChangeCommand(elem, paramName, value);
        }
        return cmd;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.CHECK);
            }

        });
        if (canAddRepositoryDecoration(param)) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("CheckController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        cLayout.setBackground(subComposite.getBackground());
        Button checkBtn = (Button) dField.getControl();
        checkBtn.setBackground(subComposite.getBackground());

        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        if (isInWizard()) {
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
        } else {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, 0);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
            }
        }
        cLayout.setLayoutData(data);
        checkBtn.setData(PARAMETER_NAME, param.getName());
        hashCurControls.put(param.getName(), checkBtn);
        checkBtn.setEnabled(!param.isReadOnly() && (elem instanceof FakeElement || !param.isRepositoryValueUsed()));
        checkBtn.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            checkBtn.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        Point initialSize = checkBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#
     * estimateRowSize (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, final IElementParameter param) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.CHECK);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            Command cmd = createCommand(event);
            executeCommand(cmd);
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Button checkBtn = (Button) hashCurControls.get(param.getName());
        Object value = param.getValue();
        if (checkBtn == null || checkBtn.isDisposed()) {
            return;
        }
        if (!param.isContextMode()) {
            if (value == null) {
                checkBtn.setSelection(false);
            } else if (value instanceof String) {
                checkBtn.setSelection(Boolean.valueOf((String) value));
            } else {
                checkBtn.setSelection((Boolean) value);
            }
        } else {
            checkBtn.setSelection(true);
            checkBtn.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
            checkBtn.setEnabled(false);
        }
    }
}
