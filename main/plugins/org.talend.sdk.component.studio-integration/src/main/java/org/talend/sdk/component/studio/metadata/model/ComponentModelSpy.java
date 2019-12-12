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
package org.talend.sdk.component.studio.metadata.model;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.AbstractLayerComponent;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class ComponentModelSpy extends AbstractLayerComponent {

    private IComponent component;

    private String name;

    private String originalName;

    private String shortName;

    private String longName;

    private String originalFamilyName;

    private String translatedFamilyName;

    private String templateFolder;

    private String templateNamePrefix;

    private List<ECodePart> codePartListX;

    private List<? extends IElementParameter> elementParameters;

    public ComponentModelSpy(final IComponent component) {
        this.component = component;
    }

    public void setComponent(final IComponent component) {
        this.component = component;
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        } else {
            return component.getName();
        }
    }

    public void spyName(final String name) {
        this.name = name;
    }

    @Override
    public String getOriginalName() {
        if (originalName != null) {
            return originalName;
        } else {
            return component.getOriginalName();
        }
    }

    public void spyOriginalName(final String originalName) {
        this.originalName = originalName;
    }

    @Override
    public String getLongName() {
        if (longName != null) {
            return longName;
        } else {
            return component.getLongName();
        }
    }

    @Override
    public String getOriginalFamilyName() {
        if (originalFamilyName != null) {
            return originalFamilyName;
        } else {
            return component.getOriginalFamilyName();
        }
    }

    @Override
    public String getTranslatedFamilyName() {
        if (translatedFamilyName != null) {
            return translatedFamilyName;
        } else {
            return component.getTranslatedFamilyName();
        }
    }

    @Override
    public void setImageRegistry(final Map<String, ImageDescriptor> imageRegistry) {
        throwActionNotAllow();
    }

    @Override
    public Map<String, ImageDescriptor> getImageRegistry() {
        return component.getImageRegistry();
    }

    @Override
    public ImageDescriptor getIcon32() {
        return component.getIcon32();
    }

    @Override
    public ImageDescriptor getIcon24() {
        return component.getIcon24();
    }

    @Override
    public ImageDescriptor getIcon16() {
        return component.getIcon16();
    }

    @Override
    public List<? extends IElementParameter> createElementParameters(final INode node) {
        if (elementParameters != null) {
            return elementParameters;
        } else {
            return component.createElementParameters(node);
        }
    }

    public void spyElementParameters(final List<? extends IElementParameter> elementParameters) {
        this.elementParameters = elementParameters;
    }

    @Override
    public List<? extends INodeReturn> createReturns(final INode node) {
        return component.createReturns(node);
    }

    @Override
    public List<? extends INodeConnector> createConnectors(final INode node) {
        return component.createConnectors(node);
    }

    @Override
    public boolean hasConditionalOutputs() {
        return component.hasConditionalOutputs();
    }

    @Override
    public boolean isMultiplyingOutputs() {
        return component.isMultiplyingOutputs();
    }

    @Override
    public String getPluginExtension() {
        return component.getPluginExtension();
    }

    @Override
    public boolean isSchemaAutoPropagated() {
        return component.isSchemaAutoPropagated();
    }

    @Override
    public boolean isDataAutoPropagated() {
        return component.isDataAutoPropagated();
    }

    @Override
    public boolean isHashComponent() {
        return component.isHashComponent();
    }

    @Override
    public boolean useMerge() {
        return component.useMerge();
    }

    @Override
    public boolean useLookup() {
        return component.useLookup();
    }

    @Override
    public String getVersion() {
        return component.getVersion();
    }

    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return component.getMultipleComponentManagers();
    }

    @Override
    public boolean isLoaded() {
        return component.isLoaded();
    }

    @Override
    public boolean isVisible() {
        return component.isVisible();
    }

    @Override
    public boolean isVisible(final String family) {
        return component.isVisible(family);
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        return component.getModulesNeeded();
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded(final INode node) {
        return component.getModulesNeeded(node);
    }

    @Override
    public String getPathSource() {
        return component.getPathSource();
    }

    @Override
    public List<ECodePart> getAvailableCodeParts() {
        if (this.codePartListX != null) {
            return this.codePartListX;
        } else {
            return component.getAvailableCodeParts();
        }
    }

    public void spyAvailableCodeParts(final List<ECodePart> codeParts) {
        this.codePartListX = codeParts;
    }

    @Override
    public List<String> getPluginDependencies() {
        return component.getPluginDependencies();
    }

    @Override
    public boolean isMultipleOutput() {
        return component.isMultipleOutput();
    }

    @Override
    public boolean useImport() {
        return component.useImport();
    }

    @Override
    public EComponentType getComponentType() {
        return component.getComponentType();
    }

    @Override
    public boolean isTechnical() {
        return component.isTechnical();
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return component.isVisibleInComponentDefinition();
    }

    @Override
    public boolean isSingleton() {
        return component.isSingleton();
    }

    @Override
    public boolean isMainCodeCalled() {
        return component.isMainCodeCalled();
    }

    @Override
    public boolean canParallelize() {
        return component.canParallelize();
    }

    @Override
    public String getShortName() {
        if (shortName != null) {
            return shortName;
        } else {
            return component.getShortName();
        }
    }

    public void spyShortName(final String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getCombine() {
        return component.getCombine();
    }

    @Override
    public IProcess getProcess() {
        return component.getProcess();
    }

    @Override
    public String getPaletteType() {
        return component.getPaletteType();
    }

    @Override
    public void setPaletteType(final String paletteType) {
        throwActionNotAllow();
    }

    @Override
    public String getRepositoryType() {
        return component.getRepositoryType();
    }

    @Override
    public boolean isLog4JEnabled() {
        return component.isLog4JEnabled();
    }

    @Override
    public EList getCONNECTORList() {
        return component.getCONNECTORList();
    }

    @Override
    public String getType() {
        return component.getType();
    }

    @Override
    public boolean isReduce() {
        return component.isReduce();
    }

    @Override
    public String getInputType() {
        return component.getInputType();
    }

    @Override
    public String getOutputType() {
        return component.getOutputType();
    }

    @Override
    public boolean isSparkAction() {
        return component.isSparkAction();
    }

    @Override
    public String getPartitioning() {
        return component.getPartitioning();
    }

    @Override
    public boolean isSupportDbType() {
        return component.isSupportDbType();
    }

    @Override
    public boolean isAllowedPropagated() {
        return component.isAllowedPropagated();
    }

    @Override
    public String getTemplateFolder() {
        if (templateFolder != null) {
            return templateFolder;
        } else {
            return component.getTemplateFolder();
        }
    }

    public void spyTemplateFolder(final String templateFolder) {
        this.templateFolder = templateFolder;
    }

    @Override
    public String getTemplateNamePrefix() {
        if (templateNamePrefix != null) {
            return templateNamePrefix;
        } else {
            return component.getTemplateNamePrefix();
        }
    }

    public void spyTemplateNamePrefix(final String templateNamePrefix) {
        this.templateNamePrefix = templateNamePrefix;
    }

    private void throwActionNotAllow() throws RuntimeException {
        throw new RuntimeException("Action is not allowed by proxy component");
    }

    @Override
    public void setOriginalFamilyName(final String familyName) {
        component.setOriginalFamilyName(familyName);
    }

    @Override
    public void setTranslatedFamilyName(final String translatedFamilyName) {
        component.setTranslatedFamilyName(translatedFamilyName);
    }

}
