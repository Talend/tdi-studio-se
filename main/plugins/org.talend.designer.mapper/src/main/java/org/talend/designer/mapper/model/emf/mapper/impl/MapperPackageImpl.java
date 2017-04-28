/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;

import org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable;
import org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.Operator;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.model.emf.mapper.SizeState;
import org.talend.designer.mapper.model.emf.mapper.UiProperties;
import org.talend.designer.mapper.model.emf.mapper.VarTable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MapperPackageImpl extends EPackageImpl implements MapperPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass mapperDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass mapperTableEntryEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass abstractDataMapTableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass abstractInOutTableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass uiPropertiesEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass varTableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass outputTableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass inputTableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum sizeStateEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum operatorEEnum = null;

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
     * @see org.talend.designer.mapper.model.emf.mapper.MapperPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private MapperPackageImpl() {
        super(eNS_URI, MapperFactory.eINSTANCE);
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
     * <p>This method is used to initialize {@link MapperPackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static MapperPackage init() {
        if (isInited) return (MapperPackage)EPackage.Registry.INSTANCE.getEPackage(MapperPackage.eNS_URI);

        // Obtain or create and register package
        MapperPackageImpl theMapperPackage = (MapperPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof MapperPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new MapperPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        TalendFilePackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theMapperPackage.createPackageContents();

        // Initialize created meta-data
        theMapperPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theMapperPackage.freeze();

  
        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(MapperPackage.eNS_URI, theMapperPackage);
        return theMapperPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getMapperData() {
        return mapperDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMapperData_UiProperties() {
        return (EReference)mapperDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMapperData_VarTables() {
        return (EReference)mapperDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMapperData_OutputTables() {
        return (EReference)mapperDataEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMapperData_InputTables() {
        return (EReference)mapperDataEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getMapperTableEntry() {
        return mapperTableEntryEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMapperTableEntry_Name() {
        return (EAttribute)mapperTableEntryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMapperTableEntry_Expression() {
        return (EAttribute)mapperTableEntryEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMapperTableEntry_Type() {
        return (EAttribute)mapperTableEntryEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMapperTableEntry_Nullable() {
        return (EAttribute)mapperTableEntryEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMapperTableEntry_Operator() {
        return (EAttribute)mapperTableEntryEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getAbstractDataMapTable() {
        return abstractDataMapTableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractDataMapTable_SizeState() {
        return (EAttribute)abstractDataMapTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractDataMapTable_Name() {
        return (EAttribute)abstractDataMapTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractDataMapTable_Minimized() {
        return (EAttribute)abstractDataMapTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getAbstractDataMapTable_MapperTableEntries() {
        return (EReference)abstractDataMapTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getAbstractInOutTable() {
        return abstractInOutTableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractInOutTable_ExpressionFilter() {
        return (EAttribute)abstractInOutTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractInOutTable_ActivateExpressionFilter() {
        return (EAttribute)abstractInOutTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractInOutTable_ActivateCondensedTool() {
        return (EAttribute)abstractInOutTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractInOutTable_ActivateColumnNameFilter() {
        return (EAttribute)abstractInOutTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAbstractInOutTable_Id() {
        return (EAttribute)abstractInOutTableEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getUiProperties() {
        return uiPropertiesEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getUiProperties_ShellMaximized() {
        return (EAttribute)uiPropertiesEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getVarTable() {
        return varTableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getOutputTable() {
        return outputTableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOutputTable_Reject() {
        return (EAttribute)outputTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOutputTable_RejectInnerJoin() {
        return (EAttribute)outputTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOutputTable_IsErrorRejectTable() {
        return (EAttribute)outputTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOutputTable_IsJoinTableOf() {
        return (EAttribute)outputTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getInputTable() {
        return inputTableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getInputTable_MatchingMode() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getInputTable_LookupMode() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getInputTable_GlobalMapKeysValues() {
        return (EReference)inputTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getInputTable_InnerJoin() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getInputTable_Persistent() {
        return (EAttribute)inputTableEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getSizeState() {
        return sizeStateEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getOperator() {
        return operatorEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MapperFactory getMapperFactory() {
        return (MapperFactory)getEFactoryInstance();
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
        mapperDataEClass = createEClass(MAPPER_DATA);
        createEReference(mapperDataEClass, MAPPER_DATA__UI_PROPERTIES);
        createEReference(mapperDataEClass, MAPPER_DATA__VAR_TABLES);
        createEReference(mapperDataEClass, MAPPER_DATA__OUTPUT_TABLES);
        createEReference(mapperDataEClass, MAPPER_DATA__INPUT_TABLES);

        mapperTableEntryEClass = createEClass(MAPPER_TABLE_ENTRY);
        createEAttribute(mapperTableEntryEClass, MAPPER_TABLE_ENTRY__NAME);
        createEAttribute(mapperTableEntryEClass, MAPPER_TABLE_ENTRY__EXPRESSION);
        createEAttribute(mapperTableEntryEClass, MAPPER_TABLE_ENTRY__TYPE);
        createEAttribute(mapperTableEntryEClass, MAPPER_TABLE_ENTRY__NULLABLE);
        createEAttribute(mapperTableEntryEClass, MAPPER_TABLE_ENTRY__OPERATOR);

        abstractDataMapTableEClass = createEClass(ABSTRACT_DATA_MAP_TABLE);
        createEAttribute(abstractDataMapTableEClass, ABSTRACT_DATA_MAP_TABLE__SIZE_STATE);
        createEAttribute(abstractDataMapTableEClass, ABSTRACT_DATA_MAP_TABLE__NAME);
        createEAttribute(abstractDataMapTableEClass, ABSTRACT_DATA_MAP_TABLE__MINIMIZED);
        createEReference(abstractDataMapTableEClass, ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES);

        abstractInOutTableEClass = createEClass(ABSTRACT_IN_OUT_TABLE);
        createEAttribute(abstractInOutTableEClass, ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER);
        createEAttribute(abstractInOutTableEClass, ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER);
        createEAttribute(abstractInOutTableEClass, ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL);
        createEAttribute(abstractInOutTableEClass, ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER);
        createEAttribute(abstractInOutTableEClass, ABSTRACT_IN_OUT_TABLE__ID);

        uiPropertiesEClass = createEClass(UI_PROPERTIES);
        createEAttribute(uiPropertiesEClass, UI_PROPERTIES__SHELL_MAXIMIZED);

        varTableEClass = createEClass(VAR_TABLE);

        outputTableEClass = createEClass(OUTPUT_TABLE);
        createEAttribute(outputTableEClass, OUTPUT_TABLE__REJECT);
        createEAttribute(outputTableEClass, OUTPUT_TABLE__REJECT_INNER_JOIN);
        createEAttribute(outputTableEClass, OUTPUT_TABLE__IS_ERROR_REJECT_TABLE);
        createEAttribute(outputTableEClass, OUTPUT_TABLE__IS_JOIN_TABLE_OF);

        inputTableEClass = createEClass(INPUT_TABLE);
        createEAttribute(inputTableEClass, INPUT_TABLE__MATCHING_MODE);
        createEAttribute(inputTableEClass, INPUT_TABLE__LOOKUP_MODE);
        createEReference(inputTableEClass, INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES);
        createEAttribute(inputTableEClass, INPUT_TABLE__INNER_JOIN);
        createEAttribute(inputTableEClass, INPUT_TABLE__PERSISTENT);

        // Create enums
        sizeStateEEnum = createEEnum(SIZE_STATE);
        operatorEEnum = createEEnum(OPERATOR);
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

        // Obtain other dependent packages
        TalendFilePackage theTalendFilePackage = (TalendFilePackage)EPackage.Registry.INSTANCE.getEPackage(TalendFilePackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        mapperDataEClass.getESuperTypes().add(theTalendFilePackage.getAbstractExternalData());
        abstractInOutTableEClass.getESuperTypes().add(this.getAbstractDataMapTable());
        varTableEClass.getESuperTypes().add(this.getAbstractDataMapTable());
        outputTableEClass.getESuperTypes().add(this.getAbstractInOutTable());
        inputTableEClass.getESuperTypes().add(this.getAbstractInOutTable());

        // Initialize classes and features; add operations and parameters
        initEClass(mapperDataEClass, MapperData.class, "MapperData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getMapperData_UiProperties(), this.getUiProperties(), null, "uiProperties", null, 0, 1, MapperData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMapperData_VarTables(), this.getVarTable(), null, "varTables", null, 0, -1, MapperData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMapperData_OutputTables(), this.getOutputTable(), null, "outputTables", null, 0, -1, MapperData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMapperData_InputTables(), this.getInputTable(), null, "inputTables", null, 0, -1, MapperData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(mapperTableEntryEClass, MapperTableEntry.class, "MapperTableEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getMapperTableEntry_Name(), ecorePackage.getEString(), "name", null, 0, 1, MapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMapperTableEntry_Expression(), ecorePackage.getEString(), "expression", null, 0, 1, MapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMapperTableEntry_Type(), ecorePackage.getEString(), "type", null, 0, 1, MapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMapperTableEntry_Nullable(), ecorePackage.getEBoolean(), "nullable", null, 0, 1, MapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMapperTableEntry_Operator(), ecorePackage.getEString(), "operator", null, 0, 1, MapperTableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(abstractDataMapTableEClass, AbstractDataMapTable.class, "AbstractDataMapTable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAbstractDataMapTable_SizeState(), this.getSizeState(), "sizeState", "", 0, 1, AbstractDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractDataMapTable_Name(), ecorePackage.getEString(), "name", null, 0, 1, AbstractDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractDataMapTable_Minimized(), ecorePackage.getEBoolean(), "minimized", null, 0, 1, AbstractDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getAbstractDataMapTable_MapperTableEntries(), this.getMapperTableEntry(), null, "mapperTableEntries", null, 0, -1, AbstractDataMapTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(abstractInOutTableEClass, AbstractInOutTable.class, "AbstractInOutTable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAbstractInOutTable_ExpressionFilter(), ecorePackage.getEString(), "expressionFilter", null, 0, 1, AbstractInOutTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractInOutTable_ActivateExpressionFilter(), ecorePackage.getEBoolean(), "activateExpressionFilter", null, 0, 1, AbstractInOutTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractInOutTable_ActivateCondensedTool(), ecorePackage.getEBoolean(), "activateCondensedTool", null, 0, 1, AbstractInOutTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractInOutTable_ActivateColumnNameFilter(), ecorePackage.getEBoolean(), "activateColumnNameFilter", null, 0, 1, AbstractInOutTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAbstractInOutTable_Id(), ecorePackage.getEString(), "id", null, 0, 1, AbstractInOutTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(uiPropertiesEClass, UiProperties.class, "UiProperties", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getUiProperties_ShellMaximized(), ecorePackage.getEBoolean(), "shellMaximized", null, 0, 1, UiProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(varTableEClass, VarTable.class, "VarTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(outputTableEClass, OutputTable.class, "OutputTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getOutputTable_Reject(), ecorePackage.getEBoolean(), "reject", null, 0, 1, OutputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getOutputTable_RejectInnerJoin(), ecorePackage.getEBoolean(), "rejectInnerJoin", null, 0, 1, OutputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getOutputTable_IsErrorRejectTable(), ecorePackage.getEBoolean(), "isErrorRejectTable", null, 0, 1, OutputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getOutputTable_IsJoinTableOf(), ecorePackage.getEString(), "isJoinTableOf", null, 0, 1, OutputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(inputTableEClass, InputTable.class, "InputTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getInputTable_MatchingMode(), ecorePackage.getEString(), "matchingMode", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getInputTable_LookupMode(), ecorePackage.getEString(), "lookupMode", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getInputTable_GlobalMapKeysValues(), this.getMapperTableEntry(), null, "globalMapKeysValues", null, 0, -1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getInputTable_InnerJoin(), ecorePackage.getEBoolean(), "innerJoin", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getInputTable_Persistent(), ecorePackage.getEBoolean(), "persistent", null, 0, 1, InputTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(sizeStateEEnum, SizeState.class, "SizeState");
        addEEnumLiteral(sizeStateEEnum, SizeState.MINIMIZED);
        addEEnumLiteral(sizeStateEEnum, SizeState.INTERMEDIATE);
        addEEnumLiteral(sizeStateEEnum, SizeState.MAXIMIZED);

        initEEnum(operatorEEnum, Operator.class, "Operator");
        addEEnumLiteral(operatorEEnum, Operator.EQUALS);
        addEEnumLiteral(operatorEEnum, Operator.NOT_EQUALS);
        addEEnumLiteral(operatorEEnum, Operator.LOWER);
        addEEnumLiteral(operatorEEnum, Operator.LOWER_OR_EQUALS);
        addEEnumLiteral(operatorEEnum, Operator.GREATER);
        addEEnumLiteral(operatorEEnum, Operator.GREATER_OR_EQUALS);

        // Create resource
        createResource(eNS_URI);
    }

} //MapperPackageImpl
