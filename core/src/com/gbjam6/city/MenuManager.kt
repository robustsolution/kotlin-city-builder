package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.gbjam6.city.general.*
import com.gbjam6.city.graphics.Helper
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.logic.Citizen
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States
import java.util.*

/**
 * Manages the menus' logic.
 */
class MenuManager(private val gbJam6: GBJam6) {

    var menus = mutableListOf<Menu>()
    var placingB: Building? = null
    var placingC: Citizen? = null
    private var frame = 0

    companion object {
        var helper = Helper()
    }

    /**
     * Called when the user selects a spot of the map.
     */
    fun open(x: Float) {
        // Check if the user clicked on a building
        val building = Util.getBuilding(x)

        // Add the corresponding menu
        if (building == null) {
            menus.add(Menu(MenuType.CREATION, "SELECT CATEGORY", x + 4f, Def.menuY, gbJam6))
        } else {
            val items = Def.customMenus[building.lBuilding.name] ?: Def.menus[MenuType.BUILDING]!!
            menus.add(Menu(MenuType.BUILDING, "SELECT ACTION", x + 4f, Def.menuY, gbJam6, items))
            menus.last().changeValidity(building)
        }
    }

    /**
     * Called to close a menu.
     */
    fun close() {
        if (menus.any())
            menus = menus.dropLast(1).toMutableList()
    }

    /**
     * Called when the user selects an option from the menu.
     */
    fun select(position: Vector3, pointerY: Float): States {
        if (City.state == States.MENU) {
            // A menu item is selected
            val menu = menus.last()
            when (menu.type) {
                MenuType.CREATION -> {
                    // TODO: Building conditions
                    val categoryB = Def.buildings.filter { it.type == BuildingType.valueOf(menu.items[menu.cursorPos]) }
                    val items = Array(categoryB.size) { categoryB[it].name }
                    val validity = Array(categoryB.size) { categoryB[it].cost <= City.ressources.stone }
                    menus.add(Menu(MenuType.CATEGORY, menu.items[menu.cursorPos], position.x + 4f, Def.menuY, gbJam6, items, validity))
                }
                // The user chooses a building to build
                MenuType.CATEGORY -> {
                    if (menu.activated[menu.cursorPos]) {
                        placingB = Building(Def.buildings.first { it.name == menu.items[menu.cursorPos] }, position.x, -16f, gbJam6.manager)
                        updateBuilding(position.x, pointerY)
                        frame = 0
                        return States.PLACE_BUILDING
                    }
                }
                // The user checks a building
                MenuType.BUILDING -> {
                    when (menu.items[menu.cursorPos]) {
                        "CITIZENS" -> {
                            val building = Util.getBuilding(position.x)!!
                            val citizens = building.citizens
                            val names = MutableList(citizens.size) { citizens[it].name }
                            val title = if (names.isEmpty()) "NO CITIZENS!" else "CHECK&MOVE"
                            names.add("RETURN")
                            menus.add(Menu(MenuType.CITIZENS, title, position.x + 4, Def.menuY, gbJam6, names.toTypedArray()))
                        }
                        "BIRTH" -> {
                            if (menu.activated[menu.cursorPos]) {
                                val building = Util.getBuilding(position.x)!!
                                placingC = Citizen(Def.names.random(), building)
                                building.citizens.add(placingC!!)
                                City.ressources.citizens += 1
                                City.ressources.happiness -= Def.BIRTH_COST
                                return States.PLACE_CITIZEN
                            }
                        }
                    }
                }
                // The user selects a citizen
                MenuType.CITIZENS -> {
                    when (menu.items[menu.cursorPos]) {
                        "RETURN" -> close()
                        else -> {
                            if (Util.housingLeft()) {
                                val building = Util.getBuilding(position.x)!!
                                placingC = building.citizens[menu.cursorPos]
                                return States.PLACE_CITIZEN
                            }
                        }
                    }
                }
                MenuType.CONFIRM -> {
                }
                MenuType.IMPROVE -> {
                }
            }
            return States.MENU
        } else if (City.state == States.PLACE_BUILDING) {
            // The building is placed
            if (placingB!!.validPos) {
                Util.placeBuilding(placingB!!)
                menus.clear()
                placingB = null
            } else {
                return States.PLACE_BUILDING
            }
        } else if (City.state == States.PLACE_CITIZEN) {
            val building = Util.getBuilding(position.x)
            if (building != null && building.citizens.size < building.lBuilding.capacity) {
                // We place the citizen in this building
                placingC!!.building.citizens.remove(placingC!!)
                placingC!!.building = building
                building.citizens.add(placingC!!)
                placingC = null
                menus.clear()
            } else {
                return States.PLACE_CITIZEN
            }
        }

        return States.IDLE

    }

    /**
     * Called when the user wants to select another item.
     */
    fun moveCursor(i: Int) {
        val menu = menus.last()
        menu.cursorPos = (menu.cursorPos + menu.items.size + i) % menu.items.size
    }

    /**
     * Draws the visible menu.
     */
    fun drawMenu(batch: SpriteBatch, font: BitmapFont) {
        // Draw the menu
        if (menus.any() && placingB == null && placingC == null) {
            menus.last().draw(batch, font)
        }
    }

    /**
     * Draws the moving building
     */
    fun drawBuilding(batch: SpriteBatch) {
        placingB?.let {
            if (it.validPos || !it.validPos && frame < 30)
                it.draw(batch)
        }
    }

    /**
     * Move [placingB] with the camera.
     */
    fun updateBuilding(x: Float, y: Float) {
        placingB?.let {
            it.x = x - it.lBuilding.door.first - Math.ceil((it.lBuilding.door.second - it.lBuilding.door.first) / 2.0).toFloat()
            it.y = y - 2
            it.validPos = it.isValid()
        }
    }

    /**
     * Move [menus] when the camera moved (for instance during [States.PLACE_BUILDING]).
     */
    fun updateMenu(x: Float) {
        for (menu in menus)
            menu.x = x + 4
    }

    /**
     * Flips [placingB].
     */
    fun flip(x: Float, y: Float) {
        placingB?.flip()
        updateBuilding(x, y)
    }

    /**
     * Called each frame, used to make [placingB] blink.
     */
    fun update() {
        frame = (frame + 1) % 60
    }

    fun drawHelper(batch: SpriteBatch, smallFont: BitmapFont, x: Float) {
        helper.draw(batch, smallFont, x - 76f)
    }

}