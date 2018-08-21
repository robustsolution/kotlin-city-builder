package com.gbjam6.city.logic

import com.gbjam6.city.graphics.Building
import com.gbjam6.city.general.Def

class Citizen(val name: String, var building: Building) {
    var life: Int = Def.LIFE_TIME

    fun older() {
        life -= 1
        if (life <= 0) {
            building.citizensToKill.add(this)
        }
    }
}