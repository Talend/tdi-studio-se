package org.talend.designer.publish.core.filters;

import org.talend.repository.model.IRepositoryNode;

public interface NodeFilter {

	/**
	 * Check node.
	 *
	 * @param node the node
	 * @return the error message if exists
	 */
	String checkNode(IRepositoryNode node);

}
