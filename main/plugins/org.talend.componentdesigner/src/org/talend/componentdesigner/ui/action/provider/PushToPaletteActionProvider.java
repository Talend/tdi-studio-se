// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.action.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.util.XSDValidator;
import org.talend.componentdesigner.util.file.FileCopy;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC slanglois class global comment. Detailled comment
 */
public class PushToPaletteActionProvider extends CommonActionProvider {

    private IAction copyProjectAction;

    private List<IFolder> selectedFolderList;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
     */
    @Override
    public void init(ICommonActionExtensionSite anExtensionSite) {

        if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            copyProjectAction = new PushToPaletteAction();
        }
    }

    /**
     * Adds a submenu to the given menu with the name "New Component".
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        menu.insertBefore("group.edit", copyProjectAction); //$NON-NLS-1$
        // Object obj = ((TreeSelection) this.getContext().getSelection()).getFirstElement();// need to get all
        // selected.
        Iterator ite = ((TreeSelection) this.getContext().getSelection()).iterator();
        selectedFolderList = new ArrayList<IFolder>();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof IFolder) {
                selectedFolderList.add((IFolder) obj);
            }
        }
    }

    /**
     * DOC slanglois PushToPaletteActionProvider class global comment. Detailled comment
     */
    class PushToPaletteAction extends Action {

        public PushToPaletteAction() {
            super(Messages.getString("PushToPaletteActionProvider.PushComponentsToPalette")); //$NON-NLS-1$
            // setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.COPYCOMPONENT_ACTION));
        }

        /*
         * (non-Javadoc) Method declared on IAction.
         */
        @Override
        public void run() {
            String path = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
                IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                        .getService(IComponentsLocalProviderService.class);
                if (service != null) {
                    path = service.getPreferenceStore().getString("USER_COMPONENTS_FOLDER"); //$NON-NLS-1$
                }
            }
            if (path == null || path.length() == 0) {
                new MessageDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("PushToPaletteActionProvider.Error"), null, //$NON-NLS-1$
                        Messages.getString("PushToPaletteActionProvider.ErrorMSG"), MessageDialog.ERROR, new String[] { Messages.getString("PushToPaletteActionProvider.OK") }, 0).open(); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            File targetFile = new File(path);
            if (!targetFile.exists()) {
                new MessageDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("PushToPaletteActionProvider.Error2"), null, //$NON-NLS-1$
                        Messages.getString("PushToPaletteActionProvider.ErrorMSG2"), MessageDialog.ERROR, new String[] { Messages.getString("PushToPaletteActionProvider.OK2") }, 0).open(); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }

            // fix issue 7636: Don't need to copy if component project URL and
            // user components folder are the same.
            String projectURL = ComponentDesigenerPlugin.getDefault().getPreferenceStore().getString(PluginConstant.PROJECT_URL);
            File source = new File(projectURL);

            List<String> invalidXMLs = new ArrayList();
            for (IFolder selectedFolder : selectedFolderList) {
                List<String> result = checkComponentXMLinFolder(selectedFolder.getRawLocation().toString());
                invalidXMLs.addAll(result);
            }

            // all XML are OK
            if (invalidXMLs.size() == 0) {

                if (!targetFile.equals(source)) {
                    for (IFolder selectedFolder : selectedFolderList) {
                        File sourceFile = selectedFolder.getRawLocation().toFile();
                        String sourceComponentFolder = sourceFile.getAbsolutePath();
                        String targetComponentFolder = targetFile.getAbsolutePath() + File.separator + sourceFile.getName();
                        org.talend.utils.io.FilesUtils.deleteFolder(new File(targetComponentFolder), true);
                        FileCopy.copyComponentFolder(sourceComponentFolder, targetComponentFolder, true);

                    }
                }
                refreshSharedComponents();
                // add for bug TDI-26719, clear image cash from EmfComponent
                IComponentsFactory components = ComponentsFactoryProvider.getInstance();
                List<IComponent> comList = components.getCustomComponents();
                for (IComponent com : comList) {
                    if (com.getImageRegistry() != null) {
                        com.getImageRegistry().clear();
                    }
                }

                MessageDialog warningMessageDialog = new MessageDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("PushToPaletteActionProvider.Information"), null, Messages.getString("PushToPaletteActionProvider.InformationMSG"), MessageDialog.INFORMATION, //$NON-NLS-1$ //$NON-NLS-2$
                        new String[] { Messages.getString("PushToPaletteActionProvider.OK3") }, 0); //$NON-NLS-1$
                warningMessageDialog.open();

                CorePlugin.getDefault().getCodeGeneratorService().refreshTemplates();
            } else {
                StringBuffer sbuffer = new StringBuffer();
                for (String invalidXML : invalidXMLs) {
                    sbuffer.append(invalidXML).append("\n"); //$NON-NLS-1$
                }

                String waringInfo = Messages.getString("PushToPaletteActionProvider.PushToPaletteActionProvider.failed") + sbuffer.toString(); //$NON-NLS-1$

                MessageDialog warningMessageDialog = new MessageDialog(

                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("PushToPaletteActionProvider.PushToPaletteActionProvider.result"), null, waringInfo, //$NON-NLS-1$
                        MessageDialog.WARNING, new String[] { Messages.getString("PushToPaletteActionProvider.OK3") }, 0); //$NON-NLS-1$

                warningMessageDialog.open();
            }

        }
        
        private void refreshSharedComponents(){
            IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit("Update custom components") {
                @Override
                public void run()  {
                    final IWorkspaceRunnable operation = new IWorkspaceRunnable() {
                        @Override
                        public void run(IProgressMonitor subMonitor) throws CoreException {
                            Project project = ProjectManager.getInstance().getCurrentProject();
                            String projectLabel = project.getTechnicalLabel();
                            IProject eclipseProject = workspace.getRoot().getProject(projectLabel);
                         
                            try {
                                IFolder componentsFolder = ResourceUtils.getFolder(eclipseProject, ERepositoryObjectType.getFolderName(ERepositoryObjectType.COMPONENTS), true);
                                if(!componentsFolder.exists()){
                                    return;
                                }
                                for (IFolder selectedFolder : selectedFolderList) {
                                    IFolder comFolder = componentsFolder.getFolder(selectedFolder.getName());
                                    if(!comFolder.exists()){
                                        continue;
                                    }
                                  
                                  for(IResource svnResource:comFolder.members()){
                                      svnResource.delete(true, null);
                                  }
                                  
                                  for(IResource resource: selectedFolder.members()){
                                      if(!(resource instanceof IFile)){
                                          continue;
                                      }
                                      IFile targetSource = comFolder.getFile(resource.getName());
                                      if (targetSource.exists()) {
                                          targetSource.delete(true, null);
                                      }
                                      File file = new File(resource.getLocationURI());
                                      targetSource.create(new FileInputStream(file), true, null);
                                  }
                                  
                                }
                            } catch (PersistenceException | FileNotFoundException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    };
                    try {
                        workspace.run(operation, null);
                    } catch (CoreException e) {
                        ExceptionHandler.process(e);
                    }
                }
            };
            repositoryWorkUnit.setAvoidUnloadResources(true);
            factory.executeRepositoryWorkUnit(repositoryWorkUnit);
        }


        private List<String> checkComponentXMLinFolder(String folderPath) {

            List<String> invalidXMLs = new ArrayList();
            File componentFolder = new File(folderPath);
            // get the correct XML file for components
            File[] list = componentFolder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    // _java.xml
                    String javaXmlName = dir.getName() + "_java.xml"; //$NON-NLS-1$
                    String perlXmlName = dir.getName() + "_perl.xml"; //$NON-NLS-1$
                    if (name.equals(javaXmlName) || name.equals(perlXmlName)) {
                        return true;
                    }
                    return false;
                }
            });

            if (list != null) {
                // check the xml one by one(for perl/java)
                for (File xmlFile : list) {
                    try {
                        String message = new XSDValidator().checkXSD(xmlFile.getCanonicalPath());
                        if (message.length() > 0) {
                            invalidXMLs.add(xmlFile.getName());
                        }
                    } catch (Exception e) {
                        org.talend.componentdesigner.exception.ExceptionHandler.process(e);
                    }
                }
            }

            return invalidXMLs;
        }
    }
}
