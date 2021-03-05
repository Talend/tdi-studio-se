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
package org.talend.repository.ui.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.preference.provider.TalendPaletteLabelProvider;
import org.talend.core.ui.component.preference.provider.TalendPaletteTreeProvider;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * DOC qwei class global comment. Detailled comment
 *
 * @deprecated see ProjectSettingDialog
 */
@Deprecated
public class PaletteSettingsDialog extends Dialog {

    private static final String FAMILY_SPEARATOR = "--FAMILY--"; //$NON-NLS-1$

    private static final String DIALOG_TITLE = "Palette Settings"; //$NON-NLS-1$

    private static final boolean RESTORE = true;

    private TreeViewer hiddenViewer, displayViewer;

    private Project project;

    private Button leftButton;

    private Button rightButton;

    private ThreeCompositesSashForm compositesSachForm;

    // <name:visiblility>
    private Map<String, Boolean> statusBackup = new HashMap<String, Boolean>();

    /**
     * DOC qwei PaletteSettingsDialog constructor comment.
     *
     * @param parentShell
     */

    public PaletteSettingsDialog(Shell parentShell, Project pro) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);

        this.project = pro;
        List<ComponentSetting> c = getComponentsFromProject();
        for (ComponentSetting componentSetting : c) {
            statusBackup.put(componentSetting.getFamily() + FAMILY_SPEARATOR + componentSetting.getName(),
                    !componentSetting.isHidden());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DIALOG_TITLE);
        newShell.setSize(800, 600);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        compositesSachForm = new ThreeCompositesSashForm(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        addTreeViewer(compositesSachForm);
        // addFileds(composite);
        return composite;
    }

    private PaletteRoot getViewerInput() {
        IComponentsFactory components = ComponentsFactoryProvider.getInstance();
        PaletteRoot paletteRoot = CoreUIPlugin.getDefault().getDesignerCoreUIService().getAllNodeStructure(components);
        return paletteRoot;
    }

    /**
     * qwei Comment method "addViewer".
     *
     * @param parent
     */
    private void addTreeViewer(ThreeCompositesSashForm parent) {

        hiddenViewer = new TreeViewer(parent.getLeftComposite());
        hiddenViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        hiddenViewer.setContentProvider(new TalendPaletteTreeProvider());
        hiddenViewer.setLabelProvider(new TalendPaletteLabelProvider());
        hiddenViewer.addFilter(getFilterForComponent(false));

        hiddenViewer.expandToLevel(2);
        hiddenViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                rightButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        createButtons(parent.getMidComposite());
        displayViewer = new TreeViewer(parent.getRightComposite());
        displayViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        displayViewer.setContentProvider(new TalendPaletteTreeProvider());
        displayViewer.setLabelProvider(new TalendPaletteLabelProvider());
        displayViewer.addFilter(getFilterForComponent(true));

        displayViewer.expandToLevel(2);
        displayViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                leftButton.setEnabled(!event.getSelection().isEmpty());
            }
        });

        PaletteRoot input = getViewerInput();
        hiddenViewer.setInput(input);
        displayViewer.setInput(input);
    }

    /**
     * DOC qwei Comment method "getFilterForHiddenComponent". isVisible false for left viewer; true for right viewer
     *
     * @return
     */
    private ViewerFilter getFilterForComponent(final boolean isVisible) {
        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                PaletteEntry entry = (PaletteEntry) element;

                if (entry instanceof PaletteContainer) {
                    return isFolderVisible((PaletteContainer) entry, isVisible);
                }

                if (isVisible) {
                    return isComponentVisible(entry);
                } else {
                    return !isComponentVisible(entry);
                }
            }
        };
        return filter;
    }

    /**
     * Check if this folder needs to be showed. *
     *
     * @param entry
     * @param isVisible
     * @return
     */
    protected boolean isFolderVisible(PaletteContainer container, boolean isVisible) {
        for (Iterator iterator = container.getChildren().iterator(); iterator.hasNext();) {
            PaletteEntry entry = (PaletteEntry) iterator.next();
            if (entry instanceof PaletteContainer) {
                boolean display = isFolderVisible((PaletteContainer) entry, isVisible);
                if (display) {
                    return true;
                } else {
                    continue;
                }
            } else {
                if (isVisible) {
                    if (isComponentVisible(entry)) {
                        return true;
                    }
                } else {
                    if (!isComponentVisible(entry)) {
                        return true;
                    }
                }
                continue;
            }
        }

        return false;
    }

    private void refreshViewer() {
        hiddenViewer.refresh();
        displayViewer.refresh();
    }

    private void createButtons(Composite parent) {
        Label label1 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 18).applyTo(label1);
        Composite buttonComposite = new Composite(compositesSachForm.getMidComposite(), SWT.BORDER);
        Label label2 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 0).applyTo(label2);

        GridLayout gridLayout = new GridLayout(1, true);
        buttonComposite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        // gridData.verticalAlignment = GridData.CENTER;
        buttonComposite.setLayoutData(gridData);

        Composite buttonComposite2 = new Composite(buttonComposite, SWT.NONE);

        gridLayout = new GridLayout(1, true);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        buttonComposite2.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalAlignment = GridData.CENTER;
        buttonComposite2.setLayoutData(gridData);
        rightButton = new Button(buttonComposite2, SWT.NONE);
        rightButton.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));
        rightButton.setToolTipText(""); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(rightButton);
        rightButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(hiddenViewer.getSelection(), true);
                getButton(IDialogConstants.OK_ID).setEnabled(true);

            }
        });
        leftButton = new Button(buttonComposite2, SWT.NONE);
        leftButton.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        leftButton.setToolTipText(""); //$NON-NLS-1$
        gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        leftButton.setLayoutData(gridData);
        leftButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(displayViewer.getSelection(), false);
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }
        });

        rightButton.setEnabled(false);
        leftButton.setEnabled(false);

    }

    /**
     * set the selected component as visible or not
     *
     * @param selection
     * @param b
     */
    protected void setComponentVisible(ISelection selection, boolean visible) {
        IStructuredSelection sel = (IStructuredSelection) selection;
        Set<String> names = new HashSet<String>();

        for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
            PaletteEntry entry = (PaletteEntry) iterator.next();
            retreiveAllEntry(names, entry);
        }

        for (String string : names) {
            setComponentVisible(string, visible, !RESTORE);
        }

        refreshViewer();
    }

    private void retreiveAllEntry(Set<String> list, PaletteEntry entry) {
        if (entry instanceof PaletteContainer) {
            PaletteContainer container = (PaletteContainer) entry;
            for (Iterator iterator = container.getChildren().iterator(); iterator.hasNext();) {
                PaletteEntry en = (PaletteEntry) iterator.next();
                retreiveAllEntry(list, en);
            }
        } else {
            String family = ComponentsFactoryProvider.getPaletteEntryFamily(entry.getParent());
            list.add(family + FAMILY_SPEARATOR + entry.getLabel());
        }

    }

    @SuppressWarnings("unchecked")
    public List<ComponentSetting> getComponentsFromProject() {
        List<ComponentSetting> components = project.getEmfProject().getComponentsSettings();
        return components;
    }

    public boolean isComponentVisible(PaletteEntry entry) {
        String label = entry.getLabel();
        String family = ComponentsFactoryProvider.getPaletteEntryFamily(entry.getParent());
        List<ComponentSetting> components = getComponentsFromProject();

        for (ComponentSetting componentSetting : components) {
            if (componentSetting.getName().equals(label) && componentSetting.getFamily().equals(family)) {
                return !componentSetting.isHidden();
            }
        }

        return true;
    }

    private void setComponentVisibleForRestore(String name, boolean visible) {
        setComponentVisible(name, visible, RESTORE);
    }

    private void setComponentVisible(String name, boolean visible, boolean restore) {
        String[] names = name.split(FAMILY_SPEARATOR);
        String family = names[0];
        String label = names[1];
        List<ComponentSetting> components = getComponentsFromProject();
        for (ComponentSetting componentSetting : components) {
            if (componentSetting.getFamily().equals(family) && componentSetting.getName().equals(label)) {
                componentSetting.setHidden(!visible);
                return;
            }
        }
        if (!restore) {
            ComponentSetting cs = PropertiesFactory.eINSTANCE.createComponentSetting();
            cs.setName(label);
            cs.setHidden(!visible);
            cs.setFamily(family);
            components.add(cs);
            statusBackup.put(label, !visible);
        }
    }

    /**
     * qwei Comment method "getViewer".
     *
     * @return
     */
    public TreeViewer getViewer() {
        return this.hiddenViewer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.YES_ID, "Restore &Defaults", false); //$NON-NLS-1$
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setText("&Apply"); //$NON-NLS-1$
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        super.okPressed();

        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            prf.saveProject(project);
            // ComponentUtilities.updatePalette(false);
            ShowStandardAction.getInstance().doRun();
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (IDialogConstants.YES_ID == buttonId) {
            retoreDefaultSettings();
        }
    }

    /**
     * bqian Comment method "retoreDefaultSettings".
     */
    private void retoreDefaultSettings() {
        ComponentsFactoryProvider.restoreComponentVisibilityStatus();
        refreshViewer();
        getButton(IDialogConstants.OK_ID).setEnabled(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        for (Object element : statusBackup.keySet()) {
            String name = (String) element;
            setComponentVisibleForRestore(name, statusBackup.get(name));
        }
    }
}
