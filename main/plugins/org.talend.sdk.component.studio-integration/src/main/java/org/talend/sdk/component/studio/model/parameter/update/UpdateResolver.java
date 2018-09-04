// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.model.parameter.update;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.resolver.AbsolutePathResolver;
import org.talend.sdk.component.studio.model.parameter.resolver.ParameterResolver;

import java.util.Map;

public class UpdateResolver implements ParameterResolver {

    private final UpdateListener listener;

    private final PropertyNode updatableNode;

    private final AbsolutePathResolver pathResolver = new AbsolutePathResolver();

    public UpdateResolver(final UpdateListener listener, final PropertyNode updatableNode) {
        this.listener = listener;
        this.updatableNode = updatableNode;
    }

    /**
     * Registers {@link UpdateListener} to target ElementParameters
     *
     * @param settings
     */
    @Override
    public void resolveParameters(Map<String, IElementParameter> settings) {
        final PropertyDefinitionDecorator.Updatable updatable = updatableNode.getProperty().getUpdatable().orElseThrow(() ->
                new IllegalStateException(getNodePath() + " node has no @Updatable"));
        final String relativeTargetPath = updatable.getTarget();
        final String absoluteTargetPath = pathResolver.resolvePath(getNodePath(), relativeTargetPath);
        final TaCoKitElementParameter targetParam = (TaCoKitElementParameter) settings.get(absoluteTargetPath);
        if (targetParam == null) {
            throw new IllegalStateException(absoluteTargetPath + " parameter not found");
        }
        targetParam.registerListener("value", listener);
    }

    /**
     * Returns action owner option path in Configuration tree
     *
     * @return option path
     */
    private String getNodePath() {
        return updatableNode.getProperty().getPath();
    }
}
