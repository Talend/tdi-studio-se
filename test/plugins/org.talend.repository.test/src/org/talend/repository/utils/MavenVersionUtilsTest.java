package org.talend.repository.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.maven.MavenConstants;

public class MavenVersionUtilsTest {

    private Property property;

    @Before
    public void setUp() throws Exception {
        property = PropertiesFactory.eINSTANCE.createProperty();

    }

    @Test
    public void testIsValidMavenVersion() {
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.0.0"));
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.1.0.0-SNAPSHOT"));
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.0-SNAPSHOT"));
        assertFalse(MavenVersionUtils.isValidMavenVersion("1.0 SNAPSHOT"));
        assertFalse(MavenVersionUtils.isValidMavenVersion("1-SNAPSHOT"));
    }

    @Test
    public void testGetItemMavenVersion() {
        assertEquals("", MavenVersionUtils.getItemMavenVersion(property));
        property.getAdditionalProperties().put(MavenConstants.NAME_USER_VERSION, "1.0.0");
        assertEquals("1.0.0", MavenVersionUtils.getItemMavenVersion(property));
    }

    @Test
    public void testSetItemMavenVersion() {
        MavenVersionUtils.setItemMavenVersion(property, "1.1.0");
        assertEquals("1.1.0", (String) property.getAdditionalProperties().get(MavenConstants.NAME_USER_VERSION));
    }

    @Test
    public void testIsVersioningType() {
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS));
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_MR));
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_STORM));
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_ROUTE));
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.TEST_CONTAINER));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.JOBLET));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.ROUTINES));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_ROUTELET));
    }

    @Test
    public void testIsHasSubjobType() {
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS));
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_MR));
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_ROUTE));
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.TEST_CONTAINER));
        assertFalse(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.JOBLET));
        assertFalse(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_ROUTELET));
    }

}
