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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.talend.designer.components.exchange.model.ExchangePackage
 * @generated
 */
public interface ExchangeFactory extends EFactory {
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    ExchangeFactory eINSTANCE = org.talend.designer.components.exchange.model.impl.ExchangeFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Category</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Category</em>'.
     * @generated
     */
    Category createCategory();

    /**
     * Returns a new object of class '<em>Version Revision</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Version Revision</em>'.
     * @generated
     */
    VersionRevision createVersionRevision();

    /**
     * Returns a new object of class '<em>Component Extension</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Component Extension</em>'.
     * @generated
     */
    ComponentExtension createComponentExtension();

    /**
     * Returns a new object of class '<em>Available Extension View Detail</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Available Extension View Detail</em>'.
     * @generated
     */
    AvailableExtensionViewDetail createAvailableExtensionViewDetail();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    ExchangePackage getExchangePackage();

} //ExchangeFactory
