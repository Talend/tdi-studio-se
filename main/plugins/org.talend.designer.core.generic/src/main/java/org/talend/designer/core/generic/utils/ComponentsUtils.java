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
package org.talend.designer.core.generic.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.OctalUnescaper;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.filters.ComponentsFactoryProviderManager;
import org.talend.core.model.components.filters.IComponentFactoryFilter;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.SchemaProperty;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.Component;
import org.talend.designer.core.generic.model.GenericComponent;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericNodeConnector;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.generic.model.mapping.WidgetFieldTypeMapper;
import org.talend.designer.core.generic.palette.GenericComponentCategoryFactory;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.ElementParameterDefaultValue;
import org.talend.designer.core.model.components.UnifiedJDBCBean;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.metadata.managment.ui.wizard.context.MetadataContextPropertyValueEvaluator;

/**
 * created by hcyi on Sep 11, 2015 Detailled comment
 *
 */
public class ComponentsUtils {

    private static Set<IComponent> components = null;

    private static ComponentService compService = null;

    public static ComponentService getComponentService() {
        if (compService != null) {
            return compService;
        }
        BundleContext bundleContext = FrameworkUtil.getBundle(ComponentsUtils.class).getBundleContext();
        ServiceReference<ComponentService> compServiceRef = bundleContext.getServiceReference(ComponentService.class);
        if (compServiceRef != null) {
            compService = bundleContext.getService(compServiceRef);
        }
        return compService;
    }

    public static ComponentProperties getComponentProperties(String compName) {
        return getComponentService().getComponentProperties(compName);
    }

    public static List<ComponentDefinition> getPossibleComponents(ComponentProperties properties) {
        List<ComponentDefinition> possibleComponents = new ArrayList<ComponentDefinition>();
        try {
            possibleComponents.addAll(getComponentService().getPossibleComponents(properties));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return possibleComponents;
    }

    public static void loadComponents(ComponentService service) {
        if (service == null) {
            return;
        }
        IComponentsFactory componentsFactory = null;
        if (componentsFactory == null) {
            componentsFactory = ComponentsFactoryProvider.getInstance();
        }
        Set<IComponent> componentsList = componentsFactory.getComponents();
        if (components == null) {
            components = new HashSet<>();
        } else {
            componentsList.removeAll(components);
            components.clear();
        }

        // for additional JDBC components
        Set<ComponentDefinition> jdbcDefinitions = new HashSet<ComponentDefinition>();
        // Load components from service
        Set<ComponentDefinition> componentDefinitions = service.getAllComponents();
        for (ComponentDefinition componentDefinition : componentDefinitions) {
            if (componentDefinition.getFamilies() != null && componentDefinition.getFamilies().length > 0
                    && componentDefinition.getFamilies()[0] != null
                    && componentDefinition.getFamilies()[0].contains("JDBC")) {
                jdbcDefinitions.add(componentDefinition);
            }
            loadComponents(components, componentDefinition);
        }

        // load additional jdbc component
        loadAdditionalJDBCComponents(components, jdbcDefinitions);

        componentsList.addAll(components);
    }

    private static void loadComponents(Set<IComponent> componentsList, ComponentDefinition componentDefinition) {
        List<String> supportedProducts = componentDefinition.getSupportedProducts();
        if (supportedProducts == null) {
            return;
        }
        for (String productType : supportedProducts) {
            List<String> paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(productType);
            if (paletteTypes == null) {
                continue;
            }
            for (String paletteType : paletteTypes) {
                loadComponent(componentsList, componentDefinition, paletteType);
            }
        }
    }

    private static void loadComponent(Set<IComponent> componentsList, ComponentDefinition componentDefinition,
            String paletteType) {
        try {
            Component currentComponent = new Component(componentDefinition, paletteType);
            afterCreateComponent(componentsList, currentComponent);
        } catch (BusinessException e) {
            ExceptionHandler.process(e);
        }
    }

    private static void afterCreateComponent(Set<IComponent> componentsList, Component currentComponent) {

        Collection<IComponentFactoryFilter> filters = ComponentsFactoryProviderManager.getInstance().getProviders();
        boolean hiddenComponent = false;
        for (IComponentFactoryFilter filter : filters) {
            if (!filter.isAvailable(currentComponent.getName())) {
                hiddenComponent = true;
                break;
            }
        }

        // if the component is not needed in the current branding,
        // and that this one IS NOT a specific component for code generation
        // just don't load it
        if (hiddenComponent
                && !(currentComponent.getOriginalFamilyName().contains("Technical") || currentComponent.isTechnical())) {
            return;
        }

        // if the component is not needed in the current branding,
        // and that this one IS a specific component for code generation,
        // hide it
        if (hiddenComponent
                && (currentComponent.getOriginalFamilyName().contains("Technical") || currentComponent.isTechnical())) {
            currentComponent.setVisible(false);
            currentComponent.setTechnical(true);
        }

        componentsList.add(currentComponent);

    }

    private static void loadAdditionalJDBCComponents(Set<IComponent> componentsList, Set<ComponentDefinition> compDefinitions) {
        Map<String, UnifiedJDBCBean> jdbcMap = UnifiedComponentUtil.getAdditionalJDBC();
        if (jdbcMap.keySet().isEmpty()) {
            return;
        }
        for (ComponentDefinition definition : compDefinitions) {
            List<String> supportedProducts = definition.getSupportedProducts();
            if (supportedProducts == null) {
                return;
            }
            for (String productType : supportedProducts) {
                List<String> paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(productType);
                if (paletteTypes == null) {
                    continue;
                }
                for (String paletteType : paletteTypes) {
                    for (UnifiedJDBCBean bean : jdbcMap.values()) {
                        try {
                            // filter unsupported components
                            if (UnifiedComponentUtil.isUnsupportedComponent(definition.getName(), bean)) {
                                continue;
                            }
                            GenericComponent currentComponent = new GenericComponent(definition, paletteType,
                                    definition.getName().replace("JDBC", bean.getComponentKey()));
                            // available for jdbc avoid TDI license blacklist
                            componentsList.add(currentComponent);
                        } catch (BusinessException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }

        }

    }

    public static List<ElementParameter> getParametersFromForm(IElement element, Form form) {
        return getParametersFromForm(element, false, null, (ComponentProperties) form.getProperties(), form);
    }

    public static List<ElementParameter> getParametersFromForm(IElement element, boolean isInitializing,
            EComponentCategory category, ComponentProperties compProperties, Form form) {
        return getParametersFromForm(element, isInitializing, category, compProperties, compProperties, null, form, null, null);
    }

    /**
     * DOC ycbai Comment method "loadParametersFromForm".
     * <p>
     * Get element parameters of <code>element</code> from <code>form</code>.
     *
     * @param node optional, used if there is a component setting up the properties
     * @param element
     * @param category
     * @param form
     * @return parameters list
     */
    private static List<ElementParameter> getParametersFromForm(IElement element, boolean isInitializing,
            EComponentCategory category, ComponentProperties rootProperty, Properties compProperties,
            String parentPropertiesPath, Form form, Widget parentWidget, AtomicInteger lastRowNum) {
        List<ElementParameter> elementParameters = new ArrayList<>();
        List<String> parameterNames = new ArrayList<>();
        EComponentCategory compCategory = category;
        if (compCategory == null) {
            compCategory = EComponentCategory.BASIC;
        }
        AtomicInteger lastRN = lastRowNum;
        if (lastRN == null) {
            lastRN = new AtomicInteger();
        }
        if (form == null) {
            return elementParameters;
        }
        Properties componentProperties = compProperties;
        if (componentProperties == null) {
            componentProperties = form.getProperties();
        }
        if (element instanceof INode) {
            INode node = (INode) element;
            // FIXME - this should be able to be removed TUP-4053
            // Set the properties only one time to get the top-level properties object
            if (node.getComponentProperties() == null) {
                node.setComponentProperties(rootProperty);
            }
        }

        // Have to initialize for the messages
        Collection<Widget> formWidgets = form.getWidgets();
        boolean isDriverContextMode = false;
        for (Widget widget : formWidgets) {
            NamedThing widgetProperty = widget.getContent();

            String propertiesPath = getPropertiesPath(parentPropertiesPath, null);
            if (widgetProperty instanceof Form) {
                Form subForm = (Form) widgetProperty;
                Properties subProperties = subForm.getProperties();
                // Reset properties path
                if (!isSameComponentProperties(componentProperties, widgetProperty)) {
                    propertiesPath = getPropertiesPath(parentPropertiesPath, subProperties.getName());
                }
                elementParameters.addAll(getParametersFromForm(element, isInitializing, compCategory, rootProperty,
                        subProperties, propertiesPath, subForm, widget, lastRN));
                continue;
            }

            GenericElementParameter param = new GenericElementParameter(element, rootProperty, form, widget,
                    getComponentService());
            String parameterName = propertiesPath.concat(param.getName());
            param.setName(parameterName);
            param.setCategory(compCategory);
            param.setShow(parentWidget == null ? !widget.isHidden() : !parentWidget.isHidden() && !widget.isHidden());
            int rowNum = 0;
            if (widget.getOrder() != 1) {
                rowNum = lastRN.get();
            } else {
                rowNum = widget.getRow();
                if (parentWidget != null) {
                    rowNum += parentWidget.getRow();
                }
                rowNum = rowNum + lastRN.get();
            }
            param.setNumRow(rowNum);
            lastRN.set(rowNum);
            // handle form...

            EParameterFieldType fieldType = getFieldType(widget, widgetProperty);
            param.setFieldType(fieldType != null ? fieldType : EParameterFieldType.TEXT);
            if (widgetProperty instanceof SchemaProperty) {
                boolean found = false;
                // set a default connector
                param.setContext(EConnectionType.FLOW_MAIN.getName());
                for (Connector connector : rootProperty.getPossibleConnectors(true)) {
                    if (!(((SchemaProperty) widgetProperty).getValue() instanceof Schema)) {
                        continue;
                    }
                    if (connector instanceof PropertyPathConnector) {
                        String linkedSchema = ((PropertyPathConnector) connector).getPropertyPath() + ".schema"; //$NON-NLS-1$
                        if (parameterName.equals(linkedSchema)) {
                            found = true;
                            param.setContext(connector.getName());
                            IElementParameterDefaultValue defaultValue = new ElementParameterDefaultValue();
                            Schema schema = ((SchemaProperty) widgetProperty).getValue();
                            defaultValue.setDefaultValue(new Schema.Parser().parse(schema.toString()));
                            param.getDefaultValues().add(defaultValue);
                        }
                    }
                }
                if (!found) {
                    // check in the input schema
                    // for now we only handle input schema named MAIN. But we will name them "FLOW" to keep
                    // compatibility.
                    for (Connector connector : rootProperty.getPossibleConnectors(false)) {
                        if (!(((SchemaProperty) widgetProperty).getValue() instanceof Schema)) {
                            continue;
                        }
                        if (connector instanceof PropertyPathConnector) {
                            String linkedSchema = ((PropertyPathConnector) connector).getPropertyPath() + ".schema"; //$NON-NLS-1$
                            if (parameterName.equals(linkedSchema)) {
                                if (GenericNodeConnector.INPUT_CONNECTOR.equals(connector.getName())) {
                                    param.setContext(EConnectionType.FLOW_MAIN.getName());
                                } else {
                                    param.setContext(connector.getName());
                                }
                                IElementParameterDefaultValue defaultValue = new ElementParameterDefaultValue();
                                Schema schema = ((SchemaProperty) widgetProperty).getValue();
                                defaultValue.setDefaultValue(new Schema.Parser().parse(schema.toString()));
                                param.getDefaultValues().add(defaultValue);
                            }
                        }
                    }
                }
            }

            if (widgetProperty instanceof PresentationItem) {
                param.setValue(widgetProperty.getDisplayName());
                if (param.getName().equals("guessQueryFromSchema")) {
                    IElementParameter sibling_param = element.getElementParameter("QUERYSTORE");
                    if (sibling_param != null) {
                        sibling_param.setShow(true);
                        sibling_param.setNumRow(rowNum);
                        sibling_param.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName()).setNumRow(rowNum);
                        sibling_param.getChildParameters().get(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName())
                                .setNumRow(rowNum);
                        param.setShowIf(EParameterName.QUERYSTORE_TYPE.getName() + " =='" + AbstractBasicComponent.BUILTIN + "'");
                    }
                }
            } else if (widgetProperty instanceof Property) {
                Property property = (Property) widgetProperty;
                param.setRequired(property.isRequired());
                Object storedValue = getParameterValue(element, property, fieldType, parameterName);
                param.setValue(storedValue);
                boolean isNameProperty = IGenericConstants.NAME_PROPERTY.equals(param.getParameterName());
                boolean isEnumProperty = EParameterFieldType.CLOSED_LIST.equals(fieldType) && storedValue != null
                        && storedValue instanceof Enum;
                if (EParameterFieldType.NAME_SELECTION_AREA.equals(fieldType) || EParameterFieldType.JSON_TABLE.equals(fieldType)
                        || EParameterFieldType.CHECK.equals(fieldType) || EParameterFieldType.MAPPING_TYPE.equals(fieldType)
                        || isNameProperty || isEnumProperty) {
                    // Disable context support for those filed types and name parameter.
                    param.setSupportContext(false);
                } else {
                    param.setSupportContext(isSupportContext(property));
                }
                property.setTaggedValue(IComponentConstants.SUPPORT_CONTEXT, param.isSupportContext());
                Object cmTV = property.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE);
                boolean isContext = Boolean.valueOf(String.valueOf(cmTV));
                param.setReadOnly(isContext);
                boolean isDynamic = Boolean.valueOf(String.valueOf(property.getTaggedValue(IGenericConstants.IS_DYNAMIC)));
                param.setContextMode(isDynamic);
                List<?> values = property.getPossibleValues();
                if (values != null || EParameterFieldType.CLOSED_LIST.equals(fieldType)) {
                    if (values == null) {
                        values = Collections.emptyList();
                    }
                    param.setPossibleValues(values);
                    List<String> possVals = new ArrayList<>();
                    List<String> possValsDisplay = new ArrayList<>();
                    for (Object obj : values) {
                        String value = null;
                        String valueDisplay = null;
                        if (obj instanceof NamedThing) {
                            NamedThing nal = (NamedThing) obj;
                            value = nal.getName();
                            valueDisplay = nal.getDisplayName();
                        } else {
                            value = String.valueOf(obj);
                            valueDisplay = String.valueOf(obj);
                        }
                        String pvDisplayName = property.getPossibleValuesDisplayName(obj);
                        if (StringUtils.isNotBlank(pvDisplayName) && !"null".equals(pvDisplayName)) { //$NON-NLS-1$
                            valueDisplay = pvDisplayName;
                        }
                        possVals.add(value);
                        possValsDisplay.add(valueDisplay);
                    }
                    param.setListItemsDisplayName(possValsDisplay.toArray(new String[0]));
                    param.setListItemsDisplayCodeName(possValsDisplay.toArray(new String[0]));
                    param.setListItemsValue(possVals.toArray(new String[0]));
                }
                if (param.getName().equals("connection.driverClass")) {
                    isDriverContextMode = isContext;
                }
            } else if (fieldType != null && fieldType.equals(EParameterFieldType.TABLE) && widgetProperty instanceof Properties) {
                Properties table = (Properties) widgetProperty;
                Form mainForm = table.getForm(Form.MAIN);
                param.setDisplayName(mainForm.getTitle());
                List<ElementParameter> parameters = getParametersFromForm(new FakeElement("table"), mainForm); //$NON-NLS-1$

                // table is always empty by default
                param.setSupportContext(isSupportContext(table));
                Boolean isReadOnly = null;
                for (ElementParameter e : parameters) {
                    if (isReadOnly == null) {
                        isReadOnly = e.isReadOnly();
                    }
                    if (!e.isReadOnly()) {
                        isReadOnly = false;
                    }
                }
                param.setReadOnly(isReadOnly);

                List<String> codeNames = new ArrayList<>();
                List<String> possValsDisplay = new ArrayList<>();
                for (ElementParameter curParam : parameters) {
                    curParam.setFilter(null);
                    curParam.setContext(null);
                    curParam.setShowIf(null);
                    curParam.setNotShowIf(null);
                    curParam.setReadOnlyIf(null);
                    curParam.setNotReadOnlyIf(null);
                    curParam.setNoContextAssist(false);
                    curParam.setRaw(false);
                    curParam.setReadOnly(false);
                    fillDefaultValsForListType(curParam);
                    codeNames.add(curParam.getName());
                    possValsDisplay.add(curParam.getDisplayName());
                }
                param.setListItemsDisplayName(possValsDisplay.toArray(new String[0]));
                param.setListItemsDisplayCodeName(codeNames.toArray(new String[0]));
                param.setListItemsValue(parameters.toArray(new ElementParameter[0]));
                String[] listItemsShowIf = new String[parameters.size()];
                String[] listItemsNotShowIf = new String[parameters.size()];
                param.setListItemsShowIf(listItemsShowIf);
                param.setListItemsNotShowIf(listItemsNotShowIf);
                param.setValue(GenericTableUtils.getTableValues(table, param));
                param.setBasedOnSchema(Boolean.valueOf(String.valueOf(widget
                        .getConfigurationValue(Widget.HIDE_TOOLBAR_WIDGET_CONF))));
            }

            if (!param.isReadOnly()) {
                param.setReadOnly(widget.isReadonly() || element.isReadOnly());
            }
            // For issue TUP-16139
            if (EParameterFieldType.COMPONENT_REFERENCE.equals(fieldType) && param.getNumRow() == 2
                    && EComponentCategory.BASIC.equals(compCategory)) {
                param.setNumRow(1);
            }
            param.setSerialized(true);
            param.setDynamicSettings(true);
            // Avoid adding duplicate prameter.
            if (!parameterNames.contains(parameterName)) {
                elementParameters.add(param);
                parameterNames.add(parameterName);
            }
        }
        if (isDriverContextMode) {
            for (ElementParameter param : elementParameters) {
                if (param.getName().equals("connection.selectClass")) {
                    param.setReadOnly(isDriverContextMode);
                }
            }
        }
        return elementParameters;
    }

    private static void fillDefaultValsForListType(ElementParameter param) {
        if (param == null) {
            return;
        }
        switch (param.getFieldType()) {
        case CONTEXT_PARAM_NAME_LIST:
        case CLOSED_LIST:
        case DBTYPE_LIST:
        case COLUMN_LIST:
        case COMPONENT_LIST:
        case CONNECTION_LIST:
        case LOOKUP_COLUMN_LIST:
        case PREV_COLUMN_LIST:
            if (param.getValue() != null) {
                param.setDefaultClosedListValue(param.getValue().toString());
            }
            break;
        default:
            return;
        }
    }

    /**
     * DOC nrousseau Comment method "getNameFromConnector".
     *
     * @param connector
     * @return
     */
    public static String getNameFromConnector(Connector connector) {
        if (Connector.MAIN_NAME.equals(connector.getName())) {
            return EConnectionType.FLOW_MAIN.getName();
        } else {
            return connector.getName();
        }
    }

    /**
     * DOC ycbai Comment method "getRelatedParameters".
     * <p>
     * Get all element parameters related to the <code>parameter<code>. For example the paramters from the form
     * associated with PresentationItem type.
     *
     * @param parameters
     * @return
     */
    public static List<ElementParameter> getRelatedParameters(GenericElementParameter parameter) {
        List<ElementParameter> params = new ArrayList<>();
        if (parameter == null) {
            return params;
        }
        Widget widget = parameter.getWidget();
        NamedThing content = widget.getContent();
        if (content instanceof PresentationItem) {
            PresentationItem pi = (PresentationItem) content;
            Form formtoShow = pi.getFormtoShow();
            List<ElementParameter> parametersFromForm = getParametersFromForm(parameter.getElement(), false,
                    parameter.getCategory(), parameter.getRootProperties(), formtoShow);
            params.addAll(parametersFromForm);
        }
        return params;
    }

    public static Object getParameterValue(IElement element, Property property, EParameterFieldType fieldType,
            String parameterName) {
        Object paramValue = property.getStoredValue();
        if (paramValue instanceof List) {
            if (ContextParameterUtils.isContainContextParam(String.valueOf(paramValue))) {
                return ((List) paramValue).get(0);
            } else {
                return null;
            }
        }
        if (fieldType == EParameterFieldType.CLOSED_LIST) {
            if (paramValue == null) {// TUP-4145
                List<?> possibleValues = property.getPossibleValues();
                if (possibleValues != null && possibleValues.size() > 0) {
                    paramValue = possibleValues.get(0);
                    property.setValue(paramValue);
                }
            }
        } else if (GenericTypeUtils.isStringType(property)) {
            boolean needInitializeProperty = false;
            IElementParameter oldParam = null;
            if (element.getElementParameters() != null) {
                oldParam = element.getElementParameter(parameterName);
            }
            if (oldParam == null || oldParam.getValue() == null
                    || !StringUtils.equals((String) oldParam.getValue(), (String) property.getStoredValue())) {
                // if parameter is not setup yet (= initialization)
                // then we set the value and check if we need to add quotes.
                //
                // if parameter value / property value are not the same
                // then the component updated the property in it's own code, so we need to add quotes/initialize.
                needInitializeProperty = true;
            }

            String value = (String) paramValue;
            // If property is not initialized by client and value is not context mode and is not in wizard then add
            // double quotes.
            if (needInitializeProperty && !(element instanceof FakeElement || ContextParameterUtils.isContainContextParam(value))) {
                if (value == null) {
                    value = StringUtils.EMPTY;
                }
                if(fieldType == EParameterFieldType.MEMO_SQL){
                	value = StringUtils.trimToEmpty(value);
                }else{
                	value = unescapeForJava(value);
                }
                paramValue = TalendQuoteUtils.addPairQuotesIfNotExist(value);
                property.setValue(paramValue);
            }
        } else if (GenericTypeUtils.isBooleanType(property)) {
            if (paramValue == null) {
                paramValue = Boolean.FALSE;
                property.setValue(paramValue);
            }
        }
        return paramValue;
    }

    public static String unescapeForJava(String input) {
        CharSequenceTranslator UNESCAPE_JAVA = new AggregateTranslator(new OctalUnescaper(), new UnicodeUnescaper(),
                new LookupTranslator(new String[][] { { "\\\\", "\\" }, { "\\\"", "\"" }, { "\\'", "'" } })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        return UNESCAPE_JAVA.translate(input);
    }

    private static String getPropertiesPath(String parentPropertiesPath, String currentPropertiesName) {
        String propertiesPath = ""; //$NON-NLS-1$
        if (StringUtils.isNotBlank(currentPropertiesName)) {
            propertiesPath = propertiesPath.concat(currentPropertiesName).concat(IGenericConstants.EXP_SEPARATOR);
        }
        if (StringUtils.isNotBlank(parentPropertiesPath)) {
            propertiesPath = parentPropertiesPath.concat(propertiesPath);
        }
        return propertiesPath;
    }

    /**
     * DOC ycbai Comment method "getFieldType".
     *
     * @param widget
     * @param widgetProperty
     * @param param
     * @param se
     * @return
     */
    private static EParameterFieldType getFieldType(Widget widget, NamedThing widgetProperty) {
        return WidgetFieldTypeMapper.getFieldType(widget, widgetProperty);
    }

    public static Properties getCurrentProperties(Properties componentProperties, String paramName) {
        if (componentProperties == null || paramName == null) {
            return null;
        }
        String compPropertiesPath = getPropertyPath(paramName);
        if (StringUtils.isEmpty(compPropertiesPath)) {
            return componentProperties;
        }
        NamedThing property = componentProperties.getProperty(compPropertiesPath);
        if (property == null) {
            return getCurrentPropertiesSpecial(componentProperties, paramName);
        }
        if (property instanceof Properties) {
            return (Properties) property;
        }
        return null;
    }

    private static Properties getCurrentPropertiesSpecial(Properties componentProperties, String paramName) {
        Properties currentComponentProperties = null;
        if (componentProperties == null || paramName == null) {
            return null;
        }
        List<NamedThing> allProps = componentProperties.getProperties();
        for (NamedThing namedThing : allProps) {
            if (paramName.equals(namedThing.getName())) {
                currentComponentProperties = componentProperties;
                break;
            }
            if (namedThing instanceof Properties) {
                Properties childComponentProperties = (Properties) namedThing;
                currentComponentProperties = getCurrentProperties(childComponentProperties, paramName);
            }
            if (currentComponentProperties != null) {
                break;
            }
        }
        return currentComponentProperties;
    }

    public static NamedThing getGenericSchemaElement(ComponentProperties componentProperties, String paramName) {
        if (componentProperties == null || paramName == null) {
            return null;
        }
        return componentProperties.getProperty(paramName);
    }

    public static Object getGenericPropertyValue(ComponentProperties componentProperties, String paramName) {
        if (componentProperties == null || paramName == null) {
            return null;
        }
        Properties currentProperties = getCurrentProperties(componentProperties, paramName);
        if (currentProperties == null) {
            return null;
        }
        Property property = componentProperties.getValuedProperty(paramName);
        if (property != null) {
            return property.getStoredValue();
        }
        return null;
    }

    public static void setGenericPropertyValue(ComponentProperties componentProperties, String paramName, Object value) {
        if (componentProperties == null || paramName == null) {
            return;
        }
        Properties currentProperties = getCurrentProperties(componentProperties, paramName);
        if (currentProperties == null) {
            return;
        }
        currentProperties.setValue(getPropertyName(paramName), value);
    }

    /**
     * looks for all Property type properties of the given properties and all is nested Properties.
     */
    public static List<Property> getAllValuedProperties(Properties properties) {
        List<Property> allValuedProperties = new ArrayList<>();
        if (properties == null) {
            return Collections.EMPTY_LIST;
        }
        List<NamedThing> namedThings = properties.getProperties();
        for (NamedThing namedThing : namedThings) {
            if (namedThing instanceof Properties) {
                Properties childComponentProperties = (Properties) namedThing;
                allValuedProperties.addAll(getAllValuedProperties(childComponentProperties));
            } else if (namedThing instanceof Property) {
                allValuedProperties.add((Property) namedThing);
            } // else other type not yet handled
        }
        return allValuedProperties;
    }

    private static String getPropertyPath(String paramName) {
        String propertyPath = ""; //$NON-NLS-1$
        if (paramName.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
            propertyPath = paramName.substring(0, paramName.lastIndexOf(IGenericConstants.EXP_SEPARATOR));
        }
        return propertyPath;
    }

    public static String getPropertyName(String paramName) {
        String propertyName = paramName;
        if (propertyName.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
            propertyName = propertyName.substring(propertyName.lastIndexOf(IGenericConstants.EXP_SEPARATOR) + 1);
        }
        return propertyName;
    }

    public static JavaType getTalendTypeFromProperty(Property<?> property) {
        if (GenericTypeUtils.isStringType(property)) {
            return JavaTypesManager.STRING;
        } else if (GenericTypeUtils.isIntegerType(property)) {
            return JavaTypesManager.INTEGER;
        } else if (GenericTypeUtils.isLongType(property)) {
            return JavaTypesManager.LONG;
        } else if (GenericTypeUtils.isFloatType(property)) {
            return JavaTypesManager.FLOAT;
        } else if (GenericTypeUtils.isDoubleType(property)) {
            return JavaTypesManager.DOUBLE;
        } else if (GenericTypeUtils.isBooleanType(property)) {
            return JavaTypesManager.BOOLEAN;
        } else {
            return JavaTypesManager.STRING;
        }
    }

    public static boolean isSupportContext(Property<?> property) {
        if (GenericTypeUtils.isSchemaType(property)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isSupportContext(Properties properties) {
        for (NamedThing thing : properties.getProperties()) {
            if (thing instanceof Property) {
                if (GenericTypeUtils.isSchemaType((Property) thing)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSameComponentProperties(Properties componentProperties, NamedThing widgetProperty) {
        if (componentProperties != null && widgetProperty instanceof Form) {
            Form subForm = (Form) widgetProperty;
            if (subForm != null) {
                return componentProperties == subForm.getProperties();
            }
        }
        return false;
    }

    public static ComponentProperties getComponentPropertiesFromSerialized(String serialized, Connection connection) {
        return getComponentPropertiesFromSerialized(serialized, connection, true);
    }

    public static ComponentProperties getComponentPropertiesFromSerialized(String serialized, Connection connection,
            boolean withEvaluator) {
        if (serialized != null) {
            SerializerDeserializer.Deserialized<ComponentProperties> fromSerialized = Properties.Helper.fromSerializedPersistent(
                    serialized, ComponentProperties.class, new PostDeserializeSetup() {

                        @Override
                        public void setup(Object properties) {
                            if (withEvaluator) {
                                ((Properties) properties)
                                        .setValueEvaluator(new MetadataContextPropertyValueEvaluator(connection));
                            }
                        }
                    });
            if (fromSerialized != null) {
                return fromSerialized.object;
            }
        }
        return null;
    }

    /**
     * Utilitary function to set a connectionProperty of a given type to a node.
     *
     * @param currentType type of the connection
     * @param node currentNode
     */
    private static void setConnectionProperty(EConnectionType currentType, INodeConnector node) {
        // One line method that factorize a lot of code.
        node.addConnectionProperty(currentType, currentType.getRGB(), currentType.getDefaultLineStyle());
    }

    /**
     * Get formal possible values of the <code>param</code>. Every possible value will be {@link NamedThing} type.
     *
     * @param param
     * @return
     */
    public static List<NamedThing> getFormalPossibleValues(GenericElementParameter param) {
        List<NamedThing> nals = new ArrayList<>();
        if (param == null) {
            return nals;
        }
        List<?> possibleValues = param.getPossibleValues();
        if (possibleValues != null) {
            for (Object object : possibleValues) {
                if (object instanceof NamedThing) {
                    nals.add((NamedThing) object);
                } else if (object instanceof String) {
                    String name = (String) object;
                    Property property = param.getProperty();
                    if (property != null) {
                        NamedThing nl = new SimpleNamedThing(name, property.getPossibleValuesDisplayName(name));
                        nals.add(nl);
                    }
                }
            }
        }
        return nals;
    }

    public static ComponentReferenceProperties getReferencedComponent(IElementParameter refPara){
        if (!(refPara instanceof GenericElementParameter)) {
            return null;
        }
        Widget widget = ((GenericElementParameter) refPara).getWidget();
        NamedThing widgetProperty = widget.getContent();
        if (widgetProperty instanceof ComponentReferenceProperties
                && Widget.COMPONENT_REFERENCE_WIDGET_TYPE.equals(widget.getWidgetType())) {
            ComponentReferenceProperties props = (ComponentReferenceProperties) widgetProperty;
            return props;
        }
        return null;
    }

    public static void initReferencedComponent(IElementParameter refPara, String newValue) {

        if (!(refPara instanceof GenericElementParameter)) {
            return;
        }
        Widget widget = ((GenericElementParameter) refPara).getWidget();
        NamedThing widgetProperty = widget.getContent();
        if (widgetProperty instanceof ComponentReferenceProperties
                && Widget.COMPONENT_REFERENCE_WIDGET_TYPE.equals(widget.getWidgetType())) {
            IElementParameter propertyParameter = refPara.getElement().getElementParameterFromField(
                    EParameterFieldType.PROPERTY_TYPE);
            ComponentReferenceProperties props = (ComponentReferenceProperties) widgetProperty;
            if (newValue == null || newValue.toString().length() <= 0) {
                props.referenceType.setValue(ComponentReferenceProperties.ReferenceType.THIS_COMPONENT);
                props.componentInstanceId.setValue(null);
                props.setReference(null);
                propertyParameter.setShow(true);
            } else {
                props.referenceType.setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
                props.componentInstanceId.setValue(newValue);
                if (refPara.getElement() != null && refPara.getElement() instanceof INode) {
                    INode node = (INode) refPara.getElement();
                    for (INode refNode : node.getProcess().getGraphicalNodes()) {
                        if (refNode.getUniqueName().equals(newValue)) {
                            props.setReference(refNode.getComponentProperties());
                        }
                    }
                }
                propertyParameter.setShow(false);
            }
        }
    }

    public static void setPropertiesPepositoryValue(ComponentProperties componentProperties, String paramName) {
        Property<?> property = componentProperties.getValuedProperty(paramName);
        if (property != null) {
            property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, paramName);
        } else {
            Properties currentProperties = componentProperties.getProperties(paramName);
            if (currentProperties != null) {
                for (NamedThing thing : currentProperties.getProperties()) {
                    if (thing instanceof Property) {
                        ((Property) thing).setTaggedValue(IGenericConstants.REPOSITORY_VALUE, paramName);
                    }
                }
            }
        }

    }

    public static NamedThing getNameThingFromComponentPropertiesByName(Properties properties, String name) {
        if (properties == null || StringUtils.isBlank(name)) {
            return null;
        }
        NamedThing nameThing = null;
        for (NamedThing thing : properties.getProperties()) {
            if (name.equals(thing.getName())) {
                nameThing = thing;
                break;
            }
            if (thing instanceof Properties) {
                Properties childProperties = (Properties) thing;
                nameThing = getNameThingFromComponentPropertiesByName(childProperties, name);
                if (nameThing != null) {
                    break;
                }
            }
        }
        return nameThing;
    }
}
