/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.IRepositoryNode;

public final class Compatibility {

    private static final Method COLLECT_REPOSITORY_NODES;

    private Compatibility() {
        new AssertionError();
    }

    static {
        try {
            COLLECT_REPOSITORY_NODES = ProjectRepositoryNode.class.getDeclaredMethod("collectRepositoryNodes", List.class);
            COLLECT_REPOSITORY_NODES.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("bad studio version", e);
        }
    }

    public static void collectRepositoryNodes(final ProjectRepositoryNode projectRepositoryNode,
            final List<IRepositoryNode> param) {
        try {
            COLLECT_REPOSITORY_NODES.invoke(projectRepositoryNode, param);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (RuntimeException.class.isInstance(targetException)) {
                throw RuntimeException.class.cast(targetException);
            }
            throw new IllegalStateException(targetException);
        }
    }

}
