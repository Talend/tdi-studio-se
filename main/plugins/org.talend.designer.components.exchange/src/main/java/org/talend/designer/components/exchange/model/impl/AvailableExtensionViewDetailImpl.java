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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;

import org.talend.designer.components.exchange.model.AvailableExtensionViewDetail;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Available Extension View Detail</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl#getAuthor <em>Author</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl#getTitle <em>Title</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl#getComment <em>Comment</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl#getReviewrate <em>Reviewrate</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl#getExtension <em>Extension</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AvailableExtensionViewDetailImpl extends EObjectImpl implements AvailableExtensionViewDetail {
    /**
     * The default value of the '{@link #getAuthor() <em>Author</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAuthor()
     * @generated
     * @ordered
     */
    protected static final String AUTHOR_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getAuthor() <em>Author</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAuthor()
     * @generated
     * @ordered
     */
    protected String author = AUTHOR_EDEFAULT;

    /**
     * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTitle()
     * @generated
     * @ordered
     */
    protected static final String TITLE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTitle()
     * @generated
     * @ordered
     */
    protected String title = TITLE_EDEFAULT;

    /**
     * The default value of the '{@link #getComment() <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getComment()
     * @generated
     * @ordered
     */
    protected static final String COMMENT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getComment() <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getComment()
     * @generated
     * @ordered
     */
    protected String comment = COMMENT_EDEFAULT;

    /**
     * The default value of the '{@link #getReviewrate() <em>Reviewrate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReviewrate()
     * @generated
     * @ordered
     */
    protected static final String REVIEWRATE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getReviewrate() <em>Reviewrate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReviewrate()
     * @generated
     * @ordered
     */
    protected String reviewrate = REVIEWRATE_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected AvailableExtensionViewDetailImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ExchangePackage.Literals.AVAILABLE_EXTENSION_VIEW_DETAIL;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getAuthor() {
        return author;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAuthor(String newAuthor) {
        String oldAuthor = author;
        author = newAuthor;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR, oldAuthor, author));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTitle() {
        return title;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTitle(String newTitle) {
        String oldTitle = title;
        title = newTitle;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE, oldTitle, title));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getComment() {
        return comment;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setComment(String newComment) {
        String oldComment = comment;
        comment = newComment;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT, oldComment, comment));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getReviewrate() {
        return reviewrate;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setReviewrate(String newReviewrate) {
        String oldReviewrate = reviewrate;
        reviewrate = newReviewrate;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE, oldReviewrate, reviewrate));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ComponentExtension getExtension() {
        if (eContainerFeatureID() != ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION) return null;
        return (ComponentExtension)eContainer();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetExtension(ComponentExtension newExtension, NotificationChain msgs) {
        msgs = eBasicSetContainer((InternalEObject)newExtension, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION, msgs);
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setExtension(ComponentExtension newExtension) {
        if (newExtension != eInternalContainer() || (eContainerFeatureID() != ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION && newExtension != null)) {
            if (EcoreUtil.isAncestor(this, newExtension))
                throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
            NotificationChain msgs = null;
            if (eInternalContainer() != null)
                msgs = eBasicRemoveFromContainer(msgs);
            if (newExtension != null)
                msgs = ((InternalEObject)newExtension).eInverseAdd(this, ExchangePackage.COMPONENT_EXTENSION__REVIEWS, ComponentExtension.class, msgs);
            msgs = basicSetExtension(newExtension, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION, newExtension, newExtension));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                if (eInternalContainer() != null)
                    msgs = eBasicRemoveFromContainer(msgs);
                return basicSetExtension((ComponentExtension)otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                return basicSetExtension(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
        switch (eContainerFeatureID()) {
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                return eInternalContainer().eInverseRemove(this, ExchangePackage.COMPONENT_EXTENSION__REVIEWS, ComponentExtension.class, msgs);
        }
        return super.eBasicRemoveFromContainerFeature(msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR:
                return getAuthor();
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE:
                return getTitle();
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT:
                return getComment();
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE:
                return getReviewrate();
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                return getExtension();
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
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR:
                setAuthor((String)newValue);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE:
                setTitle((String)newValue);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT:
                setComment((String)newValue);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE:
                setReviewrate((String)newValue);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                setExtension((ComponentExtension)newValue);
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
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR:
                setAuthor(AUTHOR_EDEFAULT);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE:
                setTitle(TITLE_EDEFAULT);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT:
                setComment(COMMENT_EDEFAULT);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE:
                setReviewrate(REVIEWRATE_EDEFAULT);
                return;
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                setExtension((ComponentExtension)null);
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
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR:
                return AUTHOR_EDEFAULT == null ? author != null : !AUTHOR_EDEFAULT.equals(author);
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE:
                return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT:
                return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE:
                return REVIEWRATE_EDEFAULT == null ? reviewrate != null : !REVIEWRATE_EDEFAULT.equals(reviewrate);
            case ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION:
                return getExtension() != null;
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
        result.append(" (author: ");
        result.append(author);
        result.append(", title: ");
        result.append(title);
        result.append(", comment: ");
        result.append(comment);
        result.append(", reviewrate: ");
        result.append(reviewrate);
        result.append(')');
        return result.toString();
    }

} //AvailableExtensionViewDetailImpl
