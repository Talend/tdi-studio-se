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
package org.talend.designer.components.tsort.io.util;

import java.io.Closeable;

public class DeleteFileOnExitUtil {

    public static void add(String file, Closeable stream) {
        DeleteOnExitHook.hook().add(file, stream);
    }

    static {
        Runtime.getRuntime().addShutdownHook(DeleteOnExitHook.hook());
    }
}
