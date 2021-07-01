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

import org.apache.commons.lang3.StringUtils;
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

	protected boolean compareGAVCT(GAV gav) {
		GAV tempGAV = new GAV();
		tempGAV.setGroupId(getComponentGroupId());
		tempGAV.setArtifactId(getComponenArtifactId());
		tempGAV.setVersion(getComponenVersion());
		tempGAV.setClassifier(getComponentClassifier());
		tempGAV.setType(getComponentType());

		return tempGAV.equals(gav);
	}

	@Override
	public boolean needInstall() {

		try {
			List<GAV> gavs = TaCoKitUtil.getInstalledComponents(new NullProgressMonitor());
			for (GAV gav : gavs) {
				if (compareGAVCT(gav)) {
					LOGGER.error("Component {0} was already installed", gav.toString());
					return false;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Get installed components error", e);
		}

		return true;
	}

	@Override
	public boolean install(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		Set<File> files = new HashSet<File>();
		files.add(getCarFile());
		try {
			ICarInstallationResult result = ITaCoKitUpdateService.getInstance().installCars(files, true, monitor);
			IStatus stat = result.getInstalledStatus().get(getCarFile());
			if (stat.getCode() == IStatus.OK) {
				LOGGER.info("TCK Component installed: {0}", getCarFile());
				return true;
			} else {
				LOGGER.error("install car failure: {0}", getCarFile());
			}
		} catch (Exception e) {
			LOGGER.error("install car failure" + getCarFile(), e);
		}
		return false;
	}

}
