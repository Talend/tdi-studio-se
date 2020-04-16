/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Abstract DB Data Map Table</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isMinimized <em>Minimized</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isReadonly <em>Readonly</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getDBMapperTableEntries <em>DB Mapper Table Entries</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getTableName <em>Table Name</em>}</li>
 * </ul>
 *
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable()
 * @model
 * @generated
 */
public interface AbstractDBDataMapTable extends EObject {
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
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Minimized</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Minimized</em>' attribute.
     * @see #setMinimized(boolean)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable_Minimized()
     * @model
     * @generated
     */
    boolean isMinimized();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isMinimized <em>Minimized</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Minimized</em>' attribute.
     * @see #isMinimized()
     * @generated
     */
    void setMinimized(boolean value);

    /**
     * Returns the value of the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Readonly</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Readonly</em>' attribute.
     * @see #setReadonly(boolean)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable_Readonly()
     * @model
     * @generated
     */
    boolean isReadonly();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isReadonly <em>Readonly</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Readonly</em>' attribute.
     * @see #isReadonly()
     * @generated
     */
    void setReadonly(boolean value);

    /**
     * Returns the value of the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>DB Mapper Table Entries</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>DB Mapper Table Entries</em>' containment reference list.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable_DBMapperTableEntries()
     * @model containment="true"
     * @generated
     */
    EList<DBMapperTableEntry> getDBMapperTableEntries();

    /**
     * Returns the value of the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Table Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Table Name</em>' attribute.
     * @see #setTableName(String)
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getAbstractDBDataMapTable_TableName()
     * @model
     * @generated
     */
    String getTableName();

    /**
     * Sets the value of the '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getTableName <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Table Name</em>' attribute.
     * @see #getTableName()
     * @generated
     */
    void setTableName(String value);

} // AbstractDBDataMapTable
