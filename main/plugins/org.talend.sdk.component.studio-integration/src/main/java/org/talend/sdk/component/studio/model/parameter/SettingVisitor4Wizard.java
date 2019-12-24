// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.Optional;

import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.resolver.HealthCheckResolver;

/**
 * created by hcyi on Dec 24, 2019
 * Detailled comment
 *
 */
public class SettingVisitor4Wizard extends SettingVisitor {

    public SettingVisitor4Wizard(IElement iNode, ElementParameter redrawParameter, ConfigTypeNode config) {
        super(iNode, redrawParameter, config);
    }

    public SettingVisitor4Wizard withCategory(final EComponentCategory category) {
        this.category = category;
        this.form = category == EComponentCategory.ADVANCED ? Metadatas.ADVANCED_FORM : Metadatas.MAIN_FORM;
        return this;
    }

    /**
     * Builds HealthCheck button
     *
     * @param node current PropertyNode
     */
    @Override
    public void buildHealthCheck(final PropertyNode node) {
        if (hasHealthCheck(node)) {
            final ActionReference action = actions.stream().filter(a -> Action.Type.HEALTHCHECK.toString().equals(a.getType()))
                    .filter(a -> a.getName().equals(node.getProperty().getHealthCheckName())).findFirst().get();
            final Layout checkableLayout = node.getLayout(form);
            final Optional<Layout> buttonLayout = checkableLayout
                    .getChildLayout(checkableLayout.getPath() + PropertyNode.CONNECTION_BUTTON);
            if (buttonLayout.isPresent()) {
                new HealthCheckResolver(element, family, node, action, category, buttonLayout.get().getPosition())
                        .resolveParameters(settings);
            } else {
                LOGGER.debug("Button layout {} not found for form {}", checkableLayout.getPath() + PropertyNode.CONNECTION_BUTTON, //$NON-NLS-1$
                        form);
            }
        }
    }
}
