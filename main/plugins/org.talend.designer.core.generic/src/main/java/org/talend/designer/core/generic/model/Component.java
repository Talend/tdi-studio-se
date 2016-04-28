// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.exception.BusinessException;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.VirtualComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.services.ComponentServiceWithValueEvaluator;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.context.ComponentContextPropertyValueEvaluator;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * created by hcyi on Sep 10, 2015 Detailled comment
 *
 */
public class Component extends AbstractBasicComponent {

    private static Logger log = Logger.getLogger(Component.class);

    private ComponentDefinition componentDefinition;

    private List<ModuleNeeded> componentImportNeedsList;

    private ComponentsProvider provider;

    private Map<String, String> translatedMap = new HashMap<>();

    public Component(ComponentDefinition componentDefinition) throws BusinessException {
        this.componentDefinition = componentDefinition;
        // TODO
        // TCOMP-92
        this.setPaletteType("DI"); //$NON-NLS-1$
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
            node.setComponentProperties(ComponentsUtils.getComponentProperties(getName()));
        }
        List<ElementParameter> listParam;
        listParam = new ArrayList<>();
        addMainParameters(listParam, node);
        addPropertyParameters(listParam, node, Form.MAIN, EComponentCategory.BASIC);
        addPropertyParameters(listParam, node, Form.ADVANCED, EComponentCategory.ADVANCED);
        initializeParametersForSchema(listParam, node);
        addViewParameters(listParam, node);
        addDocParameters(listParam, node);
        addValidationRulesParameters(listParam, node);
        return listParam;
    }

    @Override
    public List<NodeReturn> createReturns() {
        List<NodeReturn> listReturn = new ArrayList<>();
        ComponentProperties props = ComponentsUtils.getComponentProperties(getName());
        Property returns = props.returns;
        if (returns != null) {
            NodeReturn nodeRet = null;
            for (Property children : returns.getChildren()) {
                nodeRet = new NodeReturn();
                nodeRet.setType(ComponentsUtils.getTalendTypeFromPropertyType(children.getType()).getId());
                nodeRet.setDisplayName(children.getDisplayName());
                nodeRet.setName(children.getName());
                Object object = children.getTaggedValue(IGenericConstants.AVAILABILITY);
                if (object != null) {
                    nodeRet.setAvailability(object.toString());
                } else {
                    nodeRet.setAvailability("AFTER"); //$NON-NLS-1$
                }
                listReturn.add(nodeRet);
            }
        }
        return listReturn;
    }

    private void addDocParameters(final List<ElementParameter> listParam, INode node) {
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

    private void addValidationRulesParameters(final List<ElementParameter> listParam, INode node) {
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
            param.setRepositoryValue(wizardDefinition.getName());
        }
        param.setValue("");//$NON-NLS-1$
        param.setNumRow(2);
        param.setShow(wizardDefinition != null);

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

    private void addPropertyParameters(final List<ElementParameter> listParam, final INode node, String formName,
            EComponentCategory category) {
        ComponentProperties props = node.getComponentProperties();
        Form form = props.getForm(formName);
        List<ElementParameter> parameters = ComponentsUtils.getParametersFromForm(node, category, node.getComponentProperties(),
                form);
        ComponentService componentService = new ComponentServiceWithValueEvaluator(ComponentsUtils.getComponentService(),
                new ComponentContextPropertyValueEvaluator(node));
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
    private void initializeParametersForSchema(List<ElementParameter> listParam, final INode node) {
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

        for (Trigger trigger : componentDefinition.getTriggers()) {
            if (ComponentsUtils.isAValidTrigger(trigger, getName())) {
                INodeConnector connector = ComponentsUtils.generateNodeConnectorFromTrigger(trigger, parentNode);
                if (connector != null) {
                    listConnector.add(connector);
                }
            }
        }

        boolean isOutputComponent = componentDefinition instanceof OutputComponentDefinition || componentDefinition instanceof VirtualComponentDefinition;
        INodeConnector connector = addStandardType(listConnector, EConnectionType.FLOW_MAIN, parentNode);
        if (isOutputComponent) {
            connector.setMaxLinkInput(1);
        } else {
            connector.setMaxLinkInput(0);
        }
        connector.setMaxLinkOutput(0);
        addGenericType(listConnector, EConnectionType.FLOW_MAIN, Connector.MAIN_NAME, parentNode);
        addGenericType(listConnector, EConnectionType.REJECT, Connector.REJECT_NAME, parentNode);
        addStandardType(listConnector, EConnectionType.RUN_IF, parentNode);
        addStandardType(listConnector, EConnectionType.ON_COMPONENT_OK, parentNode);
        addStandardType(listConnector, EConnectionType.ON_COMPONENT_ERROR, parentNode);
        addStandardType(listConnector, EConnectionType.ON_SUBJOB_OK, parentNode);
        addStandardType(listConnector, EConnectionType.ON_SUBJOB_ERROR, parentNode);

        for (int i = 0; i < EConnectionType.values().length; i++) {
            EConnectionType currentType = EConnectionType.values()[i];

            if ((currentType == EConnectionType.FLOW_REF) || (currentType == EConnectionType.FLOW_MERGE)) {
                continue;
            }
            boolean exists = false;
            for (INodeConnector curNodeConn : listConnector) {
                if (curNodeConn.getDefaultConnectionType().equals(currentType)) {
                    exists = true;
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
                if (currentType == EConnectionType.FLOW_MAIN) {
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_REF, EConnectionType.FLOW_REF.getRGB(),
                            EConnectionType.FLOW_REF.getDefaultLineStyle());
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_MERGE, EConnectionType.FLOW_MERGE.getRGB(),
                            EConnectionType.FLOW_MERGE.getDefaultLineStyle());
                }
                listConnector.add(nodeConnector);
            }
        }
        return listConnector;
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
            INode parentNode) {
        GenericNodeConnector nodeConnector = new GenericNodeConnector(parentNode);
        nodeConnector.setMaxLinkInput(0);
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
    public List<ModuleNeeded> getModulesNeeded() {
        if (componentImportNeedsList != null) {
            return componentImportNeedsList;
        } else {
            ComponentService componentService = ComponentsUtils.getComponentService();
            Set<String> mavenUriDependencies = componentService.getMavenUriDependencies(getName());
            componentImportNeedsList = new ArrayList<>(mavenUriDependencies.size());
            for (String mvnUri : mavenUriDependencies) {
                ModuleNeeded moduleNeeded = new ModuleNeeded(getName(), "", true, mvnUri);
                componentImportNeedsList.add(moduleNeeded);
            }
            return componentImportNeedsList;
        }
    }

    @Override
    public ImageDescriptor getIcon16() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(16, 16));
        }
        return new DummyComponent("dummy").getIcon16(); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getIcon24() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
                ComponentImageType.PALLETE_ICON_32X32);
        if (imageStream != null) {
            ImageData imageData = new ImageData(imageStream);
            return ImageDescriptor.createFromImageData(imageData.scaledTo(24, 24));
        }
        return new DummyComponent("dummy").getIcon24();//$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getIcon32() {
        InputStream imageStream = ComponentsUtils.getComponentService().getComponentPngImage(getName(),
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
        theCodePartList.add(ECodePart.END);
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

        public ComponentProperties props;
    }

    protected void processCodegenPropInfos(List<CodegenPropInfo> propList, ComponentProperties props, String fieldString) {
        for (NamedThing prop : props.getProperties()) {
            if (prop instanceof ComponentProperties) {
                if (prop instanceof ComponentReferenceProperties) {
                    ((ComponentReferenceProperties) prop).componentProperties = null;
                }
                CodegenPropInfo childPropInfo = new CodegenPropInfo();
                if (fieldString.equals("")) {//$NON-NLS-1$
                    childPropInfo.fieldName = "." + prop.getName();//$NON-NLS-1$
                } else {
                    childPropInfo.fieldName = fieldString + "." + prop.getName();//$NON-NLS-1$
                }
                childPropInfo.className = prop.getClass().getName();
                childPropInfo.props = (ComponentProperties) prop;
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

    public Object getTableValue(Property property, String key, Object value) {
        if (ComponentsUtils.isPrevColumnList(property.getChildMap().get(key))) {
            return TalendQuoteUtils.addQuotes(value.toString());
        } else {
            return value;
        }
    }

    public String getCodegenValue(Property property, String value) {
        if (property.isFlag(Property.Flags.ENCRYPT)) {
            return ElementParameterParser.getEncryptedValue(value);
        }
        if (Boolean.valueOf(String.valueOf(property.getTaggedValue(IGenericConstants.ADD_QUOTES)))) {
            return "\"" + value + "\"";//$NON-NLS-1$ //$NON-NLS-2$ 
        }
        if (property.getType() == Property.Type.ENUM) {
            if (value.indexOf("context.") > -1 || value.indexOf("globalMap.get") > -1) {
                return value;//$NON-NLS-1$ //$NON-NLS-2$
            } else {
                return "\"" + value + "\"";//$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (property.getType() == Property.Type.SCHEMA) {
            // Handles embedded escaped quotes which might occur
            return "\"" + value.replace("\\\"", "\\\\\"").replace("\"", "\\\"") + "\"";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
    public void initNodePropertiesFromSerialized(INode node, String serialized) {
        if (node != null) {
            node.setComponentProperties(ComponentProperties.fromSerialized(serialized, ComponentProperties.class).properties);
        }
    }

    @Override
    public String genericToSerialized(IElementParameter param) {
        if (param instanceof GenericElementParameter) {
            return ((Node) ((GenericElementParameter) param).getElement()).getComponentProperties().toSerialized();
        } else {
            ComponentProperties componentProperties = ComponentsUtils.getComponentProperties(getName());
            return componentProperties.toSerialized();
        }
    }

    @Override
    public Object getElementParameterValueFromComponentProperties(INode iNode, IElementParameter param) {
        if (iNode != null) {
            ComponentProperties iNodeComponentProperties = iNode.getComponentProperties();
            if (iNodeComponentProperties != null && param instanceof GenericElementParameter) {
                ComponentProperties paramComponentProperties = ComponentsUtils.getCurrentComponentProperties(
                        iNodeComponentProperties, param.getName());
                if (paramComponentProperties != null) {
                    // update repository value
                    Property property = iNodeComponentProperties.getValuedProperty(param.getName());
                    if (property != null) {
                        if (property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null) {
                            param.setRepositoryValue(param.getName());
                            param.setRepositoryValueUsed(true);
                        }
                    }
                    return ComponentsUtils.getGenericPropertyValue(iNodeComponentProperties, param.getName());
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
            ComponentProperties currentComponentProperties = ComponentsUtils.getCurrentComponentProperties(componentProperties,
                    param.getName());
            if (currentComponentProperties == null) {
                return false;
            }
            Property property = componentProperties.getValuedProperty(param.getName());
            if (property != null) {
                property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, param.getName());
                return true;
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
}
