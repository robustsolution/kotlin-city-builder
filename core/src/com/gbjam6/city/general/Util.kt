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

    fun getBuilding(): Building? {
        val x = City.camera.position.x
        return City.buildings.firstOrNull { it.x <= x && x < it.x + it.width }
    }

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

        // Gets buildings' production and make them older
        for (building in City.buildings) {
            ressources add building.getProduction()
            building.older(ressources, buildingsToDestroy)
        }

        // Removes destroyed buildings
        for (building in buildingsToDestroy) {
            // TODO: Enlever de city (affichage et liste) et update le nombre de citizens
        }
        buildingsToDestroy.clear()

        // Updates the ressources count
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
     * Shows the helper
     */
    fun showIDLEHelper() {
        MenuManager.helper.visible = !MenuManager.helper.visible
    }

    /**
     * Updates the helper to show informations about the pointed building.
     */
    fun updateHelper(menus: MutableList<Menu>) {

        if (City.state == States.MENU) {
            Util.updateMenuHelper(menus)
        } else {
            val building = getBuilding()
            if (building != null) {
                // Displays informations about the building
                MenuManager.helper.update(building.lBuilding.name, building.getDescription())
            } else {
                // Indicates that the selected point is empty
                when (City.state) {
                    States.IDLE -> MenuManager.helper.update("EMPTY", "YOU CAN BUILD\nHERE!")
                    States.PLACE_CITIZEN -> MenuManager.helper.update("EMPTY", "YOU CANNOT \nPLACE THE\nCITIZEN HERE!")
                    else -> Unit
                }
            }
        }
    }

    fun updateMenuHelper(menus: MutableList<Menu>) {

        // Gets the displayed menu
        val menu = menus.lastOrNull()

        menu?.let {
            val item = menu.items[menu.cursorPos]

            // Updates the helper
            when (menu.type) {
                MenuType.CITIZENS -> {
                    val building = getBuilding()!!
                    if (menu.cursorPos < building.citizens.size) {
                        MenuManager.helper.update(item, building.citizens.elementAt(menu.cursorPos).getDescription())
                    } else {
                        MenuManager.helper.update(item, Def.getDescription("RETURN"))
                    }
                }
                MenuType.BUILDING -> {
                    val building = getBuilding()
                    when (item) {
                        "REPAIR" -> MenuManager.helper.update(item, "Integrity :\n${building!!.life}/${Def.BUILD_LIFE_TIME}\nRepair cost :\n${((1-building!!.life/Def.BUILD_LIFE_TIME.toFloat())*building!!.lBuilding.cost+1).toInt()}")
                        else -> MenuManager.helper.update(item, Def.getDescription(item))
                    }
                }
                else -> MenuManager.helper.update(item, Def.getDescription(item))
            }
        }
    }

}