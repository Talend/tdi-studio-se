package org.talend.sdk.component.studio.model.parameter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.talend.core.model.process.IElementParameter;

/**
 * Common super class for ParameterResolvers. It contains common state and functionality
 */
abstract class AbstractParameterResolver implements ParameterResolver {
    
    /**
     * Path character which denotes parent element
     */
    private static final String PARENT = "..";

    /**
     * Path character which denotes current element
     */
    private static final String CURRENT = ".";

    /**
     * Path separator which is used in Property tree
     */
    private static final String DOT_PATH_SEPARATOR = ".";

    /**
     * Path separator
     */
    private static final String PATH_SEPARATOR = "/";
    
    protected final PropertyNode actionOwner;
    
    AbstractParameterResolver(final PropertyNode actionOwner) {
        this.actionOwner = actionOwner;

    }

    static String resolve(final PropertyNode baseNode, final String relativePath) {
        final String basePath = baseNode.getParentId();
        final LinkedList<String> path = new LinkedList<>();
        path.addAll(Arrays.asList(basePath.split("\\" + DOT_PATH_SEPARATOR)));
        final List<String> parts = Arrays.asList(relativePath.split(PATH_SEPARATOR));
        for (final String p : parts) {
            if (CURRENT.equals(p)) {
                continue;
            }
            if (PARENT.equals(p)) {
                path.removeLast();
            } else {
                path.addLast(p);
            }
        }
        return path.stream().collect(Collectors.joining(DOT_PATH_SEPARATOR));
    }
    
    protected final TaCoKitElementParameter resolveParameter(final String relativePath, final Map<String, IElementParameter> settings) {
        String path = null;
        // workaround for "." relative path. "./prop" will be handled in else branch
        if (CURRENT.equals(relativePath)) {
            path = actionOwner.getProperty().getPath();
        } else {
            path = resolve(actionOwner, relativePath);
        }
        return (TaCoKitElementParameter) settings.get(path);
    }

}
