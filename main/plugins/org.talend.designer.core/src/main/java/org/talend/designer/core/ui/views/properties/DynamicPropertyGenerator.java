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
package org.talend.designer.core.ui.views.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController;
import org.talend.designer.core.ui.editor.properties.controllers.ControllerRepositoryValueHander;
import org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: DynamicTabbedPropertyGenerator.java 1 2006-12-22 上午11:24:10 +0000 (上午11:24:10) yzhang $
 *
 */
public class DynamicPropertyGenerator {

    // private static DynamicPropertyGenerator instance = null;

    private boolean initialized = false;

    IExtensionRegistry registry = Platform.getExtensionRegistry();

    IConfigurationElement[] extensionElements = registry.getConfigurationElementsFor("org.talend.designer.core.generators"); //$NON-NLS-1$

    private Map<EParameterFieldType, AbstractElementPropertySectionController> dtpControls = new HashMap<EParameterFieldType, AbstractElementPropertySectionController>();

    /**
     * DOC yzhang Comment method "initController".
     */
    public void initController(IDynamicProperty dp) {
        if (!initialized) {

            for (IConfigurationElement element : extensionElements) {
                try {
                    String controllerName = element.getAttribute("mapping"); //$NON-NLS-1$
                    EParameterFieldType key = EParameterFieldType.getFieldTypeByName(controllerName);
                    if (!dtpControls.containsKey(key)) {
                        if (!controllerName.equals(key.toString())) {
                            throw new RuntimeException("Mapping attribute " + controllerName //$NON-NLS-1$
                                    + " not included in eumn EParameterFieldType"); //$NON-NLS-1$
                        }
                        IControllerGenerator generator = (IControllerGenerator) element.createExecutableExtension("class"); //$NON-NLS-1$
                        generator.setDynamicProperty(dp);
                        AbstractElementPropertySectionController controller = generator.generate();
                        dtpControls.put(key, controller);
                        if (controller instanceof AbstractRepositoryController) {
                            ControllerRepositoryValueHander repositoryValueHander = ((AbstractRepositoryController) controller)
                                    .getRepositoryValueHander();
                            ControllerRepositoryValueHander.getRepositoryValueHandlerMap().put(key, repositoryValueHander);
                        }
                    }
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }

            }
            initialized = true;
        }
    }

    /**
     * DOC yzhang Comment method "getController".
     *
     * @param controllerName
     * @param dtp
     * @return
     */
    public AbstractElementPropertySectionController getController(EParameterFieldType controllerName, IDynamicProperty dp) {

        AbstractElementPropertySectionController controller = null;
        if (dtpControls.containsKey(controllerName)) {
            controller = dtpControls.get(controllerName);
            if (controller != null) {
                controller.init(dp);
                return controller;
            } else {
                dtpControls.remove(controllerName);
            }
        }
        return controller;
    }

    public void dispose() {
        if (dtpControls != null) {
            for (AbstractElementPropertySectionController controller : dtpControls.values()) {
                controller.dispose();
            }
            dtpControls.clear();
            // dtpControls = null;
        }
    }

}
