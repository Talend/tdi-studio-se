// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.publish.core.models;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class BundleModel extends BaseModel {

	private final File file;
    private Map<File, String> artifacts;

	public BundleModel(String groupId, String artifactId, String version) {
		this(groupId, artifactId, version, null);
	}

	public BundleModel(String groupId, String artifactId, String version, File file) {
		super(groupId, artifactId, version);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

    public String getExtension() {
        String filename = file.getName();
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public void addArtifact(File file, String suffix) {
        if (null == artifacts) {
            artifacts = new HashMap<File, String>();
        }
        artifacts.put(file, suffix);
    }

    public Map<File, String> getArtifacts() {
        if (null == artifacts) {
            return Collections.emptyMap();
        }
        return artifacts;
    }
}
