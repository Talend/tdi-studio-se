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
package org.talend.sdk.component.studio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class TemplatesExtractor {

    private final String templatesPath;

    private final String destinationFolder;

    private final String destinationSuffix;

    public TemplatesExtractor(final String templatesPath, final String destinationFolder, final String destinationSuffix) {
        this.templatesPath = templatesPath;
        this.destinationFolder = destinationFolder;
        this.destinationSuffix = destinationSuffix;
    }

    public void extract() throws IOException {
        final Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        final URL templateEntry = bundle.getEntry(templatesPath);
        final File destDir = new File(destinationFolder, destinationSuffix);
        final String templateFolderPath = FileLocator.toFileURL(templateEntry).getPath();
        final File templateFolder = new File(templateFolderPath);
        org.talend.utils.io.FilesUtils.copyDirectory(templateFolder, destDir);
    }

}
