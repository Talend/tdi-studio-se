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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.EEditSelection;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.ESelectionType;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.metadata.dialog.MetadataDialogForMerge;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.SAPParametersUtils;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.model.IMetadataService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class SapSchemaTypeController extends AbstractRepositoryController {

    /**
     *
     */
    private static final String FORCE_READ_ONLY = "FORCE_READ_ONLY"; //$NON-NLS-1$

    private static final String RESET_COLUMNS = "RESET_COLUMNS"; //$NON-NLS-1$

    private static final String COPY_CHILD_COLUMNS = "COPY_CHILD_COLUMNS"; //$NON-NLS-1$

    private static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$

    private static final String RETRIEVE_SCHEMA = "Retrieve Schema"; //$NON-NLS-1$

    private static final String TUNISERVBTGENERIC = "tUniservBTGeneric"; //$NON-NLS-1$

    protected static final int WIZARD_WIDTH = 800;

    protected static final int WIZARD_HEIGHT = 495;

    public SapSchemaTypeController(IDynamicProperty dp) {
        super(dp);
    }

    private boolean prepareReadOnlyTable(IMetadataTable table, boolean readOnlyParam, boolean readOnlyElement) {
        boolean isCustom = false;
        if (table.isReadOnly()) {
            return true;
        }
        for (IMetadataColumn column : table.getListColumns()) {
            if (column.isCustom() && !column.isReadOnly()) {
                isCustom = true;
            }
        }
        if (!isCustom) {
            return readOnlyParam || readOnlyElement;
        }
        for (IMetadataColumn column : table.getListColumns()) {
            if (!column.isCustom()) {
                column.setReadOnly(table.isReadOnly());
            }
        }
        return readOnlyElement;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = lastControl;
        if (elem instanceof Node) {
            lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        }
        lastControlUsed = addButton(subComposite, param, lastControlUsed, numInRow, top);
        return lastControlUsed;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#
     * estimateRowSize (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        int comboSize, buttonSize;

        CCombo combo = new CCombo(subComposite, SWT.BORDER);
        IElementParameter schemaTypeParameter = param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName());
        // elem.getElementParameter(EParameterName.SCHEMA_TYPE.getName());
        String[] originalList = schemaTypeParameter.getListItemsDisplayName();
        combo.setItems(originalList);
        comboSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        combo.dispose();

        Button btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        buttonSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        btn.dispose();
        return Math.max(comboSize, buttonSize) + ITabbedPropertyConstants.VSPACE;
    }

    private Control addButton(Composite subComposite, IElementParameter param, Control lastControl, int numInRow, int top) {
        Button btn;
        Button resetBtn = null;
        Control lastControlUsed = lastControl;
        Point btnSize;
        FormData data;

        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btn.addSelectionListener(listenerSelection);
        btn.setData(NAME, SCHEMA);
        btn.setData(PARAMETER_NAME, param.getName());
        // btn.setEnabled(!param.isReadOnly());

        lastControlUsed = btn;

        if (elem instanceof Node) {
            Node node = (Node) elem;
            boolean flowMainInput = false;
            boolean multipleInput = false;
            boolean tableReadOnly = false;

            IMetadataTable table = node.getMetadataFromConnector(param.getContext());
            if (table != null) {
                tableReadOnly = table.isReadOnly() || param.isReadOnly(node.getElementParametersWithChildrens());
                for (IConnection connec : node.getIncomingConnections()) {
                    if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                            || connec.getLineStyle().equals(EConnectionType.TABLE)
                            || connec.getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
                        flowMainInput = true;
                    }
                }
                IMetadataTable inputTable = null;
                if (flowMainInput) {
                    int nbMain = 0;
                    for (IConnection connec : node.getIncomingConnections()) {
                        if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                            if (inputTable == null) {
                                inputTable = connec.getMetadataTable();
                            }
                            nbMain++;
                        }
                    }
                    INodeConnector nodeConnector = node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName());
                    if (nodeConnector != null) {
                        int maxFlowInput = nodeConnector.getMaxLinkInput();
                        if (maxFlowInput > 1 && nbMain >= 1 && (nbMain <= maxFlowInput || maxFlowInput == -1)) {
                            multipleInput = true;
                        }
                    }

                }
                if (flowMainInput && !multipleInput) {
                    // if component allow schema auto propagation, verify if input/output schemas are the same
                    // if not, allow to use the button sync.

                    if (node.getComponent().isSchemaAutoPropagated()
                            && !table.sameMetadataAs(inputTable, IMetadataColumn.OPTIONS_IGNORE_KEY
                                    | IMetadataColumn.OPTIONS_IGNORE_NULLABLE | IMetadataColumn.OPTIONS_IGNORE_COMMENT
                                    | IMetadataColumn.OPTIONS_IGNORE_PATTERN | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                                    | IMetadataColumn.OPTIONS_IGNORE_DBTYPE | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                    | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                        tableReadOnly = false;
                    }
                    resetBtn = createAdditionalButton(
                            subComposite,
                            btn,
                            btnSize,
                            param,
                            Messages.getString("SchemaController.syncColumns"), Messages.getString("SchemaController.resetButton.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                            top, !tableReadOnly);
                    resetBtn.setData(NAME, RESET_COLUMNS);

                    lastControlUsed = resetBtn;

                }
            }

            if (top == 0 && node.getComponent().getName().equals(TUNISERVBTGENERIC)) {
                Button newButton = null;
                if (resetBtn != null) {
                    newButton = resetBtn;
                } else {
                    newButton = btn;
                }
                Button retrieveSchemaButton = createAdditionalButton(subComposite, newButton, btnSize, param, RETRIEVE_SCHEMA,
                        RETRIEVE_SCHEMA, top, !param.isReadOnly());
                retrieveSchemaButton.setData(NAME, RETRIEVE_SCHEMA);

                lastControlUsed = retrieveSchemaButton;
            }
            // 0004322: tRunJob can import the tBufferOutput schema from the son job
            if (node.getComponent().getName().equals("tRunJob")) { //$NON-NLS-1$
                // for bug 10489
                Button newButton = null;
                if (resetBtn != null) {
                    newButton = resetBtn;
                } else {
                    newButton = btn;
                }
                Button copySchemaButton = createAdditionalButton(subComposite, newButton, btnSize, param,
                        Messages.getString("SchemaController.copyChildSchema"), Messages //$NON-NLS-1$
                                .getString("SchemaController.copyChildSchema.tooltip"), top, !param.isReadOnly()); //$NON-NLS-1$
                copySchemaButton.setData(NAME, COPY_CHILD_COLUMNS);

                lastControlUsed = copySchemaButton;
            }
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, Messages.getString("SchemaController.editSchema")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, labelLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                + (ITabbedPropertyConstants.HSPACE * 2), SWT.RIGHT);
        if (resetBtn != null) {
            data.top = new FormAttachment(resetBtn, 0, SWT.CENTER);
        } else {
            data.top = new FormAttachment(0, top);
        }
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }

        data = new FormData();
        data.left = new FormAttachment(labelLabel, 0);
        data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        if (resetBtn != null) {
            data.top = new FormAttachment(resetBtn, 0, SWT.CENTER);
        } else {
            data.top = new FormAttachment(0, top);
        }

        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        // curRowSize = btnSize.y + ITabbedPropertyConstants.VSPACE;
        dynamicProperty.setCurRowSize(btnSize.y + ITabbedPropertyConstants.VSPACE);
        return lastControlUsed;
    }

    private Button createAdditionalButton(Composite subComposite, Button button, Point buttonSize, IElementParameter param,
            String text, String tooltip, int top, boolean enabled) {
        Button subButton = getWidgetFactory().createButton(subComposite, text, SWT.PUSH);
        subButton.setToolTipText(tooltip);

        Point subButtonnSize = subButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        subButton.addSelectionListener(listenerSelection);
        FormData data = new FormData();
        data.left = new FormAttachment(button, 0);
        data.right = new FormAttachment(button, subButtonnSize.x + ITabbedPropertyConstants.HSPACE, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = subButtonnSize.y;
        subButton.setLayoutData(data);

        subButton.setData(PARAMETER_NAME, param.getName());
        subButton.setEnabled(enabled);
        if (subButtonnSize.y > buttonSize.y) {
            buttonSize.y = subButtonnSize.y;
        }

        return subButton;

    }

    /**
     * DOC hcw Comment method "copySchemaFromChildJob".
     *
     * @param runJobNode
     * @param item
     */
    private void copySchemaFromChildJob(Node runJobNode, final Item item) {
        // 0004322: tRunJob can import the tBufferOutput schema from the son job
        if (runJobNode != null && item instanceof ProcessItem) {
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            IProcess process = service.getProcessFromProcessItem((ProcessItem) item);
            List<? extends INode> graphicalNodes = process.getGraphicalNodes();
            for (INode node : graphicalNodes) {
                if ((node != null) && node.getComponent().getName().equals("tBufferOutput")) { //$NON-NLS-1$
                    List<IMetadataTable> list = node.getMetadataList();
                    if (list.size() > 0) {
                        List<IMetadataTable> metadata = runJobNode.getMetadataList();
                        if (metadata.size() == 0) {
                            metadata.add(list.get(0).clone());
                        } else {
                            IMetadataTable table = metadata.get(0);
                            // clear schema of tRunJob, so we will replace with schema of tBufferOutput
                            table.getListColumns().clear();
                            List<IMetadataColumn> columns = list.get(0).getListColumns();
                            for (IMetadataColumn col : columns) {
                                table.getListColumns().add(col.clone());
                            }
                        }
                        // skip other tBufferOutput component
                        break;
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        super.refresh(param, check);
        // IElementParameter schemaTypeParameter = param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName());
        // // elem.getElementParameter(EParameterName.SCHEMA_TYPE.getName());
        // CCombo combo = (CCombo) hashCurControls.get(param.getName() + ":" + schemaTypeParameter.getName());
        //
        // if (combo == null || combo.isDisposed()) {
        // return;
        // }
        // Object value = schemaTypeParameter.getValue();
        //
        // if (value instanceof String) {
        // String strValue = ""; //$NON-NLS-1$
        // int nbInList = 0, nbMax = schemaTypeParameter.getListItemsValue().length;
        // String name = (String) value;
        // while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
        // if (name.equals(schemaTypeParameter.getListItemsValue()[nbInList])) {
        // strValue = schemaTypeParameter.getListItemsDisplayName()[nbInList];
        // }
        // nbInList++;
        // }
        // String[] paramItems = schemaTypeParameter.getListItemsDisplayName();
        // String[] comboItems = combo.getItems();
        // if (!paramItems.equals(comboItems)) {
        // combo.setItems(paramItems);
        // }
        // combo.setText(strValue);
        // }
        //
        // IElementParameter repositorySchemaTypeParameter = param.getChildParameters().get(
        // EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
        // Text text = (Text) hashCurControls.get(param.getName() + ":" + repositorySchemaTypeParameter.getName());
        //
        // if (text == null || text.isDisposed()) {
        // return;
        // }
        // value = repositorySchemaTypeParameter.getValue();
        //
        // if (value instanceof String) {
        // // dynamicProperty.updateRepositoryList();
        // // String strValue = ""; //$NON-NLS-1$
        // // int nbInList = 0, nbMax = repositorySchemaTypeParameter.getListItemsValue().length;
        // // String name = (String) value;
        // // while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
        // // if (name.equals(repositorySchemaTypeParameter.getListItemsValue()[nbInList])) {
        // // strValue = repositorySchemaTypeParameter.getListItemsDisplayName()[nbInList];
        // // }
        // // nbInList++;
        // // }
        // // String[] paramItems = repositorySchemaTypeParameter.getListItemsDisplayName();
        // // String[] comboItems = combo.getItems();
        // // if (!Arrays.equals(paramItems, comboItems)) {
        // // combo.setItems(paramItems);
        // // // ControlUtils.setSortedValuesForCombo(combo, paramItems);
        // // }
        // // combo.setText(strValue);
        // text.setText((String) value);
        // }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    /**
     * Find the IRepositoryObject of metadata connection thats contains current schema.
     *
     * @param schemaId
     * @return
     */
    private IRepositoryViewObject findRepositoryObject(String schemaId) {
        try {
            String[] names = schemaId.split(" - "); //$NON-NLS-1$
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            IRepositoryViewObject node = factory.getLastVersion(names[0]);
            return node;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    /**
     * Use the database table wizard to update the schema in the repository.
     *
     * @param button
     */
    private void updateRepositorySchema(Button button) {
        String paramName = (String) button.getData(PARAMETER_NAME);
        String fullParamName = paramName + ":" + getRepositoryChoiceParamName(); //$NON-NLS-1$
        IElementParameter schemaParam = elem.getElementParameter(fullParamName);
        String schemaId = (String) schemaParam.getValue();
        org.talend.core.model.metadata.builder.connection.Connection connection = MetadataToolHelper
                .getConnectionFromRepository(schemaId);
        String[] names = schemaId.split(" - "); //$NON-NLS-1$

        if (connection == null || names == null || names.length != 2) {
            // When no repository avaiable on "Repository" mode, open a MessageDialog.
            MessageDialog.openError(composite.getShell(), Messages.getString("NoRepositoryDialog.Title"), Messages //$NON-NLS-1$
                    .getString("NoRepositoryDialog.Text")); //$NON-NLS-1$
            return;
        }
        // find IRepositoryObject from repository that contains current connection
        IRepositoryViewObject node = findRepositoryObject(schemaId);

        RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(node);
        RepositoryNode metadataNode = null;

        metadataNode = findRepositoryNode(names[1], names[0], repositoryNode);
        if (metadataNode != null) {
            final IMetadataService metadataService = CorePlugin.getDefault().getMetadataService();
            if (metadataService != null) {
                metadataService.runCreateTableAction(metadataNode);
            }
        }
    }

    /**
     * yzhang Comment method "findRepositoryNode".
     *
     * @param label
     * @param root
     * @return
     */
    private RepositoryNode findRepositoryNode(String label, String id, RepositoryNode root) {
        String name = (String) root.getProperties(EProperties.LABEL);
        String rootID = root.getId();
        RepositoryNode toReturn = null;
        if (label.equals(name) && !id.equals(rootID)) {
            toReturn = root;
        } else {
            for (IRepositoryNode node : root.getChildren()) {
                toReturn = findRepositoryNode(label, id, (RepositoryNode) node);
                if (toReturn != null) {
                    break;
                }
            }
        }
        return toReturn;
    }

    /**
     * If schema type is repository, display a dialog to ask the user to change to built-in mode or update the schema in
     * the repository. Return true to stop the process.
     *
     * @param button
     */
    private boolean checkForRepositoryShema(Button button) {
        boolean stop = false;
        if (button.getData(NAME).equals(SCHEMA)) {
            String paramName = (String) button.getData(PARAMETER_NAME);
            String type = (String) elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName(), paramName);
            if (type != null && type.equals(EmfComponent.REPOSITORY)) {
                // use repository schema, pop up a dialog to ask the user for changing mode
                INode node;
                if (elem instanceof INode) {
                    node = (INode) elem;
                } else { // else instanceof Connection
                    node = ((IConnection) elem).getSource();
                }
                boolean isReadOnly = node.getProcess().isReadOnly();
                if (node.getJobletNode() != null) {
                    isReadOnly = node.isReadOnly();
                }
                ModelSelectionDialog modelSelect = new ModelSelectionDialog(button.getShell(), ESelectionType.SCHEMA, isReadOnly);
                stop = true;
                if (modelSelect.open() == ModelSelectionDialog.OK) {
                    if (modelSelect.getOptionValue() == EEditSelection.REPOSITORY) {
                        // update repository schema
                        button.setData(FORCE_READ_ONLY, false);
                        updateRepositorySchema(button);
                    } else if (modelSelect.getOptionValue() == EEditSelection.BUILDIN) {
                        // change the schema type to built in, then continue the original process
                        executeCommand(new RepositoryChangeSchemaBuiltinCommand(elem, paramName));
                        button.setData(FORCE_READ_ONLY, false);
                        stop = false;
                    } else if (modelSelect.getOptionValue() == EEditSelection.SHOW_SCHEMA) {
                        button.setData(FORCE_READ_ONLY, true);
                        stop = false;
                    }
                }
            }
        }
        return stop;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createButtonCommand(org
     * .eclipse.swt.widgets.Button)
     */
    @Override
    protected Command createButtonCommand(Button button) {
        // see 0003766: Problems with the read only mode of the properties on repository mode.
        if (checkForRepositoryShema(button)) {
            return null;
        }

        Button inputButton = button;
        IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        if (inputButton.getData(NAME).equals(SCHEMA)) {
            // this map wil hold the all input connection for the tUnite component
            Map<INode, Map<IMetadataTable, Boolean>> inputInfos = new HashMap<INode, Map<IMetadataTable, Boolean>>();

            INode node;
            if (elem instanceof Node) {
                node = (INode) elem;
            } else { // else instanceof Connection
                node = ((IConnection) elem).getSource();
            }

            IMetadataTable inputMetadata = null, inputMetaCopy = null;
            Connection inputConec = null;
            String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            IElementParameter param = node.getElementParameter(propertyName);

            IElementParameter connectionParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());
            String connectionName = null;
            if (connectionParam != null) {
                connectionName = (String) connectionParam.getValue();
            }
            Object obj = button.getData(FORCE_READ_ONLY);
            boolean forceReadOnly = false;
            if (obj != null) {
                forceReadOnly = (Boolean) obj;
            }
            boolean inputReadOnly = false, outputReadOnly = false, inputReadOnlyNode = false, inputReadOnlyParam = false;

            for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                if (connec.isActivate()
                        && (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                                || connec.getLineStyle().equals(EConnectionType.TABLE)
                                || connec.getLineStyle().equals(EConnectionType.FLOW_MERGE) || connec.getLineStyle() == EConnectionType.FLOW_REF)) {
                    if (connectionName != null && !connec.getName().equals(connectionName)) {
                        continue;
                    }
                    inputMetadata = connec.getMetadataTable();
                    inputMetaCopy = inputMetadata.clone();
                    inputConec = connec;

                    if (connec.getSource().isReadOnly()) {
                        inputReadOnlyNode = true;
                    } else {
                        for (IElementParameter curParam : connec.getSource().getElementParameters()) {
                            if (curParam.getFieldType() == EParameterFieldType.SCHEMA_TYPE) {
                                if (curParam.isReadOnly()) {
                                    inputReadOnlyParam = true;
                                }
                            }
                        }
                    }

                    // check if the inputMetadata is readonly
                    if (inputMetadata != null) {
                        for (IMetadataColumn column : inputMetadata.getListColumns(true)) {
                            IMetadataColumn columnCopied = inputMetaCopy.getColumn(column.getLabel());
                            columnCopied.setCustom(column.isCustom());
                            columnCopied.setReadOnly(column.isReadOnly());
                        }
                        inputMetaCopy.setReadOnly(inputMetadata.isReadOnly());
                        inputReadOnly = prepareReadOnlyTable(inputMetaCopy, inputReadOnlyParam, inputReadOnlyNode);
                    }

                    // store the value for Dialog
                    Map<IMetadataTable, Boolean> oneInput = new HashMap<IMetadataTable, Boolean>();
                    oneInput.put(inputMetaCopy, inputReadOnly);
                    inputInfos.put(connec.getSource(), oneInput);
                }
            }

            if (connectionParam != null && inputMetadata == null) {
                MessageDialog.openError(button.getShell(), Messages.getString("SchemaTypeController.inputNotSet"), //$NON-NLS-1$
                        Messages.getString("SchemaTypeController.connectionNotAvaliable")); //$NON-NLS-1$
                return null;
            }

            IMetadataTable originaleMetadataTable = getMetadataTableFromXml(node);
            // check if the outputMetadata is readonly
            IMetadataTable originaleOutputTable = node.getMetadataFromConnector(param.getContext());
            // when several schema_type button ,need get the right one which is opening
            IElementParameter schemaParam = param.getChildParameters().get("SCHEMA_TYPE");
            // need setRepository here
            if (!param.getContext().equals(schemaParam.getContext())) {
                schemaParam = param.getChildParameters().get("SCHEMA_TYPE");
            }
            if (schemaParam != null && EmfComponent.REPOSITORY.equals(schemaParam.getValue())) {
                if (originaleOutputTable != null && originaleOutputTable instanceof MetadataTable) {
                    ((MetadataTable) originaleOutputTable).setRepository(true);
                }
            } else if (schemaParam != null && EmfComponent.BUILTIN.equals(schemaParam.getValue())) {
                if (originaleOutputTable != null && originaleOutputTable instanceof MetadataTable) {
                    ((MetadataTable) originaleOutputTable).setRepository(false);
                }
            }

            if ("tUniservBTGeneric".equals(node.getComponent().getName())) {
                originaleOutputTable = node.getMetadataTable("OUTPUT_SCHEMA");
            }
            IMetadataTable outputMetaCopy = originaleOutputTable.clone(true);
            for (IMetadataColumn column : originaleOutputTable.getListColumns(true)) {
                IMetadataColumn columnCopied = outputMetaCopy.getColumn(column.getLabel());
                columnCopied.setCustom(column.isCustom());
                columnCopied.setReadOnly(column.isReadOnly());
                if (("tLogCatcher".equals(node.getComponent().getName()) || "tStatCatcher".equals(node.getComponent().getName())) //$NON-NLS-1$ //$NON-NLS-2$
                        && !outputMetaCopy.sameMetadataAs(originaleMetadataTable, IMetadataColumn.OPTIONS_NONE)) {
                    columnCopied.setReadOnly(false);
                }
                // setColumnLength(node, param, columnCopied);
            }
            outputMetaCopy.setReadOnly(originaleOutputTable.isReadOnly()
                    || param.isReadOnly(node.getElementParametersWithChildrens()));
            if (("tLogCatcher".equals(node.getComponent().getName()) || "tStatCatcher".equals(node.getComponent().getName())) //$NON-NLS-1$ //$NON-NLS-2$
                    && !outputMetaCopy.sameMetadataAs(originaleMetadataTable, IMetadataColumn.OPTIONS_NONE)) {
                outputMetaCopy.setReadOnly(false);
            }

            IElementParameter schemaTypeParam = param.getChildParameters().get("SCHEMA_TYPE"); //$NON-NLS-1$
            List<IElementParameterDefaultValue> defaultValues = schemaTypeParam.getDefaultValues();
            for (IElementParameterDefaultValue elementParameterDefaultValue : defaultValues) {
                if (elementParameterDefaultValue.getDefaultValue() instanceof MetadataTable) {
                    MetadataTable table = (MetadataTable) elementParameterDefaultValue.getDefaultValue();
                    outputMetaCopy.setReadOnlyColumnPosition(table.getReadOnlyColumnPosition());
                    break;
                }
            }

            outputMetaCopy.sortCustomColumns();

            if (!forceReadOnly) {
                outputReadOnly = prepareReadOnlyTable(outputMetaCopy, param.isReadOnly(), node.isReadOnly());
            } else {
                outputReadOnly = true;
            }

            // create the MetadataDialog
            MetadataDialog metaDialog = null;
            if (inputMetadata != null) {
                if (inputInfos != null && inputInfos.size() > 1 && connectionName == null) {
                    MetadataDialogForMerge metaDialogForMerge = new MetadataDialogForMerge(composite.getShell(), inputInfos,
                            outputMetaCopy, node, getCommandStack());
                    metaDialogForMerge.setText(Messages.getString("SchemaController.schemaOf") + node.getLabel()); //$NON-NLS-1$
                    metaDialogForMerge.setInputReadOnly(inputReadOnly);
                    metaDialogForMerge.setOutputReadOnly(outputReadOnly);
                    if (metaDialogForMerge.open() == MetadataDialogForMerge.OK) {
                        // inputMetaCopy = metaDialog.getInputMetaData();
                        outputMetaCopy = metaDialogForMerge.getOutputMetaData();

                        // check if the metadata is modified
                        boolean modified = false;
                        if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                            modified = true;
                        } else {
                            if (inputMetadata != null) {
                                // Notice: the Map inputInfos maybe is modified by the dialog.
                                Set<INode> inputNodes = inputInfos.keySet();
                                for (INode inputNode : inputNodes) {
                                    Map<IMetadataTable, Boolean> oneInput = inputInfos.get(inputNode);
                                    inputMetaCopy = (IMetadataTable) oneInput.keySet().toArray()[0];
                                    if (!inputMetaCopy.sameMetadataAs(inputNode.getMetadataList().get(0),
                                            IMetadataColumn.OPTIONS_NONE)) {
                                        modified = true;
                                    }
                                }

                            }
                        }

                        // create the changeMetadataCommand
                        if (modified) {
                            if (switchParam != null) {
                                switchParam.setValue(Boolean.FALSE);
                            }

                            Command changeMetadataCommand = null;

                            // only output, no input
                            if (inputInfos.isEmpty()) {
                                changeMetadataCommand = new ChangeMetadataCommand(node, param, null, null, null,
                                        originaleOutputTable, outputMetaCopy);

                            } else {
                                Set<INode> inputNodes = inputInfos.keySet();
                                int count = 0;
                                for (INode inputNode : inputNodes) {
                                    Map<IMetadataTable, Boolean> oneInput = inputInfos.get(inputNode);
                                    inputMetaCopy = (IMetadataTable) oneInput.keySet().toArray()[0];
                                    if (count == 0) {
                                        changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode, inputNode
                                                .getMetadataList().get(0), inputMetaCopy, originaleOutputTable, outputMetaCopy);
                                    } else {
                                        changeMetadataCommand = changeMetadataCommand.chain(new ChangeMetadataCommand(node,
                                                param, inputNode, inputNode.getMetadataList().get(0), inputMetaCopy,
                                                originaleOutputTable, outputMetaCopy));
                                    }
                                    count++;
                                }
                            }
                            return changeMetadataCommand;

                        }
                    }

                } else {
                    INode inputNode = (inputConec.getSource());
                    if (inputMetaCopy.getAttachedConnector() == null) {
                        INodeConnector mainConnector;
                        if (inputNode.isELTComponent()) {
                            mainConnector = inputNode.getConnectorFromType(EConnectionType.TABLE);
                        } else {
                            mainConnector = inputNode.getConnectorFromType(EConnectionType.FLOW_MAIN);
                        }
                        inputMetaCopy.setAttachedConnector(mainConnector.getName());
                    }

                    // INodeConnector outputConnector = node.getConnectorFromName(param.getContext());
                    // if (outputConnector.getMaxLinkOutput() == 0 && (originaleOutputTable.getListColumns().size() ==
                    // 0)) {
                    // metaDialog = new MetadataDialog(composite.getShell(), inputMetaCopy, inputNode,
                    // getCommandStack());
                    // } else {
                    metaDialog = new MetadataDialog(composite.getShell(), inputMetaCopy, inputNode, outputMetaCopy, node,
                            getCommandStack());
                    // }
                }
            } else {
                metaDialog = new MetadataDialog(composite.getShell(), outputMetaCopy, node, getCommandStack());
            }

            if (metaDialog != null) {
                metaDialog.setText(Messages.getString("AbstractSchemaController.schema.title", node.getLabel())); //$NON-NLS-1$
                metaDialog.setInputReadOnly(inputReadOnly);
                metaDialog.setOutputReadOnly(outputReadOnly);

                if (metaDialog.open() == MetadataDialog.OK) {
                    inputMetaCopy = metaDialog.getInputMetaData();
                    outputMetaCopy = metaDialog.getOutputMetaData();
                    boolean modified = false;
                    if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                        modified = true;
                    } else {
                        if (inputMetadata != null) {
                            if (!inputMetaCopy.sameMetadataAs(inputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                                modified = true;
                            }
                        }
                    }

                    if (modified) {
                        if (switchParam != null) {
                            switchParam.setValue(Boolean.FALSE);
                        }
                        INode inputNode = null;
                        if (inputConec != null) {
                            inputNode = inputConec.getSource();
                        }
                        ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode,
                                inputMetadata, inputMetaCopy, originaleOutputTable, outputMetaCopy);

                        return changeMetadataCommand;

                    }

                }

            }
        } else if (inputButton.getData(NAME).equals(RETRIEVE_SCHEMA)) {
            Node node = (Node) elem;
            // String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            final Command cmd = RetrieveSchemaHelper.retrieveSchemasCommand(node);
            if (switchParam != null) {
                switchParam.setValue(Boolean.FALSE);
            }
            return cmd;
        } else if (inputButton.getData(NAME).equals(RESET_COLUMNS)) {
            Node node = (Node) elem;

            String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            IElementParameter param = node.getElementParameter(propertyName);

            final Command cmd = SynchronizeSchemaHelper.createCommand(node, param);
            if (switchParam != null) {
                switchParam.setValue(Boolean.FALSE);
            }

            return cmd;
        } else if (button.getData(NAME).equals(REPOSITORY_CHOICE)) {
            String paramName = (String) button.getData(PARAMETER_NAME);
            IElementParameter schemaParam = elem.getElementParameter(paramName);

            ERepositoryObjectType type = ERepositoryObjectType.METADATA_CON_TABLE;
            String filter = schemaParam.getFilter();
            if (elem instanceof Node) {
                Node sapNode = (Node) elem;
                if (sapNode.getComponent().getName().startsWith("tSAP") && !sapNode.getComponent().getName().startsWith("tSAPHana")) { //$NON-NLS-1$ //$NON-NLS-2$
                    type = ERepositoryObjectType.METADATA_SAP_FUNCTION;
                } else if (sapNode.getComponent().getName().startsWith("tESB")) { //$NON-NLS-1$
                    filter = ERepositoryObjectType.SERVICESOPERATION.getType();
                }
            }

            RepositoryReviewDialog dialog = new RepositoryReviewDialog(button.getShell(), type, filter);
            if (dialog.open() == RepositoryReviewDialog.OK) {
                RepositoryNode node = dialog.getResult();
                while (node.getObject().getProperty().getItem() == null
                        || (!(node.getObject().getProperty().getItem() instanceof ConnectionItem))) {
                    node = node.getParent();
                }

                String id = dialog.getResult().getObject().getProperty().getId();
                String name = dialog.getResult().getObject().getLabel();// The name is Table Name.
                if (name != null) {
                    if (elem instanceof Node) {
                        Node nodeElement = (Node) elem;
                        String value = id + " - " + name; //$NON-NLS-1$
                        IMetadataTable repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(value);
                        if (nodeElement.getComponent().getName().equals("tSQLTemplateMerge")) {
                            if (paramName.equals("SCHEMA")) {
                                paramName = "SOURCE_TABLE";
                                Command dbSelectorCommand = new PropertyChangeCommand(elem, paramName,
                                        TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                                executeCommand(dbSelectorCommand);
                                Text labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));

                                paramName = "SCHEMA:REPOSITORY_SCHEMA_TYPE";
                                dbSelectorCommand = new PropertyChangeCommand(elem, paramName, TalendTextUtils.addQuotes(name));
                                executeCommand(dbSelectorCommand);
                                labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(name));
                                paramName = "SCHEMA";
                            } else if (paramName.equals("SCHEMA_TARGET")) {
                                paramName = "TARGET_TABLE";
                                Command dbSelectorCommand = new PropertyChangeCommand(elem, paramName,
                                        TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                                executeCommand(dbSelectorCommand);
                                Text labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));

                                paramName = "SCHEMA_TARGET:REPOSITORY_SCHEMA_TYPE";
                                dbSelectorCommand = new PropertyChangeCommand(elem, paramName, TalendTextUtils.addQuotes(name));
                                executeCommand(dbSelectorCommand);
                                labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(name));
                                paramName = "SCHEMA_TARGET";
                            }
                        } else if (nodeElement.getComponent().getName().startsWith("tSQLTemplate")) {
                            if (paramName.equals("SCHEMA")) {
                                paramName = "TABLE_NAME";
                                Command dbSelectorCommand = new PropertyChangeCommand(elem, paramName,
                                        TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                                executeCommand(dbSelectorCommand);
                                Text labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));

                                paramName = "SCHEMA:REPOSITORY_SCHEMA_TYPE";
                                dbSelectorCommand = new PropertyChangeCommand(elem, paramName, TalendTextUtils.addQuotes(name));
                                executeCommand(dbSelectorCommand);
                                labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(name));
                                paramName = "SCHEMA";
                            } else if (paramName.equals("SCHEMA_TARGET")) {
                                paramName = "TABLE_NAME_TARGET";
                                Command dbSelectorCommand = new PropertyChangeCommand(elem, paramName,
                                        TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                                executeCommand(dbSelectorCommand);
                                Text labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));

                                paramName = "SCHEMA_TARGET:REPOSITORY_SCHEMA_TYPE";
                                dbSelectorCommand = new PropertyChangeCommand(elem, paramName, TalendTextUtils.addQuotes(name));
                                executeCommand(dbSelectorCommand);
                                labelText = (Text) hashCurControls.get(paramName);
                                labelText.setText(TalendTextUtils.addQuotes(name));
                                paramName = "SCHEMA_TARGET";
                            }
                        } else {
                            Command dbSelectorCommand = new PropertyChangeCommand(elem, paramName,
                                    TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                            executeCommand(dbSelectorCommand);
                            Text labelText = (Text) hashCurControls.get(paramName);
                            if (labelText != null) {
                                labelText.setText(TalendTextUtils.addQuotes(repositoryMetadata.getTableName()));
                            }
                        }
                    }
                }
                String value = id + " - " + name; //$NON-NLS-1$

                String fullParamName = paramName + ":" + getRepositoryChoiceParamName(); //$NON-NLS-1$

                org.talend.core.model.metadata.builder.connection.Connection connection = null;
                if (elem instanceof Node) {
                    IMetadataTable repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(value);
                    connection = MetadataToolHelper.getConnectionFromRepository(value);

                    // For SAP see bug 5423
                    String functionId = node.getParent().getId();
                    if (((Node) elem).getUniqueName().startsWith("tSAP") && !((Node) elem).getUniqueName().startsWith("tSAPHana")//$NON-NLS-1$//$NON-NLS-2$
                            && functionId != "-1") {//$NON-NLS-1$
                        Node sapNode = (Node) elem;
                        repositoryMetadata = getMetadataFromRepository(id, functionId, name);

                        String functionName = node.getParent().getObject().getLabel();
                        for (IElementParameter param : sapNode.getElementParameters()) {
                            SAPParametersUtils.retrieveSAPParams(elem, connection, param, functionName);
                        }
                    }
                    // else {
                    // repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(value);
                    // }
                    // connection = MetadataTool.getConnectionFromRepository(value);

                    // For validation rule.
                    boolean isValRulesLost = false;
                    IRepositoryViewObject currentValRuleObj = ValidationRulesUtil.getCurrentValidationRuleObjs(elem);
                    if (currentValRuleObj != null) {
                        List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(value);
                        if (!ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, currentValRuleObj)) {
                            if (!MessageDialog.openConfirm(button.getShell(),
                                    Messages.getString("SchemaTypeController.validationrule.title.confirm"), //$NON-NLS-1$
                                    Messages.getString("SchemaTypeController.validationrule.selectMetadataMsg"))) { //$NON-NLS-1$
                                return null;
                            } else {
                                isValRulesLost = true;
                            }
                        }
                    }

                    if (repositoryMetadata == null) {
                        repositoryMetadata = new MetadataTable();
                    }
                    if (switchParam != null) {
                        switchParam.setValue(Boolean.FALSE);
                    }

                    CompoundCommand cc = new CompoundCommand();
                    RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand((Node) elem,
                            fullParamName, value, repositoryMetadata, null, null);
                    changeMetadataCommand.setConnection(connection);
                    cc.add(changeMetadataCommand);

                    if (isValRulesLost) {
                        ValidationRulesUtil.appendRemoveValidationRuleCommands(cc, elem);
                    }

                    return cc;
                }

            }
        } else if (button.getData(NAME).equals(COPY_CHILD_COLUMNS)) {
            // 0004322: tRunJob can import the tBufferOutput schema from the son job
            // 0010489 modify
            String paramName = (String) button.getData(PARAMETER_NAME);
            IElementParameter param = elem.getElementParameter(paramName);
            IElementParameter processParam = elem.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
            IElementParameter processIdParam = processParam.getChildParameters().get(
                    EParameterName.PROCESS_TYPE_PROCESS.getName());
            String id = (String) processIdParam.getValue();
            Item item = ItemCacheManager.getProcessItem(id);
            Node node = (Node) elem;
            copySchemaFromChildJob(node, item);
            // pop up the schema dialog
            MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), node.getMetadataList().get(0), node,
                    getCommandStack());
            metaDialog.setText(Messages.getString("SchemaController.schemaOf") + node.getLabel()); //$NON-NLS-1$
            if (metaDialog.open() == MetadataDialog.OK) {
                IMetadataTable outputMetaData = metaDialog.getOutputMetaData();
                return new ChangeMetadataCommand(node, param, null, outputMetaData);
            }

        }

        return null;
    }

    private RepositoryNode getTopParent(RepositoryNode node) {
        node = node.getParent();
        if (node.getObject() == null) {
            node = getTopParent(node);
        }
        return node;
    }

    private IMetadataTable getMetadataTableFromXml(INode node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
        if (param.getValue() instanceof IMetadataTable) {
            IMetadataTable table = (IMetadataTable) param.getValue();
            return table;
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
        IMetadataTable repositoryMetadata = null;

        String fullParamName = (String) combo.getData(PARAMETER_NAME);
        IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        String value = new String(""); //$NON-NLS-1$

        IElementParameter param = elem.getElementParameter(fullParamName);
        for (int j = 0; j < param.getListItemsValue().length; j++) {
            if (combo.getText().equals(param.getListItemsDisplayName()[j])) {
                value = (String) param.getListItemsValue()[j];
            }
        }

        // if change to build-in, unuse the validation rule if the component has.
        boolean isValRulesLost = false;
        IRepositoryViewObject currentValRuleObj = ValidationRulesUtil.getCurrentValidationRuleObjs(elem);
        if (value.equals(EmfComponent.BUILTIN) && currentValRuleObj != null) {
            if (!MessageDialog.openConfirm(combo.getShell(),
                    Messages.getString("SchemaTypeController.validationrule.title.confirm"), //$NON-NLS-1$
                    Messages.getString("SchemaTypeController.validationrule.selectBuildInMsg"))) { //$NON-NLS-1$
                return null;
            } else {
                isValRulesLost = true;
            }
        }

        org.talend.core.model.metadata.builder.connection.Connection connection = null;

        if (elem instanceof Node) {
            Node node = (Node) elem;
            Command baseCommand = null;
            boolean isReadOnly = false;
            String newRepositoryIdValue = null;
            if (node.getMetadataFromConnector(param.getContext()) != null) {
                isReadOnly = node.getMetadataFromConnector(param.getContext()).isReadOnly();
            }
            if (value.equals(EmfComponent.BUILTIN) && isReadOnly && !"tLogCatcher".equals(node.getComponent().getName()) //$NON-NLS-1$
                    && !"tStatCatcher".equals(node.getComponent().getName())) { //$NON-NLS-1$
                boolean hasMetadataInput = false;
                if (node.getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) > 0
                        || node.getCurrentActiveLinksNbInput(EConnectionType.TABLE) > 0) {
                    hasMetadataInput = true;
                }
                repositoryMetadata = new MetadataTable();

                if (hasMetadataInput) {
                    for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                        if (connec.isActivate()
                                && (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN) || connec.getLineStyle().equals(
                                        EConnectionType.TABLE))) {
                            repositoryMetadata = connec.getMetadataTable().clone();
                        }
                    }

                }
            } else if (value.equals(EmfComponent.REPOSITORY)) {
                // Map<String, IMetadataTable> repositoryTableMap = dynamicProperty.getRepositoryTableMap();
                IElementParameter property = ((Node) elem).getElementParameter(EParameterName.PROPERTY_TYPE.getName());
                if ((property != null) && EmfComponent.REPOSITORY.equals(property.getValue())) {
                    String propertySelected = (String) ((Node) elem).getElementParameter(
                            EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).getValue();
                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    /* 16969 */
                    Item item = null;
                    try {
                        IRepositoryViewObject repobj = factory.getLastVersion(propertySelected);
                        if (repobj != null) {
                            Property tmpproperty = repobj.getProperty();
                            if (tmpproperty != null) {
                                item = tmpproperty.getItem();
                            }
                        }
                        // item = factory.getLastVersion(propertySelected).getProperty().getItem();
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                    if (item != null && item instanceof ConnectionItem) {

                        final ConnectionItem connectionItem = (ConnectionItem) item;
                        if (connectionItem != null) {
                            connection = connectionItem.getConnection();
                        }
                    }
                }

                IElementParameter repositorySchemaType = param.getParentParameter().getChildParameters()
                        .get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                String schemaSelected = (String) repositorySchemaType.getValue();

                /* value can be devided means the value like "connectionid - label" */
                String[] keySplitValues = schemaSelected.toString().split(" - "); //$NON-NLS-1$
                if (keySplitValues.length > 1) {
                    String connectionId = keySplitValues[0];
                    String tableLabel = keySplitValues[1];
                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    Item item = null;
                    try {
                        IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                        if (repobj != null) {
                            Property tmpproperty = repobj.getProperty();
                            if (tmpproperty != null) {
                                item = tmpproperty.getItem();
                            }
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                    if (item != null && item instanceof ConnectionItem) {

                        final ConnectionItem connectionItem = (ConnectionItem) item;
                        if (connectionItem != null) {
                            connection = connectionItem.getConnection();
                        }
                    }
                    if (item != null && item instanceof ConnectionItem) {
                        boolean findTable = false;
                        for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                                .getTables(connection)) {
                            if (table.getLabel().equals(tableLabel)) {
                                repositoryMetadata = ConvertionHelper.convert(table);
                                newRepositoryIdValue = schemaSelected;
                                findTable = true;
                                break;
                            }
                        }
                        if (!findTable) {
                            repositoryMetadata = new MetadataTable();
                        }
                    } else {
                        repositoryMetadata = new MetadataTable();
                    }
                } else { // value only got a empty string
                    repositoryMetadata = new MetadataTable();
                }
                /* see bug 16969 */
                // if (repositoryTableMap.containsKey(schemaSelected)) {
                // repositoryMetadata = repositoryTableMap.get(schemaSelected);
                // // bug 6028, Display the parameter of REPOSITORY_SCHEMA_TYPE
                // newRepositoryIdValue = schemaSelected;// + " - " + repositoryMetadata.getLabel();
                // } else {
                // if (repositoryTableMap.keySet().size() == 0) {
                // repositoryMetadata = new MetadataTable();
                // } else {
                // newRepositoryIdValue = repositoryTableMap.keySet().iterator().next();
                // // Gets the schema of the first item in repository schema type combo.
                // repositoryMetadata = repositoryTableMap.get(newRepositoryIdValue);
                // // bug 6028, Display the parameter of REPOSITORY_SCHEMA_TYPE
                // // newRepositoryIdValue = newRepositoryIdValue + " - " + repositoryMetadata.getLabel();
                // }
                // }

            } else {
                baseCommand = new PropertyChangeCommand(elem, fullParamName, value);
            }
            if (switchParam != null) {
                switchParam.setValue(Boolean.FALSE);
            }

            CompoundCommand cc = new CompoundCommand();

            if (baseCommand != null) {
                cc.add(baseCommand);
            } else {
                RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand((Node) elem,
                        fullParamName, value, repositoryMetadata, newRepositoryIdValue, null);
                changeMetadataCommand.setConnection(connection);
                cc.add(changeMetadataCommand);
            }

            // unuse the validation rules of the component.
            if (isValRulesLost) {
                ValidationRulesUtil.appendRemoveValidationRuleCommands(cc, elem);
            }

            return cc;
        }

        return null;
    }

    /**
     * Created by Marvin Wang on Jun. 19, 2012 for getting metadatatable only for SAP.
     *
     * @param connectionId
     * @param functionId
     * @param tableName
     * @return
     */
    public static org.talend.core.model.metadata.IMetadataTable getMetadataFromRepository(String connectionId, String functionId,
            String tableName) {
        org.talend.core.model.metadata.builder.connection.MetadataTable table = MetadataToolHelper
                .getMetadataTableFromSAPFunction(connectionId, functionId, tableName);
        if (table != null) {
            return ConvertionHelper.convert(table);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#
     * getRepositoryChoiceParamName ()
     */
    @Override
    protected String getRepositoryChoiceParamName() {
        return EParameterName.REPOSITORY_SCHEMA_TYPE.getName();
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
        return EParameterName.SCHEMA_TYPE.getName();
    }

    // @Override
    // protected String getDisplayNameFromValue(IElementParameter param, String value) {
    // super.getDisplayNameFromValue(param, value);
    // if (value == null || "".equals(value)) { //$NON-NLS-1$
    // return null;
    // }
    // Item item = param.getLinkedRepositoryItem();
    // org.talend.core.model.metadata.builder.connection.MetadataTable table = null;
    // if (item != null) {
    // // item not match
    // table = UpdateRepositoryUtils.getTableById(item, value);
    // }
    // if (item == null || table == null) {
    // // research
    // item = UpdateRepositoryUtils.getConnectionItemByChildId(dynamicProperty.getRepositoryConnectionItemMap(), value);
    // if (item != null) {
    // table = UpdateRepositoryUtils.getTableById(item, value);
    // }
    // }
    //
    // if (table != null && item != null) {
    // return dynamicProperty.getRepositoryAliasName((ConnectionItem) item) + ":" + item.getProperty().getLabel() + " -
    // " //$NON-NLS-1$ //$NON-NLS-2$
    // + table.getLabel();
    // }
    //
    // return null;
    // }

    /**
     * Change the schema type to built in.
     */
    class RepositoryChangeSchemaBuiltinCommand extends Command {

        private IElement elem;

        private String propertyName;

        public RepositoryChangeSchemaBuiltinCommand(IElement elem, String propertyName) {
            this.elem = elem;
            this.propertyName = propertyName;
            setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        }

        @Override
        public void execute() {
            // Force redraw of Commponents propoerties
            elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));
            IElementParameter param = elem.getElementParameter(propertyName);
            IElementParameter schemaTypeParam = param.getChildParameters().get("SCHEMA_TYPE"); //$NON-NLS-1$
            schemaTypeParam.setRepositoryValueUsed(false);
            schemaTypeParam.setReadOnly(false);
            elem.setPropertyValue(param.getName() + ":SCHEMA_TYPE", EmfComponent.BUILTIN); //$NON-NLS-1$
        }

        @Override
        public void undo() {
            // Force redraw of Commponents propoerties
            elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));
            IElementParameter param = elem.getElementParameter(propertyName);
            IElementParameter schemaTypeParam = param.getChildParameters().get("SCHEMA_TYPE"); //$NON-NLS-1$
            schemaTypeParam.setRepositoryValueUsed(true);
            schemaTypeParam.setReadOnly(true);
            elem.setPropertyValue(param.getName() + ":SCHEMA_TYPE", EmfComponent.REPOSITORY); //$NON-NLS-1$
        }

    }

}
