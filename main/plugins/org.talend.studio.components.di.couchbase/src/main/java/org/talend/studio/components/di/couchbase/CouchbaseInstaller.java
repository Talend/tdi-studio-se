// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.studio.components.di.couchbase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.studio.util.TCKComponentInstallerTask;

/**
 * @author bhe created on Jul 1, 2021
 *
 */
public class CouchbaseInstaller extends TCKComponentInstallerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseInstaller.class);

    @Override
    protected File getCarFile() {
        URL carFolder = FileLocator.find(FrameworkUtil.getBundle(CouchbaseInstaller.class), new Path("car"), null);
        File carDir = null;
        if (carFolder != null) {
            try {
                carDir = new File(FileLocator.toFileURL(carFolder).getPath());
                if (carDir.isDirectory()) {
                    File[] cars = carDir.listFiles();
                    LOGGER.info("Files found: {}", Arrays.toString(cars));
                    
                    Optional<File> carFile = Stream.of(cars).filter(f -> f.getName().endsWith(".car")).findFirst();
                    File carRet = carFile.isPresent() ? carFile.get() : null;
                    if (carRet != null) {
                        LOGGER.info("car file: {}", carRet.toString());
                        return carRet;
                    }
                }

            } catch (IOException e) {
                LOGGER.error("Can't find car file", e);
            }
        }
        LOGGER.error("Can't find car file from folder {}", carDir);
        return null;
    }

}
