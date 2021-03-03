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
package org.talend.designer.core.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.SimpleJavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;

/**
 * View that will show the code of the current component.
 *
 * $Id$
 *
 */
public class CodeView extends ViewPart {

    // private StyledText text;

    private IDocument document;

    private ICodeGenerator codeGenerator = null;

    private static final int CODE_START = 0;

    private static final int CODE_MAIN = 1;

    private static final int CODE_END = 2;

    private static final int CODE_ALL = 3;

    private int codeView = CODE_MAIN;

    private int nodeCodeView = -1;

    private static final String ERROR_MESSAGE = Messages.getString("CodeView.Error"); //$NON-NLS-1$

    private static final String MULTIPLE_COMPONENT_ERROR_MESSAGE = Messages.getString("CodeView.MultipleComponentError"); //$NON-NLS-1$

    private INode selectedNode = null;

    private INode generatingNode = null;

    public static final String ID = "org.talend.designer.core.codeView"; //$NON-NLS-1$

    private Composite parent;

    private String generatedCode;

    private boolean generating = false;

    IAction viewStartAction = new Action() {

        @Override
        public String getText() {
            return Messages.getString("CodeView.Start"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            codeView = CODE_START;
            refresh();
        }

    };

    IAction viewMainAction = new Action() {

        @Override
        public String getText() {
            return Messages.getString("CodeView.Main"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            codeView = CODE_MAIN;
            refresh();
        }

    };

    IAction viewEndAction = new Action() {

        @Override
        public String getText() {
            return Messages.getString("CodeView.End"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            codeView = CODE_END;
            refresh();
        }

    };

    IAction viewAllAction = new Action() {

        @Override
        public String getText() {
            return Messages.getString("CodeView.All"); //$NON-NLS-1$
        }

        @Override
        public void run() {
            codeView = CODE_ALL;
            refresh();
        }

    };

    public CodeView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        parent.setLayout(new FillLayout());
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        ISourceViewer viewer = null;
        final StyledText text;
        int styles = SWT.H_SCROLL | SWT.V_SCROLL;
        document = new Document();
        switch (language) {
        case JAVA:
            IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
            viewer = new JavaSourceViewer(parent, null, null, false, styles, store);
            viewer.setDocument(document);
            JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
            tools.setupJavaDocumentPartitioner(viewer.getDocument(), IJavaPartitions.JAVA_PARTITIONING);
            SimpleJavaSourceViewerConfiguration config = new SimpleJavaSourceViewerConfiguration(tools.getColorManager(), store,
                    null, IJavaPartitions.JAVA_PARTITIONING, true);
            viewer.configure(config);
            viewer.getTextWidget().setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
            document = viewer.getDocument();
            break;
        default: // empty since only have java
        }
        viewer.setEditable(false);
        text = viewer.getTextWidget();
        // getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        IAction wrapAction = new Action() {

            @Override
            public void run() {
                text.setWordWrap(isChecked());
            }
        };
        wrapAction.setToolTipText("wrap"); //$NON-NLS-1$
        wrapAction.setChecked(false);
        wrapAction.setImageDescriptor(ImageDescriptor.createFromFile(DesignerPlugin.class, "/icons/wrap.gif")); //$NON-NLS-1$
        tbm.add(wrapAction);
        createMenu();

    }

    private void createMenu() {
        IMenuManager manager = getViewSite().getActionBars().getMenuManager();

        manager.add(viewStartAction);
        manager.add(viewMainAction);
        manager.add(viewEndAction);
        manager.add(viewAllAction);
        viewMainAction.setChecked(true);
    }

    public static void refreshCodeView(final IElement element) {
        DisplayUtils.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (ww != null) {
                    IWorkbenchPage page = ww.getActivePage();
                    IViewPart view = page.findView(CodeView.ID);
                    if (view != null) {
                        final CodeView codeView = (CodeView) view;
                        codeView.refresh(element);
                    }
                }
            }
        });
    }

    private void refresh(IElement element) {

        if (element instanceof INode) {
            selectedNode = (INode) element;
            if (nodeCodeView != -1) {
                codeView = nodeCodeView;
                nodeCodeView = -1;
            }
        } else if (element instanceof IConnection) {
            selectedNode = ((IConnection) element).getSource();
            nodeCodeView = codeView;
            codeView = CODE_END;
        } else if (element == null) {
            selectedNode = null;
            generatingNode = null;
        }
        refresh();
    }

    private synchronized void setGenerating(boolean generating) {
        this.generating = generating;
    }

    private synchronized boolean isGenerating() {
        return this.generating;
    }

    public void refresh() {
        ICodeGeneratorService service = DesignerPlugin.getDefault().getCodeGeneratorService();
        if (service.isInitializingJet()) {
            return;
        }

        if (isGenerating()) {
            return;
        }
        setGenerating(true);
        final List<INode> generatingNodes = new ArrayList<>();
        if(selectedNode != null){
            generatingNodes.addAll(selectedNode.getProcess().getGeneratingNodes());
        }
        Job job = new Job(Messages.getString("CodeView.initMessage")) { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (selectedNode != null) {
                    generatedCode = ""; //$NON-NLS-1$
                    try {

                        // joblet or joblet node
                        boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(selectedNode.getProcess());
                        if (!isJoblet && PluginChecker.isJobLetPluginLoaded()) {
                            IJobletProviderService jobletSservice = (IJobletProviderService) GlobalServiceRegister.getDefault()
                                    .getService(IJobletProviderService.class);
                            if (jobletSservice != null && jobletSservice.isJobletComponent(selectedNode)) {
                                isJoblet = true;
                            }
                        }
                        if (isJoblet) {
                            return org.eclipse.core.runtime.Status.OK_STATUS;
                        }

                        generatingNode = null;
                        for (INode node : generatingNodes) {
                            if (node.getUniqueName().equals(selectedNode.getUniqueName())) {
                                generatingNode = node;
                                break;
                            }
                        }
                        if (generatingNode == null) {
                            generatedCode = Messages.getString("CodeView.MultipleComponentError"); //$NON-NLS-1$
                            return org.eclipse.core.runtime.Status.OK_STATUS;
                        }

                        ICodeGeneratorService service = DesignerPlugin.getDefault().getCodeGeneratorService();
                        // have to do this dirty change to avoid the side effect for BD component.
                        codeGenerator = service.createCodeGenerator(selectedNode.getProcess(), false, false);

                        viewStartAction.setChecked(false);
                        viewMainAction.setChecked(false);
                        viewEndAction.setChecked(false);
                        viewAllAction.setChecked(false);
                        switch (codeView) {
                        case CODE_START:
                            viewStartAction.setChecked(true);
                            generatedCode = codeGenerator.generateComponentCode(generatingNode, ECodePart.BEGIN);
                            break;
                        case CODE_MAIN:
                            viewMainAction.setChecked(true);
                            generatedCode = codeGenerator.generateComponentCode(generatingNode, ECodePart.MAIN);
                            break;
                        case CODE_END:
                            viewEndAction.setChecked(true);
                            generatedCode = codeGenerator.generateComponentCode(generatingNode, ECodePart.END);
                            break;
                        case CODE_ALL:
                            viewAllAction.setChecked(true);
                            generatedCode = codeGenerator.generateComponentCode(generatingNode, ECodePart.BEGIN);
                            generatedCode += codeGenerator.generateComponentCode(generatingNode, ECodePart.MAIN);
                            generatedCode += codeGenerator.generateComponentCode(generatingNode, ECodePart.END);
                            break;
                        default:
                        }
                    } catch (SystemException e) {
                        generatedCode = Messages.getString("CodeView.Error"); //$NON-NLS-1$
                        ExceptionHandler.process(e);
                    } catch (Exception e) {
                        // Some exceptions can appear in case we close some jobs while generating
                        // Just ignore them, and set blank directly to the code view
                        generatedCode = ""; //$NON-NLS-1$
                    }
                }else{
                    generatedCode = ""; //$NON-NLS-1$
                }
                return org.eclipse.core.runtime.Status.OK_STATUS;
            }

        };
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                new UIJob("") {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        document.set(generatedCode);
                        setGenerating(false);
                        return org.eclipse.core.runtime.Status.OK_STATUS;
                    }

                }.schedule();
            }

        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.parent.setFocus();
    }
}
