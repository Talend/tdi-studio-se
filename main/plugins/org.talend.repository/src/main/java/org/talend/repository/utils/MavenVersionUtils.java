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

import java.util.ArrayList;

import org.eclipse.emf.common.util.EMap;
import org.osgi.framework.Version;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.maven.MavenConstants;

public class MavenVersionUtils {

    public final static String DOT_SNAPSHOT = ".SNAPSHOT"; //$NON-NLS-1$

    public final static String SLASH_SNAPSHOT = "-SNAPSHOT"; //$NON-NLS-1$

    private final static String DOT = "."; //$NON-NLS-1$

    public final static String DEFAULT_MAVEN_VERSION = "1.0.0"; //$NON-NLS-1$

    private static ArrayList<ERepositoryObjectType> versioningTypes;

    private static ArrayList<ERepositoryObjectType> hasSubjobTypes;

    public static boolean isValidMavenVersion(String version, boolean isSnapshot) {
        if (version == null || version.trim().equals("")) { //$NON-NLS-1$
            return false;
        }

        String mavenVersionPattern = "\\d+\\.\\d+\\.\\d+.*"; //$NON-NLS-1$
        String[] fragments = version.split(" "); //$NON-NLS-1$
        if (fragments.length > 1) {
            return false;
        }
        if (!version.matches(mavenVersionPattern)) {
            return false;
        }
        version = version.trim();
        if (isSnapshot) {
            version += MavenVersionUtils.DOT_SNAPSHOT;
        }
        try {
            Version.parseVersion(version);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int compareVersion(String version1, String version2) {
        String[] version1Fragments = version1.split("\\.");
        String[] version2Fragments = version2.split("\\.");
        if (!version1Fragments[0].equals(version2Fragments[0])) {
            return version1Fragments[0].compareTo(version2Fragments[0]);
        }
        if (!version1Fragments[1].equals(version2Fragments[1])) {
            return version1Fragments[1].compareTo(version2Fragments[1]);
        }
        return version1Fragments[2].compareTo(version2Fragments[2]);
    }

    public static String getItemMavenVersion(Property property) {
        String version = null;
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            version = (String) additionalProperties.get(MavenConstants.NAME_USER_VERSION);
        }
        if (version == null) {
            version = getDefaultVersion(property.getVersion());
        }
        return version;
    }

    public static void setItemMavenVersion(Property property, String version) {
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null) {
            if (version == null || getDefaultVersion(property.getVersion()).equals(version)) {
                if (additionalProperties.containsKey(MavenConstants.NAME_USER_VERSION)) {
                    additionalProperties.remove(MavenConstants.NAME_USER_VERSION);
                }
            } else {
                additionalProperties.put(MavenConstants.NAME_USER_VERSION, version);
            }
        }
    }

    public static boolean isItemUseSnapshot(Property property) {
        boolean useSnapshot = false;
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            useSnapshot = additionalProperties.containsKey(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);
        }
        return useSnapshot;
    }

    public static void setItemUseSnapshot(Property property, String useSnapshot) {
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null) {
            if (useSnapshot == null) {
                if (additionalProperties.containsKey(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT)) {
                    additionalProperties.remove(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);
                }
            } else {
                additionalProperties.put(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, useSnapshot);
            }
        }
    }

    public static boolean isVersioningType(ERepositoryObjectType type) {
        if (versioningTypes == null) {
            versioningTypes = new ArrayList<ERepositoryObjectType>();
            if (ERepositoryObjectType.PROCESS != null) {
                versioningTypes.add(ERepositoryObjectType.PROCESS);
            }
            if (ERepositoryObjectType.PROCESS_MR != null) {
                versioningTypes.add(ERepositoryObjectType.PROCESS_MR);
            }
            if (ERepositoryObjectType.PROCESS_STORM != null) {
                versioningTypes.add(ERepositoryObjectType.PROCESS_STORM);
            }
            if (ERepositoryObjectType.PROCESS_ROUTE != null) {
                versioningTypes.add(ERepositoryObjectType.PROCESS_ROUTE);
            }
            ERepositoryObjectType serviceType = ERepositoryObjectType.valueOf("SERVICES"); //$NON-NLS-1$
            if (serviceType != null) {
                versioningTypes.add(serviceType);
            }

        }
        return versioningTypes.contains(type);
    }

    public static boolean isHasSubjobType(ERepositoryObjectType type) {
        if (hasSubjobTypes == null) {
            hasSubjobTypes = new ArrayList<ERepositoryObjectType>();
            if (ERepositoryObjectType.PROCESS != null) {
                hasSubjobTypes.add(ERepositoryObjectType.PROCESS);
            }
            if (ERepositoryObjectType.PROCESS_MR != null) {
                hasSubjobTypes.add(ERepositoryObjectType.PROCESS_MR);
            }
            if (ERepositoryObjectType.PROCESS_ROUTE != null) {
                hasSubjobTypes.add(ERepositoryObjectType.PROCESS_ROUTE);
            }
        }
        return hasSubjobTypes.contains(type);
    }

    public static String getDefaultVersion(String version) {
        String defaultVersion = VersionUtils.getPublishVersion(version);
        return defaultVersion == null ? "" : defaultVersion; //$NON-NLS-1$
    }

    public static synchronized boolean containsKey(Object object, Object key) {
        if (object instanceof IProcess2) {
            return ((IProcess2) object).getAdditionalProperties().containsKey(key);
        }
        if (object instanceof Property) {
            return ((Property) object).getAdditionalProperties().containsKey(key);
        }
        return false;
    }

    public static synchronized void remove(Object object, Object key) {
        if (object instanceof IProcess2) {
            ((IProcess2) object).getAdditionalProperties().remove(key);
        } else if (object instanceof Property) {
            ((Property) object).getAdditionalProperties().remove(key);
        }
    }

    public static synchronized Object get(Object object, Object key) {
        if (object instanceof IProcess2) {
            return ((IProcess2) object).getAdditionalProperties().get(key);
        } else if (object instanceof Property) {
            return ((Property) object).getAdditionalProperties().get(key);
        }
        return null;
    }

    public static synchronized void put(Object object, Object key, Object value) {
        if (object instanceof IProcess2) {
            ((IProcess2) object).getAdditionalProperties().put(key, value);
        } else if (object instanceof Property) {
            ((Property) object).getAdditionalProperties().put(key, value);
        }
    }

    public static synchronized boolean isAdditionalPropertiesNull(Object object) {
        if (object instanceof IProcess2) {
            return ((IProcess2) object).getAdditionalProperties() == null;
        }
        if (object instanceof Property) {
            return ((Property) object).getAdditionalProperties() == null;
        }
        return true;
    }

    public static String increaseVersion(String version) {
        String[] versionFragments = version.split("\\.");
        assert (versionFragments.length == 3);
        int a = Integer.valueOf(versionFragments[0]);
        int b = Integer.valueOf(versionFragments[1]);
        int c = Integer.valueOf(versionFragments[2]);

        return a + DOT + b + DOT + (c + 1);
    }

}
