package org.talend.designer.publish.core.filters;

import java.util.ArrayList;
import java.util.List;

import org.talend.repository.model.IRepositoryNode;

public abstract class AbstractCombinedJobFilter implements NodeFilter {

	private List<NodeFilter> subFilters = new ArrayList<NodeFilter>();

	public AbstractCombinedJobFilter() {
		super();
		initFilters();
	}

	protected abstract void initFilters();

	protected void addFilter(NodeFilter nodeFilter) {
		subFilters.add(nodeFilter);
	}
	
	@Override
	public String checkNode(IRepositoryNode node) {
		for (NodeFilter nodeFilter : subFilters) {
			String err = nodeFilter.checkNode(node);
			if (err != null) {
				return err;
			}
		}
		return null;
	}
}
