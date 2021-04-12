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
package org.talend.designer.core.ui.editor.update.cmd;

import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class UpdateNodePathCommand extends Command {

    private UpdateResult result;

    public UpdateNodePathCommand(UpdateResult result) {
        super();
        this.result = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        IUpdateItemType updateType = result.getUpdateType();
        if (result == null || updateType == null) {
            return;
        }
        Object updateObject = result.getUpdateObject();
        if (updateObject == null || (!(updateObject instanceof Node))) {
            return;
        }
        // instance of node before might not be good (loaded while check updates needed)
        // so get the instance of the node of the current job in this object.
        IProcess process = (IProcess) result.getJob();
        for (INode node : process.getGraphicalNodes()) {
            if (node.getUniqueName().equals(((Node) updateObject).getUniqueName())) {
                updateObject = node;
                result.setUpdateObject(updateObject);
                break;
            }
        }
        Node node = (Node) updateObject;
        if (updateType instanceof EUpdateItemType) {
            switch ((EUpdateItemType) updateType) {
            case MAP_PATH:
                Object parameter = result.getParameter();
                if (!(parameter instanceof List)) {
                    return;
                }
                List<Object> params = (List<Object>) parameter;
                if (params.size() != 3) {
                    return;
                }
                IElementParameter param = (IElementParameter) params.get(0);
                if (param == null) {
                    return;
                }
                String oldPath = (String) params.get(1);
                String newPath = (String) params.get(2);
                String relativeNewPath = new Path(newPath).removeFirstSegments(2).removeFileExtension().toPortableString();
                String fullOldPath = new Path(oldPath).removeFileExtension().toPortableString();
                String relativeOldPath = new Path(oldPath).removeFirstSegments(2).removeFileExtension().toPortableString();

                Object value = TalendTextUtils.removeQuotes(String.valueOf(param.getValue()));
                if (!fullOldPath.equals(value) && !relativeOldPath.equals(value)) {
                    return;
                }
                if (fullOldPath.equals(value)) {
                    String newValue = TalendTextUtils.addQuotes(new Path(newPath).removeFileExtension().toPortableString());
                    param.setValue(newValue);
                } else if (relativeOldPath.equals(value)) {
                    param.setValue(TalendTextUtils.addQuotes(relativeNewPath));
                }
                break;
            default:
            }
        }
        if (node.getProcess() instanceof IProcess2) {
            PropertyChangeCommand pcc = new PropertyChangeCommand(node, EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);

            boolean executed = false;
            if (process instanceof IGEFProcess) {
                IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                if (designerCoreUIService != null) {
                    executed = designerCoreUIService.executeCommand((IGEFProcess) process, pcc);
                }
            }
            if (!executed) {
                pcc.execute();
            }
        }
    }
}
