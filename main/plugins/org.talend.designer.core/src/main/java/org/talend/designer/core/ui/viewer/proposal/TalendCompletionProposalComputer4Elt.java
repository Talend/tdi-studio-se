package org.talend.designer.core.ui.viewer.proposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.IDesignerCoreService;

public class TalendCompletionProposalComputer4Elt extends TalendCompletionProposalComputer {

    public List computeCompletionProposals(ITextViewer textViewer, String prefix, int offset, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault()
                .getService(IDesignerCoreService.class);

        IProcess process = service.getCurrentProcess();
        if (process == null) {
            return Collections.EMPTY_LIST;
        }
        int replacementLength = textViewer.getSelectedRange().y;
        addContextProposal(process, prefix, replacementLength, proposals, offset);
        return proposals;
    }
}
