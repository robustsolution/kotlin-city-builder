package com.gbjam6.city.general

import com.gbjam6.city.graphics.Building
import com.gbjam6.city.MenuManager
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States
import java.util.*
import kotlin.math.roundToInt


fun <T> List<T>.random(): T = this[Random().nextInt(this.size)]

object Util {

    // INPUT
    var inputFreeze = 0
    var wasPressed = false

    fun getPixel(f: Float): Float = f.roundToInt().toFloat()

    fun getBuilding(x: Float): Building? = City.buildings.firstOrNull { it.x <= x && x < it.x + it.width }

    /**
     * Returns true if at least one building can host one more citizen.
     */
    fun housingLeft(): Boolean {
        for (building in City.buildings) {
            if (building.citizens.size < building.lBuilding.capacity)
                return true
        }
        return false
    }

    fun tick() {
        // println("tick")
        val ressources = Ressources()
        val buildingsToDestroy = mutableListOf<Building>()

        // Get buildings' production and make them older
        for (building in City.buildings) {
            ressources add building.getProduction()
            building.older(ressources, buildingsToDestroy)
        }

        // Remove destroyed buildings
        for (building in buildingsToDestroy) {
            // TODO: Enlever de city (affichage et liste) et update le nombre de citizens
        }
        buildingsToDestroy.clear()

        // Update the ressources count
        City.ressources addLimit ressources
    }

    /**
     * Called when a building is placed.
     */
    fun placeBuilding(placingB: Building) {
        City.buildings.add(placingB)
        placingB.onPlaced()
    }

    /**
     * Show the helper
     */
    fun showIDLEHelper(x: Float) {
        MenuManager.helper.visible = !MenuManager.helper.visible
        updateHelper(x)
    }

    /**
     * Update the helper to show informations about the pointed building.
     */
    fun updateHelper(x: Float) {
        val building = getBuilding(x)
        if (building != null) {
            // Display informations about the building
            MenuManager.helper.update(building.lBuilding.name, building.getDescription())
        } else {
            // Indicate that the selected point is empty
            val desc = when (City.state) {
                States.IDLE -> "YOU CAN BUILD\nHERE!"
                States.PLACE_CITIZEN -> "YOU CANNOT \nPLACE THE\nCITIZEN HERE!"
                else -> Def.backupDesc
            }
            MenuManager.helper.update("EMPTY", desc)
        }
    }

    fun openAndShowHelper(x: Float) {
        updateHelper(x)
        MenuManager.helper.visible = true
    }

    fun updateMenuHelper(menus: MutableList<Menu>) {
        // Get the displayed menu
        val menu = menus.last()
        val item = menu.items[menu.cursorPos]

        // Update the helper
        when (menu.type) {
            MenuType.CITIZENS -> MenuManager.helper.update(item, "TODO")
            else -> MenuManager.helper.update(item, Def.descriptions[item] ?: Def.backupDesc)
        }
    }

}