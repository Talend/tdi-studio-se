// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.constants;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class Log4jPrefsConstants {

    public static final String COMMON_LOGGING_PROPERTIES_TEMPLATE = "COMMON_LOGGING_PROPERTIES_TEMPLATE";

    public static final String LOG4J_PROPERTIES_TEMPLATE = "LOG4J_PROPERTIES_TEMPLATE";

    public static final String LOG4J_PROPERTIES_ACTIVATE = "LOG4J_PROPERTIES_ACTIVATE";

    public static final String LOG4J_ENABLE_NODE = "log4jEnabled";

    public static final String LOG4J_CONTENT_NODE = "log4jContent";

    public static final String LOG4J_SELECT_VERSION2 = "log4jVersion2";//$NON-NLS-1$

    public static final String LOG4J_IS_NEW_PROJECT = "isNewProject";//$NON-NLS-1$

    public static final String LOG4J1 = "log4j1(Deprecated)";//$NON-NLS-1$

    public static final String LOG4J2 = "log4j2";//$NON-NLS-1$

    public static final List<String> LOG4J_VERSIONS = Arrays.asList(LOG4J1, LOG4J2);// $NON-NLS-1$ //$NON-NLS-2$

    public static final String COMMON_LOGGING_NODE = "commonLoging";

    public static final String LOG4J_RESOURCES = "org.talend.log4j";

    public static final String LOG4j_SCOPE = "<project>";

    public static final String LOG4j_PREFS_SUFFIX = ".prefs";

    public static final String COMMONLOGINGFILEPATH = "log/common-logging.properties_template"; //$NON-NLS-1$

    public static final String LOG4J_VERSION2_FILEPATH = "log/log4j2.properties_template"; //$NON-NLS-1$

    public static final String LOG4JFILEPATH = "log/log4j.properties_template"; //$NON-NLS-1$

    public static final String LOG4J_FILE_NAME = "log4j.xml";//$NON-NLS-1$

    public static final String LOG4J2_FILE_NAME = "log4j2.xml";//$NON-NLS-1$

    public static final String COMMON_LOGGING_FILE = "common-logging.properties";

    public static final String OFF = "Off";

    public static final String TRACE = "Trace";

    public static final String DEBUG = "Debug";

    public static final String INFO = "Info";

    public static final String WARNING = "Warn";

    public static final String ERROR = "Error";

    public static final String FATAL = "Fatal";

}
