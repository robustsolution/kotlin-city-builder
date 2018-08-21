package com.gbjam6.city.general

import com.gbjam6.city.Building
import com.gbjam6.city.MenuManager
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States
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
    }

    /**
     * Called when a building is placed.
     */
    fun placeBuilding(placingB: Building) {
        City.buildings.add(placingB)
        update()
    }

    fun update() {
    }

    /**
     * Show a helper
     */
    fun showIDLEHelper() {
        MenuManager.helper.visible = !MenuManager.helper.visible
    }

    fun updateHelper(x: Float) {
        when (City.state) {
            // Building description helper
            States.IDLE -> {
                val building = getBuilding(x)
                if (building != null) {
                    MenuManager.helper.update(building.lBuilding.name, "This building is a really cool one. Build it for 100 Stones.")
                } else {
                    MenuManager.helper.update("EMPTY", "YOU CAN BUILD HERE.")
                }
            }
        }

    }


}