package com.gbjam6.city.logic

import com.gbjam6.city.graphics.Building
import com.gbjam6.city.general.Def

class Citizen(val name: String, var building: Building) {
    var life: Int = Def.LIFE_TIME
    var water = false
    var well: Building? = null

    fun older() {
        life -= 1
        if (life <= 0) {
            building.citizensToKill.add(this)
        }
    }

    fun getDescription(): String {
        var description = "Life :\n${this.life}/${Def.LIFE_TIME}\n${this.building.lBuilding.name}"
        if (water) description += "\nHydrate"
        return description
    }

    override fun toString(): String {
        return this.name
    }
}