// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.model.parameter.update;

import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class UpdateListener implements PropertyChangeListener {

    /**
     * ElementParameter, which value should be updated
     */
    private final TaCoKitElementParameter updatableParameter;

    private final UpdateStrategy updateStrategy;

    public UpdateListener(final TaCoKitElementParameter updatableParameter, final UpdateStrategy updateStrategy) {
        this.updatableParameter = updatableParameter;
        this.updateStrategy = updateStrategy;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if(!"value".equals(event.getPropertyName())){
            return;
        }
        final Object newValue = event.getNewValue();
        final Object updatedValue = updateStrategy.computeValue(newValue);
        updatableParameter.setValue(updatedValue);
        updatableParameter.redraw();
        updatableParameter.firePropertyChange("show", null, true);

    }
}
