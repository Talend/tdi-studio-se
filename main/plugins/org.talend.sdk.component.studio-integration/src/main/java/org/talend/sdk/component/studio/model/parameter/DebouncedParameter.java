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

import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.debounce.DebouncedAction;

/**
 * Fires parameter change events only if {@code TIMEOUT} passed from last value changed event
 */
public class DebouncedParameter extends TaCoKitElementParameter {

    private static final int TIMEOUT = Lookups.configuration().getDebounceTimeout();

    private final DebouncedAction debounced = Lookups.debouncer().createAction();

    public DebouncedParameter(final IElement element) {
        super(element);
    }

    @Override
    public void setValue(final Object newValue) {
        final Object oldValue = getValue();
        super.updateValueOnly(newValue);
        debounced.debounce(() -> {
            firePropertyChange("value", oldValue, newValue);
            fireValueChange(oldValue, newValue);
            redraw();
        }, TIMEOUT);
    }
}
