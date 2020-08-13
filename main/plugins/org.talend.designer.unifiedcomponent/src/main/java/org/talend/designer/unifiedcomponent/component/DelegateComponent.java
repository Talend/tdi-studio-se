// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.unifiedcomponent.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.settings.ComponentsSettingsHelper;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.unifiedcomponent.resources.ComponentIconLoading;

/**
 * created by wchen on Dec 1, 2017 Detailled comment
 *
 */
public class DelegateComponent extends AbstractBasicComponent {

    private String name;

    private String familyName;

    private Boolean visible;

    private IImage componentImage;

    private Set<UnifiedObject> unifiedObjects = new HashSet<UnifiedObject>();

    private String translatedFamilyName;

    public DelegateComponent(String familyName, String name) {
        this.name = name;
        this.familyName = familyName;
        getTranslatedFamilyName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getLongName()
     */
    @Override
    public String getLongName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getShortName()
     */
    @Override
    public String getShortName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getOriginalName()
     */
    @Override
    public String getOriginalName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getOriginalFamilyName()
     */
    @Override
    public String getOriginalFamilyName() {
        return familyName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getTranslatedFamilyName()
     */
    @Override
    public String getTranslatedFamilyName() {
        String[] transNames = familyName.split("/"); //$NON-NLS-1$
        IComponentsFactory factory = ComponentsFactoryProvider.getInstance();
        if (transNames != null && transNames.length > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (String toTranslate : transNames) {
                String translated = factory.getFamilyTranslation(this, "FAMILY." + toTranslate.replace(" ", "_"));
                if (translated.startsWith("!!")) { //$NON-NLS-1$
                    if (stringBuffer.length() > 0) {
                        stringBuffer.append("/");
                    }
                    stringBuffer.append(toTranslate);
                } else {
                    if (stringBuffer.length() > 0) {
                        stringBuffer.append("/");
                    }
                    stringBuffer.append(translated);
                }
            }
            translatedFamilyName = stringBuffer.toString();
        }
        return translatedFamilyName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getIcon32()
     */
    @Override
    public ImageDescriptor getIcon32() {
        if (!this.imageRegistry.containsKey(componentImage.getLocation() + componentImage.getPath() + "_icon32")) { //$NON-NLS-1$
            ComponentIconLoading cil = new ComponentIconLoading(imageRegistry, componentImage);
            cil.getImage32();
            cil.getImage16();
            cil.getImage24();
        }
        return this.imageRegistry.get(componentImage.getLocation() + componentImage.getPath() + "_icon32"); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getIcon24()
     */
    @Override
    public ImageDescriptor getIcon24() {
        if (!this.imageRegistry.containsKey(componentImage.getLocation() + componentImage.getPath() + "_icon24")) { //$NON-NLS-1$
            ComponentIconLoading cil = new ComponentIconLoading(imageRegistry, componentImage);

            // only call to initialize the icons in the registry
            cil.getImage32();
            cil.getImage16();
            cil.getImage24();
        }
        return this.imageRegistry.get(componentImage.getLocation() + componentImage.getPath() + "_icon24"); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getIcon16()
     */
    @Override
    public ImageDescriptor getIcon16() {
        if (!this.imageRegistry.containsKey(componentImage.getLocation() + componentImage.getPath() + "_icon16")) { //$NON-NLS-1$
            ComponentIconLoading cil = new ComponentIconLoading(imageRegistry, componentImage);

            // only call to initialize the icons in the registry
            cil.getImage32();
            cil.getImage16();
            cil.getImage24();
        }
        return this.imageRegistry.get(componentImage.getLocation() + componentImage.getPath() + "_icon16"); //$NON-NLS-1$

    }

    public List<ElementParameter> createElementParameters(INode node, boolean createBasicParams) {
        List<ElementParameter> listParam = new ArrayList<ElementParameter>();
        ElementParameter param = null;
        if (createBasicParams) {
            param = new ElementParameter(node);
            param.setName(EParameterName.UNIQUE_NAME.getName());
            param.setValue(""); //$NON-NLS-1$
            param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.ADVANCED);
            param.setNumRow(1);
            param.setReadOnly(true);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.COMPONENT_NAME.getName());
            param.setValue(getName());
            param.setDisplayName(EParameterName.COMPONENT_NAME.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(1);
            param.setReadOnly(true);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.VERSION.getName());
            param.setValue(""); //$NON-NLS-1$
            //$NON-NLS-1$ //$NON-NLS-2$
            param.setDisplayName(EParameterName.VERSION.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(2);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.FAMILY.getName());
            param.setValue(getOriginalFamilyName());
            param.setDisplayName(EParameterName.FAMILY.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(3);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.STARTABLE.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.SUBTREE_START.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.SUBTREE_START.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.END_OF_FLOW.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.END_OF_FLOW.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.ACTIVATE.getName());
            param.setValue(new Boolean(true));
            param.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(false);
            param.setRequired(false);
            param.setDefaultValue(param.getValue());
            param.setShow(true);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.DUMMY.getName());
            param.setValue(Boolean.FALSE);
            param.setDefaultValue(Boolean.FALSE);
            param.setDisplayName(EParameterName.DUMMY.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(false);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.HELP.getName());
            param.setValue("");
            param.setDisplayName(EParameterName.HELP.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(6);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);

            param = new ElementParameter(node);
            param.setName(EParameterName.UPDATE_COMPONENTS.getName());
            param.setValue(new Boolean(false));
            param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);
        }

        param = new ElementParameter(node);
        param.setName(EParameterName.UNIFIED_COMPONENTS.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIFIED_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.UNIFIED_COMPONENTS);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(0);
        param.setReadOnly(false);
        param.setShow(true);
        listParam.add(param);
        return listParam;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#createElementParameters(org.talend.core.model.process.INode)
     */
    @Override
    public List<ElementParameter> createElementParameters(INode node) {
        return createElementParameters(node, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#createReturns(org.talend.core.model.process.INode)
     */
    @Override
    public List<NodeReturn> createReturns(INode node) {
        List<NodeReturn> listReturn = new ArrayList<NodeReturn>();
        return listReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#createConnectors(org.talend.core.model.process.INode)
     */
    @Override
    public List<NodeConnector> createConnectors(INode node) {
        List<NodeConnector> listConnector = new ArrayList<NodeConnector>();
        for (int i = 0; i < EConnectionType.values().length; i++) {
            EConnectionType currentType = EConnectionType.values()[i];
            if ((currentType == EConnectionType.FLOW_REF) || (currentType == EConnectionType.FLOW_MERGE)) {
                continue;
            }
            NodeConnector nodeConnector = new NodeConnector(node);
            nodeConnector.setDefaultConnectionType(currentType);
            nodeConnector.setName(currentType.getName());
            nodeConnector.setBaseSchema(currentType.getName());
            nodeConnector.addConnectionProperty(currentType, currentType.getRGB(), currentType.getDefaultLineStyle());
            nodeConnector.setLinkName(currentType.getDefaultLinkName());
            nodeConnector.setMenuName(currentType.getDefaultMenuName());
            if ((currentType == EConnectionType.PARALLELIZE) || (currentType == EConnectionType.SYNCHRONIZE)) {
                nodeConnector.setMaxLinkInput(1);
            } else {
                nodeConnector.setMaxLinkInput(0);
            }
            nodeConnector.setMaxLinkOutput(0);
            nodeConnector.setMinLinkInput(0);
            nodeConnector.setMinLinkOutput(0);
            if (currentType == EConnectionType.FLOW_MAIN) {
                nodeConnector.addConnectionProperty(EConnectionType.FLOW_REF, EConnectionType.FLOW_REF.getRGB(),
                        EConnectionType.FLOW_REF.getDefaultLineStyle());
                nodeConnector.addConnectionProperty(EConnectionType.FLOW_MERGE, EConnectionType.FLOW_MERGE.getRGB(),
                        EConnectionType.FLOW_MERGE.getDefaultLineStyle());
            }
            listConnector.add(nodeConnector);
        }
        return listConnector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#hasConditionalOutputs()
     */
    @Override
    public boolean hasConditionalOutputs() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isMultiplyingOutputs()
     */
    @Override
    public boolean isMultiplyingOutputs() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getPluginExtension()
     */
    @Override
    public String getPluginExtension() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isSchemaAutoPropagated()
     */
    @Override
    public boolean isSchemaAutoPropagated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isDataAutoPropagated()
     */
    @Override
    public boolean isDataAutoPropagated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isHashComponent()
     */
    @Override
    public boolean isHashComponent() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#useMerge()
     */
    @Override
    public boolean useMerge() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    @Override
    public boolean useLookup() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getVersion()
     */
    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getMultipleComponentManagers()
     */
    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return super.getMultipleComponentManagers();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isLoaded()
     */
    @Override
    public boolean isLoaded() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isVisible()
     */
    @Override
    public boolean isVisible() {
        return isVisible(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isVisible(java.lang.String)
     */
    @Override
    public boolean isVisible(String family) {
        if (visible != null) {
            return visible;
        }
        return ComponentsSettingsHelper.isComponentVisible(this, family);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getModulesNeeded()
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getModulesNeeded(org.talend.core.model.process.INode)
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded(INode node) {
        return new ArrayList<ModuleNeeded>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getPathSource()
     */
    @Override
    public String getPathSource() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getAvailableCodeParts()
     */
    @Override
    public List<ECodePart> getAvailableCodeParts() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getPluginDependencies()
     */
    @Override
    public List<String> getPluginDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isMultipleOutput()
     */
    @Override
    public boolean isMultipleOutput() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    @Override
    public boolean useImport() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public EComponentType getComponentType() {
        return EComponentType.EMF;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    @Override
    public boolean isTechnical() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    @Override
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isMainCodeCalled()
     */
    @Override
    public boolean isMainCodeCalled() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#canParallelize()
     */
    @Override
    public boolean canParallelize() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getCombine()
     */
    @Override
    public String getCombine() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getProcess()
     */
    @Override
    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getRepositoryType()
     */
    @Override
    public String getRepositoryType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isLog4JEnabled()
     */
    @Override
    public boolean isLog4JEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isReduce()
     */
    @Override
    public boolean isReduce() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isSparkAction()
     */
    @Override
    public boolean isSparkAction() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isSupportDbType()
     */
    @Override
    public boolean isSupportDbType() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isAllowedPropagated()
     */
    @Override
    public boolean isAllowedPropagated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getCONNECTORList()
     */
    @Override
    public EList getCONNECTORList() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Sets the componentImage.
     *
     * @param componentImage the componentImage to set
     */
    public void setComponentImage(IImage componentImage) {
        this.componentImage = componentImage;
    }

    /**
     * Getter for componentImage.
     *
     * @return the componentImage
     */
    public IImage getComponentImage() {
        return this.componentImage;
    }

    /**
     * Getter for unifiedObjects.
     *
     * @return the unifiedObjects
     */
    public Set<UnifiedObject> getUnifiedObjects() {
        return this.unifiedObjects;
    }

    public Set<UnifiedObject> getUnifiedObjectsByPalette(String paletteType) {
        Set<UnifiedObject> supportedObjects = new HashSet<UnifiedObject>();
        Set<UnifiedObject> unifiedObjects = getUnifiedObjects();
        for (UnifiedObject obj : unifiedObjects) {
            if (obj.getSupportedCategories().contains(paletteType)) {
                supportedObjects.add(obj);
            }
        }

        return supportedObjects;
    }

    public UnifiedObject getUnifiedObjectByName(String componentName) {
        for (UnifiedObject obj : getUnifiedObjects()) {
            if (obj.getDisplayComponent().equals(componentName)) {
                return obj;
            }
        }
        return null;
    }

    public UnifiedObject getUnifiedObjectByDatabase(String database) {
        for (UnifiedObject obj : getUnifiedObjects()) {
            if (obj.getDatabase().equals(database)) {
                return obj;
            }
        }
        return null;
    }

}
