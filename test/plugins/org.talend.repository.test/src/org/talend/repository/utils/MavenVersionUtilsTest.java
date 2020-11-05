package org.talend.repository.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertFalse(MavenVersionUtils.isValidMavenVersion(null, true));
        assertFalse(MavenVersionUtils.isValidMavenVersion("  ", true));
        
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.0.0", true));
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.0.0", false));
        
        assertFalse(MavenVersionUtils.isValidMavenVersion("1.0", true));
        assertFalse(MavenVersionUtils.isValidMavenVersion("1.0", false));
        
        assertTrue(MavenVersionUtils.isValidMavenVersion("1.0.1.release", false));
        assertFalse(MavenVersionUtils.isValidMavenVersion("1.0.1.release", true));
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
        assertTrue(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_ROUTE));
        ERepositoryObjectType serviceType = ERepositoryObjectType.valueOf("SERVICES");
        if (serviceType != null) {
            assertTrue(MavenVersionUtils.isVersioningType(serviceType));
        }
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.TEST_CONTAINER));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.JOBLET));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.ROUTINES));
        assertFalse(MavenVersionUtils.isVersioningType(ERepositoryObjectType.PROCESS_ROUTELET));
    }

    @Test
    public void testIsHasSubjobType() {
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS));
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_MR));
        assertTrue(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_ROUTE));
        assertFalse(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.TEST_CONTAINER));
        assertFalse(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.JOBLET));
        assertFalse(MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.PROCESS_ROUTELET));
    }
    
    @Test
    public void testGetDefaultVersion() {
        assertEquals("", MavenVersionUtils.getDefaultVersion(null));
        assertEquals("a.b", MavenVersionUtils.getDefaultVersion("a.b"));
        assertEquals("1.1.0", MavenVersionUtils.getDefaultVersion("1.1"));
        assertEquals("1.0.0", MavenVersionUtils.getDefaultVersion("1.0.0"));
        
    }
    
    @Test
    public void testGetAndSetItemUseSnapshot() {
        MavenVersionUtils.setItemUseSnapshot(property, Boolean.toString(Boolean.TRUE));
        assertTrue(property.getAdditionalProperties().containsKey(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT));
        assertTrue(MavenVersionUtils.isItemUseSnapshot(property));
    }

    @Test
    public void testCompareVersion() {
        assertEquals(1, MavenVersionUtils.compareVersion("2.0.0", "1.0.0"));
        assertEquals(1, MavenVersionUtils.compareVersion("1.2.0", "1.1.0"));
        assertEquals(1, MavenVersionUtils.compareVersion("1.0.2", "1.0.1"));
        assertEquals(0, MavenVersionUtils.compareVersion("1.0.0", "1.0.0"));
        assertEquals(-1, MavenVersionUtils.compareVersion("1.0.0", "2.0.0"));
        assertEquals(-1, MavenVersionUtils.compareVersion("1.1.0", "1.2.0"));
        assertEquals(-1, MavenVersionUtils.compareVersion("1.0.1", "1.0.2"));
    }
    
    @Test
    public void testIncreaseVersion() {
        assertEquals("1.0.1", MavenVersionUtils.increaseVersion("1.0.0"));
        assertEquals("1.1.1", MavenVersionUtils.increaseVersion("1.1.0"));
        assertEquals("1.1.2", MavenVersionUtils.increaseVersion("1.1.1"));
    }
    
}
