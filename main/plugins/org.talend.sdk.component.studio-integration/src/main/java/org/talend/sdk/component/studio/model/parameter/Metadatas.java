/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

/**
 * Metadata key constants, which are used in SimplePropertyDefinition
 */
public final class Metadatas {

    private Metadatas() {
        new AssertionError();
    }

    public static final String ACTION_DYNAMIC_VALUES = "action::dynamic_values";

    public static final String ACTION_HEALTHCHECK = "action::healthcheck";

    public static final String ACTION_SUGGESTIONS_NAME = "action::suggestions";

    public static final String ACTION_SUGGESTIONS_PARAMETERS = "action::suggestions::parameters";

    public static final String ACTION_VALIDATION_NAME = "action::validation";

    public static final String ACTION_VALIDATION_PARAMETERS = "action::validation::parameters";

    public static final String ACTION_UPDATABLE_VALUE = "action::update";

    public static final String ACTION_UPDATABLE_PARAMETERS = "action::update::parameters";

    public static final String ACTION_UPDATABLE_AFTER = "action::update::after";

    public static final String CONDITION_IF_VALUE = "condition::if::value";

    public static final String CONDITION_IF_TARGET = "condition::if::target";

    public static final String CONDITION_IF_NEGATE = "condition::if::negate";

    public static final String CONDITION_IF_EVALUTIONSTRATEGY = "condition::if::evaluationStrategy";

    public static final String CONFIG_TYPE = "configurationtype::type";

    public static final String CONFIG_NAME = "configurationtype::name";

    public static final String PARAMETER_INDEX = "definition::parameter::index";

    public static final String UI_CODE = "ui::code::value";

    public static final String UI_CREDENTIAL = "ui::credential";

    /**
     * UI gridlayout key prefix used to build gridlayout metadata key with arbitrary form name
     */
    public static final String UI_GRIDLAYOUT_PREFIX = "ui::gridlayout::";

    /**
     * UI gridlayout key suffix used to build gridlayout metadata key with arbitrary form name
     */
    public static final String UI_GRIDLAYOUT_SUFFIX = "::value";

    public static final String UI_OPTIONS_ORDER = "ui::optionsorder::value";

    public static final String UI_STRUCTURE_TYPE = "ui::structure::type";

    public static final String UI_STRUCTURE_VALUE = "ui::structure::value";

    public static final String UI_STRUCTURE_DISCOVERSCHEMA = "ui::structure::discoverSchema";

    public static final String UI_TEXTAREA = "ui::textarea";

    /**
     * Value separator for {@link #UI_OPTIONS_ORDER}
     */
    public static final String ORDER_SEPARATOR = ",";

    /**
     * Value separator for {@link #CONDITION_IF_VALUE}
     */
    public static final String VALUE_SEPARATOR = ",";

    public static final String MAIN_FORM = "Main";

    public static final String ADVANCED_FORM = "Advanced";

}
