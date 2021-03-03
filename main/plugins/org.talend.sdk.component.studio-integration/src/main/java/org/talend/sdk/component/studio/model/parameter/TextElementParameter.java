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
package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.IAdvancedElementParameter;
import org.talend.core.model.process.IElement;

public class TextElementParameter extends TaCoKitElementParameter implements IAdvancedElementParameter {

    private String placeholder;

    /**
     * Sets tagged value "org.talend.sdk.component.source", which is used in code generation to recognize component type
     *
     * @param element {@link IElement} to which this parameter belongs to
     */
    public TextElementParameter(final IElement element) {
        super(element);
    }

    @Override
    public void setValue(final Object newValue) {
        super.setValue(newValue == null ? null : String.valueOf(newValue));
    }

    @Override
    public String getMessage() {
        return placeholder;
    }

    @Override
    public void setMessage(final String s) {
        this.placeholder = s;
    }
}
