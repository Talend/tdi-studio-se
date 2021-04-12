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
package org.talend.designer.xmlmap.parts.directedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.runtime.services.IExpressionBuilderDialogService;
import org.talend.designer.gefabstractmap.figures.VarNodeTextLabel;
import org.talend.designer.gefabstractmap.figures.cells.IComboCell;
import org.talend.designer.gefabstractmap.figures.cells.IExpressionBuilderCell;
import org.talend.designer.gefabstractmap.figures.cells.ITextAreaCell;
import org.talend.designer.gefabstractmap.figures.cells.ITextCell;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.xmlmap.figures.treesettings.TreeSettingsConstant;
import org.talend.designer.xmlmap.figures.treetools.zone.XmlMapSearchZoneToolBar;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.tree.IUILookupMode;
import org.talend.designer.xmlmap.model.tree.IUIMatchingMode;
import org.talend.designer.xmlmap.model.tree.LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_MATCHING_MODE;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlMapNodeDirectEditManager extends DirectEditManager {

    private GraphicalEditPart source;

    private CellEditorLocator locator;

    private Object model;

    private Map<CellEditor, DirectEditType> cellAndType = new HashMap<CellEditor, DirectEditType>();

    private final String[] joinModel = new String[] { TreeSettingsConstant.INNER_JOIN, TreeSettingsConstant.LEFT_OUTER_JOIN };

    public XmlMapNodeDirectEditManager(GraphicalEditPart source, CellEditorLocator locator) {
        super(source, null, locator);
        this.source = source;
        this.locator = locator;
        model = source.getModel();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.DirectEditManager#initCellEditor()
     */
    @Override
    protected void initCellEditor() {
        DirectEditType directEditType = cellAndType.get(getCellEditor());
        if (model instanceof AbstractNode) {
            final AbstractNode abstractNode = (AbstractNode) model;
            if (directEditType != null) {
                switch (directEditType) {
                case EXPRESSION:
                    String expression = abstractNode.getExpression() == null ? "" : abstractNode.getExpression();
                    getCellEditor().setValue(expression);
                    Text text = ((ExtendedTextCellEditor) getCellEditor()).getTextControl();
                    text.selectAll();
                    break;
                case NODE_NAME:
                    String variable = abstractNode.getName();
                    if (variable == null) {
                        variable = "";
                    }
                    getCellEditor().setValue(variable);
                    final Text nametext = (Text) ((TextCellEditor) getCellEditor()).getControl();
                    nametext.selectAll();
                    if (model instanceof VarNode) {
                        nametext.addModifyListener(new ModifyListener() {

                            @Override
                            public void modifyText(ModifyEvent e) {
                                List<VarNode> children = new ArrayList<VarNode>();
                                children.addAll(((VarTable) abstractNode.eContainer()).getNodes());
                                children.remove(model);
                                String message = XmlMapUtil.isValidColumnName(children, ((Text) e.widget).getText());
                                if (message != null) {
                                    nametext.setBackground(ColorConstants.red);
                                } else {
                                    nametext.setBackground(ColorConstants.white);
                                }
                            }
                        });

                    }
                    break;
                case VAR_NODE_TYPE:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(getTypeDisplayValue((VarNode) abstractNode));
                    }
                    break;
                }

            }
        } else if (model instanceof InputXmlTree) {
            InputXmlTree inputxmlTree = (InputXmlTree) model;
            if (directEditType != null) {
                switch (directEditType) {
                case EXPRESSION_FILTER:
                    String expressionFilter = inputxmlTree.getExpressionFilter();
                    if (expressionFilter == null) {
                        expressionFilter = "";
                    }
                    getCellEditor().setValue(expressionFilter);
                    Text textarea = ((ExtendedTextCellEditor) getCellEditor()).getTextControl();
                    textarea.selectAll();
                    break;
                case LOOKUP_MODEL:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(getLookupDisplayName(inputxmlTree.getLookupMode()));
                    }
                    break;
                case MATCH_MODEL:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(getMatchModelDisplayName(inputxmlTree.getMatchingMode()));
                    }
                    break;
                case JOIN_MODEL:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        String join = "";
                        if (inputxmlTree.isInnerJoin()) {
                            join = joinModel[0];
                        } else {
                            join = joinModel[1];
                        }
                        combo.setText(join);
                    }
                    break;
                case PERSISTENT_MODEL:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(String.valueOf(inputxmlTree.isPersistent()));
                    }
                    break;

                }
            }

        } else if (model instanceof OutputXmlTree) {
            OutputXmlTree outputTree = (OutputXmlTree) model;
            if (directEditType != null) {
                switch (directEditType) {
                case EXPRESSION_FILTER:
                    String expressionFilter = outputTree.getExpressionFilter();
                    if (expressionFilter == null) {
                        expressionFilter = "";
                    }
                    getCellEditor().setValue(expressionFilter);
                    Text textarea = ((ExtendedTextCellEditor) getCellEditor()).getTextControl();
                    textarea.selectAll();
                    break;
                case OUTPUT_REJECT:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(String.valueOf(outputTree.isReject()));
                    }
                    break;
                case LOOK_UP_INNER_JOIN_REJECT:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(String.valueOf(outputTree.isRejectInnerJoin()));
                    }
                    break;
                case ALL_IN_ONE:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(String.valueOf(outputTree.isAllInOne()));
                    }
                    break;
                case ENABLE_EMPTY_ELEMENT:
                    if (getCellEditor() instanceof ComboBoxCellEditor) {
                        CCombo combo = (CCombo) getCellEditor().getControl();
                        combo.setText(String.valueOf(outputTree.isEnableEmptyElement()));
                    }
                }
            }

        } else if (model instanceof XmlMapData) {
            XmlMapData xmlMapData = (XmlMapData) model;
            if (directEditType != null) {
                switch (directEditType) {
                case SERACH:
                    Text text = (Text) getCellEditor().getControl();
                    text.selectAll();
                    break;
                }
            }
        }
    }

    private String getTypeDisplayValue(VarNode varNode) {
        JavaType javaType = JavaTypesManager.getJavaTypeFromId(varNode.getType());
        Class primitiveClass = javaType.getPrimitiveClass();
        Boolean nullable = varNode.isNullable();
        String displayedValue = null;
        if (primitiveClass != null && !nullable.equals(Boolean.TRUE)) {
            displayedValue = primitiveClass.getSimpleName();
        } else if (varNode.getType().equals(JavaTypesManager.DIRECTORY.getId())
                || varNode.getType().equals(JavaTypesManager.FILE.getId())
                || varNode.getType().equals(JavaTypesManager.VALUE_LIST.getId())) {
            displayedValue = javaType.getLabel();
        } else {
            displayedValue = javaType.getNullableClass().getSimpleName();
        }
        if (displayedValue == null) {
            displayedValue = JavaTypesManager.getDefaultJavaType().getLabel();
        }
        return displayedValue;
    }

    private String getLookupDisplayName(String lookupModel) {
        IUILookupMode[] availableJoins = { XML_MAP_LOOKUP_MODE.LOAD_ONCE, XML_MAP_LOOKUP_MODE.RELOAD,
                XML_MAP_LOOKUP_MODE.CACHE_OR_RELOAD };
        for (IUILookupMode model : availableJoins) {
            if (model.toString().equals(lookupModel)) {
                return model.getLabel();
            }
        }
        return lookupModel;
    }

    private String getMatchModelDisplayName(String matcheModel) {
        IUIMatchingMode[] allMatchingModel = XML_MAP_MATCHING_MODE.values();
        for (IUIMatchingMode model : allMatchingModel) {
            if (model.toString().equals(matcheModel)) {
                return model.getLabel();
            }
        }
        return matcheModel;
    }

    @Override
    protected CellEditor createCellEditorOn(Composite composite) {
        Composite parent = (Composite) source.getViewer().getControl();
        CellEditor cellEditor = null;
        Figure figure = null;
        if (this.locator instanceof XmlMapNodeCellEditorLocator) {
            XmlMapNodeCellEditorLocator lo = (XmlMapNodeCellEditorLocator) locator;
            figure = lo.getFigure();
        }
        if (figure instanceof IComboCell) {
            try {
                // tree setting can be edit or not
                if (source instanceof InputXmlTreeEditPart) {
                    InputXmlTree inputTree = (InputXmlTree) ((InputXmlTreeEditPart) source).getModel();
                    if (DirectEditType.JOIN_MODEL.equals(((IComboCell) figure).getDirectEditType())) {
                        if (!XmlMapUtil.hasAtLeastOneHashKey(inputTree)) {
                            return null;
                        }
                    }
                    if (DirectEditType.PERSISTENT_MODEL.equals(((IComboCell) figure).getDirectEditType())) {
                        if (LOOKUP_MODE.CACHE_OR_RELOAD.toString().equals(inputTree.getLookupMode())) {
                            return null;
                        }
                    }
                }

                if (source instanceof OutputXmlTreeEditPart) {
                    OutputXmlTree outputTree = (OutputXmlTree) ((OutputXmlTreeEditPart) source).getModel();
                    if (DirectEditType.ALL_IN_ONE.equals(((IComboCell) figure).getDirectEditType())) {
                        if (!XmlMapUtil.hasDocument(outputTree)) {
                            return null;
                        }
                        boolean hasAggregate = false;
                        for (int i = 0; i < outputTree.getNodes().size(); i++) {
                            OutputTreeNode outputTreeNode = outputTree.getNodes().get(i);
                            hasAggregate = hasAggreage(outputTreeNode);
                            if (hasAggregate) {
                                break;
                            }
                        }
                        if (hasAggregate) {
                            return null;
                        }
                    } else if (DirectEditType.ENABLE_EMPTY_ELEMENT.equals(((IComboCell) figure).getDirectEditType())) {
                        if (!XmlMapUtil.hasDocument(outputTree)) {
                            return null;
                        }
                    }

                }

                cellEditor = new XmlComboCellEditor();
                cellEditor.create(composite);
                ((XmlComboCellEditor) cellEditor).setItems(getComboItemsByType(((IComboCell) figure).getDirectEditType()));

                cellAndType.put(cellEditor, ((IComboCell) figure).getDirectEditType());
            } catch (Exception e) {
                return null;
            }
        } else if (figure instanceof ITextCell) {
            // this one is created for direct doc child name , no use anymore...
            cellEditor = new TextCellEditor(composite);
            cellAndType.put(cellEditor, ((ITextCell) figure).getDirectEditType());
            // for the search
            XmlMapNodeCellEditorLocator lo = (XmlMapNodeCellEditorLocator) locator;
            if (lo.getFigure() != null && lo.getFigure() instanceof VarNodeTextLabel) {
                figure = (VarNodeTextLabel) lo.getFigure();
                if (figure.getParent() != null && figure.getParent() instanceof XmlMapSearchZoneToolBar) {
                    XmlMapSearchZoneToolBar searchZone = (XmlMapSearchZoneToolBar) figure.getParent();
                    if (searchZone.getSearchMaps().size() > 0) {
                        searchZone.getSearchZoneMapper().hightlightAll(searchZone.getSearchMaps(), false);
                        searchZone.getSearchZoneMapper().setHightlightAll(false);
                        searchZone.getSearchMaps().clear();
                        searchZone.hightLightAll.setSelected(false);
                    }
                }
            }
        } else if (figure instanceof IExpressionBuilderCell && model instanceof AbstractNode) {
            IService expressionBuilderDialogService = GlobalServiceRegister.getDefault().getService(
                    IExpressionBuilderDialogService.class);
            CellEditorDialogBehavior behavior = new CellEditorDialogBehavior();
            cellEditor = new ExpressionCellEditor(composite, behavior, source, DirectEditType.EXPRESSION);
            ((ExpressionCellEditor) cellEditor).setOwnerId(((AbstractNode) model).getExpression());
            IExpressionBuilderDialogController dialog = ((IExpressionBuilderDialogService) expressionBuilderDialogService)
                    .getExpressionBuilderInstance(parent, (ExpressionCellEditor) cellEditor, null);
            cellAndType.put(cellEditor, DirectEditType.EXPRESSION);
            behavior.setCellEditorDialog(dialog);
        } else if (figure instanceof ITextAreaCell) {
            TextAreaBehavior behavior = new TextAreaBehavior();
            cellEditor = new ExpressionCellEditor(composite, behavior, source, DirectEditType.EXPRESSION_FILTER);
            cellAndType.put(cellEditor, DirectEditType.EXPRESSION_FILTER);
        } else if (figure instanceof IExpressionBuilderCell && model instanceof InputXmlTree) {
            IService expressionBuilderDialogService = GlobalServiceRegister.getDefault().getService(
                    IExpressionBuilderDialogService.class);
            CellEditorDialogBehavior behavior = new CellEditorDialogBehavior();
            cellEditor = new ExpressionCellEditor(composite, behavior, source, DirectEditType.EXPRESSION);
            ((ExpressionCellEditor) cellEditor).setOwnerId(((InputXmlTree) model).getLookupMode());
            IExpressionBuilderDialogController dialog = ((IExpressionBuilderDialogService) expressionBuilderDialogService)
                    .getExpressionBuilderInstance(parent, (ExpressionCellEditor) cellEditor, null);
            cellAndType.put(cellEditor, DirectEditType.EXPRESSION);
            behavior.setCellEditorDialog(dialog);
        }

        // }
        return cellEditor;
    }

    private boolean hasAggreage(OutputTreeNode outputTreeNode) {
        if (outputTreeNode.isAggregate()) {
            return true;
        } else if (!outputTreeNode.getChildren().isEmpty()) {
            for (int i = 0; i < outputTreeNode.getChildren().size(); i++) {
                OutputTreeNode child = (OutputTreeNode) outputTreeNode.getChildren().get(i);
                boolean hasAggreage = hasAggreage(child);
                if (hasAggreage) {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    protected boolean isDirty() {
        return super.isDirty();
    }

    @Override
    protected Object getDirectEditFeature() {
        return cellAndType.get(getCellEditor());
    }

    private String[] getComboItemsByType(DirectEditType type) {
        if (type == null) {
            return new String[0];
        }

        switch (type) {
        case VAR_NODE_TYPE:
            String[] items = JavaTypesManager.getJavaTypesLabels();
            return items;
        case LOOKUP_MODEL:
            IUILookupMode[] availableJoins = { XML_MAP_LOOKUP_MODE.LOAD_ONCE, XML_MAP_LOOKUP_MODE.RELOAD,
                    XML_MAP_LOOKUP_MODE.CACHE_OR_RELOAD };
            String names[] = new String[availableJoins.length];
            for (int i = 0; i < availableJoins.length; i++) {
                names[i] = availableJoins[i].getLabel();
            }
            return names;
        case MATCH_MODEL:
            IUIMatchingMode[] matchModel = getMatchModel();
            String names2[] = new String[matchModel.length];
            for (int i = 0; i < matchModel.length; i++) {
                names2[i] = (matchModel[i].getLabel());
            }
            return names2;
        case JOIN_MODEL:
            return joinModel;
        case PERSISTENT_MODEL:
            return new String[] { String.valueOf(Boolean.TRUE), String.valueOf(Boolean.FALSE) };
        case OUTPUT_REJECT:
        case LOOK_UP_INNER_JOIN_REJECT:
        case ALL_IN_ONE:
        case ENABLE_EMPTY_ELEMENT:
            return new String[] { String.valueOf(Boolean.TRUE), String.valueOf(Boolean.FALSE) };
        default:
            return new String[0];
        }

    }

    private IUIMatchingMode[] getMatchModel() {
        IUIMatchingMode[] allMatchingModel = XML_MAP_MATCHING_MODE.values();
        List<IUIMatchingMode> avilable = new ArrayList<IUIMatchingMode>();
        if (model instanceof InputXmlTree) {
            InputXmlTree inputTree = (InputXmlTree) model;
            for (int i = 0; i < allMatchingModel.length; ++i) {
                IUIMatchingMode matchingMode = allMatchingModel[i];
                final String text = matchingMode.getLabel();

                if (text.length() != 0) {
                    if (inputTree.getMatchingMode().equals(XML_MAP_MATCHING_MODE.ALL_ROWS.toString())) {
                        if (matchingMode.equals(XML_MAP_MATCHING_MODE.ALL_ROWS)) {
                            avilable.add(matchingMode);
                        }
                    } else {
                        if (!matchingMode.equals(XML_MAP_MATCHING_MODE.ALL_ROWS)) {
                            avilable.add(matchingMode);
                        }
                    }
                }
            }
        }

        return avilable.toArray(new IUIMatchingMode[avilable.size()]);
    }

    @Override
    public void commit() {
        DirectEditType directEditType = cellAndType.get(getCellEditor());
        if (directEditType != null) {
            switch (directEditType) {
            case SERACH:
                VarNodeTextLabel figure = null;
                if (this.locator instanceof XmlMapNodeCellEditorLocator) {
                    XmlMapNodeCellEditorLocator lo = (XmlMapNodeCellEditorLocator) locator;
                    if (lo.getFigure() != null && lo.getFigure() instanceof VarNodeTextLabel) {
                        figure = (VarNodeTextLabel) lo.getFigure();
                        Object searchTextObject = getDirectEditRequest().getCellEditor().getValue();
                        if (searchTextObject != null) {
                            if (figure.getParent() != null && figure.getParent() instanceof XmlMapSearchZoneToolBar) {
                                XmlMapSearchZoneToolBar searchZone = (XmlMapSearchZoneToolBar) figure.getParent();
                                searchZone.search(searchTextObject.toString());
                                figure.setText(searchTextObject.toString());
                            }
                        }
                    }
                }
            }
        }
        super.commit();
    }

    @Override
    public void showFeedback() {
        getEditPart().showSourceFeedback(getDirectEditRequest());
    }

    @Override
    public CellEditor getCellEditor() {
        return super.getCellEditor();
    }
}
