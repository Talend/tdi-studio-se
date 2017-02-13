package org.talend.repository.utils;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EMap;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.maven.MavenConstants;

public class MavenVersionUtils {

    public static final String DEFAULT_MAVEN_VERSION = "1.0.0"; //$NON-NLS-1$

    private static ArrayList<ERepositoryObjectType> versioningTypes;

    private static ArrayList<ERepositoryObjectType> hasSubjobTypes;

    public static boolean isValidMavenVersion(String version) {
        String mavenVersionPattern = "\\d+\\.\\d+.*"; //$NON-NLS-1$
        String[] fragments = version.split(" "); //$NON-NLS-1$
        if (fragments.length > 1) {
            return false;
        }
        return version.matches(mavenVersionPattern);
    }

    public static String getItemMavenVersion(Property property) {
        String version = null;
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            version = (String) additionalProperties.get(MavenConstants.NAME_USER_VERSION);
        }
        if (version == null) {
            version = ""; //$NON-NLS-1$
        }
        return version;
    }

    public static void setItemMavenVersion(Property property, String version) {
        EMap additionalProperties = property.getAdditionalProperties();
        if (additionalProperties != null) {
            additionalProperties.put(MavenConstants.NAME_USER_VERSION, version);
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
            if (ERepositoryObjectType.TEST_CONTAINER != null) {
                versioningTypes.add(ERepositoryObjectType.TEST_CONTAINER);
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
            if (ERepositoryObjectType.TEST_CONTAINER != null) {
                hasSubjobTypes.add(ERepositoryObjectType.TEST_CONTAINER);
            }
        }
        return hasSubjobTypes.contains(type);
    }

}
