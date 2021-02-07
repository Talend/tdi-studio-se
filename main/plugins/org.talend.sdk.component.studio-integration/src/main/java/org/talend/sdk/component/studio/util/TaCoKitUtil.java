/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.WizardRegistry;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.updates.runtime.utils.PathUtils;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitUtil {

    /**
     * Get ConnectionItem from specified project
     * 
     * @param project {@link Project} only search from the given project
     * @param itemId item id
     * @return stored item of the given parameters, or null
     * @throws Exception unexpected exception occured during searching
     */
    public static ConnectionItem getLatestTaCoKitConnectionItem(final Project project, final String itemId)
            throws Exception {
        IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(project, itemId, null,
                TaCoKitConst.METADATA_TACOKIT);
        if (lastVersion != null) {
            return (ConnectionItem) lastVersion.getProperty().getItem();
        }
        return null;
    }

    /**
     * Get ConnectionItem from main project or it's reference project
     * 
     * @param itemId item id
     * @return stored item of the given parameters, or null
     * @throws Exception unexpected exception occured during searching
     */
    public static ConnectionItem getLatestTaCoKitConnectionItem(final String itemId) throws Exception {
        ConnectionItem item = getLatestTaCoKitConnectionItem(ProjectManager.getInstance().getCurrentProject(), itemId);
        if (item != null) {
            return item;
        }
        List<Project> allReferencedProjects = ProjectManager.getInstance().getAllReferencedProjects();
        if (allReferencedProjects != null && !allReferencedProjects.isEmpty()) {
            for (Project referenceProject : allReferencedProjects) {
                item = getLatestTaCoKitConnectionItem(referenceProject, itemId);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public static IPath getTaCoKitBaseFolder(final ConfigTypeNode configNode) {
        if (configNode == null) {
            return null;
        }
        IPath baseFolderPath = new Path(""); //$NON-NLS-1$
        String parentId = configNode.getParentId();
        if (!isEmpty(parentId)) {
            ConfigTypeNode parentTypeNode = Lookups.taCoKitCache().getConfigTypeNodeMap().get(parentId);
            if (parentTypeNode == null) {
                throw new NullPointerException("Can't find parent node: " + parentId);
            }
            baseFolderPath = getTaCoKitBaseFolder(parentTypeNode);
        }
        // better to use lowercase, since different OS support different path name
        String configName = getTaCoKitFolderName(configNode);
        baseFolderPath = baseFolderPath.append(configName);
        return baseFolderPath;
    }

    public static String getTaCoKitFolderName(final ConfigTypeNode configNode) {
        return configNode.getName().toLowerCase();
    }

    public static TaCoKitConfigurationModel getTaCoKitConfigurationModel(final String itemId) throws Exception {
        ConnectionItem item = getLatestTaCoKitConnectionItem(itemId);
        if (item != null) {
            return new TaCoKitConfigurationModel(item.getConnection());
        }
        return null;
    }

    public static String getConfigTypePath(final ConfigTypeNode configTypeNode) {
        IPath tacokitPath = new Path(TaCoKitConst.METADATA_TACOKIT.getFolder());
        IPath path = tacokitPath.append(getTaCoKitBaseFolder(configTypeNode));
        return path.toPortableString();
    }

    public static ERepositoryObjectType getOrCreateERepositoryObjectType(final ConfigTypeNode configTypeNode)
            throws Exception {
        if (configTypeNode == null) {
            return null;
        }
        IPath tacokitPath = new Path(TaCoKitConst.METADATA_TACOKIT.getFolder());
        IPath baseFolder = getTaCoKitBaseFolder(configTypeNode);
        IPath path = tacokitPath.append(baseFolder);
        String portableStr = path.toPortableString();

        String type = portableStr.replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$
        String alias = portableStr.replaceAll("/", "_"); //$NON-NLS-1$//$NON-NLS-2$

        ERepositoryObjectType eType = ERepositoryObjectType.valueOf(type);
        if (eType == null) {
            eType = new WizardRegistry().createRepositoryObjectType(type, baseFolder.toPortableString(), alias,
                    portableStr, 1, // $NON-NLS-1$
                    new String[] { ERepositoryObjectType.PROD_DI });
            ConfigTypeNode parentTypeNode =
                    Lookups.taCoKitCache().getConfigTypeNodeMap().get(configTypeNode.getParentId());
            if (parentTypeNode == null) {
                eType.setAParent(TaCoKitConst.METADATA_TACOKIT);
            } else {
                eType.setAParent(getOrCreateERepositoryObjectType(parentTypeNode));
            }
        }
        return eType;
    }

    public static boolean isTaCoKitType(final ERepositoryObjectType repObjType) {
        if (repObjType == null) {
            return false;
        }
        if (TaCoKitConst.METADATA_TACOKIT.equals(repObjType)) {
            return true;
        }
        ERepositoryObjectType[] parentTypesArray = repObjType.getParentTypesArray();
        if (parentTypesArray == null || parentTypesArray.length <= 0) {
            return false;
        }
        for (ERepositoryObjectType parentType : parentTypesArray) {
            if (isTaCoKitType(parentType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(final String str1, final String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static boolean isEmpty(final String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(final String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * Method to create component name from component's family name and component's name itself.
     * 
     * @param familyName component's family name
     * @param componentName component's name
     * @return full component name
     */
    public static String getFullComponentName(final String familyName, final String componentName) {
        return familyName + TaCoKitConst.COMPONENT_NAME_SEPARATOR + componentName;
    }

    public static Collection<ConfigTypeNode> filterTopLevelNodes(Collection<ConfigTypeNode> nodes) {
        Collection<ConfigTypeNode> filteredNodes = new ArrayList<>();
        if (nodes != null && !nodes.isEmpty()) {
            for (ConfigTypeNode node : nodes) {
                String parentId = node.getParentId();
                String configType = node.getConfigurationType();
                if (StringUtils.isNotBlank(parentId) || StringUtils.isNotBlank(configType)) {
                    continue;
                }
                filteredNodes.add(node);
            }
        }
        return filteredNodes;
    }

    public static String getInstalledComponentsString(IProgressMonitor progress) throws Exception {
        File studioConfigFile = PathUtils.getStudioConfigFile();
        Properties configProps = PathUtils.readProperties(studioConfigFile);
        return configProps.getProperty(TaCoKitConst.PROP_COMPONENT);
    }

    public static List<GAV> getInstalledComponents(IProgressMonitor progress) throws Exception {
        String tckCompConfString = getInstalledComponentsString(progress);
        if (StringUtils.isNotBlank(tckCompConfString)) {
            return TaCoKitUtil.convert2GAV(tckCompConfString);
        }
        return Collections.EMPTY_LIST;
    }

    public static List<GAV> convert2GAV(String gavString) {
        List<GAV> gavs = new ArrayList<>();
        String[] componentsStr = gavString.split(","); //$NON-NLS-1$
        for (String componentStr : componentsStr) {
            String[] component = componentStr.split(":"); //$NON-NLS-1$
            GAV gav = new GAV();
            gav.setGroupId(component[0]);
            gav.setArtifactId(component[1]);
            gav.setVersion(component[2]);
            if (3 < component.length) {
                gav.setClassifier(component[3]);
            }
            if (4 < component.length) {
                gav.setType(component[4]);
            }
            gavs.add(gav);
        }
        return gavs;
    }

    public static void checkMonitor(IProgressMonitor monitor) throws Exception {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("progress.cancel"); //$NON-NLS-1$
            }
        }
    }

    public static int getConfigTypeVersion(final PropertyDefinitionDecorator p, final ConfigTypeNodes configTypeNodes,
            final String familyId) {
        final String type = p.getMetadata().get("configurationtype::type");
        final String name = p.getMetadata().get("configurationtype::name");
        return configTypeNodes.getNodes().values().stream().filter(c -> c.getConfigurationType() != null && c.getName() != null)
                .filter(c -> c.getConfigurationType().equals(type) && c.getName().equals(name))
                .filter(c -> familyId.equals(getPropertyFamilyId(c, configTypeNodes))).findFirst().map(ConfigTypeNode::getVersion)
                .orElse(-1);
    }

    public static String getPropertyFamilyId(final ConfigTypeNode it, final ConfigTypeNodes nodes) {
        if (it.getParentId() == null) {
            return null;
        }
        String parent = it.getParentId();
        while (nodes.getNodes().get(parent) != null && nodes.getNodes().get(parent).getParentId() != null) {
            parent = nodes.getNodes().get(parent).getParentId();
        }
        return parent;
    }

    public static class GAV {

        private String groupId;

        private String artifactId;

        private String version = ""; //$NON-NLS-1$

        private String classifier = ""; //$NON-NLS-1$

        private String type = ""; //$NON-NLS-1$

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.artifactId == null) ? 0 : this.artifactId.hashCode());
            result = prime * result + ((this.classifier == null) ? 0 : this.classifier.hashCode());
            result = prime * result + ((this.groupId == null) ? 0 : this.groupId.hashCode());
            result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
            result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GAV other = (GAV) obj;
            if (this.artifactId == null) {
                if (other.artifactId != null)
                    return false;
            } else if (!this.artifactId.equals(other.artifactId))
                return false;
            if (this.classifier == null) {
                if (other.classifier != null)
                    return false;
            } else if (!this.classifier.equals(other.classifier))
                return false;
            if (this.groupId == null) {
                if (other.groupId != null)
                    return false;
            } else if (!this.groupId.equals(other.groupId))
                return false;
            if (this.type == null) {
                if (other.type != null)
                    return false;
            } else if (!this.type.equals(other.type))
                return false;
            if (this.version == null) {
                if (other.version != null)
                    return false;
            } else if (!this.version.equals(other.version))
                return false;
            return true;
        }

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            return "GAV [groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version=" + this.version
                    + ", classifier=" + this.classifier + ", type=" + this.type + "]";
        }

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return this.artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getClassifier() {
            return this.classifier;
        }

        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
