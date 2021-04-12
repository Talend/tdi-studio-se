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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQPatternService;
import org.talend.core.ITDQRuleService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ComboController.java 1 2006-12-12 涓嬪崍01:58:48 +0000 (涓嬪崍01:58:48) yzhang $
 *
 */
public class ComboController extends AbstractElementPropertySectionController {

    /**
     * DOC dev ColumnListController constructor comment.
     *
     * @param parameterBean
     */
    public ComboController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createComboCommand(SelectionEvent event) {
        Set<String> elementsName;
        Control ctrl;
        elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }
                CCombo combo = (CCombo) event.getSource();

                Object data = ctrl.getData(PARAMETER_NAME);
                if (!(ctrl instanceof CCombo)) {
                    continue;
                }
                boolean isDisposed = ((CCombo) ctrl).isDisposed() || combo.isDisposed();
                if (isDisposed) {
                    continue;
                }
                if (data != null && data.equals(combo.getData(PARAMETER_NAME))) {
                    if (!((CCombo) ctrl).getText().equals(elem.getPropertyValue(name))) {

                        String value = new String(""); //$NON-NLS-1$
                        List<? extends IElementParameter> params = elem.getElementParametersWithChildrens();
                        for (int i = 0; i < params.size(); i++) {
                            IElementParameter param = params.get(i);
                            if (param.getName().equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (j < param.getListItemsDisplayName().length
                                            && ((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                    }
                                }
                            }
                        }
                        if (value.equals(elem.getPropertyValue(name))) { // same value so no need to do anything
                            return null;
                        }
                        return new PropertyChangeCommand(elem, name, value);
                    }
                }
            }
        }
        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (canAddRepositoryDecoration(param)) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("ComboController.valueFromRepository")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setItems(getListToDisplay(param));
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName());
        int nbLines = param.getNbLines();
        if (nbLines > 5) {
            combo.setVisibleItemCount(nbLines);
        }
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

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
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
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
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        if (isInWizard()) {
            labelLabel.setAlignment(SWT.RIGHT);
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
            if (nbInRow == 1) {
                data.left = new FormAttachment(100, -initialSize.x);
            } else {
                data.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow),
                        currentLabelWidth + ITabbedPropertyConstants.HSPACE);
            }

            data = (FormData) labelLabel.getLayoutData();
            data.right = new FormAttachment(cLayout, 0);
            data.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), 0);

            return labelLabel;
        }
        if (EnvironmentUtils.isMacOsSytem()) {
            data = (FormData) labelLabel.getLayoutData();
            data.top = new FormAttachment(cLayout, -3, SWT.TOP);
        }
        return cLayout;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    // /**
    // * This method is used for creating a Button named "Guess Query".
    // *
    // * @param subComposite
    // * @param lastControl
    // * @param numInRow
    // * @param top
    // */
    // private void addGuessQueryButton(Composite subComposite, IElementParameter param, Control lastControl, int
    // numInRow, int top) {
    // final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {
    //
    // public Control createControl(Composite parent, int style) {
    // return new Button(parent, style);
    // }
    // });
    // Button guessQueryButton = null;
    // Control buttonControl = dField1.getLayoutControl();
    // guessQueryButton = (Button) dField1.getControl();
    // guessQueryButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    // buttonControl.setBackground(subComposite.getBackground());
    // guessQueryButton.setEnabled(true);
    // guessQueryButton.setData(NAME, GUESS_QUERY_NAME);
    // guessQueryButton.setData(PARAMETER_NAME, param.getName());
    // guessQueryButton.setText(GUESS_QUERY_NAME);
    //
    // FormData data1 = new FormData();
    // data1.left = new FormAttachment(lastControl, 210);
    // data1.top = new FormAttachment(0, top);
    //
    // buttonControl.setLayoutData(data1);
    // guessQueryButton.addSelectionListener(listenerSelection);
    // queryButton.put(param, guessQueryButton);
    //
    // }

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
            // dynamicProperty.updateRepositoryList();
            Command cmd = createCommand(event);
            executeCommand(cmd);
            IElementParameter elementParameterFromField = elem.getElementParameterFromField(EParameterFieldType.MEMO_SQL);
            if (elementParameterFromField != null) {
                Object value = elementParameterFromField.getValue();
                Object object = hashCurControls.get(elementParameterFromField.getName());
                if (object instanceof ColorStyledText) {
                    ColorStyledText text = (ColorStyledText) object;
                    text.setText(value.toString());
                }
            }
            // dynamicProperty.refresh();

        }
    };

    /**
     * This method is used for getting command base on component type.
     *
     * @param e
     * @return
     */
    private Command createCommand(SelectionEvent event) {
        Command cmd = null;
        if (event.getSource() instanceof CCombo) {
            cmd = createComboCommand(event);
        } else if (event.getSource() instanceof Button) {
            cmd = createButtonCommand();
        }
        return cmd;
    }

    /**
     * This method is used for "Guess Query" button.
     *
     * @return
     */
    private Command createButtonCommand() {
        IMetadataTable repositoryMetadata = null;
        IMetadataTable newRepositoryMetadata = null;
        String realTableName = null;
        String realTableId = null;

        // Only for getting the real table name.
        if (elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName()).equals(EmfComponent.REPOSITORY)) {
            String paramName;
            IElementParameter repositorySchemaTypeParameter = elem.getElementParameter(EParameterName.REPOSITORY_SCHEMA_TYPE
                    .getName());
            Object repositoryControl = hashCurControls.get(repositorySchemaTypeParameter.getName());

            paramName = EParameterName.REPOSITORY_SCHEMA_TYPE.getName();

            if (repositoryControl != null) {

                String selectedComboItem = ((CCombo) repositoryControl).getText();
                if (selectedComboItem != null && selectedComboItem.length() > 0) {
                    String value = new String(""); //$NON-NLS-1$
                    for (int i = 0; i < elem.getElementParameters().size(); i++) {
                        IElementParameter param = elem.getElementParameters().get(i);
                        if (param.getName().equals(paramName)) {
                            for (int j = 0; j < param.getListItemsValue().length; j++) {
                                if (selectedComboItem.equals(param.getListItemsDisplayName()[j])) {
                                    value = (String) param.getListItemsValue()[j];
                                }
                            }
                        }
                    }
                    if (elem instanceof Node) {

                        // this.dynamicProperty.updateRepositoryList();
                        String connectionId = value.toString().split(" - ")[0];
                        String tableLabel = value.toString().split(" - ")[1];
                        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        Item item = null;
                        try {
                            IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                            if (repobj != null) {
                                Property property = repobj.getProperty();
                                if (property != null) {
                                    item = property.getItem();
                                }
                            }
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                        if (item != null && item instanceof ConnectionItem) {
                            boolean findTable = false;
                            Connection connection = ((ConnectionItem) item).getConnection();
                            for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                                    .getTables(connection)) {
                                if (table.getLabel().equals(tableLabel)) {
                                    repositoryMetadata = ConvertionHelper.convert(table);
                                    findTable = true;
                                    break;
                                }
                            }
                            if (!findTable) {
                                repositoryMetadata = new MetadataTable();
                            }
                        }

                        // if (repositoryTableMap.containsKey(value)) {
                        // repositoryMetadata = repositoryTableMap.get(value);
                        // realTableName = repositoryMetadata.getTableName();
                        // realTableId = repositoryMetadata.getId();
                        // } else {
                        // repositoryMetadata = new MetadataTable();
                        // }
                    }
                }
            }
        }// Ends

        QueryGuessCommand cmd = null;
        INode node = null;
        if (elem instanceof INode) {
            node = (INode) elem;
        } else { // else instanceof Connection
            node = ((IConnection) elem).getSource();
        }

        newRepositoryMetadata = node.getMetadataList().get(0);

        if (newRepositoryMetadata == null) {
            String schemaSelected = (String) node.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            if (schemaSelected != null) {

                String connectionId = schemaSelected.toString().split(" - ")[0];
                String tableLabel = schemaSelected.toString().split(" - ")[1];
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                Item item = null;
                try {
                    IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                    if (repobj != null) {
                        Property property = repobj.getProperty();
                        if (property != null) {
                            item = property.getItem();
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                if (item != null && item instanceof ConnectionItem) {
                    boolean findTable = false;
                    Connection connection = ((ConnectionItem) item).getConnection();
                    for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                            .getTables(connection)) {
                        if (table.getLabel().equals(tableLabel)) {
                            repositoryMetadata = ConvertionHelper.convert(table);
                            findTable = true;
                            break;
                        }
                    }
                    if (!findTable) {
                        repositoryMetadata = new MetadataTable();
                    }
                }

                // repositoryMetadata = repositoryTableMap.get(schemaSelected);
            } else if (newRepositoryMetadata == null) {
                MessageDialog.openWarning(DisplayUtils.getDefaultShell(false),
                        Messages.getString("ComboController.alert"), Messages.getString("ComboController.nothingGuess")); //$NON-NLS-1$ //$NON-NLS-2$
                return cmd;
            }
        }
        cmd = new QueryGuessCommand(node, newRepositoryMetadata);

        /* parameter can be null,the setMaps won't use the third parameter */
        cmd.setMaps(dynamicProperty.getTableIdAndDbTypeMap(), dynamicProperty.getTableIdAndDbSchemaMap(), null);
        String type = getValueFromRepositoryName("TYPE"); //$NON-NLS-1$
        cmd.setParameters(realTableId, realTableName, type);

        return cmd;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        String paramName = param.getName();
        boolean isPatternList = StringUtils.equals(paramName, "PATTERN_LIST");
        boolean isRule = StringUtils.equals(paramName, "DQRULES_LIST");
        CCombo combo = (CCombo) hashCurControls.get(paramName);

        if (combo == null || combo.isDisposed()) {
            return;
        }

        // for feature 8147
        // if it is pattern,then it can get ITDQPatternService's instance
        if (isPatternList) {
            ITDQPatternService service = null;
            try {
                service = (ITDQPatternService) GlobalServiceRegister.getDefault().getService(ITDQPatternService.class);
            } catch (RuntimeException e) {
                // nothing to do
            }
            if (service != null && elem instanceof Node) {

                Node node = (Node) elem;

                IElementParameter typeParam = node.getElementParameter("TYPE");

                service.overridePatternList(typeParam, param);
            }
        }
        if (isRule) {
            ITDQRuleService service = null;
            try {
                service = (ITDQRuleService) GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
            } catch (RuntimeException e) {
                // nothing to do
            }
            if (service != null) {
                service.fillTDQRuleList(param);
            }
        }

        Object value = param.getValue();
        if (value instanceof String) {
            String strValue = ""; //$NON-NLS-1$
            int nbInList = 0, nbMax = param.getListItemsValue().length;
            String name = (String) value;
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(param.getListItemsValue()[nbInList])) {
                    strValue = param.getListItemsDisplayName()[nbInList];
                }
                nbInList++;
            }
            String[] paramItems = getListToDisplay(param);
            String[] comboItems = combo.getItems();

            if (!Arrays.equals(paramItems, comboItems)) {
                combo.setItems(paramItems);
            }
            // bug 19837
            if (isPatternList || isRule) {
                combo.setText("".equals(strValue) ? (String) value : strValue);
            } else {
                if (!StringUtils.isEmpty(strValue)) {
                    combo.setText(strValue);
                } else {
                    combo.setText((String) value);
                }
            }

            combo.setVisible(true);
        }

        if (param.isContextMode()) {
            combo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
            combo.setEnabled(false);
        }

    }

    private String[] getListToDisplay(IElementParameter param) {
        callBeforeActive(param);
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (int i = 0; originalList != null && i < originalList.length; i++) {
                stringToDisplay.add(originalList[i]);
            }
        }

        // MOD msjian 2012-3-20 TDQ-1340: sort regex pattern list, revert 19836 MOD by gdbu
        return stringToDisplay.toArray(new String[0]);
        // MOD gdbu 2011-6-1 bug : 19836
        // String[] toArray = stringToDisplay.toArray(new String[0]);
        // String paramName = param.getName();
        // if (!"VALIDATION_TYPE".equals(paramName)) { // TDI-19822 don't sort for VALIDATION_TYPE parameter
        // Arrays.sort(toArray, String.CASE_INSENSITIVE_ORDER);
        // }
        // return toArray;
        // // ~19836
        // TDQ-1340~
    }
}
