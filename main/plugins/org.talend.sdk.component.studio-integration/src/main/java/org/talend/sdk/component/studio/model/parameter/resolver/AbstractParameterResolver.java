package org.talend.sdk.component.studio.model.parameter.resolver;

import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.ActionParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.PropertyTreeCreator;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.WidgetTypeMapper;

import static java.util.Comparator.comparing;

/**
 * Common super class for ParameterResolvers. It contains common state and functionality
 */
abstract class AbstractParameterResolver implements ParameterResolver {
    
    protected final AbsolutePathResolver pathResolver = new AbsolutePathResolver();
    
    protected final PropertyTreeCreator treeCreator = new PropertyTreeCreator(new WidgetTypeMapper());
    
    /**
     * PropertyNode, which represents Configuration class Option annotated with action annotation
     */
    private final Action action;

    protected final PropertyNode actionOwner;
    
    protected final ActionReference actionRef;

    private final PropertyChangeListener listener;

    private final ElementParameter redrawParameter;

    AbstractParameterResolver(final Action action, final PropertyNode actionOwner, final ActionReference actionRef, final PropertyChangeListener listener) {
        this(action, actionOwner, actionRef, listener, null);
    }
    
    AbstractParameterResolver(final Action action, final PropertyNode actionOwner, final ActionReference actionRef, final PropertyChangeListener listener,
                              final ElementParameter redrawParameter) {
        this.action = action;
        this.actionOwner = actionOwner;
        this.actionRef = actionRef;
        this.listener = listener;
        this.redrawParameter = redrawParameter;
    }

    /**
     * Finds ElementParameters needed for action call by their relative path.
     * Registers PropertyChangeListener to each ElementParameter needed for action call
     * Creates ActionParameter for each ElementParameter
     *
     * @param settings all "leaf" Component options
     */
    public void resolveParameters(final Map<String, IElementParameter> settings) {
        final Iterator<PropertyDefinitionDecorator> expectedParameters = PropertyDefinitionDecorator.wrap(actionRef.getProperties())
                .stream()
                .filter(p -> p.getParameter().isRoot())
                .sorted(comparing(p -> p.getParameter().getIndex()))
                .iterator();
        final List<String> relativePaths = getRelativePaths();

        relativePaths.forEach(relativePath -> {
            if (expectedParameters.hasNext()) {
                final String absolutePath = pathResolver.resolvePath(getOwnerPath(), relativePath);
                final List<TaCoKitElementParameter> parameters = resolveParameters(absolutePath, settings);
                final SimplePropertyDefinition parameterRoot = expectedParameters.next();
                parameters.forEach(parameter -> {
                    parameter.registerListener("value", listener);
                    if (redrawParameter != null) {
                        parameter.setRedrawParameter(redrawParameter);
                    }
                    final String callbackProperty = parameter.getName().replaceFirst(absolutePath, parameterRoot.getPath());
                    final String defaultValue = parameter.getStringValue();
                    final ActionParameter actionParameter = new ActionParameter(parameter.getName(), callbackProperty, defaultValue);
                    action.addParameter(actionParameter);
                });
            }
        });
    }

    protected abstract List<String> getRelativePaths();
    
    protected final TaCoKitElementParameter resolveParameter(final String relativePath, final Map<String, IElementParameter> settings) {
        String path = pathResolver.resolvePath(getOwnerPath(), relativePath);
        return (TaCoKitElementParameter) settings.get(path);
    }
    
    /**
     * Finds and returns all child ElementParameters of node with {@code absolutePath}. {@code absolutePath} may point at "leaf" Configuration option and 
     * on Configuration type as well.
     * 
     * @param absolutePath option path
     * @param settings all "leaf" options stored by their path
     * @return resolved ElementParameters
     */
    protected final List<TaCoKitElementParameter> resolveParameters(final String absolutePath, final Map<String, IElementParameter> settings) {
        final TaCoKitElementParameter parameter = (TaCoKitElementParameter) settings.get(absolutePath);
        if (parameter != null) {
            // absolute path points at "leaf" Configuration option, which doesn't have children
            return Collections.singletonList(parameter);
        } else {
            // absolute path points at Configuration type, which has no corresponding ElementParameter, however there are ElementParameters
            // for its children
            return settings.entrySet().stream()
                    .filter(e -> isChildParameter(e.getKey(), absolutePath))
                    .map(Map.Entry::getValue)
                    .map(e -> (TaCoKitElementParameter) e)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Returns action owner option path in Configuration tree
     * 
     * @return option path
     */
    protected final String getOwnerPath() {
        return actionOwner.getProperty().getPath();
    }
    
    /**
     * Finds action method parameter alias (Option annotation value)
     * This method builds property tree and assumes that root node path is a required alias
     * 
     * @return parameter alias
     */
    protected final String getParameterAlias() {
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator.wrap(actionRef.getProperties());
        final PropertyNode root = new PropertyTreeCreator(new WidgetTypeMapper()).createPropertyTree(properties);
        return root.getProperty().getPath();
    }
    
    /**
     * Checks whether specified {@code path} is a child path of {@code parentPath}
     * 
     * @param path path to be checked
     * @param parentPath parent path
     * @return true, if path is child; false - otherwise
     */
    private boolean isChildParameter(final String path, final String parentPath) {
        return path.startsWith(parentPath) && path.substring(parentPath.length()).startsWith(".");
    }

}
