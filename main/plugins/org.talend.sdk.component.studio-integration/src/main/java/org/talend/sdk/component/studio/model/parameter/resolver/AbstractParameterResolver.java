package org.talend.sdk.component.studio.model.parameter.resolver;

import static java.util.Comparator.comparing;
import static java.util.Locale.ROOT;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.form.internal.converter.impl.widget.path.AbsolutePathResolver;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.IActionParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

/**
 * Common super class for ParameterResolvers. It contains common state and functionality
 */
public abstract class AbstractParameterResolver implements ParameterResolver {

    protected final AbsolutePathResolver pathResolver = new AbsolutePathResolver();

    /**
     * PropertyNode, which represents Configuration class Option annotated with action annotation
     */
    private final Action action;

    protected final PropertyNode actionOwner;

    protected final ActionReference actionRef;

    private final ElementParameter redrawParameter;

    public AbstractParameterResolver(final Action action, final PropertyNode actionOwner, final ActionReference actionRef) {
        this(action, actionOwner, actionRef, null);
    }

    public AbstractParameterResolver(final Action action, final PropertyNode actionOwner, final ActionReference actionRef, final ElementParameter redrawParameter) {
        this.action = action;
        this.actionOwner = actionOwner;
        this.actionRef = actionRef;
        this.redrawParameter = redrawParameter;
    }

    protected static ActionReference getActionRef(final Collection<ActionReference> actions, final String name, final Action.Type type) {
        return actions
                .stream()
                .filter(a -> type.toString().equals(a.getType()))
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action with name " + name + " wasn't found"));
    }

    /**
     * Finds ElementParameters needed for action call by their relative path.
     * Registers PropertyChangeListener to each ElementParameter needed for action call
     * Creates ActionParameter for each ElementParameter
     *
     * @param settings all "leaf" Component options
     */
    public void resolveParameters(final Map<String, IElementParameter> settings) {
        final Iterator<PropertyDefinitionDecorator> expectedParameters = PropertyDefinitionDecorator
                .wrap(actionRef.getProperties())
                .stream()
                .filter(p -> p.getParameter().isRoot())
                .sorted(comparing(p -> p.getParameter().getIndex()))
                .iterator();
        final List<String> relativePaths = getRelativePaths();
        relativePaths.forEach(relativePath -> {
            if (expectedParameters.hasNext()) {
                final String absolutePath = pathResolver.resolveProperty(getOwnerPath(), relativePath);
                final SimplePropertyDefinition parameterRoot = expectedParameters.next();
                findParameters(absolutePath, settings).forEach(parameter -> {
                    if (redrawParameter != null) {
                        parameter.setRedrawParameter(redrawParameter);
                    }
                    final String callbackProperty = parameter.getName()
                            .replaceFirst(absolutePath, parameterRoot.getPath());
                    final IActionParameter actionParameter = parameter.createActionParameter(callbackProperty);
                    action.addParameter(actionParameter);
                    if (action.getContextManager() == null) {
                        INode node = INode.class.cast(parameter.getElement());
                        if (node != null && node.getProcess() != null) {
                            action.setContextManager(node.getProcess().getContextManager());
                        }
                    }
                });
            }
        });
    }

    protected final Action getAction() {
        return this.action;
    }

    protected final List<String> getRelativePaths() {
        return actionOwner.getProperty().getParameters(actionRef.getType().toLowerCase(ROOT));
    }

    /**
     * Finds and returns all child ElementParameters of node with {@code absolutePath}. {@code absolutePath} may point at "leaf" Configuration option and
     * on Configuration type as well.
     *
     * @param absolutePath option path
     * @param settings     all "leaf" options stored by their path
     *
     * @return resolved ElementParameters
     */
    private List<TaCoKitElementParameter> findParameters(final String absolutePath, final Map<String, IElementParameter> settings) {
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
    private String getOwnerPath() {
        return actionOwner.getProperty().getPath();
    }

    /**
     * Checks whether specified {@code path} is a child path of {@code parentPath}
     *
     * @param path       path to be checked
     * @param parentPath parent path
     *
     * @return true, if path is child; false - otherwise
     */
    private boolean isChildParameter(final String path, final String parentPath) {
        return path.startsWith(parentPath) && path.substring(parentPath.length()).startsWith(".");
    }

}
