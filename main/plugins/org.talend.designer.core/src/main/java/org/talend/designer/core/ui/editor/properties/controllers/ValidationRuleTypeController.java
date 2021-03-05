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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.metadata.RepositoryObjectHelper;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ValidationRuleTypeController extends AbstractRepositoryController {

    public ValidationRuleTypeController(IDynamicProperty dp) {
        super(dp);
        repositoryValueHander = new ValidationRuleValueHander();
    }

    @Override
    protected Command createButtonCommand(Button button) {
        IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        String paramName = (String) button.getData(PARAMETER_NAME);
        IElementParameter param = elem.getElementParameter(paramName);
        Object data = button.getData(NAME);
        if (data != null && data.equals(REPOSITORY_CHOICE)) {
            String propertyType = (String) elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
            if (EmfComponent.BUILTIN.equals(propertyType)) {
                MessageDialog
                        .openWarning(
                                button.getShell(),
                                "Warning",
                                "The schema type of component has been setted to 'Built-In', please set it to 'Repository' if you want to use the validation rule.");
                return null;
            }

            ERepositoryObjectType type = ERepositoryObjectType.METADATA_VALIDATION_RULES;

            RepositoryReviewDialog dialog = new RepositoryReviewDialog(button.getShell(), type, param.getRepositoryValue(),
                    new ViewerFilter[] { new ValidationRuleFilter() }, elem);

            if (dialog.open() == RepositoryReviewDialog.OK) {
                RepositoryNode node = dialog.getResult();
                while (node.getObject().getProperty().getItem() == null
                        || (!(node.getObject().getProperty().getItem() instanceof ConnectionItem))) {
                    node = node.getParent();
                }

                if (switchParam != null) {
                    switchParam.setValue(Boolean.FALSE);
                }

                String id = dialog.getResult().getObject().getProperty().getId();

                if (id.equals(elem.getPropertyValue(paramName))) {
                    return null;
                }

                return new PropertyChangeCommand(elem, EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName(), id);

            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createComboCommand(org
     * .eclipse.swt.custom.CCombo)
     */
    @Override
    protected Command createComboCommand(CCombo combo) {
        Connection repositoryConnection = null;

        String paramName = (String) combo.getData(PARAMETER_NAME);

        IElementParameter param = elem.getElementParameter(paramName);

        String value = combo.getText();

        for (int j = 0; j < param.getListItemsValue().length; j++) {
            if (combo.getText().equals(param.getListItemsDisplayName()[j])) {
                value = (String) param.getListItemsValue()[j];
            }
        }
        if (value.equals(param.getValue())) {
            return null;
        }

        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, repositoryConnection,
                paramName, value);

        return changeValuesFromRepository;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#getRepositoryChoiceParamName
     * ()
     */
    @Override
    protected String getRepositoryChoiceParamName() {
        return EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#getRepositoryTypeParamName
     * ()
     */
    @Override
    protected String getRepositoryTypeParamName() {
        return EParameterName.VALIDATION_RULE_TYPE.getName();
    }

    class ValidationRuleFilter extends ViewerFilter {

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            RepositoryNode node = (RepositoryNode) element;
            if (node.getObjectType() == ERepositoryObjectType.METADATA_VALIDATION_RULES) {
                List<IRepositoryViewObject> objs = ValidationRulesUtil.getRelatedValidationRuleObjs(elem);
                if (objs == null || objs.size() < 1) {
                    return false;
                }
                for (IRepositoryViewObject obj : objs) {
                    IRepositoryViewObject repObj = node.getObject();
                    if (repObj != null && repObj.getId() != null && repObj.getId().equals(obj.getId())) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }

    private class ValidationRuleValueHander extends ControllerRepositoryValueHander {

        @Override
        protected void fastInitializeRepositoryNames(IElementParameter param) {
            if (param.getName().equals(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName())) {
                fastRepositoryUpdateValidationRule(param);
            }
        }

        private void fastRepositoryUpdateValidationRule(IElementParameter param) {
            if (param != null && param.getValue() != null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                String linkedRepository = (String) param.getValue();
                Item item;
                String displayName = "";
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object == null) {
                        return;
                    }
                    item = object.getProperty().getItem();
                    if (item instanceof ConnectionItem) {
                        lastItemUsed = (ConnectionItem) item;
                        displayName = RepositoryObjectHelper.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                                + lastItemUsed.getProperty().getLabel();
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }

                param.setListItemsDisplayName(new String[] { displayName });
                param.setListItemsValue(new String[] { (String) param.getValue() });
            }

        }

    }
}
