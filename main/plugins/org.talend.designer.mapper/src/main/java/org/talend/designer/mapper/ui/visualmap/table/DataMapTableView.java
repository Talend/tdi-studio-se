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
package org.talend.designer.mapper.ui.visualmap.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.events.MouseAdapter;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.SearchPattern;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.runtime.thread.AsynchronousThreading;
import org.talend.commons.ui.runtime.utils.ControlUtils;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.ExtendedTextCellEditorWithProposal;
import org.talend.commons.ui.swt.proposal.ProposalUtils;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.threading.ExecutionLimiterImproved;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendTypeFilter;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.Problem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.runtime.services.IExpressionBuilderDialogService;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.IDataMapTableView;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionTrace;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.mapper.MapperMain;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;
import org.talend.designer.mapper.ui.color.ColorInfo;
import org.talend.designer.mapper.ui.color.ColorProviderMapper;
import org.talend.designer.mapper.ui.dialog.ListStringValueDialog;
import org.talend.designer.mapper.ui.dnd.DragNDrop;
import org.talend.designer.mapper.ui.event.MousePositionAnalyser;
import org.talend.designer.mapper.ui.event.ResizeHelper;
import org.talend.designer.mapper.ui.event.ResizeHelper.RESIZE_MODE;
import org.talend.designer.mapper.ui.font.FontInfo;
import org.talend.designer.mapper.ui.font.FontProviderMapper;
import org.talend.designer.mapper.ui.image.ImageInfo;
import org.talend.designer.mapper.ui.image.ImageProviderMapper;
import org.talend.designer.mapper.ui.proposal.expression.ExpressionProposalProvider;
import org.talend.designer.mapper.ui.tabs.StyledTextHandler;
import org.talend.designer.mapper.ui.visualmap.zone.InputsZone;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class DataMapTableView extends Composite implements IDataMapTableView, PropertyChangeListener {

    public static final String REPOSITORY = "Repository";

    public static final String BUILT_IN = "Built-In";

    private final Point realToolbarSize = new Point(0, 0);

    private Table tableForEntries;

    private final ResizeHelper resizeHelper = new ResizeHelper();

    protected MapperManager mapperManager;

    protected TableViewerCreator tableViewerCreatorForColumns;

    protected IDataMapTable abstractDataMapTable;

    protected Composite headerComposite;

    protected GridData warnLabelData;

    protected Label warningLabel;

    protected Label nameLabel;

    private ToolItem minimizeButton;

    protected ToolItem condensedItem;

    protected int heightForRestore;

    protected Layout parentLayout;

    protected TableViewerCreator tableViewerCreatorForFilters;

    protected TableViewerCreator tableViewerCreatorForGlobalMap;

    protected TableViewerCreator mapSettingViewerCreator;

    protected Table tableForConstraints;

    protected Table tableForGlobalMap;

    protected Table mapSettingTable;

    private boolean executeSelectionEvent = true;

    protected ToolBar toolBarActions;

    private ExpressionProposalProvider expressionProposalProvider;

    protected Point expressionEditorTextSelectionBeforeFocusLost;

    protected Text lastExpressionEditorTextWhichLostFocus;

    protected Composite centerComposite;

    private Text columnExpressionTextEditor;

    private Text constraintExpressionTextEditor;

    private Text columnNameTextFilter;

    private Label filterImageLabel;

    private Label expressionImageLabel;

    private Cursor currentCursor;

    private final ExpressionColorProvider expressionColorProvider;

    private Listener showErrorMessageListener;

    protected boolean forceExecuteSelectionEvent;

    private AbstractExtendedTableViewer<IColumnEntry> extendedTableViewerForColumns;

    protected AbstractExtendedTableViewer<FilterTableEntry> extendedTableViewerForFilters;

    protected AbstractExtendedTableViewer<GlobalMapEntry> extendedTableViewerForGlobalMap;

    protected AbstractExtendedTableViewer<GlobalMapEntry> extendedTableViewerForMapSetting;

    private static Image imageKey;

    private static Image imageEmpty;

    private static int constraintCounter = 0;

    protected static final int TIME_BEFORE_NEW_REFRESH_BACKGROUND = 150;

    protected static final int OFFSET_HEIGHT_TRIGGER = 15;

    protected static final int COLUMN_EXPRESSION_SIZE_WEIGHT = 60;

    protected static final int COLUMN_NAME_SIZE_WEIGHT = 40;

    protected static final int ADJUST_WIDTH_VALUE = 0;

    private static final int HEADER_HEIGHT = 23;

    public static final String ID_NAME_COLUMN = "ID_NAME_COLUMN"; //$NON-NLS-1$

    public static final String PREVIEW_COLUMN = "PREVIEW_COLUMN"; //$NON-NLS-1$

    public static final String ID_OPERATOR = "ID_OPERATOR"; //$NON-NLS-1$

    public static final String ID_EXPRESSION_COLUMN = "ID_EXPRESSION_COLUMN"; //$NON-NLS-1$

    public static final String MAP_SETTING_COLUMN = "MAP_SETTING_COLUMN"; //$NON-NLS-1$

    public static final String MATCH_MODEL_SETTING = "Match Model"; //$NON-NLS-1$

    public static final String LOOKUP_MODEL_SETTING = "Lookup Model"; //$NON-NLS-1$

    public static final String JOIN_MODEL_SETTING = "Join Model"; //$NON-NLS-1$

    public static final String PERSISTENCE_MODEL_SETTING = "Store temp data"; //$NON-NLS-1$

    public static final String OUTPUT_REJECT = "Catch output reject"; //$NON-NLS-1$

    public static final String LOOK_UP_INNER_JOIN_REJECT = "Catch lookup inner join reject"; //$NON-NLS-1$

    public static final String SCHEMA_TYPE = "Schema Type"; //$NON-NLS-1$

    public static final String SCHEMA_ID = "Schema Id"; //$NON-NLS-1$

    public static final String SCHEMA_SETTING_COLUMN = "Schema Setting Column"; //$NON-NLS-1$

    public static final String SCHEMA_ID_SETTING_COLUMN = "Schema ID Setting Column"; //$NON-NLS-1$

    public static final String COLUMN_NAME = "Column"; //$NON-NLS-1$

    protected GridData tableForConstraintsGridData;

    protected GridData tableForSchemaIDGridData;

    protected GridData tableForGlobalMapGridData;

    protected GridData tableForMapSettingGridData;

    private ExpressionProposalProvider expressionProposalProviderForExpressionFilter;

    private UnnotifiableColorStyledText expressionFilterText;

    private Button openExpressionBuilder; // hywang
                                          // add
                                          // for
                                          // 9225

    public static final String DEFAULT_EXPRESSION_FILTER = "<Type your filter expression>"; //$NON-NLS-1$ // DO NOT TRANSLATE IT !

    public static final String DEFAULT_POST_MATCHING_EXPRESSION_FILTER = "";

    //        Messages.getString("DataMapTableView.defaultPostMatchingFilterExpression"); //$NON-NLS-1$

    public static final String DEFAULT_OUT_EXPRESSION_FILTER = "";

    public static final String DEFAULT_FILTER = "";//$NON-NLS-1$

    //     Messages.getString("DataMapTableView.defaultOutputFilterExpression"); //$NON-NLS-1$

    private static final String EXPRESSION_FILTER_ENTRY = "EXPRESSION_FILTER_ENTRY"; //$NON-NLS-1$

    private String previousTextForExpressionFilter;

    private ContentProposalAdapterExtended proposalForExpressionFilterText;

    private ExecutionLimiterImproved executionLimiterForCheckProblemsExpressionFilter;

    private ExecutionLimiterImproved executionLimiterForExpressionFilterSetText = null;

    private ContentProposalAdapterExtended expressionProposalStyledText;

    private ToolItem activateFilterCheck;

    private ToolItem columnNameFilter;

    private boolean previousStateCheckFilter;

    private TableViewer viewer;

    private boolean previousColumnNameFilter;

    private IExpressionBuilderDialogController dialog;

    private boolean customSized;

    protected int changedOptions = 0;

    private Color color = null;

    protected Color previewColor = null;

    private boolean needInitProposals = false;

    protected MetadataTalendTypeFilter talendTypeFilter;

    /**
     * doc
     */
    enum CellValueType {

        BOOL,
        SCHEMA_TYPE,
        SCHEMA_ID,
        LOOKUP_MODEL,
        MATCH_MODEL,
        JOIN_MODEL,
        PERSISTENCE_MODEL

    }

    /**
     *
     * Call loaded() method after instanciate this class.
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
        this.talendTypeFilter = NodeUtil.createMetadataTalendTypeFilter(mapperManager.getAbstractMapComponent());
        expressionColorProvider = new ExpressionColorProvider();

        color = new Color(Display.getDefault(), 238, 238, 0);
        previewColor = new Color(Display.getDefault(), 235, 0, 219);

        createComponents();
        addListeners();
        mapperManager.addTablePair(DataMapTableView.this, abstractDataMapTable);
        if (abstractDataMapTable instanceof AbstractInOutTable) {
            IOConnection ioConnection = ((AbstractInOutTable) abstractDataMapTable).getConnection();
            if (ioConnection != null && ioConnection.getConnecion() != null) {
                IConnection connection = ioConnection.getConnecion();
                if (connection instanceof Connection) {
                    ConnectionTrace connectionTrace = ((Connection) connection).getConnectionTrace();
                    if (connectionTrace != null) {
                        connectionTrace.addPropertyChangeListener(this);
                    }
                }
            }
        }

    }

    private void createComponents() {

        final Display display = this.getDisplay();
        // final Color listForeground =
        // display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

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

        warningLabel = new Label(headerComposite, SWT.NONE);
        warningLabel.setImage(ImageProvider.getImage(EImage.WARNING_ICON));
        warnLabelData = new GridData();
        warningLabel.setLayoutData(warnLabelData);
        warnLabelData.exclude = true;
        nameLabel = new Label(headerComposite, SWT.NONE);
        nameLabel.setFont(FontProviderMapper.getFont(FontInfo.FONT_SYSTEM_BOLD));
        if (abstractDataMapTable instanceof OutputTable && ((OutputTable) abstractDataMapTable).getIsJoinTableOf() != null) {
            nameLabel.setText("Join Table " + abstractDataMapTable.getName() + " linked with "
                    + ((OutputTable) abstractDataMapTable).getIsJoinTableOf());
        } else {
            nameLabel.setText(abstractDataMapTable.getName());
        }
        nameLabel.setToolTipText(abstractDataMapTable.getName());
        GridData dataNameLabel = new GridData(GridData.FILL_HORIZONTAL);
        dataNameLabel.minimumWidth = nameLabel.getText().length() * 8;

        nameLabel.setLayoutData(dataNameLabel);
        // nameLabel.setBackground(nameLabel.getDisplay().getSystemColor(SWT.COLOR_RED));

        int rightStyle = toolbarNeedToHaveRightStyle() ? SWT.RIGHT : SWT.NONE;
        toolBarActions = new ToolBar(headerComposite, SWT.FLAT | rightStyle | SWT.NONE);
        // toolBarActions.setBackground(nameLabel.getDisplay().getSystemColor(SWT.COLOR_BLUE));

        if (addToolItems()) {
            addToolItemSeparator();
        }

        minimizeButton = new ToolItem(toolBarActions, SWT.PUSH);
        realToolbarSize.x += 45;

        Point sizeToolBar = toolBarActions.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // System.out.println(getDataMapTable().getName());
        // System.out.println("sizeToolBar:" + sizeToolBar);

        GridData gridDataToolbar = new GridData();

        // gridData.grabExcessHorizontalSpace = true;
        // gridData.horizontalAlignment = SWT.END;
        gridDataToolbar.heightHint = sizeToolBar.y;
        if (toolbarNeedToHaveRightStyle() && WindowSystem.isWIN32()) {
            if (realToolbarSize != null) {
                gridDataToolbar.widthHint = realToolbarSize.x;
                // System.out.println("realToolbarSize:" + realToolbarSize);
            }
            // to correct invalid margin when SWT.RIGHT style set in ToolBar
            // gridData.widthHint -= 48;
        }
        if (WindowSystem.isGTK()) {
            gridDataToolbar.heightHint = 26;
        }
        toolBarActions.setLayoutData(gridDataToolbar);
        // gridData.widthHint = 50;

        headerLayout.numColumns = headerComposite.getChildren().length;

        centerComposite = new Composite(this, SWT.NONE);
        GridData centerData = new GridData(GridData.FILL_BOTH);
        centerComposite.setLayoutData(centerData);

        GridLayout centerLayout = new GridLayout(3, false);
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

        createMapSettingTable();
        // if (abstractDataMapTable instanceof InputTable || abstractDataMapTable instanceof OutputTable) {
        // createSchemaSettingTable();
        // createSchemaIDSettingTable();
        // }

        if (mapperManager.isAdvancedMap() && this instanceof OutputDataMapTableView) {
            createExpressionFilter(DEFAULT_OUT_EXPRESSION_FILTER);
            createColumnNameFilter();
            initExtraTable();
        } else {
            initExtraTable();
        }

        createContent();

        if (!mapperManager.componentIsReadOnly()) {
            new DragNDrop(mapperManager, tableForEntries, true, true);
        }

        Composite footerComposite = new Composite(this, SWT.NONE);
        GridData footerGridData = new GridData(10, 2);
        footerComposite.setLayoutData(footerGridData);

        if (WindowSystem.isGTK()) {
            sizeToolBar = toolBarActions.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            gridDataToolbar.widthHint = sizeToolBar.x + 20;
            headerComposite.layout();
        }

    }

    /**
     * DOC amaumont Comment method "createContent".
     */
    protected abstract void createContent();

    protected abstract void createMapSettingTable();

    protected void initMapSettingColumns(final TableViewerCreator<GlobalMapEntry> tableViewerCreator) {
        final Table table = tableViewerCreator.getTable();
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("Property");
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
        column.setModifiable(true);

        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<GlobalMapEntry, Object>() {

            public Object get(GlobalMapEntry bean) {
                return bean.getName();
            }

            public void set(GlobalMapEntry bean, Object value) {
                // do nothing
            }
        });
        column.setColorProvider(new IColumnColorProvider<GlobalMapEntry>() {

            public Color getBackgroundColor(GlobalMapEntry bean) {
                if (needColumnBgColor(bean)) {
                    return color;
                }
                return null;
            }

            public Color getForegroundColor(GlobalMapEntry bean) {
                // TODO Auto-generated method stub
                return null;
            }
        });

        final TableViewerCreatorColumn valueColumn = new TableViewerCreatorColumn(tableViewerCreator);
        valueColumn.setTitle("Value");
        valueColumn.setId(MAP_SETTING_COLUMN);
        valueColumn.setWeight(COLUMN_NAME_SIZE_WEIGHT);
        //        CellEditorValueAdapter comboValueAdapter = CellEditorValueAdapterFactory.getComboAdapterForComboCellEditor("String"); //$NON-NLS-1$
        // cellEditor = new ComboBoxCellEditor();
        // cellEditor.create(table);
        // CCombo functCombo = (CCombo) cellEditor.getControl();
        // functCombo.setEditable(false);
        // valueColumn.setCellEditor(cellEditor, comboValueAdapter);

        final CustomDialogCellEditor cellEditor = new CustomDialogCellEditor(CellValueType.SCHEMA_ID) {

            @Override
            protected Object openDialogBox(Control cellEditorWindow) {
                Object obj = super.openDialogBox(cellEditorWindow);
                if (obj == null) {
                    return openCustomCellDialog(cellEditorWindow.getShell(), this.getType());
                }
                return obj;
            };
        };
        cellEditor.create(table);
        if (!mapperManager.componentIsReadOnly()) {
            valueColumn.setCellEditor(cellEditor);
        }
        valueColumn.setBeanPropertyAccessors(getMapSettingValueAccess(cellEditor));
        valueColumn.setModifiable(true);
        valueColumn.setColorProvider(new IColumnColorProvider<GlobalMapEntry>() {

            public Color getBackgroundColor(GlobalMapEntry bean) {
                if (needColumnBgColor(bean)) {
                    return color;
                }
                return null;
            }

            public Color getForegroundColor(GlobalMapEntry bean) {
                // TODO Auto-generated method stub
                return null;
            }
        });

    }

    /**
     * DOC ycbai Comment method "openCustomCellDialog".
     *
     * @param shell
     * @return
     */
    protected Object openCustomCellDialog(Shell shell, CellValueType type) {
        return null;
    }

    /**
     * DOC ycbai Comment method "getSchemaSettingValueAccess".
     *
     * @param functComboBox
     * @return
     */
    protected IBeanPropertyAccessors<GlobalMapEntry, Object> getSchemaSettingValueAccess(final ComboBoxCellEditor functComboBox) {
        return new IBeanPropertyAccessors<GlobalMapEntry, Object>() {

            public Object get(GlobalMapEntry bean) {
                functComboBox.setItems(new String[] { BUILT_IN, REPOSITORY });
                IDataMapTable parent = bean.getParent();
                AbstractInOutTable table = (AbstractInOutTable) parent;
                if (SCHEMA_TYPE.equals(bean.getName())) {
                    if (bean.getExpression() == null) {
                        return table.getId() == null ? BUILT_IN : REPOSITORY;
                    }
                    return bean.getExpression();
                }

                return "";
            }

            public void set(GlobalMapEntry bean, Object value) {
                if (value == null) {
                    return;
                }
                if (SCHEMA_TYPE.equals(bean.getName())) {
                    bean.setExpression(String.valueOf(value));
                    showSchemaIDSetting(REPOSITORY.equals(value));
                }
            }
        };
    }

    protected IBeanPropertyAccessors<GlobalMapEntry, Object> getMapSettingValueAccess(final CellEditor cellEditor) {
        return null;
    }

    protected boolean needColumnBgColor(GlobalMapEntry bean) {
        return false;
    }

    // only called when open the tmap
    protected void initCondensedItemImage() {
    }

    protected ImageInfo getCondencedItemImage(int i) {
        switch (i) {
        case 0:
            return ImageInfo.CONDENSED_TOOL_ICON;
        case 1:
            return ImageInfo.CONDENSED_TOOL_ICON1;
        case 2:
            return ImageInfo.CONDENSED_TOOL_ICON2;
        case 3:
            return ImageInfo.CONDENSED_TOOL_ICON3;
        case 4:
            return ImageInfo.CONDENSED_TOOL_ICON4;
        case 5:
            return ImageInfo.CONDENSED_TOOL_ICON5;
        case 6:
            return ImageInfo.CONDENSED_TOOL_ICON6;
        default:
            return null;
        }
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
     */
    protected void createTableForColumns() {
        this.extendedTableViewerForColumns = new AbstractExtendedTableViewer<IColumnEntry>(
                abstractDataMapTable.getTableColumnsEntriesModel(), centerComposite) {

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
                newTableViewerCreator.setKeyboardManagementForCellEdition(true);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable()
                // instanceof
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
                        return getBackgroundCellColor(newTableViewerCreator, element, columnIndex);
                    }

                    @Override
                    public Color getForeground(Object element, int columnIndex) {
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
                        TableViewerCreatorColumnNotModifiable column = newTableViewerCreator.getColumns().get(columnIndex);
                        if (getDataMapTable() instanceof AbstractInOutTable) {
                            AbstractInOutTableEntry entry = (AbstractInOutTableEntry) element;
                            if (column.getId().equals(ID_NAME_COLUMN)) {
                                if (entry.getMetadataColumn().isKey()) {
                                    return imageKey;
                                } else {
                                    return imageEmpty;
                                }
                            }
                        }
                        if (column.getImageProvider() != null) {
                            return column.getImageProvider().getImage(element);
                        }
                        return null;
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
        GridData tableEntriesGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableEntriesGridData.grabExcessVerticalSpace = true;
        tableEntriesGridData.horizontalSpan = 3; // for 10690
        tableEntriesGridData.minimumHeight = tableForEntries.getHeaderHeight() + tableForEntries.getItemHeight();
        tableForEntries.setLayoutData(tableEntriesGridData);
        tableViewerCreatorForColumns.setCellModifier(new TableCellModifier(tableViewerCreatorForColumns));
        addTableForColumnsListeners();
    }

    /**
     * DOC amaumont Comment method "addTableForColumnsListeners".
     */
    private void addTableForColumnsListeners() {
        tableViewerCreatorForColumns.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        final TableViewer tableViewerForEntries = tableViewerCreatorForColumns.getTableViewer();

        tableViewerForEntries.addSelectionChangedListener(new ISelectionChangedListener() {

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

            public void handleEvent(Event event) {
                onSelectedEntries(tableViewerForEntries.getSelection(), tableViewerForEntries.getTable().getSelectionIndices());
            }

        });

        tableViewerCreatorForColumns.getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

            public void handle(LineSelectionEvent e) {
                if (forceExecuteSelectionEvent) {
                    forceExecuteSelectionEvent = false;
                    onSelectedEntries(tableViewerForEntries.getSelection(), tableViewerForEntries.getTable()
                            .getSelectionIndices());
                }
            }

        });
        tableForEntries.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForColumns, event);
            }

        });

        abstractDataMapTable.getTableColumnsEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<IColumnEntry>() {

            public void handleEvent(ModifiedBeanEvent<IColumnEntry> event) {

                TableViewerCreator tableViewerCreator = tableViewerCreatorForColumns;
                ITableEntry tableEntry = event.bean;

                parseExpression(event, tableViewerCreator, tableEntry);
            }

        });

        if (abstractDataMapTable instanceof InputTable) {
            InputTable inputTable = (InputTable) abstractDataMapTable;
            inputTable.getTableGlobalMapEntriesModel().addAfterOperationListListener(
                    new IListenableListListener<GlobalMapEntry>() {

                        public void handleEvent(ListenableListEvent<GlobalMapEntry> event) {

                            if (DataMapTableView.this.canBeResizedAtPreferedSize()) {
                                resizeAtExpandedSize();
                            }

                        }

                    });
            inputTable.getTableGlobalMapEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<GlobalMapEntry>() {

                public void handleEvent(ModifiedBeanEvent<GlobalMapEntry> event) {

                    TableViewerCreator tableViewerCreator = tableViewerCreatorForGlobalMap;
                    ITableEntry tableEntry = event.bean;

                    parseExpression(event, tableViewerCreator, tableEntry);
                }

            });
        }
        if (abstractDataMapTable instanceof OutputTable) {
            OutputTable outputTable = (OutputTable) abstractDataMapTable;
            outputTable.getTableFiltersEntriesModel().addAfterOperationListListener(
                    new IListenableListListener<FilterTableEntry>() {

                        public void handleEvent(ListenableListEvent<FilterTableEntry> event) {

                            if (DataMapTableView.this.canBeResizedAtPreferedSize()) {
                                resizeAtExpandedSize();
                            }

                        }

                    });
            outputTable.getTableFiltersEntriesModel().addModifiedBeanListener(new IModifiedBeanListener<FilterTableEntry>() {

                public void handleEvent(ModifiedBeanEvent<FilterTableEntry> event) {

                    TableViewerCreator tableViewerCreator = tableViewerCreatorForFilters;
                    ITableEntry tableEntry = event.bean;

                    parseExpression(event, tableViewerCreator, tableEntry);
                }

            });
        }

        // add menu listener
        tableForEntries.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                if (e.button == 3 && getZone() == Zone.OUTPUTS) {
                    Menu mainMenu = new Menu(tableForEntries);
                    tableForEntries.setMenu(mainMenu);
                    // menu:apply routine
                    MenuItem applyRoutineItem = new MenuItem(mainMenu, SWT.PUSH);
                    applyRoutineItem.setText(Messages.getString("DataMapTableView.menu.applyRoutine"));
                    // Add menu listeners
                    final IService service = GlobalServiceRegister.getDefault().getService(IExpressionBuilderDialogService.class);
                    applyRoutineItem.addListener(SWT.Selection, new Listener() {

                        public void handleEvent(Event event) {
                            IExpressionBuilderDialogController dialogForTable = ((IExpressionBuilderDialogService) service)
                                    .getExpressionBuilderInstance(DataMapTableView.this.getCenterComposite(), null,
                                            mapperManager.getAbstractMapComponent(), true);
                            if (dialogForTable instanceof TrayDialog) {
                                TrayDialog parentDialog = (TrayDialog) dialogForTable;
                                dialogForTable.setDefaultExpression(expressionFilterText.getText());
                                if (Window.OK == parentDialog.open()) {
                                    String expressionForTable = dialogForTable.getExpressionForTable();
                                    TableItem[] selectedTableItems = tableForEntries.getSelection();
                                    for (TableItem tableItem : selectedTableItems) {
                                        ITableEntry currentModifiedEntry = (ITableEntry) tableItem.getData();
                                        String currentExpr = currentModifiedEntry.getExpression();
                                        if (StringUtils.isNotEmpty(expressionForTable) && !expressionForTable.equals(currentExpr)) {
                                            String replacedExpression = expressionForTable;
                                            if (!StringUtils.isEmpty(currentExpr)) {
                                                replacedExpression = expressionForTable.replace("${0}", currentExpr);//$NON-NLS-1$
                                            }
                                            mapperManager.changeEntryExpression(currentModifiedEntry, replacedExpression);
                                            StyledTextHandler textTarget = mapperManager.getUiManager().getTabFolderEditors()
                                                    .getStyledTextHandler();
                                            textTarget.setCurrentEntry(currentModifiedEntry);
                                            textTarget.setTextWithoutNotifyListeners(replacedExpression);
                                            mapperManager.getUiManager().parseNewExpression(replacedExpression,
                                                    currentModifiedEntry, false);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * DOC amaumont Comment method "initShowMessageErrorListener".
     *
     * @param table
     */
    protected void initShowMessageErrorListener(final Table table) {
        showErrorMessageListener = new Listener() {

            public void handleEvent(Event event) {

                switch (event.type) {
                case SWT.MouseMove:

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
                        toolTip = createErrorContentForTooltip(problems);
                    }

                    if (WindowSystem.isGTK() && toolTip == null && table.getToolTipText() != null) {
                        toolTip = defaultToolTip;
                    }
                    // System.out.println("toolTip="+toolTip);
                    // System.out.println("table.getToolTipText()="+table.getToolTipText());
                    setTableToolTipText(table, tableColumn, tableEntry, toolTip);

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
     */
    protected abstract void initExtraTable();

    /**
     * DOC amaumont Comment method "addListeners".
     */
    protected void addListeners() {

        final UIManager uiManager = mapperManager.getUiManager();

        MouseTrackListener mouseTracker = new MouseTrackListener() {

            public void mouseEnter(MouseEvent e) {
            }

            public void mouseExit(MouseEvent e) {
                setDefaultCursor();
                resizeHelper.stopDrag();
            }

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

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e) {
                minimizeTable(!abstractDataMapTable.isMinimized());
            }

        });

        // /////////////////////////////////////////////////////////////////

        initShowMessageErrorListener(tableForEntries);

        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                removeListenerForTrace();
                if (color != null) {
                    color.dispose();
                    color = null;
                }
                if (previewColor != null) {
                    previewColor.dispose();
                    previewColor = null;
                }
            }
        });

    }

    private void removeListenerForTrace() {
        if (abstractDataMapTable instanceof AbstractInOutTable) {
            IOConnection ioConnection = ((AbstractInOutTable) abstractDataMapTable).getConnection();
            if (ioConnection != null && ioConnection.getConnecion() != null) {
                IConnection connection = ioConnection.getConnecion();
                if (connection instanceof Connection) {
                    ConnectionTrace connectionTrace = ((Connection) connection).getConnectionTrace();
                    if (connectionTrace != null) {
                        connectionTrace.removePropertyChangeListener(this);
                    }
                }
            }
        }

    }

    /**
     * DOC amaumont Comment method "addManualTableResizeListeners".
     *
     * @param uiManager
     */
    private void addManualTableResizeListeners(final UIManager uiManager) {
        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                if (currentCursor != null) {
                    currentCursor.dispose();
                }
            }

        });

        Listener tableResizerListener = new Listener() {

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

                        if (newHeight < getMinimumHeight() + OFFSET_HEIGHT_TRIGGER && diff.y < 0) {
                            changeMinimizeState(true);
                            newHeight = getMinimumHeight();
                        } else if (newHeight > getMinimumHeight() + OFFSET_HEIGHT_TRIGGER && diff.y > 0) {
                            changeMinimizeState(false);
                        }
                        changeSize(new Point(newWidth, newHeight), false, true);
                        resizeHelper.setLastDragPoint(newPoint);
                        customSized = true;
                    } else if (!resizeHelper.isDragging()) {
                        Cursor cursor = null;
                        if (mpa.isOnLeftBottomCorner(eventPoint)) {
                            // cursor = new
                            // Cursor(dataMapTableView.getDisplay(),
                            // org.eclipse.swt.SWT.CURSOR_SIZESW);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.BOTH);
                        } else if (mpa.isOnRightBottomCorner(eventPoint)) {
                            // cursor = new
                            // Cursor(dataMapTableView.getDisplay(),
                            // org.eclipse.swt.SWT.CURSOR_SIZESE);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.BOTH);
                        } else if (mpa.isOnLeftBorder(eventPoint)) {
                            // cursor = new
                            // Cursor(dataMapTableView.getDisplay(),
                            // org.eclipse.swt.SWT.CURSOR_SIZEE);
                            // resizeHelper.setCurrentMode(RESIZE_MODE.HORIZONTAL);
                        } else if (mpa.isOnRightBorder(eventPoint)) {
                            // cursor = new
                            // Cursor(dataMapTableView.getDisplay(),
                            // org.eclipse.swt.SWT.CURSOR_SIZEW);
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
                        // gridData = (GridData)
                        // dataMapTableView.getLayoutData();
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

        if (visible) {
            tableForConstraintsGridData.exclude = false;
            tableForConstraints.setVisible(true);
            if (WindowSystem.isGTK()) {
                updateGridDataHeightForTableConstraints();
            }
        } else {
            tableForConstraintsGridData.exclude = true;
            tableForConstraints.setVisible(false);
        }
        tableViewerCreatorForFilters.getTableViewer().refresh();
        resizeAtExpandedSize();
    }

    /**
     * DOC ycbai Comment method "enableDiaplayViewer".
     *
     * @param enable
     */
    public void enableDiaplayViewer(boolean isRepository) {
        MetadataTableEditorView metadataEditorView = mapperManager.getUiManager().getMetadataEditorView(getZone());
        if (mapperManager.componentIsReadOnly()) {
            metadataEditorView.setReadOnly(true);
        } else {
            metadataEditorView.setReadOnly(isRepository);
        }
    }

    /**
     * DOC ycbai Comment method "showSchemaIDSetting".
     *
     * @param visible
     */
    public void showSchemaIDSetting(boolean visible) {
        ExtendedTableModel<GlobalMapEntry> tableMapSettingEntriesModel = ((AbstractInOutTable) abstractDataMapTable)
                .getTableMapSettingEntriesModel();
        if (tableMapSettingEntriesModel != null && visible) {
            tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_ID, null));
        } else {
            GlobalMapEntry schemaIDMapEntry = getGlobalMapEntryByNameFromList(tableMapSettingEntriesModel.getBeansList(),
                    SCHEMA_ID);
            if (schemaIDMapEntry != null) {
                IDataMapTable parent = schemaIDMapEntry.getParent();
                AbstractInOutTable table = (AbstractInOutTable) parent;
                table.setId(null);
                tableMapSettingEntriesModel.remove(schemaIDMapEntry);
                refreshCondensedImage(table, schemaIDMapEntry.getName());
            }
        }
        mapSettingViewerCreator.layout();
        mapSettingViewerCreator.getTableViewer().refresh();
    }

    /**
     * DOC ycbai Comment method "getGlobalMapEntryByNameFromList".
     *
     * @param list
     * @param name
     * @return
     */
    private GlobalMapEntry getGlobalMapEntryByNameFromList(List<GlobalMapEntry> list, String name) {
        if (list == null || name == null) {
            return null;
        }
        for (GlobalMapEntry mapEntry : list) {
            if (name.equals(mapEntry.getName())) {
                return mapEntry;
            }
        }
        return null;
    }

    protected void showTableMapSetting(boolean visible) {
        if (visible) {
            tableForMapSettingGridData.exclude = false;
            mapSettingTable.setVisible(true);
            if (WindowSystem.isGTK()) {
                mapSettingViewerCreator.layout();
            }
            mapSettingViewerCreator.getTableViewer().setSelection(null);
        } else {
            tableForMapSettingGridData.exclude = true;
            mapSettingTable.setVisible(false);
        }
        mapSettingViewerCreator.getTableViewer().refresh();
        resizeAtExpandedSize();
    }

    /**
     * DOC amaumont Comment method "showTableConstraints".
     */
    public void showTableGlobalMap(boolean visible) {

        if (visible) {
            tableForGlobalMapGridData.exclude = false;
            tableForGlobalMap.setVisible(true);
            if (WindowSystem.isGTK()) {
                updateGridDataHeightForTableGlobalMap();
                tableViewerCreatorForGlobalMap.refreshTableEditorControls();
            }

            InputTable inputTable = (InputTable) getDataMapTable();
            ExtendedTableModel<GlobalMapEntry> tableGlobalMapEntriesModel = inputTable.getTableGlobalMapEntriesModel();
            if (tableGlobalMapEntriesModel != null) {
                List<GlobalMapEntry> beansList = tableGlobalMapEntriesModel.getBeansList();
                for (GlobalMapEntry globalMapEntry : beansList) {
                    mapperManager.getUiManager().parseExpression(globalMapEntry.getExpression(), globalMapEntry, false, false,
                            false);
                }
            }

        } else {
            tableForGlobalMapGridData.exclude = true;
            tableForGlobalMap.setVisible(false);

            InputTable inputTable = (InputTable) getDataMapTable();
            ExtendedTableModel<GlobalMapEntry> tableGlobalMapEntriesModel = inputTable.getTableGlobalMapEntriesModel();
            if (tableGlobalMapEntriesModel != null) {
                List<GlobalMapEntry> beansList = tableGlobalMapEntriesModel.getBeansList();
                for (GlobalMapEntry globalMapEntry : beansList) {
                    mapperManager.removeLinksOf(globalMapEntry);
                }
            }
        }
        tableViewerCreatorForGlobalMap.getTableViewer().refresh();
        resizeAtExpandedSize();
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

    public TableViewerCreator getTableViewerCreatorForFilters() {
        return this.tableViewerCreatorForFilters;
    }

    public TableViewerCreator getTableViewerCreatorForGlobalMap() {
        return this.tableViewerCreatorForGlobalMap;
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
        changeSize(getPreferredSize(false, true, false), true, true);
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
        if (newSize.y < getMinimumHeight()) {
            newSize.y = getMinimumHeight();
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
        UIManager uiManager = mapperManager.getUiManager();
        List<ITableEntry> selectionEntries = uiManager.extractSelectedTableEntries(selection);
        if (executeSelectionEvent) {
            uiManager.selectLinks(DataMapTableView.this, selectionEntries, false, false);
            uiManager.selectLinkedMetadataEditorEntries(this, selectionIndices);
        }
        if (selectionIndices.length == 1 && mapperManager.componentIsReadOnly()) {
            ITableEntry tableEntry = selectionEntries.get(0);
            StyledText styledText = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler().getStyledText();
            styledText.setEnabled(true);
            styledText.setEditable(false);
            styledText.setText(tableEntry.getExpression() == null ? "" : tableEntry.getExpression()); //$NON-NLS-1$
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
        boolean isErrorReject = false;
        if (!(getDataMapTable() instanceof OutputTable)) {
            return;
        }
        isErrorReject = ((OutputTable) getDataMapTable()).isErrorRejectTable();

        condensedItem = new ToolItem(toolBarActions, SWT.CHECK);
        condensedItem.setEnabled(!isErrorReject);
        condensedItem.setSelection(((OutputTable) abstractDataMapTable).isActivateCondensedTool());
        condensedItem.setToolTipText("tMap settings");
        initCondensedItemImage();
        condensedItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((OutputTable) abstractDataMapTable).setActivateCondensedTool(condensedItem.getSelection());
                showTableMapSetting(condensedItem.getSelection());
            }

        });

        if (mapperManager.isAdvancedMap()) {
            createActivateFilterCheck();
            createColumnNameFilterCheck();

        } else {

            ToolItem addFilterButton = new ToolItem(toolBarActions, SWT.PUSH);
            addFilterButton.setEnabled(!mapperManager.componentIsReadOnly() && !isErrorReject);
            addFilterButton.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.addFilterRow")); //$NON-NLS-1$
            addFilterButton.setImage(ImageProviderMapper.getImage(ImageInfo.ADD_FILTER_ICON));

            // /////////////////////////////////////////////////////////////////
            if (addFilterButton != null) {

                addFilterButton.addSelectionListener(new SelectionListener() {

                    public void widgetDefaultSelected(SelectionEvent e) {
                    }

                    public void widgetSelected(SelectionEvent e) {

                        Table tableConstraints = tableViewerCreatorForFilters.getTable();
                        int index = tableConstraints.getItemCount();
                        int[] selection = tableViewerCreatorForFilters.getTable().getSelectionIndices();
                        if (selection.length > 0) {
                            index = selection[selection.length - 1] + 1;
                        }
                        mapperManager.addNewFilterEntry(DataMapTableView.this, "newFilter" + ++constraintCounter, index); //$NON-NLS-1$
                        updateGridDataHeightForTableConstraints();
                        DataMapTableView.this.changeSize(DataMapTableView.this.getPreferredSize(false, true, true), true, true);
                        tableViewerCreatorForFilters.getTableViewer().refresh();
                        mapperManager.getUiManager().refreshBackground(true, false);
                        showTableConstraints(true);
                        changeMinimizeState(false);
                        tableViewerCreatorForFilters.layout();
                    }

                });
            }
            // /////////////////////////////////////////////////////////////////
        }

    }

    /**
     * DOC amaumont Comment method "createActivateFilterCheck".
     */
    protected void createActivateFilterCheck() {
        AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();
        boolean isErrorReject = false;
        if (getDataMapTable() instanceof OutputTable) {
            isErrorReject = getMapperManager().ERROR_REJECT.equals(getDataMapTable().getName());
        }
        activateFilterCheck = new ToolItem(toolBarActions, SWT.CHECK);
        activateFilterCheck.setEnabled(!mapperManager.componentIsReadOnly() && !isErrorReject);
        previousStateCheckFilter = table.isActivateExpressionFilter();
        activateFilterCheck.setSelection(table.isActivateExpressionFilter());
        activateFilterCheck.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.ExpressionFilter")); //$NON-NLS-1$
        activateFilterCheck.setImage(ImageProviderMapper.getImage(ImageInfo.ACTIVATE_FILTER_ICON));

        // /////////////////////////////////////////////////////////////////
        if (activateFilterCheck != null) {

            activateFilterCheck.addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    updateExepressionFilterTextAndLayout(true);
                    previousStateCheckFilter = activateFilterCheck.getSelection();
                }

            });
        }
        // /////////////////////////////////////////////////////////////////
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

    public boolean canBeResizedAtPreferedSize() {
        return !customSized;
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
        int heightOffset = 0;
        int tableEntryItemHeight = tableViewerCreatorForColumns.getTable().getItemHeight();
        // heightOffset += newTableEntryWillBeAdded ? tableEntryItemHeight : 0;
        heightOffset += newConstraintEntryWillBeAdded ? tableViewerCreatorForFilters.getTable().getItemHeight() : 0;

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
            newHeight = getMinimumHeight();
        }
        return new Point(this.getSize().x, newHeight);
    }

    private int getMinimumHeight() {
        if (WindowSystem.isGTK()) {
            if (hasDropDownToolBarItem()) {
                return 38;
            } else {
                return 28;
            }
        } else {
            return 24;
        }
    }

    public void registerStyledExpressionEditor(final StyledTextHandler styledTextHandler) {
        if (columnExpressionTextEditor != null) {
            attachCellExpressionToStyledTextEditor(tableViewerCreatorForColumns, columnExpressionTextEditor, styledTextHandler);
        }
        if (constraintExpressionTextEditor != null) {
            attachCellExpressionToStyledTextEditor(tableViewerCreatorForFilters, constraintExpressionTextEditor,
                    styledTextHandler);
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

            public void modifyText(ModifyEvent e) {
                ITableEntry currentModifiedEntry = (ITableEntry) tableViewerCreator.getModifiedObjectInfo()
                        .getCurrentModifiedBean();
                styledTextHandler.setCurrentEntry(currentModifiedEntry);
                Text text = (Text) e.widget;
                // if (Math.abs(text.getText().length() - styledTextHandler.getStyledText().getText().length()) > 1) {
                styledTextHandler.setTextWithoutNotifyListeners(text.getText());
                // highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
                // }
            }

        });

        expressionTextEditor.addKeyListener(new TextCellEditorToMapperStyledTextKeyListener(expressionTextEditor,
                styledTextHandler));

        expressionTextEditor.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
                highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
            }

            public void mouseDown(MouseEvent e) {
                highlightLineAndSetSelectionOfStyledText(expressionTextEditor);
            }

            public void mouseUp(MouseEvent e) {

            }

        });

    }

    private void highlightLineAndSetSelectionOfStyledText(final Text expressionTextEditor) {
        highlightLineAndSetSelectionOfStyledTextFromTextControl(expressionTextEditor);

    }

    /**
     * DOC amaumont Comment method "highlightLineAndSetSelectionOfStyledText".
     *
     * @param expressionTextEditor
     */
    protected void highlightLineAndSetSelectionOfStyledTextFromTextControl(final Control textWidget) {
        final StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();

        Runnable runnable = new Runnable() {

            public void run() {

                StyledText styledText = styledTextHandler.getStyledText();
                if (styledText.isDisposed()) {
                    return;
                }

                String text = ControlUtils.getText(textWidget);
                Point selection = ControlUtils.getSelection(textWidget);
                String lineDelimiter = ControlUtils.getLineDelimiter(textWidget);
                if (selection.x - 1 > 0) {
                    while (lineDelimiter.equals(text.charAt(selection.x - 1))) {
                        selection.x++;
                    }
                }
                if (selection.y - 1 > 0) {
                    while (lineDelimiter.equals(text.charAt(selection.y - 1))) {
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
    protected void initExpressionProposals(final ExtendedTextCellEditorWithProposal cellEditor, Zone[] zones,
            final TableViewerCreator tableViewerCreator, ITableEntry currentModifiedEntry) {
        if (this.expressionProposalProvider == null) {
            this.expressionProposalProvider = createExpressionProposalProvider();
        }
        this.expressionProposalProvider.init(abstractDataMapTable, zones, currentModifiedEntry);
        cellEditor.setContentProposalProvider(this.expressionProposalProvider);
        cellEditor.setData(expressionProposalProvider.getVariables());

        StringBuffer id = new StringBuffer();

        id.append(mapperManager.getAbstractMapComponent().getUniqueName() + "=>"); //$NON-NLS-1$

        // TDI-3065:Expression Builder for Var in tMap does not show Var name correctly
        // TableItem[] items = tableViewerCreator.getTable().getSelection();
        // if (items.length == 1) {
        // Object item = items[0].getData();
        // if (item instanceof AbstractInOutTableEntry) {
        // AbstractInOutTableEntry entry = (AbstractInOutTableEntry) item;
        //                id.append(entry.getParent().getName() + "=>");//$NON-NLS-1$
        // id.append(entry.getMetadataColumn().getLabel());
        // } else if (item instanceof VarTableEntry) {
        // VarTableEntry entry = (VarTableEntry) item;
        //                id.append(entry.getParent().getName() + "=>");//$NON-NLS-1$
        // id.append(entry.getName());
        // }

        // }

        cellEditor.setOwnerId(id.toString());
        if (!needInitProposals) {
            StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
            styledTextHandler.getStyledText().setEnabled(true);
            ContentProposalAdapterExtended expressionProposalStyledText = styledTextHandler.getContentProposalAdapter();
            expressionProposalStyledText.setContentProposalProvider(this.expressionProposalProvider);
        }
    }

    /**
     * DOC amaumont Comment method "initExpressionProposalProvider".
     */
    protected ExpressionProposalProvider createExpressionProposalProvider() {
        IContentProposalProvider[] contentProposalProviders = new IContentProposalProvider[0];
        if (!MapperMain.isStandAloneMode()) {
            contentProposalProviders = new IContentProposalProvider[] { new TalendProposalProvider(mapperManager
                    .getAbstractMapComponent().getProcess()) };
        }
        return new ExpressionProposalProvider(mapperManager, contentProposalProviders);
    }

    protected ExtendedTextCellEditor createExpressionCellEditor(final TableViewerCreator tableViewerCreator,
            TableViewerCreatorColumn column, final Zone[] zones, boolean isConstraintExpressionCellEditor) {
        IService expressionBuilderDialogService = GlobalServiceRegister.getDefault().getService(
                IExpressionBuilderDialogService.class);

        CellEditorDialogBehavior behavior = new CellEditorDialogBehavior();
        final ExtendedTextCellEditorWithProposal cellEditor = new ExtendedTextCellEditorWithProposal(
                tableViewerCreator.getTable(), SWT.MULTI | SWT.BORDER, column, behavior) {

            @Override
            public void activate() {

                // UIManager uiManager = mapperManager.getUiManager();
                //
                // ITableEntry currentModifiedBean = (ITableEntry) tableViewerCreator.getModifiedObjectInfo()
                // .getCurrentModifiedBean();
                //
                // ArrayList<ITableEntry> selectedTableEntry = new ArrayList<ITableEntry>(1);
                // selectedTableEntry.add(currentModifiedBean);
                //
                // uiManager.selectLinks(DataMapTableView.this, selectedTableEntry, true, false);
                //
                // uiManager.applyActivatedCellEditorsForAllTables(tableViewerCreator);

                super.activate();
            }

        };

        dialog = ((IExpressionBuilderDialogService) expressionBuilderDialogService).getExpressionBuilderInstance(
                tableViewerCreator.getCompositeParent(), cellEditor, mapperManager.getAbstractMapComponent());

        behavior.setCellEditorDialog(dialog);

        final Text expressionTextEditor = cellEditor.getTextControl();

        if (isConstraintExpressionCellEditor) {
            constraintExpressionTextEditor = expressionTextEditor;
        } else {
            columnExpressionTextEditor = expressionTextEditor;
        }

        cellEditor.addListener(new ICellEditorListener() {

            Text text = expressionTextEditor;

            public void applyEditorValue() {
                // System.out.println("applyEditorValue:text='" + text.getText() + "'");
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreator.getModifiedObjectInfo();
                mapperManager.getUiManager().parseNewExpression(text.getText(),
                        (ITableEntry) modifiedObjectInfo.getCurrentModifiedBean(), true);
                checkChangementsAfterEntryModifiedOrAdded(false);
            }

            public void cancelEditor() {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreator.getModifiedObjectInfo();
                text.setText((String) modifiedObjectInfo.getOriginalPropertyBeanValue());
                ITableEntry tableEntry = (ITableEntry) (modifiedObjectInfo.getCurrentModifiedBean() != null ? modifiedObjectInfo
                        .getCurrentModifiedBean() : modifiedObjectInfo.getPreviousModifiedBean());
                String originalExpression = (String) modifiedObjectInfo.getOriginalPropertyBeanValue();
                mapperManager.getUiManager().parseNewExpression(originalExpression, tableEntry, true);
            }

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

            ExecutionLimiterImproved executionLimiter = null;

            public void controlMoved(ControlEvent e) {
            }

            public void controlResized(ControlEvent e) {
                if (executionLimiter == null) {
                    executionLimiter = new ExecutionLimiterImproved(50, true) {

                        @Override
                        public void execute(boolean isFinalExecution, Object data) {

                            if (isFinalExecution) {
                                expressionTextEditor.getDisplay().syncExec(new Runnable() {

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

            public void focusGained(FocusEvent e) {
                // System.out.println("expressionTextEditor focusGained:Text.getText()='"+((Text) e.widget).getText() +
                // "'");
                ITableEntry currentModifiedEntry = (ITableEntry) tableViewerCreator.getModifiedObjectInfo()
                        .getCurrentModifiedBean();
                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                    if (currentModifiedEntry instanceof AbstractInOutTableEntry) {
                        IMetadataColumn column = ((AbstractInOutTableEntry) currentModifiedEntry).getMetadataColumn();
                        String typeToGenerate = JavaTypesManager.getTypeToGenerate(column.getTalendType(), column.isNullable());
                        cellEditor.setExpressionType(typeToGenerate);
                    } else if (currentModifiedEntry instanceof VarTableEntry) {
                        boolean nullable = ((VarTableEntry) currentModifiedEntry).isNullable();
                        String talendType = ((VarTableEntry) currentModifiedEntry).getType();
                        String typeToGenerate = JavaTypesManager.getTypeToGenerate(talendType, nullable);
                        cellEditor.setExpressionType(typeToGenerate);
                        // TDI-23838 : High CPU and hang when working on "Var" section on MacOS
                        if ("".equals(currentModifiedEntry.getExpression()) || currentModifiedEntry.getExpression() == null) {
                            needInitProposals = true;
                        }
                    }
                }

                initExpressionProposals(cellEditor, zones, tableViewerCreator, currentModifiedEntry);
                resizeTextEditor(expressionTextEditor, tableViewerCreator);

                StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
                if (styledTextHandler.getCurrentEntry() != currentModifiedEntry) {
                    styledTextHandler.setCurrentEntry(currentModifiedEntry);
                    styledTextHandler.setTextWithoutNotifyListeners(currentModifiedEntry.getExpression() == null ? "" : currentModifiedEntry.getExpression()); //$NON-NLS-1$
                }
            }

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

        int moreSpace = WindowSystem.isGTK() ? tableForConstraints.getItemHeight() : 0;
        tableForConstraintsGridData.heightHint = ((OutputTable) abstractDataMapTable).getFilterEntries().size()
                * tableForConstraints.getItemHeight() + tableForConstraints.getItemHeight() / 2 + moreSpace;

        if (WindowSystem.isGTK()) {
            tableViewerCreatorForFilters.layout();
        }

    }

    /**
     * DOC amaumont Comment method "updateGridDataHeightForTableConstraints".
     */
    public void updateGridDataHeightForTableGlobalMap() {
        int moreSpace = WindowSystem.isGTK() ? tableForGlobalMap.getItemHeight() : 0;
        int size = ((InputTable) abstractDataMapTable).getGlobalMapEntries().size();
        if (size < 3) {
            tableForGlobalMapGridData.heightHint = size
                    * (tableForGlobalMap.getItemHeight() + tableForGlobalMap.getItemHeight() / 2) + moreSpace;
        } else {
            tableForGlobalMapGridData.heightHint = size * (tableForGlobalMap.getItemHeight()) + moreSpace;
        }

        if (WindowSystem.isGTK() || WindowSystem.isOSX()) {
            tableViewerCreatorForGlobalMap.layout();
        }

    }

    /**
     * DOC amaumont Comment method "unselectAllEntries".
     */
    public void unselectAllEntries() {
        unselectAllColumnEntries();
        unselectAllConstraintEntries();
        unselectAllGlobalMapEntries();
    }

    /**
     * DOC amaumont Comment method "unselectAllConstraintEntries".
     */
    public void unselectAllConstraintEntries() {
        // nothing by default, override if necessary
    }

    /**
     * DOC amaumont Comment method "unselectAllGlobalMapEntries".
     */
    public void unselectAllGlobalMapEntries() {
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

    /**
     *
     * Provide a color provider for Constraints table and dataMap table.
     *
     *
     * <br/>
     *
     */
    class ExpressionColorProvider {

        /**
         * DOC amaumont Comment method "getBackgroundColor".
         *
         * @param expression
         */
        public Color getBackgroundColor(boolean validCell) {

            if (validCell) {
                return null;
            } else {
                return ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_ERROR_EXPRESSION_CELL);
            }

        }

        /**
         * DOC amaumont Comment method "getBackgroundColor".
         *
         * @param expression
         */
        public Color getForegroundColor(boolean validCell) {
            // if (validCell) {
            // return null;
            // } else {
            // return ColorProviderMapper.getColor(ColorInfo.COLOR_FOREGROUND_ERROR_EXPRESSION_CELL);
            // }

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
        ITableEntry entry = (ITableEntry) element;
        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) tableViewerCreator.getColumns()
                .get(columnIndex);
        if (column.getId().equals(ID_EXPRESSION_COLUMN)) {
            return expressionColorProvider.getBackgroundColor(entry.getProblems() == null ? true : false);
        } else if (column.getId().equals(PREVIEW_COLUMN)) {
            return ColorProviderMapper.getColor(ColorInfo.COLOR_TMAP_PREVIEW);
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
        ITableEntry entry = (ITableEntry) element;
        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) tableViewerCreator.getColumns()
                .get(columnIndex);
        if (column.getId().equals(ID_EXPRESSION_COLUMN)) {
            return expressionColorProvider.getForegroundColor(entry.getProblems() == null ? true : false);
        }
        return null;
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
                tableViewer = DataMapTableView.this.getTableViewerCreatorForFilters().getTableViewer();
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
    private void parseExpression(ModifiedBeanEvent event, TableViewerCreator tableViewerCreator, ITableEntry tableEntry) {
        if (event.column == tableViewerCreator.getColumn(DataMapTableView.ID_EXPRESSION_COLUMN)) {
            mapperManager.getUiManager().parseExpression(tableEntry.getExpression(), tableEntry, false, false, false);
            mapperManager.getUiManager().refreshBackground(false, false);
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
    public AbstractExtendedTableViewer<FilterTableEntry> getExtendedTableViewerForFilters() {
        return this.extendedTableViewerForFilters;
    }

    protected Composite getCenterComposite() {
        return this.centerComposite;
    }

    protected ExpressionProposalProvider getExpressionProposalProvider() {
        return this.expressionProposalProvider;
    }

    public int getHeaderHeight() {
        if (WindowSystem.isGTK()) {
            return HEADER_HEIGHT + (hasDropDownToolBarItem() ? 10 : 2);
        } else {
            return HEADER_HEIGHT + (hasDropDownToolBarItem() ? 8 : 0);
        }
    }

    public void checkChangementsAfterEntryModifiedOrAdded(boolean forceEvaluation) {

    }

    public Point getRealToolbarSize() {
        return realToolbarSize;
    }

    /**
     * DOC amaumont Comment method "createExpressionFilter".
     *
     * @param defaultValue TODO
     */
    protected void createExpressionFilter(final String defaultText) {
        if (mapperManager.isAdvancedMap() && getDataMapTable() instanceof AbstractInOutTable) {

            final AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();

            IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();

            expressionImageLabel = new Label(getCenterComposite(), SWT.NONE);
            expressionImageLabel.setImage(ImageProviderMapper.getImage(ImageInfo.ACTIVATE_FILTER_ICON));
            expressionImageLabel.setVisible(table.isActivateExpressionFilter());

            expressionFilterText = new UnnotifiableColorStyledText(getCenterComposite(), SWT.BORDER | SWT.V_SCROLL,
                    preferenceStore, LanguageManager.getCurrentLanguage().getName());
            // hywang add for 9225
            openExpressionBuilder = new Button(getCenterComposite(), SWT.PUSH);
            openExpressionBuilder.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
            openExpressionBuilder.setVisible(table.isActivateExpressionFilter());
            openExpressionBuilder.addSelectionListener(new SelectionListener() {

                IService expressionBuilderDialogService = GlobalServiceRegister.getDefault().getService(
                        IExpressionBuilderDialogService.class);

                IExpressionBuilderDialogController dialogForTable = ((IExpressionBuilderDialogService) expressionBuilderDialogService)
                        .getExpressionBuilderInstance(DataMapTableView.this.getCenterComposite(), null,
                                mapperManager.getAbstractMapComponent());

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    if (dialogForTable instanceof TrayDialog) {
                        TrayDialog parentDialog = (TrayDialog) dialogForTable;
                        dialogForTable.setDefaultExpression(expressionFilterText.getText());
                        if (Window.OK == parentDialog.open()) {
                            String expressionForTable = dialogForTable.getExpressionForTable();
                            ControlUtils.setText(expressionFilterText, expressionForTable);
                            if (isFilterEqualsToDefault(expressionForTable)) {
                                table.getExpressionFilter().setExpression(null);
                            } else {
                                table.getExpressionFilter().setExpression(expressionForTable);
                            }
                            checkProblemsForExpressionFilter(false, true);
                            ITableEntry currentExpressionFilterEntry = null;
                            StyledTextHandler textTarget = mapperManager.getUiManager().getTabFolderEditors()
                                    .getStyledTextHandler();
                            if (textTarget.getCurrentEntry() != null) {
                                currentExpressionFilterEntry = textTarget.getCurrentEntry();
                            } else {
                                currentExpressionFilterEntry = table.getExpressionFilter();
                            }
                            mapperManager.getUiManager().parseNewExpression(textTarget.getStyledText().getText(),
                                    currentExpressionFilterEntry, false);
                        }
                    }
                }

            });
            if (mapperManager.componentIsReadOnly()) {
                expressionFilterText.setEditable(false);
                openExpressionBuilder.setEnabled(false);
                expressionImageLabel.setEnabled(false);
            }
            GridData gridData1 = new GridData();
            gridData1.exclude = !table.isActivateExpressionFilter();
            openExpressionBuilder.setLayoutData(gridData1);

            GridData gridData2 = new GridData();
            gridData2.exclude = !table.isActivateExpressionFilter();
            expressionImageLabel.setLayoutData(gridData2);
            //

            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumHeight = 10;
            // gridData.grabExcessVerticalSpace = true;
            gridData.heightHint = 32;
            gridData.minimumWidth = 25;
            gridData.widthHint = 50;
            expressionFilterText.setLayoutData(gridData);

            String expressionFilter = table.getExpressionFilter().getExpression();
            if (expressionFilter != null && !"".equals(expressionFilter.trim())) { //$NON-NLS-1$
                expressionFilterText.setText(expressionFilter);
            } else {
                expressionFilterText.setText(defaultText);
            }

            new DragNDrop(mapperManager, expressionFilterText, false, true);

            expressionFilterText.setVisible(table.isActivateExpressionFilter());
            gridData.exclude = !table.isActivateExpressionFilter();

            expressionFilterText.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent e) {
                    redrawExpressionFilter();
                    Control text = (Control) e.getSource();
                    if (defaultText.equals(ControlUtils.getText(text))) {
                        ControlUtils.setText(text, ""); //$NON-NLS-1$
                    }

                    ExpressionFilterEntry currentExpressionFilterEntry = table.getExpressionFilter();
                    // currentExpressionFilterEntry.setExpression(ControlUtils.getText(text));

                    mapperManager.getUiManager().selectLinks(DataMapTableView.this,
                            Arrays.<ITableEntry> asList(currentExpressionFilterEntry), true, false);
                    if (!mapperManager.isSearchOption()) {
                        expressionFilterText.setBackground(null);
                        expressionFilterText.setForeground(null);
                    }
                    StyledTextHandler styledTextHandler = mapperManager.getUiManager().getTabFolderEditors()
                            .getStyledTextHandler();
                    styledTextHandler.setCurrentEntry(currentExpressionFilterEntry);
                    previousTextForExpressionFilter = currentExpressionFilterEntry.getExpression() == null ? "" : currentExpressionFilterEntry.getExpression(); //$NON-NLS-1$
                    styledTextHandler.getStyledText().setText(previousTextForExpressionFilter);
                    expressionFilterText.setToolTipText(null);
                    styledTextHandler.getStyledText().setEnabled(true);
                    styledTextHandler.getStyledText().setEditable(true);
                    expressionProposalStyledText = styledTextHandler.getContentProposalAdapter();
                    expressionProposalProviderForExpressionFilter.init(table, getValidZonesForExpressionFilterField(),
                            table.getExpressionFilter());
                    if (expressionProposalStyledText != null) {
                        expressionProposalStyledText.setContentProposalProvider(expressionProposalProviderForExpressionFilter);
                    }
                    mapperManager.getUiManager().selectLinks(DataMapTableView.this,
                            Arrays.<ITableEntry> asList(currentExpressionFilterEntry), true, false);
                    colorExpressionFilterFromProblems(table, false);
                }

                public void focusLost(FocusEvent e) {
                    Control text = (Control) e.getSource();
                    if ("".equals(ControlUtils.getText(text).trim())) { //$NON-NLS-1$
                        ControlUtils.setText(text, defaultText);
                    }
                    setExpressionFilterFromStyledText(table, text);
                    checkProblemsForExpressionFilter(false, true);
                }

            });

            Listener showTooltipErrorListener = new Listener() {

                public void handleEvent(Event event) {

                    switch (event.type) {
                    case SWT.MouseMove:
                        if (table.getExpressionFilter().getProblems() != null && !expressionFilterText.isFocusControl()) {
                            String tooltip = createErrorContentForTooltip(table.getExpressionFilter().getProblems());
                            expressionFilterText.setToolTipText(tooltip);
                        } else {
                            expressionFilterText.setToolTipText(null);
                        }

                        break;
                    default:
                    }
                }

            };
            expressionFilterText.addListener(SWT.MouseMove, showTooltipErrorListener);

            if (executionLimiterForExpressionFilterSetText == null) {
                executionLimiterForExpressionFilterSetText = new ExecutionLimiterImproved(50, true) {

                    @Override
                    public void execute(boolean isFinalExecution, Object data) {

                        if (isFinalExecution) {
                            expressionFilterText.getDisplay().syncExec(new Runnable() {

                                public void run() {
                                    if (expressionFilterText.isDisposed()) {
                                        return;
                                    }
                                    // to correct bug of CR when content
                                    // is multiline
                                    if (expressionFilterText.getText() != null) {
                                        expressionFilterText.setText(expressionFilterText.getText());
                                    }
                                }

                            });
                        }
                    }

                };
            }

            expressionFilterText.addControlListener(new ControlListener() {

                public void controlMoved(ControlEvent e) {
                    redrawExpressionFilter();
                }

                public void controlResized(ControlEvent e) {
                    redrawExpressionFilter();
                }

            });

            ControlListener listenerForCorrectWrapBug = new ControlListener() {

                public void controlMoved(ControlEvent e) {
                    redrawExpressionFilter();
                }

                public void controlResized(ControlEvent e) {
                    correctAsynchStyledTextWrapBug();
                }
            };

            ScrolledComposite scrolledCompositeView = null;
            if (getZone() == Zone.INPUTS) {
                scrolledCompositeView = getMapperManager().getUiManager().getScrolledCompositeViewInputs();
            } else if (getZone() == Zone.OUTPUTS) {
                scrolledCompositeView = getMapperManager().getUiManager().getScrolledCompositeViewOutputs();
            }
            scrolledCompositeView.addControlListener(listenerForCorrectWrapBug);

            SelectionListener selectionListenerToCorrectWrapBug = new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    redrawExpressionFilter();
                }

            };

            scrolledCompositeView.getVerticalBar().addSelectionListener(selectionListenerToCorrectWrapBug);

            expressionFilterText.getVerticalBar().addSelectionListener(selectionListenerToCorrectWrapBug);

            // expressionFilterText.addExtendedModifyListener(new
            // ExtendedModifyListener() {
            //
            // public void modifyText(ExtendedModifyEvent event) {
            // StyledTextHandler styledTextHandler =
            // mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler();
            // styledTextHandler.getStyledText().replaceTextRange(event.start,
            // event.length, event.replacedText);
            // }
            //
            // });

            ExpressionEditorToMapperStyledTextKeyListener keyAndModifyListener = new ExpressionEditorToMapperStyledTextKeyListener(
                    expressionFilterText, mapperManager.getUiManager().getTabFolderEditors().getStyledTextHandler());
            expressionFilterText.addExtendedModifyListener(keyAndModifyListener);
            expressionFilterText.addKeyListener(keyAndModifyListener);

            // expressionFilterText.addKeyListener(new KeyListener() {
            //
            // /*
            // * (non-Javadoc)
            // *
            // * @see
            // org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
            // */
            // public void keyPressed(KeyEvent e) {
            //
            // Control text = (Control) e.getSource();
            // setExpressionFilterFromStyledText(table, text);
            // StyledTextHandler styledTextHandler =
            // mapperManager.getUiManager().getTabFolderEditors()
            // .getStyledTextHandler();
            // styledTextHandler.getStyledText().setText(expressionFilterText.getText());
            //
            // }
            //
            // public void keyReleased(KeyEvent e) {
            //
            // mapperManager.getUiManager().parseNewExpression(expressionFilterText.getText(),
            // table.getExpressionFilter(), false);
            //
            // }
            // });

        }
    }

    protected void createColumnNameFilter() {
        if (mapperManager.isAdvancedMap() && getDataMapTable() instanceof AbstractInOutTable) {
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
                    if (metadataColumn != null && (!matcher.matches(metadataColumn.getLabel()))) {
                        return false;

                    }

                }

            }
            return true;
        }

    }

    /**
     * DOC amaumont Comment method "onExpressionFilterTextResized".
     */
    private void redrawExpressionFilter() {
        // System.out.println("Filter text resized" +
        // System.currentTimeMillis());
        if (!expressionFilterText.isDisposed()) {
            expressionFilterText.redraw();
        }
    }

    private void redrawColumnNameFilter() {
        if (!columnNameTextFilter.isDisposed()) {
            columnNameTextFilter.redraw();
        }
    }

    /**
     * DOC amaumont Comment method "checkProblemsForExpressionFilter".
     */
    public void checkProblemsForExpressionFilterWithDelay() {
        if (this.executionLimiterForCheckProblemsExpressionFilter == null) {

            this.executionLimiterForCheckProblemsExpressionFilter = new ExecutionLimiterImproved(2000, true) {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.talend.commons.utils.threading.ExecutionLimiter#execute(boolean)
                 */
                @Override
                protected void execute(boolean isFinalExecution, Object data) {
                    if (!expressionFilterText.isDisposed()) {
                        expressionFilterText.getDisplay().asyncExec(new Runnable() {

                            public void run() {
                                checkProblemsForExpressionFilter(true, true);
                            }

                        });
                    }

                }

            };
        }
        executionLimiterForCheckProblemsExpressionFilter.resetTimer();
        executionLimiterForCheckProblemsExpressionFilter.startIfExecutable(true, null);

    }

    public void checkProblemsForExpressionFilter(boolean forceRecompile, boolean colorAllowed) {
        if (this.getDataMapTable() instanceof AbstractInOutTable) {
            AbstractInOutTable table = (AbstractInOutTable) this.getDataMapTable();
            if (table.isActivateExpressionFilter() && !isFilterEqualsToDefault(expressionFilterText.getText())) {
                String nextText = expressionFilterText.getText();
                if (nextText != null && previousTextForExpressionFilter != null
                        && !nextText.trim().equals(previousTextForExpressionFilter.trim()) || forceRecompile) {
                    mapperManager.getProblemsManager().checkProblemsForTableEntry(table.getExpressionFilter(), true);
                } else {
                    mapperManager.getProblemsManager().checkProblemsForTableEntry(table.getExpressionFilter(), false);
                }
            } else {
                table.getExpressionFilter().setProblems(null);
            }
            colorExpressionFilterFromProblems(table, colorAllowed);

        }

    }

    /**
     * DOC amaumont Comment method "colorExpressionFilterFromProblems".
     *
     * @param table
     * @param colorAllowed TODO
     */
    private void colorExpressionFilterFromProblems(AbstractInOutTable table, boolean colorAllowed) {
        List<Problem> problems = table.getExpressionFilter().getProblems();
        if (problems != null && colorAllowed) {
            expressionFilterText.setColoring(false);
            if (!mapperManager.isSearchOption()) {
                expressionFilterText
                        .setBackground(ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_ERROR_EXPRESSION_CELL));
                expressionFilterText
                        .setForeground(ColorProviderMapper.getColor(ColorInfo.COLOR_FOREGROUND_ERROR_EXPRESSION_CELL));
            }
        } else {
            expressionFilterText.setColoring(true);
            if (!mapperManager.isSearchOption()) {
                expressionFilterText
                        .setBackground(ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_VALID_EXPRESSION_CELL));
                expressionFilterText
                        .setForeground(ColorProviderMapper.getColor(ColorInfo.COLOR_FOREGROUND_VALID_EXPRESSION_CELL));
            }
        }
    }

    /**
     * DOC amaumont Comment method "registerProposalForExpressionFilter".
     */
    public void configureExpressionFilter() {
        if (mapperManager.isAdvancedMap() && getDataMapTable() instanceof AbstractInOutTable) {
            final AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();
            if (this.expressionProposalProviderForExpressionFilter == null) {
                this.expressionProposalProviderForExpressionFilter = createExpressionProposalProvider();
            }
            expressionProposalProviderForExpressionFilter.init(table, getValidZonesForExpressionFilterField(),
                    table.getExpressionFilter());
            table.getExpressionFilter().setName(EXPRESSION_FILTER_ENTRY);
            this.proposalForExpressionFilterText = ProposalUtils.getCommonProposal(expressionFilterText,
                    expressionProposalProviderForExpressionFilter);
            this.proposalForExpressionFilterText.addContentProposalListener(new IContentProposalListener() {

                public void proposalAccepted(IContentProposal proposal) {
                    new AsynchronousThreading(50, false, expressionFilterText.getDisplay(), new Runnable() {

                        /*
                         * (non-Javadoc)
                         *
                         * @see java.lang.Runnable#run()
                         */
                        public void run() {
                            if (!expressionFilterText.isDisposed()) {
                                mapperManager.getUiManager().parseNewExpression(expressionFilterText.getText(),
                                        table.getExpressionFilter(), false);
                            }
                        }

                    }).start();
                }

            });
            checkProblemsForExpressionFilter(false, true);
        }
    }

    /**
     * DOC amaumont Comment method "getValidZonesForExpressionFilterField".
     *
     * @return
     */
    protected abstract Zone[] getValidZonesForExpressionFilterField();

    /**
     * DOC amaumont Comment method "createErrorContentForTooltip".
     *
     * @param problems
     * @return
     */
    private String createErrorContentForTooltip(List<Problem> problems) {
        String toolTip;
        toolTip = ""; //$NON-NLS-1$
        for (Problem problem : problems) {
            String description = problem.getDescription().replaceAll("[\r\n\t]", ""); //$NON-NLS-1$ //$NON-NLS-2$
            toolTip += description + "\n"; //$NON-NLS-1$
        }
        return toolTip;
    }

    /**
     * This method must be called when all widgets has been created.
     */
    public void loaded() {

    }

    /**
     * Getter for expressionFilterText.
     *
     * @return the expressionFilterText
     */
    public UnnotifiableColorStyledText getExpressionFilterText() {
        return this.expressionFilterText;
    }

    public Text getColumnNameFilterText() {
        return this.columnNameTextFilter;
    }

    /**
     * DOC amaumont Comment method "correctAsynchStyledTextWrapBug".
     */
    private void correctAsynchStyledTextWrapBug() {
        if (!expressionFilterText.isDisposed()) {
            new AsynchronousThreading(100, false, expressionFilterText.getDisplay(), new Runnable() {

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    // System.out.println("scrolledCompositeView.addControlListener(new
                    // ControlListener()"
                    // + System.currentTimeMillis());
                    redrawExpressionFilter();
                }

            }).start();
        }
    }

    /**
     * DOC amaumont Comment method "setExpressionFilterFromStyledText".
     *
     * @param table
     * @param text
     */
    public void setExpressionFilterFromStyledText(final AbstractInOutTable table, Control text) {
        String currentContent = ControlUtils.getText(text);
        if (isFilterEqualsToDefault(currentContent)) {
            table.getExpressionFilter().setExpression(null);
        } else {
            table.getExpressionFilter().setExpression(currentContent);
        }
    }

    /**
     * Check expression errors, show or hide text widget.
     *
     * @param buttonPressed TODO
     * @param activateFilterCheck
     */
    protected void updateExepressionFilterTextAndLayout(boolean buttonPressed) {
        final AbstractInOutTable table = (AbstractInOutTable) getDataMapTable();

        GridData gridData = (GridData) expressionFilterText.getLayoutData();

        GridData gridData2 = (GridData) openExpressionBuilder.getLayoutData();

        GridData gridData3 = (GridData) expressionImageLabel.getLayoutData();

        if (activateFilterCheck.getSelection()) {
            expressionFilterText.setVisible(true);
            gridData.exclude = false;
            table.setActivateExpressionFilter(true);

            // hywang add
            openExpressionBuilder.setVisible(true);
            gridData2.exclude = false;

            expressionImageLabel.setVisible(true);
            gridData3.exclude = false;

            mapperManager.getUiManager().parseExpression(expressionFilterText.getText(), table.getExpressionFilter(), false,
                    false, false);
        } else {
            expressionFilterText.setVisible(false);
            gridData.exclude = true;
            table.setActivateExpressionFilter(false);
            // hywang add
            openExpressionBuilder.setVisible(false);
            gridData2.exclude = true;

            expressionImageLabel.setVisible(false);
            gridData3.exclude = true;

            mapperManager.removeTableEntry(table.getExpressionFilter());
        }
        // updateGridDataHeightForTableConstraints();
        if (buttonPressed) {
            DataMapTableView.this.changeSize(DataMapTableView.this.getPreferredSize(false, true, false), true, true);
        }
        DataMapTableView.this.layout();

        mapperManager.getUiManager().refreshBackground(true, false);

        if (expressionFilterText.isVisible() && buttonPressed) {
            new AsynchronousThreading(50, false, mapperManager.getUiManager().getDisplay(), new Runnable() {

                public void run() {
                    TimeMeasure.begin(Messages.getString("DataMapTableView.checkProblem")); //$NON-NLS-1$
                    checkProblemsForExpressionFilter(expressionFilterText.isFocusControl(), false);
                    TimeMeasure.end(Messages.getString("DataMapTableView.checkProblem")); //$NON-NLS-1$
                }

            }).start();

            expressionFilterText.setFocus();
            if (DataMapTableView.this.getZone() == Zone.INPUTS) {
                ScrolledComposite scrolledZoneView = mapperManager.getUiManager().getScrolledCompositeViewInputs();
                Point point = expressionFilterText.getDisplay().map(expressionFilterText,
                        mapperManager.getUiManager().getTablesZoneViewInputs(), new Point(0, 0));
                mapperManager.getUiManager().setPositionOfVerticalScrollBarZone(scrolledZoneView, point.y - 20);
            }
        }

        correctAsynchStyledTextWrapBug();
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

        notifyFocusLost();
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
                            String[] expressions = outputEntry.getExpression().split("\\s+");
                            for (String expression : expressions) {
                                mapperManager.getUiManager().parseNewFilterColumn(expression, outputEntry, false);
                                mapperManager.getUiManager().refreshBackground(false, false);
                            }
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
                            String[] expressions = textTarget.getStyledText().getText().split("\\s+");
                            for (String expression : expressions) {
                                mapperManager.getUiManager().parseNewFilterColumn(expression, textTarget.getCurrentEntry(),
                                        false);
                                mapperManager.getUiManager().refreshBackground(false, false);
                            }
                        }
                        mapperManager.getUiManager().refreshBackground(true, false);
                    }
                }
                resizeAtExpandedSize();
            }
        }

    }

    /**
     *
     * DOC amaumont InputDataMapTableView class global comment. Detailled comment <br/>
     *
     */
    class ExpressionEditorToMapperStyledTextKeyListener implements ExtendedModifyListener, KeyListener {

        private final Control textWidget;

        private final StyledTextHandler textTarget;

        private boolean modifyListenerAllowed;

        /**
         * DOC amaumont TextKeyListener constructor comment.
         */
        public ExpressionEditorToMapperStyledTextKeyListener(StyledText textWidgetSrc, StyledTextHandler textTarget) {
            super();
            this.textWidget = textWidgetSrc;
            this.textTarget = textTarget;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {

        }

        public void keyReleased(KeyEvent e) {
            // highlightLineOfCursorPosition();
            mapperManager.getUiManager().parseNewExpression(textTarget.getStyledText().getText(), textTarget.getCurrentEntry(),
                    false);

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.swt.custom.ExtendedModifyEvent)
         */
        public void modifyText(ExtendedModifyEvent event) {
            // if (modifyListenerAllowed) {
            if (isFilterEqualsToDefault(ControlUtils.getText(textWidget))) {
                textTarget.setTextWithoutNotifyListeners(""); //$NON-NLS-1$
            } else {
                textTarget.setTextWithoutNotifyListeners(ControlUtils.getText(textWidget));
            }
            highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
            // }
        }

    }

    public boolean isFilterEqualsToDefault(String value) {
        if (DEFAULT_POST_MATCHING_EXPRESSION_FILTER.equals(value) || DEFAULT_OUT_EXPRESSION_FILTER.equals(value)
                || DEFAULT_EXPRESSION_FILTER.equals(value)) {
            return true;
        }
        return false;
    }

    /**
     *
     * DOC amaumont InputDataMapTableView class global comment. Detailled comment <br/>
     *
     */
    class TextCellEditorToMapperStyledTextKeyListener implements KeyListener {

        private final Control textWidget;

        private final StyledTextHandler textTarget;

        /**
         * DOC amaumont TextKeyListener constructor comment.
         */
        public TextCellEditorToMapperStyledTextKeyListener(Text textWidgetSrc, StyledTextHandler textTarget) {
            super();
            this.textWidget = textWidgetSrc;
            this.textTarget = textTarget;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {
            // System.out.println("e.character=" + e.character);
            // System.out.println("keyCode=" + e.keyCode);
            // TUP-29766
            if (WindowSystem.isBigSurOrLater() && e.character == '\t') {
                ITableEntry currentModifiedEntry = textTarget.getCurrentEntry();
                currentModifiedEntry.setExpression(ControlUtils.getText(textWidget));
                viewer.getTable().deselectAll();
                viewer.refresh(currentModifiedEntry);
                return;
            }

            boolean ctrl = (e.stateMask & SWT.CTRL) != 0;
            boolean altgr = (e.stateMask & SWT.ALT) != 0;
            if (e.character == '\0' || ctrl && !altgr) {
                highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
            } else {
                UnnotifiableColorStyledText mapperColorStyledText = (UnnotifiableColorStyledText) textTarget.getStyledText();
                Point selection = ControlUtils.getSelection(textWidget);
                if (e.character == '\r' || e.character == '\u001b') {
                    e.doit = false;
                    textTarget.setTextWithoutNotifyListeners(ControlUtils.getText(textWidget));
                    highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
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
                            highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
                        }
                    } else {
                        // System.out.println("selection.x="+selection.x);
                        // System.out.println("selection.y="+selection.y);
                        // System.out.println("mapperColorStyledText.getText()="+mapperColorStyledText.getText().length()
                        // );
                        if (selection.y <= mapperColorStyledText.getCharCount()) {
                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, selection.y - selection.x,
                                    String.valueOf(e.character));
                        }
                        highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
                    }
                }

            }
        }

        public void keyReleased(KeyEvent e) {
            // highlightLineOfCursorPosition();
        }

    }

    /**
     * Getter for activateFilterCheck.
     *
     * @return the activateFilterCheck
     */
    protected ToolItem getActivateFilterCheck() {
        return this.activateFilterCheck;
    }

    /**
     * Getter for previousStateCheckFilter.
     *
     * @return the previousStateCheckFilter
     */
    protected boolean isPreviousStateCheckFilter() {
        return this.previousStateCheckFilter;
    }

    /**
     * Sets the previousStateCheckFilter.
     *
     * @param previousStateCheckFilter the previousStateCheckFilter to set
     */
    protected void setPreviousStateCheckFilter(boolean previousStateCheckFilter) {
        this.previousStateCheckFilter = previousStateCheckFilter;
    }

    protected void refreshCondensedImage(AbstractInOutTable table, String option) {
    }

    /**
     * DOC ycbai DataMapTableView class global comment. Detailled comment
     */
    class CustomDialogCellEditor extends DialogCellEditor {

        private CellValueType type;

        private final Map<String, String> oldMappingMap = new HashMap<String, String>();

        public CustomDialogCellEditor() {
            type = CellValueType.BOOL;
        }

        public CustomDialogCellEditor(CellValueType type) {
            this.type = type;
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            Shell shell = cellEditorWindow.getShell();
            if (type == CellValueType.BOOL) {
                ListStringValueDialog<String> dialog = new ListStringValueDialog<String>(shell, new String[] { "true", "false" }); //$NON-NLS-1$ //$NON-NLS-2$
                if (dialog.open() == IDialogConstants.OK_ID) {
                    return dialog.getSelectStr();
                }
            } else if (type == CellValueType.SCHEMA_TYPE) {
                ListStringValueDialog<String> dialog = new ListStringValueDialog<String>(shell, new String[] { BUILT_IN,
                        REPOSITORY });
                if (dialog.open() == IDialogConstants.OK_ID) {
                    return dialog.getSelectStr();
                }
            } else if (type == CellValueType.SCHEMA_ID) {
                RepositoryReviewDialog dialog = new RepositoryReviewDialog(shell, ERepositoryObjectType.METADATA_CON_TABLE);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    RepositoryNode node = dialog.getResult();
                    while (node.getObject().getProperty().getItem() == null
                            || (!(node.getObject().getProperty().getItem() instanceof ConnectionItem))) {
                        node = node.getParent();
                    }
                    String id = dialog.getResult().getObject().getProperty().getId();
                    String name = dialog.getResult().getObject().getLabel();
                    String value = id + " - " + name; //$NON-NLS-1$
                    IMetadataTable repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(value);
                    List<IMetadataColumn> columns = repositoryMetadata.getListColumns();
                    if (columns != null) {
                        MetadataTableEditorView metadataEditorView = mapperManager.getUiManager()
                                .getMetadataEditorView(getZone());
                        ExtendedTableModel<IMetadataColumn> extendedTableModel = metadataEditorView.getExtendedTableModel();
                        List<IMetadataColumn> copyedAllList = new ArrayList<IMetadataColumn>(extendedTableModel.getBeansList());

                        if (abstractDataMapTable instanceof OutputTable) {
                            OutputTable outputTable = (OutputTable) abstractDataMapTable;
                            List<IColumnEntry> oldOuputEntries = outputTable.getDataMapTableEntries();
                            if (oldOuputEntries != null) {
                                for (IColumnEntry entry : oldOuputEntries) {
                                    if (entry instanceof OutputColumnTableEntry) {
                                        OutputColumnTableEntry outputEntry = (OutputColumnTableEntry) entry;
                                        String expression = outputEntry.getExpression();
                                        String columnname = outputEntry.getName();
                                        if (expression != null) {
                                            oldMappingMap.put(columnname, expression);
                                        }
                                    }
                                }
                            }
                        }

                        // handle related table, save original expression
                        List<DataMapTableView> relatedOutputsTableView = mapperManager.getUiManager().getRelatedOutputsTableView(
                                DataMapTableView.this);
                        for (IMetadataColumn metadataColumn : columns) {
                            for (DataMapTableView tableView : relatedOutputsTableView) {
                                IDataMapTable retrieveAbstractDataMapTable = mapperManager
                                        .retrieveAbstractDataMapTable(tableView);
                                String tableName = retrieveAbstractDataMapTable.getName();
                                ITableEntry metadataTableEntry = mapperManager.retrieveTableEntry(new TableEntryLocation(
                                        tableName, metadataColumn.getLabel()));
                                mapperManager.getUiManager().getOldMappingMap()
                                        .put(tableName + "_" + metadataTableEntry.getName(), metadataTableEntry.getExpression());
                            }
                        }

                        // avoid UIManager#handleEvent execute afterOperationListener
                        // resize fire focuseLost to applyEditorValue remove the columnViewEditorListener
                        // after extendedTableModel remove/add reset back the original customSized
                        boolean isCustom = DataMapTableView.this.customSized;
                        try {
                            DataMapTableView.this.customSized = true;
                            extendedTableModel.removeAll(copyedAllList);
                            for (IMetadataColumn metaColumnToAdd : columns) {
                                String label = metaColumnToAdd.getLabel();
                                String expression = oldMappingMap.get(label);
                                if (expression != null && !"".equals(expression)) {
                                    metaColumnToAdd.setExpression(expression);
                                }
                            }
                            extendedTableModel.addAll(columns);
                            mapperManager.getUiManager().parseAllExpressionsForAllTables();
                            mapperManager.getUiManager().getOldMappingMap().clear();
                            oldMappingMap.clear();
                        } finally {
                            DataMapTableView.this.customSized = isCustom;
                        }
                        return value;
                    }
                }
            }

            return null;
        }

        /**
         * Sets the type.
         *
         * @param type the type to set
         */
        public void setType(CellValueType type) {
            this.type = type;
        }

        /**
         * Getter for type.
         *
         * @return the type
         */
        public CellValueType getType() {
            return this.type;
        }

    }

    public abstract void notifyFocusLost();

    public void propertyChange(PropertyChangeEvent evt) {
        notifyFocusLost();
        String request = evt.getPropertyName();
        if (request.equals("positionChange") || request.equals(ConnectionTrace.TRACE_PROP)) { //$NON-NLS-1$
            if (!tableViewerCreatorForColumns.getTable().isDisposed()) {
                tableViewerCreatorForColumns.refresh();
                InputsZone inputsZone = mapperManager.getUiManager().getInputsZone();
                if (inputsZone != null && !inputsZone.isDisposed() && inputsZone.getToolbar() != null) {
                    inputsZone.getToolbar().refreshCurrentRow();
                }
            }
        }
    }

    /**
     * DOC ycbai Comment method "getSchemaDisplayName".
     *
     * @param id
     * @return
     */
    protected String getSchemaDisplayName(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        String[] values = id.split(" - ");
        String itemId = values[0];
        String name = values[1];

        try {
            IProxyRepositoryFactory factory = ((IProxyRepositoryService) GlobalServiceRegister.getDefault().getService(
                    IProxyRepositoryService.class)).getProxyRepositoryFactory();
            IRepositoryViewObject object = factory.getLastVersion(itemId);
            if (object == null) {
                return null;
            }
            Item item = object.getProperty().getItem();
            if (item instanceof ConnectionItem) {
                ConnectionItem lastItemUsed = (ConnectionItem) item;
                return DesignerUtilities.getRepositoryAliasName(lastItemUsed) + ":" //$NON-NLS-1$
                        + lastItemUsed.getProperty().getLabel() + " - " + name;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        return null;
    }

    /**
     * DOC ycbai DataMapTableView class global comment. Detailled comment
     */
    class TableCellModifier extends DefaultCellModifier {

        /**
         * DOC talend TableCellModifier constructor comment.
         *
         * @param tableViewerCreator
         */
        public TableCellModifier(TableViewerCreator tableViewerCreator) {
            super(tableViewerCreator);
        }

        @Override
        public boolean canModify(Object bean, String idColumn) {
            if (bean instanceof InputColumnTableEntry) {
                InputColumnTableEntry column = (InputColumnTableEntry) bean;
                if (column.getParent() instanceof InputTable) {
                    boolean isMainConnection = ((InputTable) getDataMapTable()).isMainConnection();
                    if (!isMainConnection) {
                        TableViewerCreatorNotModifiable creator = getTableViewerCreator();
                        TableViewerCreatorColumnNotModifiable operatorColumn = creator.getColumn(ID_OPERATOR);
                        if (operatorColumn != null) {
                            if (StringUtils.trimToNull(column.getExpression()) == null) {
                                operatorColumn.setModifiable(false);
                            } else {
                                operatorColumn.setModifiable(true);
                            }
                        }
                    }
                }
            }

            return super.canModify(bean, idColumn);
        }

    }

    /**
     * Get unique name with the given name.
     *
     * @param titleName
     */
    public abstract String findUniqueName(String baseName);
}
