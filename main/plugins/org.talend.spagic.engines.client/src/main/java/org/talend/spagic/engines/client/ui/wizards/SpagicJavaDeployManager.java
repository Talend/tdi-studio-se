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
package org.talend.spagic.engines.client.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ExportFileResource;

/**
 * DOC qwei class global comment. Detailled comment <br/>
 *
 */
public class SpagicJavaDeployManager extends org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager {

    public SpagicJavaDeployManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, String... codeOptions)
            throws ProcessorException {

        for (ExportFileResource proces : process) {
            ProcessItem processItem = (ProcessItem) proces.getItem();

            ProcessorUtilities.setExportConfig(proces.getDirectoryName(), true);

            IProcess jobProcess = null;
            if (!isOptionChoosed(ExportChoice.doNotCompileCode)) {
                jobProcess = generateJobFiles(processItem, contextName, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, isOptionChoosed(ExportChoice.applyToChildren));
            }
            List<URL> resources = new ArrayList<URL>();
            resources.addAll(getLauncher(isOptionChoosed(ExportChoice.needLauncher),
                    isOptionChoosed(ExportChoice.needParameterValues), isOptionChoosed(ExportChoice.needContext), jobProcess,
                    processItem, escapeSpace(contextName), escapeSpace(launcher), statisticPort, tracePort, codeOptions));

            addJobItem(process, processItem, isOptionChoosed(ExportChoice.needJobItem), proces);

            resources.addAll(getJobScripts(processItem, isOptionChoosed(ExportChoice.needJobScript)));
            // resources.addAll(getProperties(processItem, srcList));
            resources.addAll(getProperties(processItem, contextName));
            addContextScripts(proces, isOptionChoosed(ExportChoice.needContext));

            // add children jobs
            boolean needChildren = true;
            List<URL> childrenList = addChildrenResources(process, processItem, needChildren, proces, exportChoice);
            resources.addAll(childrenList);
            proces.addResources(resources);

            // Gets job designer resouce
            // List<URL> srcList = getSource(processItem, exportChoice.get(ExportChoice.needSource));
            // process[i].addResources(JOB_SOURCE_FOLDER_NAME, srcList);
        }

        // Exports the system libs
        List<ExportFileResource> list = new ArrayList<ExportFileResource>(Arrays.asList(process));
        // Add the java system libraries
        ExportFileResource rootResource = new ExportFileResource(null, LIBRARY_FOLDER_NAME);
        ExportFileResource spagicResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        list.add(rootResource);

        list.add(spagicResource);
        // Gets system routines
        if (isOptionChoosed(ExportChoice.needSystemRoutine)) {
            rootResource.addResources(getSystemRoutine(process));
        }
        // Gets user routines
        if (isOptionChoosed(ExportChoice.needUserRoutine)) {
            rootResource.addResources(getUserRoutine(process));
        }

        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(isOptionChoosed(ExportChoice.needTalendLibraries), process);
        rootResource.addResources(talendLibraries);

        return list;
    }

    private List<URL> addChildrenResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
            ExportFileResource resource, Map<ExportChoice, Object> exportChoice) {
        List<JobInfo> list = new ArrayList<JobInfo>();
        String projectName = getCorrespondingProjectName(process);
        List<URL> allJobScripts = new ArrayList<URL>();
        if (needChildren) {
            try {
                List<ProcessItem> processedJob = new ArrayList<ProcessItem>();
                getChildrenJobAndContextName(allResources, process.getProperty().getLabel(), list, process, projectName,
                        processedJob, allJobScripts, resource, exportChoice);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

        }
        return allJobScripts;
    }

    public List<URL> getProperties(ProcessItem processItem, String contextName) {
        List<URL> list = new ArrayList<URL>();
        Properties p = new Properties();
        FileOutputStream out = null;
        String projectName = getCorrespondingProjectName(processItem);
        String jobName = processItem.getProperty().getLabel();
        String jobFolderName = JavaResourcesHelper.getJobFolderName(escapeFileNameSpace(processItem), processItem.getProperty()
                .getVersion());
        try {
            // List<SpagoBiServer> listServerSapgo = null;
            // listServerSapgo = SpagicServerHelper.parse(new SpagicPreferencePage().getPreferenceStore().getString(
            // SpagoBiServer.SPAGOBI_SERVER));
            // if (listServerSapgo != null && !listServerSapgo.isEmpty()) {
            // Iterator<SpagoBiServer> iterator = listServerSapgo.iterator();
            // while (iterator.hasNext()) {
            // SpagoBiServer spagoBiServer = iterator.next();
            // }
            // }
            IPath path = getSrcRootLocation(processItem);
            File file = new File(getTmpFolder() + PATH_SEPARATOR + "spagic.properties"); //$NON-NLS-1$
            path = path.append(projectName).append(jobFolderName).append(jobName + ".java"); //$NON-NLS-1$
            BufferedReader buff = new BufferedReader(new FileReader(path.toPortableString()));
            int nbLine = 0;
            while (buff.readLine() != null) {
                nbLine++;
            }
            out = new FileOutputStream(file);
            PrintStream ps = new PrintStream(out);
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            IProcess process = service.getProcessFromProcessItem(processItem);
            List<IContextParameter> ctxParams = process.getContextManager().getContext(contextName).getContextParameterList();
            for (IContextParameter ctxParam : ctxParams) {
                p.put(ctxParam.getName(), ctxParam.getValue());
            }
            p.put("JobClassName", getCorrespondingProjectName(null) //$NON-NLS-1$
                            + "." //$NON-NLS-1$
                            + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem
                                    .getProperty().getVersion()) + "." + processItem.getProperty().getLabel()); //$NON-NLS-1$
            p.put("talendJobClassDescription", HTMLDocUtils.checkString(processItem.getProperty().getDescription())); //$NON-NLS-1$
            p.put("rowNumber", Integer.toString(nbLine)); //$NON-NLS-1$
            p.put("host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
            p.list(ps);
            ps.flush();
            list.add(file.toURI().toURL());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
        return list;
    }
}
