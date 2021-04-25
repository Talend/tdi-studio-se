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
package org.talend.designer.core.ui.editor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.JavaCompositeReconcilingStrategy;
import org.eclipse.jdt.internal.ui.text.JavaReconciler;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.ui.views.problems.Problems;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: TalendJavaEditor.java 1 2007-1-18 下午03:26:08 +0000 (下午03:26:08, 2007-1-18 2007) yzhang $
 *
 */
public class TalendJavaEditor extends CompilationUnitEditor implements ISyntaxCheckableEditor {


    private boolean disposed = false;

    private String currentSelection;

    private IProcess2 process;

    private boolean initialized = false;

    /**
     * DOC amaumont TalendJavaEditor constructor comment.
     */
    public TalendJavaEditor(IProcess2 process) {
        super();
        this.process = process;
    }

    /*
     * Over write this method force to add complier to this editor. Beacuse by default if the editor is not editable,
     * the complier for error check will not installed.
     *
     * @see
     * org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSourceViewer().setEditable(isEditable());
    }

    /*
     * Edit is not allowed.
     *
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#isEditable()
     */
    @Override
    public boolean isEditable() {
        return false;
    }

    /*
     * Check the syntax for java code.
     *
     * @see org.talend.designer.core.ui.editor.ISyntaxCheckable#validateSyntax()
     */
    @Override
    public void validateSyntax() {
        FileEditorInput fileEditorInput = (FileEditorInput)this.getEditorInput();
        IFile codeFile = fileEditorInput.getFile();
        if (!initialized) {
            this.setInput(new FileEditorInput(codeFile));
            initialized = true;
        }
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof JavaSourceViewer) {
            this.getSourceViewer().getTextWidget().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    placeCursorToSelection();
                    Property property = process.getProperty();

                    final ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService()
                            .createRoutineSynchronizer();

                    try {
                        Item item = property.getItem();
                        List<Information> informations = Problems.addRoutineFile(synchronizer.getFile(item), property, true);
                        List<Information> testInformations = null;
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                                    .getDefault().getService(ITestContainerProviderService.class);
                            if (testContainerService != null) {
                                testInformations = testContainerService.getTestContainerInformations(item);
                            }
                        }
                        // save error status
                        property.getInformations().clear();
                        // add only the errors in the property, not the warnings
                        for (Information info : informations) {
                            if (info.getLevel().equals(InformationLevel.ERROR_LITERAL)) {
                                property.getInformations().add(info);
                            }
                        }
                        if (testInformations != null) {
                            property.getInformations().addAll(testInformations);
                        }
                        Problems.computePropertyMaxInformationLevel(property, false);
                    } catch (SystemException e) {
                        ExceptionHandler.process(e);
                    }

                    Problems.refreshProblemTreeView();
                }
            });
        }

    }

    private void placeCursorToSelection() {
        String mainPart;
        if (process != null && (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process.getComponentsType()))) {
            mainPart = "[" + currentSelection + " mrconfig ] start"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (process != null && (ComponentCategory.CATEGORY_4_SPARK.getName().equals(process.getComponentsType()))) {
            mainPart = "[" + currentSelection + " sparkconfig ] start"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (process != null && (ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(process.getComponentsType()))) {
            mainPart = "[" + currentSelection + " sparkstreamingconfig ] start"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            mainPart = "[" + currentSelection + " main ] start"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        String assignmentPart = "currentComponent = \"" + currentSelection + "\";"; //$NON-NLS-1$ //$NON-NLS-2$
        if (getDocumentProvider() == null) {
            return;
        }
        IDocument doc = getDocumentProvider().getDocument(getEditorInput());
        // TDI-21733:since the code page for joblet now will be null,must add the judgement.
        if (doc != null) {
            FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(doc);
            try {
                Region region = (Region) frda.find(0, mainPart, true, false, false, false);
                if (region != null) {
                    Region region2 = (Region) frda.find(region.getOffset(), assignmentPart, true, false, false, false);
                    if (region2 != null) {
                        selectAndReveal(region2.getOffset(), assignmentPart.length());
                    } else {
                        selectAndReveal(region.getOffset(), mainPart.length());
                    }
                } else {
                    selectAndReveal(0, 0);
                }
            } catch (BadLocationException e) {
                selectAndReveal(0, 0);
            }
        }
    }

    /*
     * Save as is not allowed.
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#dispose()
     */
    @Override
    public void dispose() {
        this.disposed = true;
        this.process = null;
        super.dispose();
    }

    /**
     *
     *
     * DOC yzhang Comment method "isDisposed".
     *
     * @return Whether this editor had been disposed.
     */
    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Getter for unit.
     *
     * @return the unit
     */
    public org.eclipse.jdt.core.ICompilationUnit getUnit() {
        return (org.eclipse.jdt.core.ICompilationUnit) getInputJavaElement();
    }

    /**
     * DOC nrousseau Comment method "placeCursorTo".
     *
     * @param string
     */
    public void placeCursorTo(String currentSelection) {
        this.currentSelection = currentSelection;
        placeCursorToSelection();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewerConfiguration()
     */
    @Override
    protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
        JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
        return new JavaSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this,
                IJavaPartitions.JAVA_PARTITIONING);
    }

    @Override
    protected void createActions() {
        super.createActions();
        getAction(IJavaEditorActionDefinitionIds.SHOW_IN_BREADCRUMB).setEnabled(false);
    }

    @Override
    protected boolean isErrorStatus(IStatus status) {
        if (!(process.getProperty().getItem() instanceof ProcessItem)) {
            return false;
        }
        return super.isErrorStatus(status);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#rememberSelection()
     */
    @Override
    protected void rememberSelection() {
        // do nothing since we display the code only in the editor after generate the code
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#restoreSelection()
     */
    @Override
    protected void restoreSelection() {
        // do nothing since we display the code only in the editor after generate the code
    }

    @Override
    protected void initializeDragAndDrop(ISourceViewer viewer) {
        if(!this.isEditable()){
            return;
        }
        super.initializeDragAndDrop(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#createJavaSourceViewer(org.eclipse.swt.widgets.
     * Composite, org.eclipse.jface.text.source.IVerticalRuler, org.eclipse.jface.text.source.IOverviewRuler, boolean,
     * int, org.eclipse.jface.preference.IPreferenceStore)
     */
    @Override
    protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
        return new TalendAdaptedSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);
    }

    @SuppressWarnings("restriction")
    protected class TalendAdaptedSourceViewer extends AdaptedSourceViewer {

        public TalendAdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
                boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
            super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
        }

        @Override
        public void configure(SourceViewerConfiguration configuration) {
            super.configure(configuration);
            if (fReconciler == null) {
                JavaCompositeReconcilingStrategy strategy = new JavaCompositeReconcilingStrategy(this, TalendJavaEditor.this,
                        configuration.getConfiguredDocumentPartitioning(this));
                JavaReconciler reconciler = new JavaReconciler(TalendJavaEditor.this, strategy, false);
                reconciler.setIsAllowedToModifyDocument(false);
                reconciler.setDelay(500);
                fReconciler = reconciler;
                fReconciler.install(this);
            }
        }
    }


}
