package org.talend.repository.ui.wizards.exportjob.extrachecker;

import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;

/**
 * Limits job contains tRESTRequest only build using [OSGI for ESB] build type.
 * 
 * @author GaoZone
 * @JIRA TESB-13867
 */
public class LimitRESTRequestJobChecker extends AbstractJobNodeChecker {

	private static final String T_REST_REQUEST = "tRESTRequest";

	@Override
	String checkNode(JobExportType exportType, NodeType nodeType) {
		if (exportType == JobExportType.OSGI) {
			return null;
		}
		String componentName = nodeType.getComponentName();
		if (T_REST_REQUEST.equals(componentName)) {
			return Messages.getString("LimitRESTRequestJobChecker.limit_tRESTRequest", componentName);
		}
		return null;
	}
}
