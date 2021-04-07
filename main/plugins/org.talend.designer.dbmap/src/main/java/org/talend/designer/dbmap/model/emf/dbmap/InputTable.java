/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Table</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getJoinType <em>Join Type</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getAlias <em>Alias</em>}</li>
 * </ul>
 *
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getInputTable()
 * @model
 * @generated
 */
public interface InputTable extends AbstaceDBInOutTable {
    /**
     * Returns the value of the '<em><b>Join Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Join Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Join Type</em>' attribute.
     * @see #setJoinType(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getInputTable_JoinType()
     * @model
     * @generated
     */
    String getJoinType();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getJoinType <em>Join Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Join Type</em>' attribute.
     * @see #getJoinType()
     * @generated
     */
    void setJoinType(String value);

    /**
     * Returns the value of the '<em><b>Alias</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Alias</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Alias</em>' attribute.
     * @see #setAlias(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getInputTable_Alias()
     * @model
     * @generated
     */
    String getAlias();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getAlias <em>Alias</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Alias</em>' attribute.
     * @see #getAlias()
     * @generated
     */
    void setAlias(String value);

} // InputTable
