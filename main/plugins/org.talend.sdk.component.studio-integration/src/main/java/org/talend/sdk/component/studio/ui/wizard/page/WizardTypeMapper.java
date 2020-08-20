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
package org.talend.sdk.component.studio.ui.wizard.page;

import static org.talend.core.model.process.EParameterFieldType.HIDDEN_TEXT;
import static org.talend.core.model.process.EParameterFieldType.TABLE;
import static org.talend.core.model.process.EParameterFieldType.TEXT;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.studio.model.parameter.WidgetTypeMapper;

/**
 * Substitutes PASSWORD widget type by HIDDEN_TEXT for Credential widget
 */
public class WizardTypeMapper extends WidgetTypeMapper {

    @Override
    protected EParameterFieldType getCredentialType() {
        return HIDDEN_TEXT;
    }

    @Override
    protected  EParameterFieldType getPrevColumnListType() {
        return TEXT;
    }

    @Override
    protected  EParameterFieldType getSuggestableTableType() {
        return TABLE;
    }
}
