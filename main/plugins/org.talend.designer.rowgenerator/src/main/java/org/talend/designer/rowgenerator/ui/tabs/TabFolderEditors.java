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
package org.talend.designer.rowgenerator.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.ui.RowGeneratorUI;
import org.talend.designer.rowgenerator.ui.editor.MetadataTableEditorViewExt;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: TabFolderEditors.java,v 1.19 2007/01/31 05:20:52 pub Exp $
 */
public class TabFolderEditors extends CTabFolder {

    public static final String PREVIEW_NUMBER_ROWS = "10"; //$NON-NLS-1$

    private TabFolderEditors tabFolderEditors;

    protected int lastSelectedTab;

    private FunParaTableView2 inputParameterEditor;

    public static final int INDEX_TAB_METADATA_EDITOR = 0;

    public static final int INDEX_TAB_EXPRESSION_EDITOR = 1;

    protected static final int VERTICAL_SPACING_FORM = 0;

    protected static final int WIDTH_BUTTON_PIXEL = 100;

    protected static final int HEIGHT_BUTTON_PIXEL = 30;

    private Button refreshButton;

    private ShadowProcessPreview processPreview;

    private RowGeneratorUI generatorUI;

    private RowGeneratorComponent component;

    private MetadataTableEditorViewExt genTableEditor2;

    private SashForm inOutMetaEditorContainer;

    public TabFolderEditors(Composite parent, int style, RowGeneratorComponent component,
            MetadataTableEditorViewExt genTableEditor2) {
        super(parent, style);
        this.genTableEditor2 = genTableEditor2;
        this.component = component;
        tabFolderEditors = this;
        setSimple(false);
        setLayout(new GridLayout());
        setLayoutData(new GridData(GridData.FILL_BOTH));
        createComponents();
    }

    /**
     * qzhang Comment method "createComponents".
     */
    private void createComponents() {

        CTabItem item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.FunParamTab.TitleText")); //$NON-NLS-1$
        inOutMetaEditorContainer = new SashForm(tabFolderEditors, SWT.SMOOTH | SWT.HORIZONTAL | SWT.SHADOW_OUT);
        inOutMetaEditorContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
        item.setControl(inOutMetaEditorContainer);

        createTableView();

        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.PreviewTab.TitleText")); //$NON-NLS-1$
        Composite composite = new Composite(tabFolderEditors, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createPreview(composite, 700, 210);
        item.setControl(composite);

        tabFolderEditors.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                lastSelectedTab = tabFolderEditors.getSelectionIndex();
            }
        });
        tabFolderEditors.setSelection(0);
    }

    private Text rowText;

    /**
     * qzhang Comment method "createPreview".
     *
     * @return
     */
    private void createPreview(final Composite parent, final int width, int height) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        // gridLayout.marginBottom = 0;
        // gridLayout.marginLeft = 0;
        // gridLayout.marginRight = 0;
        // gridLayout.marginTop = 0;
        // gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 2;
        gridLayout.marginWidth = 2;
        parent.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);

        Composite previewHeader = new Composite(parent, SWT.NONE);
        height = height - HEIGHT_BUTTON_PIXEL - 15;

        previewHeader.setLayout(gridLayout);
        previewHeader.setLayoutData(gridData);

        // row number text.

        Composite header = new Composite(previewHeader, SWT.NONE);
        GridLayout fill = new GridLayout();
        fill.numColumns = 3;
        fill.marginBottom = 0;
        fill.marginHeight = 0;
        fill.marginLeft = 0;
        fill.marginRight = 0;
        fill.marginTop = 0;
        fill.marginWidth = 0;

        header.setLayout(fill);
        rowText = new Text(header, SWT.BORDER);
        // String number = component.getNumber();
        // if (number == null || "".equals(number)) {
        // number = "10";
        // }
        rowText.setText("10"); //$NON-NLS-1$
        GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(rowText);

        // rowText.getBounds().width = 100;
        rowText.setRedraw(true);

        Label rowNum = new Label(header, SWT.NONE);
        rowNum.getBounds().height = HEIGHT_BUTTON_PIXEL;
        rowNum.setText(Messages.getString("TabFolderEditors.RowNum.LabelText")); //$NON-NLS-1$
        rowNum.setAlignment(SWT.CENTER);

        refreshButton = new Button(header, SWT.NONE);
        refreshButton.setText(Messages.getString("TabFolderEditors.PreviewButton.Label")); //$NON-NLS-1$
        refreshButton.setSize(WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        processPreview = new ShadowProcessPreview(previewHeader, width, height - 10);
        processPreview.newTablePreview();

        refreshButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                if (!refreshButton.getText().equals(Messages.getString("TabFolderEditors.WaitButton.Label"))) { //$NON-NLS-1$
                    refreshButton.setText(Messages.getString("TabFolderEditors.WaitButton.Label")); //$NON-NLS-1$
                    refreshPreview(rowText.getText());
                } else {
                    refreshButton.setText(Messages.getString("TabFolderEditors.PreviewButton.Label")); //$NON-NLS-1$
                }
            }
        });

    }

    /**
     * qzhang Comment method "refreshPreview".
     */
    public void refreshPreview(String number) {
        MetadataTableEditorViewExt editor2 = (MetadataTableEditorViewExt) generatorUI.getDataTableView();
        processPreview.refreshTablePreview(editor2.getMetadataTableEditor().getMetadataColumnList(), getItemsByRunJob(number),
                true);
    }

    /**
     * qzhang Comment method "getItemsByRunJob".
     */
    public List<List<String>> getItemsByRunJob(String number) {
        List<List<String>> items = new ArrayList<List<String>>();
        UIManager uiManager = generatorUI.getGeneratorManager().getUiManager();
        FunParaTableView2 editor = uiManager.getGeneratorUI().getTabFolderEditors().getParameterEditor();
        editor.notifyOkPressed();
        items = generatorUI.getGeneratorManager().getRowGeneratorComponent().getCodeGenMain().run(refreshButton, number);
        return items;
    }

    /**
     * qzhang Comment method "createTableView".
     *
     * @param inEditorContainer
     */
    public void createTableView() {
        inputParameterEditor = new FunParaTableView2(inOutMetaEditorContainer, SWT.BORDER, genTableEditor2, component);
        inOutMetaEditorContainer.setData(inputParameterEditor);
        inputParameterEditor.setTitle(""); //$NON-NLS-1$
    }

    public FunParaTableView2 getParameterEditor() {
        return this.inputParameterEditor;
    }

    public ShadowProcessPreview getProcessPreview() {
        return this.processPreview;
    }

    /**
     * qzhang Comment method "setRowGeneratorUI".
     *
     * @param generatorUI
     */
    public void setRowGeneratorUI(RowGeneratorUI generatorUI) {
        this.generatorUI = generatorUI;
    }

    public RowGeneratorComponent getComponent() {
        return this.component;
    }

    public String getRowText() {
        return this.rowText.getText();
    }

}
