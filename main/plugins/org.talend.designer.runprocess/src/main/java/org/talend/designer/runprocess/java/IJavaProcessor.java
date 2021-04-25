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
package org.talend.designer.runprocess.java;

import java.util.List;

/**
 * <pre>
 * This interface provides some methods for extracting command segments:
 * <li>{@link #extractAheadCommandSegments()} to extract ahead command segments like "cd `dirname $0`\n".
 * <li>{@link #extractJavaCommandSegments()} to extract java command segments like "java -Xms256M ".
 * <li>{@link #extractCPCommandSegments()} to extract cp command segments like "cp ../a.jar:../b.jar..".
 * <li>{@link #extractMainClassSegments()} to extract the main-class command segment.
 * <li>{@link #extractArgumentSegments()} to extract other arguments for commands.
 * </pre>
 *
 * Created by Marvin Wang on Mar 22, 2013.
 */
public interface IJavaProcessor {

    /**
     * Extracts the abead command segments like "cd `dirname $0`\n". Added by Marvin Wang on Mar 22, 2013.
     *
     * @return
     */
    List<String> extractAheadCommandSegments();

    /**
     * Extracts the segment of java part, like "Java -Xms256m", with some jvm arguments. Added by Marvin Wang on Mar 22,
     * 2013.
     *
     * @return
     */
    List<String> extractJavaCommandSegments();

    /**
     * Extracts cp command segments with classpath like "cp ../a.jar:../b.jar..". Added by Marvin Wang on Mar 22, 2013.
     *
     * @return
     */
    List<String> extractCPCommandSegments();

    /**
     * Extracts the main-class command segment. Added by Marvin Wang on Mar 22, 2013.
     *
     * @return
     * @deprecated getMainClass instead
     */
    @Deprecated
    String extractMainClassSegments();

    /**
     * Extracts other arguments for java commands. Added by Marvin Wang on Mar 22, 2013.
     *
     * @return
     */
    List<String> extractArgumentSegments();
}
