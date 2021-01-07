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
package org.talend.designer.runprocess.maven.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.documentation.ERepositoryActionName;

public class CodesJarChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("update codesjar by " + propertyName) { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    if (propertyName.equals(ERepositoryActionName.PROPERTIES_CHANGE.getName())) {
                        casePropertiesChange(oldValue, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())
                            || propertyName.equals(ERepositoryActionName.DELETE_TO_RECYCLE_BIN.getName())) {
                        caseDelete(propertyName, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.SAVE.getName())
                            || propertyName.equals(ERepositoryActionName.CREATE.getName())) {
                        caseCreateOrSave(newValue);
                    } else if (propertyName.equals(ERepositoryActionName.IMPORT.getName())) {
                        caseImport(propertyName, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.RESTORE.getName())) {
                        caseRestore(newValue);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    private void casePropertiesChange(Object oldValue, Object newValue) throws Exception {
        if (oldValue instanceof String[] && newValue instanceof Property) {
            Property property = (Property) newValue;
            if (!needUpdate(property.getItem())) {
                return;
            }
            String[] oldFields = (String[]) oldValue;
            String oldName = oldFields[0];
            String oldVersion = oldFields[1];
            CodesJarResourceCache.updateCache(null, oldName, oldVersion, property);
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
            IFolder folder = new AggregatorPomsHelper().getCodeFolder(type).getFolder(oldName);
            RenameResourceChange change = new RenameResourceChange(folder.getFullPath(), property.getLabel());
            change.perform(new NullProgressMonitor());
            TalendJavaProjectManager.deleteTalendCodesJarProject(type,
                    ProjectManager.getInstance().getProject(property).getTechnicalLabel(), oldName, true);
            CodesJarM2CacheManager.updateCodesJarProject(property);
        }
    }

    private void caseDelete(String propertyName, Object newValue) {
        // TODO for move to bin, not here but need to move all inner codes too
        // need to show innercode items in bin but no context menu for it
        if (newValue instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) newValue).getProperty();
            if (needUpdate(property.getItem())) {
                CodesJarResourceCache.removeCache(property);
                if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
                    TalendJavaProjectManager.deleteTalendCodesJarProject(property, true);
                }
            }
        }
    }

    private void caseCreateOrSave(Object newValue) throws Exception {
        if (newValue instanceof Item) {
            Item item = (Item) newValue;
            if (needUpdate(item)) {
                CodesJarResourceCache.addToCache(item.getProperty());
            }
        }
    }

    private void caseImport(String propertyName, Object newValue) {
        if (newValue instanceof Set) {
            Set<Item> importItems = (Set<Item>) newValue;
            importItems.stream().filter(item -> needUpdate(item))
                    .forEach(item -> CodesJarResourceCache.addToCache(item.getProperty()));
        }
    }

    private void caseRestore(Object newValue) {
        if (newValue instanceof IRepositoryViewObject) {
            IRepositoryViewObject object = (IRepositoryViewObject) newValue;
            if (needUpdate(object.getProperty().getItem())) {
                CodesJarResourceCache.addToCache(object.getProperty());
            }
        }
    }

    private boolean needUpdate(Item item) {
        return item instanceof RoutinesJarItem;
    }

}
