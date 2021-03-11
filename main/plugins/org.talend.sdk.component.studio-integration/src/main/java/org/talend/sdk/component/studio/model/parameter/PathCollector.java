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
package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor which collects all nodes paths
 */
public class PathCollector implements PropertyVisitor {

    private final List<String> paths = new ArrayList<>();

    @Override
    public void visit(final PropertyNode node) {
        paths.add(node.getProperty().getPath());
    }

    public List<String> getPaths() {
        return this.paths;
    }
}
