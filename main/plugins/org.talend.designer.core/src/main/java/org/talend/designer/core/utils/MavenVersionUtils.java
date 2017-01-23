package org.talend.designer.core.utils;

public class MavenVersionUtils {

    public static boolean isValidMavenVersion(String version) {
        String mavenVersionPattern = "\\d+\\.\\d+.*"; //$NON-NLS-1$
        String[] fragments = version.split(" ");
        if (fragments.length > 1) {
            return false;
        }
        return version.matches(mavenVersionPattern);
    }
    
}
