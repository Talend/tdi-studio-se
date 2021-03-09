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
package org.talend.designer.xmlmap.parts.directedit;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.commons.ui.runtime.thread.AsynchronousThreading;
import org.talend.commons.ui.runtime.utils.ControlUtils;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.ProposalUtils;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.parts.directedit.proposal.ExpressionProposalProvider;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.ui.tabs.StyledTextHandler;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class ExpressionCellEditor extends ExtendedTextCellEditor {

    private GraphicalEditPart source;

    private MapperManager mapperManager;

    private Composite parent;

    public ExpressionCellEditor(final Composite parent, CellEditorDialogBehavior cellEditorBehavior, GraphicalEditPart source,
            DirectEditType type) {
        super(parent, cellEditorBehavior);

        parent.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {

            }

            public void focusGained(FocusEvent e) {

                ExpressionCellEditor.this.focusLost();
            }
        });
        this.parent = parent;
        this.source = source;
        if (source.getViewer() instanceof XmlMapGraphicViewer) {
            this.mapperManager = ((XmlMapGraphicViewer) source.getViewer()).getMapperManager();
        }
        if (mapperManager != null) {
            ContentProposalAdapterExtended proposalAdaptor = ProposalUtils.getCommonProposal(getTextControl(),
                    createExpressionProposalProvider());
            final StyledTextHandler styledTextHandler = mapperManager.getMapperUI().getTabFolderEditors().getStyledTextHandler();

            getTextControl().addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    Text text = (Text) e.widget;
                    styledTextHandler.setTextWithoutNotifyListeners(text.getText());
                }

            });

            getTextControl().addKeyListener(new TextCellEditorToMapperStyledTextKeyListener(getTextControl(), styledTextHandler));

        }

    }

    public Text createText(Composite parent) {

        text = new Text(parent, SWT.NONE);
        text.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelection(e);
            }

        });

        text.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        // We really want a selection listener but it is not supported so we
        // use a key listener and a mouse listener to know when selection
        // changes
        // may have occured
        text.addMouseListener(new MouseAdapter() {

            public void mouseDown(MouseEvent e) {
                highlightLineAndSetSelectionOfStyledTextFromTextControl(text);
            }

            @Override
            public void mouseUp(MouseEvent e) {
                checkSelection();
                checkDeleteable();
                checkSelectable();
            }
        });

        text.setFont(parent.getFont());
        text.setBackground(parent.getBackground());
        text.setText("");//$NON-NLS-1$
        text.addModifyListener(getModifyListener());
        return text;

    }

    class TextCellEditorToMapperStyledTextKeyListener implements KeyListener {

        private final Control textWidget;

        private final StyledTextHandler textTarget;

        /**
         * DOC amaumont TextKeyListener constructor comment.
         */
        public TextCellEditorToMapperStyledTextKeyListener(Text textWidgetSrc, StyledTextHandler textTarget) {
            super();
            this.textWidget = textWidgetSrc;
            this.textTarget = textTarget;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {

            // execute fireCancelEditor() before set text for UnnotifiableColorStyledText to get the original value
            if (e.character == '\u001b') {
                keyReleaseOccured(e);
            }

            boolean ctrl = (e.stateMask & SWT.CTRL) != 0;
            boolean altgr = (e.stateMask & SWT.ALT) != 0;
            if (e.character == '\0' || ctrl && !altgr) {
                highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
            } else {
                UnnotifiableColorStyledText mapperColorStyledText = (UnnotifiableColorStyledText) textTarget.getStyledText();
                if (e.character == '\r' || e.character == '\u001b') {
                    // e.doit = false;
                    String expression = "";
                    if (source.getModel() instanceof AbstractNode) {
                        expression = ((AbstractNode) source.getModel()).getExpression();
                    } else if (source.getModel() instanceof AbstractInOutTree) {
                        expression = ((AbstractInOutTree) source.getModel()).getExpressionFilter();
                    }
                    String text = expression == null ? "" : expression;
                    if (!textWidget.isDisposed()) {
                        text = ControlUtils.getText(textWidget);
                    }
                    textTarget.setTextWithoutNotifyListeners(text);
                    highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
                } else {
                    Point selection = ControlUtils.getSelection(textWidget);
                    if (e.character == SWT.BS || e.character == SWT.DEL) {
                        if (selection.x == selection.y) {

                            if (e.character == SWT.BS) {
                                if (selection.x - 1 > 0 && mapperColorStyledText.getText().length() > selection.x - 1) {
                                    char previousChar = mapperColorStyledText.getText().charAt(selection.x - 1);
                                    if (previousChar == '\n') {
                                        mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x - 2, 2, ""); //$NON-NLS-1$
                                    } else {
                                        mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x - 1, 1, ""); //$NON-NLS-1$
                                    }
                                }
                            } else {
                                if (selection.x < mapperColorStyledText.getText().length()) {
                                    char nextChar = mapperColorStyledText.getText().charAt(selection.x);
                                    if (nextChar == '\r') {
                                        mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, 2, ""); //$NON-NLS-1$
                                    } else {
                                        mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, 1, ""); //$NON-NLS-1$
                                    }
                                }
                            }

                        } else {
                            if (selection.y <= mapperColorStyledText.getCharCount()) {
                                mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, selection.y
                                        - selection.x, ""); //$NON-NLS-1$
                            }
                            highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
                        }
                    } else {

                        if (selection.y <= mapperColorStyledText.getCharCount()) {
                            mapperColorStyledText.replaceTextRangeWithoutNotifyListeners(selection.x, selection.y - selection.x,
                                    String.valueOf(e.character));
                        }
                        highlightLineAndSetSelectionOfStyledTextFromTextControl(textWidget);
                    }
                }

            }
            // execute fireApplyEditorValue() after set text for UnnotifiableColorStyledText
            if (e.character != '\u001b') {
                keyReleaseOccured(e);
            }

            // as a result of processing the above call, clients may have
            // disposed this cell editor
            if ((getControl() == null) || getControl().isDisposed()) {
                return;
            }
            checkSelection(); // see explaination below
            checkDeleteable();
            checkSelectable();
        }

        public void keyReleased(KeyEvent e) {
            // highlightLineOfCursorPosition();
        }

    }

    protected void highlightLineAndSetSelectionOfStyledTextFromTextControl(final Control textWidget) {
        final StyledTextHandler styledTextHandler = mapperManager.getMapperUI().getTabFolderEditors().getStyledTextHandler();

        Runnable runnable = new Runnable() {

            public void run() {

                StyledText styledText = styledTextHandler.getStyledText();
                if (styledText.isDisposed() || textWidget.isDisposed()) {
                    return;
                }

                String text = ControlUtils.getText(textWidget);
                Point selection = ControlUtils.getSelection(textWidget);
                String lineDelimiter = ControlUtils.getLineDelimiter(textWidget);
                if (selection.x - 1 > 0) {
                    while (lineDelimiter.equals(text.charAt(selection.x - 1))) {
                        selection.x++;
                    }
                }
                if (selection.y - 1 > 0) {
                    while (lineDelimiter.equals(text.charAt(selection.y - 1))) {
                        selection.y++;
                    }
                }
                int length = styledText.getText().length();
                if (selection.x < 0) {
                    selection.x = 0;
                }
                if (selection.x > length) {
                    selection.x = length;
                }
                if (selection.y < 0) {
                    selection.y = 0;
                }
                if (selection.y > length) {
                    selection.y = length;
                }
                styledText.setSelection(selection);
                styledTextHandler.highlightLineOfCursorPosition(selection);

            }
        };
        new AsynchronousThreading(50, true, parent.getDisplay(), runnable).start();
    }

    @Override
    public void fireApplyEditorValue() {
        super.fireApplyEditorValue();
    }

    private ExpressionProposalProvider createExpressionProposalProvider() {
        IContentProposalProvider[] contentProposalProviders = new IContentProposalProvider[0];
        contentProposalProviders = new IContentProposalProvider[] { new TalendProposalProvider(mapperManager.getMapperComponent()
                .getProcess()) };
        ExpressionProposalProvider provider = new ExpressionProposalProvider(mapperManager, contentProposalProviders);
        provider.init(source);
        return provider;
    }
}
