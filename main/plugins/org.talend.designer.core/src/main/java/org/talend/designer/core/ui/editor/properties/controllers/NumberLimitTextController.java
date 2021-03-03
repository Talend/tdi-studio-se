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

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;

/**
 * The value of text field need to be is [0,1]
 * And the length of decimal is less than 6
 * 
 */
public class NumberLimitTextController extends TextController {
    
    final static public String CONTEXPREFIX="context.";

    public NumberLimitTextController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control currentControl=super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        String paramName = param.getName();
        final Text labelText = (Text) hashCurControls.get(paramName);
        labelText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                String inputValue = e.text;
                StringBuffer buffer = new StringBuffer(labelText.getText());
                buffer.delete(e.start, e.end).insert(e.start, e.text);
                String finalValue=buffer.toString();
                if(finalValue.startsWith(CONTEXPREFIX)||CONTEXPREFIX.startsWith(finalValue)) {
                  //context mode no any limit
                  return;
                }else if(finalValue.length()>=9){
                    //keep 6 digit after the Decimal point
                    e.doit = false;
                    return;
                }else if("-".equals(inputValue)) {
                    //'-' is invalid input 
                    e.doit = false;
                    return;
                }

                try {
                    double num=Double.valueOf(buffer.toString());
                    if(num>1||num<0) {
                        e.doit = false;
                        return;
                    }
                } catch (Exception e1) {
                    // any exception case need to be ignore
                    e.doit = false;
                    return;
                }
            }
        });
        return currentControl;
    }

  
    
    
    
    
    
    
    

}
