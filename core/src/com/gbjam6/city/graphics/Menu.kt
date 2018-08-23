package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.MenuType
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Util
import com.gbjam6.city.states.City

/**
 * Simple list of [items].
 * [items] can be initialized automatically using [Def.menus] map.
 */
class Menu(val type: MenuType, val title: String, var x: Float, val y: Float, gbJam6: GBJam6, array: Array<String>? = null, validity: Array<Boolean>? = null) {

    var items: Array<String> = array ?: Def.menus[type] ?: arrayOf("RETURN")
    val activated = validity ?: Array(items.size) { true }
    private val height = (items.size * 9 + 19).toFloat()
    private val texture = Util.generateRectangle(Def.menuWidth.toInt(), height.toInt(), Def.color1)
    private val cursor = gbJam6.manager.get("sprites/smallPointerRight.png", Texture::class.java)

    var cursorPos = 0

    fun draw(batch: SpriteBatch, font: BitmapFont) {
        // Draws the background
        batch.draw(texture, x, y - height)

        // Draws the title
        font.color = Def.color4
        font.draw(batch, title, x, y - 4, Def.menuWidth, 1, false)

        // Draws the items
        for ((i, item) in items.withIndex()) {
            if (activated[i]) font.color = Def.color4 else font.color = Def.color2
            font.draw(batch, item, x + 16f, y - 17 - 9f * i)
        }

        // Draws the cursor
        batch.draw(cursor, x + 8f, y - (cursorPos + 1) * 9f - 13)
    }

    /**
     * Called to update the menu items.
     * Updates [activated] array and [items] for [MenuType.CITIZENS].
     */
    fun changeValidity() {
        val building = Util.getBuilding()

        when (type) {

            // Valid items are buildings which can be constructed
            MenuType.CATEGORY -> {
                // Creates the LBuildings list
                val lBuildings = Array(items.lastIndex) { i -> Def.buildings.first { it.name == items[i] } }

                // Don't change the last item's validity ("RETURN")
                for (i in 0 until items.lastIndex) {
                    activated[i] = City.ressources.stone >= lBuildings[i].cost
                }
            }

            // Valid items are the different possible actions on the building
            MenuType.BUILDING -> {
                val b = building!!
                for ((i, item) in items.withIndex()) {
                    when (item) {
                        "UPGRADE" -> activated[i] = b.canUpgrade()
                        "REPAIR" -> activated[i] = b.canRepair()
                        "BIRTH" -> activated[i] = City.ressources.happiness >= City.progress.birthcost && b.citizens.size < b.lBuilding.capacity && City.ressources.citizens < City.limits.citizens
                        "EXCHANGE" -> activated[i] = City.ressources.food >= Def.EXCHANGE_VALUE && building.exchangeTimer == Def.EXCHANGE_TIME
                        "DESTROY" -> activated[i] = City.ressources.happiness >= b.lBuilding.cost*Def.DESTROY_HAP_PCT

                    }
                }
            }

            // Removes dead citizens
            MenuType.CITIZENS -> {
                val b = building!!
                if (items.size - 1 != b.citizens.size) {
                    // Gets the living citizens names
                    val tempItems = MutableList(b.citizens.size) { b.citizens[it].name }
                    tempItems.add("RETURN")
                    // Updates displayed items and resets the cursor position
                    items = tempItems.toTypedArray()
                    cursorPos = 0
                }
            }

            // Updates well according to watered citizens
            MenuType.HYDRATE -> {
                val b = building!!
                activated[0] = b.wateredCitizens.size < 2
                activated[1] = b.wateredCitizens.size > 0
            }

            // Updates expand menu
            MenuType.EXPAND -> {
                val nExp = Util.expandsMade()
                activated[0] = nExp < Def.EXPAND_COST.size && City.ressources.happiness >= Def.EXPAND_COST[nExp]
            }

            else -> Unit
        }
    }

}