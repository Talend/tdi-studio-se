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
package org.talend.designer.core.ui.wizards;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

/**
 * Wizard used to add a new context parameter.
 *
 * $Id$
 *
 */
public class ContextParameterPage extends WizardPage {

    private IContextParameter parameter;

    private Set<String> existingParameterNames;

    /** Name. */
    private Text nameText;

    /** Should Prompt. */
    private Button promptBtn;

    /** Prompt text. */
    private Text promptText;

    /** Type. */
    private ComboViewer typeViewer;

    /** Default. */
    private Text defaultText;

    /** Comment. */
    private Text commentText;

    private IStatus nameStatus;

    private IStatus promptStatus;

    private IStatus typeStatus;

    private IStatus defaultStatus;

    private IStatus commentStatus;

    private IContextManager contextManager;

    /**
     * Constructs a new ContextParameterPage.
     *
     * @param contextManager
     */
    public ContextParameterPage(IContextManager contextManager) {
        super("ContextParameterPage"); //$NON-NLS-1$
        this.contextManager = contextManager;

        setTitle(Messages.getString("ContextParameterPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("ContextParameterPage.description")); //$NON-NLS-1$

        nameStatus = createOkStatus();
        commentStatus = createOkStatus();
        typeStatus = createOkStatus();
        promptStatus = createOkStatus();
        defaultStatus = createOkStatus();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        GridData data;

        // Name
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(Messages.getString("ContextParameterPage.name")); //$NON-NLS-1$

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Comment
        Label commentLabel = new Label(container, SWT.NONE);
        commentLabel.setText(Messages.getString("ContextParameterPage.comment")); //$NON-NLS-1$
        commentLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        commentText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 60;
        commentText.setLayoutData(data);

        // Type
        Label typeLabel = new Label(container, SWT.NONE);
        typeLabel.setText(Messages.getString("ContextParameterPage.type")); //$NON-NLS-1$

        typeViewer = new ComboViewer(container, SWT.BORDER | SWT.READ_ONLY);
        typeViewer.setContentProvider(ArrayContentProvider.getInstance());
        typeViewer.setLabelProvider(new LabelProvider());
        typeViewer.setInput(MetadataTalendType.getCxtParameterTalendTypesLabels());
        typeViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Prompt
        Label promptLabel = new Label(container, SWT.NONE);
        promptLabel.setText(Messages.getString("ContextParameterPage.prompt")); //$NON-NLS-1$

        promptText = new Text(container, SWT.BORDER);
        promptText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Prompt
        new Label(container, SWT.NONE);

        promptBtn = new Button(container, SWT.CHECK);
        promptBtn.setText(Messages.getString("ContextParameterPage.shouldPrompt")); //$NON-NLS-1$

        // Default
        Label defaultLabel = new Label(container, SWT.NONE);
        defaultLabel.setText(Messages.getString("ContextParameterPage.defaultValue")); //$NON-NLS-1$

        defaultText = new Text(container, SWT.BORDER);
        defaultText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setControl(container);
        updateContent();
        addListeners();
        updatePageComplete();
    }

    private void addListeners() {
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (nameText.getText().length() == 0) {
                    nameStatus = new Status(IStatus.ERROR, DesignerPlugin.ID, IStatus.OK, Messages
                            .getString("ContextParameterPage.nameEmpty"), null); //$NON-NLS-1$
                } else if (existingParameterNames.contains(nameText.getText())) {
                    nameStatus = new Status(IStatus.ERROR, DesignerPlugin.ID, IStatus.OK, Messages
                            .getString("ContextParameterPage.nameExists"), null); //$NON-NLS-1$
                } else if (!contextManager.checkValidParameterName(null, nameText.getText())) {
                    nameStatus = new Status(IStatus.ERROR, DesignerPlugin.ID, IStatus.OK, "Parameter name is not valid.", null); //$NON-NLS-1$
                } else {
                    nameStatus = createOkStatus();
                }
                parameter.setName(StringUtils.trimToEmpty(nameText.getText()));
                updatePageStatus();
            }
        });

        commentText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (commentText.getText().length() == 0) {
                    commentStatus = new Status(IStatus.WARNING, DesignerPlugin.ID, IStatus.OK, Messages
                            .getString("ContextParameterPage.commentEmpty"), //$NON-NLS-1$
                            null);
                } else {
                    commentStatus = createOkStatus();
                }
                parameter.setComment(StringUtils.trimToEmpty(commentText.getText()));
                updatePageStatus();
            }
        });

        final ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
        typeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                String type;
                if (event.getSelection().isEmpty()) {
                    typeStatus = new Status(IStatus.ERROR, DesignerPlugin.ID, IStatus.OK, Messages
                            .getString("ContextParameterPage.typeEmpty"), null); //$NON-NLS-1$
                    type = null;
                } else {
                    typeStatus = createOkStatus();
                    type = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
                }
                if (curLanguage == ECodeLanguage.JAVA) {
                    type = getJavaDisplayedType(type);
                }
                final String value = parameter.getValue();
                parameter.setType(type); // value will be empty
                parameter.setValue(value); // revert the value
                updatePageStatus();
            }
        });

        PromptListener promptListener = new PromptListener();
        promptText.addModifyListener(promptListener);
        promptBtn.addSelectionListener(promptListener);

        defaultText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (defaultText.getText().length() == 0) {
                    defaultStatus = new Status(IStatus.WARNING, DesignerPlugin.ID, IStatus.OK, Messages
                            .getString("ContextParameterPage.defaultValueEmpty"), null); //$NON-NLS-1$
                } else {
                    defaultStatus = createOkStatus();
                }
                parameter.setValue(StringUtils.trimToEmpty(defaultText.getText()));
                updatePageStatus();
            }
        });
    }

    private String getJavaDisplayedType(String originalTypedValue) {
        JavaType javaType;

        javaType = ContextParameterJavaTypeManager.getJavaTypeFromLabel(originalTypedValue);

        String type;

        if (javaType == null) {
            type = MetadataTalendType.getDefaultTalendType();
        } else {
            type = javaType.getId();
        }
        return type;
    }

    public void setParameter(IContextParameter contextParameter, List<? extends IContext> contexts) {
        this.parameter = contextParameter;

        existingParameterNames = new HashSet<String>();
        for (IContext context : contexts) {
            for (IContextParameter ctxPrm : context.getContextParameterList()) {
                existingParameterNames.add(ctxPrm.getName());
            }
        }
    }

    private void updateContent() {
        nameText.setText(StringUtils.trimToEmpty(parameter.getName()));
        commentText.setText(StringUtils.trimToEmpty(parameter.getComment()));
        typeViewer.setSelection(parameter.getType() != null ? new StructuredSelection(parameter.getType())
                : StructuredSelection.EMPTY, true);
        promptText.setText(StringUtils.trimToEmpty(parameter.getPrompt()));
        promptBtn.setSelection(parameter.isPromptNeeded());
        defaultText.setText(StringUtils.trimToEmpty(parameter.getValue()));
    }

    private void updatePageStatus() {
        setMessage(findMostSevere());
        updatePageComplete();
    }

    private void updatePageComplete() {
        setPageComplete(getErrorMessage() == null && StringUtils.isNotEmpty(parameter.getName()) && parameter.getType() != null
                && (!parameter.isPromptNeeded() || StringUtils.isNotEmpty(parameter.getPrompt())));
    }

    private IStatus findMostSevere() {
        IStatus[] statuses = getStatuses();
        IStatus severeStatus = statuses[0];
        for (IStatus status : statuses) {
            if (status.getSeverity() > severeStatus.getSeverity()) {
                severeStatus = status;
            }
        }
        return severeStatus;
    }

    private void setMessage(IStatus status) {
        if (IStatus.ERROR == status.getSeverity()) {
            setErrorMessage(status.getMessage());
            // setMessage(""); //$NON-NLS-1$
        } else {
            if (StringUtils.isNotEmpty(status.getMessage())) {
                setMessage(status.getMessage(), status.getSeverity());
            } else {
                setMessage(getDescription());
            }
            setErrorMessage(null);
        }
    }

    private IStatus[] getStatuses() {
        return new IStatus[] { nameStatus, commentStatus, typeStatus, promptStatus, defaultStatus };
    }

    private static IStatus createOkStatus() {
        return new Status(IStatus.OK, DesignerPlugin.ID, IStatus.OK, "", null); //$NON-NLS-1$
    }

    /**
     * Listener on prompt changes. <br/>
     *
     * $Id$
     *
     */
    private class PromptListener extends SelectionAdapter implements ModifyListener {

        public void modifyText(ModifyEvent e) {
            updateStatus();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateStatus();
        }

        private void updateStatus() {
            if (promptText.getText().length() == 0 && promptBtn.getSelection()) {
                promptStatus = new Status(IStatus.ERROR, DesignerPlugin.ID, IStatus.OK,
                        Messages.getString("ContextParameterPage.promptEmpty"), null); //$NON-NLS-1$
            } else {
                promptStatus = createOkStatus();
            }
            parameter.setPrompt(StringUtils.trimToEmpty(promptText.getText()));
            parameter.setPromptNeeded(promptBtn.getSelection());
            updatePageStatus();
        }
    }
}
