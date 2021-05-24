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
package org.talend.designer.core.ui.viewer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.hover.SourceViewerInformationControl;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.preferences.HotKeyUtil;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.model.process.INode;


/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public abstract class ReconcilerViewer extends ProjectionViewer {

    protected IFile file = null;

    // private SourceFile source;

    private SourceViewerDecorationSupport fSourceViewerDecorationSupport;

    protected IOverviewRuler fOverviewRuler;

    private MarkerAnnotationPreferences fAnnotationPreferences;

    protected IAnnotationAccess annotationAccess;

    protected ISharedTextColors sharedColors;

    protected boolean checkCode;

    // used to calculate the full document displayed (in the visible part, no
    // matter if there is annotations or not)
    private IRegion viewerStartRegion, viewerEndRegion;

    private int oldDocLength;

    private INode hostNode = null;

    /**
     * Preference key for highlighting current line.
     */
    private static final String CURRENT_LINE = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;

    /**
     * Preference key for highlight color of current line.
     */
    private static final String CURRENT_LINE_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

    /**
     * Preference key for showing print margin ruler.
     */
    private static final String PRINT_MARGIN = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;

    /**
     * Preference key for print margin ruler color.
     */
    private static final String PRINT_MARGIN_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;

    /**
     * Preference key for print margin ruler column.
     */
    private static final String PRINT_MARGIN_COLUMN = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;

    public ReconcilerViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles, IAnnotationAccess annotationAccess, ISharedTextColors sharedColors,
            boolean checkCode, IDocument document, final INode node) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        fOverviewRuler = overviewRuler;
        this.annotationAccess = annotationAccess;
        this.sharedColors = sharedColors;
        this.checkCode = checkCode;
        this.hostNode = node;

        initializeViewer(document);
    }

    protected void initializeViewer(IDocument document) {
        fAnnotationPreferences = EditorsPlugin.getDefault().getMarkerAnnotationPreferences();

        setDocument(document);

        installViewerConfiguration();
        setEditable(true);

        Font font = JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
        getTextWidget().setFont(font);

        Control control = getControl();
        GridData data = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(data);

        prependVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {
                handleVerifyKeyPressed(event);
            }
        });
        addDocumentListener(document);

        addMenu();
    }

    private void addDocumentListener(final IDocument document) {
        final ExecutionLimiter documentReconcilerLimiter = new ExecutionLimiter(500, true) {

            @Override
            protected void execute(boolean isFinalExecution, Object data) {
                if (isFinalExecution) {
                    if (getControl() != null && !getControl().isDisposed()) {
                        getControl().getDisplay().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                // System.out.println(System.currentTimeMillis())
                                // ;
                                updateContents();
                                if (document.get().length() != 0) {
                                    calculatePositions();
                                }
                                // (bug 4289)
                                updateVisibleRegion();
                            }
                        });
                    }
                }
            }
        };

        document.addDocumentListener(new IDocumentListener() {

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // nothing
            }

            @Override
            public void documentChanged(DocumentEvent event) {
                documentReconcilerLimiter.resetTimer();
                documentReconcilerLimiter.startIfExecutable(true, null);
            }
        });
    }

    protected Map<ProjectionAnnotation, Position> oldAnnotations = new HashMap<ProjectionAnnotation, Position>();

    protected void calculatePositions() {
        if (hasSnippetsModifications()) {
            final Map<ProjectionAnnotation, Position> annotations = getAllSnippetsAnnotations();

            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!annotations.isEmpty() && getProjectionAnnotationModel() == null) {
                        enableProjection();
                    }
                    if (getProjectionAnnotationModel() != null) {
                        Annotation[] oldAnno = oldAnnotations.keySet().toArray(new Annotation[0]);
                        getProjectionAnnotationModel().modifyAnnotations(oldAnno, annotations, null);
                        oldAnnotations.clear();
                        oldAnnotations.putAll(annotations);
                        if (annotations.isEmpty()) {
                            disableProjection();
                        }
                    }
                }

            });
        }
    }

    /**
     * Check if there is any new snippet or if some has been deleted.
     *
     * @return
     */
    private boolean hasSnippetsModifications() {
        IDocument document = getDocument();
        if (document == null) {
            return false;
        }
        int curNbAnnotations = oldAnnotations.size();
        int actualNbAnnotations = 0;
        int curOffset = 0;
        FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
        try {
            IRegion startRegion = frda.find(curOffset, "SNIPPET_START", true, false, false, false); //$NON-NLS-1$
            while (startRegion != null && startRegion.getOffset() >= curOffset) {
                int startLine = document.getLineOfOffset(startRegion.getOffset());
                int startOffset = document.getLineOffset(startLine);
                curOffset = startOffset + document.getLineLength(startLine);
                IRegion endRegion = frda.find(startRegion.getOffset(), "SNIPPET_END", true, false, false, false); //$NON-NLS-1$
                if (endRegion != null) {
                    actualNbAnnotations++;
                    int endLine = document.getLineOfOffset(endRegion.getOffset());
                    int endOffset = document.getLineOffset(endLine);
                    endOffset += document.getLineLength(endLine);
                    curOffset = endOffset;
                    boolean contains = false;
                    String text = document.get(startOffset, endOffset - startOffset);
                    for (ProjectionAnnotation annotation : oldAnnotations.keySet()) {
                        Position pos = oldAnnotations.get(annotation);
                        if (annotation.getText().equals(text) && (startOffset == pos.getOffset())) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        return true;
                    }
                }
                if (curOffset < document.getLength()) {
                    startRegion = frda.find(curOffset, "SNIPPET_START", true, false, false, false); //$NON-NLS-1$
                }
            }

        } catch (BadLocationException e) {
            ExceptionHandler.process(e);
        }
        if (curNbAnnotations != actualNbAnnotations) {
            return true;
        }
        return false;
    }

    private Map<ProjectionAnnotation, Position> getAllSnippetsAnnotations() {
        Map<ProjectionAnnotation, Position> annotations = new HashMap<ProjectionAnnotation, Position>();
        IDocument document = getDocument();
        int curOffset = 0;
        FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
        try {
            IRegion startRegion = frda.find(curOffset, "SNIPPET_START", true, false, false, false); //$NON-NLS-1$
            while (startRegion != null && startRegion.getOffset() >= curOffset) {
                int startLine = document.getLineOfOffset(startRegion.getOffset());
                int startOffset = document.getLineOffset(startLine);
                curOffset = startOffset + document.getLineLength(startLine);
                IRegion endRegion = frda.find(startRegion.getOffset(), "SNIPPET_END", true, false, false, false); //$NON-NLS-1$
                if (endRegion != null) {
                    int endLine = document.getLineOfOffset(endRegion.getOffset());
                    int endOffset = document.getLineOffset(endLine);
                    endOffset += document.getLineLength(endLine);
                    curOffset = endOffset;
                    String text = document.get(startOffset, endOffset - startOffset);
                    ProjectionAnnotation annotation = new ProjectionAnnotation(true);
                    annotation.setText(text);
                    annotation.setRangeIndication(true);
                    annotations.put(annotation, new Position(startOffset, endOffset - startOffset));
                }
                if (curOffset < document.getLength()) {
                    startRegion = frda.find(curOffset, "SNIPPET_START", true, false, false, false); //$NON-NLS-1$
                }
            }

        } catch (BadLocationException e) {
            ExceptionHandler.process(e);
        }
        return annotations;
    }

    /**
     * DOC nrousseau Comment method "addMenu".
     */
    private void addMenu() {
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        Image image = ImageProvider.getImage(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
        Menu popupMenu = new Menu(this.getTextWidget());

        MenuItem undoItem = new MenuItem(popupMenu, SWT.PUSH);
        undoItem.setText("Undo"); //$NON-NLS-1$
        undoItem.setImage(image);
        undoItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                doOperation(ITextOperationTarget.UNDO);
                event.doit = false;
            }
        });

        image = ImageProvider.getImage(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
        MenuItem redoItem = new MenuItem(popupMenu, SWT.PUSH);
        redoItem.setText("Redo"); //$NON-NLS-1$
        redoItem.setData(this);
        redoItem.setImage(image);
        redoItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                doOperation(ITextOperationTarget.REDO);
                event.doit = false;
            }
        });

        // add separator
        new MenuItem(popupMenu, SWT.SEPARATOR);

        image = ImageProvider.getImage(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        MenuItem copyItem = new MenuItem(popupMenu, SWT.PUSH);
        copyItem.setText("Copy"); //$NON-NLS-1$
        copyItem.setImage(image);
        copyItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getTextWidget().copy();
            }
        });

        image = ImageProvider.getImage(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        MenuItem pasteItem = new MenuItem(popupMenu, SWT.PUSH);
        pasteItem.setText("Paste"); //$NON-NLS-1$
        pasteItem.setData(this);
        pasteItem.setImage(image);
        pasteItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getTextWidget().paste();
            }
        });

        MenuItem selectAllItem = new MenuItem(popupMenu, SWT.PUSH);
        selectAllItem.setText("Select All"); //$NON-NLS-1$
        selectAllItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getTextWidget().selectAll();
            }
        });
        getTextWidget().setMenu(popupMenu);

        getTextWidget().setKeyBinding('A' | SWT.MOD1, ST.SELECT_ALL);
    }

    /**
     * Example: configure(new TalendPerlSourceViewerConfiguration(PerlEditorPlugin .getDefault().getPreferenceStore(),
     * this));.
     */
    protected abstract void installViewerConfiguration();

    protected SourceViewerDecorationSupport getSourceViewerDecorationSupport() {
        if (fSourceViewerDecorationSupport == null) {
            fSourceViewerDecorationSupport = new SourceViewerDecorationSupport(this, fOverviewRuler, annotationAccess,
                    sharedColors);
            configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
        }
        return fSourceViewerDecorationSupport;
    }

    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {

        Iterator e = fAnnotationPreferences.getAnnotationPreferences().iterator();
        while (e.hasNext()) {
            support.setAnnotationPreference((AnnotationPreference) e.next());
        }

        support.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
        support.setMarginPainterPreferenceKeys(PRINT_MARGIN, PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
        support.setSymbolicFontName(JFaceResources.TEXT_FONT);
    }

    public abstract void updateContents();

    protected void initializeModel() {
        ProjectionSupport projectionSupport = new ProjectionSupport(this, annotationAccess, sharedColors);
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        projectionSupport.setHoverControlCreator(new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell shell) {
                return new SourceViewerInformationControl(shell, false, SWT.LEFT_TO_RIGHT, EditorsUI.getTooltipAffordanceString());
            }
        });
        projectionSupport.setInformationPresenterControlCreator(new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell shell) {
                int shellStyle = SWT.RESIZE | SWT.TOOL | SWT.LEFT_TO_RIGHT;
                int style = SWT.V_SCROLL | SWT.H_SCROLL;
                return new SourceViewerInformationControl(shell, true, SWT.LEFT_TO_RIGHT, null);
            }
        });
        projectionSupport.install();
    }

    private void handleVerifyKeyPressed(VerifyEvent event) {
        if (!event.doit) {
            return;
        }
        try {
            KeyStroke triggerKeyStroke = HotKeyUtil.getHotKey(HotKeyUtil.contentAssist);
            if (triggerKeyStroke != null) {
                // Either there are no modifiers for the trigger and we
                // check the character field...
                if ((triggerKeyStroke.getModifierKeys() == KeyStroke.NO_KEY && triggerKeyStroke.getNaturalKey() == event.character)
                        ||
                        // ...or there are modifiers, in which case the
                        // keycode and state must match
                        (((triggerKeyStroke.getNaturalKey() == event.keyCode)
                                || (Character.toLowerCase(triggerKeyStroke.getNaturalKey()) == event.keyCode) || (Character
                                .toUpperCase(triggerKeyStroke.getNaturalKey()) == event.keyCode)) && ((triggerKeyStroke
                                .getModifierKeys() & event.stateMask) == triggerKeyStroke.getModifierKeys()))) {
                    // We never propagate the keystroke for an explicit
                    // keystroke invocation of the popup
                    doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
                    event.doit = false;
                    return;
                }
            }
        } catch (ParseException e) {
            org.talend.commons.exception.ExceptionHandler.process(e);
        }

        if (event.stateMask != SWT.CTRL) {
            return;
        }

        switch (event.character) {
        case ' ':
            doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
            event.doit = false;
            break;

        case '.':
            doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
            event.doit = false;
            break;

        // CTRL-Y
        case 'y' - 'a' + 1:
            doOperation(ITextOperationTarget.REDO);
            event.doit = false;
            break;

        // CTRL-Z
        case 'z' - 'a' + 1:
            doOperation(ITextOperationTarget.UNDO);
            event.doit = false;
            break;
        default:
        }
    }

    // /**
    // * Getter for source.
    // *
    // * @return the source
    // */
    // public SourceFile getSource() {
    // return this.source;
    // }

    /**
     * Getter for file.
     *
     * @return the file
     */
    public IFile getFile() {
        return this.file;
    }

    /**
     * Adds support for dropping items into this viewer via a user drag-and-drop operation.
     *
     * @param operations a bitwise OR of the supported drag and drop operation types ( <code>DROP_COPY</code>,
     * <code>DROP_LINK</code>, and <code>DROP_MOVE</code>)
     * @param transferTypes the transfer types that are supported by the drop operation
     * @param listener the callback that will be invoked after the drag and drop operation finishes
     * @see org.eclipse.swt.dnd.DND
     */
    public void addDropSupport(int operations, Transfer[] transferTypes, final DropTargetListener listener) {
        Control control = getTextWidget();
        DropTarget dropTarget = new DropTarget(control, operations);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.source.projection.ProjectionViewer#handleDispose()
     */
    @Override
    protected void handleDispose() {
        if (!getControl().isDisposed()) {
            super.handleDispose();
        }
        if (file != null && file.exists()) {
            try {
                file.delete(false, new NullProgressMonitor());
            } catch (CoreException e1) {
                // do nothing as the delete is not important.
            }
        }
    }

    public IRegion getViewerRegion() {
        if (viewerStartRegion == null) {
            return new Region(0, getDocument().getLength());
        }
        return new Region(viewerStartRegion.getLength(), getDocument().getLength() - viewerStartRegion.getLength()
                - viewerEndRegion.getLength());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.source.projection.ProjectionViewer#setVisibleRegion (int, int)
     */
    @Override
    public void setVisibleRegion(int start, int length) {
        viewerStartRegion = new Region(0, start);
        if (getDocument() == null) {
            return;
        }
        if (getDocument().getLength() > start) {
            viewerEndRegion = new Region(start + 1 + length, getDocument().getLength() - start - length);
        } else {
            viewerEndRegion = new Region(start, 0);
        }
        super.setVisibleRegion(start, length);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.TextViewer#createTextWidget(org.eclipse.swt.widgets .Composite, int)
     */
    @Override
    protected StyledText createTextWidget(Composite parent, int styles) {
        return new ReconcilerStyledText(parent, styles, this);
    }

    /**
     *
     * ggu Comment method "updateVisibleRegion".
     */
    protected void updateVisibleRegion() {
        if (this.getDocument() == null || this.viewerEndRegion == null || this.getVisibleDocument() == null) {
            return;
        }
        final String docText = this.getDocument().get();
        final int newDocLength = docText.length();
        if (this.oldDocLength != newDocLength) {
            final String visibleText = this.getVisibleDocument().get(); // get
            // visible
            // text

            final int newLength = visibleText.length();
            final int newStart = newDocLength - newLength - this.viewerEndRegion.getLength();

            setVisibleRegion(newStart, newLength);

            this.oldDocLength = newDocLength;
        }
    }

    /**
     * Getter for hostNode.
     *
     * @return the hostNode
     */
    public INode getHostNode() {
        return this.hostNode;
    }

    /**
     * Sets the hostNode.
     *
     * @param hostNode the hostNode to set
     */
    public void setHostNode(INode hostNode) {
        this.hostNode = hostNode;
    }

    @Override
    public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset, int modelRangeLength) {
        super.setDocument(document, annotationModel, modelRangeOffset, modelRangeLength);
        if (document != null) {
            addDocumentListener(document);
        }
    }
}
