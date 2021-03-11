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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.hadoop.version.custom.ECustomVersionType;
import org.talend.core.hadoop.version.custom.HadoopCustomVersionDefineDialog;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC zwzhao class global comment. Detailled comment
 */
public class HadoopJarSetupController extends AbstractElementPropertySectionController {

    private ECustomVersionType versionType;

    /**
     * DOC zwzhao HadoopJarSetupController constructor comment.
     *
     * @param dp
     */
    public HadoopJarSetupController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#init(org.talend
     * .core.properties.tab.IDynamicProperty)
     */
    @Override
    public void init(IDynamicProperty dp) {
        super.init(dp);
        initHadoopVersionType();
    }

    private void initHadoopVersionType() {
        if (elem instanceof Node) {
            Node node = (Node) elem;
            String compName = node.getComponent().getName();

            if (compName.startsWith("tPig")) { //$NON-NLS-1$
                versionType = ECustomVersionType.PIG;
                IElementParameter elementParameter = node.getElementParameter("LOAD"); //$NON-NLS-1$
                if (elementParameter != null) {
                    String value = elementParameter.getListItemsDisplayCodeName()[elementParameter.getIndexOfItemFromList(String
                            .valueOf(elementParameter.getValue()))];
                    if ("HCATLOADER".equals(value)) { //$NON-NLS-1$
                        versionType = ECustomVersionType.PIG_HCATALOG;
                    } else if ("HBASESTORAGE".equals(value)) { //$NON-NLS-1$
                        versionType = ECustomVersionType.PIG_HBASE;
                    }
                }
            } else if (compName.startsWith("tImpala")) {
                versionType = ECustomVersionType.HIVE;
            } else {
                compName = compName.substring(1).toUpperCase();
                ECustomVersionType[] versions = ECustomVersionType.values();
                for (ECustomVersionType version : versions) {
                    if (compName.startsWith(version.getName())) {
                        versionType = version;
                        break;
                    }
                }
            }
        }
        if (versionType == null) {
            if (elem instanceof org.talend.designer.core.ui.editor.process.Process) {
                Object frameworkObj = ((org.talend.designer.core.ui.editor.process.Process) elem).getAdditionalProperties().get(
                        HadoopConstants.FRAMEWORK);

                if (frameworkObj != null) {
                    String framework = frameworkObj.toString();
                    if (HadoopConstants.FRAMEWORK_SPARK.equals(framework)) {
                        versionType = ECustomVersionType.SPARK;
                    } else if (HadoopConstants.FRAMEWORK_SPARKSTREAMING.equals(framework)) {
                        versionType = ECustomVersionType.SPARK_STREAMING;
                    }
                }

            }
            if (versionType == null) {
                versionType = ECustomVersionType.MAP_REDUCE;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, final IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {

        final Composite container = subComposite;
        Button subButton = getWidgetFactory().createButton(container, "", SWT.PUSH); //$NON-NLS-1$
        subButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                initHadoopVersionType();
                boolean readonly = false;
                String readOnlyIfString = param.getReadOnlyIf();
                if (StringUtils.isNotEmpty(readOnlyIfString)) {
                    if (param.isReadOnly(elem.getElementParameters())) {
                        readonly = true;
                    }
                }
                // if readonly is true, then needn't to do this check, since it's aim is check readonly also
                if (readonly == false) {
                    IElementParameter propertyParameter = elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName());
                    if (propertyParameter != null) {
                        if (EmfComponent.REPOSITORY.equals(propertyParameter.getValue())) {
                            readonly = true;
                        }
                    }
                }
                HadoopCustomVersionDefineDialog customVersionDialog = new HadoopCustomVersionDefineDialog(PlatformUI
                        .getWorkbench().getActiveWorkbenchWindow().getShell(), getCustomVersionMap()) {

                    @Override
                    protected ECustomVersionType[] getDisplayTypes() {
                        return new ECustomVersionType[] { versionType };
                    }
                };

                IElementParameter sparkLocalParam = elem.getElementParameter(HadoopConstants.SPARK_LOCAL_MODE);
                IElementParameter sparkParam = elem.getElementParameter(HadoopConstants.SPARK_MODE);
                boolean isSparkLocalMode = false;

                if (sparkLocalParam != null) {
                    isSparkLocalMode = (Boolean) sparkLocalParam.getValue();
                }

                if (sparkParam != null) {
                    String sparkMode = null;
                    if (isSparkLocalMode) {
                        sparkMode = "LOCAL"; //$NON-NLS-1$
                    } else {
                        sparkMode = "" + sparkParam.getValue(); //$NON-NLS-1$
                    }
                    customVersionDialog.setSparkMode(sparkMode);
                    customVersionDialog.setSparkStreamingMode(sparkMode);
                }
                customVersionDialog.setReadonly(readonly);
                Set<String> oldLibList = customVersionDialog.getLibList(versionType.getGroup());
                if (customVersionDialog.open() == Window.OK) {
                    Set<String> newLibList = customVersionDialog.getLibList(versionType.getGroup());
                    if (oldLibList != null && newLibList != null && oldLibList.size() == newLibList.size()
                            && oldLibList.containsAll(newLibList)) {
                        // means nothing changes, so nothing need to do
                    } else {
                        // changed
                        String customJars = customVersionDialog.getLibListStr(versionType.getGroup());
                        executeCommand(new PropertyChangeCommand(elem, EParameterName.HADOOP_CUSTOM_JARS.getName(), StringUtils
                                .trimToEmpty(customJars)));
                    }
                }
            }

        });
        subButton.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        FormData data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        subButton.setLayoutData(data);

        return container;
    }

    private Map<String, Set<String>> getCustomVersionMap() {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        String customJars = (String) elem.getPropertyValue(EParameterName.HADOOP_CUSTOM_JARS.getName());
        if (StringUtils.isNotEmpty(customJars)) {
            Set<String> jarSet = new HashSet<String>();
            String[] jarArray = customJars.split(";"); //$NON-NLS-1$
            for (String jar : jarArray) {
                jarSet.add(jar);
            }
            map.put(versionType.getGroup().getName(), jarSet);
        }

        return map;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub

    }

}
