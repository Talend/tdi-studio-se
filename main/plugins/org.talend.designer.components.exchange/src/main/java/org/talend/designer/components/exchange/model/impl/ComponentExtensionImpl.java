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

import java.util.Collection;
import java.util.Date;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.talend.designer.components.exchange.model.AvailableExtensionViewDetail;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangePackage;
import org.talend.designer.components.exchange.model.Language;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Component Extension</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getIdExtension <em>Id Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getTypeExtension <em>Type Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getVersionExtension <em>Version Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getLastVersionAvailable <em>Last Version Available</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getPublicationDate <em>Publication Date</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getRate <em>Rate</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getAuthor <em>Author</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getReviews <em>Reviews</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getDownloadedVersion <em>Downloaded Version</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getDateDownload <em>Date Download</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getListVersionCompatibles <em>List Version Compatibles</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getFilename <em>Filename</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl#getLinkDownload <em>Link Download</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ComponentExtensionImpl extends EObjectImpl implements ComponentExtension {
    /**
     * The default value of the '{@link #getIdExtension() <em>Id Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getIdExtension()
     * @generated
     * @ordered
     */
    protected static final String ID_EXTENSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getIdExtension() <em>Id Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getIdExtension()
     * @generated
     * @ordered
     */
    protected String idExtension = ID_EXTENSION_EDEFAULT;

    /**
     * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLabel()
     * @generated
     * @ordered
     */
    protected static final String LABEL_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLabel()
     * @generated
     * @ordered
     */
    protected String label = LABEL_EDEFAULT;

    /**
     * The default value of the '{@link #getTypeExtension() <em>Type Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeExtension()
     * @generated
     * @ordered
     */
    protected static final String TYPE_EXTENSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTypeExtension() <em>Type Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeExtension()
     * @generated
     * @ordered
     */
    protected String typeExtension = TYPE_EXTENSION_EDEFAULT;

    /**
     * The default value of the '{@link #getVersionExtension() <em>Version Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionExtension()
     * @generated
     * @ordered
     */
    protected static final String VERSION_EXTENSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getVersionExtension() <em>Version Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionExtension()
     * @generated
     * @ordered
     */
    protected String versionExtension = VERSION_EXTENSION_EDEFAULT;

    /**
     * The default value of the '{@link #getLastVersionAvailable() <em>Last Version Available</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLastVersionAvailable()
     * @generated
     * @ordered
     */
    protected static final String LAST_VERSION_AVAILABLE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLastVersionAvailable() <em>Last Version Available</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLastVersionAvailable()
     * @generated
     * @ordered
     */
    protected String lastVersionAvailable = LAST_VERSION_AVAILABLE_EDEFAULT;

    /**
     * The default value of the '{@link #getPublicationDate() <em>Publication Date</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPublicationDate()
     * @generated
     * @ordered
     */
    protected static final Date PUBLICATION_DATE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPublicationDate() <em>Publication Date</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPublicationDate()
     * @generated
     * @ordered
     */
    protected Date publicationDate = PUBLICATION_DATE_EDEFAULT;

    /**
     * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected static final String DESCRIPTION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected String description = DESCRIPTION_EDEFAULT;

    /**
     * The default value of the '{@link #getRate() <em>Rate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRate()
     * @generated
     * @ordered
     */
    protected static final String RATE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRate() <em>Rate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRate()
     * @generated
     * @ordered
     */
    protected String rate = RATE_EDEFAULT;

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
     * The default value of the '{@link #getLanguage() <em>Language</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLanguage()
     * @generated
     * @ordered
     */
    protected static final Language LANGUAGE_EDEFAULT = Language.PERL;

    /**
     * The cached value of the '{@link #getLanguage() <em>Language</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLanguage()
     * @generated
     * @ordered
     */
    protected Language language = LANGUAGE_EDEFAULT;

    /**
     * The cached value of the '{@link #getReviews() <em>Reviews</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReviews()
     * @generated
     * @ordered
     */
    protected EList<AvailableExtensionViewDetail> reviews;

    /**
     * The default value of the '{@link #getDownloadedVersion() <em>Downloaded Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDownloadedVersion()
     * @generated
     * @ordered
     */
    protected static final String DOWNLOADED_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDownloadedVersion() <em>Downloaded Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDownloadedVersion()
     * @generated
     * @ordered
     */
    protected String downloadedVersion = DOWNLOADED_VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getDateDownload() <em>Date Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDateDownload()
     * @generated
     * @ordered
     */
    protected static final Date DATE_DOWNLOAD_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDateDownload() <em>Date Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDateDownload()
     * @generated
     * @ordered
     */
    protected Date dateDownload = DATE_DOWNLOAD_EDEFAULT;

    /**
     * The default value of the '{@link #getListVersionCompatibles() <em>List Version Compatibles</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getListVersionCompatibles()
     * @generated
     * @ordered
     */
    protected static final String LIST_VERSION_COMPATIBLES_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getListVersionCompatibles() <em>List Version Compatibles</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getListVersionCompatibles()
     * @generated
     * @ordered
     */
    protected String listVersionCompatibles = LIST_VERSION_COMPATIBLES_EDEFAULT;

    /**
     * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFilename()
     * @generated
     * @ordered
     */
    protected static final String FILENAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFilename()
     * @generated
     * @ordered
     */
    protected String filename = FILENAME_EDEFAULT;

    /**
     * The default value of the '{@link #getLinkDownload() <em>Link Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLinkDownload()
     * @generated
     * @ordered
     */
    protected static final String LINK_DOWNLOAD_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLinkDownload() <em>Link Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLinkDownload()
     * @generated
     * @ordered
     */
    protected String linkDownload = LINK_DOWNLOAD_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ComponentExtensionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ExchangePackage.Literals.COMPONENT_EXTENSION;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getIdExtension() {
        return idExtension;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setIdExtension(String newIdExtension) {
        String oldIdExtension = idExtension;
        idExtension = newIdExtension;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__ID_EXTENSION, oldIdExtension, idExtension));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getLabel() {
        return label;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLabel(String newLabel) {
        String oldLabel = label;
        label = newLabel;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__LABEL, oldLabel, label));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTypeExtension() {
        return typeExtension;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTypeExtension(String newTypeExtension) {
        String oldTypeExtension = typeExtension;
        typeExtension = newTypeExtension;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__TYPE_EXTENSION, oldTypeExtension, typeExtension));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getVersionExtension() {
        return versionExtension;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setVersionExtension(String newVersionExtension) {
        String oldVersionExtension = versionExtension;
        versionExtension = newVersionExtension;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__VERSION_EXTENSION, oldVersionExtension, versionExtension));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getLastVersionAvailable() {
        return lastVersionAvailable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLastVersionAvailable(String newLastVersionAvailable) {
        String oldLastVersionAvailable = lastVersionAvailable;
        lastVersionAvailable = newLastVersionAvailable;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE, oldLastVersionAvailable, lastVersionAvailable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPublicationDate(Date newPublicationDate) {
        Date oldPublicationDate = publicationDate;
        publicationDate = newPublicationDate;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__PUBLICATION_DATE, oldPublicationDate, publicationDate));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDescription() {
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDescription(String newDescription) {
        String oldDescription = description;
        description = newDescription;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__DESCRIPTION, oldDescription, description));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRate() {
        return rate;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRate(String newRate) {
        String oldRate = rate;
        rate = newRate;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__RATE, oldRate, rate));
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
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__AUTHOR, oldAuthor, author));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLanguage(Language newLanguage) {
        Language oldLanguage = language;
        language = newLanguage == null ? LANGUAGE_EDEFAULT : newLanguage;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__LANGUAGE, oldLanguage, language));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<AvailableExtensionViewDetail> getReviews() {
        if (reviews == null) {
            reviews = new EObjectContainmentWithInverseEList<AvailableExtensionViewDetail>(AvailableExtensionViewDetail.class, this, ExchangePackage.COMPONENT_EXTENSION__REVIEWS, ExchangePackage.AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION);
        }
        return reviews;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDownloadedVersion() {
        return downloadedVersion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDownloadedVersion(String newDownloadedVersion) {
        String oldDownloadedVersion = downloadedVersion;
        downloadedVersion = newDownloadedVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__DOWNLOADED_VERSION, oldDownloadedVersion, downloadedVersion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Date getDateDownload() {
        return dateDownload;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDateDownload(Date newDateDownload) {
        Date oldDateDownload = dateDownload;
        dateDownload = newDateDownload;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__DATE_DOWNLOAD, oldDateDownload, dateDownload));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getListVersionCompatibles() {
        return listVersionCompatibles;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setListVersionCompatibles(String newListVersionCompatibles) {
        String oldListVersionCompatibles = listVersionCompatibles;
        listVersionCompatibles = newListVersionCompatibles;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES, oldListVersionCompatibles, listVersionCompatibles));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getFilename() {
        return filename;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setFilename(String newFilename) {
        String oldFilename = filename;
        filename = newFilename;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__FILENAME, oldFilename, filename));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getLinkDownload() {
        return linkDownload;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLinkDownload(String newLinkDownload) {
        String oldLinkDownload = linkDownload;
        linkDownload = newLinkDownload;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.COMPONENT_EXTENSION__LINK_DOWNLOAD, oldLinkDownload, linkDownload));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                return ((InternalEList<InternalEObject>)(InternalEList<?>)getReviews()).basicAdd(otherEnd, msgs);
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
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                return ((InternalEList<?>)getReviews()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ExchangePackage.COMPONENT_EXTENSION__ID_EXTENSION:
                return getIdExtension();
            case ExchangePackage.COMPONENT_EXTENSION__LABEL:
                return getLabel();
            case ExchangePackage.COMPONENT_EXTENSION__TYPE_EXTENSION:
                return getTypeExtension();
            case ExchangePackage.COMPONENT_EXTENSION__VERSION_EXTENSION:
                return getVersionExtension();
            case ExchangePackage.COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE:
                return getLastVersionAvailable();
            case ExchangePackage.COMPONENT_EXTENSION__PUBLICATION_DATE:
                return getPublicationDate();
            case ExchangePackage.COMPONENT_EXTENSION__DESCRIPTION:
                return getDescription();
            case ExchangePackage.COMPONENT_EXTENSION__RATE:
                return getRate();
            case ExchangePackage.COMPONENT_EXTENSION__AUTHOR:
                return getAuthor();
            case ExchangePackage.COMPONENT_EXTENSION__LANGUAGE:
                return getLanguage();
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                return getReviews();
            case ExchangePackage.COMPONENT_EXTENSION__DOWNLOADED_VERSION:
                return getDownloadedVersion();
            case ExchangePackage.COMPONENT_EXTENSION__DATE_DOWNLOAD:
                return getDateDownload();
            case ExchangePackage.COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES:
                return getListVersionCompatibles();
            case ExchangePackage.COMPONENT_EXTENSION__FILENAME:
                return getFilename();
            case ExchangePackage.COMPONENT_EXTENSION__LINK_DOWNLOAD:
                return getLinkDownload();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ExchangePackage.COMPONENT_EXTENSION__ID_EXTENSION:
                setIdExtension((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LABEL:
                setLabel((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__TYPE_EXTENSION:
                setTypeExtension((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__VERSION_EXTENSION:
                setVersionExtension((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE:
                setLastVersionAvailable((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__PUBLICATION_DATE:
                setPublicationDate((Date)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DESCRIPTION:
                setDescription((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__RATE:
                setRate((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__AUTHOR:
                setAuthor((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LANGUAGE:
                setLanguage((Language)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                getReviews().clear();
                getReviews().addAll((Collection<? extends AvailableExtensionViewDetail>)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DOWNLOADED_VERSION:
                setDownloadedVersion((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DATE_DOWNLOAD:
                setDateDownload((Date)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES:
                setListVersionCompatibles((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__FILENAME:
                setFilename((String)newValue);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LINK_DOWNLOAD:
                setLinkDownload((String)newValue);
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
            case ExchangePackage.COMPONENT_EXTENSION__ID_EXTENSION:
                setIdExtension(ID_EXTENSION_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LABEL:
                setLabel(LABEL_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__TYPE_EXTENSION:
                setTypeExtension(TYPE_EXTENSION_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__VERSION_EXTENSION:
                setVersionExtension(VERSION_EXTENSION_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE:
                setLastVersionAvailable(LAST_VERSION_AVAILABLE_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__PUBLICATION_DATE:
                setPublicationDate(PUBLICATION_DATE_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DESCRIPTION:
                setDescription(DESCRIPTION_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__RATE:
                setRate(RATE_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__AUTHOR:
                setAuthor(AUTHOR_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LANGUAGE:
                setLanguage(LANGUAGE_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                getReviews().clear();
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DOWNLOADED_VERSION:
                setDownloadedVersion(DOWNLOADED_VERSION_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__DATE_DOWNLOAD:
                setDateDownload(DATE_DOWNLOAD_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES:
                setListVersionCompatibles(LIST_VERSION_COMPATIBLES_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__FILENAME:
                setFilename(FILENAME_EDEFAULT);
                return;
            case ExchangePackage.COMPONENT_EXTENSION__LINK_DOWNLOAD:
                setLinkDownload(LINK_DOWNLOAD_EDEFAULT);
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
            case ExchangePackage.COMPONENT_EXTENSION__ID_EXTENSION:
                return ID_EXTENSION_EDEFAULT == null ? idExtension != null : !ID_EXTENSION_EDEFAULT.equals(idExtension);
            case ExchangePackage.COMPONENT_EXTENSION__LABEL:
                return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
            case ExchangePackage.COMPONENT_EXTENSION__TYPE_EXTENSION:
                return TYPE_EXTENSION_EDEFAULT == null ? typeExtension != null : !TYPE_EXTENSION_EDEFAULT.equals(typeExtension);
            case ExchangePackage.COMPONENT_EXTENSION__VERSION_EXTENSION:
                return VERSION_EXTENSION_EDEFAULT == null ? versionExtension != null : !VERSION_EXTENSION_EDEFAULT.equals(versionExtension);
            case ExchangePackage.COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE:
                return LAST_VERSION_AVAILABLE_EDEFAULT == null ? lastVersionAvailable != null : !LAST_VERSION_AVAILABLE_EDEFAULT.equals(lastVersionAvailable);
            case ExchangePackage.COMPONENT_EXTENSION__PUBLICATION_DATE:
                return PUBLICATION_DATE_EDEFAULT == null ? publicationDate != null : !PUBLICATION_DATE_EDEFAULT.equals(publicationDate);
            case ExchangePackage.COMPONENT_EXTENSION__DESCRIPTION:
                return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
            case ExchangePackage.COMPONENT_EXTENSION__RATE:
                return RATE_EDEFAULT == null ? rate != null : !RATE_EDEFAULT.equals(rate);
            case ExchangePackage.COMPONENT_EXTENSION__AUTHOR:
                return AUTHOR_EDEFAULT == null ? author != null : !AUTHOR_EDEFAULT.equals(author);
            case ExchangePackage.COMPONENT_EXTENSION__LANGUAGE:
                return language != LANGUAGE_EDEFAULT;
            case ExchangePackage.COMPONENT_EXTENSION__REVIEWS:
                return reviews != null && !reviews.isEmpty();
            case ExchangePackage.COMPONENT_EXTENSION__DOWNLOADED_VERSION:
                return DOWNLOADED_VERSION_EDEFAULT == null ? downloadedVersion != null : !DOWNLOADED_VERSION_EDEFAULT.equals(downloadedVersion);
            case ExchangePackage.COMPONENT_EXTENSION__DATE_DOWNLOAD:
                return DATE_DOWNLOAD_EDEFAULT == null ? dateDownload != null : !DATE_DOWNLOAD_EDEFAULT.equals(dateDownload);
            case ExchangePackage.COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES:
                return LIST_VERSION_COMPATIBLES_EDEFAULT == null ? listVersionCompatibles != null : !LIST_VERSION_COMPATIBLES_EDEFAULT.equals(listVersionCompatibles);
            case ExchangePackage.COMPONENT_EXTENSION__FILENAME:
                return FILENAME_EDEFAULT == null ? filename != null : !FILENAME_EDEFAULT.equals(filename);
            case ExchangePackage.COMPONENT_EXTENSION__LINK_DOWNLOAD:
                return LINK_DOWNLOAD_EDEFAULT == null ? linkDownload != null : !LINK_DOWNLOAD_EDEFAULT.equals(linkDownload);
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
        result.append(" (idExtension: ");
        result.append(idExtension);
        result.append(", label: ");
        result.append(label);
        result.append(", typeExtension: ");
        result.append(typeExtension);
        result.append(", versionExtension: ");
        result.append(versionExtension);
        result.append(", lastVersionAvailable: ");
        result.append(lastVersionAvailable);
        result.append(", publicationDate: ");
        result.append(publicationDate);
        result.append(", description: ");
        result.append(description);
        result.append(", rate: ");
        result.append(rate);
        result.append(", author: ");
        result.append(author);
        result.append(", language: ");
        result.append(language);
        result.append(", downloadedVersion: ");
        result.append(downloadedVersion);
        result.append(", dateDownload: ");
        result.append(dateDownload);
        result.append(", listVersionCompatibles: ");
        result.append(listVersionCompatibles);
        result.append(", filename: ");
        result.append(filename);
        result.append(", linkDownload: ");
        result.append(linkDownload);
        result.append(')');
        return result.toString();
    }

} //ComponentExtensionImpl
