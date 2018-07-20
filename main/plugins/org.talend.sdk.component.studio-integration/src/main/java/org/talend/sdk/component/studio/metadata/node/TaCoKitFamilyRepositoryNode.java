/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.ByteArrayInputStream;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.StableRepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

// TODO it is very similar to AbsTaCoKitRepositoryNode but reuse it
/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitFamilyRepositoryNode extends StableRepositoryNode implements ITaCoKitRepositoryNode {

    private final ConfigTypeNode configTypeNode;

    private final Image image;

    public TaCoKitFamilyRepositoryNode(final RepositoryNode parent, final String label, final IImage icon,
            final ConfigTypeNode configTypeNode) throws Exception {
        super(parent, label, icon);
        this.configTypeNode = configTypeNode;
        this.image = getTaCoKitImage(configTypeNode);
        this.setChildrenObjectType(TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode));
    }

    @Override
    public ITaCoKitRepositoryNode getParentTaCoKitNode() {
        return null;
    }

    @Override
    public void setChildrenObjectType(final ERepositoryObjectType childrenObjectType) {
        super.setChildrenObjectType(childrenObjectType);
        this.setProperties(EProperties.CONTENT_TYPE, childrenObjectType);
    }

    public static Image getTaCoKitImage(final ConfigTypeNode configTypeNode) {
        Image imageToDispose = null;
        try {
            ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
            String id = configTypeNode.getId();
            String imageKey = GAV.INSTANCE.getArtifactId() + "/TaCoKit/Family/Metadata" + id; //$NON-NLS-1$
            Image image = imageRegistry.get(imageKey);
            if (image == null) {
                try {
                    image = getFamilyImage(id);
                    imageToDispose = image;
                    image = ImageUtils.scale(image, ICON_SIZE.ICON_16);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    image = ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON);
                }
                imageRegistry.put(imageKey, image);
            }
            if (image != null) {
                return image;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (imageToDispose != null) {
                imageToDispose.dispose();
            }
        }
        return null;
    }

    private static Image getFamilyImage(final String id) throws Exception {
        byte[] icon = Lookups.client().v1().component().familyIcon(id);
        if (icon != null && 0 < icon.length) {
            ByteArrayInputStream inputStream = null;
            try {
                inputStream = new ByteArrayInputStream(icon);
                return new Image(Display.getDefault(), inputStream);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public boolean isFolderNode() {
        return false;
    }

    @Override
    public boolean isFamilyNode() {
        return true;
    }

    @Override
    public boolean isConfigNode() {
        return false;
    }

    // todo when studio is redeployed @Override
    public boolean shouldCollectRepositoryNode() {
        return true;
    }

    @Override
    public ConfigTypeNode getConfigTypeNode() {
        return this.configTypeNode;
    }

    @Override
    public Image getImage() {
        return this.image;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TaCoKitFamilyRepositoryNode))
            return false;
        final TaCoKitFamilyRepositoryNode other = (TaCoKitFamilyRepositoryNode) o;
        if (!other.canEqual(this))
            return false;
        if (!super.equals(o))
            return false;
        final Object this$configTypeNode = this.getConfigTypeNode();
        final Object other$configTypeNode = other.getConfigTypeNode();
        if (this$configTypeNode == null ? other$configTypeNode != null : !this$configTypeNode.equals(other$configTypeNode))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TaCoKitFamilyRepositoryNode;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $configTypeNode = this.getConfigTypeNode();
        result = result * PRIME + ($configTypeNode == null ? 43 : $configTypeNode.hashCode());
        return result;
    }
}
