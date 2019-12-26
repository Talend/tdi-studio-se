// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
                String finalValue=labelText.getText()+e.text;
                if(inputValue.length()>1) {
                    finalValue=inputValue;
                    //original value need to be show here no need to be check
                }else {
                    //support Backspace button 
                    if(inputValue.isEmpty()) {
                        return;
                    }
                    Pattern pattern = Pattern.compile("^\\.|\\d$"); //$NON-NLS-1$
                    Pattern finalPattern = Pattern.compile("^1(\\.0{1,5})?|0(\\.\\d{1,5})?$"); //$NON-NLS-1$
                    Pattern exceptionPattern = Pattern.compile("^1\\d+|0\\d+$"); //$NON-NLS-1$
                    //when input character one by one then make sure they are valid
                    if (inputValue.length()<=1&&!pattern.matcher(inputValue).matches()) {
                        e.doit = false;
                        return;
                    }else if(".".equals(inputValue)&&labelText.getText().length()>1) {
                        e.doit = false;
                        return;
                    }else {
                        //when the text of original value is empty then we need to make sure the finalValue is valid
                        if(StringUtils.isBlank(labelText.getText())&&!finalPattern.matcher(finalValue).matches()) {
                            e.doit = false;
                            return;
                        }
                        //when the text of original value is not empty we need to make sure both original value and final value are valid
                        if(!StringUtils.isBlank(labelText.getText())&&!finalPattern.matcher(labelText.getText()).matches()&&!finalPattern.matcher(labelText.getText()+inputValue).matches()) {
                            e.doit = false;
                            return;
                        }
                        //make sure the final value is not special value
                        if(exceptionPattern.matcher(labelText.getText()+inputValue).matches()) {
                            e.doit = false;
                            return;
                        }
                    }
                }
                try {
                    double num=Double.valueOf(finalValue);
                    if(num>1) {
                        e.doit = false;
                        return;
                    }
                } catch (Exception e1) {
                    if(inputValue.length()>1) {
                        e.doit = false;
                        return;
                        
                    }
                }
            }
        });
        return currentControl;
    }

  
    
    
    
    
    
    
    

}
