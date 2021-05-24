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
package org.talend.designer.core.ui.views.problems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.Problem.ProblemType;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.services.ISVNProviderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

import com.ibm.icu.text.MessageFormat;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class Problems {

    /**
     * This enum is used for marking the group type for problems. <br/>
     *
     */
    public enum Group {
        SEVERITY,
        TYPE,
        NONE;
    }

    private static ProblemsView problemView;

    private static List<IProcess> openJobs = new ArrayList<IProcess>();

    // public static List<Node> nodeList = new ArrayList<Node>();
    public static Map<Node, StringBuffer> nodeList = new HashMap<Node, StringBuffer>();

    private static String currentTitle = ""; //$NON-NLS-1$

    private static Group groupField = null;

    public static void setGroupField(Group group) {
        groupField = group;
    }

    static ProblemsView getProblemView() {
        if (problemView == null) {
            problemView = ProblemsView.show();
        }
        return problemView;
    }

    public static Group getGroupField() {
        if (groupField == null) {
            IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
            String key = store.getString(ProblemsView.PROBLEM_TYPE_SELECTION);
            if (key == null || key.length() == 0) {
                groupField = Group.SEVERITY;
            } else {
                groupField = Group.valueOf(key);
            }
        }
        return groupField;
    }

    public static ProblemCategory[] categories = null;

    private static ProblemList problemList = new ProblemList();

    public static void clearAll() {
        problemList.clear();
    }

    public static ProblemList getProblemList() {
        return problemList;
    }

    public static void clearAllComliationError(String javaEditorName) {
        for (Iterator<Problem> iter = problemList.getProblemList().iterator(); iter.hasNext();) {
            Problem problem = iter.next();
            if (problem instanceof TalendProblem) {
                TalendProblem routineProblem = (TalendProblem) problem;
                if (routineProblem.getJavaUnitName() != null && (routineProblem.getJavaUnitName().equals(javaEditorName))) {
                    iter.remove();
                }

            }
        }

    }

    public static String getSummary() {
        int[] counts = problemList.getMarkerCounts();
        // return MessageFormat.format(MarkerMessages.problem_statusSummaryBreakdown, new Object[] { new
        // Integer(counts[0]),
        // new Integer(counts[1]), new Integer(counts[2]) });
        return MessageFormat.format(Messages.getString("MarkerMessages.problem_statusSummaryBreakdown"), new Object[] { //$NON-NLS-1$
                new Integer(counts[0]), new Integer(counts[1]), new Integer(counts[2]) });
    }

    private static void buildHierarchy() {
        Group group = getGroupField();
        if (group.equals(Group.SEVERITY)) {
            categories = new ProblemCategory[3];
            categories[0] = new SeverityProblemCategory(problemList, ProblemStatus.ERROR);
            categories[0].setName(Messages.getString("Problems.category.errors")); //$NON-NLS-1$

            categories[1] = new SeverityProblemCategory(problemList, ProblemStatus.WARNING);
            categories[1].setName(Messages.getString("Problems.category.warnings")); //$NON-NLS-1$

            categories[2] = new SeverityProblemCategory(problemList, ProblemStatus.INFO);
            categories[2].setName(Messages.getString("Problems.category.infos")); //$NON-NLS-1$
        } else if (group.equals(Group.TYPE)) {
            categories = new ProblemCategory[2];
            categories[0] = new TypeProblemCategory(problemList, ProblemType.JOB);
            categories[0].setName(Messages.getString("Problems.category.jobs")); //$NON-NLS-1$

            categories[1] = new TypeProblemCategory(problemList, ProblemType.ROUTINE);
            categories[1].setName(Messages.getString("Problems.category.routines")); //$NON-NLS-1$
        } else {
            categories = null;
        }

    }

    public static List<? extends Problem> getRoot() {
        buildHierarchy();
        if (categories != null) {
            return Arrays.asList(categories);
        } else {
            return problemList.getProblemList();
        }
    }

    public static void clearAll(Node element) {
        removeProblemsByElement(element);
    }

    public static void add(ProblemStatus status, Element element, String description) {
        Problem problem = new Problem(element, description, status);
        add(problem);
    }

    public static void add(ProblemStatus status, IMarker marker, String javaUnitName, String markerErrorMessage, Integer lineN,
            String uniName, Integer charStart, Integer charEnd, ProblemType type, String version) {
        Problem problem = new TalendProblem(status, javaUnitName, marker, markerErrorMessage, lineN, uniName, charStart, charEnd,
                type, version);
        add(problem);
        // addErrorMark();
    }

    public static void add(ProblemStatus status, IMarker marker, Item item, String markerErrorMessage, Integer lineN,
            String uniName, Integer charStart, Integer charEnd, ProblemType type) {
        Problem problem = new TalendProblem(status, item, marker, markerErrorMessage, lineN, uniName, charStart, charEnd, type);
        add(problem);
    }

    public static void addAll(List<Problem> problems) {
        for (Problem current : problems) {
            add(current);
        }
    }

    public static void add(Problem pro) {
        // if (problemList.getProblemList().size() <= CorePlugin.getDefault().getPreferenceStore().getInt(
        // ITalendCorePrefConstants.PREVIEW_LIMIT)) {
        problemList.add(pro);
        // }
    }

    public static List<String> getStatusList(ProblemStatus status, Node element) {
        List<String> statusList = new ArrayList<String>();

        for (Problem problem : problemList.getProblemList()) {
            String elementUniqueName = element.getUniqueName();
            if (problem.getNodeName() != null && problem.getNodeName().equals(elementUniqueName)
                    && problem.getStatus().equals(status) && problem.getJobInfo() != null
                    && problem.getJobInfo().getJobId().equals(element.getProcess().getId())) {
                statusList.add(problem.getDescription());
            }
        }
        return statusList;
    }

    /**
     * DOC set the current process value,according the current process id to initial the current problems.
     *
     * @param process
     */
    public static void addProcess(IProcess process) {
        if (openJobs.contains(process)) {
            return;
        }
        openJobs.add(process);
        initCurrentProblems(process);
    }

    /**
     * DOC check the problems the corresponding of current process .
     */
    private static void initCurrentProblems(IProcess process) {
        // for (IProcess process : openJobs) {
        ((Process) process).checkProcess();
        // }

    }

    public static void recheckCurrentProblems() {
        for (IProcess process : openJobs) {
            ((Process) process).checkNodeProblems();
        }
        if (getProblemView() != null) {
            getProblemView().refresh();
        }
    }

    public static void refreshProcessAllNodesStatus(IProcess process) {
        List<? extends INode> processNodes = process.getGraphicalNodes();
        for (INode inode : processNodes) {
            if (inode instanceof Node) {
                Node node = (Node) inode;
                refreshNodeStatus(node, problemList.getProblemList());
            }
        }
    }

    public static void refreshOneNodeStatus(INode iNode) {
        if (iNode instanceof Node) {
            Node node = (Node) iNode;
            refreshNodeStatus(node, problemList.getProblemList());
        }
    }

    /*
     * DOC xhuang refresh the structure of problems view
     */
    public static void refreshProblemTreeView() {
        if (!isWorkbenchStarted()) {
            return;
        }

        if (ProblemsView.isVisible()) {
            Display.getDefault().syncExec(new Runnable() {

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    getProblemView().refresh();
                }
            });
        }
    }

    /***
     * workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=49316
     */
    private static boolean isWorkbenchStarted() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return false;
        }
        try {
            PlatformUI.getWorkbench();
            PlatformUI.getWorkbench().getWorkbenchWindows();
        } catch (Exception e) {
            return false;
        }
        return PlatformUI.getWorkbench().getWorkbenchWindows().length > 0;
    }

    public static void refreshEditorTitleImage() {
    }

    /**
     * DOC xtan Comment method "refreshRepositoryView".
     */
    public static void refreshRepositoryView() {
        if (!isWorkbenchStarted()) {
            return;
        }

        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
                // TDI-21143 : Studio repository view : remove all refresh call to repo view
                // IRepositoryView viewPart = RepositoryManagerHelper.findRepositoryView();
                // if (viewPart != null) {
                // viewPart.refresh();
                // }
            }
        });
    }

    /**
     * DOC bqian Comment method "refreshNodeStatus".
     *
     * @param node
     * @param problemList2
     */
    private static void refreshNodeStatus(Node node, List<Problem> problemList) {

        boolean hasWarning = false;
        boolean hasError = false;
        boolean hasInfo = false;
        IProcess process = node.getProcess();
        if (process == null) {
            return;
        }
        for (Problem problem : problemList) {
            /* use id and version to filter the problems,see bug 20560 */
            // if problem not instanceof TalendProblem ,node don't operate it.for bug 23088
            if (problem.getJobInfo() != null && problem.getJobInfo().getJobId() != null
                    && problem.getJobInfo().getJobId().equals(process.getId()) && !(problem instanceof TalendProblem)) {
                if (problem.getJobInfo().getJobVersion() != null
                        && problem.getJobInfo().getJobVersion().equals(process.getVersion())) {
                    if (problem.getNodeName() == null) {
                        continue;
                    } else if (problem.getNodeName() != null && (!problem.getNodeName().equals(node.getUniqueName()))) {
                        continue;
                    }
                    if (problem.getStatus().equals(ProblemStatus.INFO)) {
                        hasInfo = true;
                        node.addStatus(Process.INFO_STATUS);
                    } else if (problem.getStatus().equals(ProblemStatus.WARNING)) {
                        hasWarning = true;
                        node.addStatus(Process.WARNING_STATUS);
                    } else if (problem.getStatus().equals(ProblemStatus.ERROR)) {
                        hasError = true;
                        node.addStatus(Process.ERROR_STATUS);
                    }
                }

            }
        }
        if (!hasWarning) {
            node.removeStatus(Process.WARNING_STATUS);
        }
        if (!hasError) {
            node.removeStatus(Process.ERROR_STATUS);
        }
        if (!hasInfo) {
            node.removeStatus(Process.INFO_STATUS);
        }

        node.updateStatus();

    }

    /**
     * DOC bqian Comment method "removeProblemsByProcessId".
     *
     * @param id
     */
    public static void removeProblemsByProcess(IProcess process) {

        for (Iterator<Problem> iter = problemList.getProblemList().iterator(); iter.hasNext();) {
            Problem problem = iter.next();
            if (problem == null || problem instanceof TalendProblem) {
                continue;
            }
            if (problem.getJobInfo() != null
                    && (problem.getJobInfo().getJobId().equals(process.getId()) && problem.getJobInfo().getJobVersion()
                            .equals(process.getVersion()))) {
                iter.remove();
            }

        }
        // refreshProcessAllNodesStatus(process);
        refreshProblemTreeView();
    }

    /**
     *
     * DOC hcyi Comment method "removeProblemsByProcess".
     *
     * @param process
     * @param isDeleted
     */
    public static void removeProblemsByProcess(IProcess process, boolean isDeleted) {
        for (Iterator<Problem> iter = problemList.getProblemList().iterator(); iter.hasNext();) {
            Problem problem = iter.next();
            if (problem == null) {
                continue;
            }
            if (problem.getJobInfo() != null
                    && (problem.getJobInfo().getJobId().equals(process.getId()) && problem.getJobInfo().getJobVersion()
                            .equals(process.getVersion()))) {
                iter.remove();
            }
            // if not open editor then run job , will not fill the JobInfo .
            if (problem.getJobInfo() == null && problem instanceof TalendProblem) {
                TalendProblem talendProblem = (TalendProblem) problem;
                String jobName = talendProblem.getJavaUnitName();
                if (jobName != null && jobName.equals(process.getName())
                        && talendProblem.getVersion().equals(process.getVersion())) {
                    iter.remove();
                }
            }
        }
        refreshProblemTreeView();
    }

    public static void removeJob(IProcess process) {
        openJobs.remove(process);
    }

    public static void removeProblemsByElement(Node element) {
        IProcess process = element.getProcess();
        for (Iterator<Problem> iter = problemList.getProblemList().iterator(); iter.hasNext();) {
            Problem problem = iter.next();
            if (problem.getJobInfo() != null
                    && (problem.getJobInfo().getJobId().equals(process.getId()) && problem.getJobInfo().getJobVersion()
                            .equals(process.getVersion()))) {
                if (problem.getNodeName() != null && (problem.getNodeName().equals(element.getUniqueName()))) {
                    iter.remove();
                }
            }

        }
        refreshProblemTreeView();
    }

    public static void removeProblemsByRoutine(String routineName) {
        for (Iterator<Problem> iter = problemList.getProblemList().iterator(); iter.hasNext();) {
            Problem problem = iter.next();
            if (problem.getType().equals(ProblemType.ROUTINE)) {
                TalendProblem rp = (TalendProblem) problem;
                if (rp.getJavaUnitName().equals(routineName)) {
                    iter.remove();
                }
            }
        }
        refreshProblemTreeView();
    }

    /**
     * Sets the problemView.
     *
     * @param problemView the problemView to set
     */
    public static void setProblemView(ProblemsView problemView) {
        Problems.problemView = problemView;
    }

    public static List<Information> addRoutineFile(IFile file, final Property property, boolean... fromJob) {
        if (file == null) {
            return Collections.emptyList();
        }
        String routineFileName = null;
        String version = null;
        if (property == null) {
            routineFileName = getFileName(file);
        } else {
            routineFileName = property.getLabel();
            version = property.getVersion();
        }
        ProblemType type = ProblemType.NONE;
        if (property.getItem() instanceof RoutineItem) {
            type = ProblemType.ROUTINE;
        } else if (property.getItem() instanceof ProcessItem) {
            type = ProblemType.JOB;
        }

        List<Information> listInfos = addRoutineFile(file, type, routineFileName, version, fromJob);
        if (property == null || listInfos == null) {
            return Collections.emptyList();
        }
        return listInfos;
    }

    /**
     *
     * ggu Comment method "addRoutineFile".
     *
     *
     */
    public static List<Information> addRoutineFile(IFile file, ProblemType type, String label, String version, boolean... fromJob) {
        if (file == null || !file.exists()) {
            return Collections.emptyList();
        }
        String uniName = null;

        List<Information> informations = new ArrayList<Information>();
        try {
            IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);

            Problems.clearAllComliationError(label);
            for (IMarker marker : markers) {
                Integer lineNr = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                String message = (String) marker.getAttribute(IMarker.MESSAGE);
                Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
                Integer end = (Integer) marker.getAttribute(IMarker.CHAR_END);

                if (lineNr != null && message != null && severity != null && start != null && end != null) {
                    Information information = PropertiesFactory.eINSTANCE.createInformation();
                    information.setText(message);
                    informations.add(information);

                    ProblemStatus status = null;
                    switch (severity) {
                    case IMarker.SEVERITY_ERROR:
                        status = ProblemStatus.ERROR;
                        information.setLevel(InformationLevel.ERROR_LITERAL);
                        IPath location = file.getLocation();
                        if (location != null) {
                            String path = location.toString();
                            uniName = setErrorMark(path, lineNr);
                        }
                        break;
                    case IMarker.SEVERITY_WARNING:
                        status = ProblemStatus.WARNING;
                        information.setLevel(InformationLevel.WARN_LITERAL);
                        break;
                    case IMarker.SEVERITY_INFO:
                        status = ProblemStatus.INFO;
                        information.setLevel(InformationLevel.INFO_LITERAL);
                        break;
                    default:
                        break;
                    }
                    if (status != null) {
                        if (status != ProblemStatus.ERROR) {
                            continue;
                        }
                        if ("".equals(uniName) || uniName == null) { //$NON-NLS-1$
                            uniName = "uniName";//$NON-NLS-1$
                        }
                        add(status, marker, label, message, lineNr, uniName, start, end, type, version);
                    }
                }

            }
            if (fromJob.length > 0 && fromJob[0]) {
                addErrorMark();
            }

        } catch (org.eclipse.core.runtime.CoreException e) {
            ExceptionHandler.process(e);
        }

        return informations;
    }

    public static List<Information> addJobRoutineFile(IFile file, ProblemType type, Item item, boolean... fromJob) {
        if (file == null || !file.exists()) {
            return Collections.emptyList();
        }
        String uniName = null;

        List<Information> informations = new ArrayList<Information>();
        try {
            Set<IMarker> fullMarker = new HashSet<IMarker>();

            IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
            if (markers != null) {
                fullMarker.addAll(Arrays.asList(markers));
            }
            // remove old
            Problems.clearAllComliationError(item.getProperty().getLabel());

            for (IMarker marker : fullMarker) {
                Integer lineNr = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                String message = (String) marker.getAttribute(IMarker.MESSAGE);
                Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
                Integer end = (Integer) marker.getAttribute(IMarker.CHAR_END);

                if (lineNr != null && message != null && severity != null && start != null && end != null) {
                    Information information = PropertiesFactory.eINSTANCE.createInformation();
                    information.setText(message);
                    informations.add(information);

                    ProblemStatus status = null;
                    switch (severity) {
                    case IMarker.SEVERITY_ERROR:
                        status = ProblemStatus.ERROR;
                        information.setLevel(InformationLevel.ERROR_LITERAL);
                        IPath location = file.getLocation();
                        if (location != null) {
                            String path = location.toString();
                            uniName = setErrorMark(path, lineNr);
                        }
                        break;
                    case IMarker.SEVERITY_WARNING:
                        status = ProblemStatus.WARNING;
                        information.setLevel(InformationLevel.WARN_LITERAL);
                        break;
                    case IMarker.SEVERITY_INFO:
                        status = ProblemStatus.INFO;
                        information.setLevel(InformationLevel.INFO_LITERAL);
                        break;
                    default:
                        break;
                    }
                    if (status != null) {
                        if (status != ProblemStatus.ERROR) {
                            continue;
                        }
                        if ("".equals(uniName) || uniName == null) { //$NON-NLS-1$
                            uniName = "uniName";//$NON-NLS-1$
                        }
                        add(status, marker, item, message, lineNr, uniName, start, end, type);
                    }
                }

            }
            if (fromJob.length > 0 && fromJob[0]) {
                addErrorMark();
            }

        } catch (org.eclipse.core.runtime.CoreException e) {
            ExceptionHandler.process(e);
        }

        return informations;
    }

    @SuppressWarnings("restriction")
    private static List<Problem> computeCompilationUnit(IFile file, ProblemType type, Item item) throws CoreException {
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        // FIXME, only for standard job first, also for JobLaunchShortcut.launch
        if (itemType == null || !itemType.equals(ERepositoryObjectType.PROCESS)) {
            return Collections.emptyList();
        }
        List<Problem> compilProblems = new ArrayList<Problem>();
        final ICompilationUnit unit = JavaPlugin.getDefault().getWorkingCopyManager()
                .getWorkingCopy(new FileEditorInput(file), false);
        if (unit != null) {
            CompilationUnit ast = unit.reconcile(ASTProvider.SHARED_AST_LEVEL, ICompilationUnit.FORCE_PROBLEM_DETECTION, null,
                    null);
            IProblem[] problems = ast.getProblems();
            if (problems != null) {
                for (IProblem p : problems) {
                    String[] arguments = p.getArguments();
                    int id = p.getID();
                    String message = p.getMessage();
                    int sourceLineNumber = p.getSourceLineNumber();
                    int sourceStart = p.getSourceStart();
                    int sourceEnd = p.getSourceEnd();

                    String uniName = null;
                    IPath location = file.getLocation();
                    if (location != null) {
                        String path = location.toString();
                        uniName = setErrorMark(path, sourceLineNumber);
                    }
                    ProblemStatus status = ProblemStatus.WARNING;
                    if (p.isError()) {
                        status = ProblemStatus.ERROR;
                    }

                    TalendProblem tp = new TalendProblem(status, item, null, message, sourceLineNumber, uniName, sourceStart,
                            sourceEnd, type);
                    compilProblems.add(tp);
                }
            }

        }
        return compilProblems;
    }

    /**
     * See also AbstractEMFRepositoryFactory.computePropertyMaxInformationLevel
     *
     * @param property
     */
    public static void computePropertyMaxInformationLevel(Property property, boolean allowUpdateProperty) {
        EList<Information> informations = property.getInformations();
        InformationLevel maxLevel = null;
        for (int i = 0; i < informations.size(); i++) {
            Information information = informations.get(i);
            if (i == 0) {
                maxLevel = information.getLevel();
                continue;
            }
            int value = information.getLevel().getValue();
            if (maxLevel == null || value > maxLevel.getValue()) {
                maxLevel = information.getLevel();
            }
        }
        // TDI-22098
        // after run the job , if the modified the property ,should be save .
        InformationLevel propertyMaxLevel = property.getMaxInformationLevel();
        boolean isModified = false;
        if (maxLevel != null) {
            if (!maxLevel.equals(propertyMaxLevel)) {
                property.setMaxInformationLevel(maxLevel);
                isModified = true;
            }
        } else if (!InformationLevel.DEBUG_LITERAL.equals(propertyMaxLevel)) {
            property.setMaxInformationLevel(InformationLevel.DEBUG_LITERAL);
            isModified = true;
        }
        // save the property
        if (isModified && allowUpdateProperty) {
            IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            Item item = property.getItem();
            try {
                factory.save(item, false);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    private static boolean isSVN() {
        boolean isSvn = false;
        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }
        if (service != null && service.isProjectInSvnMode()) {
            isSvn = service.isProjectInSvnMode();
        }
        return isSvn;
    }

    private static String getFileName(IFile file) {
        if (file == null) {
            return ""; //$NON-NLS-1$
        }
        String fileName = file.getName();
        String ext = file.getFileExtension();
        if (ext == null || "".equals(ext)) { //$NON-NLS-1$
            return fileName;
        }
        fileName = fileName.substring(0, fileName.lastIndexOf(ext) - 1);
        return fileName;
    }

    public static String setErrorMark(String path, int lineNum) {
        String uniName = null;

        String[][] result = matchString(path, lineNum);

        int first = 0;
        int second = 0;
        if (result != null) {
            if (result[0][0] != null) {
                first = Integer.parseInt(result[0][0]);
            }
            if (result[1][0] != null) {
                second = Integer.parseInt(result[1][0]);

            }
        }

        if (lineNum > first && lineNum < second && result != null) {
            int index1 = 0;
            int index2 = 0;
            int inde1 = 0;
            int inde2 = 0;
            if (result[1][1] != null) {
                index1 = result[1][1].indexOf("[");//$NON-NLS-1$
                index2 = result[1][1].indexOf("]");//$NON-NLS-1$
            }

            if (result[0][1] != null) {
                inde1 = result[0][1].indexOf("[");//$NON-NLS-1$
                inde2 = result[0][1].indexOf("]");//$NON-NLS-1$
            }

            String uniNameFir = null;
            String uniNameSec = null;
            if (index1 > 0 && index2 > index1) {
                String nodeAllName = result[1][1].substring(index1, index2);
                int index3 = nodeAllName.indexOf(" ");//$NON-NLS-1$
                if (index3 > 0) {
                    uniNameSec = nodeAllName.substring(1, index3);
                }

            }

            if (inde1 > 0 && inde2 > inde1) {
                String nodeAllName = result[0][1].substring(inde1, inde2);
                int index3 = nodeAllName.indexOf(" ");//$NON-NLS-1$
                if (index3 > 0) {
                    uniNameFir = nodeAllName.substring(1, index3);
                }

            }

            if ((uniNameFir != null) && (uniNameSec != null) && (uniNameFir.equals(uniNameSec))) {
                uniName = uniNameFir;
            }

        }
        return uniName;
    }

    public static String[][] matchString(String path, int lineNum) {
        String[][] result = new String[2][2];
        Pattern patternStart = Pattern.compile("\\[\\s*(\\w)+_\\d+\\s*\\w+\\s*\\]\\s*start");//$NON-NLS-1$
        Pattern patternStop = Pattern.compile("\\[\\s*(\\w)+_\\d+\\s*\\w+\\s*\\]\\s*stop");//$NON-NLS-1$
        File file = new File(path);
        FileReader fread = null;
        try {
            fread = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fread);
        int point = 0;
        String str = null;
        String strtmp = null;
        int tmp = 0;
        int min = lineNum;
        try {
            while ((str = br.readLine()) != null) {
                point++;
                strtmp = str.trim();
                if (point < lineNum) {
                    Matcher matchStart = patternStart.matcher(strtmp);
                    if (matchStart.find()) {
                        tmp = lineNum - point;
                        if (tmp < min) {
                            min = tmp;
                            result[0][0] = String.valueOf(point);
                            result[0][1] = str;
                        }
                    }

                } else if (point > lineNum) {
                    Matcher matchStop = patternStop.matcher(strtmp);
                    if (matchStop.find()) {
                        result[1][0] = String.valueOf(point);
                        result[1][1] = str;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fread.close();
                file = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void addErrorMark() {
        nodeList.clear();
        String befor = "Error in the component's properties:";
        for (IProcess process : openJobs) {
            if (((Process) process).isActivate()) {
                for (INode inode : ((Process) process).getGraphicalNodes()) {
                    if (inode instanceof Node) {
                        Node node = (Node) inode;
                        if (problemList.getProblemList().size() > 0) {
                            for (int i = 0; i < problemList.getProblemList().size(); i++) {
                                Problem problem = problemList.getProblemList().get(i);
                                if (node.isActivate()) {
                                    if (problem.getStatus().equals(ProblemStatus.ERROR)) {
                                        if (problem instanceof TalendProblem) {
                                            TalendProblem tProblem = (TalendProblem) problem;
                                            if (!tProblem.getJavaUnitName().equals(node.getProcess().getName())) {
                                                continue;
                                            }
                                            if (tProblem.getVersion() != null) {
                                                if (!tProblem.getVersion().equals(node.getProcess().getVersion())) {
                                                    continue;
                                                }
                                            }
                                            if (tProblem.getUnitName() != null
                                                    && tProblem.getUnitName().equals(node.getUniqueName())) {
                                                // nodeList.add(node);
                                                if (nodeList.get(node) != null) {
                                                    nodeList.get(node).append("\r\n");
                                                    nodeList.get(node).append(tProblem.getDescription());
                                                } else {
                                                    nodeList.put(node, new StringBuffer(tProblem.getDescription()));
                                                }

                                            } else {
                                                if (node.isErrorFlag() == true) {
                                                    node.setErrorFlag(false);
                                                    node.setCompareFlag(false);
                                                    node.setErrorInfo(befor + tProblem.getDescription());
                                                    node.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                                                    node.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                                                } else {
                                                    continue;
                                                }
                                            }

                                        }
                                    } else {
                                        if (node.isErrorFlag() == true) {
                                            node.setErrorFlag(false);
                                            node.setCompareFlag(false);
                                            node.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                                        } else {
                                            continue;
                                        }
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            if (node.isErrorFlag() == true) {
                                node.setErrorFlag(false);
                                node.setCompareFlag(false);
                                node.setErrorInfo(null);
                                node.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                                node.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                            } else {
                                continue;
                            }
                        }
                    }
                }
            } else {
                continue;
            }
        }
        Iterator<Entry<Node, StringBuffer>> set = nodeList.entrySet().iterator();
        while (set.hasNext()) {
            Entry<Node, StringBuffer> en = set.next();
            Node node = en.getKey();
            String des = en.getValue().toString();

            if (node.isErrorFlag() == false) {
                node.setErrorFlag(true);
                node.setCompareFlag(false);
                node.setErrorInfo(befor + des);
                node.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                node.setErrorInfoChange("ERRORINFO", true);//$NON-NLS-1$
            }
        }
    }
}
