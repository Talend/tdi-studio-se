package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.avro.Schema;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.properties.Item;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.metadata.MetadataEmfFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class NewMarkLogicMigrationTask extends NewComponentFrameworkMigrationTask {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private Item item;

    @Override
    public ExecutionResult execute(final Item item) {
        this.item = item;
        return super.execute(item);
    }

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 10, 21, 16, 0, 0).getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewMarkLogicMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    /*
     * tMarkLogicInput (in tcomp) has 2 new properties: checkbox 'criteriaSearch' (should be true in criteria mode, when
     * component doesn't have input connection), input schema, which is output schema of source component (if exists)
     * Also need to migrate pageSize due to previously it supported text values and now only numeric
     * 
     * tMarkLogicBulkLoad has 1 new property: useExternalMLCP (when true - call commandline process as before).
     */
    @Override
    protected void processUnmappedElementParameter(Properties props, NodeType nodeType, GenericElementParameter param,
            NamedThing currNamedThing) {
        super.processUnmappedElementParameter(props, nodeType, param, currNamedThing);

        if (currNamedThing instanceof Property) {
            List<ConnectionType> allInputMainConnectors = getAllInputMainConnectors(nodeType);
            String namedThingName = currNamedThing.getName();
            switch (namedThingName) {
            case "criteriaSearch":
                setUseCriteriaMode(currNamedThing, allInputMainConnectors.isEmpty());
                break;
            case "schema":
                setInputSchema(nodeType, currNamedThing, allInputMainConnectors);
                break;
            case "useExternalMLCP":
                setUseExternalMLCPProperty(currNamedThing);
                break;
            case "pageSize":
                setNumericPageSize(nodeType, currNamedThing);
                break;
            }
        }
    }

    private void setNumericPageSize(NodeType node, NamedThing property) {
        String oldValue = ComponentUtilities.getNodeProperty(node, "PAGE_SIZE").getValue();
        Property<Integer> pageSizeProperty = (Property<Integer>) property;
        if (oldValue != null && !oldValue.isEmpty() && !isContextOrGlobalMapValue(oldValue)) {
            try {
                pageSizeProperty.setValue(Integer.valueOf(oldValue.replaceAll("\"", "")));
            } catch (NumberFormatException e) {
                pageSizeProperty.setValue(DEFAULT_PAGE_SIZE);
            }
        } else if (isContextOrGlobalMapValue(oldValue)) {
            pageSizeProperty.setStoredValue(oldValue); // remain globalMap or context value
        } else {
            pageSizeProperty.setValue(DEFAULT_PAGE_SIZE);
        }
    }

    private boolean isContextOrGlobalMapValue(String oldValue) {
        return oldValue.contains("context") || oldValue.contains("globalMap");
    }

    private void setUseCriteriaMode(NamedThing currNamedThing, boolean noInputMainConnections) {
        // set due to input connection
        Property<Boolean> criteriaSearchProperty = (Property<Boolean>) currNamedThing;
        criteriaSearchProperty.setValue(noInputMainConnections);
    }

    private void setInputSchema(NodeType nodeType, NamedThing currNamedThing, List<ConnectionType> allInputMainConnectors) {
        // set input schema from input component OR empty schema
        if (!allInputMainConnectors.isEmpty()) {
            NodeType sourceNode = getSourceNode(allInputMainConnectors, nodeType);
            if (sourceNode != null) {
                // "old schemas" of input component
                List<MetadataType> sourceNodeMetadatas = sourceNode.getMetadata();
                MetadataType sourceMetadata = getAppropriateMetadata(sourceNodeMetadatas, allInputMainConnectors);

                ((Property<Schema>) currNamedThing).setValue(createSchemaFromMetadata(sourceMetadata));
            }
        }
    }

    private void setUseExternalMLCPProperty(NamedThing property) {
        Property<Boolean> useExternalMLCP = (Property<Boolean>) property;
        useExternalMLCP.setValue(true);
    }

    private boolean schmemaIsEmpty(NamedThing schemaProperty) {
        return ((Property<Schema>) schemaProperty).getValue().getFields().size() == 0;
    }

    private List<ConnectionType> getAllInputMainConnectors(NodeType nodeType) {
        List<ConnectionType> allInputConnectors = ComponentUtilities.getNodeInputConnections(nodeType);
        Iterator<ConnectionType> connectionIter = allInputConnectors.iterator();
        while (connectionIter.hasNext()) {
            ConnectionType nextConnector = connectionIter.next();
            if (!"FLOW".equals(nextConnector.getConnectorName())) {
                connectionIter.remove();
            }
        }

        return allInputConnectors;
    }

    private NodeType getSourceNode(List<ConnectionType> mainConnectors, NodeType target) {
        String sourceNodeUniqueName = null;
        if (!mainConnectors.isEmpty()) {
            // could be only 1 input main connector
            sourceNodeUniqueName = mainConnectors.get(0).getSource();
        }

        List<NodeType> allNodes = getProcessType(item).getNode();
        for (NodeType node : allNodes) {
            if (sourceNodeUniqueName.equals(ComponentUtilities.getNodeUniqueName(node))) {
                return node;
            }
        }
        return null;
    }

    private static boolean isConnectorPresent(List<ConnectionType> connectors, String metadataConnectorName) {
        for (ConnectionType connection : connectors) {
            if (connection.getMetaname().equalsIgnoreCase(metadataConnectorName)) {
                return true;
            }
        }
        return false;
    }

    private Schema createSchemaFromMetadata(MetadataType metadata) {
        MetadataEmfFactory factory = new MetadataEmfFactory();
        factory.setMetadataType(metadata);

        IMetadataTable metadataTable = factory.getMetadataTable();

        return SchemaUtils.convertTalendSchemaIntoComponentSchema(ConvertionHelper.convert(metadataTable));
    }

    private MetadataType getAppropriateMetadata(List<MetadataType> allSourceNodeMetadatas,
            List<ConnectionType> allInputMainConnectors) {
        MetadataType sourceMetadata = null;
        if (allSourceNodeMetadatas.size() == 1) {
            sourceMetadata = allSourceNodeMetadatas.get(0);
        } else if (allSourceNodeMetadatas.size() >= 2) {
            for (MetadataType metadata : allSourceNodeMetadatas) {
                String connectorName = metadata.getName();
                if (isConnectorPresent(allInputMainConnectors, connectorName)) {
                    sourceMetadata = metadata;
                    break;
                }
            }
        }
        return sourceMetadata;
    }

    @Override
    protected ElementParameterType getParameterType(NodeType node, String paramName) {
        ElementParameterType paramType = ParameterUtilTool.findParameterType(node, paramName);
        if (node != null && paramType != null) {
            Object value = ParameterUtilTool.convertParameterValue(paramType);
            if ("ACTION_TYPE".equals(paramName)) {
                if ("INSERT".equals(value)) {
                    paramType.setValue("UPSERT");
                }
            } else if ("DOC_TYPE".equals(paramName)) {
                if ("GENERIC".equals(value)) {
                    paramType.setValue("MIXED");
                } else if ("TEXT".equals(value)) {
                    paramType.setValue("PLAIN_TEXT");
                }
            }
        }
        return paramType;
    }
}
