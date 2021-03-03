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
package org.talend.designer.rowgenerator.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorUiProperties;
import org.talend.designer.rowgenerator.managers.RowGeneratorManager;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.designer.rowgenerator.ui.editor.MetadataTableEditorExt;
import org.talend.designer.rowgenerator.ui.editor.MetadataTableEditorViewExt;
import org.talend.designer.rowgenerator.ui.footer.FooterComposite;
import org.talend.designer.rowgenerator.ui.tabs.TabFolderEditors;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: RowGeneratorUI.java,v 1.23 2007/01/31 05:20:52 pub Exp $
 *
 */
public class RowGeneratorUI {

    private SashForm datasFlowViewSashForm;

    private final RowGeneratorManager generatorManager;

    private Composite rowGenUIParent;

    private Thread threadToEvaluatePerformance;

    protected long lastDragAndMoveTime;

    private SashForm mainSashForm;

    private TabFolderEditors tabFolderEditors;

    private boolean inputReadOnly = false;

    private MetadataTableEditorViewExt dataTableView;

    private MetadataTableEditorExt metadataTableEditor;

    private final RowGeneratorComponent externalNode;

    private final FunctionManagerExt functionManager;

    private UIManager uiManager;

    private Map<String, String> changedNameColumns = new HashMap<String, String>();

    public Map<String, String> getChangedNameColumns() {
        return this.changedNameColumns;
    }

    public RowGeneratorUI(Composite parent, RowGeneratorManager generatorManager) {
        super();
        this.generatorManager = generatorManager;
        this.generatorManager.getUiManager().setGeneratorUI(this);
        externalNode = generatorManager.getRowGeneratorComponent();
        // add listeners.
        this.rowGenUIParent = parent;
        functionManager = new FunctionManagerExt();
    }

    /**
     * yzhang Comment method "init".
     *
     * @param fromDialog
     */
    public void init(boolean fromDialog) {
        uiManager = generatorManager.getUiManager();
        final ExternalRowGeneratorUiProperties uiProperties = uiManager.getUiProperties();
        addParentListeners(uiManager, uiProperties);

        final Display display = rowGenUIParent.getDisplay();

        GridLayout parentLayout = new GridLayout(1, true);
        rowGenUIParent.setLayout(parentLayout);

        addKeyListener(uiManager, display);

        mainSashForm = new SashForm(rowGenUIParent, SWT.SMOOTH | SWT.VERTICAL);
        GridData mainSashFormGridData = new GridData(GridData.FILL_BOTH);
        mainSashForm.setLayoutData(mainSashFormGridData);

        datasFlowViewSashForm = new SashForm(mainSashForm, SWT.SMOOTH | SWT.HORIZONTAL | SWT.BORDER);
        datasFlowViewSashForm.setLayoutData(mainSashFormGridData);
        datasFlowViewSashForm.setBackgroundMode(SWT.INHERIT_FORCE);

        initBackgroundComponents();

        // if (WindowSystem.isGTK()) {
        // datasFlowViewSashForm.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
        // }
        /* Create Schema Table Editor */
        createSchemaComposite();

        /* Create the tabs */
        tabFolderEditors = new TabFolderEditors(mainSashForm, SWT.BORDER, externalNode, dataTableView);
        tabFolderEditors.setRowGeneratorUI(this);
        tabFolderEditors.getProcessPreview().refreshTablePreview(outputMetaTable.getListColumns(), null, true);

        if (!fromDialog) {
            new FooterComposite(this.rowGenUIParent, SWT.NONE, generatorManager);
        }
        dataTableView.getTable().addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Java)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFunParameter((Table) e.getSource());
            }

        });

        dataTableView.getTable().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                dataTableView.getTableViewerCreator().refresh();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
            }

        });
    }

    /**
     * qzhang Comment method "updateFunParameter".
     *
     * @param table
     */
    public void updateFunParameter(Table table) {
        if (table.getSelection().length < 1) {
            return;
        }
        TableItem item = table.getSelection()[0];
        if (item.getData() != null) {
            Function fun = ((MetadataColumnExt) item.getData()).getFunction();
            if (fun != null) {
                // tabFolderEditors.createTableView(fun);
                tabFolderEditors.getParameterEditor().update((MetadataColumnExt) item.getData());
            }
        }
    }

    private void initBackgroundComponents() {

    }

    private IMetadataTable outputMetaTable;

    /**
     * qzhang Comment method "createSchemaComposite".
     */
    private void createSchemaComposite() {
        outputMetaTable = externalNode.getMetadataList().get(0);
        uiManager.convert(externalNode, outputMetaTable, functionManager);
        metadataTableEditor = new MetadataTableEditorExt(outputMetaTable, "", this.functionManager); //$NON-NLS-1$
        metadataTableEditor.setRowGenUI(this);

        inputReadOnly = this.externalNode.getProcess().isReadOnly();
        if (this.externalNode.getOriginalNode().getJobletNode() != null) {
            inputReadOnly = this.externalNode.getOriginalNode().isReadOnly();
        } else if (this.externalNode.getOriginalNode().getJunitNode() != null) {
            inputReadOnly = true;
        }

        dataTableView = new MetadataTableEditorViewExt(datasFlowViewSashForm, SWT.BORDER, metadataTableEditor, inputReadOnly,
                !inputReadOnly, externalNode, functionManager);
        dataTableView.getExtendedTableViewer().setCommandStack(generatorManager.getCommandStack());
        dataTableView.setGeneratorUI(this);
        dataTableView.setShowDbTypeColumn(true, true, false);
        dataTableView.setShowDbColumnName(false, false);
        metadataTableEditor.setModifiedBeanListenable(dataTableView.getTableViewerCreator());
        // see bug 7471,record the modification of the column value.
        metadataTableEditor.addModifiedBeanListener(new IModifiedBeanListener<IMetadataColumn>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<IMetadataColumn> event) {
                if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())) {
                    IMetadataColumn modifiedObject = event.bean;
                    if (modifiedObject != null) {
                        String originalLabel = changedNameColumns.get(modifiedObject);
                        if (originalLabel == null) {
                            changedNameColumns.put(modifiedObject.getLabel(), (String) event.previousValue);
                        }
                    }
                }
            }

        });
        // dataTableView.
        // resize all the columns but not the table
        for (int i = 0; i < dataTableView.getTable().getColumnCount(); i++) {
            dataTableView.getTable().getColumn(i).pack();
        }
        dataTableView.getTable().getColumn(0).setWidth(0);

    }

    /**
     * qzhang Comment method "addKeyListener".
     *
     * @param uiManager
     * @param display
     */
    private void addKeyListener(final UIManager uiManager, final Display display) {

        final Listener listener = new Listener() {

            @Override
            public void handleEvent(Event event) {

                if (event.type == SWT.KeyUp || event.type == SWT.KeyDown) {
                    boolean isPressed = event.type == SWT.KeyDown ? true : false;
                    if (event.keyCode == SWT.CTRL) {
                        uiManager.setCtrlPressed(isPressed);
                    }
                    if (event.keyCode == SWT.SHIFT) {
                        uiManager.setShiftPressed(isPressed);

                    }
                }
            }

        };
        display.addFilter(SWT.KeyUp, listener);
        display.addFilter(SWT.KeyDown, listener);
        rowGenUIParent.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                display.removeFilter(SWT.KeyUp, listener);
                display.removeFilter(SWT.KeyDown, listener);
                release();
            }
        });
    }

    /**
     * qzhang Comment method "addParentListeners".
     *
     * @param uiManager
     * @param uiProperties
     */
    private void addParentListeners(final UIManager uiManager, final ExternalRowGeneratorUiProperties uiProperties) {
        rowGenUIParent.addListener(SWT.Close, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (uiManager.getRowGenResponse() == SWT.NONE) {
                    uiManager.closeRowGenerator(SWT.CANCEL, false);
                }
            }

        });
        rowGenUIParent.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                updateBackground(false, true);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }

        });

        // store size if not maximized
        if (rowGenUIParent instanceof Shell) {
            ((Shell) rowGenUIParent).addControlListener(new ControlListener() {

                @Override
                public void controlMoved(ControlEvent e) {

                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (!((Shell) e.getSource()).getMaximized()) {
                        ExternalRowGeneratorUiProperties.setBoundsRowGen(((Shell) e.getSource()).getBounds());
                    }
                }
            });
        }
    }

    /**
     * qzhang Comment method "updateBackground".
     *
     * @param b
     * @param c
     */
    protected void updateBackground(boolean b, boolean c) {

    }

    /**
     * qzhang Comment method "release".
     */
    protected void release() {
        if (threadToEvaluatePerformance != null) {
            threadToEvaluatePerformance.interrupt();
        }
    }

    public Composite getRowGenUIParent() {
        return this.rowGenUIParent;
    }

    public void setRowGenUIParent(Composite rowGenUIParent) {
        this.rowGenUIParent = rowGenUIParent;
    }

    public TabFolderEditors getTabFolderEditors() {
        return this.tabFolderEditors;
    }

    public MetadataTableEditorViewExt getDataTableView() {
        return this.dataTableView;
    }

    public RowGeneratorManager getGeneratorManager() {
        return this.generatorManager;
    }

    public SashForm getDatasFlowViewSashForm() {
        return this.datasFlowViewSashForm;
    }

    public SashForm getMainSashForm() {
        return this.mainSashForm;
    }


    /**
     * Getter for functionManager.
     * @return the functionManager
     */
    public FunctionManagerExt getFunctionManager() {
        return this.functionManager;
    }

}
