/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.model;

/**
 * Stores return variables constants
 */
public final class ReturnVariables {

    public static final String RETURN_ERROR_MESSAGE = "errorMessage";

    public static final String RETURN_TOTAL_RECORD_COUNT = "totalRecordCount";

    /**
     * Denote return variable "AFTER" availability. I.e. such variable is available
     *
     * after current subjob
     */
    public static final String AFTER = "AFTER";

    private ReturnVariables() {
        new AssertionError();
    }
}
