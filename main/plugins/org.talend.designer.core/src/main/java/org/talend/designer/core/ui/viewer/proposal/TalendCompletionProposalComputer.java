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
package org.talend.designer.core.ui.viewer.proposal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.proposal.IExternalProposals;
import org.talend.core.ui.proposal.JavaGlobalUtils;
import org.talend.core.ui.proposal.ProposalFactory;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.TalendType;

/**
 * @author nrousseau This class is currently used only in Java mode.
 */
public class TalendCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private static final String CONTEXT_PREFIX = ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX;

    private static final String PERL_GLOBAL_PREFIX = "global."; //$NON-NLS-1$

    public TalendCompletionProposalComputer() {
    }

    @Override
    public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        String prefix = ""; //$NON-NLS-1$
        try {
            if (context != null) {
                prefix = context.computeIdentifierPrefix().toString();

                String tmpPrefix = ""; //$NON-NLS-1$
                IDocument doc = context.getDocument();
                if ((!prefix.equals("")) || (doc.get().length() == 0)) { //$NON-NLS-1$
                    tmpPrefix = prefix;
                } else {
                    int offset = context.getInvocationOffset();
                    if (doc.getChar(offset - 1) == '.') {
                        // set by default to avoid other completions

                        tmpPrefix = "."; //$NON-NLS-1$

                        if (offset >= CONTEXT_PREFIX.length()
                                && doc.get(offset - CONTEXT_PREFIX.length(), CONTEXT_PREFIX.length()).equals(CONTEXT_PREFIX)) {
                            tmpPrefix = CONTEXT_PREFIX;
                        } else if (offset >= PERL_GLOBAL_PREFIX.length()
                                & doc.get(offset - PERL_GLOBAL_PREFIX.length(), PERL_GLOBAL_PREFIX.length()).equals(
                                        PERL_GLOBAL_PREFIX)) {
                            switch (LanguageManager.getCurrentLanguage()) {
                            case JAVA:
                                // do nothing
                                break;
                            case PERL:
                            default:
                                tmpPrefix = PERL_GLOBAL_PREFIX;
                            }
                        } else {
                            // test each component label.

                            IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                                    IDesignerCoreService.class);

                            IProcess process = service.getCurrentProcess();
                            if (process == null) {
                                return Collections.EMPTY_LIST;
                            }
                            List<? extends INode> nodes = process.getGraphicalNodes();
                            for (INode node : nodes) {
                                String toTest = node.getLabel() + "."; //$NON-NLS-1$
                                if (offset >= toTest.length()
                                        && doc.get(offset - toTest.length(), toTest.length()).equals(toTest)) {
                                    tmpPrefix = toTest;
                                    break;
                                }
                            }
                        }
                    }
                }
                prefix = tmpPrefix;
                if (".".equals(prefix) && LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) { //$NON-NLS-1$
                    prefix = ""; //$NON-NLS-1$
                }
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        return computeCompletionProposals(context.getViewer(), prefix, context.getInvocationOffset(), monitor);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#computeCompletionProposals(org.eclipse.jdt.ui.text
     * .java.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
     */
    public List computeCompletionProposals(ITextViewer textViewer, String prefix, int offset, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                IDesignerCoreService.class);
        IProcess process = service.getCurrentProcess();

        if (process == null) {
            return Collections.EMPTY_LIST;
        }

        // Compute the length of replacement for proposal. See bug 0004266: Replace value with context value using
        // CTRL+Space.
        int replacementLength = textViewer.getSelectedRange().y;
        if (replacementLength == 0) {
            replacementLength = prefix.length();
        }

        this.addContextProposal(process, prefix, replacementLength, proposals, offset);

        // Proposals based on global variables
        List<? extends INode> nodes = process.getGraphicalNodes();
        for (INode node : nodes) {
            List<? extends INodeReturn> nodeReturns = node.getReturns();
            for (INodeReturn nodeReturn : nodeReturns) {
                String display = node.getLabel() + "." + nodeReturn.getName(); //$NON-NLS-1$

                if (prefix.equals("") || display.startsWith(prefix)) { //$NON-NLS-1$
                    String code = getNodeReturnContent(nodeReturn, node);

                    String description = getNodeReturnDescription(nodeReturn, node, display);

                    TalendCompletionProposal proposal = new TalendCompletionProposal(code, offset - prefix.length(),
                            replacementLength, code.length(), CoreImageProvider.getComponentIcon(node.getComponent(),
                                    ICON_SIZE.ICON_16), display, null, description);
                    proposal.setType(TalendCompletionProposal.NODE_RETURN);
                    proposals.add(proposal);
                }
            }
        }

        // Proposals based on global variables(only perl ).
        // add proposals on global variables in java (bugtracker 2554)
        // add variables in java
        IContentProposal[] javavars = JavaGlobalUtils.getProposals();
        for (IContentProposal javavar : javavars) {
            String display = javavar.getLabel();

            if (prefix.equals("") || display.startsWith(prefix)) { //$NON-NLS-1$
                String code = javavar.getContent();
                String description = getGlobalVarDescription(javavar, display);
                TalendCompletionProposal proposal = new TalendCompletionProposal(code, offset - prefix.length(),
                        replacementLength, code.length(), ImageProvider.getImage(ECoreImage.PROCESS_ICON), display, null,
                        description);
                proposal.setType(TalendCompletionProposal.VARIABLE);
                proposals.add(proposal);
            }
        }

        FunctionManager functionManager = new FunctionManager();
        List<TalendType> talendTypes = functionManager.getTalendTypes();
        for (TalendType type : talendTypes) {
            for (Object objectFunction : type.getFunctions()) {
                Function function = (Function) objectFunction;
                String display = function.getCategory() + "." + function.getName(); //$NON-NLS-1$
                if (prefix.equals("") || display.startsWith(prefix)) { //$NON-NLS-1$
                    String code = FunctionManager.getFunctionMethod(function);

                    String description = getFunctionDescription(function, display, code);

                    TalendCompletionProposal proposal = new TalendCompletionProposal(code, offset - prefix.length(),
                            replacementLength, code.length(), ImageProvider.getImage(ECoreImage.ROUTINE_ICON), display, null,
                            description);
                    proposal.setType(TalendCompletionProposal.ROUTINE);
                    proposals.add(proposal);

                }
            }
        }

        for (IExternalProposals externalProposals : ProposalFactory.getInstances()) {
            proposals.addAll(externalProposals.getAdvancedProposals(offset, prefix));
        }

        return proposals;
    }

    private String getContextContent(IContextParameter contextParameter) {
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        return ContextParameterUtils.getNewScriptCode(contextParameter.getName(), language);

    }

    private String getContextDescription(IContextParameter contextParameter, String display) {
        String desc = new String();
        if (!StringUtils.isEmpty(contextParameter.getComment())) {
            desc = contextParameter.getComment();
        } else {
            desc = Messages.getString("ContextParameterProposal.NoCommentAvaiable"); //$NON-NLS-1$
        }

        String message = "<b>" + display + "</b><br><br>" //$NON-NLS-1$ //$NON-NLS-2$
                + Messages.getString("ContextParameterProposal.Description.v1") + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("ContextParameterProposal.ContextVariable.v1") + "<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("ContextParameterProposal.Type.v1") + "<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("ContextParameterProposal.VariableName.v1"); //$NON-NLS-1$

        MessageFormat format = new MessageFormat(message);
        Object[] args = new Object[] { desc, contextParameter.getContext().getName(), contextParameter.getType(),
                contextParameter.getValue() };
        return format.format(args);
    }

    private String getNodeReturnContent(INodeReturn nodeReturn, INode node) {
        return ElementParameterParser.parse(node, nodeReturn.getVarName());
    }

    private String getNodeReturnDescription(INodeReturn nodeReturn, INode node, String display) {
        String message = "<b>" + display + "</b><br><br>" //$NON-NLS-1$ //$NON-NLS-2$
                + Messages.getString("NodeReturnProposal.Description") + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("NodeReturnProposal.GlobalVariable") + "<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("NodeReturnProposal.Type") + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("NodeReturnProposal.Availability") + "<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("NodeReturnProposal.VariableName"); //$NON-NLS-1$

        MessageFormat format = new MessageFormat(message);
        Object[] args = new Object[] { nodeReturn.getDisplayName(), node.getComponent().getName(), node.getLabel(),
                nodeReturn.getDisplayType(), nodeReturn.getAvailability(), getNodeReturnContent(nodeReturn, node) };
        return format.format(args);
    }

    private String getGlobalVarDescription(IContentProposal cp, String display) {
        String message = "<b>" + display + "</b><br><br>" //$NON-NLS-1$ //$NON-NLS-2$
                + Messages.getString("GlobalVariableProposal.Description") + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("GlobalVariableProposal.VariableName"); //$NON-NLS-1$

        MessageFormat format = new MessageFormat(message);

        Object[] args = new Object[] { cp.getDescription(), cp.getContent() };
        return format.format(args);
    }

    private String getFunctionDescription(Function function, String display, String code) {
        String message = "<b>" + display + "</b><br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("RoutinesFunctionProposal.Description") + "{0}<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("RoutinesFunctionProposal.CreatedBy") + "{1}<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("RoutinesFunctionProposal.ReturnType") + "{2}<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        message += Messages.getString("RoutinesFunctionProposal.Example") + "{3}<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$

        MessageFormat format = new MessageFormat(message);
        Object[] args = new Object[] { function.getDescription(),
                function.isUserDefined() ? Messages.getString("RoutinesFunctionProposal.User") : Messages //$NON-NLS-1$
                        .getString("RoutinesFunctionProposal.System"), function.getTalendType().getName(), code }; //$NON-NLS-1$
        return format.format(args);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#computeContextInformation(org.eclipse.jdt.ui.text
     * .java.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#getErrorMessage()
     */
    @Override
    public String getErrorMessage() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
     */
    @Override
    public void sessionEnded() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
     */
    @Override
    public void sessionStarted() {
    }

    public void addContextProposal(IProcess process, String prefix, int replacementLength, List<ICompletionProposal> proposals,
            int offset) {
        // Proposals based on process context
        List<IContextParameter> ctxParams = process.getContextManager().getDefaultContext().getContextParameterList();

        for (IContextParameter ctxParam : ctxParams) {
            String display = CONTEXT_PREFIX + ctxParam.getName();
            String code = getContextContent(ctxParam);
            String description = getContextDescription(ctxParam, display);

            String ctxName = ctxParam.getName();

            if (prefix.equals("") || display.startsWith(prefix)) { //$NON-NLS-1$
                TalendCompletionProposal proposal = new TalendCompletionProposal(code, offset - prefix.length(),
                        replacementLength, code.length(), ImageProvider.getImage(ECoreImage.CONTEXT_ICON), display, null,
                        description);
                proposal.setType(TalendCompletionProposal.CONTEXT);
                proposals.add(proposal);
            } else if (prefix.equals("") || ctxName.startsWith(prefix)) { //$NON-NLS-1$
                if (code.startsWith(CONTEXT_PREFIX)) {
                    code = code.replaceFirst(CONTEXT_PREFIX, "");
                }

                TalendCompletionProposal proposal = new TalendCompletionProposal(code, offset - prefix.length(),
                        replacementLength, code.length(), ImageProvider.getImage(ECoreImage.CONTEXT_ICON), display, null,
                        description);
                proposal.setType(TalendCompletionProposal.CONTEXT);
                proposals.add(proposal);
            }

        }
    }
}
