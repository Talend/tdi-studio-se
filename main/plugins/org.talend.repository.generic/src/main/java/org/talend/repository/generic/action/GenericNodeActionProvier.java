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
package org.talend.repository.generic.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.generic.util.GenericConnectionUtil;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.metadata.action.MetedataNodeActionProvier;

/**
 * created by ycbai on 2017年7月4日 Detailled comment
 *
 */
public class GenericNodeActionProvier extends MetedataNodeActionProvier {

    private Map<String, ITreeContextualAction> actionsMap = null;

    public GenericNodeActionProvier() {
        super();
        actionsMap = new HashMap<>();
    }

    @Override
    public void fillContextMenu(IMenuManager manager) {
        IStructuredSelection sel = (IStructuredSelection) getContext().getSelection();
        Object selObj = sel.getFirstElement();
        if (selObj instanceof RepositoryNode) {
            RepositoryNode repNode = (RepositoryNode) selObj;
            boolean isConnectionNode = true;
            ERepositoryObjectType nodeType = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
            if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)
                    || ERepositoryObjectType.METADATA_CON_COLUMN.equals(nodeType)) {
                isConnectionNode = false;
            }
            if (isConnectionNode) {
                IRepositoryViewObject repObj = repNode.getObject();
                if (repObj == null) {
                    createAndAddAction(manager, null, sel);
                } else {
                    List<ComponentWizard> wizards = GenericConnectionUtil.getAllWizards(repNode);
                    for (ComponentWizard wizard : wizards) {
                        createAndAddAction(manager, wizard, sel);
                    }
                }
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        }
        super.fillContextMenu(manager);
    }

    private void createAndAddAction(IMenuManager manager, ComponentWizard wizard, IStructuredSelection sel) {
        ITreeContextualAction action = null;
        if (wizard == null) {
            action = createAction(wizard, sel);
        } else {
            ComponentWizardDefinition definition = wizard.getDefinition();
            String wizardName = definition.getName();
            action = actionsMap.get(wizardName);
            if (action == null) {
                action = createAction(wizard, sel);
                actionsMap.put(wizardName, action);
            }
        }
        manager.add(action);
    }

    private ITreeContextualAction createAction(ComponentWizard wizard, IStructuredSelection sel) {
        ITreeContextualAction action = new GenericAction(wizard);
        action.init((TreeViewer) getActionSite().getStructuredViewer(), sel);
        return action;
    }

}
