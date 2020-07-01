package org.talend.sdk.component.studio.ui.wizard.page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.PropertyTreeCreator;
import org.talend.sdk.component.studio.model.parameter.SettingVisitor;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;

public class TaCoKitPageBuildHelper {

    private final SettingVisitor settingsCreator;

    private final PropertyNode root;

    private final List<IElementParameter> parameters = new ArrayList<>();

    public TaCoKitPageBuildHelper(TaCoKitConfigurationRuntimeData runtimeData) {
        final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
        root = new PropertyTreeCreator(new WizardTypeMapper()).createPropertyTree(configTypeNode);
        FakeElement element = new FakeElement(runtimeData.getTaCoKitRepositoryNode().getConfigTypeNode().getDisplayName());
        element.setReadOnly(runtimeData.isReadonly());
        final ElementParameter updateParameter = createUpdateComponentsParameter(element);
        this.parameters.add(updateParameter);

        final DummyComponent component = new DummyComponent(configTypeNode.getDisplayName());
        final DataNode node = new DataNode(component, component.getName());
        this.settingsCreator = new SettingVisitor(node, updateParameter, configTypeNode);
    }

    public void infer(String form) {
        EComponentCategory category = Metadatas.MAIN_FORM.equals(form) ? EComponentCategory.BASIC : EComponentCategory.ADVANCED;
        this.settingsCreator.withCategory(category);
        root.accept(settingsCreator, form);
    }

    public void terminate() {
        this.parameters.addAll(settingsCreator.getSettings());
    }

    public List<IElementParameter> getParameters(final String form) {
        return parameters.stream()
                .filter((IElementParameter p) -> this.filterForParam(p, form))
                .collect(Collectors.toList());
    }

    private boolean filterForParam(IElementParameter param, String form) {
        if (!(param instanceof TaCoKitElementParameter)) {
            return true;
        }
        final TaCoKitElementParameter tck = (TaCoKitElementParameter) param;
        return tck.getForm() == null || tck.getForm().equals(form);
    }

    /**
     * Creates and adds {@link EParameterName#UPDATE_COMPONENTS} parameter
     * This parameter is used to decide whether UI should be redrawn during Composite refresh
     */
    // TODO it is duplicated in ElementParameterCreator. Refactor to avoid duplication
    private ElementParameter createUpdateComponentsParameter(final Element element) {
        final ElementParameter parameter = new ElementParameter(element);
        parameter.setName(EParameterName.UPDATE_COMPONENTS.getName());
        parameter.setValue(false);
        parameter.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        return parameter;
    }
}
