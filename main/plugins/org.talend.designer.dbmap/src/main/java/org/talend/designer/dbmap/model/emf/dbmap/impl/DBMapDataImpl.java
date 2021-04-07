/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.talend.designer.core.model.utils.emf.talendfile.impl.AbstractExternalDataImpl;

import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;
import org.talend.designer.dbmap.model.emf.dbmap.VarTable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>DB Map Data</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl#getVarTables <em>Var Tables</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl#getInputTables <em>Input Tables</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl#getOutputTables <em>Output Tables</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DBMapDataImpl extends AbstractExternalDataImpl implements DBMapData {
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
     * The cached value of the '{@link #getInputTables() <em>Input Tables</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getInputTables()
     * @generated
     * @ordered
     */
    protected EList<InputTable> inputTables;

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
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DBMapDataImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DbmapPackage.Literals.DB_MAP_DATA;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<VarTable> getVarTables() {
        if (varTables == null) {
            varTables = new EObjectContainmentEList<VarTable>(VarTable.class, this, DbmapPackage.DB_MAP_DATA__VAR_TABLES);
        }
        return varTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<InputTable> getInputTables() {
        if (inputTables == null) {
            inputTables = new EObjectContainmentEList<InputTable>(InputTable.class, this, DbmapPackage.DB_MAP_DATA__INPUT_TABLES);
        }
        return inputTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<OutputTable> getOutputTables() {
        if (outputTables == null) {
            outputTables = new EObjectContainmentEList<OutputTable>(OutputTable.class, this, DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES);
        }
        return outputTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DbmapPackage.DB_MAP_DATA__VAR_TABLES:
                return ((InternalEList<?>)getVarTables()).basicRemove(otherEnd, msgs);
            case DbmapPackage.DB_MAP_DATA__INPUT_TABLES:
                return ((InternalEList<?>)getInputTables()).basicRemove(otherEnd, msgs);
            case DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES:
                return ((InternalEList<?>)getOutputTables()).basicRemove(otherEnd, msgs);
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
            case DbmapPackage.DB_MAP_DATA__VAR_TABLES:
                return getVarTables();
            case DbmapPackage.DB_MAP_DATA__INPUT_TABLES:
                return getInputTables();
            case DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES:
                return getOutputTables();
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
            case DbmapPackage.DB_MAP_DATA__VAR_TABLES:
                getVarTables().clear();
                getVarTables().addAll((Collection<? extends VarTable>)newValue);
                return;
            case DbmapPackage.DB_MAP_DATA__INPUT_TABLES:
                getInputTables().clear();
                getInputTables().addAll((Collection<? extends InputTable>)newValue);
                return;
            case DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES:
                getOutputTables().clear();
                getOutputTables().addAll((Collection<? extends OutputTable>)newValue);
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
            case DbmapPackage.DB_MAP_DATA__VAR_TABLES:
                getVarTables().clear();
                return;
            case DbmapPackage.DB_MAP_DATA__INPUT_TABLES:
                getInputTables().clear();
                return;
            case DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES:
                getOutputTables().clear();
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
            case DbmapPackage.DB_MAP_DATA__VAR_TABLES:
                return varTables != null && !varTables.isEmpty();
            case DbmapPackage.DB_MAP_DATA__INPUT_TABLES:
                return inputTables != null && !inputTables.isEmpty();
            case DbmapPackage.DB_MAP_DATA__OUTPUT_TABLES:
                return outputTables != null && !outputTables.isEmpty();
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
        DBMapDataImpl dbObj = (DBMapDataImpl) obj;
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
    
} //DBMapDataImpl
