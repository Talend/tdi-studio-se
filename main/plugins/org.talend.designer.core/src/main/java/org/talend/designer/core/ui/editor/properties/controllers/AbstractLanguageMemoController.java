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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.TalendJavaEditor;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.viewer.ReconcilerStyledText;
import org.talend.designer.core.ui.viewer.java.TalendJavaSourceViewer;
import org.talend.designer.core.utils.ISampleCodeFactory;
import org.talend.designer.core.utils.JavaSampleCodeFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorController.java 1 2006-12-12 上午11:24:40 +0000 (上午11:24:40) yzhang $
 *
 */
public abstract class AbstractLanguageMemoController extends AbstractElementPropertySectionController {

    private static boolean estimateInitialized = false;

    private static int rowSizeFixed = 0;

    private static int rowSizeByLine = 0;

    /**
     * DOC dev LanguageMemoController constructor comment.
     *
     * @param parameterBean
     */
    public AbstractLanguageMemoController(IDynamicProperty dp) {
        super(dp);
        setLanguage();
    }

    private String language;

    private ISourceViewer viewer;

    private Button codeGenereateButton;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        int nbLines = param.getNbLines();
        final String paramName = param.getName();

        Control cLayout;
        StyledText text;
        FormData data;
        viewer = null;
        if (param.getNbLines() != 1) {
            if (language.equals("java")) { //$NON-NLS-1$
                String context = param.getContext();
                if (!param.isNoCheck() && context != null
                        && (context.equals("begin") || context.equals("main") || context.equals("end"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    Composite a = new Composite(subComposite, SWT.NO_FOCUS);
                    a.setLayout(new FormLayout());
                    Composite b = new Composite(a, SWT.NO_FOCUS);
                    b.setLayout(new GridLayout());
                    data = new FormData();
                    data.left = new FormAttachment(0, 0);
                    data.top = new FormAttachment(0, 0);
                    data.right = new FormAttachment(100, 0);
                    data.bottom = new FormAttachment(100, 0);

                    b.setLayoutData(data);
                    Process process = null;
                    if (elem instanceof Node) {
                        process = (Process) ((Node) elem).getProcess();
                    } else if (elem instanceof Connection) {
                        process = (Process) ((Connection) elem).getSource().getProcess();
                    }
                    TalendJavaEditor javaEditor = ((AbstractMultiPageTalendEditor) process.getEditor()).getCodeEditor();

                    viewer = TalendJavaSourceViewer.createViewerForComponent(b, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                            | SWT.H_SCROLL | SWT.WRAP, process, javaEditor, null, elem.getElementName(), context);
                    text = viewer.getTextWidget();
                    IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
                    FontData fontData = PreferenceConverter.getFontData(preferenceStore,
                            TalendDesignerPrefConstants.MEMO_TEXT_FONT);
                    Font font = new Font(text.getDisplay(), fontData);
                    addResourceDisposeListener(text, font);
                    text.setFont(font);

                    text.setData(PARAMETER_NAME, param.getName());
                    editionControlHelper.register(param.getName(), text);
                    cLayout = a;
                } else {
                    Composite a = new Composite(subComposite, SWT.NO_FOCUS);
                    a.setLayout(new FormLayout());
                    Composite b = new Composite(a, SWT.NO_FOCUS);
                    b.setLayout(new GridLayout());
                    data = new FormData();
                    data.left = new FormAttachment(0, 0);
                    data.top = new FormAttachment(0, 0);
                    data.right = new FormAttachment(100, 0);
                    data.bottom = new FormAttachment(100, 0);

                    b.setLayoutData(data);

                    if (isNeedToAddCodeGenerateButton()) {
                        addCodeGenerateButton(b);
                    }

                    Process process = null;
                    if (elem instanceof Node) {
                        process = (Process) ((Node) elem).getProcess();
                    } else if (elem instanceof Connection) {
                        Connection connection = (Connection) elem;
                        process = (Process) connection.getSource().getProcess();
                        // see bug 0001645
                        if (connection.getLineStyle().equals(EConnectionType.RUN_IF)
                                || connection.getLineStyle().equals(EConnectionType.ROUTE_WHEN)
                                || connection.getLineStyle().equals(EConnectionType.ROUTE_CATCH)) {
                            viewer = TalendJavaSourceViewer.createViewerForIfConnection(b, process);
                        }
                    }
                    if (viewer == null) {
                        viewer = TalendJavaSourceViewer.createViewer(b, process, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
                                | SWT.WRAP, false);
                    }

                    text = viewer.getTextWidget();
                    IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
                    FontData fontData = PreferenceConverter.getFontData(preferenceStore,
                            TalendDesignerPrefConstants.MEMO_TEXT_FONT);
                    Font font = new Font(text.getDisplay(), fontData);
                    addResourceDisposeListener(text, font);
                    text.setFont(font);
                    text.setData(PARAMETER_NAME, param.getName());
                    editionControlHelper.register(param.getName(), text);
                    cLayout = a;
                }
            } else {
                Composite a = new Composite(subComposite, SWT.NO_FOCUS);
                a.setLayout(new FormLayout());
                Composite b = new Composite(a, SWT.NO_FOCUS);
                b.setLayout(new GridLayout());
                data = new FormData();
                data.left = new FormAttachment(0, 0);
                data.top = new FormAttachment(0, 0);
                data.right = new FormAttachment(100, 0);
                data.bottom = new FormAttachment(100, 0);

                b.setLayoutData(data);

                if (isNeedToAddCodeGenerateButton()) {
                    addCodeGenerateButton(b);
                }
                text = viewer.getTextWidget();
                IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
                FontData fontData = PreferenceConverter.getFontData(preferenceStore, TalendDesignerPrefConstants.MEMO_TEXT_FONT);
                Font font = new Font(text.getDisplay(), fontData);
                addResourceDisposeListener(text, font);
                text.setFont(font);

                Process process = null;

                if (elem instanceof Node) {
                    process = (Process) ((Node) elem).getProcess();
                } else if (elem instanceof Connection) {
                    process = (Process) ((Connection) elem).getSource().getProcess();
                }
                text.setData(PARAMETER_NAME, param.getName());
                editionControlHelper.register(param.getName(), text);
                cLayout = a;
            }
        } else {
            IControlCreator txtCtrl = new IControlCreator() {

                @Override
                public Control createControl(final Composite parent, final int style) {
                    final StyledText control = new ColorStyledText(parent, style, CorePlugin.getDefault().getPreferenceStore(),
                            language);
                    Display display = Display.getCurrent();
                    if (display == null) {
                        display = Display.getDefault();
                    }
                    if (display != null) {
                        display.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
                                FontData fontData = PreferenceConverter.getFontData(preferenceStore,
                                        TalendDesignerPrefConstants.MEMO_TEXT_FONT);
                                Font font = new Font(parent.getDisplay(), fontData);
                                addResourceDisposeListener(control, font);
                                control.setFont(font);
                            }
                        });
                    }

                    return control;
                }
            };
            DecoratedField dField = null;
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.WRAP, txtCtrl);
            cLayout = dField.getLayoutControl();
            text = (StyledText) dField.getControl();
            data = (FormData) text.getLayoutData();
            editionControlHelper.register(param.getName(), text);
            if (param.isRequired()) {
                FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                        FieldDecorationRegistry.DEC_REQUIRED);
                dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
            }
        }

        if (getAdditionalHeightSize() != 0) {
            nbLines += this.getAdditionalHeightSize() / text.getLineHeight();
        }
        data.height = text.getLineHeight() * nbLines;
        text.getParent().setSize(subComposite.getSize().x, text.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        // for bug 7580
        if (!(text instanceof ReconcilerStyledText)) {
            text.setEnabled(!param.isReadOnly());
        } else {
            text.setEditable(!param.isReadOnly());
        }
        if (elem instanceof Node) {
            text.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        addDragAndDropTarget(text);

        // IDocument document = viewer.getDocument();
        // if (document != null) {
        // Process pro = null;
        // if (elem instanceof Node) {
        // pro = (Process) ((Node) elem).getProcess();
        // } else if (elem instanceof Connection) {
        // pro = (Process) ((Connection) elem).getSource().getProcess();
        // }
        // final Process process = pro;
        // document.addDocumentListener(new IDocumentListener() {
        //
        // public void documentChanged(DocumentEvent event) {
        // if (!process.isReadOnly())
        // ContextParameterExtractor.saveContext(paramName, elem, viewer.getTextWidget().getText(), process);
        // }
        //
        // public void documentAboutToBeChanged(DocumentEvent event) {
        // // nothing to do
        // }
        // });
        //
        // }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************

        hashCurControls.put(param.getName(), text);

        Point initialSize = cLayout.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return null;
    }

    /**
     *
     * DOC YeXiaowei Comment method "addCodeGenerateButton".
     *
     * @param parent
     */
    private void addCodeGenerateButton(final Composite parent) {

        codeGenereateButton = new Button(parent, SWT.CENTER);
        codeGenereateButton.setText(Messages.getString("AbstractLanguageMemoController.generateCode")); //$NON-NLS-1$
        codeGenereateButton.setToolTipText(Messages.getString("AbstractLanguageMemoController.generateSampleCode")); //$NON-NLS-1$
        codeGenereateButton.setEnabled(!elem.isReadOnly());

        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = SWT.CENTER;

        codeGenereateButton.setLayoutData(layoutData);

        codeGenereateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (elem instanceof Node) {
                    // generate code
                    ISampleCodeFactory factory = JavaSampleCodeFactory.getInstance();
                    executeCommand(factory.generateCodeForParameters((Node) elem));
                    refresh(curParameter, false);
                }
            }

        });
    }

    private boolean isNeedToAddCodeGenerateButton() {
        if (elem instanceof Node) {
            return ((Node) elem).getUniqueName().startsWith("tJavaRow"); //$NON-NLS-1$
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        if (!estimateInitialized) {
            IControlCreator txtCtrl = new IControlCreator() {

                @Override
                public Control createControl(final Composite parent, final int style) {
                    final ColorStyledText colorText = new ColorStyledText(parent, style, CorePlugin.getDefault()
                            .getPreferenceStore(), language);
                    Display display = Display.getCurrent();
                    if (display == null) {
                        display = Display.getDefault();
                    }
                    if (display != null) {
                        display.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
                                String fontType = preferenceStore.getString(TalendDesignerPrefConstants.MEMO_TEXT_FONT);
                                FontData fontData = new FontData(fontType);
                                Font font = new Font(parent.getDisplay(), fontData);
                                addResourceDisposeListener(colorText, font);
                                colorText.setFont(font);
                            }
                        });
                    }
                    return colorText;
                }
            };

            DecoratedField dField = null;
            if (param.getNbLines() != 1) {
                dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP,
                        txtCtrl);
            } else {
                dField = new DecoratedField(subComposite, SWT.BORDER | SWT.WRAP, txtCtrl);
            }

            StyledText text = (StyledText) dField.getControl();
            FormData d = (FormData) text.getLayoutData();
            d.height = text.getLineHeight();
            text.getParent().setSize(subComposite.getSize().x, text.getLineHeight());
            rowSizeByLine = text.getLineHeight();
            Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            dField.getLayoutControl().dispose();
            rowSizeFixed = ITabbedPropertyConstants.VSPACE + (initialSize.y - rowSizeByLine);
            estimateInitialized = true;
        }

        return rowSizeFixed + (rowSizeByLine * param.getNbLines());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#hasDynamicRowSize
     * ()
     */
    @Override
    public boolean hasDynamicRowSize() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    /**
     * DOC ftang Comment method "setLanguage".
     */
    protected abstract void setLanguage();

    protected void setCurrentLanguage(String language) {
        this.language = language;
    }

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Object o = hashCurControls.get(param.getName());
        if (o == null) {
            return;
        }
        Object value = param.getValue();
        if (o instanceof StyledText) {
            StyledText text = (StyledText) o;
            if (text.isDisposed()) {
                return;
            }
            if (value == null) {
                text.setText(""); //$NON-NLS-1$
            } else {
                if (!value.equals(text.getText())) {
                    text.setText((String) value);
                }
            }
        }
    }

}
