/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.designer.dbmap.model.emf.dbmap.AbstaceDBInOutTable;
import org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;
import org.talend.designer.dbmap.model.emf.dbmap.VarTable;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * @generated
 */
public class DbmapPackageImpl extends EPackageImpl implements DbmapPackage {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass dbMapDataEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass dbMapperTableEntryEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass abstractDBDataMapTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass abstaceDBInOutTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass varTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass inputTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass outputTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private EClass filterEntryEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
     * EPackage.Registry} by the package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
     * performs initialization of the package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private DbmapPackageImpl() {
        super(eNS_URI, DbmapFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>This method is used to initialize {@link DbmapPackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static DbmapPackage init() {
        if (isInited) return (DbmapPackage)EPackage.Registry.INSTANCE.getEPackage(DbmapPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredDbmapPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        DbmapPackageImpl theDbmapPackage = registeredDbmapPackage instanceof DbmapPackageImpl ? (DbmapPackageImpl)registeredDbmapPackage : new DbmapPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        TalendFilePackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theDbmapPackage.createPackageContents();

        // Initialize created meta-data
        theDbmapPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theDbmapPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(DbmapPackage.eNS_URI, theDbmapPackage);
        return theDbmapPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getDBMapData() {
        return dbMapDataEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getDBMapData_VarTables() {
        return (EReference)dbMapDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getDBMapData_InputTables() {
        return (EReference)dbMapDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getDBMapData_OutputTables() {
        return (EReference)dbMapDataEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getDBMapperTableEntry() {
        return dbMapperTableEntryEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Name() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Expression() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Type() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Nullable() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Join() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getDBMapperTableEntry_Operator() {
        return (EAttribute)dbMapperTableEntryEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getAbstractDBDataMapTable() {
        return abstractDBDataMapTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAbstractDBDataMapTable_Name() {
        return (EAttribute)abstractDBDataMapTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAbstractDBDataMapTable_Minimized() {
        return (EAttribute)abstractDBDataMapTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAbstractDBDataMapTable_Readonly() {
        return (EAttribute)abstractDBDataMapTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getAbstractDBDataMapTable_DBMapperTableEntries() {
        return (EReference)abstractDBDataMapTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAbstractDBDataMapTable_TableName() {
        return (EAttribute)abstractDBDataMapTableEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getAbstaceDBInOutTable() {
        return abstaceDBInOutTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getVarTable() {
        return varTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getInputTable() {
        return inputTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getInputTable_JoinType() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getInputTable_Alias() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getOutputTable() {
        return outputTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getOutputTable_FilterEntries() {
        return (EReference)outputTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getFilterEntry() {
        return filterEntryEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getFilterEntry_Name() {
        return (EAttribute)filterEntryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getFilterEntry_Expression() {
        return (EAttribute)filterEntryEClass.getEStructuralFeatures().get(1);
    }

    @Override
    public EAttribute getFilterEntry_FilterKind() {
        return (EAttribute) filterEntryEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public DbmapFactory getDbmapFactory() {
        return (DbmapFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        dbMapDataEClass = createEClass(DB_MAP_DATA);
        createEReference(dbMapDataEClass, DB_MAP_DATA__VAR_TABLES);
        createEReference(dbMapDataEClass, DB_MAP_DATA__INPUT_TABLES);
        createEReference(dbMapDataEClass, DB_MAP_DATA__OUTPUT_TABLES);

        dbMapperTableEntryEClass = createEClass(DB_MAPPER_TABLE_ENTRY);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__NAME);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__EXPRESSION);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__TYPE);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__NULLABLE);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__JOIN);
        createEAttribute(dbMapperTableEntryEClass, DB_MAPPER_TABLE_ENTRY__OPERATOR);

        abstractDBDataMapTableEClass = createEClass(ABSTRACT_DB_DATA_MAP_TABLE);
        createEAttribute(abstractDBDataMapTableEClass, ABSTRACT_DB_DATA_MAP_TABLE__NAME);
        createEAttribute(abstractDBDataMapTableEClass, ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED);
        createEAttribute(abstractDBDataMapTableEClass, ABSTRACT_DB_DATA_MAP_TABLE__READONLY);
        createEReference(abstractDBDataMapTableEClass, ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES);
        createEAttribute(abstractDBDataMapTableEClass, ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME);

        abstaceDBInOutTableEClass = createEClass(ABSTACE_DB_IN_OUT_TABLE);

        varTableEClass = createEClass(VAR_TABLE);

        inputTableEClass = createEClass(INPUT_TABLE);
        createEAttribute(inputTableEClass, INPUT_TABLE__JOIN_TYPE);
        createEAttribute(inputTableEClass, INPUT_TABLE__ALIAS);

        outputTableEClass = createEClass(OUTPUT_TABLE);
        createEReference(outputTableEClass, OUTPUT_TABLE__FILTER_ENTRIES);

        filterEntryEClass = createEClass(FILTER_ENTRY);
        createEAttribute(filterEntryEClass, FILTER_ENTRY__NAME);
        createEAttribute(filterEntryEClass, FILTER_ENTRY__EXPRESSION);
        createEAttribute(filterEntryEClass, FILTER_ENTRY__FILTER_KIND);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        TalendFilePackage theTalendFilePackage = (TalendFilePackage)EPackage.Registry.INSTANCE.getEPackage(TalendFilePackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        dbMapDataEClass.getESuperTypes().add(theTalendFilePackage.getAbstractExternalData());
        abstaceDBInOutTableEClass.getESuperTypes().add(this.getAbstractDBDataMapTable());
        varTableEClass.getESuperTypes().add(this.getAbstractDBDataMapTable());
        inputTableEClass.getESuperTypes().add(this.getAbstaceDBInOutTable());
        outputTableEClass.getESuperTypes().add(this.getAbstaceDBInOutTable());

        // Initialize classes and features; add operations and parameters
        initEClass(dbMapDataEClass, DBMapData.class, "DBMapData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getDBMapData_VarTables(), this.getVarTable(), null, "VarTables", null, 0, -1, DBMapData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDBMapData_InputTables(), this.getInputTable(), null, "InputTables", null, 0, -1, DBMapData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDBMapData_OutputTables(), this.getOutputTable(), null, "OutputTables", null, 0, -1, DBMapData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(dbMapperTableEntryEClass, DBMapperTableEntry.class, "DBMapperTableEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDBMapperTableEntry_Name(), ecorePackage.getEString(), "name", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDBMapperTableEntry_Expression(), ecorePackage.getEString(), "expression", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
        initEAttribute(getDBMapperTableEntry_Type(), ecorePackage.getEString(), "type", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDBMapperTableEntry_Nullable(), ecorePackage.getEBoolean(), "nullable", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
        initEAttribute(getDBMapperTableEntry_Join(), ecorePackage.getEBoolean(), "join", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDBMapperTableEntry_Operator(), ecorePackage.getEString(), "operator", null, 0, 1, DBMapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(abstractDBDataMapTableEClass, AbstractDBDataMapTable.class, "AbstractDBDataMapTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAbstractDBDataMapTable_Name(), ecorePackage.getEString(), "name", null, 0, 1, AbstractDBDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractDBDataMapTable_Minimized(), ecorePackage.getEBoolean(), "minimized", null, 0, 1, AbstractDBDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractDBDataMapTable_Readonly(), ecorePackage.getEBoolean(), "readonly", null, 0, 1, AbstractDBDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getAbstractDBDataMapTable_DBMapperTableEntries(), this.getDBMapperTableEntry(), null, "DBMapperTableEntries", null, 0, -1, AbstractDBDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractDBDataMapTable_TableName(), ecorePackage.getEString(), "tableName", null, 0, 1, AbstractDBDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(abstaceDBInOutTableEClass, AbstaceDBInOutTable.class, "AbstaceDBInOutTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(varTableEClass, VarTable.class, "VarTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(inputTableEClass, InputTable.class, "InputTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getInputTable_JoinType(), ecorePackage.getEString(), "joinType", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getInputTable_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(outputTableEClass, OutputTable.class, "OutputTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getOutputTable_FilterEntries(), this.getFilterEntry(), null, "FilterEntries", null, 0, -1, OutputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(filterEntryEClass, FilterEntry.class, "FilterEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getFilterEntry_Name(), ecorePackage.getEString(), "name", null, 0, 1, FilterEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getFilterEntry_Expression(), ecorePackage.getEString(), "expression", null, 0, 1, FilterEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getFilterEntry_FilterKind(), ecorePackage.getEString(), "filterKind", null, 0, 1, FilterEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);
    }

} // DbmapPackageImpl
