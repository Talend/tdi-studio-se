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
package org.talend.ms.crm.odata;


import org.apache.olingo.client.api.domain.ClientEntity;
import org.junit.Before;
import org.junit.Test;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class DynamicsCRMClientTest {

    private final static Map<String, String> lookupNames = new HashMap<String, String>();
    static{
        lookupNames.put("_mylookup_value", "mylookup");
        lookupNames.put("mylookup", "mylookup");
        lookupNames.put("_x_value", "x");
        lookupNames.put("__value", "__value");
    }

    private DynamicsCRMClient client = null;


    @Before
    public void init() {
        ClientConfiguration configuration =
                ClientConfigurationFactory.buildNtlmClientConfiguration("username", "password", "host", "domain");

        client = null;
        try {
            client = new DynamicsCRMClient(configuration, "http://crm.dmnmscrm2016f.com:5555/oumscrm2016/api/data/v8.1",
                    "accounts");
            client.setEntityType("account");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        assertNotNull(client);
    }

    @Test
    public void testLookupNameExtractionMinSize() {
        for(Map.Entry<String, String> lookupName: lookupNames.entrySet()){
            assertEquals(lookupName.getValue(), client.extractNavigationLinkName(lookupName.getKey()));
        }
    }

    @Test
    public void testNbNavigationLinkToRemove(){
        ClientEntity clientEntity = client.newEntity();
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet1", "lookupName1", null, false, false); // To remove
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet2", "lookupName2", "id2", false, false); // To update
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet3", "lookupName3", null, false, false); // To remove
        int n = client.getNbNavigationLinkToRemove();

        assertEquals(2, n);
    }

    @Test
    public void testEmptyLookupIntoNull(){
        ClientEntity clientEntity = client.newEntity();
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet1", "lookupName1", null, false, false); // To remove
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet2", "lookupName2", "", true, false); // To remove (empty to null)
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet3", "lookupName3", "notNull", false, false); // To update
        int n = client.getNbNavigationLinkToRemove();

        assertEquals(2, n);
    }

    @Test
    public void testLookupIgnoreNull(){
        ClientEntity clientEntity = client.newEntity();

        boolean ignoreNull = true;
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet1", "lookupName1", null, false, ignoreNull); // Ignored
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet2", "lookupName2", "", true, ignoreNull); // Ignored (since empty to null && ignore null)
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet3", "lookupName3", "notNull", false, ignoreNull); // To update
        client.addEntityNavigationLink(clientEntity, "lookupEntitySet3", "lookupName3", null, false, !ignoreNull); // To remove
        int n = client.getNbNavigationLinkToRemove();

        assertEquals(1, n);
    }

}