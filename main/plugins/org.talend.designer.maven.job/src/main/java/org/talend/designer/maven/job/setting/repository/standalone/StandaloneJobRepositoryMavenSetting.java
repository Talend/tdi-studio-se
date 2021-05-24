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
package org.talend.designer.maven.job.setting.repository.standalone;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceNode;
import org.talend.designer.maven.job.setting.repository.MavenJobRepositoryMavenSetting;
import org.talend.designer.maven.job.setting.repository.standalone.node.StandaloneJobAssemblyRepositorySettingNode;
import org.talend.designer.maven.job.setting.repository.standalone.node.StandaloneJobPomRepositorySettingNode;
import org.talend.designer.maven.job.setting.repository.standalone.node.StandaloneJobRepositorySettingNode;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.ui.utils.DesignerMavenUiHelper;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandaloneJobRepositoryMavenSetting extends MavenJobRepositoryMavenSetting {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.maven.ui.setting.repository.RepositoryMavenSetting#createMavenScriptsChildren(org.eclipse
     * .jface.preference.IPreferenceNode, org.talend.repository.model.RepositoryNode)
     */
    @Override
    public void createMavenScriptsChildren(IPreferenceNode parentNode, RepositoryNode node) {
        StandaloneJobRepositorySettingNode standaloneJobNode = new StandaloneJobRepositorySettingNode(parentNode.getId(), node);
        parentNode.add(standaloneJobNode);

        IFolder nodeFolder = DesignerMavenUiHelper.getNodeFolder(node);
        List<IPreferenceNode> standaloneJobChildrenNodes = createStandaloneJobChildrenNodes(nodeFolder, node,
                standaloneJobNode.getId(), true);
        for (IPreferenceNode n : standaloneJobChildrenNodes) {
            standaloneJobNode.add(n);
        }

    }

    public static List<IPreferenceNode> createStandaloneJobChildrenNodes(IFolder nodeFolder, RepositoryNode node,
            String parentId, boolean checkExist) {
        List<IPreferenceNode> childrenNodes = new ArrayList<IPreferenceNode>();

        // if have existed the pom and assembly
        if (!checkExist
                || DesignerMavenUiHelper.existMavenSetting(nodeFolder, TalendMavenConstants.POM_FILE_NAME,
                        TalendMavenConstants.ASSEMBLY_FILE_NAME)) {
            IFile pomTemplateFile = nodeFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
            IFile assemblyTemplateFile = nodeFolder.getFile(TalendMavenConstants.ASSEMBLY_FILE_NAME);

            String pomId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, pomTemplateFile);
            String assemblyId = DesignerMavenUiHelper.buildRepositoryPreferenceNodeId(parentId, assemblyTemplateFile);

            StandaloneJobPomRepositorySettingNode pomNode = new StandaloneJobPomRepositorySettingNode(pomId, pomTemplateFile);
            StandaloneJobAssemblyRepositorySettingNode assemblyNode = new StandaloneJobAssemblyRepositorySettingNode(assemblyId,
                    assemblyTemplateFile);

            childrenNodes.add(pomNode);
            childrenNodes.add(assemblyNode);
        }
        return childrenNodes;
    }
}
