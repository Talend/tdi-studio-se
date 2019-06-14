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
package org.talend.sdk.component.studio.metadata.node;

import java.io.ByteArrayInputStream;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public abstract class AbsTaCoKitRepositoryNode extends RepositoryNode implements ITaCoKitRepositoryNode {

    private ITaCoKitRepositoryNode parentTaCoKitNode;

    private ConfigTypeNode configTypeNode;

    private Image image;

    public AbsTaCoKitRepositoryNode(final IRepositoryViewObject repViewObject, final RepositoryNode parent,
            final ITaCoKitRepositoryNode parentTaCoKitNode, final String label, final Image image,
            final ConfigTypeNode configTypeNode) throws Exception {
        super(repViewObject, parent, IRepositoryNode.ENodeType.SYSTEM_FOLDER);
        this.parentTaCoKitNode = parentTaCoKitNode;
        this.configTypeNode = configTypeNode;
        this.image = image;
        this.setProperties(EProperties.LABEL, label);
        this.setProperties(EProperties.CONTENT_TYPE, TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode));
    }

    public Image getTaCoKitImage(final ConfigTypeNode configTypeNode) {
        Image imageToDispose = null;
        try {
            ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
            String id = configTypeNode.getId();
            String imageKey = GAV.INSTANCE.getArtifactId() + "/TaCoKit/Family/Metadata/" + id; //$NON-NLS-1$
            Image image = imageRegistry.get(imageKey);
            if (image == null) {
                try {
                    image = buildTaCoKitImage(id);
                    imageToDispose = image;
                    image = ImageUtils.scale(image, ICON_SIZE.ICON_16);
                } catch (Exception e) {
                    if (CommonsPlugin.isDebugMode()) {
                        ExceptionHandler.process(e);
                    }
                    image = getDefaultImage();
                    imageToDispose = image;
                    image = ImageUtils.scale(image, ICON_SIZE.ICON_16);
                }
                if (image != null) {
                    imageRegistry.put(imageKey, image);
                }
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

    protected Image getDefaultImage() {
        return ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON);
    }

    protected Image buildTaCoKitImage(final String id) throws Exception {
        byte[] icon = requestIcon(id);
        return buildTaCoKitImage(icon);
    }

    protected Image buildTaCoKitImage(byte[] icon) throws Exception {
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

    protected byte[] requestFamilyIcon(final String id) throws Exception {
        return Lookups.client().v1().component().familyIcon(id);
    }

    protected byte[] requestIcon(final String id) throws Exception {
        return Lookups.client().v1().component().icon(id);
    }

    public void setContentType(final ERepositoryObjectType contentType) {
        this.setProperties(EProperties.CONTENT_TYPE, contentType);
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
        return false;
    }

    @Override
    public boolean isConfigNode() {
        return false;
    }

    @Override
    public ITaCoKitRepositoryNode getParentTaCoKitNode() {
        return this.parentTaCoKitNode;
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
    public String getLabel() {
        String label = (String) this.getProperties(EProperties.LABEL);
        if (label != null && !label.trim().isEmpty()) {
            return label;
        } else {
            return super.getLabel();
        }
    }

    public void setParentTaCoKitNode(final ITaCoKitRepositoryNode parentTaCoKitNode) {
        this.parentTaCoKitNode = parentTaCoKitNode;
    }

    public void setConfigTypeNode(final ConfigTypeNode configTypeNode) {
        this.configTypeNode = configTypeNode;
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AbsTaCoKitRepositoryNode)) {
            return false;
        }
        if (!this.getClass().equals(o.getClass())) {
            return false;
        }
        final AbsTaCoKitRepositoryNode other = (AbsTaCoKitRepositoryNode) o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Object this$configTypeNode = this.getConfigTypeNode();
        final Object other$configTypeNode = other.getConfigTypeNode();
        if (this$configTypeNode == null ? other$configTypeNode != null : !this$configTypeNode.equals(other$configTypeNode)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AbsTaCoKitRepositoryNode;
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
