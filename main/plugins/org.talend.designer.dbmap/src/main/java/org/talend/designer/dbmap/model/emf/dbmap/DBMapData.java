/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.common.util.EList;

import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>DB Map Data</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getVarTables <em>Var Tables</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getInputTables <em>Input Tables</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getOutputTables <em>Output Tables</em>}</li>
 * </ul>
 *
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapData()
 * @model
 * @generated
 */
public interface DBMapData extends AbstractExternalData {
    /**
     * Returns the value of the '<em><b>Var Tables</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.dbmap.model.emf.dbmap.VarTable}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Var Tables</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Var Tables</em>' containment reference list.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapData_VarTables()
     * @model containment="true"
     * @generated
     */
    EList<VarTable> getVarTables();

    /**
     * Returns the value of the '<em><b>Input Tables</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.dbmap.model.emf.dbmap.InputTable}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Input Tables</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Input Tables</em>' containment reference list.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapData_InputTables()
     * @model containment="true"
     * @generated
     */
    EList<InputTable> getInputTables();

    /**
     * Returns the value of the '<em><b>Output Tables</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.dbmap.model.emf.dbmap.OutputTable}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Output Tables</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Output Tables</em>' containment reference list.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapData_OutputTables()
     * @model containment="true"
     * @generated
     */
    EList<OutputTable> getOutputTables();

} // DBMapData
