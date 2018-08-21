package com.gbjam6.city.general

import com.gbjam6.city.Building
import com.gbjam6.city.MenuManager
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States
import java.util.*
import kotlin.math.roundToInt


fun <T> List<T>.random(): T = this[Random().nextInt(this.size)]
fun <T> Array<T>.random(): T = this[Random().nextInt(this.size)]

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
        //println("tick")
        val ressources = Ressources()
        for (building in City.buildings) {
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

    /**
     * Show a helper
     */
    fun showIDLEHelper(x: Float) {
        MenuManager.helper.visible = !MenuManager.helper.visible
        updateHelper(x)
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