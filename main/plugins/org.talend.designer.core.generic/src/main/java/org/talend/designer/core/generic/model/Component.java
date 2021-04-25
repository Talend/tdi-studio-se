// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.resource.BundleFileUtil;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.component.VirtualComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.runtime.util.ComponentReturnVariableUtils;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.settings.ComponentsSettingsHelper;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.SchemaProperty;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.designer.core.CheckLogManamger;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.context.ComponentContextPropertyValueEvaluator;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.ElementParameterDefaultValue;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.librariesmanager.model.ExtensionModuleManager;

/**
 * created by hcyi on Sep 10, 2015 Detailled comment
 *
 */
public class Component extends AbstractBasicComponent {

    private static Logger log = Logger.getLogger(Component.class);

    private ComponentDefinition componentDefinition;

    private ComponentsProvider provider;

    private Map<String, String> translatedMap = new HashMap<>();

    private static String ERROR_MESSAGE = "ERROR_MESSAGE"; //$NON-NLS-1$

    public Component(ComponentDefinition componentDefinition) throws BusinessException {
        this(componentDefinition, ComponentCategory.CATEGORY_4_DI.getName());
    }

    public Component(ComponentDefinition componentDefinition, String paletteType) throws BusinessException {
        this.componentDefinition = componentDefinition;
        this.setPaletteType(paletteType);
    }

    public ComponentDefinition getComponentDefinition() {
        return componentDefinition;
    }

    @Override
    public String getName() {
        return componentDefinition.getName();
    }

    @Override
    public String getLongName() {
        return componentDefinition.getTitle();
    }

    @Override
    public String getOriginalFamilyName() {
        if (familyName != null) {
            return familyName;
        }
        String[] families = componentDefinition.getFamilies();
        StringBuffer sb = new StringBuffer();
        for (String familyName : families) {
            if (sb.length() > 0) {
                sb.append("|");//$NON-NLS-1$
            }
            sb.append(familyName);
        }
        return sb.toString();
    }

    @Override
    public String getTranslatedFamilyName() {
        // TCOMP-93 , Need translated name
        return getOriginalFamilyName();
    }

    private String getTranslatedValue(final String nameValue) {
        String returnValue = nameValue;
        if (translatedMap.containsKey(nameValue)) {
            return translatedMap.get(nameValue);
        }
        return returnValue;
    }

    @Override
    public List<ElementParameter> createElementParameters(INode node) {
        if (node.getComponentProperties() == null) {
            node.setComponentProperties(ComponentsUtils.getComponentProperties(componentDefinition.getName()));
        }
        List<ElementParameter> listParam = new ArrayList<>();
        addMainParameters(listParam, node);
        node.setElementParameters(listParam); // initialize the parameters to setup the querystore, since it's needed
                                              // for the jdbc components
        addPropertyParameters(listParam, node, Form.MAIN, EComponentCategory.BASIC);
        addPropertyParameters(listParam, node, Form.ADVANCED, EComponentCategory.ADVANCED);
        initializeParametersForSchema(listParam, node);
        addViewParameters(listParam, node);
        addDocParameters(listParam, node);
        addValidationRulesParameters(listParam, node);
        return listParam;
    }

    @Override
    public List<NodeReturn> createReturns(INode parentNode) {
        List<NodeReturn> listReturn = new ArrayList<>();

        ComponentProperties componentProperties = parentNode.getComponentProperties();
        if (componentProperties == null) {
            parentNode.setComponentProperties(ComponentsUtils.getComponentProperties(componentDefinition.getName()));
            componentProperties = parentNode.getComponentProperties();
        }
        if (!(componentProperties instanceof ComponentPropertiesImpl)) {
            return listReturn;
        }
        NodeReturn nodeRet = new NodeReturn();
        nodeRet.setType(JavaTypesManager.STRING.getLabel());
        nodeRet.setDisplayName(ComponentReturnVariableUtils.getTranslationForVariable(ComponentDefinition.RETURN_ERROR_MESSAGE,
                ComponentDefinition.RETURN_ERROR_MESSAGE));
        nodeRet.setName(ComponentReturnVariableUtils.getStudioNameFromVariable(ComponentDefinition.RETURN_ERROR_MESSAGE));
        nodeRet.setAvailability("AFTER"); //$NON-NLS-1$
        listReturn.add(nodeRet);

        for (Property<?> child : componentDefinition.getReturnProperties()) {
            nodeRet = new NodeReturn();
            nodeRet.setType(ComponentsUtils.getTalendTypeFromProperty(child).getId());
            nodeRet.setDisplayName(
                    ComponentReturnVariableUtils.getTranslationForVariable(child.getName(), child.getDisplayName()));
            nodeRet.setName(ComponentReturnVariableUtils.getStudioNameFromVariable(child.getName()));
            if (nodeRet.getName().equals(ERROR_MESSAGE)) {
                continue;
            }
            Object object = child.getTaggedValue(IGenericConstants.AVAILABILITY);
            if (object != null) {
                nodeRet.setAvailability(object.toString());
            } else {
                nodeRet.setAvailability("AFTER"); //$NON-NLS-1$
            }
            listReturn.add(nodeRet);
        }
        return listReturn;
    }

    protected void addDocParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param = new ElementParameter(node);
        param.setName(EParameterName.INFORMATION.getName());
        param.setValue(new Boolean(false));
        param.setDisplayName(EParameterName.INFORMATION.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.DOC);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.COMMENT.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.COMMENT.getDisplayName());
        param.setFieldType(EParameterFieldType.MEMO);
        param.setNbLines(10);
        param.setCategory(EComponentCategory.DOC);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        listParam.add(param);
    }

    protected void addValidationRulesParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;

        param = new ElementParameter(node);
        param.setName(EParameterName.VALIDATION_RULES.getName());
        param.setValue(new Boolean(false));
        param.setDisplayName(EParameterName.VALIDATION_RULES.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.VALIDATION_RULES);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setDefaultValue(param.getValue());
        param.setShow(true);
        listParam.add(param);
        listParam.add(addValidationRuleType(node, 3));
    }

    private ElementParameter addValidationRuleType(INode node, int rowNb) {
        ElementParameter parentParam = new ElementParameter(node);
        parentParam.setName(EParameterName.VALIDATION_RULE_TYPE.getName());
        parentParam.setDisplayName(EParameterName.VALIDATION_RULE_TYPE.getDisplayName());
        parentParam.setFieldType(EParameterFieldType.VALIDATION_RULE_TYPE);
        parentParam.setCategory(EComponentCategory.VALIDATION_RULES);
        parentParam.setNumRow(rowNb);
        parentParam.setReadOnly(false);
        parentParam.setShow(true);
        parentParam.setShowIf(EParameterName.VALIDATION_RULES.getName() + " == 'true'"); //$NON-NLS-1$
        parentParam.setContext(IGenericConstants.CONTEXT);
        parentParam.setRepositoryValue(ERepositoryCategoryType.VALIDATIONRULES.getName());
        parentParam.setValue("");

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.VALIDATION_RULES);
        newParam.setName(EParameterName.VALIDATION_RULE_TYPE.getName());
        newParam.setDisplayName(EParameterName.VALIDATION_RULE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
        newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
        newParam.setValue(BUILTIN);
        newParam.setNumRow(rowNb);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setShow(true);
        newParam.setShowIf(parentParam.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        newParam.setReadOnly(false);
        newParam.setContext(IGenericConstants.CONTEXT);
        newParam.setParentParameter(parentParam);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.VALIDATION_RULES);
        newParam.setName(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(rowNb);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setContext(IGenericConstants.CONTEXT);
        newParam.setParentParameter(parentParam);
        return parentParam;
    }

    private String getNodeFormatIdWithoutFormatType(String nodeLabel, String nodeFamily) {
        String ids = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
            IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                    .getService(IComponentsLocalProviderService.class);
            if (service != null) {
                ids = service.getPreferenceStore().getString(IComponentsLocalProviderService.FORMAT_IDS);
            }
        }
        String[] idArray = null;
        if (!StringUtils.isBlank(ids)) {
            idArray = ids.split(IComponentsLocalProviderService.IDS_SEPARATOR);

            String label = nodeLabel + IComponentsLocalProviderService.PALETTE_ENTRY_TYPE;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return nodeLabel + IComponentsLocalProviderService.PALETTE_ENTRY_TYPE;
                }
            }

            label = nodeFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return nodeFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_HINT;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_HINT;
                }
            }

            label = IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION;
            for (String id : idArray) {
                if (id.contains(label)) {
                    return IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION;
                }
            }

            if (nodeFamily.contains("/")) { //$NON-NLS-1$
                String rootFamily = nodeFamily.split("/")[0]; //$NON-NLS-1$
                label = rootFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                for (String id : idArray) {
                    if (id.contains(label)) {
                        return rootFamily + IComponentsLocalProviderService.PALETTE_CONTAINER_TYPE;
                    }
                }
            }
        }
        return null;
    }

    public void addViewParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;
        IPreferenceStore localComponentProviderStore = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
            IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister.getDefault()
                    .getService(IComponentsLocalProviderService.class);
            if (service != null) {
                localComponentProviderStore = service.getPreferenceStore();
            }
        }
        String formatId = getNodeFormatIdWithoutFormatType(getName(), getTranslatedFamilyName());
        param = new ElementParameter(node);
        param.setName(EParameterName.LABEL.getName());
        param.setDisplayName(EParameterName.LABEL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_LABEL);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        } else {
            // in case label/format is not set in the preferences.
            String label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_LABEL);
            if (!"".equals(label)) { //$NON-NLS-1$
                param.setValue(label);
            }
        }
        param.setDefaultValue(param.getValue());

        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.HINT.getName());
        param.setDisplayName(EParameterName.HINT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_HINT);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        } else {
            // in case hint/format is not set in the preferences.
            String label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_HINT);
            if (!"".equals(label)) { //$NON-NLS-1$
                param.setValue(label);
            }
        }
        param.setDefaultValue(param.getValue());

        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.CONNECTION_FORMAT.getName());
        param.setDisplayName(EParameterName.CONNECTION_FORMAT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.VIEW);
        param.setNumRow(3);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        if (formatId != null) {
            if (localComponentProviderStore != null) {
                String label = localComponentProviderStore.getString(IComponentsLocalProviderService.PREFERENCE_TYPE_CONNECTION);
                if (!"".equals(label)) { //$NON-NLS-1$
                    param.setValue(label);
                }
            }
        }
        param.setDefaultValue(param.getValue());
        listParam.add(param);
    }

    public void addMainParameters(final List<ElementParameter> listParam, INode node) {
        ElementParameter param;
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
        // TUP-4142
        if (canStart()) {
            param = new ElementParameter(node);
            param.setName(EParameterName.START.getName());
            param.setValue(new Boolean(false));
            param.setDisplayName(EParameterName.START.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setNumRow(5);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            listParam.add(param);
        }
        // TUP-4142
        param = new ElementParameter(node);
        param.setName(EParameterName.STARTABLE.getName());
        param.setValue(new Boolean(canStart()));
        param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);
        // TUP-4142
        param = new ElementParameter(node);
        param.setName(EParameterName.SUBTREE_START.getName());
        param.setValue(new Boolean(canStart()));
        param.setDisplayName(EParameterName.SUBTREE_START.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);
        // TUP-4142
        param = new ElementParameter(node);
        param.setName(EParameterName.END_OF_FLOW.getName());
        param.setValue(new Boolean(canStart()));
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

        // TUP-4143
        param = new ElementParameter(node);
        param.setName(EParameterName.HELP.getName());
        param.setValue(getTranslatedValue(PROP_HELP));
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

        //
        param = new ElementParameter(node);
        param.setName(EParameterName.IREPORT_PATH.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setDisplayName(EParameterName.IREPORT_PATH.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(CorePlugin.getDefault().getPluginPreferences().getString(ITalendCorePrefConstants.IREPORT_PATH));
        param.setReadOnly(true);
        listParam.add(param);

        ComponentWizardDefinition wizardDefinition = getWizardDefinition(node.getComponentProperties());
        param = new ElementParameter(node);
        param.setName("PROPERTY");//$NON-NLS-1$
        param.setCategory(EComponentCategory.BASIC);
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        if (wizardDefinition != null) {
            if (isExtraType(wizardDefinition.getName())) {
                param.setRepositoryValue("DATABASE:" + wizardDefinition.getName());
            } else {
                param.setRepositoryValue(wizardDefinition.getName());
            }
        }
        param.setValue("");//$NON-NLS-1$
        param.setNumRow(1);
        param.setShow(wizardDefinition != null);
        param.setTaggedValue(IGenericConstants.IS_PROPERTY_SHOW, wizardDefinition != null);

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
        newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
        newParam.setValue(BUILTIN);
        newParam.setNumRow(param.getNumRow());
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setShow(false);
        newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        newParam.setReadOnly(param.isReadOnly());
        newParam.setNotShowIf(param.getNotShowIf());
        newParam.setContext(IGenericConstants.CONTEXT);
        newParam.setSerialized(true);
        newParam.setParentParameter(param);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(param.getNumRow());
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setReadOnly(param.isReadOnly());
        newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$//$NON-NLS-2$
        newParam.setNotShowIf(param.getNotShowIf());
        newParam.setContext(IGenericConstants.CONTEXT);
        newParam.setSerialized(true);
        newParam.setParentParameter(param);
        listParam.add(param);

        ElementParameter sibling_param = new ElementParameter(node);
        sibling_param.setName("QUERYSTORE");
        sibling_param.setCategory(EComponentCategory.BASIC);
        sibling_param.setDisplayName(EParameterName.QUERYSTORE_TYPE.getDisplayName());
        sibling_param.setFieldType(EParameterFieldType.QUERYSTORE_TYPE);
        sibling_param.setNumRow(0);
        sibling_param.setShow(false);
        sibling_param.setValue("");
        listParam.add(sibling_param);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.QUERYSTORE_TYPE.getName());
        newParam.setDisplayName(EParameterName.QUERYSTORE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        newParam.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        newParam.setValue(EmfComponent.BUILTIN);
        newParam.setNumRow(0);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setParentParameter(sibling_param);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(0);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue("");
        newParam.setRequired(true);
        newParam.setParentParameter(sibling_param);
        newParam.setShow(false);

        if (ComponentCategory.CATEGORY_4_DI.getName().equals(this.getPaletteType())) {
            boolean isStatCatcherComponent = false;
            /* for bug 0021961,should not show parameter TSTATCATCHER_STATS in UI on component tStatCatcher */
            if (!isStatCatcherComponent) {
                boolean tStatCatcherAvailable = ComponentsFactoryProvider.getInstance().get(EmfComponent.TSTATCATCHER_NAME,
                        ComponentCategory.CATEGORY_4_DI.getName()) != null;
                param = new ElementParameter(node);
                param.setName(EParameterName.TSTATCATCHER_STATS.getName());
                param.setValue(Boolean.FALSE);
                param.setDisplayName(EParameterName.TSTATCATCHER_STATS.getDisplayName());
                param.setFieldType(EParameterFieldType.CHECK);
                param.setCategory(EComponentCategory.ADVANCED);
                param.setNumRow(199);
                param.setReadOnly(false);
                param.setRequired(false);
                param.setDefaultValue(param.getValue());
                param.setShow(tStatCatcherAvailable);
                listParam.add(param);
            }
        }

        // These parameters is only work when TIS is loaded
        // GLiu Added for Task http://jira.talendforge.org/browse/TESB-4279
        if (PluginChecker.isTeamEdition() && !ComponentCategory.CATEGORY_4_CAMEL.getName().equals(getPaletteType())) {
            boolean defaultParalelize = componentDefinition.isParallelize();
            param = new ElementParameter(node);
            param.setReadOnly(!defaultParalelize);
            param.setName(EParameterName.PARALLELIZE.getName());
            param.setValue(Boolean.FALSE);
            param.setDisplayName(EParameterName.PARALLELIZE.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.ADVANCED);
            param.setNumRow(200);
            param.setShow(true);
            param.setDefaultValue(param.getValue());
            listParam.add(param);

            param = new ElementParameter(node);
            param.setReadOnly(!defaultParalelize);
            param.setName(EParameterName.PARALLELIZE_NUMBER.getName());
            param.setValue("2");
            param.setDisplayName(EParameterName.PARALLELIZE_NUMBER.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.ADVANCED);
            param.setNumRow(201);
            param.setShowIf(EParameterName.PARALLELIZE.getName() + " == 'true'"); //$NON-NLS-1$
            param.setShow(true);
            param.setDefaultValue(param.getValue());
            listParam.add(param);

            param = new ElementParameter(node);
            param.setReadOnly(!defaultParalelize);
            param.setName(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
            param.setValue(Boolean.FALSE);
            param.setDisplayName(EParameterName.PARALLELIZE_KEEP_EMPTY.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.ADVANCED);
            param.setNumRow(202);
            param.setShow(false);
            param.setDefaultValue(param.getValue());
            listParam.add(param);
        }
    }

    private boolean isExtraType(String definame) {
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService != null) {
            for (ERepositoryObjectType type : dbService.getExtraTypes()) {
                if (type.getLabel().equals(definame)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ComponentWizardDefinition getWizardDefinition(ComponentProperties componentProperties) {
        ComponentWizardDefinition definition = null;
        if (componentProperties == null) {
            return null;
        }
        ComponentService service = ComponentsUtils.getComponentService();
        List<ComponentWizard> componentWizards = service.getComponentWizardsForProperties(componentProperties, null);
        for (ComponentWizard componentWizard : componentWizards) {
            definition = componentWizard.getDefinition();
            // Can we ensure it is the same wizard with metadata connection wizard by this way?
            if (definition.isTopLevel()) {
                return definition;
            }
        }
        List<NamedThing> namedThings = componentProperties.getProperties();
        for (NamedThing namedThing : namedThings) {
            if (namedThing instanceof ComponentProperties) {
                definition = getWizardDefinition((ComponentProperties) namedThing);
                if (definition != null && definition.isTopLevel()) {
                    return definition;
                }
            }
        }
        return null;
    }

    protected void addPropertyParameters(final List<ElementParameter> listParam, final INode node, String formName,
            EComponentCategory category) {
        ComponentProperties props = node.getComponentProperties();
        Form form = props.getForm(formName);
        List<ElementParameter> parameters = ComponentsUtils.getParametersFromForm(node, this.isInitializing(), category, props,
                form);
        props.setValueEvaluator(new ComponentContextPropertyValueEvaluator(node));
        ComponentService componentService = ComponentsUtils.getComponentService();
        for (ElementParameter parameter : parameters) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                genericElementParameter.setComponentService(componentService);
            }
        }
        listParam.addAll(parameters);
    }

    /**
     * Sometimes the property parameters of schema are base on other parameters,but they might be initialized after the
     * schema. So there need to initialize the schema's again.
     *
     */
    protected void initializeParametersForSchema(List<ElementParameter> listParam, final INode node) {
        ComponentProperties rootProperty = node.getComponentProperties();
        Map<String, SchemaProperty> listSchemaProperties = new HashMap<>();
        findSchemaProperties(rootProperty, listParam, listSchemaProperties, null);
        for (String paramName : listSchemaProperties.keySet()) {
            boolean found = setupConnector(node, listParam, paramName, listSchemaProperties.get(paramName), true);
            if (!found) {
                // check in the input schema
                // for now we only handle input schema named MAIN. But we will name them "FLOW" to keep
                // compatibility.
                setupConnector(node, listParam, paramName, listSchemaProperties.get(paramName), false);
            }
        }

        ElementParameter autoSwitchParam = null;
        for (ElementParameter param : listParam) { // TUP-4161
            if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())) {
                ElementParameter newParam = new ElementParameter(node);
                newParam.setCategory(EComponentCategory.BASIC);
                newParam.setName(EParameterName.SCHEMA_TYPE.getName());
                newParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
                newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
                newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
                newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });
                newParam.setValue(BUILTIN);
                newParam.setNumRow(param.getNumRow());
                newParam.setFieldType(EParameterFieldType.TECHNICAL);
                newParam.setShow(false);
                newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                newParam.setReadOnly(param.isReadOnly());
                newParam.setNotShowIf(param.getNotShowIf());
                newParam.setContext(IGenericConstants.CONTEXT);
                newParam.setSerialized(true);
                newParam.setParentParameter(param);

                newParam = new ElementParameter(node);
                newParam.setCategory(EComponentCategory.BASIC);
                newParam.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                newParam.setDisplayName(EParameterName.REPOSITORY_SCHEMA_TYPE.getDisplayName());
                newParam.setListItemsDisplayName(new String[] {});
                newParam.setListItemsValue(new String[] {});
                newParam.setNumRow(param.getNumRow());
                newParam.setFieldType(EParameterFieldType.TECHNICAL);
                newParam.setValue(""); //$NON-NLS-1$
                newParam.setShow(false);
                newParam.setRequired(true);
                newParam.setReadOnly(param.isReadOnly());
                newParam.setShowIf(param.getName() + " =='" + REPOSITORY + "'"); //$NON-NLS-1$//$NON-NLS-2$
                newParam.setNotShowIf(param.getNotShowIf());
                newParam.setContext(IGenericConstants.CONTEXT);
                newParam.setSerialized(true);
                newParam.setParentParameter(param);

                if (autoSwitchParam == null) {
                    autoSwitchParam = new ElementParameter(node);
                    autoSwitchParam.setCategory(EComponentCategory.TECHNICAL);
                    autoSwitchParam.setName(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
                    autoSwitchParam.setDisplayName(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getDisplayName());
                    autoSwitchParam.setNumRow(param.getNumRow());
                    autoSwitchParam.setFieldType(EParameterFieldType.CHECK);
                    autoSwitchParam.setValue(Boolean.FALSE);
                    autoSwitchParam.setShow(false);
                    autoSwitchParam.setRequired(true);
                    autoSwitchParam.setReadOnly(true);
                }
            }
        }
        if (autoSwitchParam != null) {
            listParam.add(autoSwitchParam);
        }
    }

    /**
     * DOC nrousseau Comment method "setupConnector".
     *
     * @param node
     * @param rootProperty
     * @param paramName
     * @param schemaProperty
     * @param found
     * @return
     */
    private boolean setupConnector(final INode node, List<ElementParameter> listParam, String paramName,
            SchemaProperty schemaProperty, boolean isOutput) {
        ComponentProperties rootProperty = node.getComponentProperties();
        boolean found = false;
        for (Connector connector : rootProperty.getPossibleConnectors(isOutput)) {
            if (!(schemaProperty.getValue() instanceof Schema)) {
                continue;
            }
            Schema schema = schemaProperty.getValue();
            if (connector instanceof PropertyPathConnector) {
                String linkedSchema = ((PropertyPathConnector) connector).getPropertyPath() + ".schema"; //$NON-NLS-1$
                if (paramName.equals(linkedSchema)) {
                    found = true;
                    ElementParameter param = new ElementParameter(node);
                    param.setName(paramName);
                    param.setFieldType(EParameterFieldType.SCHEMA_REFERENCE);
                    param.setShow(false);
                    if (!isOutput) {
                        param.setContext(EConnectionType.FLOW_MAIN.getName());
                    } else {
                        param.setContext(connector.getName());
                    }
                    param.setCategory(EComponentCategory.TECHNICAL);
                    IElementParameterDefaultValue defaultValue = new ElementParameterDefaultValue();
                    defaultValue.setDefaultValue(new Schema.Parser().parse(schema.toString()));
                    param.getDefaultValues().add(defaultValue);
                    listParam.add(param);
                }
            }
        }
        return found;
    }

    /**
     * DOC nrousseau Comment method "findSchemaProperties".
     *
     * @param rootProperty
     * @param listParam
     * @return
     */
    private void findSchemaProperties(Properties rootProperty, List<ElementParameter> listParam,
            Map<String, SchemaProperty> schemaProperties, String parentPropertiesPath) {

        for (NamedThing nt : rootProperty.getProperties()) {
            String path = parentPropertiesPath;
            if (path == null) {
                path = nt.getName();
            } else {
                path = parentPropertiesPath + "." + nt.getName(); //$NON-NLS-1$
            }
            if (nt instanceof SchemaProperty) {
                boolean found = false;
                for (IElementParameter param : listParam) {
                    if (path.equals(param.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    schemaProperties.put(path, (SchemaProperty) nt);
                }
            } else if (nt instanceof Properties) {
                findSchemaProperties((Properties) nt, listParam, schemaProperties, path);
            }
        }
    }

    @Override
    public String getShortName() {
        String originalComponentName = getName();
        String calculatedShortName = "";//$NON-NLS-1$
        char[] cars = new char[originalComponentName.length()];
        int nbChars = 0;

        for (int i = 0; i < originalComponentName.length(); i++) {
            for (char c = 'A'; c <= 'Z'; c++) {
                if (originalComponentName.charAt(i) == c) {
                    cars[nbChars] = originalComponentName.charAt(i);
                    nbChars++;
                }
            }
        }
        calculatedShortName = String.copyValueOf(cars, 0, nbChars);
        calculatedShortName = calculatedShortName.toLowerCase();
        return calculatedShortName;
    }

    public boolean canStart() {
        return componentDefinition.isStartable();
    }

    @Override
    public List<INodeConnector> createConnectors(INode parentNode) {
        List<INodeConnector> listConnector = new ArrayList<>();

        ComponentProperties componentProperties = parentNode.getComponentProperties();
        if (componentProperties == null) {
            parentNode.setComponentProperties(ComponentsUtils.getComponentProperties(componentDefinition.getName()));
            componentProperties = parentNode.getComponentProperties();
        }
        Set<? extends Connector> inputConnectors = componentProperties.getPossibleConnectors(false);

        if (inputConnectors.isEmpty()) {
            INodeConnector connector = null;
            connector = addStandardType(listConnector, EConnectionType.FLOW_MAIN, parentNode);
            connector.setMaxLinkInput(0);
            connector.setMaxLinkOutput(0);
        } else {
            for (Connector connector : inputConnectors) {
                addGenericType(listConnector, EConnectionType.FLOW_MAIN, connector.getName(), parentNode, componentProperties,
                        false);
            }
        }

        Set<? extends Connector> outputConnectors = componentProperties.getPossibleConnectors(true);
        List<? extends Connector> sortedOutputConnectors = new ArrayList<>(outputConnectors);
        sortedOutputConnectors.sort(new Comparator<Connector>() {

            @Override
            public int compare(Connector o1, Connector o2) {
                if (Connector.MAIN_NAME.equals(o1.getName())) {
                    return -1;
                }
                if (Connector.MAIN_NAME.equals(o2.getName())) {
                    return 1;
                }
                return 0;
            }
        });
        for (Connector connector : sortedOutputConnectors) {
            EConnectionType type = EConnectionType.FLOW_MAIN;
            if (Connector.REJECT_NAME.equals(connector.getName())) {
                type = EConnectionType.REJECT;
            }
            addGenericType(listConnector, type, connector.getName(), parentNode, componentProperties, true);
        }
        addStandardType(listConnector, EConnectionType.RUN_IF, parentNode);
        addStandardType(listConnector, EConnectionType.ON_COMPONENT_OK, parentNode);
        addStandardType(listConnector, EConnectionType.ON_COMPONENT_ERROR, parentNode);
        addStandardType(listConnector, EConnectionType.ON_SUBJOB_OK, parentNode);
        addStandardType(listConnector, EConnectionType.ON_SUBJOB_ERROR, parentNode);

        Set<ConnectorTopology> topologies = componentDefinition.getSupportedConnectorTopologies();
        createIterateConnectors(topologies, listConnector, parentNode);

        for (int i = 0; i < EConnectionType.values().length; i++) {
            EConnectionType currentType = EConnectionType.values()[i];

            if ((currentType == EConnectionType.FLOW_REF) || (currentType == EConnectionType.FLOW_MERGE)) {
                continue;
            }
            boolean exists = false;
            for (INodeConnector curNodeConn : listConnector) {
                if (curNodeConn.getDefaultConnectionType().equals(currentType)) {
                    exists = true;
                    if (currentType == EConnectionType.FLOW_MAIN) {
                        curNodeConn.addConnectionProperty(EConnectionType.FLOW_REF, EConnectionType.FLOW_REF.getRGB(),
                                EConnectionType.FLOW_REF.getDefaultLineStyle());
                        curNodeConn.addConnectionProperty(EConnectionType.FLOW_MERGE, EConnectionType.FLOW_MERGE.getRGB(),
                                EConnectionType.FLOW_MERGE.getDefaultLineStyle());
                    }

                }
            }
            if (!exists) { // will add by default all connectors not defined in
                NodeConnector nodeConnector = new NodeConnector(parentNode);
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
                listConnector.add(nodeConnector);
            }
        }
        return listConnector;
    }

    /**
     * Create iterate connector for this {@link Component} There are 4 types of components (depending on what main
     * connections allowed): 1. StandAlone component (can't have main connections at all) 2. Input component (can have
     * outgoing main connection) 3. Output component (can have incoming main connection) 4. Intermediate component (can
     * have both incoming and outgoing main connections)
     *
     * Iterate connector is created by default for TCOMP component with following rules: Outgoing iterate: all types of
     * components can have infinite outgoing iterate connections Incoming iterate: StandAlone, Input components (also
     * called startable components) can have 1 incoming iterate flow; Output, Intermediate components can't have
     * incoming iterate flow (because they are not startable)
     *
     * Note: infinite value is defined by -1 int value
     *
     * @param topologies connection topologies supported by this {@link Component}. Component could support several
     * topologies. Such component is called hybrid
     * @param listConnector list of all {@link Component} connectors
     * @param parentNode parent node
     */
    private void createIterateConnectors(Set<ConnectorTopology> topologies, List<INodeConnector> listConnector,
            INode parentNode) {
        boolean inputOrNone = topologies.contains(ConnectorTopology.NONE) || topologies.contains(ConnectorTopology.OUTGOING);
        INodeConnector iterateConnector = addStandardType(listConnector, EConnectionType.ITERATE, parentNode);
        iterateConnector.setMaxLinkOutput(-1);
        if (inputOrNone) {
            iterateConnector.setMaxLinkInput(1);
        } else {
            iterateConnector.setMaxLinkInput(0);
        }
    }

    /**
     * Add default connector type, if not already defined by component.
     *
     * @param listConnector
     * @param type
     * @param parentNode
     * @return
     */
    private INodeConnector addStandardType(List<INodeConnector> listConnector, EConnectionType type, INode parentNode) {
        NodeConnector nodeConnector = new NodeConnector(parentNode);
        nodeConnector.setName(type.getName());
        nodeConnector.setBaseSchema(type.getName());
        nodeConnector.setDefaultConnectionType(type);
        nodeConnector.setLinkName(type.getDefaultLinkName());
        nodeConnector.setMenuName(type.getDefaultMenuName());
        nodeConnector.addConnectionProperty(type, type.getRGB(), type.getDefaultLineStyle());
        listConnector.add(nodeConnector);
        return nodeConnector;
    }

    private void addGenericType(List<INodeConnector> listConnector, EConnectionType type, String genericConnectorType,
            INode parentNode, ComponentProperties componentProperties, boolean isOutput) {
        GenericNodeConnector nodeConnector = new GenericNodeConnector(parentNode, isOutput);
        nodeConnector.setComponentProperties(componentProperties);
        nodeConnector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        nodeConnector.setGenericConnectorType(genericConnectorType);
        nodeConnector.setLinkName(type.getDefaultLinkName());
        if (type == EConnectionType.REJECT) {
            nodeConnector.addConnectionProperty(EConnectionType.FLOW_MAIN, new RGB(255, 0, 0), 2);
            nodeConnector.getConnectionProperty(EConnectionType.FLOW_MAIN).setRGB(new RGB(255, 0, 0));
        } else {
            nodeConnector.addConnectionProperty(type, type.getRGB(), type.getDefaultLineStyle());
        }
        listConnector.add(nodeConnector);
    }

    @Override
    public boolean isSchemaAutoPropagated() {
        return componentDefinition.isSchemaAutoPropagate();
    }

    @Override
    public boolean isDataAutoPropagated() {
        return componentDefinition.isDataAutoPropagate();
    }

    @Override
    public boolean canParallelize() {
        return componentDefinition.isParallelize();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getModulesNeeded()
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        return getModulesNeeded(null);
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded(INode node) {
        List<ModuleNeeded> componentImportNeedsList = new ArrayList<>();
        ConnectorTopology topology = null;
        if (node != null) {
            boolean hasInput = !NodeUtil.getIncomingConnections(node, IConnectionCategory.DATA).isEmpty();
            boolean hasOutput = !NodeUtil.getOutgoingConnections(node, IConnectionCategory.DATA).isEmpty();
            if (hasInput && hasOutput) {
                topology = ConnectorTopology.INCOMING_AND_OUTGOING;
            } else if (hasInput) {
                topology = ConnectorTopology.INCOMING;
            } else if (hasOutput) {
                topology = ConnectorTopology.OUTGOING;
            } else {
                topology = ConnectorTopology.NONE;
            }
        } else {
            Set<ConnectorTopology> topologies = componentDefinition.getSupportedConnectorTopologies();
            if (!topologies.isEmpty()) {
                topology = topologies.iterator().next();
            }
        }
        RuntimeInfo runtimeInfo = null;
        try {
            runtimeInfo = componentDefinition.getRuntimeInfo(ExecutionEngine.DI,
                    node == null ? null : node.getComponentProperties(), topology);
        } catch (Exception e) {
            if (node == null) {
                // not handled, must because the runtime info must have a node configuration (properties are null)
            } else if (e instanceof TalendRuntimeException) {
                // no need to check talend runtime exception in design time.
                e.printStackTrace(); // only for debug.
            } else {
                ExceptionHandler.process(e);
            }
        }
        if (runtimeInfo != null) {
            if (runtimeInfo instanceof JarRuntimeInfo) {
                JarRuntimeInfo currentRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
                runtimeInfo = currentRuntimeInfo.cloneWithNewJarUrlString(currentRuntimeInfo.getJarUrl().toString()
                        .replace("mvn:", "mvn:" + MavenConstants.LOCAL_RESOLUTION_URL + "!"));
            }
            final Bundle bundle = FrameworkUtil.getBundle(componentDefinition.getClass());
            for (URL mvnUri : runtimeInfo.getMavenUrlDependencies()) {
                ModuleNeeded moduleNeeded = new ModuleNeeded(getName(), "", true, mvnUri.toString()); //$NON-NLS-1$
                componentImportNeedsList.add(moduleNeeded);

                if (bundle != null) { // update module location
                    try {
                        final MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(moduleNeeded.getDefaultMavenURI());
                        final String moduleFileName = artifact.getFileName(false);
                        final File bundleFile = BundleFileUtil.getBundleFile(bundle, moduleFileName);
                        if (bundleFile != null && bundleFile.exists()) {
                            // FIXME, better install the embed jars from bundle directly in this way.
                            moduleNeeded.setModuleLocaion(
                                    ExtensionModuleManager.URIPATH_PREFIX + bundle.getSymbolicName() + '/' + moduleFileName);
                        }
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        
        ModuleNeeded moduleNeeded = null;
        if(!CheckLogManamger.isSelectLog4j2()) {
            //TODO consider to let it works for all jobs, not only for tcompv0 components, mean move the code to ReplaceNodeInProcess and UpdateLog4jJarUtils when not log4j2
            moduleNeeded = new ModuleNeeded(getName(), "", true, "mvn:org.slf4j/slf4j-log4j12/1.7.25");
            componentImportNeedsList.add(moduleNeeded);
        }
        moduleNeeded = new ModuleNeeded(getName(), "", true, "mvn:org.talend.libraries/talend-codegen-utils/0.30.0");
        componentImportNeedsList.add(moduleNeeded);
        return componentImportNeedsList;
    }

    @Override
    public ImageDescriptor getIcon16() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(componentDefinition.getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(16, 16));
        }
        return new DummyComponent("dummy").getIcon16(); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getIcon24() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(componentDefinition.getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(24, 24));
        }
        return new DummyComponent("dummy").getIcon24();//$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getIcon32() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(componentDefinition.getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData);
        }
        return new DummyComponent("dummy").getIcon32();//$NON-NLS-1$
    }

    private ArrayList<ECodePart> createCodePartList() {
        ArrayList<ECodePart> theCodePartList = new ArrayList<>();
        theCodePartList.add(ECodePart.BEGIN);
        theCodePartList.add(ECodePart.MAIN);
        theCodePartList.add(ECodePart.PROCESS_DATA_BEGIN);
        theCodePartList.add(ECodePart.PROCESS_DATA_END);
        theCodePartList.add(ECodePart.END);
        theCodePartList.add(ECodePart.FINALLY);
        return theCodePartList;
    }

    @Override
    public List<ECodePart> getAvailableCodeParts() {
        if (codePartListX == null) {
            codePartListX = createCodePartList();
        }
        return codePartListX;
    }

    @Override
    public EComponentType getComponentType() {
        return EComponentType.GENERIC;
    }

    public ComponentsProvider getProvider() {
        return this.provider;
    }

    @Override
    protected List<IMultipleComponentManager> createMultipleComponentManagers() {
        List<IMultipleComponentManager> multipleComponentManagers = new ArrayList<>();
        if (componentDefinition instanceof VirtualComponentDefinition) {
            VirtualComponentDefinition definition = (VirtualComponentDefinition) componentDefinition;
            String inputComponentName = null;
            String outputComponentName = null;
            ComponentDefinition inputComponentDefinition = definition.getInputComponentDefinition();
            if (inputComponentDefinition != null) {
                inputComponentName = inputComponentDefinition.getName();
            }
            ComponentDefinition outputComponentDefinition = definition.getOutputComponentDefinition();
            if (outputComponentDefinition != null) {
                outputComponentName = outputComponentDefinition.getName();
            }
            if (inputComponentName == null || outputComponentName == null) {
                return multipleComponentManagers;
            }
            IMultipleComponentManager multipleComponentManager = new MultipleComponentManager(inputComponentName,
                    outputComponentName);
            IMultipleComponentItem inputItem = multipleComponentManager.addItem(inputComponentName, inputComponentName);
            multipleComponentManager.setExistsLinkTo(true);
            String cType = EConnectionType.ON_ROWS_END.getName(); // FIXME: should get the connector type by other way.
            inputItem.getOutputConnections().add(new MultipleComponentConnection(cType, outputComponentName));
            multipleComponentManager.addItem(outputComponentName, outputComponentName);
            multipleComponentManager.validateItems();
            multipleComponentManagers.add(multipleComponentManager);
        }
        return multipleComponentManagers;
    }

    @Override
    public boolean useMerge() {
        // for (IConnector connector : componentDefinition.getConnectors()) {
        // if (ComponentsUtils.isAValidConnector(connector, getName())) {
        // if (connector.getType().equals(EConnectionType.FLOW_MERGE.getName())) {
        // return true;
        // }
        // }
        // }
        return false;
    }

    @Override
    public boolean useFlow() {
        // for (Connector connector : componentDefinition.getConnectors()) {
        // if (ComponentsUtils.isAValidConnector(connector, getName())) {
        // if (EConnectionType.FLOW_MAIN.getName().equals(connector.getType())
        // && !(connector.getMaxInput() == 0 && connector.getMaxOutput() == 0)) {
        // return true;
        // }
        // }
        // }
        return true;
    }

    @Override
    public boolean useSchema(Node node) {
        boolean useSchema = false;
        if (node != null) {
            List<? extends IElementParameter> listParam = node.getElementParameters();
            for (IElementParameter param : listParam) {
                if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType()) && !param.isReadOnly()
                        && (param.getContext() == null || EConnectionType.FLOW_MAIN.getName().equals(param.getContext()))) {
                    useSchema = true;
                    break;
                }
            }
        }
        return useSchema;
    }

    public void setProvider(ComponentsProvider provider) {
        this.provider = provider;
    }

    public static class CodegenPropInfo {

        public String fieldName;

        public String className;

        public Properties props;
    }

    protected void processCodegenPropInfos(List<CodegenPropInfo> propList, Properties props, String fieldString) {
        for (NamedThing prop : props.getProperties()) {
            if (prop instanceof Properties) {
                if (prop instanceof ReferenceProperties) {
                    ReferenceProperties rp = (ReferenceProperties) prop;
                    rp.referenceDefinitionName.setTaggedValue(IGenericConstants.ADD_QUOTES, true);
                }
                if (prop instanceof ComponentReferenceProperties) {
                    ComponentReferenceProperties crp = (ComponentReferenceProperties) prop;
                    crp.componentInstanceId.setTaggedValue(IGenericConstants.ADD_QUOTES, true);
                }
                CodegenPropInfo childPropInfo = new CodegenPropInfo();
                if (fieldString.equals("")) {//$NON-NLS-1$
                    childPropInfo.fieldName = "." + prop.getName();//$NON-NLS-1$
                } else {
                    childPropInfo.fieldName = fieldString + "." + prop.getName();//$NON-NLS-1$
                }
                childPropInfo.className = prop.getClass().getName();
                childPropInfo.props = (Properties) prop;
                propList.add(childPropInfo);
                processCodegenPropInfos(propList, childPropInfo.props, childPropInfo.fieldName);
            }
        }
    }

    public List<CodegenPropInfo> getCodegenPropInfos(ComponentProperties props) {
        List<CodegenPropInfo> propsList = new ArrayList<>();
        CodegenPropInfo propInfo = new CodegenPropInfo();
        propInfo.fieldName = "";//$NON-NLS-1$
        propInfo.className = props.getClass().getName();
        propInfo.props = props;
        propsList.add(propInfo);
        processCodegenPropInfos(propsList, props, propInfo.fieldName);
        return propsList;
    }

    public static final CharSequenceTranslator ESCAPE_SCHEMA = new AggregateTranslator(
            new CharSequenceTranslator[] { new LookupTranslator(new String[][] { { "\"", "\\\"" }, { "\\", "\\\\" } }),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()), JavaUnicodeEscaper.outsideOf(32, 127) });

    public String getCodegenValue(Property property, String value) {
        if (property.isFlag(Property.Flags.ENCRYPT)) {
            return ElementParameterParser.getEncryptedValue(value);
        }
        if (Boolean.valueOf(String.valueOf(property.getTaggedValue(IGenericConstants.ADD_QUOTES)))) {
            return TalendQuoteUtils.addQuotesIfNotExist(value);
        }
        if (GenericTypeUtils.isEnumType(property)) {
            if (ContextParameterUtils.isContainContextParam(value) || value.indexOf("globalMap.get") > -1) {
                return value;
            }else {
                return TalendQuoteUtils.addQuotesForComplexusString(value);
            }
        }
        if (GenericTypeUtils.isStringType(property)
                && property.getTaggedValue(IGenericConstants.LINE_SEPARATOR_REPLACED_TO) != null) {
            //process for the sql field for jdbc, snowflake, salesforce, LINE_SEPARATOR_REPLACED_TO key can tell us which a sql type field,
            //as sql type value may have newline and return characters, which make compiler issue in java code,
            //so have to convert the newline characters to visible "\r", "\n" for pass the compiler issue and can't only convert them to white space as TDI-41898
            //jdbc drivers, salesforce driver can work like that sql : select * \nfrom Account, so it is ok
            String replacedString = NodeUtil.replaceCRLFInMEMO_SQL(value).trim();

            // For the case when sql field ends with extra semicolon, it has to be removed to avoid compilation error.
            return replacedString.endsWith(";")
                    ? replacedString.substring(0, replacedString.length() -1)
                    : replacedString;
        }
        if (GenericTypeUtils.isSchemaType(property)) {
            // Handles embedded escaped quotes which might occur
            return "\"" + ESCAPE_SCHEMA.translate(value) + "\"";
        }
        if (GenericTypeUtils.isIntegerType(property) && ContextParameterUtils.isContainContextParam(value)) {
            value = "routines.system.ObjectUtil.nonNull(" + value + ") ? Integer.valueOf(" + value + ") : null";
        }
        if (GenericTypeUtils.isFloatType(property) && ContextParameterUtils.isContainContextParam(value)) {
            value = "routines.system.ObjectUtil.nonNull(" + value + ") ? Float.valueOf(" + value + ") : null";
        }
        if (GenericTypeUtils.isDoubleType(property) && ContextParameterUtils.isContainContextParam(value)) {
            value = "routines.system.ObjectUtil.nonNull(" + value + ") ? Double.valueOf(" + value + ") : null";
        }
        if (GenericTypeUtils.isLongType(property) && ContextParameterUtils.isContainContextParam(value)) {
            value = "routines.system.ObjectUtil.nonNull(" + value + ") ? Long.valueOf(" + value + ") : null";
        }
        if ("\"\"\"".equals(value)) {
            value = "\"\\\"\"";
        }
        return value;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((this.getPaletteType() == null) ? 0 : this.getPaletteType().hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Component other = (Component) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (this.getPaletteType() == null) {
            if (other.getPaletteType() != null) {
                return false;
            }
        } else if (!this.getPaletteType().equals(other.getPaletteType())) {
            return false;
        }
        return true;
    }

    @Override
    public void initNodeProperties(INode newNode, INode oldNode) {
        this.initNodePropertiesFromSerialized(newNode, oldNode.getComponentProperties().toSerialized());
    }

    @Override
    public void initNodePropertiesFromSerialized(INode node, String serialized) {
        if (node != null) {
            node.setComponentProperties(
                    Properties.Helper.fromSerializedPersistent(serialized, ComponentProperties.class, new PostDeserializeSetup() {

                        @Override
                        public void setup(Object properties) {
                            ((Properties) properties).setValueEvaluator(new ComponentContextPropertyValueEvaluator(node));
                            ComponentsUtils.getComponentService().postDeserialize(((Properties) properties));
                        }

                    }).object);
        }
    }

    @Override
    public String genericToSerialized(IElementParameter param) {
        if (param instanceof GenericElementParameter) {
            Node node = (Node) ((GenericElementParameter) param).getElement();
            ComponentProperties properties = node.getComponentProperties();
            return properties.toSerialized();
        } else {
            ComponentProperties componentProperties = ComponentsUtils.getComponentProperties(componentDefinition.getName());
            return componentProperties.toSerialized();
        }
    }

    @Override
    public Object getElementParameterValueFromComponentProperties(INode iNode, IElementParameter param) {
        if (iNode != null) {
            ComponentProperties iNodeComponentProperties = iNode.getComponentProperties();
            if (iNodeComponentProperties != null && param instanceof GenericElementParameter) {
                Properties paramProperties = ComponentsUtils.getCurrentProperties(iNodeComponentProperties, param.getName());
                if (paramProperties != null) {
                    // update repository value
                    Property property = iNodeComponentProperties.getValuedProperty(param.getName());
                    if (property != null) {
                        if (property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null) {
                            param.setRepositoryValue(param.getName());
                            param.setRepositoryValueUsed(true);
                        }
                    } else {
                        Properties currentProperties = ((GenericElementParameter) param).getProperties();
                        if (currentProperties != null) {
                            boolean isRepostory = false;
                            for (NamedThing thing : currentProperties.getProperties()) {
                                if (thing instanceof Property) {
                                    if (((Property) thing).getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null) {
                                        isRepostory = true;
                                    }
                                }
                            }
                            if (isRepostory) {
                                param.setRepositoryValue(param.getName());
                                param.setRepositoryValueUsed(true);
                            }
                        }
                    }
                    Object value = ComponentsUtils.getGenericPropertyValue(iNodeComponentProperties, param.getName());
                    if (value == null && EParameterFieldType.TABLE.equals(param.getFieldType())) {
                        value = GenericTableUtils.getTableValues(iNodeComponentProperties.getProperties(param.getName()), param);
                    }
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public boolean setGenericPropertyValue(IElementParameter param) {
        if (param == null || param.getName() == null) {
            return false;
        }
        if (param instanceof GenericElementParameter) {
            ComponentProperties componentProperties = ((Node) ((GenericElementParameter) param).getElement())
                    .getComponentProperties();
            Property<?> property = componentProperties.getValuedProperty(param.getName());
            if (property != null) {
                property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, param.getName());
                return true;
            }

            Properties currentProperties = ((GenericElementParameter) param).getProperties();
            if (currentProperties != null) {
                boolean isRepostory = false;
                for (NamedThing thing : currentProperties.getProperties()) {
                    if (thing instanceof Property) {
                        ((Property) thing).setTaggedValue(IGenericConstants.REPOSITORY_VALUE, param.getName());
                        isRepostory = true;
                    }
                }
                return isRepostory;
            }

        }
        return false;
    }

    /**
     * Getter for log. Will be used by ComponentUtil.
     *
     * @return the log
     */
    public static Logger getLog() {
        return log;
    }

    @Override
    public boolean hasConditionalOutputs() {
        return componentDefinition.isConditionalInputs();
    }

    @Override
    public String getRepositoryType(Connection connection) {
        String propertiesStr = null;
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null) {
            propertiesStr = wizardService.getConnectionProperties(connection);
        }
        ComponentProperties properties = ComponentsUtils.getComponentPropertiesFromSerialized(propertiesStr, connection, true);
        if (properties != null) {
            ComponentWizardDefinition wizardDefinition = getWizardDefinition(properties);
            if (wizardDefinition != null) {
                return wizardDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Sets the visible.
     *
     * @param visible the visible to set
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
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

    @Override
    public boolean isVisible(String family) {
        if (visible != null) {
            return visible;
        }
        return ComponentsSettingsHelper.isComponentVisible(this, family);
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return true;
    }

    /**
     * Sets the technical.
     *
     * @param technical the technical to set
     */
    public void setTechnical(Boolean technical) {
        this.technical = technical;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    @Override
    public boolean isTechnical() {
        if (technical != null) {
            if (technical) {
                return true;
            } else {
                if (isFamilyNameEmpty()) {// TUP-17720 when family name is null, should not show component
                    return true;
                }
            }
        } else {
            return isFamilyNameEmpty();
        }
        return false;
    }

    public boolean isFamilyNameEmpty() {
        if (getOriginalFamilyName() == null || getOriginalFamilyName().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String getVersion() {
        Version version = FrameworkUtil.getBundle(componentDefinition.getClass()).getVersion();
        if (version != null) {
            return version.toString();
        }
        return super.getVersion();
    }

    public String getJetFileNamePrefix() {
        return "component";
    }

    @Override
    public String getTemplateFolder() {
        return "jet_stub/generic";
    }

    @Override
    public String getTemplateNamePrefix() {
        return "component";
    }

}
