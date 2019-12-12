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
package org.talend.sdk.component.studio.ui.composite.controller;

import java.beans.PropertyChangeEvent;

import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public abstract class AbstractTaCoKitController extends AbstractElementPropertySectionController {

    public AbstractTaCoKitController(final IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public int estimateRowSize(final Composite subComposite, final IElementParameter param) {
        return 0;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // no-op
    }

    @Override
    public void refresh(final IElementParameter param, final boolean check) {
        // no-op
    }

}
