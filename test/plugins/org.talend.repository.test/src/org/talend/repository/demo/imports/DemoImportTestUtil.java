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
package org.talend.repository.demo.imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.osgi.framework.Bundle;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.ui.actions.importproject.DemoProjectBean;

/**
 * created by ldong on 2014-4-29 Detailled comment
 *
 */
public class DemoImportTestUtil {

	public static List<ResourcesManager> getResourceManagers(List<DemoProjectBean> checkedProjectBean) {
		List<ResourcesManager> resManagers = new ArrayList<ResourcesManager>();
		try {
			for (DemoProjectBean pro : checkedProjectBean) {
				ResourcesManager resManager = null;

				Bundle bundle = Platform.getBundle(pro.getPluginId());
				URL demoURL = FileLocator.find(bundle, new Path(pro.getDemoProjectFilePath()), null);
				demoURL = FileLocator.toFileURL(demoURL);
				String filePath = new Path(demoURL.getFile()).toOSString();
				File srcFile = new File(filePath);
				FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance()
						.createFileUnityManager(srcFile);
				resManager = fileUnityManager.doUnify();
				if (resManager != null) {
					resManagers.add(resManager);
				}
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TarException e) {
			e.printStackTrace();
		}
		return resManagers;
	}

	public static ResourcesManager getResourceManager(DemoProjectBean checkedProjectBean) {
		ResourcesManager resManager = null;
		try {
			Bundle bundle = Platform.getBundle(checkedProjectBean.getPluginId());
			URL demoURL = FileLocator.find(bundle, new Path(checkedProjectBean.getDemoProjectFilePath()), null);
			demoURL = FileLocator.toFileURL(demoURL);
			String filePath = new Path(demoURL.getFile()).toOSString();
			File srcFile = new File(filePath);
			FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance()
					.createFileUnityManager(srcFile);
			resManager = fileUnityManager.doUnify();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TarException e) {
			e.printStackTrace();
		}
		return resManager;
	}

	public static List<File> collectProjectFilesFromDirectory(File file, String filesExtension, boolean nested) {
		List<File> results = new ArrayList<File>();
		if (file.isFile()) {
			boolean canAdd = true;
			if ((filesExtension != null && !file.getName().endsWith(filesExtension))) {
				canAdd = false;
			}
			if (canAdd) {
				results.add(file);
			}
		} else if (file.isDirectory() && nested) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				results.addAll(collectProjectFilesFromDirectory(file2, filesExtension, nested));
			}
		}

		return results;
	}
}
