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
package org.talend.designer.codegen.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.CodeGenPlugin;
import org.eclipse.emf.codegen.jet.JETCompiler;
import org.eclipse.emf.codegen.jet.JETEmitter;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.debug.TalendDebugHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.core.model.components.ComponentBundleToPath;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TalendJetEmitter extends JETEmitter {

    private String templateName;

    private String templateLanguage;

    private String codePart;

    private TalendEclipseHelper talendEclipseHelper;

    private String componentFamily;

    private static Logger log = Logger.getLogger(TalendJetEmitter.class);

    private JetBean jetbean;

    private boolean classAvailable = true;

    /**
     * DOC mhirt TalendJetEmitter constructor comment.
     *
     * @param arg0
     * @param arg1
     * @param globalClasspath
     * @throws JETException
     */
    public TalendJetEmitter(String arg0, ClassLoader arg1, IProgressMonitor progressMonitor,
            HashMap<String, String> globalClasspath, boolean rebuild) throws JETException {
        super(arg0, arg1);

        for (String classKey : globalClasspath.keySet()) {
            this.addVariable(classKey, globalClasspath.get(classKey));
        }
        this.talendEclipseHelper = new TalendEclipseHelper(progressMonitor, this, rebuild);
    }

    public TalendJetEmitter(JetBean jetbean, TalendEclipseHelper teh) {
        super(jetbean.getFullTemplatePath(), jetbean.getClassLoader());
        this.jetbean = jetbean;
        this.templateName = jetbean.getClassName();
        this.componentFamily = jetbean.getFamily();
        this.templateLanguage = jetbean.getLanguage();
        this.codePart = jetbean.getCodePart();
        if (templateName.endsWith(codePart + "" + templateLanguage)) { //$NON-NLS-1$
            this.templateName = templateName.substring(templateName.lastIndexOf(".") + 1, templateName.lastIndexOf(codePart)); //$NON-NLS-1$
        }
        this.talendEclipseHelper = teh;
    }

    public TalendEclipseHelper getTalendEclipseHelper() {
        return this.talendEclipseHelper;
    }

    public void setTalendEclipseHelper(TalendEclipseHelper talendEclipseHelper) {
        this.talendEclipseHelper = talendEclipseHelper;
    }

    /**
     * Compiles the template to {@link #setMethod set} the method will be invoked to generate template results.
     *
     * @param progressMonitor the progress monitor for tracking progress.
     */
    @Override
    public void initialize(IProgressMonitor progressMonitor) throws JETException {
        initialize(BasicMonitor.toMonitor(progressMonitor));
    }

    /**
     * Compiles the template to {@link #setMethod set} the method will be invoked to generate template results.
     *
     * @param progressMonitor the progress monitor for tracking progress.
     */
    @Override
    public void initialize(Monitor progressMonitor) throws JETException {
        if (EMFPlugin.IS_ECLIPSE_RUNNING) {
            talendEclipseHelper.initialize(progressMonitor, this, componentFamily, templateName, templateLanguage, codePart);
        }
    }

    @Override
    public List getClasspathEntries() {
        // TODO Auto-generated method stub
        return super.getClasspathEntries();
    }

    @Override
    public void addVariable(String variableName, String pluginID) throws JETException {
        super.addVariable(variableName, pluginID);
    }

    /**
     * .
     */
    public class TalendEclipseHelper {

        IProject project;

        JetBean helperJetBean;

        public TalendEclipseHelper(IProgressMonitor progressMonitor, TalendJetEmitter jetEmitter, boolean rebuild)
                throws JETException {
            progressMonitor.beginTask("", 10); //$NON-NLS-1$

            try {
                final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
                if (!javaModel.isOpen()) {
                    javaModel.open(new SubProgressMonitor(progressMonitor, 1));
                } else {
                    progressMonitor.worked(1);
                }

                project = workspace.getRoot().getProject(projectName);
                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETPreparingProject_message", //$NON-NLS-1$
                        new Object[] { project.getName() }));

                if (!project.exists()) {
                    progressMonitor.subTask("JET creating project " + project.getName()); //$NON-NLS-1$
                    project.create(new SubProgressMonitor(progressMonitor, 1));
                    progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreatingProject_message", //$NON-NLS-1$
                            new Object[] { project.getName() }));
                }
                if (!project.isOpen()) {
                    project.open(new SubProgressMonitor(progressMonitor, 5));
                    project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(progressMonitor, 1));
                }

                IFolder sourceFolder = project.getFolder(new Path("src")); //$NON-NLS-1$
                if (!sourceFolder.exists()) {
                    sourceFolder.create(false, true, new SubProgressMonitor(progressMonitor, 1));
                }
                IFolder runtimeFolder = project.getFolder(new Path("runtime")); //$NON-NLS-1$
                if (!runtimeFolder.exists()) {
                    runtimeFolder.create(false, true, new SubProgressMonitor(progressMonitor, 1));
                }

                progressMonitor.done();
            } catch (CoreException exception) {
                throw new JETException(exception);
            } catch (Exception exception) {
                throw new JETException(exception);
            } finally {
                progressMonitor.done();
            }
        }

        private boolean isClasspathDifferent(IJavaProject javaProject, Set<IClasspathEntry> newClasspath) {
            IClasspathEntry[] rawClasspath;
            try {
                rawClasspath = javaProject.getRawClasspath();
            } catch (JavaModelException e) {
                return true;
            }

            Set<IClasspathEntry> settedClasspath = new HashSet<IClasspathEntry>();
            for (IClasspathEntry classpathEntry : rawClasspath) {
                settedClasspath.add(classpathEntry);
            }

            // source and target classpath must be the same
            for (IClasspathEntry classpathEntry : newClasspath) {
                if (!settedClasspath.contains(classpathEntry)) {
                    return true;
                }
            }

            for (IClasspathEntry classpathEntry : settedClasspath) {
                if (!newClasspath.contains(classpathEntry)) {
                    return true;
                }
            }

            return false;
        }

        public void initialize(Monitor monitor, TalendJetEmitter jetEmitter, String componentFamily, String templateName,
                String templateLanguage, String codePart) throws JETException {
            IProgressMonitor progressMonitor = BasicMonitor.toIProgressMonitor(monitor);
            progressMonitor.beginTask("", 10); //$NON-NLS-1$
            progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_GeneratingJETEmitterFor_message", //$NON-NLS-1$
                    new Object[] { jetEmitter.templateURI }));

            ByteArrayOutputStream outputStream = null;
            InputStream contents = null;

            try {
                JETCompiler jetCompiler = null;
                if (jetEmitter.templateURIPath == null) {
                    jetCompiler = new TalendJETCompiler(jetEmitter.templateURI, jetEmitter.encoding, jetEmitter.classLoader);
                } else {
                    jetCompiler = new TalendJETCompiler(jetEmitter.templateURIPath, jetEmitter.templateURI, jetEmitter.encoding,
                            jetEmitter.classLoader);
                }
                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETParsing_message", //$NON-NLS-1$
                        new Object[] { jetCompiler.getResolvedTemplateURI() }));
                jetCompiler.parse();
                jetCompiler.getSkeleton().setPackageName("org.talend.designer.codegen.translators." + componentFamily); //$NON-NLS-1$
                jetCompiler.getSkeleton().setClassName(templateName + codePart + templateLanguage);
                progressMonitor.worked(1);

                outputStream = new ByteArrayOutputStream();
                jetCompiler.generate(outputStream);
                contents = new ByteArrayInputStream(outputStream.toByteArray());

                progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETOpeningJavaProject_message", //$NON-NLS-1$
                        new Object[] { project.getName() }));

                String packageName = jetCompiler.getSkeleton().getPackageName();
                StringTokenizer stringTokenizer = new StringTokenizer(packageName, "."); //$NON-NLS-1$
                IProgressMonitor subProgressMonitor = new SubProgressMonitor(progressMonitor, 1);
                subProgressMonitor.beginTask("", stringTokenizer.countTokens() + 4); //$NON-NLS-1$
                subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_CreateTargetFile_message")); //$NON-NLS-1$
                IFolder sourceContainer = project.getFolder("src");

                while (stringTokenizer.hasMoreElements()) {
                    String folderName = stringTokenizer.nextToken();
                    sourceContainer = sourceContainer.getFolder(new Path(folderName));
                    if (!sourceContainer.exists()) {
                        try {
                            sourceContainer.create(true, true, new SubProgressMonitor(subProgressMonitor, 1));
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
                boolean needRebuild = true;
                String targetFileName = jetCompiler.getSkeleton().getClassName() + ".java"; //$NON-NLS-1$
                IFile targetFile = sourceContainer.getFile(new Path(targetFileName));
                if (!targetFile.exists()) {
                    subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreating_message", //$NON-NLS-1$
                            new Object[] { targetFile.getFullPath() }));
                    targetFile.create(contents, true, new SubProgressMonitor(subProgressMonitor, 1));
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    DataInputStream dis = new DataInputStream(targetFile.getContents());
                    int len = 0;
                    byte[] buf = new byte[1024];
                    while (((len = dis.read(buf))) != -1) {
                        baos.write(buf, 0, len);
                    }
                    dis.close();

                    String currentContent = baos.toString();
                    String newContent = outputStream.toString();
                    // since the build is done on linux, if use windows, it will use different rules for \r + \n
                    // so compare without \r, at least to see if original string is the same
                    // if yes, consider all is ok, no need to replace the content and recompile
                    currentContent = currentContent.replace("\r", ""); //$NON-NLS-1$//$NON-NLS-2$
                    newContent = newContent.replace("\r", ""); //$NON-NLS-1$//$NON-NLS-2$
                    if (!newContent.equals(currentContent)) {
                        subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETUpdating_message", //$NON-NLS-1$
                                new Object[] { targetFile.getFullPath() }));
                        targetFile.setContents(contents, true, true, new SubProgressMonitor(subProgressMonitor, 1));
                    } else {
                        needRebuild = false;
                    }
                }

                // if jetEmitter.getMethod() == null, means the class file doesn't exist anymore
                // it should be impossible to have only the class file deleted and the .java never modified, but still
                // handle this case.
                // we could optimize the speed at this point by removing the method call here.
                // but since this part is only called when there is a change detected in the jet emitters, it should be
                // ok.
                if (needRebuild || jetEmitter.method == null) {
                    subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETBuilding_message", //$NON-NLS-1$
                            new Object[] { project.getName() }));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compileSingleClass(project, targetFile, baos);
                    String output = baos.toString();
                    // project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new
                    // SubProgressMonitor(subProgressMonitor, 1));

                    boolean errors = false;
                    if (!output.isEmpty()) {
                        errors = true;
                        if (this.helperJetBean != null) {
                            this.helperJetBean.setGenerationError(output);
                        }
                        log.error(output);
                        setClassAvailable(false);
                    } else if (this.helperJetBean != null) {
                        this.helperJetBean.setGenerationError(null);
                    }

                    if (!errors) {
                        subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETLoadingClass_message", //$NON-NLS-1$
                                new Object[] { jetCompiler.getSkeleton().getClassName() + ".class" })); //$NON-NLS-1$

                        // Construct a proper URL for relative lookup.
                        //
                        URL url = new File(project.getLocation() + "/" + "runtime" + "/") //$NON-NLS-1$ //$NON-NLS-2$
                                .toURL();
                        URLClassLoader theClassLoader = new URLClassLoader(new URL[] { url }, jetEmitter.classLoader);
                        Class theClass = theClassLoader.loadClass((packageName.length() == 0 ? "" : packageName + ".") //$NON-NLS-1$ //$NON-NLS-2$
                                + jetCompiler.getSkeleton().getClassName());
                        String methodName = jetCompiler.getSkeleton().getMethodName();
                        Method[] methods = theClass.getDeclaredMethods();
                        for (Method method2 : methods) {
                            if (method2.getName().equals(methodName)) {
                                jetEmitter.setMethod(method2);
                                break;
                            }
                        }
                    }
                }
                subProgressMonitor.done();
            } catch (CoreException exception) {
                TalendDebugHandler.debug(exception);
                throw new JETException(
                        Messages.getString("TalendJetEmitter.exception") + " " + templateName + codePart + templateLanguage, exception); //$NON-NLS-1$
            } catch (Exception exception) {
                TalendDebugHandler.debug(exception);
                throw new JETException(
                        Messages.getString("TalendJetEmitter.exception") + " " + templateName + codePart + templateLanguage, exception); //$NON-NLS-1$
            } finally {
                try {
                    contents.close();
                } catch (Exception e) {
                    // ignore me even if i'm null
                }

                try {
                    outputStream.close();
                } catch (Exception e) {
                    // ignore me even if i'm null
                }

                progressMonitor.done();
            }
        }

        public JetBean getHelperJetBean() {
            return helperJetBean;
        }

        public void setHelperJetBean(JetBean helperJetBean) {
            this.helperJetBean = helperJetBean;
        }
    }

    private void compileSingleClass(IProject project, IFile javaFile, OutputStream errorMsgStream) {
        compileSingleClass(project, javaFile, null, errorMsgStream, null);
    }

    /**
     * DOC ycbai Comment method "compileSingleClass".
     * <p>
     * Compile one class directly.
     *
     * @param project
     * @param javaFile
     * @param standardMsgStream
     * @param errorMsgStream
     * @param progress
     */
    private void compileSingleClass(IProject project, IFile javaFile, OutputStream standardMsgStream,
            OutputStream errorMsgStream, CompilationProgress progress) {
        OutputStream msgStream = standardMsgStream;
        OutputStream errorStream = errorMsgStream;
        if (msgStream == null) {
            msgStream = System.out;
        }
        if (errorStream == null) {
            errorStream = System.err;
        }
        BatchCompiler.compile(getBatchCompilerCmd(project, javaFile), new PrintWriter(msgStream), new PrintWriter(errorStream),
                progress);
    }

    /**
     * DOC ycbai Comment method "getBatchCompilerCmd".
     *
     * @param project
     * @param javaFile
     * @return
     */
    private String[] getBatchCompilerCmd(IProject project, IFile javaFile) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add(javaFile.getLocation().toPortableString());
        String classpathStr = getClasspathStr();
        if (!classpathStr.isEmpty()) {
            cmdList.add("-classpath"); //$NON-NLS-1$
            cmdList.add(classpathStr);
        }
        //cmdList.add("-time"); //$NON-NLS-1$
        cmdList.add("-g"); //$NON-NLS-1$
        cmdList.add("-warn:none"); //$NON-NLS-1$
        cmdList.add('-' + JavaCore.VERSION_1_6);
        cmdList.add("-d"); //$NON-NLS-1$
        cmdList.add(getClassOutputPath(project, javaFile));

        return cmdList.toArray(new String[0]);
    }

    /**
     * DOC ycbai Comment method "getClassOutputPath".
     * <p>
     * Get class output folder of project.
     *
     * @param project
     * @param javaFile
     * @return
     */
    private String getClassOutputPath(IProject project, IFile javaFile) {
        IFolder runtimeFolder = project.getFolder("runtime"); //$NON-NLS-1$
        return runtimeFolder.getLocation().toPortableString();
    }

    /**
     * DOC ycbai Comment method "getClasspathStr".
     * <p>
     * Get character string of classpath with separator.
     *
     * @return
     */
    private String getClasspathStr() {
        StringBuffer cps = new StringBuffer();
        String classPathSeparator = JavaUtils.JAVA_CLASSPATH_SEPARATOR;
        List<IClasspathEntry> cpes = getClasspathEntries();
        for (IClasspathEntry cpe : cpes) {
            String classpath = getClasspathFromEntry(cpe);
            cps.append(classpath).append(classPathSeparator);
        }
        if (cps.length() > 0) {
            cps.deleteCharAt(cps.length() - 1);
        }

        return cps.toString();
    }

    /**
     * DOC ycbai Comment method "getClasspathFromEntry".
     * <p>
     * Get the absolute classpath.
     *
     * @param entry
     * @return
     */
    private String getClasspathFromEntry(IClasspathEntry entry) {
        if (entry == null) {
            return null;
        }
        IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
        if (extraAttributes.length > 0) {
            for (IClasspathAttribute ca : extraAttributes) {
                if ("plugin_id".equals(ca.getName())) { //$NON-NLS-1$
                    String pluginId = ca.getValue();
                    if (pluginId != null) {
                        File plugin = new File(ComponentBundleToPath.getPathFromBundle(pluginId));
                        String pluginPath = plugin.getAbsolutePath();
                        if (Platform.inDevelopmentMode() && plugin.isDirectory()) {
                            String output;
                            try {
                                output = getIdeOutputSubDir(Platform.getBundle(pluginId));
                                if (output != null) {
                                    pluginPath = pluginPath + File.separator + output;
                                }
                            } catch (IOException e) {
                                // for dev only so just keep a print stacktrace
                                e.printStackTrace();
                            }
                        }
                        return pluginPath;
                    }
                }
            }
        }

        return null;
    }

    /**
     * DOC ycbai Comment method "getIdeOutputSubDir".
     * <p>
     * Get class folder path of bundle. Just use for development environment.
     *
     * @param bundle
     * @return
     * @throws IOException
     */
    private String getIdeOutputSubDir(Bundle bundle) throws IOException {
        String outputSubDir = null;
        // get output folder path in case we are running from the IDE
        URL buildPropUrl = bundle.getEntry("/build.properties"); //$NON-NLS-1$
        if (buildPropUrl != null) {
            Properties buildProp = new Properties();
            InputStream buildPropStream = buildPropUrl.openStream();
            try {
                buildProp.load(buildPropStream);
                outputSubDir = buildProp.getProperty("output..", null); //$NON-NLS-1$
            } finally {
                buildPropStream.close();
            }
        }
        return outputSubDir;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.emf.codegen.jet.JETEmitter#getMethod()
     */
    @Override
    public Method getMethod() {
        Method localMethod = super.getMethod();
        if (localMethod == null) {
            try {
                localMethod = loadMethod();
            } catch (Exception e) {
                // nothing since if got exception here, the method will be reloaded bellow. (normal case)
                // real error should be logged if the initialize fail.
            }
            // add this part in case there is any problem in the project (should never be called in normal use)
            // but if there is any problem, it will force to regenerate again this component.
            // This might be also called for custom components.
            if (localMethod == null) {
                try {
                    talendEclipseHelper.initialize(BasicMonitor.toMonitor(new NullProgressMonitor()), this, componentFamily,
                            templateName, templateLanguage, codePart);
                    localMethod = super.getMethod();
                } catch (JETException e) {
                    ExceptionHandler.process(e);
                }
            } else {
                setMethod(localMethod);
            }
        }
        return localMethod;
    }

    /**
     * DOC mhirt Comment method "loadMethod".
     *
     * @param methodName
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     */
    private Method loadMethod() throws ClassNotFoundException, MalformedURLException {
        if (jetbean == null) {
            return null;
        }
        if (currentClassLoader != jetbean.getClassLoader()) {
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = workspace.getRoot().getProject(projectName);

            URL url = new File(project.getLocation() + "/runtime").toURL(); //$NON-NLS-1$
            currentClassLoader = jetbean.getClassLoader();
            theClassLoader = new URLClassLoader(new URL[] { url }, jetbean.getClassLoader());
        }
        Class theClass;
        try {
            theClass = theClassLoader.loadClass(jetbean.getClassName());
        } catch (Error e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }
        // TDI-23079
        try {
            Method[] methods = theClass.getDeclaredMethods();
            for (Method method2 : methods) {
                if (method2.getName().equals(jetbean.getMethodName())) {
                    return method2;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static ClassLoader currentClassLoader = null;

    private static URLClassLoader theClassLoader = null;

    /**
     * Getter for classAvailable.
     *
     * @return the classAvailable
     */
    public boolean isClassAvailable() {
        return this.classAvailable;
    }

    public void setClassAvailable(boolean classAvailable) {
        this.classAvailable = classAvailable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.emf.codegen.jet.JETEmitter#generate(org.eclipse.emf.common.util.Monitor, java.lang.Object[],
     * java.lang.String)
     */
    @Override
    public String generate(Monitor progressMonitor, Object[] arguments, String lineDelimiter) throws JETException {
        List list = new ArrayList();
        for (Object o : arguments) {
            if (o instanceof JetBean) {
                list.add(((JetBean) o).getArgument());
                talendEclipseHelper.setHelperJetBean((JetBean) o);
            } else {
                list.add(o);
            }
        }

        getMethod(); // force to load the method before generate in case it was not set before.
        return super.generate(progressMonitor, list.toArray(), lineDelimiter);
    }
}
