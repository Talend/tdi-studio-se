package org.talend.repository.ui.wizards.exportjob.extrachecker;

import java.util.Collections;
import java.util.List;

import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;

/**
 * Common Abstract Checker to check each node in one job. Subclasses need to implement
 * {@link #checkNode(JobExportType, NodeType)} to return error message.
 * 
 * @author GaoZone
 */
public abstract class AbstractJobNodeChecker implements ExtraBuildChecker {

    @Override
    public String check(JobExportType exportType, RepositoryNode[] checkNodes) {
        for (RepositoryNode node : checkNodes) {
            List<NodeType> compNodes = getProcessNodeTypes(node);
            for (NodeType nodeType : compNodes) {
                if (skipDeactivatedNode() && checkIsDeactivated(nodeType)) {
                    continue;
                }
                String err = checkNode(exportType, nodeType);
                if (err != null) {
                    return node.getDisplayText() + " - " + err;
                }
            }
        }
        return null;
    }

    protected boolean skipDeactivatedNode() {
        return true;
    }

    abstract String checkNode(JobExportType exportType, NodeType nodeType);

    @SuppressWarnings("unchecked")
    protected static List<NodeType> getProcessNodeTypes(RepositoryNode node) {
        Item item = node.getObject().getProperty().getItem();
        if (item instanceof ProcessItem) {
            ProcessType process = ((ProcessItem) item).getProcess();
            return process.getNode();
        }
        return Collections.emptyList();
    }

    protected static boolean checkIsDeactivated(NodeType nodeType) {
        @SuppressWarnings("unchecked")
        List<ElementParameterType> params = nodeType.getElementParameter();
        for (ElementParameterType param : params) {
            if ("Activate".equals(param.getName())) { //$NON-NLS-1$
                return "false".equals(param.getValue()); //$NON-NLS-1$
            }
        }
        return false;
    }
}
