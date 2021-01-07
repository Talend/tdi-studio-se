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
package org.talend.designer.mapper.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.drawing.background.BackgroundRefresher;
import org.talend.commons.ui.swt.linking.BgDrawableComposite;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.commons.utils.threading.ExecutionLimiterImproved;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.IConnection;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.listener.DropTargetOperationListener;
import org.talend.designer.abstractmap.ui.listener.MouseScrolledListener;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.mapper.MapperComponent;
import org.talend.designer.mapper.external.data.ExternalMapperUiProperties;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.MapperModel;
import org.talend.designer.mapper.model.table.AbstractDataMapTable;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.ui.color.ColorInfo;
import org.talend.designer.mapper.ui.color.ColorProviderMapper;
import org.talend.designer.mapper.ui.dnd.DragNDrop;
import org.talend.designer.mapper.ui.event.MouseMoveScrollZoneHelper;
import org.talend.designer.mapper.ui.font.FontProviderMapper;
import org.talend.designer.mapper.ui.footer.FooterComposite;
import org.talend.designer.mapper.ui.footer.StatusBar;
import org.talend.designer.mapper.ui.image.ImageProviderMapper;
import org.talend.designer.mapper.ui.tabs.TabFolderEditors;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.OutputDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.VarsDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.InputsZone;
import org.talend.designer.mapper.ui.visualmap.zone.OutputsZone;
import org.talend.designer.mapper.ui.visualmap.zone.SearchZone;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.InputTablesZoneView;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.OutputTablesZoneView;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.VarsTableZoneView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MapperUI {

    /**
     * Default value for middle performance in ms.
     */
    public static final int DEFAULT_TIME_BEFORE_NEW_BG_REFRESH = 100;

    private SashForm datasFlowViewSashForm;

    private InputTablesZoneView inputTablesZoneView;

    private VarsTableZoneView varsTableZoneView;

    private OutputTablesZoneView outputTablesZoneView;

    private ScrolledComposite sc1;

    private ScrolledComposite sc2;

    private ScrolledComposite sc3;

    private MouseScrolledListener sc1MSListener;

    private MouseScrolledListener sc2MSListener;

    private MouseScrolledListener sc3MSListener;

    private SashForm mainSashForm;

    private TabFolderEditors tabFolderEditors;

    protected Image bgImage1;

    protected Image bgImage2;

    private Color bgColorLinksZone;

    private ScrollBar vBar1;

    private ScrollBar vBar2;

    private ScrollBar vBar3;

    private ExecutionLimiterImproved backgroundRefreshLimiter;

    private ExecutionLimiterImproved backgroundRefreshLimiterForceRecalculate;

    private int backgroundRefreshTimeForScrolling = DEFAULT_TIME_BEFORE_NEW_BG_REFRESH;

    protected long lastDragAndMoveTime;

    private MapperManager mapperManager;

    private Composite mapperUIParent;

    private Thread threadToEvaluatePerformance;

    private boolean firstTimeShowlinks;

    /* Constants */
    static final int SASH_WIDTH = 1;

    static final int SASH_LIMIT = 20;

    final MouseMoveScrollZoneHelper mouseMoveScrollZoneHelper = new MouseMoveScrollZoneHelper();

    private static final boolean SHOW_BORDERS = false;

    private static final int INCREMENT_VERTICAL_SCROLLBAR_ZONE = 100;

    public static final int OFFSET_VISIBLES_POINTS = 15;

    private DropTargetOperationListener dropTargetOperationListener;

    private DraggingInfosPopup draggingInfosPopup;

    private InputsZone inputsZone;

    private OutputsZone outputsZone;

    private MapperBgDrawableComposite bgDrawableComposite;

    private BackgroundRefresher backgroundRefresher;

    private Display display;

    private Shell shell;

    private boolean updateBackgroundEnabled;

    private FooterComposite footerComposite;

    private Map<IConnection, Set<String>> preColumnSet = new HashMap<IConnection, Set<String>>();

    public MapperUI(Composite parent, MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
        this.mapperManager.getUiManager().setMapperUI(this);
        this.mapperUIParent = parent;
        this.display = this.mapperUIParent.getDisplay();
    }

    public MapperUI(Display display, MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
        this.mapperManager.getUiManager().setMapperUI(this);
        this.display = display;
    }

    public void createUI(MapperModel mapperModel) {
        if (mapperUIParent == null) {
            this.shell = createWindow(display, mapperModel);
        } else {
            createCompositeContent(mapperModel);
        }
    }

    /**
     * DOC amaumont Comment method "createUI".
     *
     * @param display
     */
    public Shell createWindow(final Display display, MapperModel model) {

        Shell activeShell = display.getActiveShell();
        Shell mapperShell = null;
        int style = SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL | SWT.RESIZE;
        if (activeShell == null) {
            mapperShell = new Shell(mapperShell, style);
        } else {
            mapperShell = new Shell(activeShell, style);
        }

        this.mapperUIParent = mapperShell;
        final Shell mapperShellFinal = mapperShell;

        mapperShell.addShellListener(new ShellListener() {

            public void shellActivated(ShellEvent e) {
            }

            public void shellClosed(ShellEvent e) {
                checkExpressionFilter();
                UIManager uiManager = mapperManager.getUiManager();
                if (uiManager.getMapperResponse() == SWT.NONE) {
                    for (DataMapTableView dataMapTableView : uiManager.getInputsTablesView()) {
                        dataMapTableView.notifyFocusLost();
                    }
                    for (DataMapTableView dataMapTableView : uiManager.getOutputsTablesView()) {
                        dataMapTableView.notifyFocusLost();
                    }
                    for (DataMapTableView dataMapTableView : uiManager.getVarsTablesView()) {
                        dataMapTableView.notifyFocusLost();
                    }
                    uiManager.setMapperResponse(SWT.CANCEL);
                    uiManager.prepareClosing(uiManager.getMapperResponse());
                }
                if (!mapperManager.componentIsReadOnly() && mapperManager.isDataChanged()
                        && !mapperManager.getUiManager().isCloseWithoutPrompt()) {
                    boolean closeWindow = MessageDialog.openConfirm(mapperShellFinal,
                            Messages.getString("MapperUI.CancelWithoutSaveModifications.Title"), //$NON-NLS-1$
                            Messages.getString("MapperUI.CancelWithoutSaveModifications.Message")); //$NON-NLS-1$
                    if (!closeWindow) {
                        e.doit = false;
                    } else {
                        mapperManager.getAbstractMapComponent().setExternalData(mapperManager.getOriginalExternalData());
                        mapperManager.getUiManager().prepareClosing(SWT.CANCEL);
                    }

                }
            }

            public void shellDeactivated(ShellEvent e) {
            }

            public void shellDeiconified(ShellEvent e) {
            }

            public void shellIconified(ShellEvent e) {
            }

        });

        MapperComponent component = (MapperComponent) mapperManager.getAbstractMapComponent();

        ExternalMapperUiProperties uiProperties = mapperManager.getUiManager().getUiProperties();

        mapperShell.setImage(CoreImageProvider.getComponentIcon(component.getComponent(), ICON_SIZE.ICON_32));

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String productName = brandingService.getFullProductName();
        mapperShell.setText(Messages.getString(
                "MapperMain.ShellTitle", productName, component.getComponent().getName(), component.getUniqueName())); //$NON-NLS-1$

        Rectangle boundsMapper = uiProperties.getBoundsMapper();

        if (uiProperties.isShellMaximized()) {
            mapperShell.setMaximized(uiProperties.isShellMaximized());
        } else {

            if (uiProperties.getBoundsMapper().equals(ExternalMapperUiProperties.DEFAULT_BOUNDS_MAPPER)) {
                mapperShell.setMaximized(true);
                // boundsMapper = display.getBounds(); //no need this, will cause the full problems.
            } else {
                // // move shell at outer of display zone to avoid visual effect on loading
                // Rectangle tmpBoundsMapper = new Rectangle(-boundsMapper.width, boundsMapper.y, boundsMapper.width,
                // boundsMapper.height);
                // shell.setBounds(tmpBoundsMapper);
                boundsMapper = uiProperties.getBoundsMapper();
                if (boundsMapper.x < 0) {
                    boundsMapper.x = 0;
                }
                if (boundsMapper.y < 0) {
                    boundsMapper.y = 0;
                }
                mapperShell.setBounds(boundsMapper);
            }
        }
        createCompositeContent(model);
        mapperShell.open();
        return mapperShell;

    }

    private void checkExpressionFilter() {
        // TUP-22701 for MAC won't trigger focusLost
        if (!EnvironmentUtils.isMacOsSytem()) {
            return;
        }
        UIManager uiManager = mapperManager.getUiManager();
        for (DataMapTableView inputTableView : uiManager.getInputsTablesView()) {
            setExpressionFilterOnfocusForShellClose(inputTableView);
        }
        for (DataMapTableView outputTableView : uiManager.getOutputsTablesView()) {
            setExpressionFilterOnfocusForShellClose(outputTableView);
        }
    }

    private void setExpressionFilterOnfocusForShellClose(DataMapTableView dataMapTableView) {
        UnnotifiableColorStyledText expressionFilterText = dataMapTableView.getExpressionFilterText();
        if (!expressionFilterText.isFocusControl()) {
            return;
        }
        if ("".equals(expressionFilterText.getText().trim())) {
            expressionFilterText.setText("");
        }
        final AbstractInOutTable table = (AbstractInOutTable) dataMapTableView.getDataMapTable();
        dataMapTableView.setExpressionFilterFromStyledText(table, expressionFilterText);
        dataMapTableView.checkProblemsForExpressionFilter(false, true);
    }

    public void createCompositeContent(MapperModel mapperModel) {
        // long time1 = System.currentTimeMillis();

        // CommandStack commandStack = new CommandStackForComposite(this.mapperUIParent);
        // mapperManager.setCommandStack(commandStack);

        updateBackgroundEnabled = false;
        List<? extends IConnection> inConnections = mapperManager.getAbstractMapComponent().getIncomingConnections();
        List<? extends IConnection> outConnections = mapperManager.getAbstractMapComponent().getOutgoingConnections();
        // see bug 7471, record the precolumns value
        for (IConnection conn : inConnections) {
            conn.getMetadataTable();
            if (conn.getMetadataTable() != null) {
                List<IMetadataColumn> inputColumns = conn.getMetadataTable().getListColumns();
                for (IMetadataColumn inputColumn : inputColumns) {
                    Set<String> columnSet = preColumnSet.get(conn);
                    if (columnSet == null) {
                        columnSet = new HashSet<String>();
                        preColumnSet.put(conn, columnSet);
                    }
                    columnSet.add(inputColumn.getLabel());
                    preColumnSet.put(conn, columnSet);
                }
            }
        }
        for (IConnection conn : outConnections) {
            if (conn.getMetadataTable() != null) {
                List<IMetadataColumn> outputColumns = conn.getMetadataTable().getListColumns();
                for (IMetadataColumn outputColumn : outputColumns) {
                    Set<String> columnSet = preColumnSet.get(conn);
                    if (columnSet == null) {
                        columnSet = new HashSet<String>();
                        preColumnSet.put(conn, columnSet);
                    }
                    columnSet.add(outputColumn.getLabel());
                    preColumnSet.put(conn, columnSet);
                }
            }
        }
        final UIManager uiManager = mapperManager.getUiManager();
        final ExternalMapperUiProperties uiProperties = uiManager.getUiProperties();

        addParentListeners(uiManager, uiProperties);

        bgColorLinksZone = ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_LINKS_ZONE);

        GridLayout parentLayout = new GridLayout(1, true);
        mapperUIParent.setLayout(parentLayout);

        addKeyListener(uiManager, display);

        addBackgroundRefreshLimiters(display);

        this.dropTargetOperationListener = new DropTargetOperationListener(mapperManager);
        dropTargetOperationListener.addControl(this.mapperUIParent.getShell());

        mainSashForm = new SashForm(mapperUIParent, SWT.SMOOTH | SWT.VERTICAL);
        GridData mainSashFormGridData = new GridData(GridData.FILL_BOTH);
        mainSashForm.setLayoutData(mainSashFormGridData);

        datasFlowViewSashForm = new SashForm(mainSashForm, SWT.SMOOTH | SWT.HORIZONTAL | SWT.BORDER);

        initBackgroundComponents();

        if (WindowSystem.isGTK()) {
            datasFlowViewSashForm.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
        }

        // dropTargetOperationListener.addControl(datasFlowViewSashForm);

        // datasFlowViewSashForm.addControlListener(new ControlListener() {
        //
        // public void controlMoved(ControlEvent e) {
        // }
        //
        // public void controlResized(ControlEvent e) {
        // createBgImages();
        // updateBackground(true, false);
        // }
        //
        // });

        /* Create the tabs */
        tabFolderEditors = new TabFolderEditors(mainSashForm, SWT.BORDER, mapperManager);

        createInputZoneWithTables(mapperModel, display);

        createVarsZoneWithTables(mapperModel, display);

        createOutputZoneWithTables(mapperModel, uiManager, display);

        if (WindowSystem.isBigSurOrLater()) {
            Color bgColorTransparent = ColorProviderMapper.getRGBAColor(ColorInfo.COLOR_BACKGROUND_TRANSPRENT);
            datasFlowViewSashForm.setBackgroundMode(SWT.INHERIT_NONE);
            sc1.setBackground(bgColorTransparent);
            inputTablesZoneView.setBackground(bgColorTransparent);
            sc2.setBackground(bgColorTransparent);
            varsTableZoneView.setBackground(bgColorTransparent);
            sc3.setBackground(bgColorTransparent);
            outputTablesZoneView.setBackground(bgColorTransparent);
        } else {
            datasFlowViewSashForm.setBackgroundMode(SWT.INHERIT_FORCE);
        }

        uiManager.parseAllExpressionsForAllTables();
        mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
        mapperManager.getProblemsManager().checkLookupExpressionProblem();

        this.draggingInfosPopup = DraggingInfosPopup.getNewShell(this.mapperUIParent.getShell());

        configureZoneScrollBars(display);

        int[] weightsDatasFlowViewSashForm = uiProperties.getWeightsDatasFlowViewSashForm();
        datasFlowViewSashForm.setWeights(weightsDatasFlowViewSashForm.length != 0 ? weightsDatasFlowViewSashForm
                : ExternalMapperUiProperties.DEFAULT_WEIGHTS_DATAS_FLOW_SASH_FORM);

        int[] weightsMainSashForm = uiProperties.getWeightsMainSashForm();
        weightsMainSashForm = weightsMainSashForm.length != 0 ? weightsMainSashForm
                : ExternalMapperUiProperties.DEFAULT_WEIGHTS_MAIN_SASH_FORM;
        mainSashForm.setWeights(weightsMainSashForm);

        footerComposite = new FooterComposite(this.mapperUIParent, SWT.NONE, mapperManager);

        selectFirstInOutTablesView();
        updateBackgroundEnabled = true;
        updateBackground(true, false);

        // List<DataMapTableView> inputsTablesView = uiManager.getInputsTablesView();
        // for (DataMapTableView table : inputsTablesView) {
        //
        // table.showTableGlobalMap(false);
        // table.showTableGlobalMap(true);
        //
        // // table.updateGridDataHeightForTableGlobalMap();
        // // table.resizeAtExpandedSize();
        // }

    }

    /**
     * DOC amaumont Comment method "initBackgroundRefresher".
     */
    private void initBackgroundComponents() {
        this.bgDrawableComposite = new MapperBgDrawableComposite(datasFlowViewSashForm);
        this.backgroundRefresher = new BackgroundRefresher(this.bgDrawableComposite);
        this.backgroundRefresher.setBackgroundColor(bgColorLinksZone);
    }

    private void selectFirstInOutTablesView() {
        List<DataMapTableView> inputsTablesView = mapperManager.getUiManager().getInputsTablesView();
        UIManager uiManager = mapperManager.getUiManager();
        if (inputsTablesView.size() > 0) {
            uiManager.selectDataMapTableView(inputsTablesView.get(0), true, false);
        }

        List<DataMapTableView> outputsTablesView = mapperManager.getUiManager().getOutputsTablesView();
        if (outputsTablesView.size() > 0) {
            uiManager.selectDataMapTableView(outputsTablesView.get(0), true, false);
        }
    }

    private void resizeNotMinimizedTablesAtExpandedSize(final Display display) {
        display.syncExec(new Runnable() {

            public void run() {

                Collection<DataMapTableView> tablesView = mapperManager.getTablesView();
                for (DataMapTableView view : tablesView) {
                    if (!view.getDataMapTable().isMinimized()) {
                        view.resizeAtExpandedSize();
                    }
                }
            }

        });
    }

    private void configureZoneScrollBars(final Display display) {
        vBar1 = sc1.getVerticalBar();
        vBar2 = sc2.getVerticalBar();
        vBar3 = sc3.getVerticalBar();

        vBar1.setIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);
        vBar2.setIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);
        vBar3.setIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);
        vBar1.setPageIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);
        vBar2.setPageIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);
        vBar3.setPageIncrement(INCREMENT_VERTICAL_SCROLLBAR_ZONE);

        SelectionListener scrollListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.detail == SWT.NONE) {
                    mouseMoveScrollZoneHelper.mouseDownOnScroll = false;
                    mouseMoveScrollZoneHelper.scrolling = false;
                    updateBackground(false, true);
                } else {
                    mouseMoveScrollZoneHelper.mouseDownOnScroll = true;
                    mouseMoveScrollZoneHelper.scrolling = true;
                    backgroundRefreshLimiter.setTimeBeforeNewExecution(backgroundRefreshTimeForScrolling);
                    backgroundRefreshLimiter.startIfExecutable();
                }
            }
        };
        vBar1.addSelectionListener(scrollListener);
        vBar2.addSelectionListener(scrollListener);
        vBar3.addSelectionListener(scrollListener);
    }

    private void addBackgroundRefreshLimiters(final Display display) {

        int time = 100;

        backgroundRefreshLimiter = new ExecutionLimiterImproved(time, true) {

            @Override
            public void execute(final boolean isFinalExecution, Object data) {

                // new Exception("backgroundRefreshLimiter.updateBackground").printStackTrace();

                display.syncExec(new Runnable() {

                    public void run() {

                        if (mouseMoveScrollZoneHelper.mouseDownOnScroll && mouseMoveScrollZoneHelper.scrolling) {
                            updateBackground(false, false);
                        } else {
                            updateBackground(false, true);
                        }
                    }

                });

            }
        };
        backgroundRefreshLimiterForceRecalculate = new ExecutionLimiterImproved(time, true) {

            @Override
            public void execute(final boolean isFinalExecution, Object data) {

                // new Exception("backgroundRefreshLimiterForceRecalculate.updateBackground").printStackTrace();

                display.syncExec(new Runnable() {

                    public void run() {
                        if (!isFinalExecution) {
                            updateBackground(true, false);
                        } else {
                            updateBackground(true, true);
                        }
                    }

                });
            }
        };
    }

    private void addKeyListener(final UIManager uiManager, final Display display) {
        Listener listener = new Listener() {

            public void handleEvent(Event event) {
                // System.out.println("event : "+EventUtil.getEventName(event.type));
                // System.out.println(": "+event.widget);

                if (event.type == SWT.KeyUp || event.type == SWT.KeyDown) {
                    boolean isPressed = event.type == SWT.KeyDown ? true : false;
                    if (event.keyCode == SWT.CTRL) {

                        uiManager.setCtrlPressed(isPressed);

                        // System.out.println("+++++ Ctrl : " + isPressed);
                    }
                    if (event.keyCode == SWT.SHIFT) {
                        uiManager.setShiftPressed(isPressed);
                        // System.out.println("+++++ Shift : "+ isPressed);

                    }
                }

                // System.out.println(event.stateMask);

            }

        };
        display.addFilter(SWT.KeyUp, listener);
        display.addFilter(SWT.KeyDown, listener);
        // display.addFilter(SWT.Move, listener);
        // display.addFilter(SWT.MouseMove, listener);
        // display.addFilter(SWT.HardKeyDown, listener);
        // display.addFilter(SWT.HardKeyUp, listener);
        // display.addFilter(SWT.None, listener);
        // display.addFilter(SWT.DragDetect, listener);
        // display.addFilter(SWT.Paint, listener);

        // display.addListener(SWT.KeyUp, listener);
        // display.addListener(SWT.KeyDown, listener);
        // display.addListener(SWT.Move, listener);
        // display.addListener(SWT.MouseMove, listener);
        // display.addListener(SWT.HardKeyDown, listener);
        // display.addListener(SWT.HardKeyUp, listener);
        // display.addListener(SWT.None, listener);
        // display.addListener(SWT.DragDetect, listener);
        // display.addListener(SWT.Paint, listener);

    }

    private void addParentListeners(final UIManager uiManager, final ExternalMapperUiProperties uiProperties) {
        mapperUIParent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                release();
            }

        });

        mapperUIParent.addListener(SWT.Close, new Listener() {

            public void handleEvent(Event event) {
                if (uiManager.getMapperResponse() == SWT.NONE) {
                    uiManager.setMapperResponse(SWT.CANCEL);
                }
            }

        });

        mapperUIParent.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                updateBackground(false, true);
            }

            public void focusLost(FocusEvent e) {
            }

        });

        // store size if not maximized
        if (mapperUIParent instanceof Shell) {
            ((Shell) mapperUIParent).addControlListener(new ControlListener() {

                public void controlMoved(ControlEvent e) {

                }

                public void controlResized(ControlEvent e) {
                    if (!((Shell) e.getSource()).getMaximized()) {
                        uiProperties.setBoundsMapper(((Shell) e.getSource()).getBounds());
                    }
                }
            });
        }
    }

    private void createInputZoneWithTables(MapperModel mapperModel, final Display display) {
        inputsZone = new InputsZone(datasFlowViewSashForm, getZoneStyle(), mapperManager);
        inputsZone.createHeaderZoneComponents();

        sc1 = new ScrolledComposite(inputsZone, getBorder() | SWT.H_SCROLL | SWT.V_SCROLL);
        // this.dropTargetOperationListener.addControl(sc1);

        GridData sc1GridData = new GridData(GridData.FILL_BOTH);
        sc1.setLayoutData(sc1GridData);

        sc1.setBackgroundMode(SWT.INHERIT_DEFAULT);
        sc1.addControlListener(new ControlListener() {

            public void controlMoved(ControlEvent e) {
                // System.out.println("sc1 controlMoved");

            }

            public void controlResized(ControlEvent e) {
                // System.out.println("sc1 controlResized");
                onSashResized(display);
            }

        });
        // add for auto scroll
        new DragNDrop(mapperManager, sc1, true, true);
        inputTablesZoneView = new InputTablesZoneView(sc1, getBorder(), mapperManager);
        // this.dropTargetOperationListener.addControl(inputTablesZoneView);
        inputTablesZoneView.setBackgroundMode(SWT.INHERIT_DEFAULT);

        sc1.setExpandHorizontal(true);

        sc1.setContent(inputTablesZoneView);
        sc1MSListener = new MouseScrolledListener(mapperManager.getUiManager(), sc1);

        inputTablesZoneView.initInsertionIndicator();

        Control previousControl = null;

        List<InputTable> tables = mapperModel.getInputDataMapTables();
        Boolean minimizeStateOfTables = getMinimizedButtonState(tables);
        if (minimizeStateOfTables != null) {
            inputsZone.getToolbar().setMinimizeButtonState(minimizeStateOfTables.booleanValue());
        }
        for (InputTable inputTable : tables) {

            InputDataMapTableView dataMapTableView = new InputDataMapTableView(inputTablesZoneView, SWT.BORDER, inputTable,
                    mapperManager);
            FormData formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(previousControl);
            dataMapTableView.setLayoutData(formData);
            previousControl = dataMapTableView;
            boolean tableIsMinimized = inputTable.isMinimized();
            dataMapTableView.minimizeTable(tableIsMinimized);
            dataMapTableView.registerStyledExpressionEditor(getTabFolderEditors().getStyledTextHandler());
            dataMapTableView.loaded();
            // dataMapTableView.fillMinimumSize(false);
        }
        inputTablesZoneView.setSize(inputTablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        sc1MSListener.addMouseWheelListener(sc1);
    }

    /**
     * DOC amaumont Comment method "getCommonMinimizedStateOfTables".
     *
     * @param tables
     * @return new Boolean(true) if button state should be to minimize, else new Boolean(false)
     */
    private Boolean getMinimizedButtonState(List<? extends AbstractDataMapTable> tables) {
        boolean allTablesAreMinimized = true;
        boolean allTablesAreNotMinimized = true;

        for (IDataMapTable table : tables) {
            if (table.isMinimized()) {
                allTablesAreNotMinimized = false;
            } else {
                allTablesAreMinimized = false;
            }
        }

        if (allTablesAreMinimized) {
            return new Boolean(true);
        } else if (allTablesAreNotMinimized) {
            return new Boolean(false);
        }

        return new Boolean(false);
    }

    private void createVarsZoneWithTables(MapperModel mapperModel, final Display display) {
        Control previousControl;
        // Feature TDI-26691 : Add search option
        SearchZone searchZone = new SearchZone(datasFlowViewSashForm, getZoneStyle(), mapperManager);
        searchZone.createSearchZone();

        sc2 = new ScrolledComposite(searchZone, getBorder() | SWT.H_SCROLL | SWT.V_SCROLL);

        GridData sc2GridData = new GridData(GridData.FILL_BOTH);
        sc2.setLayoutData(sc2GridData);

        varsTableZoneView = new VarsTableZoneView(sc2, getBorder(), mapperManager);

        sc2.setContent(varsTableZoneView);
        sc2.setBackgroundMode(SWT.INHERIT_DEFAULT);
        sc2.setExpandHorizontal(true);
        sc2.addControlListener(new ControlListener() {

            public void controlMoved(ControlEvent e) {
                // System.out.println("sc2 controlMoved");

            }

            public void controlResized(ControlEvent e) {
                // System.out.println("sc2 controlResized");
                onSashResized(display);
            }

        });
        sc2MSListener = new MouseScrolledListener(mapperManager.getUiManager(), sc2);
        varsTableZoneView.initInsertionIndicator();

        // final Composite finalTablesZoneViewVars = tablesZoneViewVars;

        previousControl = null;
        for (VarsTable varsTable : mapperModel.getVarsDataMapTables()) {

            DataMapTableView dataMapTableView = new VarsDataMapTableView(varsTableZoneView, SWT.BORDER, varsTable, mapperManager);

            FormData formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(previousControl);
            dataMapTableView.setLayoutData(formData);
            previousControl = dataMapTableView;
            dataMapTableView.minimizeTable(varsTable.isMinimized());
            dataMapTableView.registerStyledExpressionEditor(getTabFolderEditors().getStyledTextHandler());
            // dataMapTableView.fillMinimumSize(false);
        }

        varsTableZoneView.setSize(varsTableZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sc2MSListener.addMouseWheelListener(sc2);
    }

    private void createOutputZoneWithTables(MapperModel mapperModel, final UIManager uiManager, final Display display) {
        Control previousControl;
        outputsZone = new OutputsZone(datasFlowViewSashForm, getZoneStyle(), mapperManager);
        outputsZone.createHeaderZoneComponents();
        // this.dropTargetOperationListener.addControl(outputsZone);

        sc3 = new ScrolledComposite(outputsZone, getBorder() | SWT.H_SCROLL | SWT.V_SCROLL);
        // this.dropTargetOperationListener.addControl(sc3);

        GridData sc3GridData = new GridData(GridData.FILL_BOTH);
        sc3.setLayoutData(sc3GridData);
        sc3.setBackgroundMode(SWT.INHERIT_DEFAULT);
        sc3.addControlListener(new ControlListener() {

            public void controlMoved(ControlEvent e) {
                // System.out.println("sc3 controlMoved");

            }

            public void controlResized(ControlEvent e) {
                // System.out.println("sc3 controlResized");

                onSashResized(display);

            }

        });

        outputTablesZoneView = new OutputTablesZoneView(sc3, getBorder(), mapperManager);
        // this.dropTargetOperationListener.addControl(outputTablesZoneView);
        outputTablesZoneView.setBackgroundMode(SWT.INHERIT_DEFAULT);

        sc3.setExpandHorizontal(true);
        sc3.setContent(outputTablesZoneView);

        outputTablesZoneView.initInsertionIndicator();

        previousControl = null;

        List<OutputTable> tables = mapperModel.getOutputDataMapTables();
        Boolean minimizeStateOfTables = getMinimizedButtonState(tables);
        if (minimizeStateOfTables != null) {
            outputsZone.getToolbar().setMinimizeButtonState(minimizeStateOfTables.booleanValue());
        }
        sc3MSListener = new MouseScrolledListener(mapperManager.getUiManager(), sc3);
        for (OutputTable outputTable : tables) {

            OutputDataMapTableView dataMapTableView = uiManager.createNewOutputTableView(previousControl, outputTable,
                    outputTablesZoneView);
            previousControl = dataMapTableView;
        }
        outputTablesZoneView.setSize(outputTablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    public MouseScrolledListener getVarMouseSrolledListener() {
        return this.sc2MSListener;
    }

    public MouseScrolledListener getOutputMouseSrolledListener() {
        return this.sc3MSListener;
    }

    /**
     * DOC amaumont Comment method "release".
     */
    protected void release() {
        if (threadToEvaluatePerformance != null) {
            threadToEvaluatePerformance.interrupt();
        }
        ImageProviderMapper.releaseImages();
        ColorProviderMapper.releaseColors();
        FontProviderMapper.releaseFonts();
        if (backgroundRefreshLimiter != null) {
            backgroundRefreshLimiter.shutdown();
        }
        if (backgroundRefreshLimiterForceRecalculate != null) {
            backgroundRefreshLimiterForceRecalculate.shutdown();
        }
    }

    protected void updateBackground(boolean forceRecalculate, boolean antialias) {

        if (updateBackgroundEnabled) {

            antialias = false;

            bgDrawableComposite.setAntialias(antialias);
            bgDrawableComposite.setForceRecalculate(forceRecalculate);
            backgroundRefresher.refreshBackground();
        }

    }

    private int getBorder() {
        return SHOW_BORDERS ? SWT.BORDER : SWT.NONE;
    }

    private int getZoneStyle() {
        return WindowSystem.isBigSurOrLater() ? (SWT.NONE | SWT.NO_BACKGROUND) : SWT.NONE;
    }

    /**
     *
     * DOC amaumont MapperUI class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    public class MapperBgDrawableComposite extends BgDrawableComposite {

        private boolean antialias;

        private boolean forceRecalculate;

        /**
         * DOC amaumont MapperBackgroundRefresher constructor comment.
         *
         * @param commonParent
         */
        public MapperBgDrawableComposite(Composite commonParent) {
            super(commonParent);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.commons.ui.swt.drawing.link.BackgroundRefresher#drawBackground(org.eclipse.swt.graphics.GC)
         */
        @Override
        public void drawBackground(GC gc) {

            if (antialias && backgroundRefresher.isAntialiasAllowed() && mapperManager.getCurrentNumberLinks() < 250) {
                gc.setAntialias(antialias && backgroundRefresher.isAntialiasAllowed() ? SWT.ON : SWT.OFF);
            } else {
                gc.setAdvanced(false);
            }

            List<IMapperLink> links = mapperManager.getLinks();

            if (firstTimeShowlinks) {
                forceRecalculate = true;
                firstTimeShowlinks = false;
            }
            int lstSize = links.size();

            Rectangle bounds = datasFlowViewSashForm.getBounds();
            Rectangle boundsOfDrawing = new Rectangle(0, 0, 0, 0);
            int heightToolbarZone = inputsZone.getToolbar().getComposite().getSize().y;
            boundsOfDrawing.x = -OFFSET_VISIBLES_POINTS;
            boundsOfDrawing.y = heightToolbarZone - OFFSET_VISIBLES_POINTS;
            boundsOfDrawing.width = bounds.width + 2 * OFFSET_VISIBLES_POINTS;
            boundsOfDrawing.height = bounds.height + 2 * OFFSET_VISIBLES_POINTS - heightToolbarZone;
            for (int i = 0; i < lstSize; i++) {
                IMapperLink link = links.get(i);
                if (forceRecalculate) {
                    link.calculate();
                }
                link.draw(gc, boundsOfDrawing);
            }

        } // public void drawBackground(GC gc) {

        /**
         * Getter for antialias.
         *
         * @return the antialias
         */
        public boolean isAntialias() {
            return this.antialias;
        }

        /**
         * Sets the antialias.
         *
         * @param antialias the antialias to set
         */
        public void setAntialias(boolean antialias) {
            this.antialias = antialias;
        }

        /**
         * Getter for forceRecalculate.
         *
         * @return the forceRecalculate
         */
        public boolean isForceRecalculate() {
            return this.forceRecalculate;
        }

        /**
         * Sets the forceRecalculate.
         *
         * @param forceRecalculate the forceRecalculate to set
         */
        public void setForceRecalculate(boolean forceRecalculate) {
            this.forceRecalculate = forceRecalculate;
        }
    } // public class MapperBackgroundRefresher extends BackgroundRefresher {

    public Composite getVisualMapReferenceComposite() {
        return this.datasFlowViewSashForm;
    }

    public int getScollbarSelectionZoneInputs() {
        return vBar1.getSelection();
    }

    public int getScollbarSelectionZoneVars() {
        return vBar2.getSelection();
    }

    public int getScollbarSelectionZoneOutputs() {
        return vBar3.getSelection();
    }

    public ScrollBar getScollbarZoneInputs() {
        return vBar1;
    }

    public ScrollBar getScollbarZoneVars() {
        return vBar2;
    }

    public ScrollBar getScollbarZoneOutputs() {
        return vBar3;
    }

    public ExecutionLimiterImproved getBackgroundRefreshLimiter() {
        return this.backgroundRefreshLimiter;
    }

    public ExecutionLimiterImproved getBackgroundRefreshLimiterForceRecalculate() {
        return this.backgroundRefreshLimiterForceRecalculate;
    }

    public Composite getMapperUIParent() {
        return this.mapperUIParent;
    }

    public TabFolderEditors getTabFolderEditors() {
        return this.tabFolderEditors;
    }

    private void onSashResized(Display display) {
        backgroundRefreshLimiterForceRecalculate.startIfExecutable();
    }

    public InputTablesZoneView getInputTablesZoneView() {
        return this.inputTablesZoneView;
    }

    public TablesZoneView getOutputTablesZoneView() {
        return this.outputTablesZoneView;
    }

    public TablesZoneView getVarsTableZoneView() {
        return this.varsTableZoneView;
    }

    public ScrolledComposite getScrolledCompositeViewOutputs() {
        return sc3;
    }

    public ScrolledComposite getScrolledCompositeViewVars() {
        return sc2;
    }

    public ScrolledComposite getScrolledCompositeViewInputs() {
        return sc1;
    }

    public SashForm getDatasFlowViewSashForm() {
        return this.datasFlowViewSashForm;
    }

    public SashForm getMainSashForm() {
        return this.mainSashForm;
    }

    public DropTargetOperationListener getDropTargetOperationListener() {
        return this.dropTargetOperationListener;
    }

    public DraggingInfosPopup getDraggingInfosPopup() {
        return this.draggingInfosPopup;
    }

    public InputsZone getInputsZone() {
        return this.inputsZone;
    }

    public OutputsZone getOutputsZone() {
        return this.outputsZone;
    }

    /**
     * Getter for shell.
     *
     * @return the shell
     */
    public Shell getShell() {
        return this.shell;
    }

    /**
     *
     * DOC amaumont Comment method "getStatusBarLabel".
     *
     * @return null if label is not created
     */
    public StatusBar getStatusBar() {
        if (footerComposite != null) {
            return footerComposite.getStatusBar();
        } else {
            return null;
        }
    }

    /**
     * DOC wzhang Comment method "getPreColumnSet".
     */
    public Map<IConnection, Set<String>> getPreColumnSet() {
        return this.preColumnSet;
    }

}
