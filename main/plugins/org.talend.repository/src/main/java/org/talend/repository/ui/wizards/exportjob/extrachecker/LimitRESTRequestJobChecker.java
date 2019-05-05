package org.talend.repository.ui.wizards.exportjob.extrachecker;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;

/**
 * Limits job contains tRESTRequest only build using [OSGI for ESB] build type.
 * 
 * @author GaoZone
 * @JIRA TESB-13867
 */
public class LimitRESTRequestJobChecker extends AbstractJobNodeChecker {

	private static final String T_REST_REQUEST = "tRESTRequest";

    private static final String[] ESB_COMPONENTS = { "tESBConsumer", "tRESTClient", T_REST_REQUEST };

    boolean tRESTRequestExist = false;

    boolean containsESBComponents = false;

	@Override
	String checkNode(JobExportType exportType, NodeType nodeType) {
        String componentName = nodeType.getComponentName();

        if (exportType == JobExportType.OSGI) {
            if (ArrayUtils.contains(ESB_COMPONENTS, componentName)) {
                containsESBComponents = true;
            }

			return null;
		}

        if (exportType == JobExportType.MSESB) {
            if (T_REST_REQUEST.equals(componentName)) {
                tRESTRequestExist = true;
            }
            return null;
        }

        if (exportType == JobExportType.MSESB_IMAGE) {
            if (T_REST_REQUEST.equals(componentName)) {
                tRESTRequestExist = true;
            }
            return null;
        }

        if (T_REST_REQUEST.equals(componentName)) {
            return Messages.getString("LimitRESTRequestJobChecker.limit_tRESTRequest", componentName);
        }

		return null;
	}

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

        if (exportType == JobExportType.OSGI) {
            if (!containsESBComponents) {
                return Messages.getString("LimitRESTRequestJobChecker.limit_ESBJobForOSGI");
            }
        } else if (exportType == JobExportType.MSESB) {
            if (!tRESTRequestExist) {
                return Messages.getString("LimitRESTRequestJobChecker.limit_ESBJobForMS", T_REST_REQUEST);
            }
        } else if (exportType == JobExportType.MSESB_IMAGE) {
            if (!tRESTRequestExist) {
                return Messages.getString("LimitRESTRequestJobChecker.limit_ESBJobForMSImage", T_REST_REQUEST);
            }
        }

        return null;
    }
}
