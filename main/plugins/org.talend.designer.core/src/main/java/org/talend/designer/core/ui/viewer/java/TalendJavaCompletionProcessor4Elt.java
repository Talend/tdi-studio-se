package org.talend.designer.core.ui.viewer.java;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class TalendJavaCompletionProcessor4Elt extends TalendJavaCompletionProcessor {

    public TalendJavaCompletionProcessor4Elt(ContentAssistant assistant, String partition) {
        super(assistant, partition);
    }

    @Override
    protected List<ICompletionProposal> sortProposals(List<ICompletionProposal> proposals, IProgressMonitor monitor,
            ContentAssistInvocationContext context) {
        // TODO Auto-generated method stub
        return super.sortProposals(proposals, monitor, context);
    }
    
    @Override
    protected boolean checkDefaultEnablement(CompletionProposalCategory category) {
        // use customize extension to filter the proposal category
        return "org.talend.designer.core.eltmap_proposal".equals(category.getId());
    }

    @Override
    protected boolean checkSeparateEnablement(CompletionProposalCategory category) {
        return true;
    }
    
}
