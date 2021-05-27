/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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

import org.talend.core.model.repository.ERepositoryObjectType;

public class TaCoKitConst {

    public static final String DEFAULT_LOCALHOST = "127.0.0.1"; //$NON-NLS-1$

    public static final String BUNDLE_ID = "org.talend.sdk.component.studio-integration"; //$NON-NLS-1$

    public static final ERepositoryObjectType METADATA_TACOKIT = ERepositoryObjectType.valueOf("TACOKIT"); //$NON-NLS-1$

    public static final String METADATA_TACOKIT_PATH = METADATA_TACOKIT.getFolder();

    public static final String METADATA_PREFIX = "repository."; //$NON-NLS-1$

    public static final String IMAGE_TACOKIT_REPOSITORY_PATH = "/icon/connection.png"; //$NON-NLS-1$

    public static final String IMAGE_TACOKIT_CONFIGURATION_PATH = "/icon/configuration_16x16.png"; //$NON-NLS-1$

    public static final String GUESS_SCHEMA_COMPONENT_NAME = "tTaCoKitGuessSchema"; //$NON-NLS-1$

    //TODO remove it and replace by TACOKIT_COMPONENT_PLUGIN_NAME
    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_PLUGIN_NAME =
            "___TACOKIT_GUESS_SCHEMA_PARAMETER_PLUGIN_NAME___"; //$NON-NLS-1$

    /**
     * DON'T modify the value, otherwise please also modify it in connection_begin.javajet and close_begin.javajet
     * In fact, the same meaning with GUESS_SCHEMA_PARAMETER_PLUGIN_NAME, but not sure if that is stored in .item or any location,
     * so keep duplicated now
     */
    public static final String TACOKIT_COMPONENT_PLUGIN_NAME =
            "___TACOKIT_COMPONENT_PLUGIN_NAME___";

    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_ACTION_NAME =
            "___TACOKIT_GUESS_SCHEMA_PARAMETER_ACTION_NAME___"; //$NON-NLS-1$

    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_TACOKIT_COMPONENT_TYPE = "___TACOKIT_GUESS_SCHEMA_PARAMETER_COMPONENT_TYPE___"; //$NON-NLS-1$

    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_OUTPUT_CONNECTION_NAME = "___TACOKIT_GUESS_SCHEMA_PARAMETER_OUTPUT_CONNECTION_NAME___"; //$NON-NLS-1$

    public static final String TYPE_STRING = "STRING"; //$NON-NLS-1$

    public static final String COMPONENT_NAME_SEPARATOR = "";

    public static final String PROP_COMPONENT = "component.java.coordinates"; //$NON-NLS-1$

    public static final String PROP_COMPONENT_SEPARATOR = ","; //$NON-NLS-1$

    public static final String PROP_REGISTRY = "component.java.registry"; //$NON-NLS-1$

    public static final String COMPONENT_NAME_PREFIX = "t"; //$NON-NLS-1$

    public static final String BASE_HELP_LINK = "org.talend.help."; //$NON-NLS-1$

    public static final String MAVEN_INF = "MAVEN-INF"; //$NON-NLS-1$

    public static final String TALEND_INF = "TALEND-INF"; //$NON-NLS-1$

    public static final String PARAMETER_USE_EXISTING_CONNECTION = "USE_EXISTING_CONNECTION";

    public static final String PARAMETER_CONNECTION = "CONNECTION";

    public static final String CONFIG_NODE_ID_DATASTORE = "datastore";

    public static final String CONFIG_NODE_ID_DATASET = "dataset";

    public static final String CONFIG_NODE_ID_CONFIGURATION = "configuration";

    public static final String CONFIG_NODE_ID_CONNECTION="connection";

    public static final String CLOSE_CONNECTION_ATCION_NAME = "close_connection";

    public static final String CREATE_CONNECTION_ATCION_NAME = "create_connection";

    public static final String META_KEY_AFTER_VARIABLE = "variables::after::value"; //$NON-NLS-1$

    public static final String AFTER_VARIABLE_VALUE_DELIMITER = "\\\\:";

    public static final String AFTER_VARIABLE_LINE_DELIMITER = "\\\\;";
}
