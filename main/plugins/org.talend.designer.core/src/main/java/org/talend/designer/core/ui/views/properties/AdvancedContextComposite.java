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
package org.talend.designer.core.ui.views.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.eclipse.emf.common.ui.celleditor.ExtendedComboBoxCellEditor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.CopyPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButtonForExtendedTable;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.DesignerUtilities;

/**
 * yzhang class global comment. Detailled comment
 */
public class AdvancedContextComposite extends ScrolledComposite implements IDynamicProperty {

    private static final String CODE_PROPERTY = "codeProperty"; //$NON-NLS-1$

    private static final String NAME_PROPERTY = "nameProperty"; //$NON-NLS-1$

    private Node node;

    private List<IElementParameter> comboContent = new ArrayList<IElementParameter>();

    private final List<IElementParameter> legalParameters = new ArrayList<IElementParameter>();

    /**
     * yzhang AdvancedContextComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public AdvancedContextComposite(Composite parent, int style, final Element element) {
        super(parent, style);
        node = (Node) element;
        setExpandHorizontal(true);
        setExpandVertical(true);

        FormData layouData = new FormData();
        layouData.left = new FormAttachment(0, 0);
        layouData.right = new FormAttachment(100, 0);
        layouData.top = new FormAttachment(0, 0);
        layouData.bottom = new FormAttachment(100, 0);
        setLayoutData(layouData);

        final Composite panel = new Composite(this, SWT.NONE);
        setContent(panel);
        panel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

        FormLayout layout = new FormLayout();
        layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
        layout.marginHeight = ITabbedPropertyConstants.VSPACE;
        layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
        panel.setLayout(layout);

        ExtendedTableModel<IElementParameter> model = new ExtendedTableModel<IElementParameter>();
        AdvancedContextTableView tableViewer = new AdvancedContextTableView(panel, model, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.MULTI, node);
        if (node.getProcess() instanceof IProcess2) {
            IProcess2 process = (IProcess2) node.getProcess();
            if (process instanceof IGEFProcess) {
                tableViewer.getExtendedTableViewer().setCommandStack(((IGEFProcess) process).getCommandStack());
            }
            if (node.getJobletNode() != null) {
                tableViewer.setReadOnly(node.isReadOnly());
            } else {
                tableViewer.setReadOnly(process.isReadOnly());
            }

        }

        FormData tableLayoutData = new FormData();
        tableLayoutData.left = new FormAttachment(10, 0);
        tableLayoutData.right = new FormAttachment(90, 0);
        tableLayoutData.top = new FormAttachment(5, 0);
        tableLayoutData.bottom = new FormAttachment(90, 0);
        tableViewer.getMainComposite().setLayoutData(tableLayoutData);

        final Table table = tableViewer.getTable();
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.minimumHeight = 80;
        layoutData.minimumWidth = 300;
        table.setLayoutData(layoutData);

        setMinSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        needContextModeParameters();

        //
        List<IElementParameter> tableContent = new ArrayList<IElementParameter>();
        for (IElementParameter parameter : element.getElementParameters()) {
            if (parameter.isContextMode()) {
                tableContent.add(parameter);
            }
        }
        tableViewer.getTableViewerCreator().setInputList(tableContent);

        comboContent.addAll(calculateComboContent(tableViewer, legalParameters));

        tableViewer.getTableViewerCreator().refresh();
        tableViewer.getExtendedToolbar().updateEnabledStateOfButtons();

        // add CSS class
        CoreUIPlugin.setCSSClass(this, AdvancedContextComposite.class.getSimpleName());
    }

    private void needContextModeParameters() {
        legalParameters.clear();
        for (IElementParameter parameter : node.getElementParameters()) {
            if (parameter.isDynamicSettings()
                    && parameter.isShow(node.getElementParameters())
                    && parameter.getCategory() != EComponentCategory.TECHNICAL
                    && (parameter.getFieldType() == EParameterFieldType.CHECK
                            || parameter.getFieldType() == EParameterFieldType.CLOSED_LIST
                            || parameter.getFieldType() == EParameterFieldType.MODULE_LIST
                            || parameter.getFieldType() == EParameterFieldType.RADIO || parameter.getFieldType() == EParameterFieldType.COMPONENT_LIST)) {
                legalParameters.add(parameter);
            }
        }
    }

    /**
     * yzhang Comment method "calculateComboContent".
     *
     * @param tableViewer
     * @param legalParameters
     * @return
     */
    private List<IElementParameter> calculateComboContent(AdvancedContextTableView tableViewer,
            List<IElementParameter> legalParameters) {
        List<IElementParameter> tableInput = tableViewer.getTableViewerCreator().getInputList();
        List<IElementParameter> content = new ArrayList<IElementParameter>(legalParameters);
        content.removeAll(tableInput);
        return content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getCurRowSize()
     */
    @Override
    public int getCurRowSize() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getElement()
     */
    @Override
    public Element getElement() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getHashCurControls()
     */
    @Override
    public BidiMap getHashCurControls() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getPart()
     */
    @Override
    public AbstractMultiPageTalendEditor getPart() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getQueriesMap()
     */
    public Map<String, List<String>> getQueriesMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryAliasName(org
     * .talend.core.model.properties.ConnectionItem)
     */
    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        return null;
    }

    /* 16969 */
    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // *
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryConnectionItemMap
    // * ()
    // */
    // public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // *
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryQueryStoreMap()
    // */
    // public Map<String, Query> getRepositoryQueryStoreMap() {
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryTableMap()
    // */
    // public Map<String, IMetadataTable> getRepositoryTableMap() {
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getSection()
     */
    @Override
    public EComponentCategory getSection() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getTablesMap()
     */
    public Map<String, List<String>> getTablesMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        if (!isDisposed()) {
            getParent().layout();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#setCurRowSize(int)
     */
    @Override
    public void setCurRowSize(int i) {

    }

    /**
     *
     * ggu AdvancedContextTableView class global comment. Detailled comment
     */
    class AdvancedContextTableView extends AbstractDataTableEditorView<IElementParameter> {

        private Node node;

        private TextCellEditorWithProposal codeCellEditor;

        private DynamicComboBoxCellEditor nameCellEditor;

        public AdvancedContextTableView(Composite parentComposite, ExtendedTableModel<IElementParameter> model,
                int mainCompositeStyle, Node node) {
            super(parentComposite, mainCompositeStyle, model, false, true, true, false);
            this.node = node;
            initGraphicComponents();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#createColumns(org.talend.commons
         * .ui.swt.tableviewer.TableViewerCreator, org.eclipse.swt.widgets.Table)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void createColumns(TableViewerCreator<IElementParameter> tableViewerCreator, Table table) {

            TableViewerCreatorColumn column;
            // name
            column = new TableViewerCreatorColumn(tableViewerCreator);
            column.setTitle(Messages.getString("AdvancedContextComposite.Name")); //$NON-NLS-1$
            column.setId(NAME_PROPERTY);
            column.setWidth(250);
            column.setModifiable(true);
            needContextModeParameters();

            nameCellEditor = new DynamicComboBoxCellEditor(table, legalParameters, comboboxCellEditorLabelProvider);
            column.setCellEditor(nameCellEditor);
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<IElementParameter, Object>() {

                @Override
                public String get(IElementParameter bean) {
                    return bean.getDisplayName();
                }

                @Override
                public void set(IElementParameter bean, Object value) {
                    if (value != null && value instanceof IElementParameter) {
                        List<IElementParameter> tableInput = getTableViewerCreator().getInputList();
                        int i = tableInput.indexOf(bean);
                        tableInput.remove(i);
                        bean.setContextMode(false);
                        IElementParameter newBean = (IElementParameter) value;
                        tableInput.add(i, newBean);
                        newBean.setContextMode(true);

                        // add related
                        List<IElementParameter> associateParams = DesignerUtilities.findRadioParamInSameGroup(comboContent, newBean);
                        for (IElementParameter param : associateParams) {
                            param.setContextMode(true);
                            tableInput.add(param);
                        }
                        // set dirty
                        executeCommand(new Command() {
                        });

                        refreshComboContent(AdvancedContextTableView.this, legalParameters);
                        getExtendedToolbar().updateEnabledStateOfButtons();
                        getTableViewerCreator().refresh();
                    }
                }

            });
            column.setColumnCellModifier(new ColumnCellModifier(column) {

                @Override
                public boolean canModify(Object bean) {
                    if (super.canModify(bean) && !comboContent.isEmpty()) {
                        List<IElementParameter> associateParams = DesignerUtilities.findRadioParamInSameGroup(getTableViewerCreator()
                                .getInputList(), (IElementParameter) bean);
                        return associateParams.isEmpty();
                    }
                    return false;
                }

                @Override
                public Object getValue(Object bean) {
                    refreshComboContent(AdvancedContextTableView.this, legalParameters);
                    comboContent.add(0, (IElementParameter) bean);
                    nameCellEditor.refresh();
                    return bean;
                }

            });

            // code
            column = new TableViewerCreatorColumn(tableViewerCreator);
            column.setTitle(Messages.getString("AdvancedContextComposite.Code")); //$NON-NLS-1$
            column.setId(CODE_PROPERTY);
            column.setWidth(200);
            column.setModifiable(true);

            codeCellEditor = new TextCellEditorWithProposal(tableViewerCreator.getTable(), SWT.BORDER, column);
            codeCellEditor.setContentProposalProvider(new TalendProposalProvider(node.getProcess(), node));
            column.setCellEditor(codeCellEditor);

            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<IElementParameter, Object>() {

                @Override
                public Object get(IElementParameter bean) {
                    final Object value = bean.getValue();
                    return value == null ? "" : String.valueOf(value); //$NON-NLS-1$
                }

                @Override
                public void set(IElementParameter bean, Object value) {
                    if (value != null && !value.equals(bean.getValue())) {
                        executeCommand(new PropertyChangeCommand(node, bean.getName(), value));
                        getTableViewerCreator().refresh();
                    }
                }

            });
        }

        private void executeCommand(Command cmd) {
            CommandStack cmdStack = getTableViewerCreator().getCommandStack();
            if (cmdStack != null) {
                cmdStack.execute(cmd);
            } else {
                cmd.execute();
            }
        }

        /**
         * yzhang Comment method "refreshComboContent".
         *
         * @param tableViewer
         * @param legalParameters
         */
        private void refreshComboContent(AdvancedContextTableView tableViewer, List<IElementParameter> legalParameters) {
            comboContent.clear();
            comboContent.addAll(calculateComboContent(tableViewer, legalParameters));
        }

        @Override
        protected ExtendedToolbarView initToolBar() {

            return new ExtendedToolbarView(getMainComposite(), SWT.NONE, getExtendedTableViewer()) {

                @Override
                protected AddPushButton createAddPushButton() {
                    return new AddPushButtonForExtendedTable(toolbar, extendedTableViewer) {

                        @Override
                        protected Object getObjectToAdd() {
                            return null; // un-used
                        }

                        @Override
                        protected Command getCommandToExecute() {
                            return new Command() {

                                @Override
                                public void execute() {
                                    if (comboContent.size() > 0) {
                                        List<IElementParameter> tableInput = getTableViewerCreator().getInputList();
                                        IElementParameter parameter = comboContent.get(0);
                                        tableInput.add(parameter);

                                        parameter.setContextMode(true);

                                        List<IElementParameter> associateParams = DesignerUtilities.findRadioParamInSameGroup(comboContent,
                                                parameter);
                                        for (IElementParameter param : associateParams) {
                                            param.setContextMode(true);
                                            tableInput.add(param);
                                        }

                                        refreshComboContent(AdvancedContextTableView.this, legalParameters);
                                        getExtendedToolbar().updateEnabledStateOfButtons();
                                        getTableViewerCreator().refresh();
                                    }
                                }
                            };

                        }

                        @Override
                        public boolean getEnabledState() {
                            if (super.getEnabledState()) {
                                List<IElementParameter> inputList = getTableViewerCreator().getInputList();
                                return inputList != null && inputList.size() < legalParameters.size();
                            }
                            return false;
                        }

                    };
                }

                @Override
                protected RemovePushButton createRemovePushButton() {
                    return new RemovePushButtonForExtendedTable(toolbar, extendedTableViewer) {

                        @Override
                        protected Command getCommandToExecute() {
                            return new Command() {

                                @Override
                                public void execute() {
                                    ISelection selection = getTableViewerCreator().getTableViewer().getSelection();
                                    List<IElementParameter> tableViewerInput = getTableViewerCreator().getInputList();
                                    boolean needRefresh = false;
                                    if (!selection.isEmpty() && selection instanceof StructuredSelection) {
                                        Object[] elements = ((StructuredSelection) selection).toArray();
                                        for (Object element : elements) {
                                            tableViewerInput.remove(element);
                                            removeAssociateParams(tableViewerInput, element);
                                            IElementParameter elementParameter = (IElementParameter) element;
                                            elementParameter.setContextMode(false);
                                            if (elementParameter.getFieldType() == EParameterFieldType.COMPONENT_LIST) {
                                                executeCommand(new PropertyChangeCommand(node, elementParameter.getName(), "")); //$NON-NLS-1$
                                            }
                                        }
                                        needRefresh = true;
                                    } else if (!tableViewerInput.isEmpty()) {
                                        int index = tableViewerInput.size() - 1;
                                        final IElementParameter elementParameter = tableViewerInput.get(index);
                                        elementParameter.setContextMode(false);
                                        tableViewerInput.remove(index);
                                        removeAssociateParams(tableViewerInput, elementParameter);
                                        if (elementParameter.getFieldType() == EParameterFieldType.COMPONENT_LIST) {
                                            executeCommand(new PropertyChangeCommand(node, elementParameter.getName(), "")); //$NON-NLS-1$
                                        }
                                        needRefresh = true;
                                    }

                                    if (needRefresh) {
                                        refreshComboContent(AdvancedContextTableView.this, legalParameters);
                                        getTableViewerCreator().refresh();
                                        getExtendedToolbar().updateEnabledStateOfButtons();
                                    }
                                }

                            };
                        }

                        @Override
                        public boolean getEnabledState() {
                            return !getExtendedControlViewer().isReadOnly() && !getTableViewerCreator().getInputList().isEmpty();
                        }

                    };
                }

                @Override
                protected MoveDownPushButton createMoveDownPushButton() {
                    return null;
                }

                @Override
                protected MoveUpPushButton createMoveUpPushButton() {
                    return null;
                }

                @Override
                protected CopyPushButton createCopyPushButton() {
                    return null;
                }

                private void removeAssociateParams(List<IElementParameter> tableViewerInput, Object element) {
                    List<IElementParameter> associateParams = DesignerUtilities.findRadioParamInSameGroup(tableViewerInput,
                            (IElementParameter) element);
                    for (IElementParameter param : associateParams) {
                        param.setContextMode(false);
                        tableViewerInput.remove(param);
                    }
                }
            };
        }

        /**
         *
         * YeXiaowei Comment method "findRadioParamInSameGroup".
         *
         * @param param
         * @return
         */


        /**
         * yzhang AdvancedContextComposite class global comment. Detailled comment
         */
        private class DynamicComboBoxCellEditor extends ExtendedComboBoxCellEditor {

            /**
             * yzhang ComboBoxCellEditor constructor comment.
             *
             * @param composite
             * @param list
             * @param labelProvider
             */
            public DynamicComboBoxCellEditor(Composite composite, List<?> list, ILabelProvider labelProvider) {
                super(composite, list, labelProvider);
            }

            public void refresh() {
                refreshItems(""); //$NON-NLS-1$
            }

        }

        private ILabelProvider comboboxCellEditorLabelProvider = new ILabelProvider() {

            @Override
            public Image getImage(Object element) {
                return null;
            }

            @Override
            public String getText(Object element) {
                return ((IElementParameter) element).getDisplayName();
            }

            @Override
            public void addListener(ILabelProviderListener listener) {

            }

            @Override
            public void dispose() {

            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener) {

            }

        };
    }
}
