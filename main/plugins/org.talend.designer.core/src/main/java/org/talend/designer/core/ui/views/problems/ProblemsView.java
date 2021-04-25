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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.BasicJobInfo;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.views.problems.Problems.Group;
import org.talend.repository.documentation.ERepositoryActionName;
import org.talend.repository.ui.actions.routines.AbstractRoutineAction;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ProblemsView extends ViewPart implements PropertyChangeListener {

    public static final String PROBLEM_TYPE_SELECTION = "PROBLEM.TYPE.SELECTION";//$NON-NLS-1$

    private static final String ID = "org.talend.designer.core.ui.views.ProblemsView"; //$NON-NLS-1$

    private Composite parent;

    public static boolean isVisible() {
        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (ww != null) {
            IWorkbenchPage activePage = ww.getActivePage();
            if (activePage != null) {
                IViewPart part = activePage.findView(ID);
                if (part != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ProblemsView show() {
        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (ww != null) {
            IWorkbenchPage activePage = ww.getActivePage();
            if (activePage != null) {
                IViewPart part = activePage.findView(ID);
                try {
                    part = activePage.showView(ID);
                } catch (Exception e) {
                    // do nothing
                }
                return (ProblemsView) part;
            }
        }
        return null;
    }

    private TreeViewer viewer;

    public ProblemsView() {
        setPartName(Messages.getString("ProblemsView.problems.defaultTitle")); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().addPropertyChangeListener(this);
        parent.setLayout(new FillLayout());

        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);

        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Problem problem = (Problem) selection.getFirstElement();
                if (problem != null && problem.isConcrete()) {
                    if (problem.getNodeName() != null) {
                        selectInDesigner(problem.getJobInfo(), problem.getNodeName());
                    } else if (problem instanceof TalendProblem) {
                        selectInRoutine((TalendProblem) problem);
                    }
                }
            }
        });

        TreeColumn column1 = new TreeColumn(tree, SWT.CENTER);
        column1.setText(Messages.getString("ProblemsView.description")); //$NON-NLS-1$
        column1.setWidth(400);
        column1.setAlignment(SWT.LEFT);
        column1.setResizable(true);

        TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
        column2.setText(Messages.getString("ProblemsView.resource")); //$NON-NLS-1$
        column2.setWidth(400);
        column2.setResizable(true);

        ProblemViewProvider provider = new ProblemViewProvider();
        viewer.setLabelProvider(provider);
        viewer.setContentProvider(provider);
        resetContent();

        IActionBars actionBars = getViewSite().getActionBars();
        initMenu(actionBars.getMenuManager());
    }

    public void resetContent() {
        viewer.setInput(Problems.getRoot());

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().removePropertyChangeListener(this);
        Problems.setProblemView(null);
    }

    /**
     * initialize the Menu of problem view.
     *
     * @param menuManager
     */
    private void initMenu(IMenuManager menu) {
        // MenuManager groupByMenu = new MenuManager(MarkerMessages.ProblemView_GroupByMenu);
        MenuManager groupByMenu = new MenuManager(Messages.getString("MarkerMessages.ProblemView_GroupByMenu")); //$NON-NLS-1$
        // groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_Type, Group.TYPE, this));
        groupByMenu.add(new GroupingAction(Messages.getString("MarkerMessages.ProblemView_Type"), Group.TYPE, this)); //$NON-NLS-1$

        groupByMenu.add(new GroupingAction(Messages.getString("ProblemsView.severity"), Group.SEVERITY, this)); //$NON-NLS-1$

        // groupByMenu.add(new GroupingAction(MarkerMessages.ProblemView_None, Group.NONE, this));
        groupByMenu.add(new GroupingAction(Messages.getString("MarkerMessages.ProblemView_None"), Group.NONE, this)); //$NON-NLS-1$
        menu.add(groupByMenu);
    }

    private void selectInDesigner(BasicJobInfo jobInfo, String nodeName) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        IEditorReference[] editorParts = page.getEditorReferences();

        for (IEditorReference reference : editorParts) {
            IEditorPart editor = reference.getEditor(false);
            if (editor instanceof AbstractMultiPageTalendEditor) {
                AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) editor;
                if (mpte.getTalendEditor().getProcess().getId().equals(jobInfo.getJobId())
                        && mpte.getTalendEditor().getProcess().getVersion().equals(jobInfo.getJobVersion())) {
                    page.bringToTop(mpte);
                    mpte.selectNode(nodeName);
                }
            }
        }
    }

    private void selectInRoutine(TalendProblem problem) {
        OpenRoutineAction openRoutineAction = new OpenRoutineAction(problem);
        openRoutineAction.run();

    }

    @Override
    public void setFocus() {
        // viewer.getTree().setFocus();
        this.parent.setFocus();
    }

    /**
     * DOC bqian Comment method "refresh".
     */
    public void refresh() {
        viewer.refresh();
        setContentDescription(Problems.getSummary());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.views.markers.internal.TableView#getDialogSettings()
     */
    protected IPreferenceStore getDialogSettings() {
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        return store;
    }

    /**
     * An internal Action that can be used to group the problems.
     *
     */
    private class GroupingAction extends Action {

        Group groupingField;

        ProblemsView problemView;

        /**
         * Creates a new instance of the receiver.
         *
         * @param label
         * @param field
         * @param view
         */
        public GroupingAction(String label, Group group, ProblemsView view) {
            super(label, IAction.AS_RADIO_BUTTON);
            groupingField = group;
            problemView = view;
            Group categoryField = Problems.getGroupField();
            setChecked(categoryField.equals(groupingField));
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {

            if (isChecked()) {
                Problems.setGroupField(groupingField);
                problemView.resetContent();
                getDialogSettings().setValue(PROBLEM_TYPE_SELECTION, groupingField.toString());
            }
        }
    }

    /**
     *
     * DOC denny ProblemsView class global comment. Detailled comment
     */
    private class OpenRoutineAction extends AbstractRoutineAction {

        TalendProblem problem;

        /*
         * (non-Javadoc)
         *
         * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
         * org.eclipse.jface.viewers.IStructuredSelection)
         */
        public OpenRoutineAction(TalendProblem problem) {
            super();
            this.problem = problem;
        }

        @Override
        protected void doRun() {
            if (problem != null) {
                try {
                    RoutineItem routine = getRoutineItem();
                    IEditorPart editor = openRoutineEditor(routine, false);
                    focusMarkerForRoutineEditor(editor);
                    // TDI-21143 : Studio repository view : remove all refresh call to repo view
                    // IRepositoryView view = getViewPart();
                    // if (view != null) {
                    // view.refresh(ERepositoryObjectType.ROUTINES);
                    // }
                } catch (SystemException e) {
                    MessageBoxExceptionHandler.process(e);
                } catch (PartInitException e) {
                    MessageBoxExceptionHandler.process(e);
                }
            }
        }

        /**
         *
         * ggu Comment method "focusMarkerForRoutineEditor".
         *
         * focus to the marker position.
         */
        private void focusMarkerForRoutineEditor(IEditorPart editor) {
            if (editor != null) {
                int start = problem.getCharStart();
                int length = problem.getCharEnd() - start;
                if (length < 0) {
                    length = 0;
                }
                ISourceViewer sourceViewer = null;
                if (editor instanceof JavaEditor) {
                    sourceViewer = ((JavaEditor) editor).getViewer();
                }
                if (sourceViewer != null) {
                    sourceViewer.setRangeIndication(start, length, true);
                    sourceViewer.setSelectedRange(start, length);
                }
            }
        }

        /**
         * bqian Comment method "getRoutineItem".
         *
         * @return
         */
        private RoutineItem getRoutineItem() throws PersistenceException {
            List<IRepositoryViewObject> list = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                    .getAll(ERepositoryObjectType.ROUTINES, true);

            for (IRepositoryViewObject repositoryObject : list) {
                String name = repositoryObject.getProperty().getLabel();
                String id = repositoryObject.getProperty().getId();
                if (matchRoutine(id, name, problem.getMarker() != null ? problem.getMarker().getResource().getName() : "")) {
                    return (RoutineItem) repositoryObject.getProperty().getItem();
                }
            }
            return null;
        }

        /**
         * Check whether the routine name is correct. <br>
         * For example: routineLabel AAA <br>
         * resourceName AAA.java| AAA.pem
         *
         * @param string
         *
         * @param name
         * @param string
         * @return
         */
        public boolean matchRoutine(String routineID, String routineLabel, String resourceName) {
            if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                try {
                    Boolean foundMatch = resourceName.matches(routineLabel + ".java"); //$NON-NLS-1$
                    return foundMatch.booleanValue();
                } catch (PatternSyntaxException ex) {
                    // Syntax error in the regular expression
                }
            } else if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                try {
                    Boolean foundMatch = resourceName.matches("tempRoutine" + routineID); //$NON-NLS-1$
                    return foundMatch.booleanValue();
                } catch (PatternSyntaxException ex) {
                    // Syntax error in the regular expression
                }
            }

            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
         * org.eclipse.jface.viewers.IStructuredSelection)
         */
        @Override
        public void init(TreeViewer viewer, IStructuredSelection selection) {
            // setEnabled(true);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!(evt.getNewValue() instanceof IRepositoryObject)) {
            return;
        }
        IRepositoryObject object = (IRepositoryObject) evt.getNewValue();
        if (object.getRepositoryObjectType() != ERepositoryObjectType.ROUTINES) {
            return;
        }
        if (evt.getPropertyName().equals(ERepositoryActionName.DELETE_TO_RECYCLE_BIN.getName())
                || evt.getPropertyName().equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
            String routineLabel = object.getProperty().getLabel();
            Problems.removeProblemsByRoutine(routineLabel);
        }
        if (evt.getPropertyName().equals(ERepositoryActionName.RESTORE.getName())) {

            RoutineItem item = (RoutineItem) object.getProperty().getItem();

            restoreProblem(item);
        }
    }

    /**
     *
     * ggu Comment method "restoreProblem".
     *
     * when restore the item, check the problem.
     */
    private void restoreProblem(RoutineItem item) {
        ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        ITalendSynchronizer routineSynchronizer = null;
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            routineSynchronizer = service.createJavaRoutineSynchronizer();
            break;
        case PERL:
            routineSynchronizer = service.createPerlRoutineSynchronizer();
            break;
        default:
        }
        if (routineSynchronizer != null) {
            try {
                routineSynchronizer.syncRoutine(item, true);
                IFile file = routineSynchronizer.getFile(item);
                if (file == null) {
                    return;
                }
                file.refreshLocal(IResource.DEPTH_ONE, null);
                Problems.addRoutineFile(file, item.getProperty());
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    Problems.refreshProblemTreeView();
                }
            });
        }
    }
}
