package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.MenuType
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States

/**
 * Manages the menus' logic.
 */
class MenuManager(private val gbJam6: GBJam6) {

    var menus = mutableListOf<Menu>()
    var placingB: Building? = null
    private var frame = 0

    /**
     * Called when the user selects a spot of the map.
     */
    fun open(x: Float) {
        // Check if the user clicked on a building
        val building = City.buildings.firstOrNull { it.x <= x && x <= it.x + it.width }

        // Add the corresponding menu
        if (building == null) {
            menus.add(Menu(MenuType.CREATION, "SELECT CATEGORY", x + 4f, Def.menuY, gbJam6))
        } else {
            menus.add(Menu(MenuType.BUILDING, "SELECT ACTION", x + 4f, Def.menuY, gbJam6))
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
                    placingB = Building(Def.buildings[menu.cursorPos], position.x, -16f, gbJam6.manager)
                    updateBuilding(position.x, pointerY)
                    frame = 0
                    return States.PLACE_BUILDING
                }
                MenuType.BUILDING -> {
                    when (menu.items[menu.cursorPos]) {
                        "CITIZENS" -> {
                            val building = City.buildings.first { it.x <= position.x && position.x < it.x + it.width }
                            val citizens = building.citizens
                            var names = List(citizens.size) { citizens[it].name }
                            if (names.isEmpty()) names = arrayListOf("RETURN")
                            menus.add(Menu(MenuType.CITIZENS, "NO CITIZENS!", position.x + 4, Def.menuY, gbJam6, names.toTypedArray()))
                        }
                    }
                }
                MenuType.CITIZENS -> {
                    when (menu.items[menu.cursorPos]) {
                        "RETURN" -> close()
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
                City.buildings.add(placingB!!)
                close()
                placingB = null
            } else {
                return States.PLACE_BUILDING
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
        if (menus.any() && placingB == null) {
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

}