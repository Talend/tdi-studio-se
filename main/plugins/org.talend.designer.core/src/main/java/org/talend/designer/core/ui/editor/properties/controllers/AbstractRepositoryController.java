// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
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
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public abstract class AbstractRepositoryController extends AbstractElementPropertySectionController {

    protected static final String REPOSITORY_CHOICE = "REPOSITORY_CHOICE"; //$NON-NLS-1$

    private static final int STANDARD_REPOSITORY_WIDTH = 250;

    /**
     * DOC nrousseau AbstractRepositoryController constructor comment.
     *
     * @param dp
     */
    public AbstractRepositoryController(IDynamicProperty dp) {
        super(dp);
    }

    protected abstract String getRepositoryTypeParamName();

    protected abstract String getRepositoryChoiceParamName();

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
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        CCombo combo;

        Control lastControlUsed = lastControl;

        combo = new CCombo(subComposite, SWT.BORDER);
        IElementParameter propertyTypeParameter = param.getChildParameters().get(getRepositoryTypeParamName());
        FormData data;
        String[] originalList = propertyTypeParameter.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        for (String element : originalList) {
            stringToDisplay.add(element);
        }
        combo.setItems(stringToDisplay.toArray(new String[0]));
        combo.setEditable(false);
        combo.setEnabled(!propertyTypeParameter.isReadOnly());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + propertyTypeParameter.getVariableName());
        }
        if (!propertyTypeParameter.isReadOnly()) {
            if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE
                    || param.getFieldType() == EParameterFieldType.SCHEMA_TYPE
                    || param.getFieldType() == EParameterFieldType.QUERYSTORE_TYPE) {
                combo.setEnabled(ExtractMetaDataUtils.getInstance().haveLoadMetadataNode());
            }
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, propertyTypeParameter.getDisplayName());
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
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(propertyTypeParameter.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (param.isRepositoryValueUsed()) {
            param.setReadOnly(true);
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
        data.top = new FormAttachment(0, top);
        combo.setLayoutData(data);
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName() + ":" + propertyTypeParameter.getName()); //$NON-NLS-1$
        lastControlUsed = combo;

        String propertyType = (String) propertyTypeParameter.getValue();
        param.setReadOnly(propertyTypeParameter.isReadOnly());
        if (propertyType != null && propertyType.equals(EmfComponent.REPOSITORY)) {
            lastControlUsed = addRepositoryChoice(subComposite, lastControlUsed, numInRow, nbInRow, top, param);
        }

        // **********************
        hashCurControls.put(param.getName() + ":" + propertyTypeParameter.getName(), combo); //$NON-NLS-1$

        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return lastControlUsed;
    }

    private Control addRepositoryChoice(Composite subComposite, Control lastControl, int numInRow, int nbInRow, int top,
            IElementParameter param) {
        FormData data;

        IElementParameter repositoryParameter = param.getChildParameters().get(getRepositoryChoiceParamName());

        Text labelText;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();

        labelText.setData(PARAMETER_NAME, param.getName());

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(false);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        // *********************
        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, STANDARD_REPOSITORY_WIDTH, SWT.RIGHT);
        // data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);

        Button btn;
        Point btnSize;

        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btn.addSelectionListener(listenerSelection);
        btn.setData(NAME, REPOSITORY_CHOICE);
        btn.setData(PARAMETER_NAME, param.getName());
        btn.setEnabled(!param.isReadOnly());
        data = new FormData();
        data.left = new FormAttachment(cLayout, 0);
        data.right = new FormAttachment(cLayout, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        // **********************

        hashCurControls.put(param.getName() + ":" + repositoryParameter.getName(), labelText); //$NON-NLS-1$

        return btn;
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing.
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand(e);
            if (cmd instanceof ChangeMetadataCommand) {
                ((ChangeMetadataCommand) cmd).setConnection(getConnection());
            }
            executeCommand(cmd);
        }
    };

    /**
     *
     * DOC wzhang Comment method "getConnection".
     *
     * @return
     */
    private Connection getConnection() {
        if (this.elem == null) {
            return null;
        }
        if (elem instanceof Node) {
            IElementParameter elementParameter = ((Node) elem).getElementParameter(EParameterName.PROPERTY_TYPE.getName());
            if (elementParameter != null && !EmfComponent.BUILTIN.equals(elementParameter.getValue())) {
                String propertyValue = (String) (((Node) elem)
                        .getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()));
                IRepositoryViewObject lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(propertyValue);
                if (lastVersion != null) {
                    final Item item = lastVersion.getProperty().getItem();
                    if (item != null && item instanceof ConnectionItem) {
                        Connection repositoryConn = ((ConnectionItem) item).getConnection();
                        return repositoryConn;
                    }
                }
            }
        }
        return null;

    }

    private Command createCommand(SelectionEvent selectionEvent) {
        if (selectionEvent.getSource() instanceof Button) {
            return createButtonCommand((Button) selectionEvent.getSource());
        }
        if (selectionEvent.getSource() instanceof CCombo) {
            return createComboCommand((CCombo) selectionEvent.getSource());
        }
        return null;
    }

    protected abstract Command createButtonCommand(Button button);

    protected abstract Command createComboCommand(CCombo combo);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        CCombo combo = new CCombo(subComposite, SWT.BORDER);
        Point initialSize = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        combo.dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    protected ConnectionItem lastItemUsed;

    private FileItem lastFileItemUsed; // hywang add for 6484

    private LinkRulesItem lastLinkItem;

    private void fastRepositoryUpdateProperty(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
            String linkedRepository = (String) param.getValue();
            // value stored is the id, so we can get this item directly
            Item item;
            String displayName = "";
            try {
                IRepositoryViewObject object = factory.getLastVersion(linkedRepository.split(" - ")[0]);
                if (object == null) {
                    return;
                }
                item = object.getProperty().getItem();
                // Assert.isTrue(item instanceof ConnectionItem);
                IESBService service = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                    service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                }
                if (service != null && ERepositoryObjectType.getItemType(item) == service.getServicesType()) {
                    lastItemUsed = (ConnectionItem) item;
                    displayName = "Service:" + service.getServiceLabel(item, linkedRepository);
                } else if (item instanceof ConnectionItem) {
                    lastItemUsed = (ConnectionItem) item;
                    displayName = dynamicProperty.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                            + lastItemUsed.getProperty().getLabel();
                }
                if (item instanceof FileItem) { // hywang add for 6484
                    lastFileItemUsed = (FileItem) item;
                    if (lastFileItemUsed instanceof RulesItem) {
                        displayName = "Rules:" //$NON-NLS-1$
                                + lastFileItemUsed.getProperty().getLabel();
                    }
                }
                if (item instanceof LinkRulesItem) {
                    lastLinkItem = (LinkRulesItem) item;
                    displayName = "Rules:" //$NON-NLS-1$
                            + lastLinkItem.getProperty().getLabel();

                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

            param.setListItemsDisplayName(new String[] { displayName });
            param.setListItemsValue(new String[] { (String) param.getValue() });
        }

    }

    private void fastRepositoryUpdateSchema(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            String queryIdAndName = (String) param.getValue();
            String[] names = queryIdAndName.split(" - "); //$NON-NLS-1$
            if (names.length < 2) {
                return;
            }
            String linkedRepository = names[0];
            String tableName = null;
            if (names.length == 2) {
                tableName = names[1];
            } else if (names.length > 2) {
                tableName = queryIdAndName.substring(linkedRepository.length() + 3);
            }

            if (lastItemUsed != null) {
                if (!linkedRepository.equals(lastItemUsed.getProperty().getId())) {
                    lastItemUsed = null;
                }
            }
            if (lastItemUsed == null) {
                IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
                Item item;
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object == null) {
                        return;
                    }
                    item = object.getProperty().getItem();
                    Assert.isTrue(item instanceof ConnectionItem);
                    lastItemUsed = (ConnectionItem) item;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }

            // EList<MetadataTable> tableList = lastItemUsed.getConnection().getTables();
            // for (MetadataTable table )

            String displayName = dynamicProperty.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                    + lastItemUsed.getProperty().getLabel() + " - " + tableName; //$NON-NLS-1$

            param.setListItemsDisplayName(new String[] { displayName });
            param.setListItemsValue(new String[] { (String) param.getValue() });
        }
    }

    private void fastRepositoryUpdateQuery(IElementParameter param) {
        if (param != null && param.getValue() != null) {
            String queryIdAndName = (String) param.getValue();
            String[] names = queryIdAndName.split(" - "); //$NON-NLS-1$
            if (names.length < 2) {
                return;
            }
            String linkedRepository = names[0];
            String queryName = null;
            if (names.length == 2) {
                queryName = names[1];
            } else if (names.length > 2) {
                queryName = queryIdAndName.substring(linkedRepository.length() + 3);
            }
            if (lastItemUsed != null) {
                if (!linkedRepository.equals(lastItemUsed.getProperty().getId())) {
                    lastItemUsed = null;
                }
            }
            if (lastItemUsed == null) {
                IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
                Item item;
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object == null) {
                        return;
                    }
                    item = object.getProperty().getItem();
                    lastItemUsed = (ConnectionItem) item;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
            Assert.isTrue(lastItemUsed instanceof DatabaseConnectionItem);
            QueriesConnection queriesConnection = ((DatabaseConnection) lastItemUsed.getConnection()).getQueries();
            EList<Query> queries = queriesConnection.getQuery();

            String repositoryAliasName = dynamicProperty.getRepositoryAliasName(lastItemUsed);
            for (Query currentQuery : queries) {
                if (currentQuery.getLabel().equals(queryName)) {
                    String displayName = repositoryAliasName + ":" //$NON-NLS-1$
                            + lastItemUsed.getProperty().getLabel() + " - " + currentQuery.getLabel(); //$NON-NLS-1$

                    param.setListItemsDisplayName(new String[] { displayName });
                    param.setListItemsValue(new String[] { (String) param.getValue() });
                    /* query cache should be deleted ,bug 16969 */
                    // dynamicProperty.getRepositoryQueryStoreMap().clear();
                    // dynamicProperty.getRepositoryQueryStoreMap().put((String) param.getValue(), currentQuery);
                }
            }
        }
    }

    protected void fastInitializeRepositoryNames(IElementParameter param) {
        lastItemUsed = null;

        if (param.getValue() == null || param.getValue().equals("")) {
            return;
        }
        if (param.getName().equals(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
            fastRepositoryUpdateProperty(param);
        } else if (param.getName().equals(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())) {
            fastRepositoryUpdateSchema(param);
        } else if (param.getName().equals(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())) {
            fastRepositoryUpdateQuery(param);
        }
    }

    protected String getDisplayNameFromValue(IElementParameter param, String value) {
        // to load informations from repository only if needed.

        int index = param.getIndexOfItemFromList(value);
        if (index == -1) {

            fastInitializeRepositoryNames(param);
            // if even after the initialize there is nothing, just return an empty string
            if (param.getListItemsDisplayName().length == 0) {
                return ""; //$NON-NLS-1$
            }
            index = param.getIndexOfItemFromList(value);
            if (index == -1) {
                return ""; //$NON-NLS-1$
            }
        }

        return param.getListItemsDisplayName()[index];
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
        IElementParameter propertyTypeParameter = param.getChildParameters().get(getRepositoryTypeParamName());
        CCombo combo = (CCombo) hashCurControls.get(param.getName() + ":" + propertyTypeParameter.getName()); //$NON-NLS-1$

        if (combo == null || combo.isDisposed()) {
            return;
        }
        Object value = propertyTypeParameter.getValue();

        if (value instanceof String) {
            String strValue = ""; //$NON-NLS-1$
            int nbInList = 0, nbMax = propertyTypeParameter.getListItemsValue().length;
            String name = (String) value;
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(propertyTypeParameter.getListItemsValue()[nbInList])) {
                    strValue = propertyTypeParameter.getListItemsDisplayName()[nbInList];
                }
                nbInList++;
            }
            String[] paramItems = propertyTypeParameter.getListItemsDisplayName();
            String[] comboItems = combo.getItems();
            if (!paramItems.equals(comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText(strValue);
        }

        IElementParameter repositoryChoiceParameter = param.getChildParameters().get(getRepositoryChoiceParamName());
        Text text = (Text) hashCurControls.get(param.getName() + ":" + repositoryChoiceParameter.getName()); //$NON-NLS-1$

        if (text == null || text.isDisposed()) {
            return;
        }
        value = repositoryChoiceParameter.getValue();

        if (value instanceof String) {
            String toDisplay = getDisplayNameFromValue(repositoryChoiceParameter, (String) value);
            if (toDisplay == null || "".equals(value)) { //$NON-NLS-1$
                text.setText(""); //$NON-NLS-1$
            } else {
                text.setText(toDisplay);
            }
        }
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

}
