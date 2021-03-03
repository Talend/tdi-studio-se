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
package org.talend.designer.core.ui.routine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ProjectManager;

/**
 * ggu class global comment. Detailled comment
 */
public class ShowRoutineItemsLabelProvider extends LabelProvider implements IColorProvider, ITreeContentProvider,
        ITableLabelProvider {

    private List<RoutineItemRecord> existedRecord = null;

    private Map<Project, List<Property>> allItems = null;

    public ShowRoutineItemsLabelProvider(Map<Project, List<Property>> allItems, List<RoutineItemRecord> existedRecord) {
        super();
        this.existedRecord = existedRecord;
        this.allItems = allItems;
    }

    public Object[] getElements(Object inputElement) {
        Set<Project> projects = allItems.keySet();
        List<Object> objects = new ArrayList<Object>();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        for (Project p : projects) {
            if (p.equals(currentProject)) {
                List<Property> items = allItems.get(p);
                if (items != null) {
                    objects.addAll(items);
                }
            } else {
                objects.add(p);
            }
        }
        return objects.toArray();
    }

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Project) {
            List<Property> list = allItems.get(parentElement);
            if (list != null) {
                return list.toArray();
            }
        }
        return null;
    }

    public Object getParent(Object element) {
        return null;
    }

    public boolean hasChildren(Object element) {
        Object[] children = getChildren(element);
        return children != null && children.length > 0;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

    public Image getColumnImage(Object element, int columnIndex) {
        return getImage(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof Project) {
            return ImageProvider.getImage(ECoreImage.REFERENCED_ICON);
        }
        return super.getImage(element);
    }

    public String getColumnText(Object element, int columnIndex) {
        return getText(element);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof Project) {
            return ((Project) element).getLabel();
        } else if (element instanceof Property) {
            Property property = (Property) element;
            return property.getLabel();
        }
        return super.getText(element);
    }

    public Color getBackground(Object element) {
        if (existed(element)) {
            Display disp = Display.getCurrent();
            if (disp == null) {
                disp = Display.getDefault();
            }
            return disp.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        }
        return null;
    }

    public Color getForeground(Object element) {
        if (existed(element)) {
            Display disp = Display.getCurrent();
            if (disp == null) {
                disp = Display.getDefault();
            }
            return disp.getSystemColor(SWT.COLOR_DARK_GRAY);
        }
        return null;
    }

    public boolean existed(Object element) {
        if (existedRecord != null && element != null && element instanceof Property) {
            Item item = ((Property) element).getItem();
            String itemType = ERepositoryObjectType.getItemType(item).name();
            if (item instanceof RoutineItem) {
                for (RoutineItemRecord record : existedRecord) {
                    if (item.getProperty().getLabel().equals(record.getLabel())
                            || item.getProperty().getId().equals(record.getId())) {
                        return true;
                    }
                }
            } else if (item instanceof RoutinesJarItem) {
                for (RoutineItemRecord record : existedRecord) {
                    if (itemType.equals(record.getType()) && (item.getProperty().getLabel().equals(record.getLabel())
                            || item.getProperty().getId().equals(record.getId()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
