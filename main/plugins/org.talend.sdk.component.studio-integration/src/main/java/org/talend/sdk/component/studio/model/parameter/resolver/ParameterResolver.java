package org.talend.sdk.component.studio.model.parameter.resolver;

import java.util.Map;

import org.talend.core.model.process.IElementParameter;

/**
 * Finds and makes initial setup for action (component server callback) parameters
 */
public interface ParameterResolver {
    
    void resolveParameters(final Map<String, IElementParameter> settings);

}
