/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.talend.designer.core.model.utils.emf.talendfile.impl.AbstractExternalDataImpl;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.model.emf.mapper.UiProperties;
import org.talend.designer.mapper.model.emf.mapper.VarTable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl#getUiProperties <em>Ui Properties</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl#getVarTables <em>Var Tables</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl#getOutputTables <em>Output Tables</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl#getInputTables <em>Input Tables</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MapperDataImpl extends AbstractExternalDataImpl implements MapperData {
    /**
     * The cached value of the '{@link #getUiProperties() <em>Ui Properties</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUiProperties()
     * @generated
     * @ordered
     */
    protected UiProperties uiProperties;

    /**
     * The cached value of the '{@link #getVarTables() <em>Var Tables</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVarTables()
     * @generated
     * @ordered
     */
    protected EList<VarTable> varTables;

    /**
     * The cached value of the '{@link #getOutputTables() <em>Output Tables</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOutputTables()
     * @generated
     * @ordered
     */
    protected EList<OutputTable> outputTables;

    /**
     * The cached value of the '{@link #getInputTables() <em>Input Tables</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getInputTables()
     * @generated
     * @ordered
     */
    protected EList<InputTable> inputTables;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected MapperDataImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MapperPackage.Literals.MAPPER_DATA;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public UiProperties getUiProperties() {
        return uiProperties;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetUiProperties(UiProperties newUiProperties, NotificationChain msgs) {
        UiProperties oldUiProperties = uiProperties;
        uiProperties = newUiProperties;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MapperPackage.MAPPER_DATA__UI_PROPERTIES, oldUiProperties, newUiProperties);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setUiProperties(UiProperties newUiProperties) {
        if (newUiProperties != uiProperties) {
            NotificationChain msgs = null;
            if (uiProperties != null)
                msgs = ((InternalEObject)uiProperties).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MapperPackage.MAPPER_DATA__UI_PROPERTIES, null, msgs);
            if (newUiProperties != null)
                msgs = ((InternalEObject)newUiProperties).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MapperPackage.MAPPER_DATA__UI_PROPERTIES, null, msgs);
            msgs = basicSetUiProperties(newUiProperties, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.MAPPER_DATA__UI_PROPERTIES, newUiProperties, newUiProperties));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<VarTable> getVarTables() {
        if (varTables == null) {
            varTables = new EObjectContainmentEList<VarTable>(VarTable.class, this, MapperPackage.MAPPER_DATA__VAR_TABLES);
        }
        return varTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<OutputTable> getOutputTables() {
        if (outputTables == null) {
            outputTables = new EObjectContainmentEList<OutputTable>(OutputTable.class, this, MapperPackage.MAPPER_DATA__OUTPUT_TABLES);
        }
        return outputTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<InputTable> getInputTables() {
        if (inputTables == null) {
            inputTables = new EObjectContainmentEList<InputTable>(InputTable.class, this, MapperPackage.MAPPER_DATA__INPUT_TABLES);
        }
        return inputTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case MapperPackage.MAPPER_DATA__UI_PROPERTIES:
                return basicSetUiProperties(null, msgs);
            case MapperPackage.MAPPER_DATA__VAR_TABLES:
                return ((InternalEList<?>)getVarTables()).basicRemove(otherEnd, msgs);
            case MapperPackage.MAPPER_DATA__OUTPUT_TABLES:
                return ((InternalEList<?>)getOutputTables()).basicRemove(otherEnd, msgs);
            case MapperPackage.MAPPER_DATA__INPUT_TABLES:
                return ((InternalEList<?>)getInputTables()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case MapperPackage.MAPPER_DATA__UI_PROPERTIES:
                return getUiProperties();
            case MapperPackage.MAPPER_DATA__VAR_TABLES:
                return getVarTables();
            case MapperPackage.MAPPER_DATA__OUTPUT_TABLES:
                return getOutputTables();
            case MapperPackage.MAPPER_DATA__INPUT_TABLES:
                return getInputTables();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case MapperPackage.MAPPER_DATA__UI_PROPERTIES:
                setUiProperties((UiProperties)newValue);
                return;
            case MapperPackage.MAPPER_DATA__VAR_TABLES:
                getVarTables().clear();
                getVarTables().addAll((Collection<? extends VarTable>)newValue);
                return;
            case MapperPackage.MAPPER_DATA__OUTPUT_TABLES:
                getOutputTables().clear();
                getOutputTables().addAll((Collection<? extends OutputTable>)newValue);
                return;
            case MapperPackage.MAPPER_DATA__INPUT_TABLES:
                getInputTables().clear();
                getInputTables().addAll((Collection<? extends InputTable>)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case MapperPackage.MAPPER_DATA__UI_PROPERTIES:
                setUiProperties((UiProperties)null);
                return;
            case MapperPackage.MAPPER_DATA__VAR_TABLES:
                getVarTables().clear();
                return;
            case MapperPackage.MAPPER_DATA__OUTPUT_TABLES:
                getOutputTables().clear();
                return;
            case MapperPackage.MAPPER_DATA__INPUT_TABLES:
                getInputTables().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case MapperPackage.MAPPER_DATA__UI_PROPERTIES:
                return uiProperties != null;
            case MapperPackage.MAPPER_DATA__VAR_TABLES:
                return varTables != null && !varTables.isEmpty();
            case MapperPackage.MAPPER_DATA__OUTPUT_TABLES:
                return outputTables != null && !outputTables.isEmpty();
            case MapperPackage.MAPPER_DATA__INPUT_TABLES:
                return inputTables != null && !inputTables.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated not
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapperDataImpl dbObj = (MapperDataImpl) obj;
        EList<InputTable> inputs = dbObj.getInputTables();
        EList<OutputTable> outputs =dbObj.getOutputTables();
        EList<VarTable> vars =dbObj.getVarTables();
        if(inputs.size() != getInputTables().size()){
            return false;
        }
        if(outputs.size() != getOutputTables().size()){
            return false;
        }
        if(vars.size() != getVarTables().size()){
            return false;
        }
        for(InputTable inputTable:inputTables){
            boolean found = false;
            for(InputTable input:inputs){
                if(inputTable.getName().equals(input.getName())){
                    found = true;
                    if(!inputTable.equals(input)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        
        for(OutputTable outputTable:outputTables){
            boolean found = false;
            for(OutputTable output:outputs){
                if(outputTable.getName().equals(output.getName())){
                    found = true;
                    if(!outputTable.equals(output)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        
        for(VarTable varTable:varTables){
            boolean found = false;
            for(VarTable var:vars){
                if(varTable.getName().equals(var.getName())){
                    found = true;
                    if(!varTable.equals(var)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        return true;
    }
} //MapperDataImpl
