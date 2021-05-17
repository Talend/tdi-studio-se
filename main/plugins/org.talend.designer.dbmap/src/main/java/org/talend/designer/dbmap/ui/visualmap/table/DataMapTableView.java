// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.visualmap.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.SearchPattern;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.runtime.thread.AsynchronousThreading;
import org.talend.commons.ui.runtime.utils.ControlUtils;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.ExtendedTextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.Problem;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.IDataMapTableView;
import org.talend.designer.dbmap.MapperMain;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.ProblemsManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.table.AbstractInOutTable;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.ui.color.ColorInfo;
import org.talend.designer.dbmap.ui.color.ColorProviderMapper;
import org.talend.designer.dbmap.ui.dialog.ExpressionBuilderDialogForElt;
import org.talend.designer.dbmap.ui.dnd.DragNDrop;
import org.talend.designer.dbmap.ui.event.MousePositionAnalyser;
import org.talend.designer.dbmap.ui.event.ResizeHelper;
import org.talend.designer.dbmap.ui.event.ResizeHelper.RESIZE_MODE;
import org.talend.designer.dbmap.ui.font.FontInfo;
import org.talend.designer.dbmap.ui.font.FontProviderMapper;
import org.talend.designer.dbmap.ui.image.ImageInfo;
import org.talend.designer.dbmap.ui.image.ImageProviderMapper;
import org.talend.designer.dbmap.ui.proposal.expression.ExpressionProposalProvider;
import org.talend.designer.dbmap.ui.proposal.expression.KeyWordProposalProvider;
import org.talend.designer.dbmap.ui.tabs.StyledTextHandler;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DataMapTableView.java 2010 2007-02-12 13:18:38Z amaumont $
 *
 */
public abstract class DataMapTableView extends Composite implements IDataMapTableView {

    private final Point realToolbarSize = new Point(0, 0);

    private Table tableForEntries;

    private final ResizeHelper resizeHelper = new ResizeHelper();

    protected MapperManager mapperManager;

    protected TableViewerCreator tableViewerCreatorForColumns;

    protected IDataMapTable abstractDataMapTable;

    private Composite headerComposite;

    private Label nameLabel;

    private ToolItem minimizeButton;

    protected int heightForRestore;

    protected Layout parentLayout;

    protected String filterColumnLabel;

    // protected TableViewerCreator tableViewerCreatorForFilters;

    // protected Table tableForConstraints;

    private boolean executeSelectionEvent = true;

    protected ToolBar toolBarActions;

    private ExpressionProposalProvider expressionProposalProvider;

    protected Point expressionEditorTextSelectionBeforeFocusLost;

    protected Text lastExpressionEditorTextWhichLostFocus;

    protected Composite centerComposite;

    private Text columnExpressionTextEditor;

    protected Text whereConstraintExpressionTextEditor;

    protected Text otherConstraintExpressionTextEditor;

    private Cursor currentCursor;

    private ExpressionColorProvider expressionColorProvider;

    private Listener showErrorMessageListener;

    protected boolean forceExecuteSelectionEvent;

    private AbstractExtendedTableViewer<IColumnEntry> extendedTableViewerForColumns;

    protected AbstractExtendedTableViewer<FilterTableEntry> extendedTableViewerForFilters;

    protected AbstractExtendedTableViewer<FilterTableEntry> entendedTableViewerForOtherClauses;

    private static Image imageKey;

    private static Image imageEmpty;

    private static int constraintCounter = 0;

    private static final int MINIMUM_HEIGHT = WindowSystem.isGTK() ? 28 : 24;

    protected static final int TIME_BEFORE_NEW_REFRESH_BACKGROUND = 150;

    protected static final int OFFSET_HEIGHT_TRIGGER = 15;

    protected static final int COLUMN_EXPRESSION_SIZE_WEIGHT = 60;

    protected static final int COLUMN_NAME_SIZE_WEIGHT = 40;

    protected static final int ADJUST_WIDTH_VALUE = 0;

    private static final int HEADER_HEIGHT = 23;

    public static final String ID_NAME_COLUMN = "ID_NAME_COLUMN"; //$NON-NLS-1$

    public static final String ID_EXPRESSION_COLUMN = "ID_EXPRESSION_COLUMN"; //$NON-NLS-1$

    public static final String ID_OPERATOR = "ID_OPERATOR"; //$NON-NLS-1$

    public static final String ID_EXPLICIT_JOIN = "ID_EXPLICIT_JOIN"; //$NON-NLS-1$

    public static final String COLUMN_NAME = "Column"; //$NON-NLS-1$

    private IExpressionBuilderDialogController dialog;

    private ToolItem columnNameFilter;

    private boolean previousColumnNameFilter;

    private Text columnNameTextFilter;

    private Label filterImageLabel;

    public static final String DEFAULT_FILTER = "";//$NON-NLS-1$

    private TableViewer viewer;


    /**
     *
     * Call finalizeInitialization(...) after instanciate this class.
     *
     * @param parent
     * @param style
     * @param abstractDataMapTable
     * @param mapperManager
     */
    public DataMapTableView(Composite parent, int style, IDataMapTable abstractDataMapTable, MapperManager mapperManager) {
        super(parent, style);
        this.mapperManager = mapperManager;
        this.abstractDataMapTable = abstractDataMapTable;
        expressionColorProvider = new ExpressionColorProvider();
        createComponents();
        addListeners();
        mapperManager.addTablePair(DataMapTableView.this, abstractDataMapTable);
    }

    private void createComponents() {

        final Display display = this.getDisplay();
        // final Color listForeground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        if (WindowSystem.isGTK()) {
            Color systemColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
            setBackground(new Color(display, systemColor.getRed(), systemColor.getGreen(), systemColor.getBlue()));
            setBackgroundMode(SWT.INHERIT_NONE);
        } else {
            Color listBackground = display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
            this.setBackground(listBackground);
        }

        GridLayout mainLayout = new GridLayout();
        int marginMainLayout = 0;
        mainLayout.marginLeft = marginMainLayout;
        mainLayout.marginRight = marginMainLayout;
        mainLayout.marginTop = marginMainLayout;
        mainLayout.marginBottom = marginMainLayout;
        mainLayout.marginWidth = marginMainLayout;
        mainLayout.marginHeight = marginMainLayout;
        int spacingMainLayout = 2;
        mainLayout.horizontalSpacing = spacingMainLayout;
        mainLayout.verticalSpacing = spacingMainLayout;
        setLayout(mainLayout);

        headerComposite = new Composite(this, SWT.NONE);
        GridData headerGridData = new GridData(GridData.FILL_HORIZONTAL);
        headerGridData.heightHint = getHeaderHeight();
        headerComposite.setLayoutData(headerGridData);
        GridLayout headerLayout = new GridLayout();

        int margin = 0;
        headerLayout.marginLeft = 3;
        headerLayout.marginRight = margin;
        headerLayout.marginTop = margin;
        headerLayout.marginBottom = margin;
        headerLayout.marginWidth = margin;
        headerLayout.marginHeight = margin;
        int spacing = 2;
        headerLayout.horizontalSpacing = spacing;
        headerLayout.verticalSpacing = spacing;
        headerComposite.setLayout(headerLayout);

        nameLabel = new Label(headerComposite, SWT.NONE);
        nameLabel.setFont(FontProviderMapper.getFont(FontInfo.FONT_SYSTEM_BOLD));
        nameLabel.setText(getTitle());
        nameLabel.setToolTipText(getTitle());
        nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));

        int rightStyle = toolbarNeedToHaveRightStyle() ? SWT.RIGHT : SWT.NONE;
        toolBarActions = new ToolBar(headerComposite, SWT.FLAT | rightStyle);
        toolBarActions.setFont(FontProviderMapper.getFont(FontInfo.FONT_SYSTEM));

        if (addToolItems()) {
            addToolItemSeparator();
        }

        minimizeButton = new ToolItem(toolBarActions, SWT.PUSH);
        realToolbarSize.x += 45;

        Point sizeToolBar = toolBarActions.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        GridData gridDataToolbar = new GridData();

        if (toolbarNeedToHaveRightStyle() && WindowSystem.isWIN32()) {
            if (realToolbarSize != null) {
                gridDataToolbar.widthHint = realToolbarSize.x;
            }
        }
        if (WindowSystem.isGTK()) {
            gridDataToolbar.heightHint = 26;
        } else {
            // gridDataToolbar.heightHint = 30; // Win32
        }

        if (WindowSystem.isGTK()) {
            toolBarActions.setLayoutData(gridDataToolbar);
        } else {
            toolBarActions.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
        }

        headerLayout.numColumns = headerComposite.getChildren().length;

        centerComposite = new Composite(this, SWT.NONE);
        GridData centerData = new GridData(GridData.FILL_BOTH);
        centerComposite.setLayoutData(centerData);

        GridLayout centerLayout = new GridLayout();
        int marginCenterLayout = 0;
        centerLayout.marginLeft = marginCenterLayout;
        centerLayout.marginRight = marginCenterLayout;
        centerLayout.marginTop = marginCenterLayout;
        centerLayout.marginBottom = marginCenterLayout;
        centerLayout.marginWidth = marginCenterLayout;
        centerLayout.marginHeight = marginCenterLayout;
        int spacingCenterLayout = 2;
        centerLayout.horizontalSpacing = spacingCenterLayout;
        centerLayout.verticalSpacing = spacingCenterLayout;
        centerComposite.setLayout(centerLayout);

        initTableFilters(centerComposite);

        createColumnNameFilter();
        createTableForColumns(centerComposite);


        new DragNDrop(mapperManager, tableForEntries, true, true);

        Composite footerComposite = new Composite(this, SWT.NONE);
        GridData footerGridData = new GridData(10, 2);
        footerComposite.setLayoutData(footerGridData);

        headerComposite.moveAbove(nameLabel);

        if (WindowSystem.isGTK()) {
            sizeToolBar = toolBarActions.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            gridDataToolbar.widthHint = sizeToolBar.x + 20;
            headerComposite.layout();
        }

    }

    /**
     * DOC amaumont Comment method "getTitle".
     *
     * @return
     */
    protected String getTitle() {
        return abstractDataMapTable.getName();
    }

    /**
     * DOC amaumont Comment method "addToolItemSeparator".
     */
    protected void addToolItemSeparator() {
        ToolItem separator = new ToolItem(toolBarActions, SWT.SEPARATOR);
        separator.setWidth(10);
        getRealToolbarSize().x += separator.getWidth();
        // separator.setControl(headerComposite);
    }

    /**
     * DOC amaumont Comment method "createTableForColumns".
     *
     * @param parent
     */
    private void createTableForColumns(Composite parent) {
        this.extendedTableViewerForColumns = new AbstractExtendedTableViewer<IColumnEntry>(
                abstractDataMapTable.getTableColumnsEntriesModel(), parent) {

            @Override
            protected void createColumns(TableViewerCreator<IColumnEntry> tableViewerCreator, Table table) {
                initColumnsOfTableColumns(tableViewerCreator);
            }

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer#initTableListeners()
             */
            @Override
            protected void initTableListeners() {
                super.initTableListeners();
                getTableViewerCreator().addCellValueModifiedListener(new ITableCellValueModifiedListener() {

                    @Override
                    public void cellValueModified(TableCellValueModifiedEvent e) {
                        // System.out.println();
                        // System.out.println(getTable().hashCode());
                        // System.out.println(e.tableItem.getParent().hashCode());

                        // if (e.tableItem.getParent().isFocusControl()) {
                        // only if table of item is actually the focus control
                        getTableViewerCreator().getSelectionHelper().deselectAll();
                        getTable().forceFocus();
                        // }
                    }

                });
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions
             * (org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(final TableViewerCreator<IColumnEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setColumnsResizableByDefault(true);
                newTableViewerCreator.setBorderVisible(false);
                newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
                newTableViewerCreator.setKeyboardManagementForCellEdition(false);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable() instanceof
                // AbstractInOutTable);
                newTableViewerCreator.setFirstColumnMasked(true);

                if (getDataMapTable() instanceof AbstractInOutTable) {

                    if (imageKey == null) {
                        imageKey = org.talend.commons.ui.runtime.image.ImageProvider.getImage(EImage.KEY_ICON);
                    }
                    if (imageEmpty == null) {
                        imageEmpty = org.talend.commons.ui.runtime.image.ImageProvider.getImage(EImage.EMPTY);
                    }
                }
                newTableViewerCreator.setLabelProvider(new DefaultTableLabelProvider(newTableViewerCreator) {

                    @Override
                    public Color getBackground(Object element, int columnIndex) {
                        Color backgroundCellColor = getBackgroundCellColor(newTableViewerCreator, element, columnIndex);
                        if (backgroundCellColor != null) {
                            return backgroundCellColor;
                        }
                        return super.getBackground(element, columnIndex);
                    }

                    @Override
                    public Color getForeground(Object element, int columnIndex) {
                        Color foregroundCellColor = getForegroundCellColor(newTableViewerCreator, element, columnIndex);
                        if (foregroundCellColor != null) {
                            return foregroundCellColor;
                        }
                        return getForegroundCellColor(newTableViewerCreator, element, columnIndex);
                    }

                    @Override
                    public Image getColumnImage(Object element, int columnIndex) {
                        return getColumnImageExecute(element, columnIndex);
                    }

                    /**
                     * DOC amaumont Comment method "getColumnImageExecute".
                     *
                     * @param element
                     * @param columnIndex
                     * @return
                     */
                    private Image getColumnImageExecute(Object element, int columnIndex) {
                        if (getDataMapTable() instanceof AbstractInOutTable) {
                            AbstractInOutTableEntry entry = (AbstractInOutTableEntry) element;
                            TableViewerCreatorColumnNotModifiable column = newTableViewerCreator.getColumns().get(columnIndex);
                            if (column.getId().equals(ID_NAME_COLUMN)) {
                                if (entry.getMetadataColumn().isKey()) {
                                    return imageKey;
                                } else {
                                    return imageEmpty;
                                }
                            }
                        }
                        return null;
                    }

                });
            }

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer#initModelListeners()
             */
            @Override
            protected void initModelListeners() {
                super.initModelListeners();
                getExtendedTableModel().addAfterOperationListListener(100, new IListenableListListener<IColumnEntry>() {

                    @Override
                    public void handleEvent(ListenableListEvent<IColumnEntry> event) {
                        if (tableViewerCreator.getTable() != null && !tableViewerCreator.getTable().isDisposed()) {
                            if (event.type == TYPE.ADDED) {
                                Collection<IColumnEntry> addedObjects = event.addedObjects;
                                for (IColumnEntry tableEntry : addedObjects) {
                                    mapperManager.getProblemsManager().checkProblemsForTableEntry(tableEntry, false);
                                }
                            }
                        }
                    }

                });
            }

        };
        tableViewerCreatorForColumns = this.extendedTableViewerForColumns.getTableViewerCreator();

        if (getZone() == Zone.INPUTS || getZone() == Zone.OUTPUTS) {
            viewer = tableViewerCreatorForColumns.getTableViewer();
            viewer.addFilter(new selectorViewerFilter());
        }

        this.extendedTableViewerForColumns.setCommandStack(mapperManager.getCommandStack());

        tableForEntries = tableViewerCreatorForColumns.getTable();

        GridData tableEntriesGridData = new GridData(GridData.FILL_BOTH);
        tableEntriesGridData.grabExcessVerticalSpace = true;
        tableEntriesGridData.minimumHeight = tableForEntries.getHeaderHeight() + tableForEntries.getItemHeight();
        // tableEntriesGridData.heightHint = 50;
        // tableEntriesGridData.grabExcessVerticalSpace = false;
        tableForEntries.setLayoutData(tableEntriesGridData);

        addTablesListeners();

    }

    /**
     * DOC amaumont Comment method "addTableForColumnsListeners".
     */
    private void addTablesListeners() {
        tableViewerCreatorForColumns.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            @Override
            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        abstractDataMapTable.getTableColumnsEntriesModel().addAfterOperationListListener(
                new IListenableListListener<FilterTableEntry>() {

                    @Override
                    public void handleEvent(ListenableListEvent<FilterTableEntry> event) {

                        mapperManager.getUiManager().refreshSqlExpression();

                    }

                });

        final TableViewer tableViewerForEntries = tableViewerCreatorForColumns.getTableViewer();

        tableViewerForEntries.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                int[] selectionIndices = tableViewerForEntries.getTable().getSelectionIndices();
                if (selectionIndices.length > 0) {
                    selectThisDataMapTableView();
                    onSelectedEntries(event.getSelection(), selectionIndices);
                    // bug 18414
                    MetadataTableEditorView metadataTableEditorView = null;
                    if (getZone() == Zone.INPUTS) {
                        metadataTableEditorView = mapperManager.getUiManager().getInputMetaEditorView();
                    } else if (getZone() == Zone.OUTPUTS) {
                        metadataTableEditorView = mapperManager.getUiManager().getOutputMetaEditorView();
                    }
                    if (metadataTableEditorView != null) {
                        metadataTableEditorView.getTableViewerCreator().refresh();
                    }
                }
            }

        });

        tableForEntries.addListener(SWT.DragDetect, new Listener() {

            @Override
            public void handleEvent(Event event) {
                onSelectedEntries(tableViewerForEntries.getSelection(), tableViewerForEntries.getTable().getSelectionIndices());
            }

        });

        tableViewerCreatorForColumns.getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                if (forceExecuteSelectionEvent) {
                    forceExecuteSelectionEvent = false;
                    onSelectedEntries(tableViewerForEntries.getSelection(), tableViewerForEntries.getTable()
                            .getSelectionIndices());
                }
            }

        });
        tableForEntries.addListener(SWT.KeyDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForColumns, event);
            }

        });

        abstractDataMapTable.getTableColumnsEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<IColumnEntry>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<IColumnEntry> event) {

                TableViewerCreator tableViewerCreator = tableViewerCreatorForColumns;
                ITableEntry tableEntry = event.bean;
                parseExpressionIfNeeded(event, tableViewerCreator, tableEntry);
                mapperManager.getUiManager().refreshSqlExpression();

            }

        });

        if (abstractDataMapTable instanceof OutputTable) {
            OutputTable outputTable = (OutputTable) abstractDataMapTable;
            outputTable.getWhereTableFiltersEntriesModel().addAfterOperationListListener(
                    new IListenableListListener<FilterTableEntry>() {

                        @Override
                        public void handleEvent(ListenableListEvent<FilterTableEntry> event) {

                            resizeAtExpandedSize();
                            mapperManager.getUiManager().refreshSqlExpression();

                        }

                    });
            outputTable.getWhereTableFiltersEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<FilterTableEntry>() {

                @Override
                public void handleEvent(ModifiedBeanEvent<FilterTableEntry> event) {

                    onOutputTableFiltersModified(event, extendedTableViewerForFilters);
                    mapperManager.getUiManager().refreshSqlExpression();
                }

            });

            outputTable.getOtherTableFiltersEntriesModel().addAfterOperationListListener(
                    new IListenableListListener<FilterTableEntry>() {

                        @Override
                        public void handleEvent(ListenableListEvent<FilterTableEntry> event) {

                            resizeAtExpandedSize();
                            mapperManager.getUiManager().refreshSqlExpression();

                        }

                    });
            outputTable.getOtherTableFiltersEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<FilterTableEntry>() {

                @Override
                public void handleEvent(ModifiedBeanEvent<FilterTableEntry> event) {

                    onOutputTableFiltersModified(event, entendedTableViewerForOtherClauses);
                    mapperManager.getUiManager().refreshSqlExpression();
                }

            });
        }
    }

    protected void onOutputTableFiltersModified(ModifiedBeanEvent<FilterTableEntry> event,
            AbstractExtendedTableViewer<FilterTableEntry> _extendedTableViewerForFilters) {
        TableViewerCreator tableViewerCreator = _extendedTableViewerForFilters.getTableViewerCreator();
        ITableEntry tableEntry = event.bean;

        parseExpressionIfNeeded(event, tableViewerCreator, tableEntry);

    }

    /**
     * DOC amaumont Comment method "initShowMessageErrorListener".
     *
     * @param table
     */
    protected void initShowMessageErrorListener(final Table table) {
        showErrorMessageListener = new Listener() {

            @Override
            public void handleEvent(Event event) {

                switch (event.type) {
                case SWT.MouseMove:

                    // System.out.println("ToolTipText:" + table.getToolTipText());

                    String defaultToolTip = null;
                    if (WindowSystem.isGTK() && table.getToolTipText() != null) {
                        defaultToolTip = " "; //$NON-NLS-1$
                    }

                    Point cursorPositionFromTableOrigin = TableUtils.getCursorPositionFromTableOrigin(table, event);
                    TableColumn tableColumn = TableUtils.getTableColumn(table, cursorPositionFromTableOrigin);
                    if (tableColumn == null) {
                        setTableToolTipText(table, null, null, defaultToolTip);
                        return;
                    }
                    TableItem tableItem = TableUtils.getTableItem(table, cursorPositionFromTableOrigin);

                    if (tableItem == null) {
                        setTableToolTipText(table, tableColumn, null, defaultToolTip);
                        return;
                    }
                    ITableEntry tableEntry = (ITableEntry) tableItem.getData();
                    String toolTip = null;
                    if (tableEntry.getProblems() != null) {
                        List<Problem> problems = tableEntry.getProblems();
                        toolTip = ""; //$NON-NLS-1$
                        for (Problem problem : problems) {
                            String description = problem.getDescription().replaceAll("[\r\n\t]", ""); //$NON-NLS-1$ //$NON-NLS-2$
                            toolTip += description + "\n"; //$NON-NLS-1$
                        }
                    }

                    if (WindowSystem.isGTK() && toolTip == null && table.getToolTipText() != null) {
                        toolTip = defaultToolTip;
                    }
                    setTableToolTipText(table, tableColumn, tableEntry, toolTip);

                    // System.out.println("toolTip="+toolTip);
                    // System.out.println("table.getToolTipText()="+table.getToolTipText());

                    break;
                default:
                }
            }

            /**
             * DOC amaumont Comment method "setTableToolTipText".
             *
             * @param table
             * @param tableColumn
             * @param text
             */
            private void setTableToolTipText(final Table table, TableColumn tableColumn, ITableEntry tableEntry, String text) {
                table.setToolTipText(text);
            }

        };
        table.addListener(SWT.MouseMove, showErrorMessageListener);
    }

    /**
     * DOC amaumont Comment method "addEntriesActionsComponents".
     *
     * @return true if one or more ToolItem has been added
     */
    protected abstract boolean addToolItems();

    /**
     * DOC amaumont Comment method "initTableConstraints".
     *
     * @param parent
     */
    protected abstract void initTableFilters(Composite parent);

    /**
     * DOC amaumont Comment method "addListeners".
     */
    protected void addListeners() {

        final UIManager uiManager = mapperManager.getUiManager();

        MouseTrackListener mouseTracker = new MouseTrackListener() {

            @Override
            public void mouseEnter(MouseEvent e) {
            }

            @Override
            public void mouseExit(MouseEvent e) {
                setDefaultCursor();
                resizeHelper.stopDrag();
            }

            @Override
            public void mouseHover(MouseEvent e) {
            }

        };
        this.addMouseTrackListener(mouseTracker);

        SelectionListener scrollListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                uiManager.refreshBackground(true, false);
            }
        };

        tableForEntries.getVerticalBar().addSelectionListener(scrollListener);

        // /////////////////////////////////////////////////////////////////

        addManualTableResizeListeners(uiManager);
        // /////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////
        Listener onSelectDataMapTableViewListener = new Listener() {

            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.MouseDown:
                    onSelectDataMapTableView();
                    break;
                default:
                }

            }
        };

        this.addListener(SWT.MouseDown, onSelectDataMapTableViewListener);
        headerComposite.addListener(SWT.MouseDown, onSelectDataMapTableViewListener);
        nameLabel.addListener(SWT.MouseDown, onSelectDataMapTableViewListener);
        // /////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////
        minimizeButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                minimizeTable(!abstractDataMapTable.isMinimized());
            }

        });

        // /////////////////////////////////////////////////////////////////

        initShowMessageErrorListener(tableForEntries);

    }

    /**
     * DOC amaumont Comment method "addManualTableResizeListeners".
     *
     * @param uiManager
     */
    private void addManualTableResizeListeners(final UIManager uiManager) {
        this.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (currentCursor != null) {
                    currentCursor.dispose();
                }
            }

        });

        Listener tableResizerListener = new Listener() {

            @Override
            public void handleEvent(Event event) {

                MousePositionAnalyser mpa = new MousePositionAnalyser(DataMapTableView.this);
                Point eventPoint = new Point(event.x, event.y);

                boolean leftMouseButton = (event.stateMask & SWT.BUTTON1) != 0 || event.button == 1;

                switch (event.type) {
                case SWT.MouseMove:
                    if (resizeHelper.isDragging()) {
                        Point newPoint = convertToParentOrigin(DataMapTableView.this, eventPoint);
                        Point dragPoint = resizeHelper.getLastDragPoint();
                        Point diff = new Point(newPoint.x - dragPoint.x, newPoint.y - dragPoint.y);
                        if (mpa.isOnLeftBorder(eventPoint)) {
                            diff.x *= -1;
                        }
                        Rectangle rect = DataMapTableView.this.getClientArea();
                        rect.width += 2 * DataMapTableView.this.getBorderWidth();
                        rect.height += 2 * DataMapTableView.this.getBorderWidth();

                        RESIZE_MODE currentMode = resizeHelper.getCurrentMode();
                        int newWidth = (currentMode == RESIZE_MODE.HORIZONTAL || currentMode == RESIZE_MODE.BOTH) ? rect.width
                                + diff.x * 2 : rect.width;
                        int newHeight = (currentMode == RESIZE_MODE.VERTICAL || currentMode == RESIZE_MODE.BOTH) ? rect.height
                                + diff.y : rect.height;

                        if (newHeight < MINIMUM_HEIGHT + OFFSET_HEIGHT_TRIGGER && diff.y < 0) {
                            changeMinimizeState(true);
                            newHeight = MINIMUM_HEIGHT;
                        } else if (newHeight > MINIMUM_HEIGHT + OFFSET_HEIGHT_TRIGGER && diff.y > 0) {
                            changeMinimizeState(false);
                        }
                        changeSize(new Point(newWidth, newHeight), false, true);
                        resizeHelper.setLastDragPoint(newPoint);
                    } else if (!resizeHelper.isDragging()) {
                        Cursor cursor = null;
                        if (mpa.isOnLeftBottomCorner(eventPoint)) {
                            // cursor = new Cursor(dataMapTableView.getDisplay(), org.eclipse.swt.SWT.CURSOR_SIZESW);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.BOTH);
                        } else if (mpa.isOnRightBottomCorner(eventPoint)) {
                            // cursor = new Cursor(dataMapTableView.getDisplay(), org.eclipse.swt.SWT.CURSOR_SIZESE);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.BOTH);
                        } else if (mpa.isOnLeftBorder(eventPoint)) {
                            // cursor = new Cursor(dataMapTableView.getDisplay(), org.eclipse.swt.SWT.CURSOR_SIZEE);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.HORIZONTAL);
                        } else if (mpa.isOnRightBorder(eventPoint)) {
                            // cursor = new Cursor(dataMapTableView.getDisplay(), org.eclipse.swt.SWT.CURSOR_SIZEW);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.HORIZONTAL);
                        } else if (mpa.isOnBottomBorder(eventPoint)) {
                            cursor = new Cursor(DataMapTableView.this.getDisplay(), org.eclipse.swt.SWT.CURSOR_SIZES);
                            resizeHelper.setCurrentMode(RESIZE_MODE.VERTICAL);
                        }
                        if (cursor != null) {
                            DataMapTableView.this.setCursor(cursor);
                        } else {
                            setDefaultCursor();
                            resizeHelper.setCurrentMode(RESIZE_MODE.NONE);
                        }
                    }

                    break;
                case SWT.MouseDown:
                    if (leftMouseButton) {
                        if (mpa.isOnLeftBorder(eventPoint) || mpa.isOnRightBorder(eventPoint) || mpa.isOnBottomBorder(eventPoint)) {
                            resizeHelper.startDrag(convertToParentOrigin(DataMapTableView.this, new Point(event.x, event.y)));
                        } else {
                            setDefaultCursor();
                        }
                        parentLayout = DataMapTableView.this.getParent().getLayout();
                        DataMapTableView.this.getParent().setLayout(null);
                    }
                    break;
                case SWT.MouseUp:
                    if (leftMouseButton) {
                        resizeHelper.stopDrag();
                        // gridData = (GridData) dataMapTableView.getLayoutData();
                        // gridData.exclude = false;
                        DataMapTableView.this.getParent().setLayout(parentLayout);
                        DataMapTableView.this.getParent().layout();
                        uiManager.resizeTablesZoneViewAtComputedSize(getZone());
                        uiManager.refreshBackground(true, false);
                    }
                    break;
                case SWT.MouseExit:
                    setDefaultCursor();
                    break;
                default:
                }

            }

        };

        this.addListener(SWT.MouseMove, tableResizerListener);
        this.addListener(SWT.MouseDown, tableResizerListener);
        this.addListener(SWT.MouseUp, tableResizerListener);
    }

    /**
     * DOC amaumont Comment method "showTableConstraints".
     */
    protected void showTableConstraints(boolean visible) {

        showTableConstraints(visible, this.getTableViewerCreatorForWhereFilters());

    }

    protected void showTableConstraints(boolean visible, TableViewerCreator _tableViewerCreatorForFilters) {
        Table _tableForConstraints = _tableViewerCreatorForFilters.getTable();
        GridData _tableForConstraintsGridData = (GridData) _tableForConstraints.getLayoutData();
        if (visible) {
            _tableForConstraintsGridData.exclude = false;
            _tableForConstraints.setVisible(true);
            if (WindowSystem.isGTK()) {
                updateGridDataHeightForTableConstraints(_tableForConstraints);
            }
        } else {
            _tableForConstraintsGridData.exclude = true;
            _tableForConstraints.setVisible(false);
        }
        _tableViewerCreatorForFilters.getTableViewer().refresh();
        resizeAtExpandedSize(_tableViewerCreatorForFilters);
    }

    /**
     * DOC amaumont Comment method "onSelectDataMapTableView".
     */
    protected void onSelectDataMapTableView() {
        mapperManager.getUiManager().selectDataMapTableView(this, true, true);
    }

    /**
     * DOC amaumont Comment method "initClumns".
     */
    public abstract void initColumnsOfTableColumns(TableViewerCreator tableViewerCreator);

    public TableViewerCreator getTableViewerCreatorForColumns() {
        return this.tableViewerCreatorForColumns;
    }

    public TableViewerCreator getTableViewerCreatorForWhereFilters() {
        if (this.extendedTableViewerForFilters != null) {
            return this.extendedTableViewerForFilters.getTableViewerCreator();
        } else {
            return null;
        }
    }

    public TableViewerCreator getTableViewerCreatorForOtherFilters() {
        if (this.entendedTableViewerForOtherClauses != null) {
            return this.entendedTableViewerForOtherClauses.getTableViewerCreator();
        } else {
            return null;
        }
    }

    public Point convertToParentOrigin(Composite child, Point point) {
        Rectangle bounds = child.getBounds();
        return new Point(bounds.x + point.x, bounds.y + point.y);
    }

    private void setDefaultCursor() {
        Cursor cursor = new Cursor(DataMapTableView.this.getDisplay(), 0);
        DataMapTableView.this.setCursor(cursor);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public MapperManager getMapperManager() {
        return this.mapperManager;
    }

    public abstract Zone getZone();

    /**
     * DOC amaumont Comment method "resizeWithPreferredSize".
     */
    public void resizeAtExpandedSize() {
        resizeAtExpandedSize(this.getTableViewerCreatorForWhereFilters());
    }

    public void resizeAtExpandedSize(TableViewerCreator _tableViewerCreatorForFilters) {
        changeSize(getPreferredSize(false, true, false, this.getTableViewerCreatorForWhereFilters()), true, true);
    }

    /**
     *
     * Change size of the DataMapTableView.
     *
     * @param newWidth
     * @param newHeight
     * @param refreshNow refresh background with links if true
     * @param recalculateParentSize
     */
    public void changeSize(Point newSize, boolean refreshNow, boolean recalculateParentSize) {
        if (newSize.y < getHeaderHeight()) {
            newSize.y = getHeaderHeight();
        }
        FormData formData = (FormData) DataMapTableView.this.getLayoutData();
        formData.width = newSize.x;
        formData.height = newSize.y;
        DataMapTableView.this.setSize(newSize);
        UIManager uiManager = mapperManager.getUiManager();
        if (recalculateParentSize) {
            uiManager.resizeTablesZoneViewAtComputedSize(getZone());
        }
        if (refreshNow) {
            uiManager.refreshBackground(true, false);
        } else {
            uiManager.refreshBackground(true, TIME_BEFORE_NEW_REFRESH_BACKGROUND, true);
        }
        // dataMapTableView.redraw();
        // dataMapTableView.layout(true, true);
    }

    public void onSelectedEntries(ISelection selection, int[] selectionIndices) {
        if (executeSelectionEvent) {
            UIManager uiManager = mapperManager.getUiManager();
            uiManager.selectLinks(DataMapTableView.this, uiManager.extractSelectedTableEntries(selection), false, false);
            uiManager.selectLinkedMetadataEditorEntries(this, selectionIndices);
        }
    }

    public IDataMapTable getDataMapTable() {
        return this.abstractDataMapTable;
    }

    /**
     * DOC amaumont Comment method "setTableSelection".
     *
     * @param selectionIndices
     */
    public void setTableSelection(int[] selectionIndices) {
        this.executeSelectionEvent = false;
        this.tableViewerCreatorForColumns.getSelectionHelper().setSelection(selectionIndices);
        this.executeSelectionEvent = true;

    }

    protected void createFiltersToolItems() {

        ToolItem addFilterButton = new ToolItem(toolBarActions, SWT.PUSH);
        addFilterButton.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.addFilterRow")); //$NON-NLS-1$
        addFilterButton.setImage(ImageProviderMapper.getImage(ImageInfo.ADD_FILTER_ICON));

        // /////////////////////////////////////////////////////////////////
        if (addFilterButton != null) {

            addFilterButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }

                @Override
                public void widgetSelected(SelectionEvent e) {

                    onFiltersToolItemsSelected(e, getTableViewerCreatorForWhereFilters(), FilterTableEntry.WHERE_FILTER);
                }

            });
        }
        // createActivateFilterCheck();
        createColumnNameFilterCheck();
    }

    synchronized protected void onFiltersToolItemsSelected(SelectionEvent e, TableViewerCreator _tableViewerCreatorForFilters,
            String filterKind) {

        Table tableConstraints = _tableViewerCreatorForFilters.getTable();
        int index = tableConstraints.getItemCount();
        int[] selection = _tableViewerCreatorForFilters.getTable().getSelectionIndices();
        if (selection.length > 0) {
            index = selection[selection.length - 1] + 1;
        }
        mapperManager.addNewFilterEntry(DataMapTableView.this, "newFilter" + ++constraintCounter, index, filterKind); //$NON-NLS-1$
        updateGridDataHeightForTableConstraints();
        changeSize(getPreferredSize(false, true, true), true, true);
        _tableViewerCreatorForFilters.getTableViewer().refresh();
        mapperManager.getUiManager().refreshBackground(true, false);
        showTableConstraints(true, _tableViewerCreatorForFilters);
        changeMinimizeState(false);
        _tableViewerCreatorForFilters.layout();

    }

    protected void createColumnNameFilter() {
        if (getDataMapTable() instanceof AbstractInOutTable) {
            final AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();

            IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();

            filterImageLabel = new Label(getCenterComposite(), SWT.NONE);
            filterImageLabel.setImage(ImageProviderMapper.getImage(ImageInfo.TMAP_FILTER_ICON));
            filterImageLabel.setVisible(table.isActivateColumnNameFilter());

            columnNameTextFilter = new Text(getCenterComposite(), SWT.BORDER);

            if (mapperManager.componentIsReadOnly()) {
                columnNameTextFilter.setEditable(false);
                filterImageLabel.setEnabled(false);
            }

            GridData gridData1 = new GridData();
            gridData1.exclude = !table.isActivateColumnNameFilter();
            gridData1.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
            filterImageLabel.setLayoutData(gridData1);

            GridData nameFilterTextGridData = new GridData(GridData.FILL_HORIZONTAL);
            nameFilterTextGridData.minimumHeight = 10;
            nameFilterTextGridData.heightHint = 20;
            nameFilterTextGridData.minimumWidth = 25;
            nameFilterTextGridData.widthHint = 50;
            columnNameTextFilter.setLayoutData(nameFilterTextGridData);

            String columnNameFilter = table.getColumnNameFilter();
            if (columnNameFilter != null && !DEFAULT_FILTER.equals(columnNameFilter.trim())) {
                columnNameTextFilter.setText(columnNameFilter);
            } else {
                columnNameTextFilter.setText(DEFAULT_FILTER);
            }

            columnNameTextFilter.setVisible(table.isActivateColumnNameFilter());
            nameFilterTextGridData.exclude = !table.isActivateColumnNameFilter();
            //
            columnNameTextFilter.setBackground(ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_VALID_EXPRESSION_CELL));
            columnNameTextFilter.setForeground(ColorProviderMapper.getColor(ColorInfo.COLOR_FOREGROUND_VALID_EXPRESSION_CELL));

            columnNameTextFilter.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent e) {
                    redrawColumnNameFilter();
                    Control text = (Control) e.getSource();
                    if (DEFAULT_FILTER.equals(ControlUtils.getText(text))) {
                        ControlUtils.setText(text, DEFAULT_FILTER);
                    }
                }

                public void focusLost(FocusEvent e) {
                    Control text = (Control) e.getSource();
                    String currentContent = ControlUtils.getText(text);
                    if (currentContent != null && DEFAULT_FILTER.equals(currentContent.trim())) {
                        ControlUtils.setText(text, DEFAULT_FILTER);
                        table.setColumnNameFilter(DEFAULT_FILTER);
                    } else {
                        table.setColumnNameFilter(currentContent);
                    }
                }

            });
            columnNameTextFilter.addControlListener(new ControlListener() {

                public void controlMoved(ControlEvent e) {
                    redrawColumnNameFilter();
                }

                public void controlResized(ControlEvent e) {
                    redrawColumnNameFilter();
                }

            });

            columnNameTextFilter.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    parseColumnNameFilterRefresh();
                }
            });
        }
    }

    private void redrawColumnNameFilter() {
        if (!columnNameTextFilter.isDisposed()) {
            columnNameTextFilter.redraw();
        }
    }

    protected Composite getCenterComposite() {
        return this.centerComposite;
    }

    protected void createColumnNameFilterCheck() {
        AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();
        boolean isErrorReject = false;
        if (getDataMapTable() instanceof OutputTable) {
            isErrorReject = getMapperManager().ERROR_REJECT.equals(getDataMapTable().getName());
        }
        //
        columnNameFilter = new ToolItem(toolBarActions, SWT.CHECK);
        columnNameFilter.setEnabled(!mapperManager.componentIsReadOnly() && !isErrorReject);
        previousColumnNameFilter = table.isActivateColumnNameFilter();
        columnNameFilter.setSelection(table.isActivateColumnNameFilter());
        columnNameFilter.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.ColumnNameFilter")); //$NON-NLS-1$
        columnNameFilter.setImage(ImageProviderMapper.getImage(ImageInfo.TMAP_FILTER_ICON));

        if (columnNameFilter != null) {

            columnNameFilter.addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    updateColumnNameFilterTextAndLayout(true);
                    previousColumnNameFilter = columnNameFilter.getSelection();
                }
            });
        }
    }

    protected void updateColumnNameFilterTextAndLayout(boolean buttonPressed) {
        final AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();
        GridData gridData = (GridData) columnNameTextFilter.getLayoutData();
        GridData gridData1 = (GridData) filterImageLabel.getLayoutData();

        if (columnNameFilter.getSelection()) {
            columnNameTextFilter.setVisible(true);
            gridData.exclude = false;
            table.setActiveColumnNameFilter(true);
            gridData1.exclude = false;
            filterImageLabel.setVisible(true);

        } else {
            columnNameTextFilter.setVisible(false);
            gridData.exclude = true;
            table.setActiveColumnNameFilter(false);
            gridData1.exclude = true;
            filterImageLabel.setVisible(false);

        }
        parseColumnNameFilterRefresh();
        if (buttonPressed) {
            DataMapTableView.this.changeSize(DataMapTableView.this.getPreferredSize(false, true, false), true, true);
        }
        DataMapTableView.this.layout();

        mapperManager.getUiManager().refreshBackground(true, false);

        if (columnNameTextFilter.isVisible() && buttonPressed) {
            columnNameTextFilter.setFocus();
        }
    }


    private void parseColumnNameFilterRefresh() {

        viewer.refresh();
        if (abstractDataMapTable instanceof OutputTable) {
            OutputTable outputTable = (OutputTable) abstractDataMapTable;
            List<IColumnEntry> oldOuputEntries = outputTable.getDataMapTableEntries();
            // Table tableViewerForEntries = tableViewerCreatorForColumns.getTableViewer().getTable();
            if (oldOuputEntries != null) {
                for (IColumnEntry entry : oldOuputEntries) {
                    if (entry instanceof OutputColumnTableEntry) {
                        OutputColumnTableEntry outputEntry = (OutputColumnTableEntry) entry;
                        if (outputEntry.getExpression() != null) {
                            mapperManager.getUiManager().refreshBackground(false, false);
                        }
                    }
                }
                resizeAtExpandedSize();
            }
        }

            if (abstractDataMapTable instanceof InputTable) {
                InputTable inputTable = (InputTable) abstractDataMapTable;
                List<IColumnEntry> oldInputEntries = inputTable.getDataMapTableEntries();
                if (oldInputEntries != null) {
                    for (IColumnEntry entry : oldInputEntries) {
                        if (entry instanceof InputColumnTableEntry) {
                            // InputColumnTableEntry inputEntry = (InputColumnTableEntry) entry;
                            StyledTextHandler textTarget = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
                            if (textTarget.getStyledText().getText() != null && textTarget.getCurrentEntry() != null) {
                                mapperManager.getUiManager().refreshBackground(false, false);
                            }
                            mapperManager.getUiManager().refreshBackground(true, false);
                        }
                    }
                    resizeAtExpandedSize();
                }
            }

    }

    protected void createToolItems() {

    }

    public void minimizeTable(boolean minimize) {
        Point size = DataMapTableView.this.getSize();
        if (minimize) {
            // System.out.println("store height before minimize"+size.y);
            this.heightForRestore = size.y - 4;
            changeSize(new Point(size.x, getHeaderHeight()), true, true);
            changeMinimizeState(true);
        } else {
            if (heightForRestore != getHeaderHeight() && heightForRestore > 0) {
                size.y = heightForRestore;
            } else {
                size = DataMapTableView.this.getPreferredSize(false, true, false);
            }
            changeSize(size, true, true);
            changeMinimizeState(false);
        }
    }

    public void changeMinimizeState(boolean newStateIsMinimized) {
        if (newStateIsMinimized) {
            abstractDataMapTable.setMinimized(true);
            minimizeButton.setSelection(true);
            minimizeButton.setImage(ImageProviderMapper.getImage(ImageInfo.RESTORE_ICON));
            minimizeButton.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.restore")); //$NON-NLS-1$
        } else {
            abstractDataMapTable.setMinimized(false);
            minimizeButton.setSelection(false);
            minimizeButton.setImage(ImageProviderMapper.getImage(ImageInfo.MINIMIZE_ICON));
            minimizeButton.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.minimize")); //$NON-NLS-1$
        }
    }

    /**
     *
     * Return current size of DataMapTableView.
     *
     * @return
     */
    public Point getPreferredSize() {
        return getPreferredSize(false, false, false);
    }

    /**
     *
     * DOC amaumont Comment method "getPreferredSize".
     *
     * @param newTableEntryWillBeAdded
     * @param expandedSize
     * @param newConstraintEntryWillBeAdded
     * @return
     */
    public Point getPreferredSize(boolean newTableEntryWillBeAdded, boolean expandedSize, boolean newConstraintEntryWillBeAdded) {
        return getPreferredSize(newTableEntryWillBeAdded, expandedSize, newConstraintEntryWillBeAdded,
                this.getTableViewerCreatorForWhereFilters());
    }

    public Point getPreferredSize(boolean newTableEntryWillBeAdded, boolean expandedSize, boolean newConstraintEntryWillBeAdded,
            TableViewerCreator _tableViewerCreatorForFilters) {
        int heightOffset = 0;
        int tableEntryItemHeight = tableViewerCreatorForColumns.getTable().getItemHeight();
        // heightOffset += newTableEntryWillBeAdded ? tableEntryItemHeight : 0;
        heightOffset += newConstraintEntryWillBeAdded ? _tableViewerCreatorForFilters.getTable().getItemHeight() : 0;

        int newHeight = this.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - tableEntryItemHeight + heightOffset;
        if (WindowSystem.isGTK()) {
            newHeight += tableEntryItemHeight;
        }
        if (tableViewerCreatorForColumns.getInputList().size() == 0) {
            newHeight += tableEntryItemHeight;
        } else {
            newHeight += 5;
        }

        if (abstractDataMapTable.isMinimized() && !expandedSize) {
            newHeight = getHeaderHeight();
        }
        return new Point(this.getSize().x, newHeight);
    }

    public void registerStyledExpressionEditor(final StyledTextHandler styledTextHandler) {
        if (columnExpressionTextEditor != null) {
            attachCellExpressionToStyledTextEditor(tableViewerCreatorForColumns, columnExpressionTextEditor, styledTextHandler);
        }
        if (whereConstraintExpressionTextEditor != null) {
            attachCellExpressionToStyledTextEditor(this.getTableViewerCreatorForWhereFilters(),
                    whereConstraintExpressionTextEditor, styledTextHandler);
        }
        if (otherConstraintExpressionTextEditor != null) {
            attachCellExpressionToStyledTextEditor(this.getTableViewerCreatorForOtherFilters(),
                    otherConstraintExpressionTextEditor, styledTextHandler);
        }
    }

    /**
     * DOC amaumont Comment method "attachCellExpressionToStyledTextEditor".
     *
     * @param tableViewerCreator TODO
     * @param styledTextHandler
     * @param expressionEditorText2
     */
    protected void attachCellExpressionToStyledTextEditor(final TableViewerCreator tableViewerCreator,
            final Text expressionTextEditor, final StyledTextHandler styledTextHandler) {
        expressionTextEditor.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                ITableEntry currentModifiedEntry = (ITableEntry) tableViewerCreator.getModifiedObjectInfo()
                        .getCurrentModifiedBean();
                styledTextHandler.setCurrentEntry(currentModifiedEntry);
                Text text = (Text) e.widget;
                if (Math.abs(text.getText().length() - styledTextHandler.getStyledText().getText().length()) > 1) {
                    styledTextHandler.setTextWithoutNotifyListeners(text.getText());
                    // highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                }
            }

        });

        expressionTextEditor.addKeyListener(new KeyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
             */
            @Override
            public void keyPressed(KeyEvent e) {
                // System.out.println("e.character=" + e.character);
                // System.out.println("keyCode=" + e.keyCode);

                boolean ctrl = (e.stateMask & SWT.CTRL) != 0;
                boolean altgr = (e.stateMask & SWT.ALT) != 0;
                if (e.character == '\0' || ctrl && !altgr) {
                    highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                } else {
                    UnnotifiableColorStyledText mapperColorStyledText = (UnnotifiableColorStyledText) styledTextHandler
                            .getStyledText();
                    Point selection = expressionTextEditor.getSelection();
                    if (e.character == '\r' || e.character == '\u001b') {
                        e.doit = false;
                        styledTextHandler.setTextWithoutNotifyListeners(expressionTextEditor.getText());
                        highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                    } else {
                        if (e.character == SWT.BS || e.character == SWT.DEL) {
                            if (selection.x == selection.y) {

                                if (e.character == SWT.BS) {
                                    if (selection.x - 1 > 0 && mapperColorStyledText.getText().length() > selection.x - 1) {
                                        char previousChar = mapperColorStyledText.getText().charAt(selection.x - 1);
                                        if (previousChar == '\n') {
                                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x - 2, 2, ""); //$NON-NLS-1$
                                        } else {
                                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x - 1, 1, ""); //$NON-NLS-1$
                                        }
                                    }
                                } else {
                                    if (selection.x < mapperColorStyledText.getText().length()) {
                                        char nextChar = mapperColorStyledText.getText().charAt(selection.x);
                                        if (nextChar == '\r') {
                                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, 2, ""); //$NON-NLS-1$
                                        } else {
                                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, 1, ""); //$NON-NLS-1$
                                        }
                                    }
                                }

                            } else {
                                if (selection.y <= mapperColorStyledText.getCharCount()) {
                                    mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, selection.y
                                            - selection.x, ""); //$NON-NLS-1$
                                }
                                highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                            }
                        } else {
                            // System.out.println("selection.x="+selection.x);
                            // System.out.println("selection.y="+selection.y);
                            // System.out.println("mapperColorStyledText.getText()="+mapperColorStyledText.getText().length());
                            if (selection.y <= mapperColorStyledText.getCharCount()) {
                                mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, selection.y
                                        - selection.x, String.valueOf(e.character));
                            }
                            highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                        }
                    }

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // highlightLineOfCursorPosition();
            }

        });

        expressionTextEditor.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
            }

            @Override
            public void mouseDown(MouseEvent e) {
                highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
            }

            @Override
            public void mouseUp(MouseEvent e) {

            }

        });

    }

    private void highlightLineAndSetSelectionOfStyledText(final Text expressionTextEditor) {
        final StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                StyledText styledText = styledTextHandler.getStyledText();
                if (styledText.isDisposed()) {
                    return;
                }

                String text = expressionTextEditor.getText();
                Point selection = expressionTextEditor.getSelection();
                if (selection.x - 1 > 0) {
                    while (expressionTextEditor.getLineDelimiter().equals(text.charAt(selection.x - 1))) {
                        selection.x++;
                    }
                }
                if (selection.y - 1 > 0) {
                    while (expressionTextEditor.getLineDelimiter().equals(text.charAt(selection.y - 1))) {
                        selection.y++;
                    }
                }
                int length = styledText.getText().length();
                if (selection.x < 0) {
                    selection.x = 0;
                }
                if (selection.x > length) {
                    selection.x = length;
                }
                if (selection.y < 0) {
                    selection.y = 0;
                }
                if (selection.y > length) {
                    selection.y = length;
                }
                styledText.setSelection(selection);
                styledTextHandler.highlightLineOfCursorPosition(selection);

            }
        };
        new AsynchronousThreading(50, true, DataMapTableView.this.getDisplay(), runnable).start();

    }

    /**
     *
     * Init proposals of Text control, and StyledText in tab "Expression editor".
     *
     * @param textControl
     * @param zones
     * @param tableViewerCreator
     * @param currentModifiedEntry
     */
    protected void initExpressionProposals(final ExtendedTextCellEditorWithProposal textCellEditor, Zone[] zones,
            final TableViewerCreator tableViewerCreator, ITableEntry currentModifiedEntry) {
        if (this.expressionProposalProvider == null) {
            IContentProposalProvider[] contentProposalProviders = new IContentProposalProvider[0];
            if (!MapperMain.isStandAloneMode()) {
                contentProposalProviders = new IContentProposalProvider[] {
                // new ProcessProposalProvider(mapperManager.getComponent().getProcess())
                new KeyWordProposalProvider("tsql", "KEYWORD1") }; //$NON-NLS-1$ //$NON-NLS-2$
            }
            this.expressionProposalProvider = new ExpressionProposalProvider(mapperManager, contentProposalProviders);
        }
        this.expressionProposalProvider.init(abstractDataMapTable, zones, currentModifiedEntry);
        textCellEditor.setContentProposalProvider(this.expressionProposalProvider);

        StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
        styledTextHandler.getStyledText().setEnabled(true);

        ContentProposalAdapterExtended expressionProposalStyledText = styledTextHandler.getContentProposalAdapter();
        expressionProposalStyledText.setContentProposalProvider(this.expressionProposalProvider);

        // System.out.println("init expression proposal:"+this.expressionProposal);
    }

    protected ExtendedTextCellEditorWithProposal createExpressionCellEditor(final TableViewerCreator tableViewerCreator,
            TableViewerCreatorColumn column, final Zone[] zones, boolean isConstraintExpressionCellEditor) {
        CellEditorDialogBehavior behavior = new CellEditorDialogBehavior();
        final ExtendedTextCellEditorWithProposal cellEditor = new ExtendedTextCellEditorWithProposal(
                tableViewerCreator.getTable(), SWT.MULTI | SWT.BORDER, column, behavior) {

            @Override
            public void activate() {
                super.activate();
            }

        };
        dialog = new ExpressionBuilderDialogForElt(tableViewerCreator.getCompositeParent().getShell(), cellEditor, mapperManager,
                tableViewerCreator, column);

        behavior.setCellEditorDialog(dialog);

        final Text expressionTextEditor = cellEditor.getTextControl();

        if (isConstraintExpressionCellEditor) {
            // moved to it's caller to execute
            // constraintExpressionTextEditor = expressionTextEditor;
        } else {
            columnExpressionTextEditor = expressionTextEditor;
        }

        cellEditor.addListener(new ICellEditorListener() {

            Text text = cellEditor.getTextControl();

            @Override
            public void applyEditorValue() {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreator.getModifiedObjectInfo();
                mapperManager.getUiManager().parseNewExpression(text.getText(),
                        (ITableEntry) modifiedObjectInfo.getCurrentModifiedBean(), true);
            }

            @Override
            public void cancelEditor() {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreator.getModifiedObjectInfo();
                text.setText((String) modifiedObjectInfo.getOriginalPropertyBeanValue());
                ITableEntry tableEntry = (ITableEntry) (modifiedObjectInfo.getCurrentModifiedBean() != null ? modifiedObjectInfo
                        .getCurrentModifiedBean() : modifiedObjectInfo.getPreviousModifiedBean());
                String originalExpression = (String) modifiedObjectInfo.getOriginalPropertyBeanValue();
                mapperManager.getUiManager().parseNewExpression(originalExpression, tableEntry, true);
            }

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {

                if (expressionTextEditor.isFocusControl() || lastExpressionEditorTextWhichLostFocus == expressionTextEditor) {
                    ModifiedObjectInfo modifiedObjectInfo = tableViewerCreator.getModifiedObjectInfo();
                    ITableEntry tableEntry = (ITableEntry) (modifiedObjectInfo.getCurrentModifiedBean() != null ? modifiedObjectInfo
                            .getCurrentModifiedBean() : modifiedObjectInfo.getPreviousModifiedBean());
                    mapperManager.getUiManager().parseNewExpression(text.getText(), tableEntry, false);
                    resizeTextEditor(text, tableViewerCreator);
                }
            }

        });
        expressionTextEditor.addControlListener(new ControlListener() {

            ExecutionLimiter executionLimiter = null;

            @Override
            public void controlMoved(ControlEvent e) {
            }

            @Override
            public void controlResized(ControlEvent e) {
                if (executionLimiter == null) {
                    executionLimiter = new ExecutionLimiter(50, true) {

                        @Override
                        public void execute(boolean isFinalExecution, Object data) {

                            if (isFinalExecution && !expressionTextEditor.isDisposed()) {
                                expressionTextEditor.getDisplay().syncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (expressionTextEditor.isDisposed()) {
                                            return;
                                        }
                                        resizeTextEditor(expressionTextEditor, tableViewerCreator);
                                    }

                                });
                            }
                        }

                    };
                }
                executionLimiter.startIfExecutable();
            }

        });
        expressionTextEditor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                ITableEntry currentModifiedEntry = (ITableEntry) tableViewerCreator.getModifiedObjectInfo()
                        .getCurrentModifiedBean();
                initExpressionProposals(cellEditor, zones, tableViewerCreator, currentModifiedEntry);
                resizeTextEditor(expressionTextEditor, tableViewerCreator);
                StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
                styledTextHandler.setCurrentEntry(currentModifiedEntry);
                styledTextHandler.setTextWithoutNotifyListeners(currentModifiedEntry.getExpression() == null ? "" : currentModifiedEntry.getExpression()); //$NON-NLS-1$
            }

            @Override
            public void focusLost(FocusEvent e) {
                expressionEditorTextSelectionBeforeFocusLost = expressionTextEditor.getSelection();
                lastExpressionEditorTextWhichLostFocus = expressionTextEditor;
            }

        });
        column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

            @Override
            public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                return super.getCellEditorTypedValue(cellEditor, originalTypedValue);
            }

            @Override
            public String getColumnText(CellEditor cellEditor, Object bean, Object cellEditorTypedValue) {
                return super.getColumnText(cellEditor, bean, cellEditorTypedValue).replaceAll("[\r\n\t]+", " ... "); //$NON-NLS-1$ //$NON-NLS-2$
            }

            @Override
            public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                return super.getOriginalTypedValue(cellEditor, cellEditorTypedValue);
            }

        });
        return cellEditor;
    }

    private void resizeTextEditor(Text textEditor, TableViewerCreator tableViewerCreator) {

        Point currentSize = textEditor.getSize();
        Rectangle currentBounds = textEditor.getBounds();
        String text = textEditor.getText();
        Table currentTable = tableViewerCreator.getTable();
        int itemHeight = currentTable.getItemHeight();
        int minHeight = itemHeight + 3;
        int maxHeight = 2 * itemHeight + 4;
        int newHeight = 0;
        if ((text.contains("\n") || text.contains("\r")) && currentBounds.y + maxHeight < currentTable.getBounds().height) { //$NON-NLS-1$ //$NON-NLS-2$
            newHeight = maxHeight;
        } else {
            newHeight = minHeight;
        }
        if (currentSize.y != newHeight) {
            textEditor.setSize(textEditor.getSize().x, newHeight);
        }
    }

    /**
     * DOC amaumont Comment method "updateGridDataHeightForTableConstraints".
     */
    public void updateGridDataHeightForTableConstraints() {

        updateGridDataHeightForTableConstraints(getTableViewerCreatorForWhereFilters().getTable());

    }

    public void updateGridDataHeightForTableConstraints(Table _tableForConstraints) {
        GridData _tableForConstraintsGridData = (GridData) _tableForConstraints.getLayoutData();

        int moreSpace = WindowSystem.isGTK() ? _tableForConstraints.getItemHeight() : 0;
        _tableForConstraintsGridData.heightHint = _tableForConstraints.getItemCount() * _tableForConstraints.getItemHeight()
                + _tableForConstraints.getItemHeight() / 2 + moreSpace;
    }

    /**
     * DOC amaumont Comment method "unselectAllEntries".
     */
    public void unselectAllEntries() {
        unselectAllColumnEntries();
        unselectAllConstraintEntries();
    }

    /**
     * DOC amaumont Comment method "unselectAllConstraintEntries".
     */
    public void unselectAllConstraintEntries() {
        // nothing by default, override if necessary
    }

    /**
     * DOC amaumont Comment method "unselectAllColumnEntries".
     */
    public void unselectAllColumnEntries() {
        tableViewerCreatorForColumns.getSelectionHelper().deselectAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
     */
    @Override
    public void setCursor(Cursor cursor) {
        if (this.currentCursor != null) {
            this.currentCursor.dispose();
        }
        this.currentCursor = cursor;
        super.setCursor(cursor);
    }

    /**
     * DOC amaumont Comment method "selectThisDataMapTableView".
     */
    protected void selectThisDataMapTableView() {
        UIManager uiManager = mapperManager.getUiManager();
        if (uiManager.getCurrentSelectedInputTableView() != DataMapTableView.this
                && uiManager.getCurrentSelectedOutputTableView() != DataMapTableView.this) {

            uiManager.selectDataMapTableView(DataMapTableView.this, true, false);
        }
    }

    public void updateSelectedDataMapTableViewTitle() {
        nameLabel.setText(getTitle());
        nameLabel.getParent().layout();
    }

    /**
     *
     * Provide a color provider for Constraints table and dataMap table.
     *
     *
     * <br/>
     *
     * $Id: DataMapTableView.java 2010 2007-02-12 13:18:38Z amaumont $
     *
     */
    class ExpressionColorProvider {

        /**
         * DOC amaumont Comment method "getBackgroundColor".
         *
         * @param isBackground TODO
         * @param keyProblem
         * @param expression
         */
        public Color getColor(boolean isBackground, List<Problem> problems, String... keyProblems) {

            if (problems != null) {
                boolean hasError = false;
                boolean hasWarning = false;
                for (Problem problem : problems) {
                    for (String keyProblem : keyProblems) {
                        if (keyProblem.equals(problem.getKey())) {
                            if (problem.getStatus() == Problem.ProblemStatus.ERROR) {
                                hasError = true;
                            }
                            if (problem.getStatus() == Problem.ProblemStatus.WARNING) {
                                hasWarning = true;
                            }
                        }
                    }
                }
                if (hasError) {
                    if (isBackground) {
                        return ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_ERROR_EXPRESSION_CELL);
                    } else {
                        return ColorProviderMapper.getColor(ColorInfo.COLOR_FOREGROUND_ERROR_EXPRESSION_CELL);
                    }
                }
                if (hasWarning) {
                    if (isBackground) {
                        return ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_WARNING_EXPRESSION_CELL);
                    } else {
                        return null;
                    }
                }

            }
            return null;

        }

    }

    protected ExpressionColorProvider getExpressionColorProvider() {
        return this.expressionColorProvider;
    }

    /**
     * DOC amaumont Comment method "getCommonBackgroundColor".
     *
     * @param element
     * @param columnIndex
     * @return
     */
    protected Color getBackgroundCellColor(TableViewerCreatorNotModifiable tableViewerCreator, Object element, int columnIndex) {
        return getCellColor(tableViewerCreator, element, columnIndex, true);
    }

    /**
     * DOC amaumont Comment method "getCellColor".
     *
     * @param tableViewerCreator
     * @param element
     * @param columnIndex
     * @param isBackground TODO
     * @return
     */
    protected Color getCellColor(TableViewerCreatorNotModifiable tableViewerCreator, Object element, int columnIndex,
            boolean isBackground) {
        ITableEntry entry = (ITableEntry) element;
        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) tableViewerCreator.getColumns()
                .get(columnIndex);
        if (column.getId().equals(ID_EXPRESSION_COLUMN)) {
            return expressionColorProvider.getColor(isBackground, entry.getProblems(),
                    ProblemsManager.KEY_INPUT_EXPRESSION_EMPTY, ProblemsManager.KEY_OUTPUT_EXPRESSION_EMPTY);
        }
        return null;
    }

    /**
     * DOC amaumont Comment method "getCommonBackgroundColor".
     *
     * @param element
     * @param columnIndex
     * @return
     */
    protected Color getForegroundCellColor(TableViewerCreatorNotModifiable tableViewerCreator, Object element, int columnIndex) {
        return getCellColor(tableViewerCreator, element, columnIndex, false);
    }

    /**
     * DOC amaumont Comment method "unselectAllEntriesIfErrorDetected".
     *
     * @param e
     */
    protected void unselectAllEntriesIfErrorDetected(TableCellValueModifiedEvent e) {
        if (e.column.getId().equals(ID_EXPRESSION_COLUMN)) {
            ITableEntry currentEntry = (ITableEntry) e.bean;
            TableViewer tableViewer = null;
            if (currentEntry instanceof IColumnEntry) {
                tableViewer = DataMapTableView.this.getTableViewerCreatorForColumns().getTableViewer();
            } else if (currentEntry instanceof FilterTableEntry) {
                if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) currentEntry).getFilterKind())) {
                    tableViewer = entendedTableViewerForOtherClauses.getTableViewerCreator().getTableViewer();
                } else {
                    tableViewer = extendedTableViewerForFilters.getTableViewerCreator().getTableViewer();
                }
            }
            if (currentEntry.getProblems() != null) {
                tableViewer.getTable().deselectAll();
            }
        }
    }

    /**
     * DOC amaumont Comment method "processEnterKeyDown".
     *
     * @param tableViewer
     * @param event
     */
    protected void processEnterKeyDown(final TableViewerCreator tableViewerCreator, Event event) {
        if (event.character == '\r') {
            Object element = null;
            if (tableViewerCreator.getTable().getSelectionCount() == 1) {
                element = tableViewerCreator.getTable().getSelection()[0].getData();
            } else {
                element = tableViewerCreator.getModifiedObjectInfo().getPreviousModifiedBean();
            }
            if (element != null) {
                int indexColumn = tableViewerCreator.getColumn(ID_EXPRESSION_COLUMN).getIndex();
                tableViewerCreator.getTableViewer().editElement(element, indexColumn);
            }
        }
    }

    public abstract boolean toolbarNeedToHaveRightStyle();

    public abstract boolean hasDropDownToolBarItem();

    /**
     * DOC amaumont Comment method "parseExpression".
     *
     * @param event
     * @param tableViewerCreator
     * @param tableEntry
     */
    protected void parseExpressionIfNeeded(ModifiedBeanEvent event, TableViewerCreator tableViewerCreator, ITableEntry tableEntry) {
        if (ID_EXPRESSION_COLUMN.equals(event.column.getId())) {
            mapperManager.getUiManager().parseExpression(tableEntry.getExpression(), tableEntry, false, false, false);
            mapperManager.getUiManager().refreshBackground(false, false);
            if (tableEntry instanceof InputColumnTableEntry) {
                InputColumnTableEntry inputEntry = (InputColumnTableEntry) tableEntry;
                if (inputEntry.getExpression() == null || inputEntry.getExpression().trim().length() == 0) {
                    inputEntry.setOperator(""); //$NON-NLS-1$
                } else if (inputEntry.getOperator() == null || inputEntry.getOperator().trim().length() == 0) {
                    inputEntry.setOperator("="); //$NON-NLS-1$
                }
                mapperManager.getProblemsManager().checkProblemsForTableEntry(tableEntry, true);
                tableViewerCreator.getTableViewer().refresh(tableEntry);
            }
        }
    }

    /**
     * Getter for extendedTableViewerForColumns.
     *
     * @return the extendedTableViewerForColumns
     */
    public AbstractExtendedTableViewer<IColumnEntry> getExtendedTableViewerForColumns() {
        return this.extendedTableViewerForColumns;
    }

    /**
     * Getter for extendedTableViewerForFilters.
     *
     * @return the extendedTableViewerForFilters
     */
    // public AbstractExtendedTableViewer<FilterTableEntry> getExtendedTableViewerForFilters() {
    // return this.extendedTableViewerForFilters;
    // }

    public int getHeaderHeight() {
        if (WindowSystem.isGTK()) {
            return HEADER_HEIGHT + (hasDropDownToolBarItem() ? 10 : 2);
        } else {
            return HEADER_HEIGHT + (hasDropDownToolBarItem() ? 8 : 0);
        }
    }

    public Point getRealToolbarSize() {
        return realToolbarSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
     */
    @Override
    public void setLayout(Layout layout) {
        super.setLayout(layout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Control#setLayoutData(java.lang.Object)
     */
    @Override
    public void setLayoutData(Object layoutData) {
        super.setLayoutData(layoutData);
    }

    /**
     * Getter for extendedTableViewerForFilters.
     *
     * @return the extendedTableViewerForFilters
     */
    public AbstractExtendedTableViewer<FilterTableEntry> getExtendedTableViewerForWhereFilters() {
        return this.extendedTableViewerForFilters;
    }

    /**
     * Getter for entendedTableViewerForOtherClauses.
     *
     * @return the entendedTableViewerForOtherClauses
     */
    public AbstractExtendedTableViewer<FilterTableEntry> getEntendedTableViewerForOtherClauses() {
        return this.entendedTableViewerForOtherClauses;
    }

    public String getNameFilter() {
        return this.columnNameTextFilter.getText().trim();
    }

    class selectorViewerFilter extends ViewerFilter {

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            String pattern = getNameFilter();
            if (!columnNameFilter.getSelection()) {
                pattern = "";
            }
            // to collect all check buttons into a map
            List<Button> btnList = new ArrayList<>();
            Map<String, Button> btnMap = new HashMap<String, Button>();
            if (tableForEntries != null) {
                for (Control c : tableForEntries.getChildren()) {
                    if (c instanceof Button) {
                        Button b = (Button) c;
                        Object ID_EXPLICIT_JOIN = b.getData("ID_EXPLICIT_JOIN");
                        if (ID_EXPLICIT_JOIN != null) {
                            String columnName = String.valueOf(b.getData("columnName"));
                            btnMap.put(columnName, b);
                        }
                        btnList.add(b);
                    }
                }

            }

            SearchPattern matcher = new SearchPattern();
            // SearchPattern for dynamic search/exact match/fuzzy match
            matcher.setPattern("*" + pattern.trim() + "*");
            if (element instanceof OutputColumnTableEntry) {
                OutputColumnTableEntry outputColumn = (OutputColumnTableEntry) element;
                //
                if (outputColumn.getParent() instanceof OutputTable) {
                    IMetadataColumn metadataColumn = outputColumn.getMetadataColumn();
                    if (metadataColumn != null && (!matcher.matches(metadataColumn.getLabel()))) {
                        return false;

                    }

                }

            }
            if (element instanceof InputColumnTableEntry) {
                InputColumnTableEntry inputColumn = (InputColumnTableEntry) element;
                if (inputColumn.getParent() instanceof InputTable) {
                    IMetadataColumn metadataColumn = inputColumn.getMetadataColumn();
                    String originalDbColumnName = inputColumn.getMetadataColumn().getOriginalDbColumnName();
                    Button checkBtn = btnMap.get(originalDbColumnName);
                    if (metadataColumn != null && (!matcher.matches(metadataColumn.getLabel()))) {
                        if (checkBtn != null) {
                            checkBtn.setVisible(false);
                        }
                        return false;
                    } else {
                        if (checkBtn != null) {
                            checkBtn.setVisible(true);
                        }
                    }

                }

            }
            return true;
        }

    }

    public Text getColumnNameFilterText() {
        return this.columnNameTextFilter;
    }
}
