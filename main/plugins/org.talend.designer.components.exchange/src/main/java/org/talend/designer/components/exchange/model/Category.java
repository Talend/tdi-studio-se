/**
 * // ============================================================================
 * //
 * // Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 * //
 * // This source code is available under agreement available at
 * // %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * //
 * // You should have received a copy of the agreement
 * // along with this program; if not, write to Talend SA
 * // 9 rue Pages 92150 Suresnes, France
 * //
 * // ============================================================================
 */
package org.talend.designer.components.exchange.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Category</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.Category#getCategoryId <em>Category Id</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.Category#getCategoryName <em>Category Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.components.exchange.model.ExchangePackage#getCategory()
 * @model
 * @generated
 */
public interface Category extends EObject {
    /**
     * Returns the value of the '<em><b>Category Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Category Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Category Id</em>' attribute.
     * @see #setCategoryId(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getCategory_CategoryId()
     * @model required="true"
     * @generated
     */
    String getCategoryId();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.Category#getCategoryId <em>Category Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Category Id</em>' attribute.
     * @see #getCategoryId()
     * @generated
     */
    void setCategoryId(String value);

    /**
     * Returns the value of the '<em><b>Category Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Category Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Category Name</em>' attribute.
     * @see #setCategoryName(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getCategory_CategoryName()
     * @model required="true"
     * @generated
     */
    String getCategoryName();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.Category#getCategoryName <em>Category Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Category Name</em>' attribute.
     * @see #getCategoryName()
     * @generated
     */
    void setCategoryName(String value);

} // Category
