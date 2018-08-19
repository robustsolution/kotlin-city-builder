package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.MenuType
import com.gbjam6.city.graphics.Menu

/**
 *
 */
class MenuManager(private val gbJam6: GBJam6) {

    private val menus = mutableListOf<Menu>()

    /**
     * Called when the user selects a spot of the map.
     */
    fun open(buildings: MutableList<Building>, x: Float) {
        // Check if the user clicked on a building
        val building = buildings.firstOrNull { it.x <= x && x <= it.x + it.width }

        // Add the corresponding menu
        if (building == null) {
            menus.add(Menu(MenuType.CREATION, x + 8f, 68f, gbJam6))
        } else {
            menus.add(Menu(MenuType.BUILDING, x + 8f, 68f, gbJam6))
        }
    }

    /**
     * Called to close a menu.
     */
    fun close() {
        if (menus.any())
            menus.removeAt(menus.lastIndex)
    }

    /**
     * Called when the user selects an option from the menu.
     */
    fun select() {
        val menu = menus.last()
        when (Pair(menu.type, menu.cursorPos)) {
            Pair(MenuType.BUILDING, 0) -> {
                val citizens = menu.building!!.citizens
                val list = List(citizens.size) { citizens[it].name }
            }
            Pair(MenuType.CITIZENS, 0) -> {
            }
            Pair(MenuType.CONFIRM, 0) -> {
            }
            Pair(MenuType.IMPROVE, 0) -> {
            }
        }
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
        if (menus.any())
            menus.last().draw(batch, font)
    }

}