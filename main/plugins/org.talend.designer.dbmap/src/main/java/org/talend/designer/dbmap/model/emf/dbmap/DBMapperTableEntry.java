/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>DB Mapper Table Entry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getType <em>Type</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isJoin <em>Join</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getOperator <em>Operator</em>}</li>
 * </ul>
 *
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry()
 * @model
 * @generated
 */
public interface DBMapperTableEntry extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Expression</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Expression</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Expression</em>' attribute.
     * @see #setExpression(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Expression()
     * @model ordered="false"
     * @generated
     */
    String getExpression();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getExpression <em>Expression</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Expression</em>' attribute.
     * @see #getExpression()
     * @generated
     */
    void setExpression(String value);

    /**
     * Returns the value of the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type</em>' attribute.
     * @see #setType(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Type()
     * @model
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

    /**
     * Returns the value of the '<em><b>Nullable</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Nullable</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Nullable</em>' attribute.
     * @see #setNullable(boolean)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Nullable()
     * @model ordered="false"
     * @generated
     */
    boolean isNullable();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isNullable <em>Nullable</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Nullable</em>' attribute.
     * @see #isNullable()
     * @generated
     */
    void setNullable(boolean value);

    /**
     * Returns the value of the '<em><b>Join</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Join</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Join</em>' attribute.
     * @see #setJoin(boolean)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Join()
     * @model
     * @generated
     */
    boolean isJoin();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isJoin <em>Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Join</em>' attribute.
     * @see #isJoin()
     * @generated
     */
    void setJoin(boolean value);

    /**
     * Returns the value of the '<em><b>Operator</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Operator</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Operator</em>' attribute.
     * @see #setOperator(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getDBMapperTableEntry_Operator()
     * @model
     * @generated
     */
    String getOperator();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getOperator <em>Operator</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Operator</em>' attribute.
     * @see #getOperator()
     * @generated
     */
    void setOperator(String value);

} // DBMapperTableEntry
