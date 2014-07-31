package org.talend.designer.publish.core.filters.impl;

import java.util.List;

import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.publish.core.filters.AbstractJobNodeFilter;
import org.talend.designer.publish.core.i18n.Messages;

public class BanSoapServiceJobFilter extends AbstractJobNodeFilter {

	@Override
	protected String checkNodeTypes(List<NodeType> nodeTypes) {
		for (NodeType nodeType : nodeTypes) {
			if(checkIsDeactivated(nodeType)) {
				continue;
			}
			if("tESBProviderRequest".equals(nodeType.getComponentName())) {
				return Messages.NoPublishingServiceJob;
			}
		}
		return null;
	}

}
