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

import org.eclipse.gef.commands.Command;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class UpdateJunitNodeCommand extends Command {

    private UpdateResult result;

    public UpdateJunitNodeCommand(UpdateResult result) {
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
        Object job = result.getJob();
        if (job == null) {
            return;
        }
        if (job instanceof IProcess2) {
            Item item = ((IProcess2) job).getProperty().getItem();
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService != null) {
                    boolean isTestContainer = testContainerService.isTestContainerItem(item);
                    if (!isTestContainer) {
                        return;
                    }
                    IUpdateItemType updateType = result.getUpdateType();
                    if (updateType instanceof EUpdateItemType) {
                        switch ((EUpdateItemType) updateType) {
                        case JUNIT_RELOAD:
                            List<Node> junitsNodes = (List<Node>) result.getUpdateObject();
                            if (junitsNodes != null && !junitsNodes.isEmpty()) {
                                for (Node node : junitsNodes) {
                                    testContainerService.reloadJunitsNodes(node);
                                }
                            }
                        default:
                        }
                    }
                }
            }
        }
    }
}
