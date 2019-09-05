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
package org.talend.sdk.component.studio.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.AbstractComponentsProvider;
// import org.talend.sdk.component.studio.util.TaCoKitConst;

public class TaCoKitComponentsProvider extends AbstractComponentsProvider {

    public TaCoKitComponentsProvider() {
        // nothing to do
    }

    @Override
    protected File getExternalComponentsLocation() {
        URL url = FileLocator.find(Platform.getBundle("org.talend.designer.codegen"), new Path(getFolderName()), null);
        // TODO: Fix this part. We need to remove this extension point if server is not started.
        if (url == null) {
            return null;
        }
        URL fileUrl;
        try {
            fileUrl = FileLocator.toFileURL(url);
            return new File(fileUrl.getPath());
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    @Override
    public String getFolderName() {
        return "tacokit/components"; //$NON-NLS-1$
    }

    public boolean isCustom() {
        return true;
    }

    public String getComponentsBundle() {
        return "org.talend.designer.codegen";
    }

    public String getComponentsLocation() {
        return getFolderName();
    }

}
