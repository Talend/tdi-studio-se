// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.build;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TestProcessor implements IProcessor {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#cleanBeforeGenerate(int)
     */
    @Override
    public void cleanBeforeGenerate(int options) throws ProcessorException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateCode(boolean, boolean, boolean)
     */
    @Override
    public void generateCode(boolean statistics, boolean trace, boolean context) throws ProcessorException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateCode(boolean, boolean, boolean, int)
     */
    @Override
    public void generateCode(boolean statistics, boolean trace, boolean context, int option) throws ProcessorException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateContextCode()
     */
    @Override
    public void generateContextCode() throws ProcessorException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateEsbFiles()
     */
    @Override
    public void generateEsbFiles() throws ProcessorException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String)
     */
    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam) throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String, java.lang.String,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam, String log4jLevel, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(java.lang.String[], int, int)
     */
    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort) throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(java.lang.String[], int, int,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeContext()
     */
    @Override
    public String getCodeContext() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodePath()
     */
    @Override
    public IPath getCodePath() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getContextPath()
     */
    @Override
    public IPath getContextPath() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getDataSetPath()
     */
    @Override
    public IPath getDataSetPath() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeProject()
     */
    @Override
    public IProject getCodeProject() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getTalendJavaProject()
     */
    @Override
    public ITalendProcessJavaProject getTalendJavaProject() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getLineNumber(java.lang.String)
     */
    @Override
    public int getLineNumber(String nodeName) {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getInterpreter()
     */
    @Override
    public String getInterpreter() throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setInterpreter(java.lang.String)
     */
    @Override
    public void setInterpreter(String interpreter) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setLibraryPath(java.lang.String)
     */
    @Override
    public void setLibraryPath(String libraryPath) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeLocation()
     */
    @Override
    public String getCodeLocation() throws ProcessorException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setCodeLocation(java.lang.String)
     */
    @Override
    public void setCodeLocation(String codeLocation) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProcessorType()
     */
    @Override
    public String getProcessorType() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setProcessorStates(int)
     */
    @Override
    public void setProcessorStates(int states) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.runprocess.IProcessor#setSyntaxCheckableEditor(org.talend.designer.core.ISyntaxCheckableEditor
     * )
     */
    @Override
    public void setSyntaxCheckableEditor(ISyntaxCheckableEditor editor) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getTypeName()
     */
    @Override
    public String getTypeName() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#saveLaunchConfiguration()
     */
    @Override
    public Object saveLaunchConfiguration() throws CoreException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCommandLine(boolean, boolean, int, int, java.lang.String[])
     */
    @Override
    public String[] getCommandLine(boolean needContext, boolean externalUse, int statOption, int traceOption,
            String... codeOptions) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setContext(org.talend.core.model.process.IContext)
     */
    @Override
    public void setContext(IContext context) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getTargetPlatform()
     */
    @Override
    public String getTargetPlatform() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setTargetPlatform(java.lang.String)
     */
    @Override
    public void setTargetPlatform(String targetPlatform) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#initPath()
     */
    @Override
    public void initPath() throws ProcessorException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProcess()
     */
    @Override
    public IProcess getProcess() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getContext()
     */
    @Override
    public IContext getContext() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProperty()
     */
    @Override
    public Property getProperty() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#isCodeGenerated()
     */
    @Override
    public boolean isCodeGenerated() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setCodeGenerated(boolean)
     */
    @Override
    public void setCodeGenerated(boolean codeGenerated) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProxyParameters()
     */
    @Override
    public String[] getProxyParameters() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setProxyParameters(java.lang.String[])
     */
    @Override
    public void setProxyParameters(String[] proxyParameters) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#syntaxCheck()
     */
    @Override
    public void syntaxCheck() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getMainClass()
     */
    @Override
    public String getMainClass() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getJVMArgs()
     */
    @Override
    public String[] getJVMArgs() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getNeededModules()
     */
    @Override
    public Set<ModuleNeeded> getNeededModules() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getBuildChildrenJobs()
     */
    @Override
    public Set<JobInfo> getBuildChildrenJobs() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setOldBuildJob(boolean)
     */
    @Override
    public void setOldBuildJob(boolean oldBuildJob) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#build(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void build(IProgressMonitor monitor) throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getArguments()
     */
    @Override
    public Map<String, Object> getArguments() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setArguments(java.util.Map)
     */
    @Override
    public void setArguments(Map<String, Object> argumentsMap) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#cleanWorkingDirectory()
     */
    @Override
    public void cleanWorkingDirectory() throws SecurityException {

    }

}
