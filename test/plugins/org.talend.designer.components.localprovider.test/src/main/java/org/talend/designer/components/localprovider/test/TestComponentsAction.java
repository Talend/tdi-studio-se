// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.components.localprovider.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2007-05-21 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class TestComponentsAction extends Action {

    /**
     *
     */
    private static final String EXECUTION_OK = "##EXECUTION_OK##"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(TestComponentsAction.class);

    static final String PLUGIN_LOCATION = "org.talend.designer.components.localprovider.test"; //$NON-NLS-1$

    private static java.util.Properties properties = new java.util.Properties();

    public TestComponentsAction() {
        super();
        this.setId("testComponents"); //$NON-NLS-1$
        this.setActionDefinitionId("testComponents"); //$NON-NLS-1$
        loadProperties();
    }

    @Override
    public void run() {
        initialize();
    }

    public void initialize() {
        Job job = new Job("Component Test") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {

                    // IProgressMonitor monitorWrap = new CodeGeneratorProgressMonitor(monitor);
                    monitor.beginTask("Component Test Running", 1100); //$NON-NLS-1$
                    IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
                    Collection<IComponent> components = componentsFactory.readComponents();
                    monitor.worked(100);

                    RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                            Context.REPOSITORY_CONTEXT_KEY);
                    ECodeLanguage codeLanguage = repositoryContext.getProject().getLanguage();
                    String path = getPath();
                    // TODO

                    int taskTotal = components.size();
                    for (IComponent component : components) {
                        String templateURI = path + File.separatorChar + component.getName() + File.separatorChar
                                + TestParameter.GENERATE_TEST;
                        File templateFile = new File(templateURI);
                        // System.out.println(templateFile + " " + templateFile.exists());
                        if (templateFile.exists()) {
                            File[] fileArray = getFile(templateFile, codeLanguage);
                            generateCode(fileArray, templateURI.replace("\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        monitor.worked(1 * 1000 / taskTotal);
                    }
                } catch (Exception e) {
                    log.error("Exception during test Initialization", e); //$NON-NLS-1$
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();

    }

    private void loadProperties() {
        Bundle b = Platform.getBundle(PLUGIN_LOCATION);

        URL url = null;
        try {
            url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources"), null)); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
        }

        File propertyFile = new File(url.getPath() + "/Test.properties"); //$NON-NLS-1$
        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getPath() {
        IPath filePath = new Path("components"); //$NON-NLS-1$
        Bundle b = Platform.getBundle(TestComponentsPlugin.PLUGINCOMPONENTID);
        String dir = null;
        try {
            URL url = FileLocator.toFileURL(FileLocator.find(b, filePath, null));
            dir = url.getPath();
            dir = dir.substring(1, dir.length() - 1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dir;
    }

    public File[] getFile(File directory, ECodeLanguage language) {
        File[] itemFiles = null;
        if (language == ECodeLanguage.JAVA) {
            itemFiles = directory.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    boolean result = false;
                    if (file.getName().toLowerCase().endsWith("java.item")) { //$NON-NLS-1$
                        result = true;
                        return result;
                    }
                    return result;
                }
            });
        } else {
            itemFiles = directory.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    boolean result = false;
                    if (file.getName().toLowerCase().endsWith("perl.item")) { //$NON-NLS-1$
                        result = true;
                        return result;
                    }
                    return result;
                }
            });
        }

        return itemFiles;
    }

    public void generateCode(File[] tempFile, String componentPath) {
        for (File file : tempFile) {
            if (file.length() > 0) {
                URI uri = URI.createFileURI(file.toString());
                TalendFilePackage.eINSTANCE.getNsURI();
                Resource resource = new ResourceSetImpl().getResource(uri, true);
                ProcessType processType = (ProcessType) EcoreUtil.getObjectByType(resource.getContents(),
                        TalendFilePackage.eINSTANCE.getProcessType());
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
                item.setProcess(processType);
                item.setProperty(PropertiesFactory.eINSTANCE.createProperty());
                item.getProperty().setLabel(TestParameter.TEST_DIR);
                item.getProperty().setAuthor(PropertiesFactory.eINSTANCE.createUser());
                item.getProperty().setId(file.getName());
                item.getProperty().setStatusCode(file.getName());
                item.getProperty().setVersion(TestParameter.VERSION);
                IProcess process = service.getProcessFromProcessItem(item);
                setDefaultProperties(process, componentPath);
                IProcessor processor = ProcessorUtilities.getProcessor(process, item.getProperty(), process.getContextManager()
                        .getDefaultContext());
                try {
                    // generate
                    log.info(TestParameter.GENERATE_START + " : " + file.getName()); //$NON-NLS-1$
                    // processor.generateCode(process.getContextManager().getDefaultContext(), false, false, true);
                    processor.generateCode(false, false, true);

                    log.info(TestParameter.GENERATE_END + " : " + file.getName()); //$NON-NLS-1$
                    // run
                    log.info(TestParameter.RUN_START + " : " + file.getName()); //$NON-NLS-1$
                    java.lang.Process runningProcess = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
                    StringBuffer errBuff = new StringBuffer();
                    if (isRunOK(runningProcess, errBuff)) {
                        log.info(TestParameter.RUN_SUCCESS + " : " + file.getName()); //$NON-NLS-1$
                    } else {
                        // 2 possibilities : OK not found / error stream
                        log.error(TestParameter.RUN_FAIL + " : " + file.getName(), new Exception(errBuff.toString())); //$NON-NLS-1$

                    }

                } catch (ProcessorException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private boolean isRunOK(java.lang.Process runningProcess, StringBuffer errBuff) {
        try {
            runningProcess.waitFor();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
        }
        BufferedReader outIs = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()));
        BufferedReader errIs = new BufferedReader(new InputStreamReader(runningProcess.getErrorStream()));
        String s = null;
        boolean retValue = false;
        try {
            while ((s = outIs.readLine()) != null) {
                if (s.equals(EXECUTION_OK)) {
                    retValue = true;
                }

            }
            if (!retValue) {
                errBuff.append(EXECUTION_OK + "not found").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            while ((s = errIs.readLine()) != null) {
                errBuff.append(s);
                retValue = false;
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            retValue = false;
        }

        return retValue;
    }

    /**
     * DOC qwei Comment method "setDefaultProperties".
     *
     * @param process
     */
    private void setDefaultProperties(IProcess process, String componentPath) {
        // input_path / output_path
        IContext context = process.getContextManager().getDefaultContext();
        List<IContextParameter> tempContextParameter = context.getContextParameterList();

        for (IContextParameter parameter : tempContextParameter) {
            fillParameter(parameter, componentPath);
        }

    }

    /**
     * DOC qwei Comment method "fillParameter".
     *
     * @param parameter
     */
    private void fillParameter(IContextParameter parameter, String componentPath) {
        // TODO Auto-generated method stub
        if (parameter.getName().toLowerCase().equals(TestParameter.INPUT_PATH)) {
            parameter.setValue(componentPath);
            return;
        }
        if (parameter.getName().toLowerCase().equals(TestParameter.OUTPUT_PATH)) {
            parameter.setValue(getTemppath());
            return;
        }

        String value = properties.getProperty(parameter.getName());
        if (value != null) {
            parameter.setValue(value);
        }
    }

    private String getTemppath() {
        IPreferenceStore prefStore = CorePlugin.getDefault().getPreferenceStore();
        return prefStore.getString(ITalendCorePrefConstants.FILE_PATH_TEMP).replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
