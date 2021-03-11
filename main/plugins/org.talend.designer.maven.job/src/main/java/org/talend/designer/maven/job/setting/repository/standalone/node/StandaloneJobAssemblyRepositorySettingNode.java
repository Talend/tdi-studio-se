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
package org.talend.designer.maven.job.setting.repository.standalone.node;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.PreferencePage;
import org.talend.designer.maven.job.setting.repository.standalone.page.StandaloneJobAssemblyRepositorySettingPage;
import org.talend.designer.maven.ui.setting.repository.node.RepositoryMavenSettingNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandaloneJobAssemblyRepositorySettingNode extends RepositoryMavenSettingNode {

    public StandaloneJobAssemblyRepositorySettingNode(String id, IFile assemblyFile) {
        super(id, assemblyFile);
    }

    @Override
    protected PreferencePage createPreferencePage() {
        return new StandaloneJobAssemblyRepositorySettingPage(this.getFile());
    }
}
