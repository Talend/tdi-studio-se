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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.ui.celleditor.ExtendedComboBoxCellEditor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.SQLPatternUtils;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;

/**
 * bqian class global comment. Detailled comment
 */
public class SQLPatternComposite extends ScrolledComposite implements IDynamicProperty, IResourceChangeListener {

    private static final String NAME_PROPERTY = "nameProperty"; //$NON-NLS-1$

    private static final int ADD_COLUMN = 0;

    private List<SQLPatternInfor> comboContent;

    private Element element;

    private DynamicComboBoxCellEditor dynamicComboBoxCellEditor;

    private Button buttonAdd, upButton, downButton;

    private Button buttonRemove;

    private TableViewer tableViewer;

    private Text codeText;

    private Map<SQLPatternItem, Project> sqlPatternAndProject = new HashMap<SQLPatternItem, Project>();

    /**
     * yzhang AdvancedContextComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public SQLPatternComposite(Composite parent, int style, final Element element) {
        super(parent, style);
        this.element = element;
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

        tableViewer = new TableViewer(panel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        FormData tableLayoutData = new FormData();
        tableLayoutData.left = new FormAttachment(10, 0);
        tableLayoutData.right = new FormAttachment(90, 0);
        tableLayoutData.top = new FormAttachment(10, 0);
        tableLayoutData.bottom = new FormAttachment(50, 0);

        final Table table = tableViewer.getTable();

        table.setLayoutData(tableLayoutData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn columnName = new TableColumn(table, SWT.NONE, ADD_COLUMN);
        columnName.setText(Messages.getString("SQLPatternComposite.sqlPattern")); //$NON-NLS-1$
        columnName.setWidth(200);
        columnName.setResizable(true);
        columnName.setMoveable(true);

        Image addImage = ImageProvider.getImage(EImage.ADD_ICON);
        Image removeImage = ImageProvider.getImage(EImage.DELETE_ICON);

        buttonAdd = new Button(panel, SWT.NONE);
        buttonAdd.setText(Messages.getString("SQLPatternComposite.add")); //$NON-NLS-1$
        buttonAdd.setImage(addImage);

        FormData buttonAddData = new FormData();
        buttonAddData.left = new FormAttachment(table, 10, SWT.LEFT);
        buttonAddData.top = new FormAttachment(table, 2);
        buttonAdd.setLayoutData(buttonAddData);

        buttonRemove = new Button(panel, SWT.NONE);
        buttonRemove.setText(Messages.getString("SQLPatternComposite.delete")); //$NON-NLS-1$
        buttonRemove.setImage(removeImage);

        FormData buttonRemoveData = new FormData();
        buttonRemoveData.left = new FormAttachment(buttonAdd, 1, SWT.RIGHT);
        buttonRemoveData.top = new FormAttachment(table, 2);
        buttonRemove.setLayoutData(buttonRemoveData);

        upButton = new Button(panel, SWT.NONE);
        upButton.setText(Messages.getString("SQLPatternComposite.up")); //$NON-NLS-1$
        upButton.setImage(ImageProvider.getImage(EImage.UP_ICON));
        FormData upFormData = new FormData();
        upFormData.left = new FormAttachment(buttonRemove, 1, SWT.RIGHT);
        upFormData.top = new FormAttachment(table, 2);
        upButton.setLayoutData(upFormData);

        downButton = new Button(panel, SWT.NONE);
        downButton.setText(Messages.getString("SQLPatternComposite.down")); //$NON-NLS-1$
        downButton.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
        FormData downFormData = new FormData();
        downFormData.left = new FormAttachment(upButton, 1, SWT.RIGHT);
        downFormData.top = new FormAttachment(table, 2);
        downButton.setLayoutData(downFormData);

        createCodeControl(panel, buttonAdd);
        setMinSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        TableViewerProvider provider = new TableViewerProvider();
        tableViewer.setContentProvider(provider);
        tableViewer.setLabelProvider(provider);
        addTableSorter(tableViewer, columnName);

        tableViewer.setCellModifier(new ICellModifier() {

            @Override
            public boolean canModify(Object element, String property) {
                return comboContent.size() > 0;
            }

            @Override
            public Object getValue(Object element, String property) {

                if (property.equals(NAME_PROPERTY)) {
                    Map map = (Map) element;
                    String id = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                    id = id.split(SQLPatternUtils.ID_SEPARATOR)[0];

                    IRepositoryViewObject repositoryObject = SQLPatternUtils.getLastVersionRepositoryObjectById(id);
                    String label = repositoryObject.getLabel();

                    refreshComboContent(tableViewer, false);
                    SQLPatternInfor infor = null;
                    for (SQLPatternInfor sqlPatternInfor : comboContent) {
                        if (sqlPatternInfor.getCompoundId().equals(id)) {
                            infor = sqlPatternInfor;
                            break;
                        }
                    }
                    if (infor == null) {
                        infor = new SQLPatternInfor(id + SQLPatternUtils.ID_SEPARATOR + label, label);
                    }
                    comboContent.add(infor);
                    dynamicComboBoxCellEditor.refresh();
                    return infor;
                }
                return ""; //$NON-NLS-1$
            }

            @Override
            public void modify(Object elem, String property, Object value) {
                TableItem item = null;
                if (elem instanceof Table) {
                    item = ((Table) elem).getItem(0);
                } else if (elem instanceof TableItem) {
                    item = (TableItem) elem;
                }

                if (item != null && value != null) {
                    if (property.equals(NAME_PROPERTY)) {
                        Map map = (Map) item.getData();

                        SQLPatternInfor sqlPatternInfor = (SQLPatternInfor) value;
                        if (map.get(SQLPatternUtils.SQLPATTERNLIST).equals(sqlPatternInfor.getCompoundId())) {
                            return;
                        }
                        map.put(SQLPatternUtils.SQLPATTERNLIST, sqlPatternInfor.getCompoundId());
                        executeCommand(new Command() {
                        });
                        refreshComboContent(tableViewer, false);
                        updateCodeText();
                    }
                    tableViewer.refresh();
                }
            }

        });

        List<Map> tableContent = getTableInput(element);
        tableViewer.setInput(tableContent);

        refreshComboContent(tableViewer, false);

        dynamicComboBoxCellEditor = new DynamicComboBoxCellEditor(table, comboContent, comboboxCellEditorLabelProvider);
        tableViewer.setCellEditors(new CellEditor[] { dynamicComboBoxCellEditor });
        tableViewer.setColumnProperties(new String[] { NAME_PROPERTY });
        addSelectionChangeListener(tableViewer);

        buttonAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (comboContent.size() > 0) {
                    List<Map> tableInput = (List<Map>) tableViewer.getInput();
                    SQLPatternInfor sqlPatternInfor = comboContent.get(0);
                    Map map = new HashMap();
                    map.put(SQLPatternUtils.SQLPATTERNLIST, sqlPatternInfor.getCompoundId());
                    tableInput.add(map);

                    refreshComboContent(tableViewer, false);

                    tableViewer.refresh();
                    if (!buttonRemove.isEnabled()) {
                        buttonRemove.setEnabled(true);
                    }
                    executeCommand(new PropertyChangeCommand(element, EParameterName.SQLPATTERN_VALUE.getName(), element
                            .getElementParameter(EParameterName.SQLPATTERN_VALUE.getName()).getValue()));
                    refreshCode(element);
                }
            }

        });

        buttonRemove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = tableViewer.getSelection();
                List<Map> tableViewerInput = ((List<Map>) tableViewer.getInput());

                boolean needRefresh = false;
                if (!selection.isEmpty() && selection instanceof StructuredSelection) {
                    Object[] elements = ((StructuredSelection) selection).toArray();
                    for (Object element : elements) {
                        tableViewerInput.remove(element);
                    }
                    needRefresh = true;
                } else if (!tableViewerInput.isEmpty()) {
                    int index = tableViewerInput.size() - 1;
                    tableViewerInput.remove(index);
                    needRefresh = true;
                }

                if (needRefresh) {
                    refreshCode(element);
                    refreshComboContent(tableViewer, false);
                    tableViewer.refresh();
                    executeCommand(new PropertyChangeCommand(element, EParameterName.SQLPATTERN_VALUE.getName(), element
                            .getElementParameter(EParameterName.SQLPATTERN_VALUE.getName()).getValue()));
                }
            }
        });

        upButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                if (selection.size() != 1) {
                    return;
                }
                Map selectedElement = (Map) selection.getFirstElement();
                List<Map> tableInput = (List<Map>) tableViewer.getInput();
                int index = tableInput.indexOf(selectedElement);
                if (index == 0) {
                    return;
                }
                tableInput.remove(selectedElement);
                tableInput.add(index - 1, selectedElement);
                refreshCode(element);
                refreshComboContent(tableViewer, false);
                tableViewer.refresh();
                executeCommand(new PropertyChangeCommand(element, EParameterName.SQLPATTERN_VALUE.getName(), element
                        .getElementParameter(EParameterName.SQLPATTERN_VALUE.getName()).getValue()));
            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                if (selection.size() != 1) {
                    return;
                }
                Map selectedElement = (Map) selection.getFirstElement();
                List<Map> tableInput = (List<Map>) tableViewer.getInput();
                int index = tableInput.indexOf(selectedElement);
                if (index == tableInput.size() - 1) {
                    return;
                }
                tableInput.remove(selectedElement);
                tableInput.add(index + 1, selectedElement);
                refreshCode(element);
                refreshComboContent(tableViewer, false);
                tableViewer.refresh();
                executeCommand(new PropertyChangeCommand(element, EParameterName.SQLPATTERN_VALUE.getName(), element
                        .getElementParameter(EParameterName.SQLPATTERN_VALUE.getName()).getValue()));
            }
        });

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        if (element != null && element instanceof INode) {
            INode node = (INode) element;
            if (buttonAdd != null && !buttonAdd.isDisposed()) {
                if (getCurrentOpenedEditors() != null) {
                    for (IEditorReference editor : getCurrentOpenedEditors()) {
                        ERepositoryObjectType type = null;
                        try {
                            type = getCurrentOpenEditorType(editor);
                        } catch (PartInitException e) {
                            e.printStackTrace();
                        }
                        if (type != null && ERepositoryObjectType.SQLPATTERNS.equals(type)) {
                            buttonAdd.setEnabled(false);
                        }
                    }
                } else {
                    buttonAdd.setEnabled(!node.isReadOnly());
                }
            }
            if (buttonRemove != null && !buttonRemove.isDisposed()) {
                buttonRemove.setEnabled(!node.isReadOnly());
            }
            if (table != null && !table.isDisposed()) {
                table.setEnabled(!node.isReadOnly());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    /**
     * DOC bqian Comment method "addTableSorter".
     *
     * @param tableViewer2
     * @param columnName
     */
    private void addTableSorter(TableViewer viewer, TableColumn columnName) {
        new ColumnViewerSorter(viewer, columnName);
    }

    /**
     * bqian SQLPatternComposite class global comment. Detailled comment
     */
    private static class ColumnViewerSorter extends ViewerComparator {

        public static final int ASC = 1;

        public static final int NONE = 0;

        public static final int DESC = -1;

        private int direction = 0;

        private TableColumn column;

        private ColumnViewer viewer;

        public ColumnViewerSorter(ColumnViewer viewer, TableColumn column) {
            this.column = column;
            this.viewer = viewer;
            this.column.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (ColumnViewerSorter.this.viewer.getComparator() != null) {
                        if (ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this) {
                            int tdirection = ColumnViewerSorter.this.direction;

                            if (tdirection == ASC) {
                                setSorter(ColumnViewerSorter.this, DESC);
                            } else if (tdirection == DESC) {
                                setSorter(ColumnViewerSorter.this, NONE);
                            }
                        } else {
                            setSorter(ColumnViewerSorter.this, ASC);
                        }
                    } else {
                        setSorter(ColumnViewerSorter.this, ASC);
                    }
                }
            });
        }

        public void setSorter(ColumnViewerSorter sorter, int direction) {
            if (direction == NONE) {
                column.getParent().setSortColumn(null);
                column.getParent().setSortDirection(SWT.NONE);
                viewer.setComparator(null);
            } else {
                column.getParent().setSortColumn(column);
                sorter.direction = direction;

                if (direction == ASC) {
                    column.getParent().setSortDirection(SWT.DOWN);
                } else {
                    column.getParent().setSortDirection(SWT.UP);
                }
                if (viewer.getComparator() == sorter) {
                    viewer.refresh();
                } else {
                    viewer.setComparator(sorter);
                }
            }
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            return direction * super.compare(viewer, e1, e2);
        }

    }

    /**
     * DOC bqian Comment method "createCodeControl".
     *
     * @param panel
     * @param
     */
    private void createCodeControl(Composite panel, Control reference) {
        codeText = new Text(panel, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        codeText.setEditable(false);
        FormData fd = new FormData();
        fd.left = new FormAttachment(10, 0);
        fd.right = new FormAttachment(90, 0);
        fd.top = new FormAttachment(reference, 2);
        fd.bottom = new FormAttachment(95, 0);
        codeText.setLayoutData(fd);

        codeText.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                if (tableViewer.getTable().getSelection().length == 0) {
                    return;
                }
                TableItem item = tableViewer.getTable().getSelection()[0];
                if (item == null || item.getText() == null) {
                    return;
                }
                String sqlpatternName = item.getText();
                SQLPatternItem patternItem = SQLPatternUtils.getSQLPatternItem(SQLPatternComposite.this.element, sqlpatternName);
                if (patternItem.isSystem()) {
                    boolean answer = MessageDialog.openQuestion(getShell(), Messages.getString("SQLPatternComposite.TOS"), //$NON-NLS-1$
                            Messages.getString("SQLPatternComposite.forbidModification")); //$NON-NLS-1$

                    if (!answer) {
                        return;
                    }

                    IRepositoryService repositoryService = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                            IRepositoryService.class);

                    IElementParameter parameter = element.getElementParameter(EParameterName.SQLPATTERN_DB_NAME.getName());
                    String dbName = (String) parameter.getValue();
                    String path = dbName + "/" + RepositoryConstants.USER_DEFINED; //$NON-NLS-1$

                    repositoryService.createSqlpattern(path, true);
                } else if (sqlPatternAndProject.get(patternItem) != null) {
                    MessageDialog.openInformation(getShell(), "Information",
                            "Forbid modification on sql template from reference project");
                } else {
                    boolean answer = MessageDialog.openQuestion(getShell(), Messages.getString("SQLPatternComposite.TOS"), //$NON-NLS-1$
                            Messages.getString("SQLPatternComposite.modifySQLPattern")); //$NON-NLS-1$

                    if (!answer) {
                        return;
                    }

                    IRepositoryService repositoryService = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                            IRepositoryService.class);

                    repositoryService.openSQLPatternEditor(patternItem, false);
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {
                // do nothing

            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                mouseUp(e);

            }

        });

    }

    /**
     * DOC bqian Comment method "addSelectionChangeListener".
     *
     * @param tableViewer
     */
    private void addSelectionChangeListener(final TableViewer tableViewer) {
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateCodeText();
            }
        });

    }

    /**
     * bqian Comment method "getTableInput".
     *
     * @param element
     * @return
     */
    private List<Map> getTableInput(Element element) {
        List<Map> value = refreshCode(element);
        return value;
    }

    private boolean firstTimeLoad = true;

    /**
     * yzhang Comment method "refreshCode".
     *
     * @param element
     * @return
     */
    private List<Map> refreshCode(Element element) {
        IElementParameter sqlPatternValue = element.getElementParameter(EParameterName.SQLPATTERN_VALUE.getName());
        if (sqlPatternValue != null) {
            List<Map> values = (List<Map>) sqlPatternValue.getValue();

            if (firstTimeLoad) {
                List<Map> unusedValues = new ArrayList<Map>();
                for (Map map : values) {
                    String compoundId = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                    String id = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[0];

                    IRepositoryViewObject repositoryObject = SQLPatternUtils.getLastVersionRepositoryObjectById(id);
                    String name = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[1];

                    SQLPatternItem item = null;
                    if (repositoryObject == null && (item = SQLPatternUtils.getSQLPatternItem(element, name)) == null) {
                        unusedValues.add(map);
                    }
                }
                values.removeAll(unusedValues);
                firstTimeLoad = false;
            }

            return values;
        }
        return new ArrayList();
    }

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

    /**
     * yzhang Comment method "executeCommand".
     *
     * @param cmd
     */
    private void executeCommand(Command cmd) {
        if (commandStack == null) {
            IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (part instanceof AbstractMultiPageTalendEditor) {
                AbstractTalendEditor editor = ((AbstractMultiPageTalendEditor) part).getTalendEditor();
                commandStack = (CommandStack) editor.getAdapter(CommandStack.class);
            }
        }
        if (commandStack == null) {
            return;
        }
        commandStack.execute(cmd);
    }

    /**
     * yzhang Comment method "refreshComboContent".
     *
     * @param tableViewer
     * @param legalParameters
     */
    private void refreshComboContent(final TableViewer tableViewer, final boolean modifySQL) {
        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
                if (comboContent == null) {
                    comboContent = new ArrayList<SQLPatternInfor>();
                }
                comboContent.clear();

                List<Map> tableInput = (List<Map>) tableViewer.getInput();
                List<SQLPatternInfor> content = getAllSqlPatterns(tableInput, modifySQL);
                for (Map map : tableInput) {
                    String id = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                    SQLPatternInfor unusedSqlPatternInfor = null;
                    for (SQLPatternInfor patternInfor : content) {
                        if (patternInfor.getCompoundId().equals(id)) {
                            unusedSqlPatternInfor = patternInfor;
                            break;
                        }
                    }
                    content.remove(unusedSqlPatternInfor);
                }

                comboContent.addAll(content);
                refreshButton();

            }
        });
    }

    private void refreshButton() {
        buttonAdd.setEnabled(comboContent.size() > 0);
        buttonRemove.setEnabled(((List<Map>) tableViewer.getInput()).size() > 0);
    }

    private List<SQLPatternInfor> getAllSqlPatterns(List<Map> tableInput, boolean modifySQL) {

        IElementParameter elementParam = element.getElementParameter(EParameterName.SQLPATTERN_DB_NAME.getName());
        if (elementParam == null) {
            return Collections.EMPTY_LIST;
        }

        // String dbName = (String) elementParam.getValue();
        List<SQLPatternInfor> patternInfor = new ArrayList<SQLPatternInfor>();
        try {
            List<IRepositoryViewObject> list = null;
            if (isItemIndexChecked() && modifySQL) {
                List<Relation> relations = new ArrayList<Relation>();
                IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
                List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
                for (Map map : tableInput) {
                    String id = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                    relations.addAll(RelationshipItemBuilder.getInstance().getItemsRelatedTo(id, ItemCacheManager.LATEST_VERSION,
                            RelationshipItemBuilder.SQLPATTERN_RELATION));
                }
                for (Relation relation : relations) {
                    try {
                        IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
                        if (obj != null) {
                            updateList.add(obj);
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
                list = updateList;
            } else {
                list = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                        .getAll(ERepositoryObjectType.SQLPATTERNS, false);

                // add reference sql pattern
                addReferencedSQLTemplate(list, ProjectManager.getInstance().getCurrentProject());
            }
            for (IRepositoryViewObject repositoryObject : list) {
                Item item = repositoryObject.getProperty().getItem();
                if (item instanceof SQLPatternItem) {
                    SQLPatternItem sqlitem = (SQLPatternItem) repositoryObject.getProperty().getItem();
                    // disable this test as there is now only Generic ELT components
                    // if (item.getEltName().equals(dbName)) {
                    patternInfor.add(new SQLPatternInfor(sqlitem.getProperty().getId() + SQLPatternUtils.ID_SEPARATOR
                            + sqlitem.getProperty().getLabel(), sqlitem.getProperty().getLabel()));
                    // }
                }

            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return patternInfor;
    }

    private void addReferencedSQLTemplate(List<IRepositoryViewObject> list, Project project) {
        try {
            Context ctx = CorePlugin.getContext();
            if (ctx == null) {
                return;
            }
            String parentBranch = ProjectManager.getInstance().getMainProjectBranch(project);

            List<ProjectReference> referencedProjects = project.getProjectReferenceList();
            for (ProjectReference referenced : referencedProjects) {
                if (referenced.getBranch() != null && !parentBranch.equals(referenced.getBranch())) {
                    continue;
                }

                org.talend.core.model.properties.Project referencedEmfProject = referenced.getReferencedProject();
                List<ProjectReference> refeInRef = new Project(referencedEmfProject).getProjectReferenceList();
                Project newProject = new Project(referencedEmfProject);
                if (refeInRef != null && refeInRef.size() > 0) {
                    addReferencedSQLTemplate(list, newProject);
                }
                List<IRepositoryViewObject> refList;
                refList = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                        .getAll(newProject, ERepositoryObjectType.SQLPATTERNS, false);
                for (IRepositoryViewObject repositoryObject : refList) {
                    Item item = repositoryObject.getProperty().getItem();
                    if (item instanceof SQLPatternItem) {
                        if (!((SQLPatternItem) item).isSystem()) {
                            list.add(repositoryObject);
                            sqlPatternAndProject.put((SQLPatternItem) item, project);
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    private ILabelProvider comboboxCellEditorLabelProvider = new ILabelProvider() {

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            return ((SQLPatternInfor) element).getLabel();
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

    private CommandStack commandStack;

    /**
     * yzhang AdvancedContextComposite class global comment. Detailled comment
     */
    private class TableViewerProvider extends LabelProvider implements IStructuredContentProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         * java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            if (element instanceof Map) {
                Map ep = (Map) element;
                String compoundId = (String) ep.get(SQLPatternUtils.SQLPATTERNLIST);
                String id = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[0];

                IRepositoryViewObject repositoryObject = SQLPatternUtils.getLastVersionRepositoryObjectById(id);
                String name = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[1];
                SQLPatternItem item = null;
                if (repositoryObject == null
                        && (item = SQLPatternUtils.getSQLPatternItem(SQLPatternComposite.this.element, name)) != null) {
                    ep.put(SQLPatternUtils.SQLPATTERNLIST, item.getProperty().getId() + SQLPatternUtils.ID_SEPARATOR
                            + item.getProperty().getLabel());
                    return item.getProperty().getLabel();
                }
                if (repositoryObject != null) {
                    return repositoryObject.getLabel();
                }

            }
            return null;
        }
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
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        refreshCode(element);
        updateCodeText();

        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
                getParent().layout();
            }
        });
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
     * yzhang Comment method "updateCodeText".
     */
    private void updateCodeText() {

        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {

                Object o = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                if (o == null) {
                    return;
                }
                Map map = (Map) o;
                Object object = map.get(SQLPatternUtils.SQLPATTERNLIST);
                String id = null;
                if (object instanceof String) {
                    id = (String) object;
                    id = id.split(SQLPatternUtils.ID_SEPARATOR)[0];
                } else {
                    TableItem item = tableViewer.getTable().getSelection()[0];
                    id = item.getText();
                }
                IRepositoryViewObject repositoryObject = SQLPatternUtils.getLastVersionRepositoryObjectById(id);
                byte[] code = ((SQLPatternItem) repositoryObject.getProperty().getItem()).getContent().getInnerContent();
                codeText.setText(new String(code));

            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent
     * )
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        boolean needRefresh = false;
        boolean modifySQL = true;
        if (event.getSource() instanceof SQLPatternItem && event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            needRefresh = true;
            modifySQL = false;
            // if still have sql template editor to be opened.
            if (getCurrentOpenedEditors() != null) {
                for (IEditorReference editor : getCurrentOpenedEditors()) {
                    ERepositoryObjectType type = null;
                    try {
                        type = getCurrentOpenEditorType(editor);
                    } catch (PartInitException e) {
                        e.printStackTrace();
                    }
                    if (type != null && ERepositoryObjectType.SQLPATTERNS.equals(type)) {
                        modifySQL = true;
                    }
                }
            }
        }
        if (event.getSource() instanceof SQLPatternItem && event.getType() == IResourceChangeEvent.POST_CHANGE) {
            needRefresh = true;
        }
        if (needRefresh) {
            refreshComboContent(this.tableViewer, modifySQL);
            refresh();
        }
    }

    public IEditorReference[] getCurrentOpenedEditors() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null) {
            IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
            if (workbenchPage != null) {
                return workbenchPage.getEditorReferences();
            }
        }
        return null;
    }

    public ERepositoryObjectType getCurrentOpenEditorType(IEditorReference editor) throws PartInitException {
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof RepositoryEditorInput) {
            RepositoryEditorInput repoEditorInput = (RepositoryEditorInput) editorInput;
            Item item = repoEditorInput.getItem();
            return ERepositoryObjectType.getItemType(item);
        }
        return null;
    }

    /**
     * yzhang SQLPatternComposite class global comment. Detailled comment
     */
    private class SQLPatternInfor {

        private String label;

        private String id;

        private String compoundId;

        /**
         * yzhang SQLPatternComposite.SQLPatternInfor constructor comment.
         */
        public SQLPatternInfor(String id, String label) {
            this.compoundId = id;
            this.label = label;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Sets the label.
         *
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Sets the id.
         *
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * yzhang Comment method "getCompoundId".
         *
         * @return
         */
        public String getCompoundId() {
            return compoundId;
        }

    }

    public boolean isItemIndexChecked() {
        // IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        // IPreferenceStore preferenceStore = designerCoreService.getDesignerCorePreferenceStore();
        // return preferenceStore.getBoolean(ITalendCorePrefConstants.ITEM_INDEX);
        return true;
    }

}
