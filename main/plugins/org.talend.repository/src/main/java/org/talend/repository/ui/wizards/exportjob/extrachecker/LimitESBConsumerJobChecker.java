package org.talend.repository.ui.wizards.exportjob.extrachecker;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.talend.core.PluginChecker;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;

/**
 * Limits job contains specific ESB components only build using [OSGI for ESB]
 * build type.
 * 
 * TODO better move to other bundle relative to this check.(some appropriate esb
 * bundle)
 * 
 * @author GaoZone
 * @JIRA TESB-13867
 */
public class LimitESBConsumerJobChecker extends AbstractJobNodeChecker {

	/** The Constant ESB_CONSUMER_JOB_NODES. */
	private static final String[] ESB_CONSUMER_JOB_NODES = { "tESBConsumer", "tRESTClient" };

	@Override
	String checkNode(JobExportType exportType, NodeType nodeType) {
        if (exportType == JobExportType.OSGI || exportType == JobExportType.MSESB || exportType == JobExportType.MSESB_IMAGE) {
			return null;
		}
		String componentName = nodeType.getComponentName();
		if (isESBConsumerComponent(componentName)) {
			if (exportType != JobExportType.POJO && exportType != JobExportType.IMAGE) {
				return Messages.getString("LimitESBConsumerJobChecker.limit_tRESTClient_tESBConsumer", componentName);
			}
			return checkLimitFeaturesWhenBuildStandalone(nodeType);
		}
		return null;
	}

	private String checkLimitFeaturesWhenBuildStandalone(NodeType nodeType) {
		@SuppressWarnings("unchecked")
		List<ElementParameterType> params = nodeType.getElementParameter();
		boolean useLocator = false;
		boolean useSAM = false;
		for (ElementParameterType param : params) {
			// check Use_Registry has priority to SL/SAM.
			if (PluginChecker.isTIS() && param.getName().equals("USE_SR") && "true".equals(param.getValue())) {
				return Messages.getString("LimitESBConsumerJobChecker.limitFeaturesWithStandalone.SR",
						nodeType.getComponentName());
			}
			if (param.getName().equals("SERVICE_LOCATOR") && "true".equals(param.getValue())) {
				useLocator = true;
			}
			if (param.getName().equals("SERVICE_ACTIVITY_MONITOR") && "true".equals(param.getValue())) {
				useSAM = true;
			}
		}
		if(useLocator) {
			return Messages.getString("LimitESBConsumerJobChecker.limitFeaturesWithStandalone.locator",
					nodeType.getComponentName());
		}
		if(useSAM) {
			return Messages.getString("LimitESBConsumerJobChecker.limitFeaturesWithStandalone.SAM",
					nodeType.getComponentName());
		}
		return null;
	}

	/**
	 * Checks if is esb job node.
	 *
	 * @param nodeType
	 *            the node type
	 * @return true, if checks if is esb job node
	 */
	private boolean isESBConsumerComponent(String componentName) {
		return ArrayUtils.contains(ESB_CONSUMER_JOB_NODES, componentName);
	}

}
