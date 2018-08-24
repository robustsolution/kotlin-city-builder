package com.gbjam6.city.logic

import com.gbjam6.city.GBJam6
import com.gbjam6.city.graphics.Building
import com.gbjam6.city.general.Def
import com.gbjam6.city.states.City

class Citizen(val name: String, var building: Building, val parent: Citizen? = null) {
    var life: Int = City.progress.lifetime
    var water = false
    var well: Building? = null

    fun older() {
        life -= 1
        if (life <= 0) {
            building.citizensToKill.add(this)
        }
    }

    fun getDescription(): String {
        var description = "Life :\n${this.life}/${City.progress.lifetime}\n${this.building.lBuilding.name}"
        if (water) description += "\nHydrate"
        description += "\nProductivity :\n"+this.getProductivity()
        return description
    }

    fun getProductivity(): Double{
        var productivity = 1.0
        if (this.water) productivity += 0.5
        if (this.parent != null) {
                    if (this.parent!! in building.citizens)
                        productivity += 0.5
                }
        return productivity
    }

    override fun toString(): String {
        return this.name
    }
}