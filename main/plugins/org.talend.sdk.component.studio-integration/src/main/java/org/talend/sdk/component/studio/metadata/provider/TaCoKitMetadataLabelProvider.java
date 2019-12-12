/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.label.RepositoryViewLabelProvider;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitLeafRepositoryNode;
import org.talend.sdk.component.studio.util.ETaCoKitImage;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class TaCoKitMetadataLabelProvider extends RepositoryViewLabelProvider implements ILabelProvider {

    private Map<ERepositoryObjectType, Image> imageMap = new HashMap<>();

    @Override
    public Image getImage(final Object element) {
        try {
            Image image = null;
            if (element instanceof ITaCoKitRepositoryNode) {
                ITaCoKitRepositoryNode rnode = ((ITaCoKitRepositoryNode) element);
                image = rnode.getImage();
                if (image != null) {
                    if (rnode.isLeafNode()) {
                        return super.decorateImageWithStatus(image, rnode.getObject());
                    }
                    return image;
                }
            }
            if (element instanceof RepositoryNode) {
                RepositoryNode repoNode = ((RepositoryNode) element);
                ENodeType nodeType = repoNode.getType();
                ERepositoryObjectType type = repoNode.getObjectType();
                if (nodeType == ENodeType.REPOSITORY_ELEMENT && type != null) {
                    if (TaCoKitUtil.isTaCoKitType(type)) {
                        Image img = imageMap.get(type);
                        if (img == null) {
                            ConfigTypeNode configTypeNode = Lookups.taCoKitCache().getRepositoryObjectType2ConfigTypeNodeMap()
                                    .get(type);
                            if (configTypeNode != null) {
                                try {
                                    img = new TaCoKitLeafRepositoryNode(null, null, null, "dummy", configTypeNode).getImage(); //$NON-NLS-1$
                                } catch (Exception e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                            if (img == null) {
                                img = ImageProvider.getImage(ETaCoKitImage.TACOKIT_REPOSITORY_ICON);
                            }
                            imageMap.put(type, img);
                        }
                        return super.decorateImageWithStatus(img, repoNode.getObject());
                    }
                }
            }
            if (element instanceof ITaCoKitRepositoryNode) {
                ITaCoKitRepositoryNode rnode = ((ITaCoKitRepositoryNode) element);
                if (rnode.isFamilyNode()) {
                    return ImageProvider.getImage(ETaCoKitImage.TACOKIT_REPOSITORY_ICON);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return super.getImage(element);
    }

}
