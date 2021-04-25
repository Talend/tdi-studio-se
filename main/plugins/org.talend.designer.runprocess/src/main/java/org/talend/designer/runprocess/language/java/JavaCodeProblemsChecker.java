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
package org.talend.designer.runprocess.language.java;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.generation.CodeGenerationUtils;
import org.talend.core.language.CodeProblemsChecker;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.Problem;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.designer.codegen.IAloneProcessNodeConfigurer;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.DetailedProblem;
import org.talend.designer.core.ui.editor.TalendJavaEditor;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;

/**
 * Check syntax of Java expressions.
 *
 * $Id$
 *
 */
public class JavaCodeProblemsChecker extends CodeProblemsChecker {

    /**
     * DOC amaumont PerlExpressionSyntaxChecker constructor comment.
     */
    public JavaCodeProblemsChecker() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.language.perl.CodeSyntaxChecker#checkSyntax(java.lang.String)
     */
    public List<Problem> checkProblemsForExpression(String code) {
        return null;
    }

    /**
     * DOC amaumont Comment method "retrieveCode".
     *
     * @param process
     * @param selectedNodeName
     * @param externalData optional data, can be null
     * @return
     */
    private String retrieveCode(IProcess process, String selectedNodeName, IAloneProcessNodeConfigurer nodeConfigurer) {
        ICodeGeneratorService codeGeneratorService = RunProcessPlugin.getDefault().getCodeGeneratorService();
        ICodeGenerator codeGenerator = codeGeneratorService.createCodeGenerator(process, false, false);
        String code = codeGenerator.generateComponentCodeWithRows(selectedNodeName, nodeConfigurer);
        return code;
    }

    public List<Problem> checkProblems(IAloneProcessNodeConfigurer nodeConfigurer) {
        List<DetailedProblem> list = retrieveDetailedProblems(nodeConfigurer);
        ArrayList<Problem> problems = null;
        if (list != null) {
            problems = processDetailedProblems(list);
        }

        setProblems(problems);
        return problems;
    }

    /**
     * DOC amaumont Comment method "retrieveDetailedProblems".
     *
     * @return
     */
    private List<DetailedProblem> retrieveDetailedProblems(final IAloneProcessNodeConfigurer nodeConfigurer) {

        final ArrayList<DetailedProblem> iproblems = new ArrayList<DetailedProblem>();
        final IWorkbench workbench = PlatformUI.getWorkbench();
        Display display = workbench.getDisplay();
        if (display != null) {
            display.syncExec(new Runnable() {

                public void run() {
                    IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                    IEditorPart editorPart = page.getActiveEditor();
                    if (editorPart instanceof AbstractMultiPageTalendEditor) {
                        AbstractMultiPageTalendEditor multiPageTalendEditor = ((AbstractMultiPageTalendEditor) editorPart);
                        AbstractTalendEditor talendEditor = multiPageTalendEditor.getTalendEditor();
                        IProcess process = talendEditor.getProcess();
                        TalendJavaEditor codeEditor = (TalendJavaEditor) multiPageTalendEditor.getCodeEditor();
                        if (codeEditor == null || AbstractProcessProvider.isExtensionProcessForJoblet(process)) { // joblet
                            return;
                        }
                        org.eclipse.jdt.core.ICompilationUnit compilationUnit = (org.eclipse.jdt.core.ICompilationUnit) codeEditor
                                .getUnit();

                        String selectedNodeName = multiPageTalendEditor.getSelectedNodeName();

                        if (selectedNodeName == null) {
                            return;
                        }

                        String uniqueNodeName = null;

                        boolean found = false;
                        List<? extends INode> generatingNodes = process.getGeneratingNodes();
                        int generatingNodesListSize = generatingNodes.size();
                        for (int i = 0; i < generatingNodesListSize; i++) {
                            INode node = generatingNodes.get(i);
                            /* startsWith method used in case of virtual component such as 'tMap_1_TMAP_OUT' */
                            if (node.getUniqueName().equals(selectedNodeName)
                                    || node.getUniqueName().startsWith(selectedNodeName + "_")) { //$NON-NLS-1$
                                uniqueNodeName = node.getUniqueName();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // in case the component doesn't exist
                            return;
                        }

                        final String code = retrieveCode(process, uniqueNodeName, nodeConfigurer);

                        // System.out.println(code);

                        // create requestor for accumulating discovered problems
                        MyProblemRequestor problemRequestor = new MyProblemRequestor(code, iproblems, selectedNodeName);

                        // use working copy to hold source with error
                        org.eclipse.jdt.core.ICompilationUnit workingCopy = null;
                        try {
                            JavaProcessorUtilities.computeLibrariesPath(process.getNeededModules(TalendProcessOptionConstants.MODULES_DEFAULT), process);
                            try {
                                WorkingCopyOwner workingCopyOwner = new WorkingCopyOwner() {
                                };
                                workingCopy = ((org.eclipse.jdt.core.ICompilationUnit) compilationUnit).getWorkingCopy(
                                        workingCopyOwner, problemRequestor, null);
                                problemRequestor.setReportProblems(true);
                                ((IOpenable) workingCopy).getBuffer().setContents(code);
                                ((org.eclipse.jdt.core.ICompilationUnit) workingCopy).reconcile(ICompilationUnit.NO_AST, true,
                                        null, null);
                                problemRequestor.setReportProblems(false);
                            } finally {
                                if (workingCopy != null) {
                                    workingCopy.discardWorkingCopy();
                                }
                            }
                        } catch (JavaModelException e) {
                            ExceptionHandler.process(e);
                        } catch (CoreException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            });
        }
        //

        return iproblems;
    }

    /**
     * DOC amaumont Comment method "processDetailedProblems".
     *
     * @param list
     * @return
     */
    private ArrayList<Problem> processDetailedProblems(List<DetailedProblem> list) {

        ArrayList<Problem> problems = null;
        if (list != null) {

            problems = new ArrayList<Problem>();
            for (DetailedProblem problemWithLine : list) {
                Problem talendProblem = new Problem();
                talendProblem.setDescription(Messages.getString("JavaCodeProblemsChecker.processDetailError", //$NON-NLS-1$
                        new Object[] { problemWithLine.getSource(), problemWithLine.getJdtProblem().getMessage() }));
                talendProblem.setStatus(Problem.ProblemStatus.ERROR);
                talendProblem.setKey(problemWithLine.getKey());
                problems.add(talendProblem);
            }
        }
        return problems;
    }

    /**
     *
     */
    class MyProblemRequestor implements IProblemRequestor {

        boolean reportProblems;

        Set alreadyAdded = new HashSet();

        String code;

        private String selectedNodeName;

        private ArrayList<DetailedProblem> iproblems;

        /**
         * DOC amaumont MyProblemRequestor constructor comment.
         *
         * @param code
         * @param iproblems
         */
        public MyProblemRequestor(String code, ArrayList<DetailedProblem> iproblems, String selectedNodeName) {
            super();
            this.code = code;
            this.iproblems = iproblems;
            this.selectedNodeName = selectedNodeName;
        }

        public void acceptProblem(IProblem problem) {
            String keyAlreadyAdded = problem.getMessage() + problem.getSourceLineNumber();
            if (reportProblems && problem.isError() && !alreadyAdded.contains(keyAlreadyAdded)) {
                // System.out.println(problem.getID() + ": " + problem.getMessage());
                char[] charArray = this.code.toCharArray();

                String source = String.copyValueOf(charArray, problem.getSourceStart(),
                        problem.getSourceEnd() - problem.getSourceStart() + 1);

                // System.out.println("source=" + source);

                // String key = extractKey(charArray, problem.getSourceStart());
                String key = extractKey(charArray, problem.getSourceEnd());
                if ("".equals(key) || key == null) {
                    String nodeNameKey = getNodeNameKey(code, problem.getSourceLineNumber());
                    if (nodeNameKey != null && selectedNodeName != null && nodeNameKey.equals(selectedNodeName)) {
                        key = selectedNodeName;
                    }
                }
                DetailedProblem detailedProblem = new DetailedProblem(problem, key, source);
                iproblems.add(detailedProblem);
                alreadyAdded.add(keyAlreadyAdded);
            }

        }

        private String getNodeNameKey(String codeCon, int lineNum) {
            int returnline = 0;
            int endline = 0;
            String nodeName = null;
            Pattern patternStart = null;
            Pattern patternEnd = null;
            Perl5Compiler compilerStart = new Perl5Compiler();
            Perl5Compiler compilerEnd = new Perl5Compiler();
            Perl5Matcher matcher = new Perl5Matcher();
            try {
                patternStart = compilerStart.compile("\\*\\s\\[\\s*(\\w+_\\d)+\\s*\\w+\\s*\\]\\s*start");//$NON-NLS-1$
                patternEnd = compilerEnd.compile("\\*\\s\\[\\s*(\\w+_\\d)+\\s*\\w+\\s*\\]\\s*end");
            } catch (MalformedPatternException e) {
                ExceptionHandler.process(e);
                return null;
            }
            LineNumberReader lineReader = new LineNumberReader(new StringReader(codeCon));
            String ste = null;
            try {
                while ((ste = lineReader.readLine()) != null) {
                    int cur = lineReader.getLineNumber();
                    if (cur <= lineNum) {
                        if (matcher.contains(ste, patternStart)) {
                            // System.out.println(ste);
                            MatchResult matchResult = matcher.getMatch();
                            returnline = cur;
                            nodeName = matchResult.group(1);
                        } else if (matcher.contains(ste, patternEnd)) {
                            endline = cur;
                        }
                    } else
                        break;
                }
            } catch (IOException e) {
                ExceptionHandler.process(e);
            } finally {
                try {
                    lineReader.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
            if (endline != 0 && endline > returnline && endline <= lineNum) {
                nodeName = null;
            }
            return nodeName;// new String[] { String.valueOf(returnline), returnString };
        }

        private String extractKey(char[] contents, int start) {

            Perl5Compiler compiler = new Perl5Compiler();
            Perl5Matcher matcher = new Perl5Matcher();
            String key = null;
            String fieldType = null;
            Pattern pattern = null;
            try {
                // exemple of matching string : /** Start field value1:value2:value3 */
                // other exemple of matching string : /** End field xxx:yyyy:zzzz:mmmm */
                pattern = compiler.compile("/\\*\\*\\s*(" + CodeGenerationUtils.START_FIELD + "|" //$NON-NLS-1$ //$NON-NLS-2$
                        + CodeGenerationUtils.END_FIELD + ")\\s*([\\w:]+)\\s*\\*/$", Perl5Compiler.MULTILINE_MASK); //$NON-NLS-1$
            } catch (MalformedPatternException e) {
                ExceptionHandler.process(e);
                return null;
            }

            String firstStringToSearch = "*/"; //$NON-NLS-1$
            int sizeFirstStringToSearch = firstStringToSearch.length();

            for (int i = start + sizeFirstStringToSearch; i < contents.length; i++) {

                boolean parseWithRegExp = String.copyValueOf(contents, i + 1 - sizeFirstStringToSearch, sizeFirstStringToSearch)
                        .equals(firstStringToSearch);

                if (parseWithRegExp) {
                    String stringToParse = String.copyValueOf(contents, start, i - start + 1);
                    if (matcher.contains(stringToParse, pattern)) {
                        MatchResult matchResult = matcher.getMatch();
                        fieldType = matchResult.group(1);
                        if (fieldType.equals(CodeGenerationUtils.START_FIELD)) {
                            // error doesn't come from UI field
                            break;
                        }

                        key = matchResult.group(2);
                        break;
                    }
                }
            }

            return key;
        }

        public void beginReporting() {
        }

        public void endReporting() {
        }

        public boolean isActive() {
            return true;
        } // will detect problems if active

        /**
         * Sets the reportProblems.
         *
         * @param reportProblems the reportProblems to set
         */
        public void setReportProblems(boolean reportProblems) {
            this.reportProblems = reportProblems;
        }

    };

    public static void main(String[] args) {
        JavaCodeProblemsChecker checker = new JavaCodeProblemsChecker();

        String string = "/** Start field xxx:hhhh:gggg */row1.fff /** End field xxx:hhhh:gggg */"; //$NON-NLS-1$
        MyProblemRequestor requestor = checker.new MyProblemRequestor(string, null, "");

        requestor.extractKey(string.toCharArray(), 36);
    }

}
