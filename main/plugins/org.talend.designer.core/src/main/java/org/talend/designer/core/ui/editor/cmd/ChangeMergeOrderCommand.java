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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class ChangeMergeOrderCommand extends Command {

    private INode mergeNode;

    private List<IConnection> connectionInNewOrder;

    private List<IConnection> connectionInOldOrder;

    public ChangeMergeOrderCommand(INode mergeNode, List<IConnection> inputConnections) {
        this.mergeNode = mergeNode;
        this.connectionInNewOrder = inputConnections;
        this.connectionInOldOrder = (List<IConnection>) mergeNode.getIncomingConnections();

        if (connectionInNewOrder.size() != connectionInOldOrder.size()) {
            throw new IllegalArgumentException("new connection list should have the same size as the old one"); //$NON-NLS-1$
        }
        this.setLabel(Messages.getString("ChangeMergeOrderCommand.label")); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {

        mergeNode.setIncomingConnections(connectionInNewOrder);
        connectionInNewOrder.get(0).updateAllId();
        ((Process) mergeNode.getProcess()).checkStartNodes();

        if (mergeNode.getComponent().getName().equalsIgnoreCase("tFileOutputMSXML")) { //$NON-NLS-1$
            reOrder("ROOT"); //$NON-NLS-1$
            reOrder("GROUP"); //$NON-NLS-1$
            reOrder("LOOP"); //$NON-NLS-1$
        }
        checkProcess();
    }

    private void reOrder(String paramName) {
        IElementParameter param = mergeNode.getElementParameter(paramName);
        if (param != null) {
            List<Map<String, Object>> reOrder = reOrder((List<Map<String, Object>>) param.getValue());
            param.setValue(reOrder);
        }
    }

    private List<Map<String, Object>> reOrder(List<Map<String, Object>> values) {
        List<Map<String, Object>> newValues = new ArrayList<Map<String, Object>>();
        if (values == null || values.isEmpty()) {
            return newValues;
        }
        List<? extends IConnection> incomingConnections = this.mergeNode.getIncomingConnections();
        for (IConnection conn : incomingConnections) {
            if (conn.getLineStyle().hasConnectionCategory(org.talend.core.model.process.IConnectionCategory.FLOW)) {
                String uniqueName = conn.getUniqueName();
                List<Map<String, Object>> findConnValue = findConnValue(values, uniqueName);
                if (findConnValue != null) {
                    newValues.addAll(findConnValue);
                }
            }
        }
        return newValues;
    }

    private List<Map<String, Object>> findConnValue(List<Map<String, Object>> values, String row) {
        List<Map<String, Object>> newValues = new ArrayList<Map<String, Object>>();
        if (values == null || values.isEmpty()) {
            return newValues;
        }
        for (Map<String, Object> line : values) {
            String refColumn = (String) line.get("COLUMN"); //$NON-NLS-1$
            String[] rowAndCol = refColumn.split(":"); //$NON-NLS-1$
            if (rowAndCol.length > 0) {
                if (rowAndCol[0].equals(row)) {
                    newValues.add(line);
                }
            }
        }
        return newValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */

    @Override
    public void undo() {
        mergeNode.setIncomingConnections(connectionInOldOrder);
        connectionInOldOrder.get(0).updateAllId();
        ((Process) mergeNode.getProcess()).checkStartNodes();
        checkProcess();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        super.redo();
        checkProcess();
    }

    /**
     * tang Comment method "checkProcess".
     */
    private void checkProcess() {
        IProcess process = mergeNode.getProcess();
        if (process instanceof IProcess2) {
            ((IProcess2) process).checkProcess();
        }
    }

}
