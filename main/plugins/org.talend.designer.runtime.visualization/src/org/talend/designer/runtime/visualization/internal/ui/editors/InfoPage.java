/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.internal.ui.editors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.core.dump.IProfileInfo;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The info page.
 */
public class InfoPage extends Composite {

    /** The runtime name splitter. */
    private static final String RUNTIME_NAME_SPLITTER = "@"; //$NON-NLS-1$

    /** The hostname text. */
    private Text hostnameText;

    /** The PID text. */
    private Text pidText;

    /** The main class text. */
    private Text mainClassText;

    /** The arguments text. */
    private Text argumentsText;

    /** The date text. */
    private Text dateText;

    /** The comments text. */
    private Text commentsText;

    /** The dump editor. */
    AbstractDumpEditor editor;

    /** The comments in the last save operation. */
    private String lastComments;

    /** The runtime section. */
    private Section runtimeSection;

    /**
     * The constructor.
     * 
     * @param editor The dump editor
     * @param parent The parent composite
     */
    public InfoPage(AbstractDumpEditor editor, Composite parent) {
        super(parent, SWT.NONE);
        this.editor = editor;
        createControls(this, new FormToolkit(Display.getDefault()));
    }

    /**
     * Gets the state indicating if the editor contents have changed sine the last save operation.
     * 
     * @return <tt>true</tt> if the editor contents have changed
     */
    protected boolean isDirty() {
        return lastComments != null && !lastComments.equals(commentsText.getText());
    }

    /**
     * Saves the changed editor contents.
     * 
     * @param monitor The progress monitor
     */
    protected void doSave(IProgressMonitor monitor) {
        IEditorInput input = editor.getEditorInput();
        IFile resourceFile = null;
        File file = null;
        InputStream inputStream = null;

        String comments = commentsText.getText();
        try {
            if (input instanceof IFileEditorInput) {
                resourceFile = ((IFileEditorInput) input).getFile();
                inputStream = new BufferedInputStream(resourceFile.getContents());
                file = resourceFile.getRawLocation().toFile();
            } else if (input instanceof FileStoreEditorInput) {
                file = new File(((FileStoreEditorInput) input).getURI().getPath());
                inputStream = new FileInputStream(file);
            } else {
                return;
            }

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            Element root = document.getDocumentElement();
            root.setAttribute("comments", comments); //$NON-NLS-1$

            DOMSource source = new DOMSource(root);
            StreamResult result = new StreamResult(file);
            Transformer transformer = XmlUtils.getXmlSecureTransform();
            transformer.transform(source, result);

            if (resourceFile != null) {
                resourceFile.refreshLocal(0, monitor);
            }

            lastComments = comments;
            editor.firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, Messages.saveFileFailedMsg, e);
        } catch (SAXException e) {
            Activator.log(IStatus.ERROR, Messages.saveFileFailedMsg, e);
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, Messages.saveFileFailedMsg, e);
        } catch (ParserConfigurationException e) {
            Activator.log(IStatus.ERROR, Messages.saveFileFailedMsg, e);
        } catch (TransformerException e) {
            Activator.log(IStatus.ERROR, Messages.saveFileFailedMsg, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Refreshes.
     * 
     * @param info The profile info
     */
    protected void refresh(IProfileInfo info) {
        if (info == null || dateText == null || mainClassText == null || pidText == null || hostnameText == null
                || argumentsText == null) {
            return;
        }

        runtimeSection.setExpanded(true);
        dateText.setText(info.getDate());
        mainClassText.setText(info.getMainClass());
        String runtime = info.getRuntime();
        if (runtime.contains(RUNTIME_NAME_SPLITTER)) {
            String[] elements = runtime.split(RUNTIME_NAME_SPLITTER);
            pidText.setText(elements[0]);
            hostnameText.setText(elements[1]);
        }
        argumentsText.setText(info.getArguments().replace(" -", "\n-")); //$NON-NLS-1$ //$NON-NLS-2$

        String comments = info.getComments();
        commentsText.setText(comments);
        lastComments = comments;
    }

    /**
     * Sets the focus to the comments text.
     */
    protected void focusCommnentsText() {
        commentsText.forceFocus();
    }

    /**
     * Creates the controls.
     * 
     * @param parent The parent composite
     * @param toolkit The toolkit
     */
    private void createControls(Composite parent, FormToolkit toolkit) {
        parent.setLayout(new FillLayout());

        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.getBody().setLayout(new GridLayout(1, false));

        createRuntimeSection(form.getBody(), toolkit);
        createSnapshotSection(form.getBody(), toolkit);
    }

    /**
     * Creates the runtime section.
     * 
     * @param parent The parent composite
     * @param toolkit The toolkit
     */
    private void createRuntimeSection(Composite parent, FormToolkit toolkit) {
        runtimeSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        runtimeSection.setText(Messages.runtimeSectionLabel);
        runtimeSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite composite = toolkit.createComposite(runtimeSection);
        runtimeSection.setClient(composite);
        composite.setLayout(new GridLayout(2, false));

        hostnameText = createText(composite, toolkit, Messages.hostnameLabel);
        pidText = createText(composite, toolkit, Messages.pidLabel);
        mainClassText = createText(composite, toolkit, Messages.mainClassLabel);
        argumentsText = createText(composite, toolkit, Messages.argumentsLabel);
    }

    /**
     * Creates the snapshot section.
     * 
     * @param parent The parent composite
     * @param toolkit The toolkit
     */
    private void createSnapshotSection(Composite parent, FormToolkit toolkit) {
        ExpandableComposite section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
        section.setText(Messages.snapshotSectionLabel);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
        composite.setLayout(new GridLayout(2, false));

        dateText = createText(composite, toolkit, Messages.dateLabel);
        commentsText = createEditableText(composite, toolkit, Messages.commentsLabel);
        commentsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                editor.firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        });
    }

    /**
     * Creates the text.
     * 
     * @param parent The parent composite
     * @param toolkit The toolkit
     * @param title The title for text
     * @return The text
     */
    private static Text createText(Composite parent, FormToolkit toolkit, String title) {
        Label label = toolkit.createLabel(parent, title, SWT.NONE);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        Text text = toolkit.createText(parent, "", SWT.WRAP | SWT.BORDER); //$NON-NLS-1$
        text.setEditable(false);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return text;
    }

    /**
     * Creates the editable text.
     * 
     * @param parent The parent composite
     * @param toolkit The toolkit
     * @param title The title for text
     * @return The text
     */
    private static Text createEditableText(Composite parent, FormToolkit toolkit, String title) {
        Label label = toolkit.createLabel(parent, title, SWT.NONE);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        Text text = toolkit.createText(parent, "", SWT.MULTI | SWT.WRAP //$NON-NLS-1$
                | SWT.BORDER | SWT.V_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        return text;
    }
}
