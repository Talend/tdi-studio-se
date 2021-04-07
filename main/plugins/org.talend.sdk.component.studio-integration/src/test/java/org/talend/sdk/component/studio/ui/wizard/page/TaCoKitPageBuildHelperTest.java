package org.talend.sdk.component.studio.ui.wizard.page;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.eclipse.core.internal.registry.osgi.RegistryProviderOSGI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.metadata.node.TaCoKitFamilyRepositoryNode;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Instantiation of ERepositoryObjectType need eclipse.runtime env initiate")
class TaCoKitPageBuildHelperTest {

    @Test
    void getParameters() throws Exception {

        this.init();
        ProjectManager.getInstance().setMainProjectBranch("studio", "main");
        RegistryProviderFactory.setDefault(new RegistryProviderOSGI(new ExtensionRegistry(null, "master", "user")));

        TaCoKitConfigurationRuntimeData runtimeData = new TaCoKitConfigurationRuntimeData();

        final ConfigTypeNode cfgNode = new ConfigTypeNode();
        cfgNode.setName("Test");
        cfgNode.setDisplayName("Test");
        cfgNode.setId("d29ya2RheSNXb3JrZGF5");
        cfgNode.getEdges().add("d29ya2RheSNXb3JrZGF5I2RhdGFzdG9yZSNXb3JrZGF5RGF0YVN0b3Jl");

        TaCoKitFamilyRepositoryNode family = new TaCoKitFamilyRepositoryNode(
                null, // without parent
                null,
                cfgNode);

       /* final RepositoryNode parentNode = new RepositoryNode(null,
                null, //project,
                ENodeType.STABLE_SYSTEM_FOLDER);
        family.setParent(parentNode);*/
        family.setType(ENodeType.STABLE_SYSTEM_FOLDER);
        runtimeData.setTaCoKitRepositoryNode(family);

        SimplePropertyDefinition cfg = new SimplePropertyDefinition();
        cfg.setPath("configuration");
        cfg.setName("configuration");
        cfg.setDisplayName("Data connection");
        cfg.setType("OBJECT");
        cfg.setPlaceholder("configuration");
        Map<String, String> meta = new HashMap<>();

        meta.put("ui::gridlayout::Advanced::value", "param2");
        meta.put("ui::gridlayout::Main::value", "param1");
        meta.put("documentation::value", "The connection to test datastore");
        //meta.put("action::healthcheck", "WORKDAY_HEALTH_CHECK");
        meta.put("configurationtype::name", "TestDataStore");
        meta.put("configurationtype::type", "datastore");
        meta.put("definition::parameter::index", "0");
        cfg.setMetadata(meta);
        cfgNode.getProperties().add(cfg);


        cfgNode.getProperties().add(new SimplePropertyDefinition("configuration.param1",
                "param1",
                "first param",
                "STRING",
                null,
                null,
                 Collections.singletonMap("documentation::value", "doc param 1"),
                "place holder 1",
                null));
        cfgNode.getProperties().add(new SimplePropertyDefinition("configuration.param2",
                "param2",
                "second param",
                "STRING",
                null,
                null,
                Collections.singletonMap("documentation::value", "doc param 2"),
                "place holder 2",
                null));

        runtimeData.setCreation(true);
        runtimeData.setReadonly(false);

        final TaCoKitPageBuildHelper helper = new TaCoKitPageBuildHelper(runtimeData);
        helper.infer("Main");
        helper.infer("Advanced");
        helper.terminate();
        final List<IElementParameter> mainParameters = helper.getParameters("Main");
        final List<IElementParameter> advancedParameters = helper.getParameters("Advanced");
        Assertions.assertNotNull(mainParameters);
    }

    void init() {
        try {
            final Constructor<ERepositoryObjectType> declaredConstructor =
                    ERepositoryObjectType.class.getDeclaredConstructor(String.class, String.class, String.class,
                            String.class, int.class, boolean.class, String.class, String[].class, boolean.class,
                            String[].class, boolean[].class);
            declaredConstructor.setAccessible(true);
            ERepositoryObjectType typeObject = declaredConstructor.newInstance("repository.metadata.TaCoKit", "TaCoKit",
                    "metadata/tacokit", "TACOKIT", 1, false, "TACOKIT",
                    new String[] {"DI"},
                    false,
                    new String[] { "DI" },
                    new boolean[] { true});
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}