// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.commons.utils.Hex;


/**
 * DOC sbliu  class global comment. Detailled comment
 */
public class UpdateMoudleListInComponentsMigrationTest {

    @Test
    public void testParseJarNameForHexVaue() {
        String contextA = "context.jdbc_jar\n";
        String encoded_contextA = Hex.encodeHexString(contextA.getBytes());
        String jar = "mysql-connector-5.1.3\n";
        String encoded_jar = Hex.encodeHexString(jar.getBytes());
        
        String prefix = "mvn:org.talend.libraries/";
        String suffix = "/6.0.0-SNAPSHOT";
        
        
        //parseJarNameForHexVaue param must be or contains hex string
        //test migrate to 7.3
        String jarName = prefix + encoded_contextA + suffix;
        UpdateModuleListInComponentsMigrationTask mt_73 = new UpdateModuleListInComponentsMigrationTask();
        String parsedJarName = mt_73.parseJarNameForHexVaue(jarName);//contains hex context
        assertEquals(jarName, parsedJarName);
        
        parsedJarName = mt_73.parseJarNameForHexVaue(encoded_contextA);//be hex context
        assertEquals(encoded_contextA, parsedJarName);
        
        parsedJarName = mt_73.parseJarNameForHexVaue(encoded_jar);//be hex jar 
        assertEquals(Hex.encodeHexString((prefix + jar + suffix).getBytes()), parsedJarName);
        
        jarName = prefix + encoded_jar + suffix;
        parsedJarName = mt_73.parseJarNameForHexVaue(jarName); //contains hex jar
        assertEquals(jarName, parsedJarName);
        
        jarName = prefix + jar + suffix;
        String encodeHexString = Hex.encodeHexString(jarName.getBytes());
        parsedJarName = mt_73.parseJarNameForHexVaue(encodeHexString); // be hex jar with 'mvn:' prefix
        assertEquals(encodeHexString, parsedJarName);
        
        //test new migration task
        jarName = prefix + encoded_contextA + suffix;
        UpdateModuleListInComponents2MigrationTask mt_74 = new UpdateModuleListInComponents2MigrationTask();
        parsedJarName = mt_74.parseJarNameForHexVaue(jarName); //contains hex context
        assertEquals(encoded_contextA, parsedJarName);
        
        jarName = prefix + encoded_jar + suffix;
        parsedJarName = mt_74.parseJarNameForHexVaue(jarName); //contains hex jar
        assertEquals(Hex.encodeHexString((prefix + jar + suffix).getBytes()), parsedJarName);
        
        parsedJarName = mt_74.parseJarNameForHexVaue(encoded_contextA); //be hex context
        assertEquals(encoded_contextA, parsedJarName);
        
        jarName = prefix + encoded_jar + suffix;
        parsedJarName = mt_74.parseJarNameForHexVaue(encoded_jar); //be hex jar
        assertEquals(Hex.encodeHexString((prefix + jar + suffix).getBytes()), parsedJarName);
        
        parsedJarName = mt_74.parseJarNameForHexVaue(encodeHexString);// be hex jar with 'mvn:' prefix
        assertEquals(encodeHexString, parsedJarName);
    }

}
