/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter.resolver;

import java.util.Objects;

/**
 * Resolves PropertyNode path from given PropertyNode and relative path This is
 * a duplicate of AbsolutePathResolver class from component-form-core
 */
public class AbsolutePathResolver {

    /**
     * Resolves absolute path of parameter from given {@code nodePath} and parameter
     * relative path. Resolution is done according rules described in component-api
     * Validable annotation.
     * 
     * @param nodePath
     *            given node absolute path (ex: root.part2.part3)
     * @param parameterReference
     *            relative path which should be resolved using nodePath (ex:
     *            ../part1/part2)
     * @return parameter/node absolute path
     */
    public String resolvePath(final String nodePath, final String parameterReference) {
        check(nodePath, parameterReference);
        return doResolvePath(nodePath, normalizePath(parameterReference));
    }

    private void check(final String nodePath, final String parameterReference) {
        Objects.requireNonNull(nodePath, "nodePath should not be null");
        Objects.requireNonNull(parameterReference, "parameterReference should not be null");
        if (nodePath.isEmpty()) {
            throw new IllegalArgumentException("nodePath should not be empty");
        }
        if (parameterReference.isEmpty()) {
            throw new IllegalArgumentException("parameterReference should not be empty");
        }
    }

    /**
     * If relative path starts with path part, but not with "." or "../", then "../"
     * is added in the beggining. According resolution rules {@code sibling/child}
     * path is equavalent to {@code ../sibling/child}
     * 
     * @param parameterReference
     *            relative path which should be resolved using nodePath (ex:
     *            ../part1/part2)
     * @return normalized path which starts with either "." or "../"
     */
    private String normalizePath(final String parameterReference) {
        return (parameterReference.startsWith(".") ? "" : "../") + parameterReference;
    }

    /**
     * Resolves absolute path from relative parameterReference path
     * 
     * @param nodePath node absolute path
     * @param parameterReference relative parameter reference path
     * @return resolved path
     */
    private String doResolvePath(final String nodePath, final String parameterReference) {
        if (".".equals(parameterReference)) {
            return nodePath;
        }
        if (parameterReference.startsWith("..")) {
            String current = nodePath;
            String ref = parameterReference;
            while (ref.startsWith("..")) {
                final int lastDot = current.lastIndexOf('.');
                if (lastDot < 0) {
                    ref = "";
                    break;
                }
                current = current.substring(0, lastDot);
                ref = ref.substring("..".length(), ref.length());
                if (ref.startsWith("/")) {
                    ref = ref.substring(1);
                }
            }
            return current + (!ref.isEmpty() ? "." : "") + ref.replace('/', '.');
        }
        if (parameterReference.startsWith(".") || parameterReference.startsWith("./")) {
            return nodePath + '.' + parameterReference.replaceFirst("\\./?", "").replace('/', '.');
        }
        return parameterReference;
    }
}
