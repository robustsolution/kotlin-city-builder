package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.MenuType
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.gbjam6.city.Building
import com.gbjam6.city.GBJam6
import com.gbjam6.city.states.City

/**
 * Simple list of [items].
 * [items] can be initialized automatically using [Def.menus] map.
 */
class Menu(val type: MenuType, val title: String, var x: Float, val y: Float, gbJam6: GBJam6, array: Array<String>? = null, validity: Array<Boolean>? = null) {

    val items: Array<String> = array ?: Def.menus[type] ?: arrayOf("RETURN")
    val activated = validity ?: Array(items.size) { true }
    private val height = (items.size * 9 + 19).toFloat()
    private val texture: Texture
    private val cursor = gbJam6.manager.get("sprites/smallPointerRight.png", Texture::class.java)

    var cursorPos = 0

    init {
        val pixmap = Pixmap(Def.menuWidth.toInt(), height.toInt(), Pixmap.Format.RGBA8888)
        pixmap.setColor(Def.color1)
        pixmap.fillRectangle(0, 0, Def.menuWidth.toInt(), height.toInt())
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    fun draw(batch: SpriteBatch, font: BitmapFont) {
        // Draw the background
        batch.draw(texture, x, y - height)

        // Draw the title
        font.color = Def.color4
        font.draw(batch, title, x, y - 4, Def.menuWidth, 1, false)

        // Draw the items
        for ((i, item) in items.withIndex()) {
            if (activated[i]) font.color = Def.color4 else font.color = Def.color2
            font.draw(batch, item, x + 16f, y - 17 - 9f * i)
        }

        // Draw the cursor
        batch.draw(cursor, x + 8f, y - (cursorPos + 1) * 9f - 13)
    }

    fun changeValidity(building: Building) {
        for ((i, item) in items.withIndex()) {
            when (item) {
                "USE" -> activated[i] = building.canUse()
                "UPGRADE" -> activated[i] = building.canUpgrade()
                "REPAIR" -> activated[i] = building.canRepair()
                "BIRTH" -> activated[i] = City.ressources.happiness >= Def.BIRTH_COST && building.citizens.size < building.lBuilding.capacity && City.ressources.citizens < City.limits.citizens
            }
        }
    }

}