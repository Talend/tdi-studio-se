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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.prefs.GeneralParametersProvider;
import org.talend.core.prefs.GeneralParametersProvider.GeneralParameters;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;

/**
 *
 */
public class TalendWizardArchiveFileResourceExportPage1 extends WizardArchiveFileResourceExportPage1 {

    private IStructuredSelection initialResourceSelection;

    private ResourceTreeAndListGroup resourceGroup;

    // private final static String SELECT_TYPES_TITLE = IDEWorkbenchMessages.WizardTransferPage_selectTypes;
    private final static String SELECT_TYPES_TITLE = Messages.getString("IDEWorkbenchMessages.WizardTransferPage_selectTypes"); //$NON-NLS-1$

    // private final static String SELECT_ALL_TITLE = IDEWorkbenchMessages.WizardTransferPage_selectAll;
    private final static String SELECT_ALL_TITLE = Messages.getString("IDEWorkbenchMessages.WizardTransferPage_selectAll"); //$NON-NLS-1$

    // private final static String DESELECT_ALL_TITLE = IDEWorkbenchMessages.WizardTransferPage_deselectAll;
    private final static String DESELECT_ALL_TITLE = Messages.getString("IDEWorkbenchMessages.WizardTransferPage_deselectAll"); //$NON-NLS-1$

    /**
     * Constants from org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
     */
    public class IDataTransferHelpContextIds {

        public static final String ZIP_FILE_EXPORT_WIZARD_PAGE = PlatformUI.PLUGIN_ID + "." + "zip_file_export_wizard_page"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public TalendWizardArchiveFileResourceExportPage1(IStructuredSelection selection) {
        super(selection);
        initialResourceSelection = selection;
        // setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setTitle(Messages.getString("DataTransferMessages.ArchiveExport_exportTitle")); //$NON-NLS-1$
        // setDescription(DataTransferMessages.ArchiveExport_description);
        setDescription(Messages.getString("DataTransferMessages.ArchiveExport_description")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        myCreateResourcesGroup(composite);
        myCreateButtonsGroup(composite);

        createDestinationGroup(composite);

        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook
        if (initialResourceSelection != null) {
            setupBasedOnInitialSelections();
        }

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(composite);

        giveFocusToDestination();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
    }

    protected void myCreateResourcesGroup(Composite parent) {

        // create the input element, which has the root resource
        // as its only child
        List<String> notExportProjects = Arrays.asList(GeneralParametersProvider
                .getStrings(GeneralParameters.PROJECTS_EXCLUDED_FROM_EXPORT));

        List input = new ArrayList();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].isOpen() && !notExportProjects.contains(projects[i].getName())) {
                input.add(projects[i]);
            }
        }

        resourceGroup = new ResourceTreeAndListGroup(parent, input, getResourceProvider(IResource.FOLDER | IResource.PROJECT),
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), getResourceProvider(IResource.FILE),
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), SWT.NONE, inRegularFontMode(parent));

        ICheckStateListener listener = new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };

        resourceGroup.addCheckStateListener(listener);

    }

    /**
     * Return the number of rows available in the current display using the current font.
     *
     * @param parent The Composite whose Font will be queried.
     * @return int The result of the display size divided by the font size.
     */
    public static int availableRows(Composite parent) {

        int fontHeight = (parent.getFont().getFontData())[0].getHeight();
        int displayHeight = parent.getDisplay().getClientArea().height;

        return displayHeight / fontHeight;
    }

    /**
     * Return whether or not the font in the parent is the size of a regular font. Typically used to know if a font is
     * smaller than the High Contrast Font. This method is used to make layout decisions based on screen space.
     *
     * @param parent The Composite whose Font will be queried.
     * @return boolean. True if there are more than 50 lines of possible text in the display.
     */
    public static boolean inRegularFontMode(Composite parent) {

        return availableRows(parent) > 50;
    }

    @Override
    protected Iterator getSelectedResourcesIterator() {
        return resourceGroup.getAllCheckedListItems().iterator();
    }

    @Override
    protected List getWhiteCheckedResources() {
        return resourceGroup.getAllWhiteCheckedItems();
    }

    @Override
    protected void setupBasedOnInitialSelections() {
        Iterator it = initialResourceSelection.iterator();
        while (it.hasNext()) {
            IResource currentResource = (IResource) it.next();
            if (currentResource.getType() == IResource.FILE) {
                resourceGroup.initialCheckListItem(currentResource);
            } else {
                resourceGroup.initialCheckTreeItem(currentResource);
            }
        }
    }

    private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {

            @Override
            public Object[] getChildren(Object o) {
                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        // just return an empty set of children
                        return new Object[0];
                    }

                    // filter out the desired resource types
                    ArrayList results = new ArrayList();
                    for (IResource member : members) {
                        // And the test bits with the resource types to see if they are what we want
                        if ((member.getType() & resourceType) > 0) {
                            results.add(member);
                        }
                    }
                    return results.toArray();
                }
                // input element case
                if (o instanceof ArrayList) {
                    return ((ArrayList) o).toArray();
                }
                return new Object[0];
            }
        };
    }

    protected final void myCreateButtonsGroup(Composite parent) {

        Font font = parent.getFont();

        // top level group
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        // types edit button
        Button selectTypesButton = createButton(buttonComposite, IDialogConstants.SELECT_TYPES_ID, SELECT_TYPES_TITLE, false);

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTypesEditButtonPressed();
            }
        };
        selectTypesButton.addSelectionListener(listener);
        selectTypesButton.setFont(font);
        setButtonLayoutData(selectTypesButton);

        Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);

        listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                resourceGroup.setAllSelections(true);
                updateWidgetEnablements();
            }
        };
        selectButton.addSelectionListener(listener);
        selectButton.setFont(font);
        setButtonLayoutData(selectButton);

        Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);

        listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                resourceGroup.setAllSelections(false);
                updateWidgetEnablements();
            }
        };
        deselectButton.addSelectionListener(listener);
        deselectButton.setFont(font);
        setButtonLayoutData(deselectButton);

    }

    @Override
    public boolean finish() {
        List resourcesToExport = getWhiteCheckedResources();

        if (!ensureTargetIsValid()) {
            return false;
        }

        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();

        // about to invoke the operation so save our state
        saveWidgetValues();

        final List results = new ArrayList(1);
        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().executeRepositoryWorkUnit(new RepositoryWorkUnit("refresh") {

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
                Display.getCurrent().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        List resourcesToExport = getWhiteCheckedResources();
                        boolean r = executeExportOperation(new ArchiveFileExportOperation(null, resourcesToExport,
                                getDestinationValue()));
                        results.add(r);
                    }
                });
            }

        });

        return results.size() == 1;
    }
}
