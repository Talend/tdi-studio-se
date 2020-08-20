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
package org.talend.sdk.component.studio.model.parameter;

import java.util.Objects;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * Special ElementParameter for carrying layout information to be used during Settings Composite filling
 */
public class LayoutParameter extends ElementParameter {

    public static final String NAME_PREFIX = "LAYOUT_";

    private final Layout layout;

    public LayoutParameter(final IElement element, final Layout layout, final EComponentCategory category) {
        super(element);
        Objects.requireNonNull(layout);
        Objects.requireNonNull(category);
        this.layout = layout;
        setName(name(category));
        super.setCategory(category);
        super.setFieldType(EParameterFieldType.TECHNICAL);
    }

    public static String name(final EComponentCategory category) {
        Objects.requireNonNull(category);
        if (category == EComponentCategory.BASIC) {
            return name(Metadatas.MAIN_FORM);
        }
        if (category == EComponentCategory.ADVANCED) {
            return name(Metadatas.ADVANCED_FORM);
        }
        throw new IllegalArgumentException("Unsupported category " + category);
    }

    public static String name(final String form) {
        Objects.requireNonNull(form);
        return NAME_PREFIX + form.toUpperCase();
    }

    public Layout getLayout() {
        return this.layout;
    }
}
