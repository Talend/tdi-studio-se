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
 * A representation of the model object '<em><b>Available Extension View Detail</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getAuthor <em>Author</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getTitle <em>Title</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getComment <em>Comment</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getReviewrate <em>Reviewrate</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension <em>Extension</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail()
 * @model
 * @generated
 */
public interface AvailableExtensionViewDetail extends EObject {
    /**
     * Returns the value of the '<em><b>Author</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Author</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Author</em>' attribute.
     * @see #setAuthor(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail_Author()
     * @model required="true"
     * @generated
     */
    String getAuthor();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getAuthor <em>Author</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Author</em>' attribute.
     * @see #getAuthor()
     * @generated
     */
    void setAuthor(String value);

    /**
     * Returns the value of the '<em><b>Title</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Title</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Title</em>' attribute.
     * @see #setTitle(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail_Title()
     * @model required="true"
     * @generated
     */
    String getTitle();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getTitle <em>Title</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Title</em>' attribute.
     * @see #getTitle()
     * @generated
     */
    void setTitle(String value);

    /**
     * Returns the value of the '<em><b>Comment</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comment</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Comment</em>' attribute.
     * @see #setComment(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail_Comment()
     * @model required="true"
     * @generated
     */
    String getComment();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getComment <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Comment</em>' attribute.
     * @see #getComment()
     * @generated
     */
    void setComment(String value);

    /**
     * Returns the value of the '<em><b>Reviewrate</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Reviewrate</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Reviewrate</em>' attribute.
     * @see #setReviewrate(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail_Reviewrate()
     * @model required="true"
     * @generated
     */
    String getReviewrate();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getReviewrate <em>Reviewrate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Reviewrate</em>' attribute.
     * @see #getReviewrate()
     * @generated
     */
    void setReviewrate(String value);

    /**
     * Returns the value of the '<em><b>Extension</b></em>' container reference.
     * It is bidirectional and its opposite is '{@link org.talend.designer.components.exchange.model.ComponentExtension#getReviews <em>Reviews</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Extension</em>' container reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Extension</em>' container reference.
     * @see #setExtension(ComponentExtension)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getAvailableExtensionViewDetail_Extension()
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getReviews
     * @model opposite="reviews" required="true" transient="false"
     * @generated
     */
    ComponentExtension getExtension();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension <em>Extension</em>}' container reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Extension</em>' container reference.
     * @see #getExtension()
     * @generated
     */
    void setExtension(ComponentExtension value);

} // AvailableExtensionViewDetail
