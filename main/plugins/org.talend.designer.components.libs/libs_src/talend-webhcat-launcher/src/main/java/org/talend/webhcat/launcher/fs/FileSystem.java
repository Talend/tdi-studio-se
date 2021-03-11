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
package org.talend.webhcat.launcher.fs;

import java.io.InputStream;

public interface FileSystem {
	boolean exists(String file);
	InputStream open(String file);
	void delete(String folder);
	void mkdir(String folder);
	void copyFromLocal(String localFile, String target);
	String getFileSystemPrefix();
}
