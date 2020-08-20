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
package org.talend.sdk.component.studio.util;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public enum ETaCoKitImage implements IImage {

    TACOKIT_REPOSITORY_ICON(TaCoKitConst.IMAGE_TACOKIT_REPOSITORY_PATH),
    TACOKIT_CONFIGURATION_ICON(TaCoKitConst.IMAGE_TACOKIT_CONFIGURATION_PATH);

    private String path;

    ETaCoKitImage(final String path) {
        this.path = path;
    }

    @Override
    public Class getLocation() {
        return ETaCoKitImage.class;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
