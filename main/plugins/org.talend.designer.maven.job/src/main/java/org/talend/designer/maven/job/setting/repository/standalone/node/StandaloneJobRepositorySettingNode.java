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

import org.eclipse.jface.preference.PreferencePage;
import org.talend.designer.maven.job.i18n.Messages;
import org.talend.designer.maven.job.setting.repository.standalone.page.StandaloneJobRepositorySettingPage;
import org.talend.designer.maven.ui.setting.repository.node.RepositoryPreferenceNode;
import org.talend.designer.maven.ui.utils.DesignerMavenUiHelper;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandaloneJobRepositorySettingNode extends RepositoryPreferenceNode {

    @SuppressWarnings("nls")
    public StandaloneJobRepositorySettingNode(String parentId, RepositoryNode node) {
        super(DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, "StandaloneJob"), Messages
                .getString("StandaloneJobRepositorySettingNode_Label"), null, node);
    }

    @Override
    protected PreferencePage createPreferencePage() {
        return new StandaloneJobRepositorySettingPage(this.getNode());
    }
}
