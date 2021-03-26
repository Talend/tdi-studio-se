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
package org.talend.sdk.component.studio;

import java.util.HashMap;
import java.util.Map;

import org.talend.sdk.component.server.front.model.ComponentDetail;

public class VirtualComponentRegister {

    private static final VirtualComponentRegister instance = new VirtualComponentRegister();

    private static final Map<String, VirtualComponentModel> VIRTUAL_COMPONENT_NAME_CACHE = new HashMap<String, VirtualComponentModel>();

    private static final Map<String, VirtualComponentModel> VIRTUAL_COMPONENT_ID_CACHE = new HashMap<String, VirtualComponentModel>();

    private VirtualComponentRegister() {
    }

    public static VirtualComponentRegister getInstance() {
        return instance;
    }

    public void registeVirtualComponent(VirtualComponentModel component) {
        VIRTUAL_COMPONENT_NAME_CACHE.put(component.getName(), component);
        VIRTUAL_COMPONENT_ID_CACHE.put(component.getComponentId(), component);
    }

    public boolean isVirtualComponentName(String componentName) {
        if (VIRTUAL_COMPONENT_NAME_CACHE.containsKey(componentName)) {
            return true;
        }
        return false;
    }

    public boolean isVirtualComponentId(String componentId) {
        if (VIRTUAL_COMPONENT_ID_CACHE.containsKey(componentId)) {
            return true;
        }
        return false;
    }

    public ComponentDetail getComponentDetailByName(String componentName) {
        if (VIRTUAL_COMPONENT_NAME_CACHE.containsKey(componentName)) {
            return VIRTUAL_COMPONENT_NAME_CACHE.get(componentName).getDetail();
        }
        return null;
    }

    public ComponentDetail getComponentDetailById(String componentId) {
        if (VIRTUAL_COMPONENT_NAME_CACHE.containsKey(componentId)) {
            return VIRTUAL_COMPONENT_ID_CACHE.get(componentId).getDetail();
        }
        return null;
    }
}
