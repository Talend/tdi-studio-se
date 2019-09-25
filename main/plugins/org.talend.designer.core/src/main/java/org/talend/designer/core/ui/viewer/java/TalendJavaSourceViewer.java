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
package org.talend.designer.core.ui.viewer.java;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.viewer.ReconcilerViewer;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class TalendJavaSourceViewer extends ReconcilerViewer {

    private String filename;

    private static String className;

    private Process process;

    public static final String VIEWER_CLASS_NAME = "TalendJavaSourceViewer"; //$NON-NLS-1$

    private static int currentId = 0;

    private ICompilationUnit compilationUnit;

    private static final String NL = "\n"; //$NON-NLS-1$

    private static final String TEXT_1 = NL + "    // create and load default properties" + NL //$NON-NLS-1$
            + "\tprivate static java.util.Properties defaultProps = new java.util.Properties();" + NL //$NON-NLS-1$
            + "\t// create application properties with default" + NL //$NON-NLS-1$
            + "\tprivate static class ContextProperties extends java.util.Properties {" + NL + "\t    " + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "\t    private static final long serialVersionUID = 1L;" + NL + NL
            + "\t    public ContextProperties(java.util.Properties properties){" + NL + "\t        super(properties);" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "\t    }" + NL + "\t    public ContextProperties(){" + NL + "\t        super();" + NL + "\t    }" + NL + "\t    " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            + NL + "\t\tpublic void synchronizeContext(){" + NL + "\t\t\t"; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String TEXT_2 = NL + "\t\t\tthis.put(\""; //$NON-NLS-1$

    private static final String TEXT_3 = "\", "; //$NON-NLS-1$

    private static final String TEXT_4 = ");" + NL + "\t\t\t"; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String TEXT_5 = NL + "\t\t}" + NL + "\t\t\t    "; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String TEXT_6 = NL + "     public  static String "; //$NON-NLS-1$

    private static final String TEXT_7 = ";"; //$NON-NLS-1$

    private static final String TEXT_8 = "      " + NL + " public  static "; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String TEXT_9 = "  "; //$NON-NLS-1$

    private static final String TEXT_10 = ";"; //$NON-NLS-1$

    private static final String TEXT_11 = NL + "\t}"; //$NON-NLS-1$

    /**
     * DOC nrousseau TalendJavaSourceViewer2 constructor comment.
     *
     * @param parent
     * @param process
     * @param verticalRuler
     * @param overviewRuler
     * @param showAnnotationsOverview
     * @param styles
     * @param annotationAccess
     * @param sharedColors
     * @param checkCode
     */
    public TalendJavaSourceViewer(Composite parent, Process process, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles, IAnnotationAccess annotationAccess, ISharedTextColors sharedColors,
            boolean checkCode, IDocument document) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, annotationAccess, sharedColors, checkCode,
                document, null);
        this.process = process;
        int id = currentId++;
        className = TalendJavaSourceViewer.VIEWER_CLASS_NAME + id;
        filename = TalendJavaSourceViewer.VIEWER_CLASS_NAME + id++ + JavaUtils.JAVA_EXTENSION;
        IPackageFragment packageFragment;
        try {

            IRunProcessService runProcessService = getRunProcessService();
            ITalendProcessJavaProject talendProcessJavaProject = null;
            if (runProcessService != null) {
                if (process != null) {
                    talendProcessJavaProject = runProcessService.getTalendJobJavaProject(process.getProperty());
                } else {
                    talendProcessJavaProject = runProcessService.getTempJavaProject();
                }
                if (talendProcessJavaProject == null) {
                    return;
                }
                IPackageFragmentRoot root = talendProcessJavaProject.getJavaProject().getPackageFragmentRoot(
                        talendProcessJavaProject.getSrcFolder());
                packageFragment = root.getPackageFragment(JavaUtils.JAVA_INTERNAL_DIRECTORY);
                if (packageFragment != null) {
                    compilationUnit = packageFragment.createCompilationUnit(filename, document.get(), false,
                            new NullProgressMonitor());
                }
            }
        } catch (JavaModelException e) {
            ExceptionHandler.process(e);
        }
        updateContents();
    }

    private static IRunProcessService getRunProcessService() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IService service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
            return (IRunProcessService) service;
        }
        return null;
    }

    public static ISourceViewer createViewer(Composite composite, int styles, boolean checkCode) {
        return createViewer(composite, null, styles, checkCode);
    }

    public static ISourceViewer createViewer(Composite composite, Process process, int styles, boolean checkCode) {
        /*
         * Special for cProcessor https://jira.talendforge.org/browse/TESB-3687 for cJavaDSLProcessor
         * https://jira.talendforge.org/browse/TESB-7615
         */
        boolean isRouteProcess = isRouteProcess();

        StringBuffer buff = new StringBuffer();
        buff.append("package internal;\n\n"); //$NON-NLS-1$
        buff.append(getImports());

        // add special imports for RouteBuilder
        if (isRouteProcess) {
            buff.append("import org.apache.camel.*;");
        }

        buff.append("public class " + VIEWER_CLASS_NAME + currentId + " {\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buff.append("\tprivate static java.util.Properties context = new java.util.Properties();\n"); //$NON-NLS-1$
        buff.append("\tprivate static final java.util.Map<String, Object> globalMap = new java.util.HashMap<String, Object>();\n"); //$NON-NLS-1$

        /*
         * Special for cProcessor https://jira.talendforge.org/browse/TESB-3687
         */
        if (isRouteProcess) {
            buff.append("\tprivate org.apache.camel.Exchange exchange;\n"); //$NON-NLS-1$
            buff.append("\torg.apache.camel.impl.DefaultCamelContext camelContext;\n"); //$NON-NLS-1$
            buff.append("\tjavax.jms.ConnectionFactory jmsConnectionFactory;\n"); //$NON-NLS-1$
        }
        // end of https://jira.talendforge.org/browse/TESB-3687

        buff.append("\tpublic void myFunction(){\n"); //$NON-NLS-1$
        buff.append("\t  if( \n"); //$NON-NLS-1$

        /*
         * for cJavaDSLProcessor https://jira.talendforge.org/browse/TESB-7615
         */
        if (isRouteProcess) {
            buff.append("new org.apache.camel.model.RouteDefinition()\n");
        }
        // End of https://jira.talendforge.org/browse/TESB-7615

        buff.append("\n");
        int documentOffset = buff.toString().length();
        buff.append("\n");

        buff.append("){\n\t}"); //$NON-NLS-1$
        buff.append("\n\t\n}\n}"); //$NON-NLS-1$

        IDocument document = new Document();
        document.set(buff.toString());
        return initializeViewer(composite, process, styles, checkCode, document, documentOffset);
    }

    /**
     * if the current process is a route process or not
     *
     * @return
     */
    private static boolean isRouteProcess() {
        try {
            IRunProcessService runProcessService = getRunProcessService();
            if (runProcessService == null) {
                return false;
            }
            IProcess activeProcess = runProcessService.getActiveProcess();
            boolean isRouteProcess = (activeProcess != null && "org.talend.camel.designer.ui.editor.RouteProcess"
                    .equals(activeProcess.getClass().getName()));
            return isRouteProcess;
        } catch (Throwable e) {
            return false;
        }
    }

    public static ISourceViewer createViewerForIfConnection(Composite composite, Process process) {
        StringBuffer buff = new StringBuffer();
        buff.append("package internal;\n\n"); //$NON-NLS-1$
        buff.append(getImports());
        buff.append("public class " + VIEWER_CLASS_NAME + currentId + " {\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buff.append("\tprivate static java.util.Properties context = new java.util.Properties();\n"); //$NON-NLS-1$
        buff.append("\tprivate static final java.util.Map<String, Object> globalMap = new java.util.HashMap<String, Object>();\n"); //$NON-NLS-1$

        buff.append("\tpublic void myFunction(){\n"); //$NON-NLS-1$
        buff.append("\t  if( \n"); //$NON-NLS-1$

        buff.append("\n");
        int documentOffset = buff.toString().length();
        buff.append("\n");

        buff.append("\n){\n\t}"); //$NON-NLS-1$
        buff.append("\n\t\n}}"); //$NON-NLS-1$

        IDocument document = new Document();
        document.set(buff.toString());
        boolean checkCode = false;
        int styles = SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP;
        return initializeViewer(composite, process, styles, checkCode, document, documentOffset);
    }

    public static ReconcilerViewer createViewerWithVariables(Composite composite, int styles, IExpressionDataBean dataBean) {
        IDocument document = new Document();
        StringBuffer buff = new StringBuffer();
        buff.append("\npackage internal;\n\n"); //$NON-NLS-1$
        buff.append(getImports());
        buff.append("public class " + VIEWER_CLASS_NAME + currentId + " {\n"); //$NON-NLS-1$ //$NON-NLS-2$
        IRunProcessService runProcessService = getRunProcessService();
        if (runProcessService != null && runProcessService.getSelectedContext() != null) {

            List<IContextParameter> params = runProcessService.getSelectedContext().getContextParameterList();
            buff.append(TEXT_1);
            for (IContextParameter ctxParam : params) {
                buff.append(TEXT_2);
                buff.append(ctxParam.getName());
                buff.append(TEXT_3);
                buff.append(ctxParam.getName());
                buff.append(TEXT_4);
            }
            buff.append(TEXT_5);
            for (IContextParameter ctxParam : params) {

                if (ctxParam.getType().equals("id_List Of Value") || ctxParam.getType().equals("id_File") //$NON-NLS-1$ //$NON-NLS-2$
                        || ctxParam.getType().equals("id_Directory") || ctxParam.getType().equals("id_Character")) { //$NON-NLS-1$ //$NON-NLS-2$

                    buff.append(TEXT_6);
                    buff.append(ctxParam.getName());
                    buff.append(TEXT_7);

                } else {

                    buff.append(TEXT_8);
                    buff.append(JavaTypesManager.getTypeToGenerate(ctxParam.getType(), true));
                    buff.append(TEXT_9);
                    buff.append(ctxParam.getName());
                    buff.append(TEXT_10);
                }
            }
            buff.append(TEXT_11);

            buff.append("\tprivate static ContextProperties context = new ContextProperties();\n"); //$NON-NLS-1$
            buff.append("\tprivate static final java.util.Map<String, Object> globalMap = new java.util.HashMap<String, Object>();\n"); //$NON-NLS-1$

            if (dataBean != null) {
                buff.append(parseVariables(dataBean.getVariables()));
                buff.append("\tpublic " + dataBean.getExpressionType() + " myFunction(){\n"); //$NON-NLS-1$ //$NON-NLS-2$

            }

            buff.append("\t\treturn \n"); //$NON-NLS-1$
        }

        buff.append("\n");
        int length = buff.toString().length();
        buff.append("\n");

        String defaultValue = ""; //$NON-NLS-1$
        buff.append(defaultValue + "\n;\t\n}\n}"); //$NON-NLS-1$

        document.set(buff.toString());

        return initializeViewer(composite, null, styles, true, document, length);
    }

    public static ISourceViewer createViewerForComponent(Composite composite, int styles, Process process, CompilationUnitEditor editor,
            List<Variable> variableList, String uniqueName, String codePart) {

        String selectedPart = "[" + uniqueName + " " + codePart + " ] start"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        IDocument originalDocument = editor.getDocumentProvider().getDocument(editor.getEditorInput());

        int documentOffset = -1;
        String globalFields = ""; //$NON-NLS-1$
        String localFields = ""; //$NON-NLS-1$
        globalFields = "\tprivate static java.util.Properties context = new java.util.Properties();\n"; //$NON-NLS-1$
        globalFields += "\tprivate static final java.util.Map<String, Object> globalMap = new java.util.HashMap<String, Object>();\n"; //$NON-NLS-1$

        IDocument newDoc = new Document();
        boolean checkCode = false;

        FindReplaceDocumentAdapter frda = null;
        String documentText = originalDocument.get(); // hywang modified for bug 9180
        if (documentText != null && !documentText.equals("") && documentText.length() > 0) { //$NON-NLS-1$
            frda = new FindReplaceDocumentAdapter(originalDocument);
        }
        try {
            Region region = null;
            if (frda != null) {
                region = (Region) frda.find(0, selectedPart, true, false, false, false);
            }
            if (region != null) {
                checkCode = true;
                documentOffset = region.getOffset();
                documentOffset = originalDocument.getLineOffset(originalDocument.getLineOfOffset(documentOffset) + 2);
                JavaCompletionProcessor processor = new JavaCompletionProcessor(editor, new ContentAssistant(),
                        IDocument.DEFAULT_CONTENT_TYPE);

                boolean globalFieldsDone = false;
                globalFields = ""; //$NON-NLS-1$
                String className = editor.getPartName().substring(0, editor.getPartName().indexOf('.') + 1);

                ICompletionProposal[] proposals = processor.computeCompletionProposals(editor.getViewer(), documentOffset);
                for (ICompletionProposal curProposal : proposals) {
                    if (curProposal instanceof JavaCompletionProposal) {
                        JavaCompletionProposal javaProposal = ((JavaCompletionProposal) curProposal);

                        if (javaProposal.getJavaElement() instanceof SourceField) {
                            globalFieldsDone = true;
                            SourceField sourceField = (SourceField) javaProposal.getJavaElement();
                            globalFields += "\t" + sourceField.getSource() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                            // System.out.println();
                        }
                        if (javaProposal.getJavaElement() == null && !globalFieldsDone) {
                            String[] str = javaProposal.getSortString().split(" "); //$NON-NLS-1$
                            String variable = ""; //$NON-NLS-1$
                            for (int i = str.length - 1; i >= 0; i--) {
                                variable += str[i].length() == 0 ? " " : str[i]; //$NON-NLS-1$
                            }
                            if (variable.contains(className)) {
                                continue;
                            }
                            variable += ";"; //$NON-NLS-1$
                            localFields += "\t\t" + variable + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                            // System.out.println(variable);
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            ExceptionHandler.process(e);
        } catch (JavaModelException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        StringBuffer buff = new StringBuffer();
        buff.append("package internal;\n\n"); //$NON-NLS-1$
        buff.append(getImports());
        buff.append("public class " + VIEWER_CLASS_NAME + currentId + " {\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buff.append(globalFields);
        buff.append("\tpublic void myFunction(){\n"); //$NON-NLS-1$
        buff.append(localFields);

        buff.append("\n");
        documentOffset = buff.toString().length();
        buff.append("\n");

        buff.append("\n\t\n}\n}"); //$NON-NLS-1$

        newDoc.set(buff.toString());

        return initializeViewer(composite, process, styles, checkCode, newDoc, documentOffset);
    }

    private static ReconcilerViewer initializeViewer(Composite composite, Process process, int styles, boolean checkCode, IDocument document,
            int visibleOffset) {
        IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        ISharedTextColors sharedColors = JavaPlugin.getDefault().getJavaTextTools().getColorManager();
        IOverviewRuler overviewRuler = null;
        IVerticalRuler verticalRuler = null;
        if (checkCode) {
            overviewRuler = new OverviewRuler(annotationAccess, 12, sharedColors);
            Iterator e = EditorsPlugin.getDefault().getMarkerAnnotationPreferences().getAnnotationPreferences().iterator();
            while (e.hasNext()) {
                AnnotationPreference preference = (AnnotationPreference) e.next();
                if (preference.contributesToHeader()) {
                    overviewRuler.addHeaderAnnotationType(preference.getAnnotationType());
                }
            }
        }
        verticalRuler = new CompositeRuler(12);
        ReconcilerViewer viewer = new TalendJavaSourceViewer(composite, process, verticalRuler, overviewRuler, checkCode, styles,
                annotationAccess, sharedColors, checkCode, document);

        if (visibleOffset != -1) {
            viewer.setVisibleRegion(visibleOffset, 0);
        }

        return viewer;
    }

    private static String getImports() {
        String imports = ""; //$NON-NLS-1$

        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IRepositoryService.class)) {
            return imports;
        }
        IRepositoryService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);

        IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
        try {
            List<IRepositoryViewObject> routines = repositoryFactory.getAll(ERepositoryObjectType.ROUTINES);
            for (IRepositoryViewObject routine : routines) {
                if (routine.getProperty().getItem() instanceof RoutineItem
                        && ((RoutineItem) routine.getProperty().getItem()).isBuiltIn()) {

                    imports += "import routines." + routine.getLabel() + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    imports += "import routines." + routine.getLabel() + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ""; //$NON-NLS-1$
        }

        imports += "import routines.system.*;\n"; //$NON-NLS-1$
        imports += "import java.text.ParseException;\n"; //$NON-NLS-1$
        imports += "import java.text.SimpleDateFormat;\n"; //$NON-NLS-1$
        imports += "import java.util.Date;\n"; //$NON-NLS-1$
        imports += "import java.util.List;\n"; //$NON-NLS-1$
        imports += "import java.math.BigDecimal;\n"; //$NON-NLS-1$
        imports += "\n";
        imports += "@SuppressWarnings(\"unused\")";
        imports += "\n\n";
        return imports;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.viewer.ReconcilerViewer#initializeModel(IDocument document)
     */
    @Override
    protected void initializeModel() {
        getSourceViewerDecorationSupport().install(JavaPlugin.getDefault().getCombinedPreferenceStore());
        this.setRangeIndicator(new DefaultRangeIndicator());

        IAnnotationModel model;
        IDocument document;
        if (checkCode) {
            IDocumentProvider provider = JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
            IEditorInput ei = new FileEditorInput(file);
            try {
                provider.connect(ei);
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
            document = provider.getDocument(ei);
            model = provider.getAnnotationModel(ei);
        } else {
            model = new AnnotationModel();
            document = getDocument();
            model.connect(document);
        }

        if (document != null) {
            setDocument(document, model);
            showAnnotations(model != null && checkCode);
        }
        super.initializeModel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.viewer.ReconcilerViewer#installViewerConfiguration()
     */
    @Override
    protected void installViewerConfiguration() {
        JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
        tools.setupJavaDocumentPartitioner(getDocument(), IJavaPartitions.JAVA_PARTITIONING);
        IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
        configure(new TalendJavaViewerConfiguration((IColorManager) sharedColors, store, this));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.viewer.ReconcilerViewer#setContents(org.eclipse.jface.text.IDocument)
     */
    @Override
    public void updateContents() {
        if (getDocument() == null) {
            return;
        }
        InputStream codeStream = new ByteArrayInputStream(getDocument().get().getBytes());
        try {
            IRunProcessService runProcessService = getRunProcessService();
            if (file == null && runProcessService != null) {
                ITalendProcessJavaProject talendProcessJavaProject = null;
                if (process != null) {
                    talendProcessJavaProject = runProcessService.getTalendJobJavaProject(process.getProperty());
                } else {
                    talendProcessJavaProject = runProcessService.getTempJavaProject();
                }
                if (talendProcessJavaProject == null) {
                    return;
                }
                file = talendProcessJavaProject.getSrcFolder().getFile(JavaUtils.JAVA_INTERNAL_DIRECTORY + '/' + filename);
                file.setContents(codeStream, true, false, null);
                initializeModel();
            } else {
                // only happen sometimes with a "fast click" (file don't exist yet).
                if (getViewerRegion().getLength() == 0) {
                    return;
                } else {
                    file.setContents(codeStream, true, false, null);
                }
            }
        } catch (CoreException e) {
            // don't display any error.
        }
    }

    /**
     * DOC nrousseau Comment method "parseVariables".
     *
     * @param variables
     * @return
     */
    private static String parseVariables(List<Variable> variables) {
        StringBuffer buff = new StringBuffer();
        Map<String, List<Variable>> classesToGenerate = new HashMap<String, List<Variable>>();

        if (variables != null) {
            for (Variable var : variables) {
                if (var.getName() != null && var.getTalendType() != null) {
                    if (var.getName().contains(".")) { //$NON-NLS-1$
                        // class
                        StringTokenizer token = new StringTokenizer(var.getName(), "."); //$NON-NLS-1$
                        String className = token.nextToken();
                        if (token.hasMoreTokens()) {
                            String subVarName = var.getName().substring(className.length() + 1);
                            if (subVarName.contains("(") && subVarName.contains("")) {
                                // no need for method like TalendDate.addDate(row1.test,arg2,arg3)
                                continue;
                            }
                            String newVarName = token.nextToken();
                            Variable newVar = new Variable(newVarName, var.getValue(), var.getTalendType(), var.isNullable());
                            if (!classesToGenerate.containsKey(className)) {
                                classesToGenerate.put(className, new ArrayList<Variable>());
                            }
                            classesToGenerate.get(className).add(newVar);
                        }
                    } else {
                        String typeToGenerate = JavaTypesManager.getTypeToGenerate(var.getTalendType(), var.isNullable());
                        buff.append("\tprivate " + typeToGenerate + " " + var.getName() + ";\\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }
            for (String className : classesToGenerate.keySet()) {
                buff.append("\tprivate class " + className + "Struct {\n"); //$NON-NLS-1$ //$NON-NLS-2$
                for (Variable var : classesToGenerate.get(className)) {
                    String typeToGenerate = JavaTypesManager.getTypeToGenerate(var.getTalendType(), var.isNullable());
                    buff.append("\t\tprivate " + typeToGenerate + " " + var.getName() + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                buff.append("\t};\n"); //$NON-NLS-1$
                buff.append("\tprivate " + className + "Struct " + className + "= new " + className + "Struct();\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }
        buff.append("\t" + VIEWER_CLASS_NAME + currentId + "() {\n"); //$NON-NLS-1$ //$NON-NLS-2$
        buff.append("\t\tglobalMap.put(\"NULL\", null);\n"); //$NON-NLS-1$
        if (variables != null) {
            for (String className : classesToGenerate.keySet()) {
                for (Variable var : classesToGenerate.get(className)) {
                    buff.append("\t\t" + className + "." + var.getName() + " =  " + var.getValue() + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
            }
        }
        buff.append("\t}\n"); //$NON-NLS-1$

        return buff.toString();
    }

    /**
     * Getter for compilationUnit.
     *
     * @return the compilationUnit
     */
    public ICompilationUnit getCompilationUnit() {
        return this.compilationUnit;
    }

    /**
     * Sets the compilationUnit.
     *
     * @param compilationUnit the compilationUnit to set
     */
    public void setCompilationUnit(ICompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public static String getClassName() {
        return className;
    }
}
