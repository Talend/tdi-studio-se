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
package org.talend.designer.maven.job.setting.repository.standalone.page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.maven.job.MavenJobPlugin;
import org.talend.designer.maven.job.i18n.Messages;
import org.talend.designer.maven.job.setting.repository.standalone.StandaloneJobRepositoryMavenSetting;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.ui.setting.repository.page.FolderMavenSettingPage;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
@SuppressWarnings("rawtypes")
public class StandaloneJobRepositorySettingPage extends FolderMavenSettingPage {

    protected static final String ID_MAVEN_STANDALONE_JOB_PROJECT_SETTING = "projectsetting.StandaloneJob"; //$NON-NLS-1$

    public StandaloneJobRepositorySettingPage(RepositoryNode node) {
        super(node);
    }

    protected String createMessages(boolean created) {
        StringBuffer messages = new StringBuffer(200);
        // existed
        if (created) {
            messages.append(Messages.getString("StandaloneJobRepositorySettingPage_ExistedMavenSettingMessage",//$NON-NLS-1$
                    buildLinks(getProcessFileNames())));
        } else {
            messages.append(Messages.getString("StandaloneJobRepositorySettingPage_CreatingMavenSettingMessage", //$NON-NLS-1$
                    getProcessFileNames()));
            messages.append('\n');
            messages.append('\n');

            messages.append(Messages.getString("RepositorySettingPage_CreatingMavenSettingNote"));//$NON-NLS-1$
            messages.append(' ');
            String mvnProjectSettingLinkStr = "<a href=\"" + getDefaultProjectSettingId() + "\">Default</a>";//$NON-NLS-1$ //$NON-NLS-2$
            messages.append(Messages.getString("RepositorySettingPage_CreatingMavenSettingNoteMessage", //$NON-NLS-1$
                    mvnProjectSettingLinkStr));
        }
        messages.append('\n');

        return messages.toString();
    }

    protected String getDefaultProjectSettingId() {
        return ID_MAVEN_STANDALONE_JOB_PROJECT_SETTING;
    }

    @Override
    protected ProjectPreferenceManager getProjectSettingManager() {
        return MavenJobPlugin.getDefault().getProjectPreferenceManager();
    }

    @Override
    protected Map<String, String> getProjectSettingKeyWithFileNamesMap() {
        Map<String, String> projectSettingKeyWithFileNamesMap = new HashMap<String, String>(
                super.getProjectSettingKeyWithFileNamesMap());

        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_STANDALONE_JOB_POM,
                TalendMavenConstants.POM_FILE_NAME);
        projectSettingKeyWithFileNamesMap.put(IProjectSettingPreferenceConstants.TEMPLATE_STANDALONE_JOB_ASSEMBLY,
                TalendMavenConstants.ASSEMBLY_FILE_NAME);

        return projectSettingKeyWithFileNamesMap;
    }

    @Override
    protected List<IPreferenceNode> createMavenChildrenNodes(IFolder nodeFolder) {
        List<IPreferenceNode> standaloneJobChildrenNodes = StandaloneJobRepositoryMavenSetting.createStandaloneJobChildrenNodes(
                nodeFolder, getNode(), getPrefNodeId(), false);
        return standaloneJobChildrenNodes;
    }

}
