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
package org.talend.sdk.component.studio.service;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.ServerManager;
import org.talend.sdk.component.studio.toolbar.ReloadAction;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.updates.runtime.service.ITaCoKitUpdateService;


public class TaCoKitService implements ITaCoKitService {

    @Override
    public void start() throws Exception {
        ServerManager.getInstance().start();
        try {
            TaCoKitUtil.registAllTaCoKitRepositoryTypes();
        } catch (Exception e) {
            Exception ex = Lookups.manager().getLoadException();
            if (ex == null) {
                Lookups.manager().setLoadException(e);
            }
            ExceptionHandler.process(e);
        }
    }

    @Override
    public boolean isStarted() throws Exception {
        return ServerManager.getInstance().isStarted();
    }

    @Override
    public String reload(IProgressMonitor monitor) throws Exception {
        String result = new ReloadAction().reload(monitor);
        try {
            TaCoKitUtil.registAllTaCoKitRepositoryTypes();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return result;
    }

    @Override
    public boolean isTaCoKitCar(File file, IProgressMonitor monitor) throws Exception {
        return ITaCoKitUpdateService.getInstance().isCar(file, monitor);
    }
}
