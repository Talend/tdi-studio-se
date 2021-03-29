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

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.model.parameter.ElementParameterCreator;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class VirtualComponentModel extends ComponentModel {

    private VirtualComponentModelType modelType;

    public VirtualComponentModel(ComponentIndex index, ComponentDetail detail, ConfigTypeNodes configTypeNodes,
            ImageDescriptor image32, String reportPath, boolean isCatcherAvailable, VirtualComponentModelType modelType) {
        super(index, detail, configTypeNodes, image32, reportPath, isCatcherAvailable);
        this.modelType = modelType;
    }

    @Override
    public String getName() {
        if (isMadeByTalend()) {
            return "t" + TaCoKitUtil.getFullComponentName(index.getId().getFamily(), modelType.getDisplayName());
        }
        return TaCoKitUtil.getFullComponentName(index.getId().getFamily(), modelType.getDisplayName());
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    /**
     * Returns long component name, aka title (e.g. "Salesforce Input"). It is i18n title. In v0 component it is
     * specified by "component.{compName}.title" message key
     *
     * @return long component name, aka title (e.g. "") or translated
     */
    @Override
    public String getLongName() {
        return getName();
    }

    public List<? extends IElementParameter> createElementParameters(final INode node) {
        if (isNeedMigration() && node.getProcess() != null) {
            ProcessItem processItem = ItemCacheManager.getProcessItem(node.getProcess().getId());
            if (processItem != null) {
                manager.checkNodeMigration(processItem, getName());
            }
        }
        TaCoKitCache cache = Lookups.taCoKitCache();
        ConfigTypeNode configTypeNode = cache.findConfigTypeNodeById(detail.getId().getFamily(), "datastore");
        ElementParameterCreator creator = new ElementParameterCreator(this, detail,
                configTypeNode == null ? null : configTypeNode.getProperties(), node, reportPath, isCatcherAvailable);
        List<IElementParameter> parameters = (List<IElementParameter>) creator.createParameters();
        //connection and close runtime need plugin name to fetch plugin object in runtime
        parameters.add(createPluginNameParameter(node));
        return parameters;
    }
    
    /**
     * Create {@link TaCoKitConst#TACOKIT_COMPONENT_PLUGIN_NAME} parameter. This parameter is used during code generation to know
     * which component runtime to use
     */
    private ElementParameter createPluginNameParameter(final INode node) {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(TaCoKitConst.TACOKIT_COMPONENT_PLUGIN_NAME);
        parameter.setValue(detail.getId().getPlugin());
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        return parameter;
    }
    
    public boolean isShowPropertyParameter() {
        if (modelType == VirtualComponentModelType.CLOSE) {
            return false;
        }   
        return true;
    }

    @Override
    public String getTemplateFolder() {
        return "tacokit/jet_stub/generic/" + getForder();
    }

    private String getForder() {
        switch (modelType) {
        case CONNECTION:
            return "connection";
        case CLOSE:
            return "close";
        default:
            return null;
        }
    }

    public VirtualComponentModelType getModelType() {
        return modelType;
    }

    @Override
    public String getTemplateNamePrefix() {
        return getForder();
    }

    public String getComponentName() {
        return getName();
    }

    public String getComponentId() {
        return detail.getId().getId() + this.modelType.getDisplayName();
    }
    
    public static String getDefaultConnectionName(ComponentIndex index) {
        if (TaCoKitUtil.isTaCoKitComponentMadeByTalend(index)) {
            return "t" + TaCoKitUtil.getFullComponentName(index.getId().getFamily(), VirtualComponentModelType.CONNECTION.getDisplayName());
        }
        return TaCoKitUtil.getFullComponentName(index.getId().getFamily(), VirtualComponentModelType.CONNECTION.getDisplayName());
    }

    public enum VirtualComponentModelType {
        CONNECTION("Connection"),
        CLOSE("Close");

        String displayName;

        VirtualComponentModelType(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Getter for displayName.
         *
         * @return the displayName
         */
        public String getDisplayName() {
            return this.displayName;
        }

    }
}
