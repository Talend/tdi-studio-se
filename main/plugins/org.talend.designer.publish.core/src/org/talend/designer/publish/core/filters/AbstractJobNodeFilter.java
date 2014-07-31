package org.talend.designer.publish.core.filters;

import java.util.Collections;
import java.util.List;

import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IRepositoryNode;

public abstract class AbstractJobNodeFilter implements NodeFilter {

	protected abstract String checkNodeTypes(List<NodeType> nodeTypes);

	@Override
	public String checkNode(IRepositoryNode node) {
		List<NodeType> nodeTypes = getProcessNodeTypes(node);
		return checkNodeTypes(nodeTypes);
	}

	@SuppressWarnings("unchecked")
	private static List<NodeType> getProcessNodeTypes(IRepositoryNode node) {
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
			if (param.getName().equals("Activate")) {
				return "false".equals(param.getValue());
			}
		}
		return false;
	}

}
