package org.talend.designer.publish.core.filters.impl;

import org.talend.designer.publish.core.filters.AbstractCombinedJobFilter;

/**
 * Add filters when trying to publish a job.
 */
public class PublishJobNodeFilter extends AbstractCombinedJobFilter {

	@Override
	protected void initFilters() {
		addFilter(new BanSoapServiceJobFilter());
	}
}
