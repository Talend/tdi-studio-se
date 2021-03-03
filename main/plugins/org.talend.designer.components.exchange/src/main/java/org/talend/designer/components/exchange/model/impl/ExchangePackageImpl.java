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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.talend.designer.components.exchange.model.AvailableExtensionViewDetail;
import org.talend.designer.components.exchange.model.Category;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangeFactory;
import org.talend.designer.components.exchange.model.ExchangePackage;
import org.talend.designer.components.exchange.model.Language;
import org.talend.designer.components.exchange.model.VersionRevision;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExchangePackageImpl extends EPackageImpl implements ExchangePackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass categoryEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass versionRevisionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass componentExtensionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass availableExtensionViewDetailEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum languageEEnum = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.talend.designer.components.exchange.model.ExchangePackage#eNS_URI
     * @see #init()
     * @generated
     */
    private ExchangePackageImpl() {
        super(eNS_URI, ExchangeFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>This method is used to initialize {@link ExchangePackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static ExchangePackage init() {
        if (isInited) return (ExchangePackage)EPackage.Registry.INSTANCE.getEPackage(ExchangePackage.eNS_URI);

        // Obtain or create and register package
        ExchangePackageImpl theExchangePackage = (ExchangePackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ExchangePackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ExchangePackageImpl());

        isInited = true;

        // Create package meta-data objects
        theExchangePackage.createPackageContents();

        // Initialize created meta-data
        theExchangePackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theExchangePackage.freeze();


        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(ExchangePackage.eNS_URI, theExchangePackage);
        return theExchangePackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCategory() {
        return categoryEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCategory_CategoryId() {
        return (EAttribute)categoryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCategory_CategoryName() {
        return (EAttribute)categoryEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getVersionRevision() {
        return versionRevisionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getVersionRevision_VersionId() {
        return (EAttribute)versionRevisionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getVersionRevision_VersionName() {
        return (EAttribute)versionRevisionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getComponentExtension() {
        return componentExtensionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_IdExtension() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Label() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_TypeExtension() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_VersionExtension() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_LastVersionAvailable() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_PublicationDate() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Description() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Rate() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Author() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Language() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getComponentExtension_Reviews() {
        return (EReference)componentExtensionEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_DownloadedVersion() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_DateDownload() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_ListVersionCompatibles() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_Filename() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(14);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getComponentExtension_LinkDownload() {
        return (EAttribute)componentExtensionEClass.getEStructuralFeatures().get(15);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getAvailableExtensionViewDetail() {
        return availableExtensionViewDetailEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAvailableExtensionViewDetail_Author() {
        return (EAttribute)availableExtensionViewDetailEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAvailableExtensionViewDetail_Title() {
        return (EAttribute)availableExtensionViewDetailEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAvailableExtensionViewDetail_Comment() {
        return (EAttribute)availableExtensionViewDetailEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAvailableExtensionViewDetail_Reviewrate() {
        return (EAttribute)availableExtensionViewDetailEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getAvailableExtensionViewDetail_Extension() {
        return (EReference)availableExtensionViewDetailEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getLanguage() {
        return languageEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ExchangeFactory getExchangeFactory() {
        return (ExchangeFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        categoryEClass = createEClass(CATEGORY);
        createEAttribute(categoryEClass, CATEGORY__CATEGORY_ID);
        createEAttribute(categoryEClass, CATEGORY__CATEGORY_NAME);

        versionRevisionEClass = createEClass(VERSION_REVISION);
        createEAttribute(versionRevisionEClass, VERSION_REVISION__VERSION_ID);
        createEAttribute(versionRevisionEClass, VERSION_REVISION__VERSION_NAME);

        componentExtensionEClass = createEClass(COMPONENT_EXTENSION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__ID_EXTENSION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__LABEL);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__TYPE_EXTENSION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__VERSION_EXTENSION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__PUBLICATION_DATE);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__DESCRIPTION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__RATE);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__AUTHOR);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__LANGUAGE);
        createEReference(componentExtensionEClass, COMPONENT_EXTENSION__REVIEWS);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__DOWNLOADED_VERSION);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__DATE_DOWNLOAD);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__FILENAME);
        createEAttribute(componentExtensionEClass, COMPONENT_EXTENSION__LINK_DOWNLOAD);

        availableExtensionViewDetailEClass = createEClass(AVAILABLE_EXTENSION_VIEW_DETAIL);
        createEAttribute(availableExtensionViewDetailEClass, AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR);
        createEAttribute(availableExtensionViewDetailEClass, AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE);
        createEAttribute(availableExtensionViewDetailEClass, AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT);
        createEAttribute(availableExtensionViewDetailEClass, AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE);
        createEReference(availableExtensionViewDetailEClass, AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION);

        // Create enums
        languageEEnum = createEEnum(LANGUAGE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes

        // Initialize classes and features; add operations and parameters
        initEClass(categoryEClass, Category.class, "Category", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCategory_CategoryId(), ecorePackage.getEString(), "categoryId", null, 1, 1, Category.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCategory_CategoryName(), ecorePackage.getEString(), "categoryName", null, 1, 1, Category.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(versionRevisionEClass, VersionRevision.class, "VersionRevision", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getVersionRevision_VersionId(), ecorePackage.getEString(), "versionId", null, 1, 1, VersionRevision.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getVersionRevision_VersionName(), ecorePackage.getEString(), "versionName", null, 1, 1, VersionRevision.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(componentExtensionEClass, ComponentExtension.class, "ComponentExtension", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getComponentExtension_IdExtension(), ecorePackage.getEString(), "idExtension", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Label(), ecorePackage.getEString(), "label", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_TypeExtension(), ecorePackage.getEString(), "typeExtension", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_VersionExtension(), ecorePackage.getEString(), "versionExtension", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_LastVersionAvailable(), ecorePackage.getEString(), "lastVersionAvailable", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_PublicationDate(), ecorePackage.getEDate(), "publicationDate", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Description(), ecorePackage.getEString(), "description", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Rate(), ecorePackage.getEString(), "rate", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Author(), ecorePackage.getEString(), "author", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Language(), this.getLanguage(), "language", "", 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getComponentExtension_Reviews(), this.getAvailableExtensionViewDetail(), this.getAvailableExtensionViewDetail_Extension(), "reviews", null, 1, -1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_DownloadedVersion(), ecorePackage.getEString(), "downloadedVersion", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_DateDownload(), ecorePackage.getEDate(), "dateDownload", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_ListVersionCompatibles(), ecorePackage.getEString(), "listVersionCompatibles", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_Filename(), ecorePackage.getEString(), "filename", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getComponentExtension_LinkDownload(), ecorePackage.getEString(), "linkDownload", null, 1, 1, ComponentExtension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(availableExtensionViewDetailEClass, AvailableExtensionViewDetail.class, "AvailableExtensionViewDetail", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAvailableExtensionViewDetail_Author(), ecorePackage.getEString(), "author", null, 1, 1, AvailableExtensionViewDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAvailableExtensionViewDetail_Title(), ecorePackage.getEString(), "title", null, 1, 1, AvailableExtensionViewDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAvailableExtensionViewDetail_Comment(), ecorePackage.getEString(), "comment", null, 1, 1, AvailableExtensionViewDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAvailableExtensionViewDetail_Reviewrate(), ecorePackage.getEString(), "reviewrate", null, 1, 1, AvailableExtensionViewDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getAvailableExtensionViewDetail_Extension(), this.getComponentExtension(), this.getComponentExtension_Reviews(), "extension", null, 1, 1, AvailableExtensionViewDetail.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(languageEEnum, Language.class, "Language");
        addEEnumLiteral(languageEEnum, Language.PERL);
        addEEnumLiteral(languageEEnum, Language.JAVA);

        // Create resource
        createResource(eNS_URI);
    }

} //ExchangePackageImpl
