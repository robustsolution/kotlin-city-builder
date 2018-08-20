package com.gbjam6.city.general

import kotlin.math.roundToInt

object Util {

    // INPUT
    var inputFreeze = 0
    var wasPressed = false

    fun getPixel(f: Float): Float = f.roundToInt().toFloat()

}