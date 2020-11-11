/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.action.IActionParameter;
import org.talend.sdk.component.studio.model.action.SettingsActionParameter;
import org.talend.sdk.component.studio.ui.composite.problemmanager.IProblemManager;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.studio.process.TaCoKitNode;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitElementParameter extends ElementParameter implements IAdditionalInfo {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final List<IValueChangedListener> valueChangeListeners = new ArrayList<>();

    /**
     * UPDATE_COMPONENTS {@link ElementParameter}
     * If it is set, this {@link TaCoKitElementParameter} sets it to {@code true}.
     * It will redraw UI after value is changed
     */
    private ElementParameter redrawParameter;

    private Map<String, Object> additionalInfoMap = new HashMap<>();

    private Optional<IProblemManager> problemManager = Optional.ofNullable(null);

    private Optional<Callable<Void>> registValidatorCallback = Optional.ofNullable(null);

    private PropertyNode propertyNode;

    /**
     * Form that own this tck element paramter.
     * actually : Main or Advanced.
     */
    private String form;

    public TaCoKitElementParameter() {
        this(null);
    }

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

    public Optional<IProblemManager> getProblemManager() {
        return problemManager;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setProblemManager(IProblemManager problemManager) {
        this.problemManager = Optional.ofNullable(problemManager);
    }

    public Optional<Callable<Void>> getRegistValidatorCallback() {
        return registValidatorCallback;
    }

    public void setRegistValidatorCallback(Callable<Void> registValidatorCallback) {
        this.registValidatorCallback = Optional.ofNullable(registValidatorCallback);
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

    void fireValueChange(final Object oldValue, final Object newValue) {
        for (final IValueChangedListener listener : valueChangeListeners) {
            listener.onValueChanged(this, oldValue, newValue);
        }
    }

    @Override
    public String getRepositoryValue() {
        String valueFromParentClass = super.getRepositoryValue();
        if (StringUtils.isNotBlank(valueFromParentClass)) {
            return valueFromParentClass;
        }
        String defaultRepositoryValue = getName();
        try {
            /**
             * It is better to don't use cache here, because it may have issue after metadata type is changed
             */
            if (propertyNode != null) {
                IElement element = this.getElement();
                if (element != null) {
                    IElementParameter metadataTypeIdParam = element.getElementParameter(TaCoKitNode.TACOKIT_METADATA_TYPE_ID);
                    if (metadataTypeIdParam != null) {
                        String metadataTypeId = (String) metadataTypeIdParam.getValue();
                        if (StringUtils.isNotBlank(metadataTypeId)) {
                            ConfigTypeNode configTypeNode = Lookups.taCoKitCache().getConfigTypeNode(metadataTypeId);
                            if (configTypeNode != null) {
                                PropertyNode propNode = TaCoKitUtil.getSamePropertyNode(propertyNode, configTypeNode);
                                if (propNode != null) {
                                    String prefix = propNode.getProperty().getPath();
                                    if (!defaultRepositoryValue.startsWith(prefix + ".")) { //$NON-NLS-1$
                                        defaultRepositoryValue = null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return defaultRepositoryValue;
    }

    public void redraw() {
        if (isRedrawable()) {
            redrawParameter.setValue(true);
        }
    }

    public PropertyNode getPropertyNode() {
        return propertyNode;
    }

    public void setPropertyNode(PropertyNode propertyNode) {
        this.propertyNode = propertyNode;
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
    public Object func(String funcName, Object... params) throws Exception {
        if (TaCoKitUtil.equals(funcName, "isPersisted")) { //$NON-NLS-1$
            return isPersisted();
        }
        return IAdditionalInfo.super.func(funcName, params);
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
     * Note, parameter change event is fired with value converted to String by calling {@link #getStringValue()} method
     * Subclasses should extend (override and call super.setValue()) this method to provide correct conversion, when
     * they use other value type than String.
     *
     * @param newValue value to be set
     */
    @Override
    public void setValue(final Object newValue) {
        final Object oldValue = super.getValue();
        super.setValue(newValue);
        firePropertyChange("value", oldValue, getStringValue());
        fireValueChange(oldValue, newValue);
    }

    public void updateValueOnly(final Object newValue) {
        super.setValue(newValue);
    }

    /**
     * Denotes whether parameter should be persisted in the repository.
     * Default (this) implementation returns {@code true}, however it can be overridden
     *
     * @return true
     */
    public boolean isPersisted() {
        return true;
    }

    @Override
    public boolean isSerialized() {
        if (isPersisted()) {
            return super.isSerialized();
        } else {
            // return true to skip serialization
            return true;
        }
    }

    /**
     * Creates IActionParameter
     *
     * @param actionParameter action parameter name
     * @return IActionParameter
     */
    public IActionParameter createActionParameter(final String actionParameter) {
        final IActionParameter parameter = new SettingsActionParameter(this, actionParameter);
        return parameter;
    }
}
