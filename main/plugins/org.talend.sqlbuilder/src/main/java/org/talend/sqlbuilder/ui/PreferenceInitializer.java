// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sqlbuilder.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SqlBuilderPlugin.getDefault()
				.getPreferenceStore();

		// for PassPolicy Preference
		store.setDefault(IConstants.CLIP_EXPORT_SEPARATOR, "|"); //$NON-NLS-1$
		store.setDefault(IConstants.DATASETRESULT_DATE_FORMAT, "yyyy-MM-dd"); //$NON-NLS-1$
		store.setDefault(IConstants.DATASETRESULT_FORMAT_DATES, true);
		store.setDefault(IConstants.SQL_ASSIST, false);

		store.setDefault(IConstants.LINE_DELIMITER, "\n"); //$NON-NLS-1$

		store.setDefault(IConstants.COMMENT_DELIMITER, "#"); //$NON-NLS-1$

		store.setDefault(IConstants.ALTERNATE_DELIMITER, ""); //$NON-NLS-1$

		store.setDefault(IConstants.QUERY_DELIMITER, ";"); //$NON-NLS-1$

		store.setDefault(IConstants.WARN_RESEULTS, 5000);
	}
}
