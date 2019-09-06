// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.security.CryptoHelper;
import org.talend.utils.security.StudioEncryption;

public class PasswordMigrationUtil {

    private static StudioEncryption se = StudioEncryption.getStudioEncryption(null);

    public static String getDecryptPassword(String pass) throws Exception {
        String cleanPass = pass;
        if (StringUtils.isNotEmpty(pass)) {
            if (StudioEncryption.hasEncryptionSymbol(pass)) {
                cleanPass = se.decrypt(pass);
            } else {
                try {
                    cleanPass = CryptoHelper.getDefault().decrypt(pass);
                } catch (Exception e) {
                    // Ignore here
                }
            }
        }
        return cleanPass;
    }

    public static String getEncryptPasswordIfNeed(String pass) throws Exception {
        String cleanPass = getDecryptPassword(pass);
        return se.encrypt(cleanPass);
    }

}
