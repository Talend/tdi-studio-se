/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata.tester;

import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;

public class TaCoKitNodeTest extends AbstractNodeTester {

    private static final String IS_TACOKIT_NODE = "isTaCoKitNode"; //$NON-NLS-1$

    @Override
    protected Boolean testProperty(final Object receiver, final String property, final Object[] args,
            final Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            if (IS_TACOKIT_NODE.equals(property)) {
                if (receiver instanceof ITaCoKitRepositoryNode) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

}
