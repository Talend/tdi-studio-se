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
package org.talend.repository.imports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.CoreImageProvider;

/**
 * DOC hcw class global comment. Detailled comment
 *
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
public class TreeBuilder {

    static final ERepositoryObjectType ALL_TYPES[] = { ERepositoryObjectType.BUSINESS_PROCESS, ERepositoryObjectType.PROCESS,
            ERepositoryObjectType.JOBLET, ERepositoryObjectType.CONTEXT, ERepositoryObjectType.ROUTINES,
            ERepositoryObjectType.SQLPATTERNS, ERepositoryObjectType.METADATA_CONNECTIONS,
            ERepositoryObjectType.METADATA_FILE_DELIMITED, ERepositoryObjectType.METADATA_FILE_POSITIONAL,
            ERepositoryObjectType.METADATA_FILE_REGEXP, ERepositoryObjectType.METADATA_FILE_XML,
            ERepositoryObjectType.METADATA_FILE_EXCEL, ERepositoryObjectType.METADATA_FILE_LDIF,
            ERepositoryObjectType.METADATA_LDAP_SCHEMA, ERepositoryObjectType.METADATA_GENERIC_SCHEMA,
            ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA, ERepositoryObjectType.METADATA_WSDL_SCHEMA,
            ERepositoryObjectType.DOCUMENTATION, ERepositoryObjectType.RECYCLE_BIN };

    static Map<ERepositoryObjectType, Integer> typeOrders = new HashMap<ERepositoryObjectType, Integer>();

    static {
        for (int i = 0; i < ALL_TYPES.length; i++) {
            if (ALL_TYPES[i] != null) {
                typeOrders.put(ALL_TYPES[i], i);
            }
        }
    }

    Set<Project> inputs = new HashSet<Project>();

    Map<String, ProjectNode> projects = new HashMap<String, ProjectNode>();

    TreeBuilder() {
    }

    public static int compare(FolderNode o1, FolderNode o2) {
        return o1.getLabel().compareTo(o2.getLabel());
    }

    public static int compare(ProjectNode o1, ProjectNode o2) {
        return o1.project.getLabel().compareTo(o2.project.getLabel());
    }

    public static int compare(TypeNode o1, TypeNode o2) {
        Integer to1 = typeOrders.get(o1.type);
        Integer to2 = typeOrders.get(o2.type);
        if (to1 == null || to2 == null) {
            return 0;
        }
        return to1 - to2;
    }

    public static int compare(ItemRecord o1, ItemRecord o2) {
        int res = o1.getProperty().getLabel().compareTo(o2.getProperty().getLabel());
        if (res == 0) {
            // item with same name, should compare version
            return VersionUtils.compareTo(o1.getProperty().getVersion(), o2.getProperty().getVersion());
        } else {
            return res;
        }
    }

    public static ViewerSorter createSorter() {
        return new ViewerSorter() {

            @Override
            public int compare(Viewer v, Object o1, Object o2) {
                if (o1 instanceof ProjectNode) {
                    return TreeBuilder.compare((ProjectNode) o1, (ProjectNode) o2);
                } else if (o1 instanceof TypeNode) {
                    return TreeBuilder.compare((TypeNode) o1, (TypeNode) o2);
                }
                // else if (o1 instanceof ItemRecord) {
                // return TreeBuilder.compare((ItemRecord) o1, (ItemRecord) o2);
                // } else if (o1 instanceof FolderNode) {
                // return TreeBuilder.compare((FolderNode) o1, (FolderNode) o2);
                // }
                // return super.compare(v, o1, o2);

                // ignore ItemRecord and FolderNode, they are already sorted
                return 0;

            }

        };
    }

    public void addItem(Project project, ItemRecord itemRecord) {
        ProjectNode node = projects.get(project.getTechnicalLabel());
        if (node == null) {
            node = new ProjectNode(project);
            projects.put(project.getTechnicalLabel(), node);
        }
        node.addItem(itemRecord);
    }

    public List<ProjectNode> getInput() {
        List<ProjectNode> nodes = new ArrayList<ProjectNode>(projects.values());

        // sort the project by label
        Collections.sort(nodes, new Comparator<ProjectNode>() {

            @Override
            public int compare(ProjectNode o1, ProjectNode o2) {
                return TreeBuilder.compare(o1, o2);
            }
        });

        return nodes;
    }

    /**
     * DOC hcw Comment method "clear".
     */
    public void clear() {
        inputs.clear();
        projects.clear();
    }

    /**
     *
     * DOC hcw TreeBuilder class global comment. Detailled comment
     */
    public interface IContainerNode {

        public String getLabel();

        public boolean hasChildren();

        public List getChildren();

        public Image getImage();
    }

    /**
     *
     * DOC hcw ImportItemUtil class global comment. Detailled comment
     */
    public class ProjectNode implements IContainerNode {

        boolean sorted = false;

        Project project;

        Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();

        Map<ERepositoryObjectType, TypeNode> typeMap = new HashMap<ERepositoryObjectType, TypeNode>();

        ProjectNode(Project project) {
            this.project = project;
        }

        @Override
        public String getLabel() {
            return project.getLabel();
        }

        public Project getProject() {
            return this.project;
        }

        @Override
        public Image getImage() {
            return ImageProvider.getImage(ECoreImage.PROJECT_ICON);
        }

        public void addItem(ItemRecord itemRecord) {
            sorted = false;
            ERepositoryObjectType type = itemRecord.getType();
            boolean isdelete = itemRecord.getProperty().getItem().getState().isDeleted();
            if (isdelete) {
                type = ERepositoryObjectType.RECYCLE_BIN;
            }
            types.add(type);
            TypeNode folder = typeMap.get(type);
            if (folder == null) {
                folder = new TypeNode(type);
                typeMap.put(type, folder);
            }
            folder.add(itemRecord);
        }

        private void sort(List<TypeNode> nodes) {
            if (sorted == false) {

                Collections.sort(nodes, new Comparator<TypeNode>() {

                    @Override
                    public int compare(TypeNode o1, TypeNode o2) {
                        return TreeBuilder.compare(o1, o2);
                    }
                });

                sorted = true;
            }
        }

        @Override
        public List<TypeNode> getChildren() {
            List<TypeNode> nodes = new ArrayList<TypeNode>(typeMap.values());
            // sort the folder by type
            sort(nodes);

            return nodes;
        }

        @Override
        public boolean hasChildren() {
            return types.size() > 0;
        }
    }

    /**
     *
     * DOC chuang TreeBuilder class global comment. Detailled comment
     */
    public class FolderNode implements IContainerNode {

        private String label;

        List<ItemRecord> items = new ArrayList<ItemRecord>();

        List<FolderNode> folders = new ArrayList<FolderNode>();

        boolean sorted = false;

        /**
         * DOC chuang FolderNode constructor comment.
         *
         * @param lastSegment
         */
        public FolderNode(String label) {
            this.label = label;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.localprovider.imports.TreeBuilder.IContainerNode#getChildren()
         */
        @Override
        public List getChildren() {
            sort();
            List list = new ArrayList(folders);
            list.addAll(items);
            return list;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.localprovider.imports.TreeBuilder.IContainerNode#getImage()
         */
        @Override
        public Image getImage() {
            return CoreImageProvider.getImage(ERepositoryObjectType.FOLDER);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.localprovider.imports.TreeBuilder.IContainerNode#getLabel()
         */
        @Override
        public String getLabel() {
            return label;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.localprovider.imports.TreeBuilder.IContainerNode#hasChildren()
         */
        @Override
        public boolean hasChildren() {
            return !items.isEmpty() || !folders.isEmpty();
        }

        public void add(ItemRecord itemRecord) {
            items.add(itemRecord);
            sorted = false;
        }

        /**
         * DOC chuang Comment method "add".
         *
         * @param folder
         */
        public void add(FolderNode folder) {
            folders.add(folder);
            sorted = false;
        }

        /**
         *
         * Sort item by label and version.
         */
        void sort() {
            if (sorted == false) {

                Collections.sort(items, new Comparator<ItemRecord>() {

                    @Override
                    public int compare(ItemRecord o1, ItemRecord o2) {
                        Property p1 = o1.getProperty();
                        Property p2 = o2.getProperty();
                        if (p1 != null && p2 != null && p1.getLabel() != null && p2.getLabel() != null) {
                            int res = p1.getLabel().compareTo(p2.getLabel());
                            if (res == 0) {
                                // item with same name, should compare version
                                return VersionUtils.compareTo(p1.getVersion(), p2.getVersion());
                            } else {
                                return res;
                            }
                        }
                        return 0;
                    }
                });

                Collections.sort(folders, new Comparator<FolderNode>() {

                    @Override
                    public int compare(FolderNode o1, FolderNode o2) {
                        return TreeBuilder.compare(o1, o2);
                    }
                });

                sorted = true;
            }
        }

    }

    /**
     *
     * DOC hcw ImportItemUtil class global comment. Detailled comment
     */
    public class TypeNode extends FolderNode {

        ERepositoryObjectType type;

        Map<String, FolderNode> folderMap = new HashMap<String, FolderNode>();

        public TypeNode(ERepositoryObjectType type) {
            super(type.toString());
            this.type = type;
        }

        @Override
        public Image getImage() {
            return CoreImageProvider.getImage(type);
        }

        @Override
        public String getLabel() {
            return type.toString();
        }

        @Override
        public void add(ItemRecord itemRecord) {

            String path = itemRecord.getProperty().getItem().getState().getPath();
            if (StringUtils.isBlank(path)) {
                super.add(itemRecord);
            } else {
                FolderNode folder = folderMap.get(path.toString());
                if (folder == null) {
                    folder = createFolder(new Path(path));
                }
                folder.add(itemRecord);
            }
            sorted = false;
        }

        /**
         * DOC chuang Comment method "createFolder".
         *
         * @param path
         * @return
         */
        private FolderNode createFolder(IPath path) {
            FolderNode folder = folderMap.get(path.toString());
            if (folder != null) {
                return folder;
            }

            String lastSegment = path.lastSegment();
            if (lastSegment != null) {
                folder = new FolderNode(lastSegment);
                folderMap.put(path.toString(), folder);
                // create parent folder
                FolderNode parent = createFolder(path.removeLastSegments(1));
                if (parent != null) {
                    parent.add(folder);
                } else {
                    super.add(folder);
                }

                return folder;
            } else {
                return null;
            }

        }

    }
}
