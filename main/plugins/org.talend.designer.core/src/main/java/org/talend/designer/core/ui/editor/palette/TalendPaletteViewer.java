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
package org.talend.designer.core.ui.editor.palette;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.gef.ui.parts.PaletteViewerKeyHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.prefs.BackingStoreException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.threading.ExecutionLimiterImproved;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory.RecentlyUsedComponent;
import org.talend.designer.core.ui.preferences.PaletteSettingsPreferencePage;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 *
 */
public class TalendPaletteViewer extends PaletteViewer {

    private static final String TOOL_TIP = Messages.getString("TalendPaletteViewer.tooltipValue"); //$NON-NLS-1$

    private static final String SEARCH_COMPONENT = Messages.getString("TalendPaletteViewer.searchComponent"); //$NON-NLS-1$

    private static List<Text> filters = new ArrayList<Text>();

    private static Text setTextOnly;

    private static String currentFilterText;

    protected TalendDrawerEditPart favoritesEditPart;

    protected TalendDrawerEditPart recentlyUsedEditPart;

    protected TalendPaletteEditPartFactory paletteEditPartFactory;

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    private final ExecutionLimiterImproved expandLimiter = new ExecutionLimiterImproved(500, false) {

        ExpandPaletteRunnable runnable = null;

        @Override
        public void execute(final boolean isFinalExecution, Object data) {
            final Text text = (Text) data;
            if (runnable != null) {
                runnable.stopExpand();
            }
            filterPalette(text);
            final StringBuffer strBuffer = new StringBuffer();
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    strBuffer.append(text.getText());
                }
            });
            if (!strBuffer.toString().equals("")) { //$NON-NLS-1$
                runnable = new ExpandPaletteRunnable();
                runnable.run();
            }
        }
    };

    public TalendPaletteViewer(EditDomain graphicalViewerDomain, TalendPaletteCSSStyleSetting cssStyleSetting) {
        setEditDomain(graphicalViewerDomain);
        setKeyHandler(new PaletteViewerKeyHandler(this));
        this.cssStyleSetting = cssStyleSetting;
        paletteEditPartFactory = new TalendPaletteEditPartFactory(cssStyleSetting);
        setEditPartFactory(paletteEditPartFactory);
        this.enableVerticalScrollbar(true);
        setupPreferences();
    }

    private void setupPreferences() {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        final String firstTimeRunning = "org.talend.designer.core.ui.editor.palette.TalendPaletteViewer.isFirstTimeRunning"; //$NON-NLS-1$
        boolean isFirstTimeRunning = true;
        if (preferenceStore != null) {
            String value = preferenceStore.getString(firstTimeRunning);
            if (StringUtils.isNotEmpty(value)) {
                isFirstTimeRunning = Boolean.valueOf(value);
            }
        }
        if (isFirstTimeRunning) {
            PaletteViewerPreferences paletteViewerPreferences = this.getPaletteViewerPreferences();
            if (paletteViewerPreferences != null) {
                paletteViewerPreferences.setAutoCollapseSetting(PaletteViewerPreferences.COLLAPSE_NEVER);
                paletteViewerPreferences.getAutoCollapseSetting();
                if (preferenceStore != null) {
                    preferenceStore.setValue(firstTimeRunning, Boolean.FALSE.toString());
                    try {
                        InstanceScope.INSTANCE.getNode(DesignerPlugin.ID).flush();
                    } catch (BackingStoreException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    /**
     * FIXME ggu
     *
     * Because the method createControl is final in super calss ScrollingGraphicalViewer,
     *
     * Then, use the reflection function to move to class TalendPaletteViewerProvider.
     */
    // @Override
    // public Control createControl(Composite parent) {
    // FigureCanvas canvas = new TalendFigureCanvas(parent, getLightweightSystem(), this);
    // setControl(canvas);
    // installRootFigure(); // change the parent method to "protected".
    // return canvas;
    // }
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.ScrollingGraphicalViewer#creatToolControl(org.eclipse.swt.widgets.Composite)
     */
    public Control creatToolControl(Composite parent) {
        Composite container = new Composite(parent, SWT.BORDER);
        // GridLayout layout = new GridLayout(3, false);
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        container.setLayout(layout);
        final Text text = new Text(container, SWT.NONE);

        GridData textGridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        textGridData.horizontalIndent = 0;
        textGridData.verticalIndent = 0;
        text.setLayoutData(textGridData);
        filters.add(text);
        initFilterTextControl(text);

        ToolBar toolbar = new ToolBar(container, SWT.NONE);
        Color searchButtonForgroundColor = cssStyleSetting.getSearchButtonBackgroundColor();
        if (searchButtonForgroundColor != null) {
            toolbar.setBackground(searchButtonForgroundColor);
        }
        GridLayout toolbarLayout = new GridLayout();
        toolbarLayout.marginLeft = 0;
        toolbarLayout.marginRight = 0;
        toolbarLayout.marginTop = 0;
        toolbarLayout.marginBottom = 0;
        toolbarLayout.marginHeight = 0;
        toolbarLayout.marginWidth = 0;
        toolbar.setLayout(toolbarLayout);

        Image findImage = cssStyleSetting.getSearchButtonImage();
        if (findImage == null) {
            findImage = ImageProvider.getImage(EImage.FIND_ICON);
        }

        ToolItem findItem = new ToolItem(toolbar, SWT.NONE);
        findItem.setImage(findImage);
        findItem.setToolTipText("Search");
        findItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                startFiltering(text);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        // Image clearImage = ImageProvider.getImage(ECoreImage.PALETTE_CLEAR_ICON);
        // final ToolItem clearItem = new ToolItem(toolbar, SWT.NONE);
        // clearItem.setImage(clearImage);
        // clearItem.addSelectionListener(new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        //                text.setText(""); //$NON-NLS-1$
        // // Reset to default palette
        // startFiltering(text);
        // }
        // });
        return container;
    }

    /**
     * yzhang Comment method "initFilterTextControl".
     *
     * @param control
     */
    private void initFilterTextControl(Text filterText) {
        if (currentFilterText != null) {
            filterText.setText(currentFilterText);
        } else {
            filterText.setText(SEARCH_COMPONENT);
        }

        filterText.setToolTipText(TOOL_TIP);
        configListeners(filterText);

    }

    /**
     * yzhang Comment method "configListeners".
     *
     * @param text
     */
    private void configListeners(final Text text) {
        text.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {

            }

            @Override
            public void mouseDown(MouseEvent e) {
                if (text.getText().equals(SEARCH_COMPONENT)) {
                    text.setText(""); //$NON-NLS-1$
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {

            }
        });

        text.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                //                text.setText(""); //$NON-NLS-1$
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (text.getText() == "") { //$NON-NLS-1$
                    text.setText(SEARCH_COMPONENT);
                }
            }
        });

        text.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                startFiltering(text);
            }

        });
    }

    private void startFiltering(final Text text) {
        String keyword = text.getText();
        if (!SEARCH_COMPONENT.equals(keyword)) {
            expandLimiter.resetTimer();
            expandLimiter.startIfExecutable(true, text);
        }
    }

    /**
     * yzhang TalendPaletteViewer class global comment. Detailled comment
     */
    private class ExpandPaletteRunnable implements Runnable {

        private volatile boolean stop;

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Display display = Display.getDefault();
            if (display == null) {
                return;
            }
            display.syncExec(new Runnable() {

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    List children = ComponentPaletteUtilities.getPaletteRoot().getChildren();
                    int counter = 0;
                    for (Object obj : children) {
                        if (stop || counter > 5) {
                            return;
                        }
                        if (obj instanceof PaletteDrawer) {
                            ((PaletteDrawer) obj).setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
                            counter++;
                            PaletteDrawer subDrawer = getSubDrawer((PaletteDrawer) obj);
                            if (subDrawer != null) {
                                subDrawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
                                counter++;
                            }
                        }

                    }
                }
            });

        }

        public void stopExpand() {
            stop = true;
        }
    }

    private PaletteDrawer getSubDrawer(PaletteDrawer parent) {
        for (Object obj : parent.getChildren()) {
            if (obj instanceof PaletteDrawer) {
                return (PaletteDrawer) obj;
            }
        }
        return null;
    }

    private void filterPalette(final Text text) {
        final List<Text> disposed = new ArrayList<Text>();
        final Display display = DisplayUtils.getDisplay();
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                TalendPaletteViewer.this.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_APPSTARTING));
                prepareFilterPalette(text, disposed);
            }
        });
        if (!currentFilterText.equals(SEARCH_COMPONENT)) {
            ComponentPaletteUtilities.filterPalette(currentFilterText.trim());
        }

        filters.removeAll(disposed);
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                TalendPaletteViewer.this.setCursor(null);
            }
        });
    }

    protected boolean prepareFilterPalette(Text text, List<Text> disposed) {
        boolean canExecute = true;
        if (setTextOnly == text) {
            return false;
        }

        for (Text otherText : filters) {

            if (text == otherText) {
                continue;
            }

            if (otherText.isDisposed()) {
                disposed.add(otherText);
                continue;
            }
            setTextOnly = otherText;
            otherText.setText(text.getText());
            setTextOnly = null;
        }

        currentFilterText = text.getText();
        return canExecute;
    }

    // expose getLightweightSystem to public
    public LightweightSystem getLightweightSys() {
        return getLightweightSystem();
    }

    public void setFigureCanvas(FigureCanvas canvas) {
        setControl(canvas);
        hookRootFigure();
    }

    public void addFavoritesComponent(CombinedTemplateCreationEntry component) {

        if (favoritesEditPart != null) {
            List children = favoritesEditPart.getChildren();
            int insertIndex = 0;
            boolean alreadyExist = false;
            if (children != null) {
                for (; insertIndex < children.size(); insertIndex++) {
                    TalendEntryEditPart entryEditPart = (TalendEntryEditPart) children.get(insertIndex);
                    CombinedTemplateCreationEntry entryModule = (CombinedTemplateCreationEntry) entryEditPart.getModel();
                    int compareResult = entryModule.getLabel().toLowerCase().compareTo(component.getLabel().toLowerCase());
                    if (0 == compareResult) {
                        alreadyExist = true;
                        break;
                    }
                    if (0 < compareResult) {
                        break;
                    }
                }
            }

            if (!alreadyExist) {
                TalendEditorPaletteFactory.addNewFavoriteIntoPreference(component.getLabel());
                CombinedTemplateCreationEntry newFavorite = TalendEditorPaletteFactory.createEntryFrom(component);
                newFavorite.setParent(favoritesEditPart.getDrawer());
                EditPart child = favoritesEditPart.createChild(newFavorite);
                PaletteDrawer paletteDrawer = favoritesEditPart.getDrawer();
                if (paletteDrawer != null) {
                    paletteDrawer.add(insertIndex, newFavorite);
                }
                // needn't use this, or will cause problem
                // favoritesEditPart.addChild(child, insertIndex);
            }
        }

    }

    public void removeFavoritesComponent(CombinedTemplateCreationEntry component) {

        if (favoritesEditPart != null) {
            TalendEditorPaletteFactory.deleteFavoriteFromPreference(component.getLabel());
            List children = favoritesEditPart.getChildren();
            if (children != null) {
                for (Object obj : children) {
                    TalendEntryEditPart entryEditPart = (TalendEntryEditPart) obj;
                    CombinedTemplateCreationEntry entryModule = (CombinedTemplateCreationEntry) entryEditPart.getModel();
                    if (entryModule.getLabel().equals(component.getLabel())) {
                        PaletteDrawer paletteDrawer = favoritesEditPart.getDrawer();
                        if (paletteDrawer != null) {
                            paletteDrawer.remove(entryModule);
                        }
                        // needn't use this
                        // favoritesEditPart.removeChild(entryEditPart);
                        break;
                    }
                }
            }
        }

    }

    public void addRecentlyUsedComponent(IComponent component) {
        IComponent delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        EComponentType componentType = component.getComponentType();
        if (componentType == EComponentType.JOBLET_INPUT_OUTPUT || componentType == EComponentType.JOBLET_TRIGGER) {
            return;
        }
        if (recentlyUsedEditPart != null) {
            List children = recentlyUsedEditPart.getChildren();
            int insertIndex = 0;
            boolean alreadyExist = false;
            if (children != null) {
                for (; insertIndex < children.size(); insertIndex++) {
                    TalendEntryEditPart entryEditPart = (TalendEntryEditPart) children.get(insertIndex);
                    TalendCombinedTemplateCreationEntry entryModule = (TalendCombinedTemplateCreationEntry) entryEditPart
                            .getModel();
                    int compareResult = entryModule.getComponentName().compareTo(delegateComponent.getName());
                    if (0 == compareResult) {
                        alreadyExist = true;
                        break;
                    }
                    // if (0 < compareResult) {
                    // break;
                    // }
                }
            }

            // if already exist, remove first
            if (alreadyExist) {
                if (0 == insertIndex) {
                    return;
                }
                TalendEntryEditPart entryEditPart = (TalendEntryEditPart) children.get(insertIndex);
                CombinedTemplateCreationEntry entryModule = (CombinedTemplateCreationEntry) entryEditPart.getModel();
                removeRecentlyUsedComponent(entryModule);
            }

            // if (!alreadyExist) {
            TalendCombinedTemplateCreationEntry newRecently = TalendEditorPaletteFactory.createEntryFrom(component);
            newRecently.setTimestemp(new Date());
            newRecently.setParent(recentlyUsedEditPart.getDrawer());
            EditPart child = recentlyUsedEditPart.createChild(newRecently);
            PaletteDrawer paletteDrawer = recentlyUsedEditPart.getDrawer();
            if (paletteDrawer != null) {
                // paletteDrawer.add(insertIndex, newRecently);
                paletteDrawer.add(0, newRecently);
            }
            // reobtain
            children = recentlyUsedEditPart.getChildren();
            if (children != null) {
                int size = children.size();
                final int recentlyUsedSize = PaletteSettingsPreferencePage.getPaletteRencentlyUsedListSize();
                if (recentlyUsedSize < size) {
                    TalendEntryEditPart entryEditPart = (TalendEntryEditPart) children.get(recentlyUsedSize);
                    CombinedTemplateCreationEntry entryModule = (CombinedTemplateCreationEntry) entryEditPart.getModel();
                    removeRecentlyUsedComponent(entryModule);
                }
            }
            // needn't use this, or will cause problem
            // recentlyUsedEditPart.addChild(child, insertIndex);
            // }

            refreshRecentlyUsedComponentToReference();
        }

    }

    protected void refreshRecentlyUsedComponentToReference() {
        if (recentlyUsedEditPart == null) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (recentlyUsedEditPart == null) {
                    return;
                }
                List children = recentlyUsedEditPart.getModelChildren();
                if (children == null) {
                    return;
                }
                List<RecentlyUsedComponent> recentlyUsedList = new ArrayList<RecentlyUsedComponent>();

                for (Object obj : children) {
                    TalendCombinedTemplateCreationEntry entryModule = (TalendCombinedTemplateCreationEntry) obj;
                    RecentlyUsedComponent recentlyUsedComponent = new RecentlyUsedComponent();
                    recentlyUsedComponent.setName(entryModule.getComponentName());
                    recentlyUsedComponent.setTimestamp(entryModule.getTimestemp());
                    recentlyUsedList.add(recentlyUsedComponent);
                }

                TalendEditorPaletteFactory.storeRecentlyUsedList(recentlyUsedList);
            }
        }).start();

    }

    public void removeRecentlyUsedComponent(CombinedTemplateCreationEntry component) {
        if (recentlyUsedEditPart != null) {
            List children = recentlyUsedEditPart.getChildren();
            if (children != null) {
                for (Object obj : children) {
                    TalendEntryEditPart entryEditPart = (TalendEntryEditPart) obj;
                    CombinedTemplateCreationEntry entryModule = (CombinedTemplateCreationEntry) entryEditPart.getModel();
                    String entryLabel = component.getLabel();
                    if (component instanceof TalendCombinedTemplateCreationEntry) {
                        // entryLabel = ((TalendCombinedTemplateCreationEntry) component).getComponentName();
                    }
                    if (entryModule.getLabel().equals(entryLabel)) {
                        PaletteDrawer paletteDrawer = recentlyUsedEditPart.getDrawer();
                        if (paletteDrawer != null) {
                            paletteDrawer.remove(entryModule);
                        }
                        // needn't use this
                        // favoritesEditPart.removeChild(entryEditPart);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Getter for favoritesEditPart.
     *
     * @return the favoritesEditPart
     */
    public TalendDrawerEditPart getFavoritesEditPart() {
        return this.favoritesEditPart;
    }

    /**
     * Sets the favoritesEditPart.
     *
     * @param favoritesEditPart the favoritesEditPart to set
     */
    public void setFavoritesEditPart(TalendDrawerEditPart favoritesEditPart) {
        this.favoritesEditPart = favoritesEditPart;
    }

    /**
     * Getter for recentlyUsedEditPart.
     *
     * @return the recentlyUsedEditPart
     */
    public TalendDrawerEditPart getRecentlyUsedEditPart() {
        return this.recentlyUsedEditPart;
    }

    /**
     * Sets the recentlyUsedEditPart.
     *
     * @param recentlyUsedEditPart the recentlyUsedEditPart to set
     */
    public void setRecentlyUsedEditPart(TalendDrawerEditPart recentlyUsedEditPart) {
        this.recentlyUsedEditPart = recentlyUsedEditPart;
    }

}
