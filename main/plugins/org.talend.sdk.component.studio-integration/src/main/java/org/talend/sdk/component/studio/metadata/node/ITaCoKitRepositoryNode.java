/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata.node;

import org.eclipse.swt.graphics.Image;
import org.talend.repository.model.IRepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;

public interface ITaCoKitRepositoryNode extends IRepositoryNode {

    ConfigTypeNode getConfigTypeNode();

    ITaCoKitRepositoryNode getParentTaCoKitNode();

    Image getImage();

    boolean isLeafNode();

    boolean isFolderNode();

    boolean isFamilyNode();

    boolean isConfigNode();

}
