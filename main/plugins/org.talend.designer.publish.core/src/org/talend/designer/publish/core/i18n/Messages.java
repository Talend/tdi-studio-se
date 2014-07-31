package org.talend.designer.publish.core.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.talend.designer.publish.core.i18n.messages"; //$NON-NLS-1$

	public static String NoPublishingServiceJob;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
