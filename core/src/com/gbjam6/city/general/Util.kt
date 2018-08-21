package com.gbjam6.city.general

import com.gbjam6.city.Building
import com.gbjam6.city.states.City
import kotlin.math.roundToInt

object Util {

    // INPUT
    var inputFreeze = 0
    var wasPressed = false

    fun getPixel(f: Float): Float = f.roundToInt().toFloat()

    fun getBuilding(x: Float): Building? = City.buildings.firstOrNull { it.x <= x && x < it.x + it.width }

    fun housingLeft(): Boolean {
        for (building in City.buildings) {
            if (building.citizens.size < building.lBuilding.capacity)
                return true
        }
        return false
    }

    fun tick() {
        println("tick")
        var ressources = Ressources(0,0,0,0,0)
        for (building in City.buildings){
            ressources add building.getProduction()
        }
        City.ressources addLimit ressources
    }

    /**
     * Called when a building is placed.
     */
    fun placeBuilding(placingB: Building) {
        City.buildings.add(placingB)
        placingB.placed()
    }

}