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
package org.talend.repository.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class EsbConfigUtils {

    private EsbConfigUtils() {
    }

    /**
     * obtain ESB config location folder
     */
    public static File getEclipseEsbFolder() {
        try {
            return new File(new URL((String) System.getProperties().get("eclipse.home.location")).getPath(), "esb"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
