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
package org.talend.sdk.component.studio.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.core.model.utils.BaseComponentInstallerTask;
import org.talend.sdk.component.studio.util.TaCoKitUtil.GAV;
import org.talend.updates.runtime.service.ITaCoKitUpdateService;
import org.talend.updates.runtime.service.ITaCoKitUpdateService.ICarInstallationResult;

/**
 * @author bhe created on Jul 1, 2021
 *
 */
public class TCKComponentInstallerTask extends BaseComponentInstallerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCKComponentInstallerTask.class);

    /**
     * Get car file
     * 
     * @return car file
     */
    protected File getCarFile() {
        return null;
    }

    protected boolean compareGAVCT(GAV componentGAV, GAV installedGAV) {

        return componentGAV.equals(installedGAV);
    }

    protected GAV getComponentGAV() {
        GAV tempGAV = new GAV();
        tempGAV.setGroupId(getComponentGroupId());
        tempGAV.setArtifactId(getComponenArtifactId());
        tempGAV.setVersion(getComponenVersion());
        tempGAV.setClassifier(getComponentClassifier());
        tempGAV.setType(getComponentType());
        return tempGAV;
    }

    @Override
    public boolean needInstall() {
        GAV compGAV = getComponentGAV();
        try {
            List<GAV> gavs = TaCoKitUtil.getInstalledComponents(new NullProgressMonitor());
            for (GAV gav : gavs) {
                if (compareGAVCT(compGAV, gav)) {
                    LOGGER.error("Component: {} was already installed", gav.toString());
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Get installed components error", e);
        }
        LOGGER.info("Component: {} is going to be installed", compGAV.toString());
        return true;
    }

    @Override
    public boolean install(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        Set<File> files = new HashSet<File>();
        File carFile = getCarFile();
        if (carFile != null) {
            LOGGER.info("Car file: {} was added", carFile);
            files.add(carFile);
        }
        if (files.isEmpty()) {
            LOGGER.info("No car files");
            return false;
        }
        try {
            ICarInstallationResult result = ITaCoKitUpdateService.getInstance().installCars(files, true, monitor);
            IStatus stat = result.getInstalledStatus().get(carFile);
            if (stat.getCode() == IStatus.OK) {
                LOGGER.info("TCK Component installed: {}", carFile);
                return true;
            } else {
                LOGGER.error("Failed to install car: {}", carFile);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to install car: {}", carFile, e);
        }
        return false;
    }

}
