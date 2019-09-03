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
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.utils.security.StudioEncryption;

public class PasswordMigrationUtil {

    public static String getConnectionEncryptedPassword(String pass) throws Exception {
        String encryptPass = pass;
        if (!StudioEncryption.isEncypted(pass)) {
            encryptPass = StudioEncryption.encryptPassword(pass);
        }
        return encryptPass;
    }

    public static String getConnectionDecryptedPassword(String pass) throws Exception {
        String dePass = pass;
        if (StudioEncryption.isEncypted(pass)) {
            dePass = StudioEncryption.decryptPassword(pass);
        }
        return dePass;
    }

    public static String getDecryptPassword(String pass) throws Exception {
        String cleanPass = pass;
        if (StringUtils.isNotEmpty(pass)) {
            if (StudioEncryption.isEncypted(pass)) {
                cleanPass = StudioEncryption.decryptPassword(pass);
            } else {
                try {
                    int ind = pass.lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
                    if (ind >= 0) {
                        pass = new StringBuilder(pass).replace(ind, ind + PasswordEncryptUtil.ENCRYPT_KEY.length(), "") //$NON-NLS-1$
                                .toString();
                    }
                    cleanPass = PasswordEncryptUtil.decryptPassword(pass);
                } catch (Exception e) {
                    // Ignore here
                }
            }
        }
        return cleanPass;
    }

    public static String getEncryptPasswordIfNeed(String pass) throws Exception {
        String cleanPass = getDecryptPassword(pass);
        return getConnectionEncryptedPassword(cleanPass);
    }

}
