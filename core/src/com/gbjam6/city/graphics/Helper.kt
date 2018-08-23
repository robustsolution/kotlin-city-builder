package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util

/**
 * Shows informations on the left side of the screen.
 */
class Helper {

    private var title = ""
    private var description = ""
    private var texture: Texture? = null
    var visible = false

    fun update(title: String, description: String) {
        // Updates Strings
        this.title = title
        this.description = description

        // Creates the background ressource
        val height = description.split("\n").size * 7 + 21
        texture = Util.generateRectangle(Def.helperWidth.toInt(), height, Def.color1)
    }

    fun draw(batch: SpriteBatch, font: BitmapFont, x: Float) {
        if (visible) {
            texture?.let {
                // Draws the background
                batch.draw(it, x, Def.helperY - it.height)

                font.color = Def.color4
                font.draw(batch, title, x, Def.helperY - 4, Def.helperWidth, 1, false)
                font.draw(batch, description, x + 3, Def.helperY - 17)
            }
        }
    }

}