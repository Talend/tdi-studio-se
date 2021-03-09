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
package org.talend.designer.components.exchange.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.talend.designer.components.exchange.model.Category;
import org.talend.designer.components.exchange.model.ExchangePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Category</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.CategoryImpl#getCategoryId <em>Category Id</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.CategoryImpl#getCategoryName <em>Category Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CategoryImpl extends EObjectImpl implements Category {
    /**
     * The default value of the '{@link #getCategoryId() <em>Category Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCategoryId()
     * @generated
     * @ordered
     */
    protected static final String CATEGORY_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCategoryId() <em>Category Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCategoryId()
     * @generated
     * @ordered
     */
    protected String categoryId = CATEGORY_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getCategoryName() <em>Category Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCategoryName()
     * @generated
     * @ordered
     */
    protected static final String CATEGORY_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCategoryName() <em>Category Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCategoryName()
     * @generated
     * @ordered
     */
    protected String categoryName = CATEGORY_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CategoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ExchangePackage.Literals.CATEGORY;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCategoryId(String newCategoryId) {
        String oldCategoryId = categoryId;
        categoryId = newCategoryId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.CATEGORY__CATEGORY_ID, oldCategoryId, categoryId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCategoryName(String newCategoryName) {
        String oldCategoryName = categoryName;
        categoryName = newCategoryName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.CATEGORY__CATEGORY_NAME, oldCategoryName, categoryName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ExchangePackage.CATEGORY__CATEGORY_ID:
                return getCategoryId();
            case ExchangePackage.CATEGORY__CATEGORY_NAME:
                return getCategoryName();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ExchangePackage.CATEGORY__CATEGORY_ID:
                setCategoryId((String)newValue);
                return;
            case ExchangePackage.CATEGORY__CATEGORY_NAME:
                setCategoryName((String)newValue);
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
            case ExchangePackage.CATEGORY__CATEGORY_ID:
                setCategoryId(CATEGORY_ID_EDEFAULT);
                return;
            case ExchangePackage.CATEGORY__CATEGORY_NAME:
                setCategoryName(CATEGORY_NAME_EDEFAULT);
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
            case ExchangePackage.CATEGORY__CATEGORY_ID:
                return CATEGORY_ID_EDEFAULT == null ? categoryId != null : !CATEGORY_ID_EDEFAULT.equals(categoryId);
            case ExchangePackage.CATEGORY__CATEGORY_NAME:
                return CATEGORY_NAME_EDEFAULT == null ? categoryName != null : !CATEGORY_NAME_EDEFAULT.equals(categoryName);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (categoryId: ");
        result.append(categoryId);
        result.append(", categoryName: ");
        result.append(categoryName);
        result.append(')');
        return result.toString();
    }

} //CategoryImpl
