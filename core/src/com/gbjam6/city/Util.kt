package com.gbjam6.city

import kotlin.math.roundToInt

object Util {

    var inputFreeze = 0

    fun getPixel(f: Float): Float = f.roundToInt().toFloat()

}