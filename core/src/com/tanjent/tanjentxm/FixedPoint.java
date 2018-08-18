/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Provides macros and functions for converting floating point values to fixed-point integers and back. 
 * </p>
 * <p>
 * Decimal resolution is 1 / FP_SHIFT.
 * If FP_SHIFT is > 15 on a 32 bit system it will overflow when multiplied, causing nasty distortion or silence.
 * If FP_SHIFT is < 10 there will be either brutal quantization noise or silence.
 * </p>
 * @author Jonas Murman */
public class FixedPoint {
    /// Set to 14 to give some headroom for modules with many loud samples playing simultanously
    public static final int FP_SHIFT = 14;

    /// Bitwise AND-mask to get the fractional part of a FP number
    public static final int FP_FRAC_MASK = ((1 << FP_SHIFT) - 1);

    /// Multiply a floating point number by this to get the FP representation
    public static final float FP_FLOAT_MUL = (1 << FP_SHIFT);

    /// Multiply a FP number by this to get a floating point representation
    public static final float FP_FLOAT_MUL_INV = (1.0f / (float)(1 << FP_SHIFT));

    /** Converts a floating point value to a fixed point (FP) representation
     *
     * @param f the floating point value to convert */
    public static int FLOAT_TO_FP(float f)
    {
        return (int)(f * FixedPoint.FP_FLOAT_MUL);
    }

    /** Converts a fixed point (FP) value to a floating point value
     *
     * @param fp the fixed point value to convert */
    public static float FP_TO_FLOAT(int fp)
    {
        return (float)(fp * FixedPoint.FP_FLOAT_MUL_INV);
    }

    public static final int FP_HALF = FixedPoint.FLOAT_TO_FP(0.5f);
    public static final int FP_ONE = FixedPoint.FLOAT_TO_FP(1.0f);

    public static final int FP_ZERO_POINT_05 = FixedPoint.FLOAT_TO_FP(0.05f);
    public static final int FP_ZERO_POINT_95 = FixedPoint.FLOAT_TO_FP(0.95f);
}
