// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package performance;

import junit.framework.TestCase;

import org.talend.commons.utils.time.TimeMeasure;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class PerformanceJavaTMap extends TestCase {

    /**
     *
     * DOC amaumont Perf class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    class PrimitiveBean {

        public int intVar0 = Integer.MAX_VALUE;

        public int intVar1 = Integer.MAX_VALUE;

        public int intVar2 = Integer.MAX_VALUE;

        public int intVar3 = Integer.MAX_VALUE;

        public int intVar4 = Integer.MAX_VALUE;

        public int intVar5 = Integer.MAX_VALUE;

        public int intVar6 = Integer.MAX_VALUE;

        public int intVar7 = Integer.MAX_VALUE;

        public int intVar8 = Integer.MAX_VALUE;

        public int intVar9 = Integer.MAX_VALUE;

    }

    /**
     *
     * DOC amaumont Perf class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    class NullablePrimitiveBean {

        public int[] intVar0 = new int[1];

        public int[] intVar1 = new int[1];

        public int[] intVar2 = new int[1];

        public int[] intVar3 = new int[1];

        public int[] intVar4 = new int[1];

        public int[] intVar5 = new int[1];

        public int[] intVar6 = new int[1];

        public int[] intVar7 = new int[1];

        public int[] intVar8 = new int[1];

        public int[] intVar9 = new int[1];

    }

    /**
     *
     * DOC amaumont Perf class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    class IntegerBean {

        public Integer integerVar0 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar1 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar2 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar3 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar4 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar5 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar6 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar7 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar8 = new Integer(Integer.MAX_VALUE);

        public Integer integerVar9 = new Integer(Integer.MAX_VALUE);

    }

    /**
     *
     * DOC amaumont Perf class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private static class StaticPrimitiveBeanInput {

        public static int intVar0 = Integer.MAX_VALUE;

        public static int intVar1 = Integer.MAX_VALUE;

        public static int intVar2 = Integer.MAX_VALUE;

        public static int intVar3 = Integer.MAX_VALUE;

        public static int intVar4 = Integer.MAX_VALUE;

        public static int intVar5 = Integer.MAX_VALUE;

        public static int intVar6 = Integer.MAX_VALUE;

        public static int intVar7 = Integer.MAX_VALUE;

        public static int intVar8 = Integer.MAX_VALUE;

        public static int intVar9 = Integer.MAX_VALUE;

    }

    /**
     *
     * DOC amaumont Perf class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private static class StaticPrimitiveBeanOutput {

        public static int intVar0 = Integer.MAX_VALUE;

        public static int intVar1 = Integer.MAX_VALUE;

        public static int intVar2 = Integer.MAX_VALUE;

        public static int intVar3 = Integer.MAX_VALUE;

        public static int intVar4 = Integer.MAX_VALUE;

        public static int intVar5 = Integer.MAX_VALUE;

        public static int intVar6 = Integer.MAX_VALUE;

        public static int intVar7 = Integer.MAX_VALUE;

        public static int intVar8 = Integer.MAX_VALUE;

        public static int intVar9 = Integer.MAX_VALUE;

    }

    private static final int N_ITERATION = 500000000;

    private PrimitiveBean beanInput;

    public void testPrimitiveIntPropertiesCopyWithLocalBeansWithOperations() {

        PrimitiveBean beanInput = new PrimitiveBean();

        TimeMeasure.begin("testPrimitiveIntPropertiesCopyWithLocalBeansWithOperations"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            beanInput.intVar0 = beanInput.intVar0 + Integer.MIN_VALUE;
            beanInput.intVar1 = beanInput.intVar1 + Integer.MIN_VALUE;
            beanInput.intVar2 = beanInput.intVar2 + Integer.MIN_VALUE;
            beanInput.intVar3 = beanInput.intVar3 + Integer.MIN_VALUE;
            beanInput.intVar4 = beanInput.intVar4 + Integer.MIN_VALUE;
            beanInput.intVar5 = beanInput.intVar5 + Integer.MIN_VALUE;
            beanInput.intVar6 = beanInput.intVar6 + Integer.MIN_VALUE;
            beanInput.intVar7 = beanInput.intVar7 + Integer.MIN_VALUE;
            beanInput.intVar8 = beanInput.intVar8 + Integer.MIN_VALUE;
            beanInput.intVar9 = beanInput.intVar9 + Integer.MIN_VALUE;

        }

        TimeMeasure.end("testPrimitiveIntPropertiesCopyWithLocalBeansWithOperations"); //$NON-NLS-1$
        System.out.println();

    }

    public void testPrimitiveIntPropertiesCopyWithInstanceBeansWithOperations() {

        beanInput = new PrimitiveBean();

        TimeMeasure.begin("testPrimitiveIntPropertiesCopyWithLocalBeansWithOperations"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            beanInput.intVar0 = beanInput.intVar0 + Integer.MIN_VALUE;
            beanInput.intVar1 = beanInput.intVar1 + Integer.MIN_VALUE;
            beanInput.intVar2 = beanInput.intVar2 + Integer.MIN_VALUE;
            beanInput.intVar3 = beanInput.intVar3 + Integer.MIN_VALUE;
            beanInput.intVar4 = beanInput.intVar4 + Integer.MIN_VALUE;
            beanInput.intVar5 = beanInput.intVar5 + Integer.MIN_VALUE;
            beanInput.intVar6 = beanInput.intVar6 + Integer.MIN_VALUE;
            beanInput.intVar7 = beanInput.intVar7 + Integer.MIN_VALUE;
            beanInput.intVar8 = beanInput.intVar8 + Integer.MIN_VALUE;
            beanInput.intVar9 = beanInput.intVar9 + Integer.MIN_VALUE;

        }

        TimeMeasure.end("testPrimitiveIntPropertiesCopyWithLocalBeansWithOperations"); //$NON-NLS-1$
        System.out.println();

    }

    public void testBeansReferenceCopy() {

        PrimitiveBean row1 = new PrimitiveBean();
        PrimitiveBean row2;

        TimeMeasure.begin("testBeansReferenceCopy"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            row2 = row1;

        }

        TimeMeasure.end("testBeansReferenceCopy"); //$NON-NLS-1$
        System.out.println();

    }

    public void testPrimitiveIntPropertiesCopyWithLocalBeans() {

        PrimitiveBean beanInput = new PrimitiveBean();
        PrimitiveBean beanOutput = new PrimitiveBean();

        TimeMeasure.begin("testPrimitiveIntPropertiesCopyWithLocalBeans"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            beanOutput.intVar0 = beanInput.intVar0;
            beanOutput.intVar1 = beanInput.intVar1;
            beanOutput.intVar2 = beanInput.intVar2;
            beanOutput.intVar3 = beanInput.intVar3;
            beanOutput.intVar4 = beanInput.intVar4;
            beanOutput.intVar5 = beanInput.intVar5;
            beanOutput.intVar6 = beanInput.intVar6;
            beanOutput.intVar7 = beanInput.intVar7;
            beanOutput.intVar8 = beanInput.intVar8;
            beanOutput.intVar9 = beanInput.intVar9;

        }

        TimeMeasure.end("testPrimitiveIntPropertiesCopyWithLocalBeans"); //$NON-NLS-1$
        System.out.println();

    }

    public void testIntegerPropertiesCopyWithLocalBeans() {

        IntegerBean beanInput = new IntegerBean();
        IntegerBean beanOutput = new IntegerBean();

        TimeMeasure.begin("testIntegerPropertiesCopyWithLocalBeans"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            beanOutput.integerVar0 = beanInput.integerVar0;
            beanOutput.integerVar1 = beanInput.integerVar1;
            beanOutput.integerVar2 = beanInput.integerVar2;
            beanOutput.integerVar3 = beanInput.integerVar3;
            beanOutput.integerVar4 = beanInput.integerVar4;
            beanOutput.integerVar5 = beanInput.integerVar5;
            beanOutput.integerVar6 = beanInput.integerVar6;
            beanOutput.integerVar7 = beanInput.integerVar7;
            beanOutput.integerVar8 = beanInput.integerVar8;
            beanOutput.integerVar9 = beanInput.integerVar9;

        }

        TimeMeasure.end("testIntegerPropertiesCopyWithLocalBeans"); //$NON-NLS-1$
        System.out.println();

    }

    public void testPrimitivePropertiesCopyWithStaticBeans() {

        TimeMeasure.begin("testPrimitivePropertiesCopyWithStaticBeans"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            StaticPrimitiveBeanOutput.intVar0 = StaticPrimitiveBeanInput.intVar0;
            StaticPrimitiveBeanOutput.intVar1 = StaticPrimitiveBeanInput.intVar1;
            StaticPrimitiveBeanOutput.intVar2 = StaticPrimitiveBeanInput.intVar2;
            StaticPrimitiveBeanOutput.intVar3 = StaticPrimitiveBeanInput.intVar3;
            StaticPrimitiveBeanOutput.intVar4 = StaticPrimitiveBeanInput.intVar4;
            StaticPrimitiveBeanOutput.intVar5 = StaticPrimitiveBeanInput.intVar5;
            StaticPrimitiveBeanOutput.intVar6 = StaticPrimitiveBeanInput.intVar6;
            StaticPrimitiveBeanOutput.intVar7 = StaticPrimitiveBeanInput.intVar7;
            StaticPrimitiveBeanOutput.intVar8 = StaticPrimitiveBeanInput.intVar8;
            StaticPrimitiveBeanOutput.intVar9 = StaticPrimitiveBeanInput.intVar9;

        }

        TimeMeasure.end("testPrimitivePropertiesCopyWithStaticBeans"); //$NON-NLS-1$
        System.out.println();

    }

    public void testPrimitiveIntArrayCopyWithLocalBeans() {

        int[] intArrayInput = new int[10];
        int[] intArrayOutput = new int[10];

        TimeMeasure.begin("testPrimitiveIntArrayCopyWithLocalBeans"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            intArrayOutput[0] = intArrayInput[0];
            intArrayOutput[1] = intArrayInput[1];
            intArrayOutput[2] = intArrayInput[2];
            intArrayOutput[3] = intArrayInput[3];
            intArrayOutput[4] = intArrayInput[4];
            intArrayOutput[5] = intArrayInput[5];
            intArrayOutput[6] = intArrayInput[6];
            intArrayOutput[7] = intArrayInput[7];
            intArrayOutput[8] = intArrayInput[8];
            intArrayOutput[9] = intArrayInput[9];

        }

        TimeMeasure.end("testPrimitiveIntArrayCopyWithLocalBeans"); //$NON-NLS-1$
        System.out.println();

    }

    public void testCopyFirstPrimitiveArrayValueInFirstPrimitiveArrayValue() {

        NullablePrimitiveBean beanInput = new NullablePrimitiveBean();
        NullablePrimitiveBean beanOutput = new NullablePrimitiveBean();

        TimeMeasure.begin("testCopyFirstPrimitiveArrayValueInFirstPrimitiveArrayValue"); //$NON-NLS-1$

        for (int i = 0; i < N_ITERATION; i++) {

            beanOutput.intVar0[0] = beanInput.intVar0[0];
            beanOutput.intVar1[0] = beanInput.intVar1[0];
            beanOutput.intVar2[0] = beanInput.intVar2[0];
            beanOutput.intVar3[0] = beanInput.intVar3[0];
            beanOutput.intVar4[0] = beanInput.intVar4[0];
            beanOutput.intVar5[0] = beanInput.intVar5[0];
            beanOutput.intVar6[0] = beanInput.intVar6[0];
            beanOutput.intVar7[0] = beanInput.intVar7[0];
            beanOutput.intVar8[0] = beanInput.intVar8[0];
            beanOutput.intVar9[0] = beanInput.intVar9[0];

        }

        TimeMeasure.end("testCopyFirstPrimitiveArrayValueInFirstPrimitiveArrayValue"); //$NON-NLS-1$
        System.out.println();

    }
}
