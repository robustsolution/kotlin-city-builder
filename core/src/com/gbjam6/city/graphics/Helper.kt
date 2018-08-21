package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.Building
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util

/**
 * Show informations on the left side of the screen.
 *
 * TODO: Separate lines with \n
 */
class Helper {

    private var title = "TITLE"
    private var description = ""
    private var texture: Texture? = null
    var visible = false

    fun update(title: String, description: String) {
        // Update Strings
        this.title = title
        this.description = description

        // Update the background
        val height = (description.length / 14f + 1) * 9 + 15
        val pixmap = Pixmap(Def.helperWidth.toInt(), Util.getPixel(height).toInt(), Pixmap.Format.RGBA8888)
        pixmap.setColor(Def.color1)
        pixmap.fillRectangle(0, 0, Def.helperWidth.toInt(), Util.getPixel(height).toInt())
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    fun draw(batch: SpriteBatch, font: BitmapFont, x: Float) {
        if (visible) {
            texture?.let {
                // Draw the background
                batch.draw(it, x, Def.menuY - it.height)

                font.color = Def.color4
                font.draw(batch, title, x, Def.menuY - 4, Def.helperWidth, 1, false)

                font.draw(batch, description, x + 1, Def.menuY - 17, Def.helperWidth, 1, true)

            }
        }
    }

}