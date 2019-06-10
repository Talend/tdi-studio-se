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
package org.talend.sdk.component.studio.metadata.provider;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.repository.viewer.label.RepositoryViewLabelProvider;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;

public class TaCoKitMetadataLabelProvider extends RepositoryViewLabelProvider implements ILabelProvider {

    @Override
    public Image getImage(final Object element) {
        Image image = null;
        if (element instanceof ITaCoKitRepositoryNode) {
            image = ((ITaCoKitRepositoryNode) element).getImage();
        }
        if (image != null) {
            if (element instanceof ITaCoKitRepositoryNode) {
                ITaCoKitRepositoryNode rnode = ((ITaCoKitRepositoryNode) element);
                if (rnode.isLeafNode()) {
                    return super.decorateImageWithStatus(image, rnode.getObject());
                }
            }
            return image;
        }
        return super.getImage(element);
    }

}
