package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Def
import com.gbjam6.city.states.City

/**
 * Displays the different ressources on the top of the screen.
 */
class GUI(gbJam6: GBJam6) {

    private val texture = gbJam6.manager.get("sprites/gui/GUI.png", Texture::class.java)
    var visible = true

    fun draw(batch: SpriteBatch, font: BitmapFont) {
        if (visible) {

            val x = City.camera.position.x

            // Draws the ressources count
            batch.draw(texture, x - 80f, 42f)

            // Draws the ressources count
            font.draw(batch, String.format("%03d", City.ressources.citizens), x - 80f + 5, 70f)
            font.draw(batch, String.format("%03d", City.ressources.food), x - 80f + 5 + 32, 70f)
            font.draw(batch, String.format("%03d", City.ressources.stone), x - 80f + 5 + 2 * 32, 70f)
            font.draw(batch, String.format("%03d", City.limits.citizens), x - 80f + 19, 64f)
            font.draw(batch, String.format("%03d", City.limits.food), x - 80f + 19 + 32, 64f)
            font.draw(batch, String.format("%03d", City.limits.stone), x - 80f + 19 + 2 * 32, 64f)
            font.draw(batch, String.format("%04d", City.ressources.happiness), x - 80f + 13 + 3 * 32, 67f)
            font.draw(batch, String.format("%04d", City.ressources.research), x - 80f + 12 + 4 * 32, 67f)

            // Draws speed indicator
            font.draw(batch, "x${City.speed}", City.camera.position.x - 80 + Def.speedOffset, Def.speedY)
        }
    }


}