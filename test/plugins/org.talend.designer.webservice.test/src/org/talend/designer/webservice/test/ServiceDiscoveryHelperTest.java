package org.talend.designer.webservice.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
//============================================================================
//
//Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
import javax.wsdl.Import;

import org.junit.Test;
import org.talend.designer.webservice.ws.wsdlutil.ServiceDiscoveryHelper;

public class ServiceDiscoveryHelperTest {

	private static final String MAIN_DEF_NAMESPACE = "http://www.talend.org/service/";
	private static final String BINDING_DEF_NAMESPACE = "http://www.talend.org/service/binding/";

	@Test
	public void testCircularDependencyLoading() throws Exception {
		String wsdlLocation = getClass().getResource("TestService.wsdl").toExternalForm();
		ServiceDiscoveryHelper helper = new ServiceDiscoveryHelper(wsdlLocation);
		List<Definition> definitions = helper.getDefinitions();
		assertNotNull(definitions);
		assertEquals(3, definitions.size());
		Definition mainDef = definitions.get(0);
		assertEquals(MAIN_DEF_NAMESPACE, mainDef.getTargetNamespace());
		Map<?, ?> mainDefImports = mainDef.getImports();
		assertNotNull(mainDefImports);
		assertEquals(1, mainDefImports.size());
		List<?> bindingNamespaceImports = (List<?>) mainDefImports.get(BINDING_DEF_NAMESPACE);
		assertNotNull(bindingNamespaceImports);
		assertEquals(2, bindingNamespaceImports.size());
		Import bindingNamespaceImport = (Import) bindingNamespaceImports.get(0);
		assertEquals(BINDING_DEF_NAMESPACE, bindingNamespaceImport.getNamespaceURI());
		assertTrue(bindingNamespaceImport.getLocationURI().indexOf("Jms") < 0);
		Import bindingNamespaceImportJms = (Import) bindingNamespaceImports.get(1);
		assertEquals(BINDING_DEF_NAMESPACE, bindingNamespaceImportJms.getNamespaceURI());
		assertNotEquals(bindingNamespaceImport.getLocationURI(), bindingNamespaceImportJms.getLocationURI());
		Definition bindingDef = definitions.get(1);
		assertEquals(BINDING_DEF_NAMESPACE, bindingDef.getTargetNamespace());
		assertTrue(((Binding) bindingDef.getBindings().values().iterator().next()).getQName().getLocalPart().indexOf("Jms") < 0);
		Map<?, ?> bindingDefImports = bindingDef.getImports();
		if (bindingDefImports != null) {
			int size = bindingDefImports.size();
			if (size == 1) {
				List<?> mainNamespaceImports = (List<?>) bindingDefImports.get(MAIN_DEF_NAMESPACE);
				assertNotNull(mainNamespaceImports);
				assertEquals(0, mainNamespaceImports.size());
			} else {
				assertEquals(0, bindingDefImports.size());
			}
		}
		bindingDef = definitions.get(2);
		assertEquals(BINDING_DEF_NAMESPACE, bindingDef.getTargetNamespace());
		assertTrue(((Binding) bindingDef.getBindings().values().iterator().next()).getQName().getLocalPart().indexOf("Jms") > 0);
		bindingDefImports = bindingDef.getImports();
		if (bindingDefImports != null) {
			int size = bindingDefImports.size();
			if (size == 1) {
				List<?> mainNamespaceImports = (List<?>) bindingDefImports.get(MAIN_DEF_NAMESPACE);
				assertNotNull(mainNamespaceImports);
				assertEquals(0, mainNamespaceImports.size());
			} else {
				assertEquals(0, bindingDefImports.size());
			}
		}
	}
}
