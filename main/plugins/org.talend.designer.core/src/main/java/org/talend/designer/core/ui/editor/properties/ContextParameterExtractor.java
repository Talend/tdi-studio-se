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
package org.talend.designer.core.ui.editor.properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.wizards.ContextParameterWizard;

/**
 * Extract context parameter from the GUI. <br/>
 *
 * $Id$
 *
 */
public final class ContextParameterExtractor {

    /**
     * Constructs a new ContextParameterExtractor.
     */
    private ContextParameterExtractor() {
    }

    /**
     * Install a context parameter extracter on a text field associated with a given process.
     *
     * @param text Component on wich extractor is installed.
     * @param process Process on wich context parameter is added.
     */
    public static void installOn(final Control text, final IProcess2 process, final String parameterName, final IElement elem) {
        text.addKeyListener(new KeyAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.F5) {
                    // String repositoryId = process.getRepositoryId();
                    // if (repositoryId != null) {
                    // MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages
                    // .getString("ContextParameterExtractor.Warning"), Messages
                    // .getString("ContextParameterExtractor.warningInfo"));// "It is impossible to add a
                    // // new parameter when using a
                    // // repository context.");
                    // return;
                    // }
                    // add for bug TDI-20336
                    if (process == null) {
                        return;
                    }
                    IContextParameter parameter = buildParameterFrom(text, process.getContextManager(), parameterName);
                    if (parameter == null) { // some context have existed
                        return;
                    }
                    ContextParameterWizard prmWizard = new ContextParameterWizard(process.getContextManager(), parameter);
                    WizardDialog dlg = new WizardDialog(text.getShell(), prmWizard);
                    if (dlg.open() == WizardDialog.OK) {
                        String replaceCode = ContextParameterUtils.getNewScriptCode(parameter.getName());
                        if (text instanceof Text) {
                            if (((Text) text).getSelectionCount() == 0) {
                                ((Text) text).setText(replaceCode);
                            } else {
                                ((Text) text).insert(replaceCode);

                            }
                            saveContext(parameterName, elem, ((Text) text).getText(), process);
                        } else {
                            if (text instanceof StyledText) {
                                if (((StyledText) text).getSelectionCount() == 0) {
                                    ((StyledText) text).setText(replaceCode);
                                } else {
                                    ((StyledText) text).insert(replaceCode);

                                }
                                saveContext(parameterName, elem, ((StyledText) text).getText(), process);
                            }
                        }
                        // process.getEditor().getTalendEditor().setDirty(true);
                    }
                }
            }

        });
    }

    /**
     * qzhang Comment method "saveContext".
     *
     * @param parameterName
     * @param elem
     * @param replaceCode
     */
    public static void saveContext(final String parameterName, final IElement elem, String replaceCode, IProcess2 process) {
        PropertyChangeCommand cmd = new PropertyChangeCommand(elem, parameterName, replaceCode);
        boolean executed = false;
        if (process != null && process instanceof IGEFProcess) {
            IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
            if (designerCoreUIService != null) {
                executed = designerCoreUIService.executeCommand((IGEFProcess) process, cmd);
            }
        }
        if (!executed) {
            cmd.execute();
        }

        // note that no undo will be available
    }

    private static IContextParameter buildParameterFrom(final Control text, final IContextManager manager,
            final String parameterName) {
        String nameProposal = ""; //$NON-NLS-1$
        if (text instanceof Text) {
            nameProposal = ((Text) text).getSelectionText();
            if (nameProposal.length() == 0) {
                nameProposal = ((Text) text).getText();
            }
        } else {
            if (text instanceof StyledText) {
                nameProposal = ((StyledText) text).getSelectionText();
                if (nameProposal.length() == 0) {
                    nameProposal = ((StyledText) text).getText();
                }
            }
        }
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            nameProposal = TalendTextUtils.removeQuotes(nameProposal);
        }

        String value = nameProposal;
        if (ContextParameterUtils.containContextVariables(value)) {
            MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                    Messages.getString("ContextParameterExtractor.ExistedTitle"), //$NON-NLS-1$
                    Messages.getString("ContextParameterExtractor.ExistedMessages")); //$NON-NLS-1$
            return null;
        }
        nameProposal = nameProposal.replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$

        IContextParameter parameter = new JobContextParameter();
        if (manager.checkValidParameterName(null, parameterName)) {
            parameter.setName(parameterName);
        } else if (manager.checkValidParameterName(null, nameProposal)) {
            parameter.setName(nameProposal);
        } else {
            parameter.setName(""); //$NON-NLS-1$
        }
        ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
        if (curLanguage == ECodeLanguage.PERL) {
            parameter.setType(MetadataTalendType.getDefaultTalendType());
        } else {
            parameter.setType(JavaTypesManager.getDefaultJavaType().getId());
        }
        parameter.setPrompt(parameterName + "?"); //$NON-NLS-1$
        parameter.setComment(""); //$NON-NLS-1$
        parameter.setValue(value);
        return parameter;
    }
}
