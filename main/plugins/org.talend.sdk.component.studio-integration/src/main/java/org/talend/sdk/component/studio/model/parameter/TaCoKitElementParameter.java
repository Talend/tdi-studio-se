/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IElement;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitElementParameter extends ElementParameter implements IAdditionalInfo {

    private static final String GUESS_BUTTON_PREFIX = "Guess Schema_";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final List<IValueChangedListener> valueChangeListeners = new ArrayList<>();

    /**
     * UPDATE_COMPONENTS {@link ElementParameter}
     * If it is set, this {@link TaCoKitElementParameter} sets it to {@code true}.
     * It will redraw UI after value is changed
     */
    private ElementParameter redrawParameter;

    private Map<String, Object> additionalInfoMap = new HashMap<>();

    /**
     * Sets tagged value "org.talend.sdk.component.source", which is used in code generation to recognize component type
     *
     * @param element {@link IElement} to which this parameter belongs to
     */
    public TaCoKitElementParameter(final IElement element) {
        super(element);
        setTaggedValue("org.talend.sdk.component.source", "tacokit");
    }

    /**
     * Returns parameter value converted to String type.
     * This method may be used to get value for serialization in repository.
     * Default implementation returns value without conversion assuming it is already stored as String.
     * Subclasses should override this method to provide correct conversion according parameter type.
     *
     * @return this parameter value
     */
    public String getStringValue() {
        return (String) getValue();
    }

    public void registerListener(final String propertyName, final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void unregisterListener(final String propertyName, final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public void addValueChangeListener(final IValueChangedListener listener) {
        valueChangeListeners.add(listener);
    }

    public boolean removeValueChangeListener(final IValueChangedListener listener) {
        return valueChangeListeners.remove(listener);
    }

    public void firePropertyChange(final String name, final Object oldValue, final Object newValue) {
        pcs.firePropertyChange(name, oldValue, newValue);
    }

    public void fireValueChange(final Object oldValue, final Object newValue) {
        for (final IValueChangedListener listener : valueChangeListeners) {
            listener.onValueChanged(this, oldValue, newValue);
        }
    }

    public void redraw() {
        if (isRedrawable()) {
            redrawParameter.setValue(true);
        }
    }

    /**
     * Computes index of specified <code>item</code> either in itemsDisplayCodeName or itemsDisplayCodeName
     * super class fields
     * This overridden implementation fixes an error, when <code>item</code> wasn't found in both arrays.
     * It returns 0 in such case instead of -1. -1 causes ArrayIndexOutOfBoundsException, when new table column is added
     *
     * @param item default closed list value
     * @return default value index in possible values array
     */
    @Override
    public int getIndexOfItemFromList(final String item) {
        final int index = super.getIndexOfItemFromList(item);
        if (index == -1) {
            return 0;
        }
        return index;
    }

    /**
     * Checks whether this {@link TaCoKitElementParameter} forces redraw after each value change
     * It forces redraw if {@link #redrawParameter} was set
     *
     * @return true, if it forces redraw; false - otherwise
     */
    public boolean isRedrawable() {
        return redrawParameter != null;
    }

    public interface IValueChangedListener {

        void onValueChanged(final TaCoKitElementParameter elementParameter, final Object oldValue, final Object
                newValue);
    }

    /**
     * Creates name for Guess Schema button ElementParameter
     *
     * @param schemaName a name of schema, which is guessed by the button
     * @return name for Guess Schema button ElementParameter
     */
    public static String guessButtonName(final String schemaName) {
        return GUESS_BUTTON_PREFIX + schemaName;
    }

    /**
     * UPDATE_COMPONENTS {@link ElementParameter}
     * If it is set, this {@link TaCoKitElementParameter} sets it to {@code true}.
     * It will redraw UI after value is changed
     */
    public void setRedrawParameter(final ElementParameter redrawParameter) {
        this.redrawParameter = redrawParameter;
    }

    @Override
    public Object getInfo(final String key) {
        return additionalInfoMap.get(key);
    }

    @Override
    public void putInfo(final String key, final Object value) {
        additionalInfoMap.put(key, value);
    }

    @Override
    public void onEvent(final String event, final Object... parameters) {
        // nothing to do
    }

    @Override
    public void cloneAddionalInfoTo(final IAdditionalInfo targetAdditionalInfo) {
        if (targetAdditionalInfo == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : additionalInfoMap.entrySet()) {
            targetAdditionalInfo.putInfo(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets parameter value and fires parameter change event, which is handled by registered listeners.
     * Subclasses should extend (override and call super.setValue()) this method to provide correct conversion, when
     * they use other value type than String.
     *
     * @param newValue value to be set
     */
    @Override
    public void setValue(final Object newValue) {
        final Object oldValue = super.getValue();
        super.setValue(newValue);
        firePropertyChange(getName(), oldValue, newValue);
        fireValueChange(oldValue, newValue);
        redraw();
    }

    public void updateValueOnly(final Object newValue) {
        super.setValue(newValue);
    }
}
