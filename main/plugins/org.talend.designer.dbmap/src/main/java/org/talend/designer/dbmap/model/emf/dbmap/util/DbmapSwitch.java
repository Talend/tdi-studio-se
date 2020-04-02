/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;

import org.talend.designer.dbmap.model.emf.dbmap.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage
 * @generated
 */
public class DbmapSwitch<T> extends Switch<T> {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static DbmapPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DbmapSwitch() {
        if (modelPackage == null) {
            modelPackage = DbmapPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param ePackage the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case DbmapPackage.DB_MAP_DATA: {
                DBMapData dbMapData = (DBMapData)theEObject;
                T result = caseDBMapData(dbMapData);
                if (result == null) result = caseAbstractExternalData(dbMapData);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY: {
                DBMapperTableEntry dbMapperTableEntry = (DBMapperTableEntry)theEObject;
                T result = caseDBMapperTableEntry(dbMapperTableEntry);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE: {
                AbstractDBDataMapTable abstractDBDataMapTable = (AbstractDBDataMapTable)theEObject;
                T result = caseAbstractDBDataMapTable(abstractDBDataMapTable);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.ABSTACE_DB_IN_OUT_TABLE: {
                AbstaceDBInOutTable abstaceDBInOutTable = (AbstaceDBInOutTable)theEObject;
                T result = caseAbstaceDBInOutTable(abstaceDBInOutTable);
                if (result == null) result = caseAbstractDBDataMapTable(abstaceDBInOutTable);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.VAR_TABLE: {
                VarTable varTable = (VarTable)theEObject;
                T result = caseVarTable(varTable);
                if (result == null) result = caseAbstractDBDataMapTable(varTable);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.INPUT_TABLE: {
                InputTable inputTable = (InputTable)theEObject;
                T result = caseInputTable(inputTable);
                if (result == null) result = caseAbstaceDBInOutTable(inputTable);
                if (result == null) result = caseAbstractDBDataMapTable(inputTable);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.OUTPUT_TABLE: {
                OutputTable outputTable = (OutputTable)theEObject;
                T result = caseOutputTable(outputTable);
                if (result == null) result = caseAbstaceDBInOutTable(outputTable);
                if (result == null) result = caseAbstractDBDataMapTable(outputTable);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case DbmapPackage.FILTER_ENTRY: {
                FilterEntry filterEntry = (FilterEntry)theEObject;
                T result = caseFilterEntry(filterEntry);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>DB Map Data</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>DB Map Data</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseDBMapData(DBMapData object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>DB Mapper Table Entry</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>DB Mapper Table Entry</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseDBMapperTableEntry(DBMapperTableEntry object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Abstract DB Data Map Table</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Abstract DB Data Map Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAbstractDBDataMapTable(AbstractDBDataMapTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Abstace DB In Out Table</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Abstace DB In Out Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAbstaceDBInOutTable(AbstaceDBInOutTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Var Table</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Var Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseVarTable(VarTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Input Table</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Input Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseInputTable(InputTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Output Table</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Output Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseOutputTable(OutputTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Filter Entry</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Filter Entry</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseFilterEntry(FilterEntry object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Abstract External Data</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Abstract External Data</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAbstractExternalData(AbstractExternalData object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} //DbmapSwitch
