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
package org.talend.sdk.component.studio.model.parameter;

import java.util.List;
import java.util.Optional;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * SchemaElementParameter for Component output schema.
 * It stores outgoing metadata in this Component Node
 */
public class OutputSchemaParameter extends SchemaElementParameter {

    public OutputSchemaParameter(final IElement element, final String name, final String connectionName,
                                 final String discoverSchema, final boolean show) {
        super(element);

        setName(name);
        setDisplayName(DISPLAY_NAME);
        setCategory(EComponentCategory.BASIC);
        setFieldType(EParameterFieldType.SCHEMA_TYPE);
        setShow(show);
        setReadOnly(false);
        setRequired(true);
        setContext(connectionName);

        createSchemaType(name, connectionName, show);
        createRepository(show);
        createGuessSchema(name, connectionName, discoverSchema, show);

        setTaggedValue(CONNECTION_TYPE, PropertyDefinitionDecorator.Connection.Type.OUT.toString());
    }

    private void createSchemaType(final String name, final String connectionName, final boolean show) {
        final ElementParameter schemaType = new ElementParameter(getElement());
        schemaType.setCategory(EComponentCategory.BASIC);
        schemaType.setContext(EConnectionType.FLOW_MAIN.getName());
        schemaType.setDisplayName(schemaDisplayName(connectionName, name));
        schemaType.setFieldType(EParameterFieldType.TECHNICAL);
        schemaType.setListItemsDisplayCodeName(new String[]{"BUILT_IN", "REPOSITORY"});
        schemaType.setListItemsDisplayName(new String[]{"Built-In", "Repository"});
        schemaType.setListItemsValue(new String[]{"BUILT_IN", "REPOSITORY"});
        schemaType.setName(EParameterName.SCHEMA_TYPE.getName());
        schemaType.setNumRow(1);
        schemaType.setParentParameter(this);
        schemaType.setShow(show);
        schemaType.setShowIf("SCHEMA =='REPOSITORY'");
        schemaType.setValue("BUILT_IN");
        this.getChildParameters().put(EParameterName.SCHEMA_TYPE.getName(), schemaType);
    }

    private void createRepository(final boolean show) {
        final ElementParameter repository = new ElementParameter(getElement());
        repository.setCategory(EComponentCategory.BASIC);
        repository.setContext(EConnectionType.FLOW_MAIN.getName());
        repository.setDisplayName("Repository");
        repository.setFieldType(EParameterFieldType.TECHNICAL);
        repository.setListItemsDisplayName(new String[0]);
        repository.setListItemsValue(new String[0]);
        repository.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
        repository.setParentParameter(this);
        repository.setRequired(true);
        repository.setShow(show);
        repository.setShowIf("SCHEMA =='REPOSITORY'");
        repository.setValue("");
        this.getChildParameters().put(EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), repository);
    }

    private void createGuessSchema(final String name, final String connectionName, final String discoverSchemaAction,
                                   final boolean show) {
        if (canAddGuessSchema(connectionName)) {
            final TaCoKitElementParameter guessSchemaParameter = new TaCoKitElementParameter(getElement());
            guessSchemaParameter.setCategory(EComponentCategory.BASIC);
            guessSchemaParameter.setContext(connectionName);
            guessSchemaParameter.updateValueOnly(discoverSchemaAction);
            guessSchemaParameter.setDisplayName(Messages.getString("guessSchema.button", connectionName));
            guessSchemaParameter.setFieldType(EParameterFieldType.TACOKIT_GUESS_SCHEMA);
            guessSchemaParameter.setListItemsDisplayName(new String[0]);
            guessSchemaParameter.setListItemsValue(new String[0]);
            guessSchemaParameter.setName(guessButtonName(name));
            guessSchemaParameter.setParentParameter(this);
            guessSchemaParameter.setReadOnly(false);
            guessSchemaParameter.setRequired(false);
            guessSchemaParameter.setShow(show);
            guessSchemaParameter.putInfo(TaCoKitConst.ADDITIONAL_PARAM_METADATA_ELEMENT, this);
        }
    }

    private boolean canAddGuessSchema(final String connectorName) {
        if (TaCoKitUtil.isBlank(connectorName)) {
            return false;
        }
        boolean canAddGuessSchema = false;
        final IElement node = getElement();
        if (node instanceof Node) {
            boolean hasOutputConnector = false;
            final List<? extends INodeConnector> listConnector = ((Node) node).getListConnector();
            if (listConnector != null) {
                for (final INodeConnector connector : listConnector) {
                    if (connectorName.equals(connector.getName())) {
                        if (0 < connector.getMaxLinkOutput()) {
                            hasOutputConnector = true;
                            // input and output connectors may have same name
                            break;
                        }
                    }
                }
            }
            canAddGuessSchema = hasOutputConnector;
        }
        return canAddGuessSchema;
    }

    // TODO i18n it
    // TODO Refactor it
    private String schemaDisplayName(final String connectionName, final String schemaName) {
        final String connectorName = connectionName.equalsIgnoreCase(EConnectionType.FLOW_MAIN.getName())
                ? EConnectionType.FLOW_MAIN.getDefaultLinkName()
                : connectionName;
        if ("REJECT".equalsIgnoreCase(connectionName)) {
            return "Reject Schema";
        }
        // TODO remove $$
        if (schemaName.contains("$$")) {
            final String type = schemaName.substring(0, schemaName.indexOf("$$"));
            if ("OUT".equalsIgnoreCase(type)) {
                return "Output Schema" + "(" + connectorName + ")";
            }
            if ("IN".equalsIgnoreCase(type)) {
                return "Input Schema" + "(" + connectorName + ")";
            }
        }
        return "Schema" + "(" + connectorName + ")";
    }

    /**
     * Gets metadata from this Component node
     *
     * @return metedata
     */
    protected Optional<IMetadataTable> getMetadata() {
        IElement elem = getElement();
        if (elem == null || !(elem instanceof Node)) {
            return Optional.empty();
        }
        final IMetadataTable metadata = ((Node) elem).getMetadataFromConnector(getContext());
        return Optional.ofNullable(metadata);
    }
}
