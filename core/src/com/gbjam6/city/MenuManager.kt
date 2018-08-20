package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.MenuType
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.states.States

/**
 * Manages the menus' logic.
 */
class MenuManager(private val gbJam6: GBJam6) {

    private var menus = mutableListOf<Menu>()
    var placingB: Building? = null

    /**
     * Called when the user selects a spot of the map.
     */
    fun open(buildings: MutableList<Building>, x: Float) {
        // Check if the user clicked on a building
        val building = buildings.firstOrNull { it.x <= x && x <= it.x + it.width }

        // Add the corresponding menu
        if (building == null) {
            menus.add(Menu(MenuType.CREATION, "SELECT CATEGORY", x + 4f, 68f, gbJam6))
        } else {
            menus.add(Menu(MenuType.BUILDING, "SELECT ACTION", x + 4f, 68f, gbJam6))
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
    fun select(state: States, position: Vector3, buildings: MutableList<Building>, pointerY: Float): States {
        if (state == States.MENU) {
            // A menu item is selected
            val menu = menus.last()
            when (menu.type) {
                MenuType.CREATION -> {
                    placingB = Building(Def.buildings[menu.cursorPos], position.x, -16f, gbJam6.manager)
                    updateBuilding(position.x, pointerY)
                    close()
                    return States.PLACE_BUILDING
                }
                MenuType.BUILDING -> {
                    //val citizens = menu.building!!.citizens
                    //val list = List(citizens.size) { citizens[it].name }
                }
                MenuType.CITIZENS -> {
                }
                MenuType.CONFIRM -> {
                }
                MenuType.IMPROVE -> {
                }
            }
            return States.MENU
        } else if (state == States.PLACE_BUILDING) {
            // The building is placed
            buildings.add(placingB!!)
            placingB = null
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
    fun draw(batch: SpriteBatch, font: BitmapFont) {
        // Draw the menu
        if (menus.any()) {
            menus.last().draw(batch, font)
        }

        // Draw the building if it's not null
        placingB?.draw(batch)
    }

    fun updateBuilding(x: Float, y: Float) {
        placingB?.let {
            it.x = x - it.width / 2
            it.y = y + 15
        }
    }

}