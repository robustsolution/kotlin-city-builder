package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.MenuType
import kotlin.math.min
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.gbjam6.city.Building
import com.gbjam6.city.GBJam6

/**
 * Simple list of [items].
 * [items] can be initialized automatically using [Def.menus] map.
 */
class Menu(val type: MenuType, val x: Float, val y: Float, gbJam6: GBJam6, array: Array<String> = arrayOf()) {

    val items: Array<String> = Def.menus[type] ?: array
    private val height = min(items.size * 16, 6 * 16).toFloat() + 8
    private val texture: Texture
    private val dot = gbJam6.manager.get("sprites/dot.png", Texture::class.java)
    val building: Building? = null

    var cursorPos = 0

    init {
        val pixmap = Pixmap(Def.menuWidth.toInt(), height.toInt(), Pixmap.Format.RGBA8888)
        pixmap.setColor(Def.darkColor)
        pixmap.fillRectangle(0, 0, Def.menuWidth.toInt(), height.toInt())
        texture = Texture(pixmap)
        pixmap.dispose()
    }


    fun draw(batch: SpriteBatch, font: BitmapFont) {
        // Draw the background
        batch.draw(texture, x, y - height)

        // Draw the items
        for ((i, item) in items.withIndex()) {
            font.draw(batch, item, x + 16f, y - 8f - 16f * i)
        }

        // Draw the cursor
        batch.draw(dot, x + 4f, y - (cursorPos + 1) * 16f)
    }

}