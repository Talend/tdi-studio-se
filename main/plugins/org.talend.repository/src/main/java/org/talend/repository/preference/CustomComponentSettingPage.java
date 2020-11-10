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
package org.talend.repository.preference;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.CustomComponentSetting;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.services.ISVNProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class CustomComponentSettingPage extends ProjectSettingPage {

    private TreeViewer componentViewer, shareViewer;

    private Button shareButton, backButton;

    private ThreeCompositesSashForm compositesSachForm;

    private Map<IComponent, CustomComponentSetting> sharedAdded = new HashMap<IComponent, CustomComponentSetting>();

    private Map<IComponent, CustomComponentSetting> backAdded = new HashMap<IComponent, CustomComponentSetting>();

    private List<CustomComponentSetting> sharedInitValues = new ArrayList<CustomComponentSetting>();

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);
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
        init();

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        return composite;
    }

    private void init() {
        for (CustomComponentSetting settings : (List<CustomComponentSetting>) pro.getEmfProject().getCustomComponentSettings()) {
            if (settings.isShare()) {
                sharedInitValues.add(settings);
            }
        }
    }

    private void addTreeViewer(ThreeCompositesSashForm parent) {

        Composite leftComposite = parent.getLeftComposite();
        Label label = new Label(leftComposite, SWT.NONE);
        label.setText("Custom Components");
        componentViewer = new TreeViewer(leftComposite);
        componentViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        componentViewer.setContentProvider(new CustomCompSettingContentProvider());
        componentViewer.setLabelProvider(new CustomCompSettingLabelProvider());
        componentViewer.addFilter(getFilter(false));

        componentViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                shareButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        createButtons(parent.getMidComposite());
        Composite rightComposite = parent.getRightComposite();
        Label rlabel = new Label(rightComposite, SWT.NONE);
        rlabel.setText("Shared Components");
        shareViewer = new TreeViewer(rightComposite);
        shareViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        shareViewer.setContentProvider(new CustomCompSettingContentProvider());
        shareViewer.setLabelProvider(new CustomCompSettingLabelProvider());
        shareViewer.addFilter(getFilter(true));

        shareViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                backButton.setEnabled(!event.getSelection().isEmpty());
            }
        });

        List<IComponent> input = ComponentsFactoryProvider.getInstance().getCustomComponents();
        componentViewer.setInput(input);
        shareViewer.setInput(input);
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

        shareButton = new Button(buttonComposite2, SWT.NONE);
        shareButton.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));
        shareButton.setToolTipText(""); //$NON-NLS-1$
        gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        shareButton.setLayoutData(gridData);
        shareButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // share components
                ISelection selection = componentViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Iterator iterator = ((IStructuredSelection) selection).iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        if (next instanceof IComponent) {
                            IComponent component = (IComponent) next;
                            CustomComponentSetting setting = null;
                            for (CustomComponentSetting compSetting : getCustomComponentSettings()) {
                                if (compSetting.getName() != null && compSetting.getName().equals(component.getName())) {
                                    setting = compSetting;
                                    break;
                                }
                            }
                            if (setting == null) {
                                setting = PropertiesFactory.eINSTANCE.createCustomComponentSetting();
                                setting.setName(component.getName());
                                getCustomComponentSettings().add(setting);
                            }
                            setting.setShare(true);
                            if (backAdded.containsKey(next)) {
                                backAdded.remove(next);
                            } else {
                                sharedAdded.put(component, setting);
                            }
                        }
                    }
                    refreshViewer();
                }

            }
        });
        shareButton.setEnabled(false);

        backButton = new Button(buttonComposite2, SWT.NONE);
        backButton.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        backButton.setToolTipText(""); //$NON-NLS-1$
        gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        backButton.setLayoutData(gridData);
        backButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = shareViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Iterator iterator = ((IStructuredSelection) selection).iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        if (next instanceof IComponent) {
                            IComponent component = (IComponent) next;
                            CustomComponentSetting setting = null;
                            for (CustomComponentSetting compSetting : getCustomComponentSettings()) {
                                if (compSetting.getName() != null && compSetting.getName().equals(component.getName())) {
                                    setting = compSetting;
                                    break;
                                }
                            }
                            if (setting == null) {
                                return;
                            }
                            setting.setShare(false);

                            if (sharedAdded.containsKey(component)) {
                                sharedAdded.remove(component);
                            } else {
                                backAdded.put(component, setting);
                            }
                        }
                    }
                    refreshViewer();
                }

            }
        });
        backButton.setEnabled(false);

    }

    private ViewerFilter getFilter(final boolean shared) {
        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                IComponent component = (IComponent) element;

                if (shared) {
                    return isComponentShared(component);
                } else {
                    return !isComponentShared(component);
                }
            }
        };
        return filter;
    }

    public boolean isComponentShared(IComponent component) {
        String name = component.getName();
        List<CustomComponentSetting> customComponentSettings = getCustomComponentSettings();
        for (CustomComponentSetting componentSetting : customComponentSettings) {
            if (componentSetting.getName().equals(name)) {
                return componentSetting.isShare();
            }
        }

        return false;
    }

    private List<CustomComponentSetting> getCustomComponentSettings() {
        return pro.getEmfProject().getCustomComponentSettings();
    }

    private void refreshViewer() {
        componentViewer.refresh();
        shareViewer.refresh();
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void performDefaults() {
        resetCustomComponentSetting();
        refreshViewer();
        super.performDefaults();
    }

    @Override
    protected void performApply() {
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Save custom component setting", (sharedAdded.size() + backAdded.size()) * 100);
                finish(monitor);
            }

        };
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            if (!sharedAdded.isEmpty() || !backAdded.isEmpty()) {
                dialog.run(true, false, runnable);
            }
            init();
            sharedAdded.clear();
            backAdded.clear();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }

    }

    @Override
    public boolean performOk() {
        finish();
        return super.performOk();
    }

    @Override
    public boolean performCancel() {
        if (componentViewer != null) {
            resetCustomComponentSetting();
        }
        return super.performCancel();
    }

    private void finish(IProgressMonitor... monitorWrap) {
        IProgressMonitor monitor = null;
        if (monitorWrap != null && monitorWrap.length == 1) {
            monitor = monitorWrap[0];
        }
        final IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();

        if (PluginChecker.isSVNProviderPluginLoaded() && (!sharedAdded.isEmpty() || !backAdded.isEmpty())) {
            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit("Update custom components") {

                @Override
                public void run() throws PersistenceException {
                    final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                        @Override
                        public void run(IProgressMonitor subMonitor) throws CoreException {

                            ISVNProviderService service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(
                                    ISVNProviderService.class);
                            String projectLabel = pro.getTechnicalLabel();
                            IWorkspace workspace = ResourcesPlugin.getWorkspace();
                            IProject eclipseProject = workspace.getRoot().getProject(projectLabel);
                            String targetRoot = eclipseProject.getLocation().toString() + "/"
                                    + ERepositoryObjectType.getFolderName(ERepositoryObjectType.COMPONENTS);
                            File componentFolder = new File(targetRoot);
                            try {
                                if (!componentFolder.exists()) {
                                    FilesUtils.createFoldersIfNotExists(targetRoot, false);
                                }

                                String sourceRoot = ComponentsFactoryProvider.getInstance().getCustomComponentBundlePath() + "/";

                                // delete share
                                for (IComponent component : backAdded.keySet()) {
                                    String componentFullPath = targetRoot + File.separator + component.getName();
                                    if (service.isSVNProject(pro)) {
                                        service.svnEclipseHandlerDelete(eclipseProject, pro, componentFullPath);
                                        if (subMonitor != null) {
                                            subMonitor.worked(10);
                                        }
                                    } else {
                                        File file = new File(componentFullPath);
                                        if (file != null && file.exists()) {
                                            org.talend.utils.io.FilesUtils.deleteFolder(file, true);
                                        }
                                    }
                                }
                                if (!backAdded.isEmpty()) {
                                    getCustomComponentSettings().removeAll(backAdded.values());
                                }

                                FileFilter ff = new FileFilter() {

                                    @Override
                                    public boolean accept(File pathname) {
                                        if (FilesUtils.isSVNFolder(pathname)) {
                                            return false;
                                        }
                                        return true;
                                    }

                                };

                                // share
                                for (IComponent component : sharedAdded.keySet()) {
                                    String sourcePath = sourceRoot + component.getPathSource() + File.separator
                                            + component.getName();
                                    File sourceFile = new File(sourcePath);

                                    String targetPath = targetRoot + File.separatorChar + component.getName();
                                    File targetFile = new File(targetPath);
                                    FilesUtils.copyFolder(sourceFile, targetFile, true, ff, null, true, false);
                                    if (subMonitor != null) {
                                        subMonitor.worked(10);
                                    }
                                }

                            } catch (Exception e) {
                                resetCustomComponentSetting();
                                ExceptionHandler.process(e);
                            }
                            try {
                                prf.saveProject(pro);
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }

                            try {
                                eclipseProject.refreshLocal(IResource.DEPTH_INFINITE, subMonitor);
                            } catch (CoreException e1) {
                                ExceptionHandler.process(e1);
                            }
                        }
                    };
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule schedulingRule = workspace.getRoot();
                        // the update the project files need to be done in the workspace runnable to avoid all
                        // notification
                        // of changes before the end of the modifications.
                        workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
                    } catch (CoreException e) {
                        throw new PersistenceException(e.getCause());
                    }
                }
            };
            repositoryWorkUnit.setRefreshRepository(false);
            repositoryWorkUnit.setForceTransaction(true);
            prf.executeRepositoryWorkUnit(repositoryWorkUnit);
            try {
                repositoryWorkUnit.throwPersistenceExceptionIfAny();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
        if (monitor != null) {
            monitor.done();
        }
        // refresh again after the gui closed .
        try {
            String projectLabel = pro.getTechnicalLabel();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject eclipseProject = workspace.getRoot().getProject(projectLabel);
            eclipseProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (CoreException e1) {
            ExceptionHandler.process(e1);
        }
    }

    private void resetCustomComponentSetting() {
        for (CustomComponentSetting setting : (List<CustomComponentSetting>) pro.getEmfProject().getCustomComponentSettings()) {
            if (sharedInitValues.contains(setting)) {
                setting.setShare(true);
            } else {
                setting.setShare(false);
            }
        }
        sharedAdded.clear();
        backAdded.clear();
    }

    /**
     *
     * wchen CustomComponentSettingPage class global comment. Detailled comment
     */
    class CustomCompSettingLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof IComponent) {
                return ImageProvider.getImage(((IComponent) element).getIcon16());
            }
            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IComponent) {
                return ((IComponent) element).getName();
            }
            return super.getText(element);
        }

    }

    /**
     *
     * wchen CustomComponentSettingPage class global comment. Detailled comment
     */
    class CustomCompSettingContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getParent(Object element) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray();
            }
            return null;
        }

        @Override
        public void dispose() {
            // TODO Auto-generated method stub

        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

    }

}
