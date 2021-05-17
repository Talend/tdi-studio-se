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
package org.talend.designer.core.ui.editor.cmd;

import org.apache.commons.lang.ArrayUtils;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection;

/**
 * This class is used for encoding type changing command. <br/>
 *
 * $Id: EncodingTypeChangeMetadataCommand.java 2007-2-11,02:27:04 ftang $
 *
 */
public class EncodingTypeChangeCommand extends PropertyChangeCommand {

    private String oldType;

    private String newRealValue;

    private IElementParameter paramEncoding;

    private boolean fromCombo;

    public EncodingTypeChangeCommand(IElement elem, String propName, Object propValue, boolean fromCombo) {
        super(elem, propName, EmfComponent.ENCODING_TYPE_CUSTOM.equals(propValue) ? elem.getPropertyValue(propName) : propValue);
        newRealValue = (String) propValue;
        IElementParameter curParam = getElement().getElementParameter(getPropName());
        paramEncoding = curParam.getChildParameters().get(EParameterName.ENCODING_TYPE.getName());
        this.fromCombo = fromCombo;
    }

    @Override
    public void execute() {
        oldType = (String) paramEncoding.getValue();

        String tempValue = newRealValue;
        tempValue = tempValue.replaceAll("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
        tempValue = tempValue.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$

        boolean newIsCustomAndoldIsNotCustom = tempValue.equals(EmfComponent.ENCODING_TYPE_CUSTOM)
                && !paramEncoding.getValue().equals(EmfComponent.ENCODING_TYPE_CUSTOM);
        boolean newIsNotCustomAndoldIsCustom = !tempValue.equals(EmfComponent.ENCODING_TYPE_CUSTOM)
                && paramEncoding.getValue().equals(EmfComponent.ENCODING_TYPE_CUSTOM);
        boolean toRefresh = false;
        if (fromCombo && (newIsCustomAndoldIsNotCustom || newIsNotCustomAndoldIsCustom)) {
            toRefresh = true;
        }
        if (ArrayUtils.contains(paramEncoding.getListItemsValue(), tempValue)) {
            paramEncoding.setValue(tempValue);
        }
        if (toRefresh) {
            getElement().setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
            if (DynamicTabbedPropertySection.getLastPropertyUsed() != null) {
                DynamicTabbedPropertySection.getLastPropertyUsed().refresh();
            }
        }
        super.execute();

    }

    @Override
    public void undo() {
        if (!oldType.equals(EmfComponent.ENCODING_TYPE_CUSTOM)) {
            paramEncoding.setValue(oldType);
            paramEncoding.setShow(false);
        }
        super.undo();
    }
}
