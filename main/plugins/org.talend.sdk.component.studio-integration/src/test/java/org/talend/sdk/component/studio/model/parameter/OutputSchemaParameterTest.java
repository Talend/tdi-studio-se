package org.talend.sdk.component.studio.model.parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.IActionParameter;

class OutputSchemaParameterTest {

    private static final String CONNECTOR_NAME = "FLOW";

    @Test
    void testCreateActionParameter() {
        final Node nodeMock = mockNode(metadata());
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        final IActionParameter actionParameter = parameter.createActionParameter("param");
        final Collection<Pair<String, String>> parameters = actionParameter.parameters();

        assertEquals(2, parameters.size());
        final Iterator<Pair<String, String>> iterator = parameters.iterator();
        assertEquals(new Pair<String, String>("param[0]", "c1"), iterator.next());
        assertEquals(new Pair<String, String>("param[1]", "c2"), iterator.next());
    }

    @Test
    void testGetValue() {
        final Node nodeMock = mockNode(metadata());
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        assertEquals(Arrays.asList("c1", "c2"), parameter.getValue());
    }

    @Test
    void testGetValueNoMetadata() {
        final Node nodeMock = mockNode(null);
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        assertEquals(Collections.emptyList(), parameter.getValue());
    }

    @Test
    void testGetStringValue() {
        final Node nodeMock = mockNode(metadata());
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        assertEquals("[c1, c2]", parameter.getStringValue());
    }

    @Test
    void testGetStringValueNoMetadata() {
        final Node nodeMock = mockNode(null);
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        assertEquals("[]", parameter.getStringValue());
    }

    @Test
    void testSetValue() {
        final IMetadataTable metadata = new MetadataTable();
        final Node nodeMock = mockNode(metadata);
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());

        final List<String> schema = Arrays.asList("c1", "c2", "c3");
        parameter.setValue(schema);

        assertEquals(3, metadata.getListColumns().size());
        final List<String> actualLabels = metadata.getListColumns().stream()
                .map(IMetadataColumn::getLabel)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("c1", "c2", "c3"), actualLabels);

        final List<String> actualDbColumnNames = metadata.getListColumns().stream()
                .map(IMetadataColumn::getOriginalDbColumnName)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("c1", "c2", "c3"), actualDbColumnNames);

        final List<String> actualTypes = metadata.getListColumns().stream()
                .map(IMetadataColumn::getTalendType)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("id_String", "id_String", "id_String"), actualTypes);
    }

    /**
     * Check there is no exception in case of MetadataTable is missed
     */
    @Test
    void testSetValueNoMetadata() {
        final Node nodeMock = mockNode(null);
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());

        final List<String> schema = Arrays.asList("c1", "c2", "c3");
        parameter.setValue(schema);
    }

    @Test
    void testIsPersisted() {
        final OutputSchemaParameter parameter = new OutputSchemaParameter(null, "schema", CONNECTOR_NAME, null, true, Collections.emptyList());
        Assertions.assertTrue(parameter.isPersisted());
    }

    @Test
    void testGuessButtonName() {
        assertEquals("Guess Schema_config.datastore.dataset", OutputSchemaParameter.guessButtonName("config.datastore.dataset"));
    }

    private Node mockNode(final IMetadataTable metadata) {
        final Node nodeMock = mock(Node.class);
        when(nodeMock.getMetadataFromConnector(CONNECTOR_NAME)).thenReturn(metadata);
        return nodeMock;
    }

    private IMetadataTable metadata() {
        final IMetadataTable metadata = new MetadataTable();
        final List<IMetadataColumn> columns = new ArrayList<>();

        final IMetadataColumn c1 = new MetadataColumn();
        c1.setLabel("c1");
        columns.add(c1);

        final IMetadataColumn c2 = new MetadataColumn();
        c2.setLabel("c2");
        columns.add(c2);
        metadata.setListColumns(columns);
        return metadata;
    }
}