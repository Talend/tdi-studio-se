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
package org.talend.repository.ftp.ui.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.view.di.metadata.tester.CoMetadataNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class FtpMetadataNodeTester extends CoMetadataNodeTester {

    private static final String IS_FTP = "isFtp"; //$NON-NLS-1$

    @Override
    protected ERepositoryObjectType findType(String property) {
        if (IS_FTP.equals(property)) {
            return ERepositoryObjectType.METADATA_FILE_FTP;
        }
        return null;
    }
}
