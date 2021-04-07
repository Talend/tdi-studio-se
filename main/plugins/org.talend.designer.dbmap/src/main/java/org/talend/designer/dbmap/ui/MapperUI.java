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
package org.talend.designer.dbmap.ui;

import java.util.Collection;
import java.util.List;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.thread.AsynchronousThreading;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.drawing.background.BackgroundRefresher;
import org.talend.commons.ui.swt.linking.BgDrawableComposite;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.model.process.IConnection;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.listener.DropTargetOperationListener;
import org.talend.designer.abstractmap.ui.listener.MouseScrolledListener;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.external.data.ExternalDbMapUiProperties;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.MapperModel;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.ui.color.ColorInfo;
import org.talend.designer.dbmap.ui.color.ColorProviderMapper;
import org.talend.designer.dbmap.ui.event.MouseMoveScrollZoneHelper;
import org.talend.designer.dbmap.ui.font.FontProviderMapper;
import org.talend.designer.dbmap.ui.footer.FooterComposite;
import org.talend.designer.dbmap.ui.image.ImageProviderMapper;
import org.talend.designer.dbmap.ui.tabs.TabFolderEditors;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.OutputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.InputsZone;
import org.talend.designer.dbmap.ui.visualmap.zone.OutputsZone;
import org.talend.designer.dbmap.ui.visualmap.zone.SearchZone;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.InputTablesZoneView;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.OutputTablesZoneView;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.VarsTableZoneView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: MapperUI.java 1744 2007-01-31 13:05:30Z amaumont $
 *
 */
public class MapperUI {

    /**
     * Default value for middle performance in ms.
     */
    public static final int DEFAULT_TIME_BEFORE_NEW_BG_REFRESH = 5;

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

    private ExecutionLimiter backgroundRefreshLimiter;

    private ExecutionLimiter backgroundRefreshLimiterForceRecalculate;

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

    public MapperUI(Composite parent, MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
        this.mapperManager.getUiManager().setMapperUI(this);
        this.mapperUIParent = parent;
    }

    public void init(MapperModel mapperModel) {
        // long time1 = System.currentTimeMillis();

        // CommandStack commandStack = new CommandStackForComposite(this.mapperUIParent);
        // mapperManager.setCommandStack(commandStack);

        final UIManager uiManager = mapperManager.getUiManager();
        final ExternalDbMapUiProperties uiProperties = uiManager.getUiProperties();

        addParentListeners(uiManager, uiProperties);

        final Display display = mapperUIParent.getDisplay();
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
        String dbmsId = null;
        if (mapperModel.getInputDataMapTables() != null && !mapperModel.getInputDataMapTables().isEmpty()) {
            InputTable input = mapperModel.getInputDataMapTables().get(0);
            if (input.getMetadataTable() != null) {
                dbmsId = input.getMetadataTable().getDbms();
            }
        }
        if(dbmsId == null && mapperModel.getOutputDataMapTables() != null && !mapperModel.getOutputDataMapTables().isEmpty()) {
            OutputTable output = mapperModel.getOutputDataMapTables().get(0);
            if (output.getMetadataTable() != null) {
                dbmsId = output.getMetadataTable().getDbms();
            }
        }
        if(dbmsId == null && mapperManager.getAbstractMapComponent() != null 
                && !mapperManager.getAbstractMapComponent().getIncomingConnections().isEmpty()){
            IConnection conn = mapperManager.getAbstractMapComponent().getIncomingConnections().get(0);
            if(conn.getMetadataTable() != null) {
                dbmsId = conn.getMetadataTable().getDbms();
            }
        }
        if (dbmsId == null) {
            Object dbmsValue = mapperManager.getElementParameterValue(EParameterName.MAPPING.getName());
            if (dbmsValue != null && dbmsValue instanceof String) {
                dbmsId = (String) dbmsValue;
            }
        }
        
        tabFolderEditors = new TabFolderEditors(mainSashForm, SWT.BORDER, mapperManager, dbmsId);

        createInputZoneWithTables(mapperModel, uiManager, display);

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

        mapperManager.initInternalData();

        uiManager.parseAllExpressionsForAllTables();
        mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);

        this.draggingInfosPopup = DraggingInfosPopup.getNewShell(this.mapperUIParent.getShell());

        configureZoneScrollBars(display);

        int[] weightsDatasFlowViewSashForm = uiProperties.getWeightsDatasFlowViewSashForm();
        datasFlowViewSashForm.setWeights(weightsDatasFlowViewSashForm.length != 0 ? weightsDatasFlowViewSashForm
                : ExternalDbMapUiProperties.DEFAULT_WEIGHTS_DATAS_FLOW_SASH_FORM);

        int[] weightsMainSashForm = uiProperties.getWeightsMainSashForm();
        mainSashForm.setWeights(weightsMainSashForm.length != 0 ? weightsMainSashForm
                : ExternalDbMapUiProperties.DEFAULT_WEIGHTS_MAIN_SASH_FORM);

        new FooterComposite(this.mapperUIParent, SWT.NONE, mapperManager);

        if (WindowSystem.isGTK()) {
            // resize especially for GTK
            new AsynchronousThreading(1000, false, display, new Runnable() {

                public void run() {
                    resizeNotMinimizedTablesAtExpandedSize(display);
                    mapperUIParent.getShell().layout();
                }

            }).start();
        }
        if (WindowSystem.isWIN32()) {
            List<DataMapTableView> inputsTablesView = uiManager.getInputsTablesView();
            for (DataMapTableView view : inputsTablesView) {
                ((InputDataMapTableView) view).refreshLabelForJoinDropDown();
            }
        }

        selectFirstInOutTablesView();

        mapperManager.getUiManager().refreshSqlExpression();
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
        UIManager uiManager = mapperManager.getUiManager();
        List<DataMapTableView> inputsTablesView = uiManager.getInputsTablesView();
        if (inputsTablesView.size() > 0) {
            uiManager.selectDataMapTableView(inputsTablesView.get(0), true, false);
        }

        List<DataMapTableView> outputsTablesView = uiManager.getOutputsTablesView();
        if (outputsTablesView.size() > 0) {
            uiManager.selectDataMapTableView(outputsTablesView.get(0), true, false);
        }

        for (DataMapTableView view : outputsTablesView) {
            view.getTableViewerCreatorForColumns().getTableViewer().refresh();
        }
    }

    private void resizeNotMinimizedTablesAtExpandedSize(final Display display) {
        display.asyncExec(new Runnable() {

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

            public void widgetSelected(SelectionEvent event) {
                if (event.detail == SWT.NONE) {
                    mouseMoveScrollZoneHelper.mouseDownOnScroll = false;
                    mouseMoveScrollZoneHelper.scrolling = false;
                    updateBackground(false, true);
                } else {
                    mouseMoveScrollZoneHelper.mouseDownOnScroll = true;
                    mouseMoveScrollZoneHelper.scrolling = true;
                    backgroundRefreshLimiter.setTimeBeforeNewExecution(backgroundRefreshTimeForScrolling);
                    display.asyncExec(new Runnable() {

                        public void run() {
                            backgroundRefreshLimiter.startIfExecutable();
                        }
                    });

                }
            }
        };
        vBar1.addSelectionListener(scrollListener);
        vBar2.addSelectionListener(scrollListener);
        vBar3.addSelectionListener(scrollListener);
    }

    private void addBackgroundRefreshLimiters(final Display display) {
        backgroundRefreshLimiter = new ExecutionLimiter(DEFAULT_TIME_BEFORE_NEW_BG_REFRESH, true) {

            @Override
            public void execute(final boolean isFinalExecution, Object data) {

                display.asyncExec(new Runnable() {

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
        backgroundRefreshLimiterForceRecalculate = new ExecutionLimiter(DEFAULT_TIME_BEFORE_NEW_BG_REFRESH, true) {

            @Override
            public void execute(final boolean isFinalExecution, Object data) {
                display.asyncExec(new Runnable() {

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

    private void addParentListeners(final UIManager uiManager, final ExternalDbMapUiProperties uiProperties) {
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

    private void createInputZoneWithTables(MapperModel mapperModel, UIManager uiManager, final Display display) {
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

            InputDataMapTableView dataMapTableView = uiManager.createNewInputTableView(previousControl, inputTable,
                    inputTablesZoneView);

            if (previousControl == null) {
                uiManager.updateDropDownJoinTypeForInputs();
            }

            previousControl = dataMapTableView;

        }
        inputTablesZoneView.setSize(inputTablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    /**
     * DOC amaumont Comment method "getCommonMinimizedStateOfTables".
     *
     * @param tables
     * @return new Boolean(true) if button state should be to minimize, else new Boolean(false)
     */
    private Boolean getMinimizedButtonState(List<? extends IDataMapTable> tables) {
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
        // this.dropTargetOperationListener.addControl(varsTableZoneView);

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
        // varsTableZoneView.initInsertionIndicator();
        //
        // // final Composite finalTablesZoneViewVars = tablesZoneViewVars;
        //
        // previousControl = null;
        // for (IDataMapTable abstractDataMapTable : mapperModel.getVarsDataMapTables()) {
        //
        // DataMapTableView dataMapTableView = new VarsDataMapTableView(varsTableZoneView, SWT.BORDER,
        // abstractDataMapTable, mapperManager);
        //
        // FormData formData = new FormData();
        // formData.left = new FormAttachment(0, 0);
        // formData.right = new FormAttachment(100, 0);
        // formData.top = new FormAttachment(previousControl);
        // dataMapTableView.setLayoutData(formData);
        // previousControl = dataMapTableView;
        // dataMapTableView.minimizeTable(abstractDataMapTable.isMinimized());
        // // dataMapTableView.registerStyledExpressionEditor(getTabFolderEditors().getStyledTextHandler());
        // // dataMapTableView.fillMinimumSize(false);
        // }

        varsTableZoneView.setSize(varsTableZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));
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
        sc3MSListener = new MouseScrolledListener(mapperManager.getUiManager(), sc3);
        outputTablesZoneView.initInsertionIndicator();

        previousControl = null;

        List<OutputTable> tables = mapperModel.getOutputDataMapTables();
        Boolean minimizeStateOfTables = getMinimizedButtonState(tables);
        if (minimizeStateOfTables != null) {
            outputsZone.getToolbar().setMinimizeButtonState(minimizeStateOfTables.booleanValue());
        }

        for (IDataMapTable abstractDataMapTable : tables) {

            OutputDataMapTableView dataMapTableView = uiManager.createNewOutputTableView(previousControl, abstractDataMapTable,
                    outputTablesZoneView);
            previousControl = dataMapTableView;
        }
        outputTablesZoneView.setSize(outputTablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    public MouseScrolledListener getInputMouseSrolledListener() {
        return this.sc1MSListener;
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
    }

    protected void updateBackground(boolean forceRecalculate, boolean antialias) {

        bgDrawableComposite.setAntialias(antialias);
        bgDrawableComposite.setForceRecalculate(forceRecalculate);
        backgroundRefresher.refreshBackground();

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
     * $Id: MapperUI.java 1744 2007-01-31 13:05:30Z amaumont $
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

    public ExecutionLimiter getBackgroundRefreshLimiter() {
        return this.backgroundRefreshLimiter;
    }

    public ExecutionLimiter getBackgroundRefreshLimiterForceRecalculate() {
        return this.backgroundRefreshLimiterForceRecalculate;
    }

    public Composite getMapperUIParent() {
        return this.mapperUIParent;
    }

    public TabFolderEditors getTabFolderEditors() {
        return this.tabFolderEditors;
    }

    private void onSashResized(Display display) {
        display.asyncExec(new Runnable() {

            public void run() {

                backgroundRefreshLimiterForceRecalculate.startIfExecutable();
            }
        });
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

    public Shell getShell() {
        return mapperUIParent.getShell();
    }

}
