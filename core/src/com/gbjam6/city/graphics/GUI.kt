package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Def
import com.gbjam6.city.states.City

/**
 * Displays the different ressources on the top of the screen.
 */
class GUI(gbJam6: GBJam6) {

    private val citizens = gbJam6.manager.get("sprites/gui/Citizens.png", Texture::class.java)
    private val food = gbJam6.manager.get("sprites/gui/Food.png", Texture::class.java)
    private val happiness = gbJam6.manager.get("sprites/gui/Happiness.png", Texture::class.java)
    private val research = gbJam6.manager.get("sprites/gui/Research.png", Texture::class.java)
    private val stone = gbJam6.manager.get("sprites/gui/Stone.png", Texture::class.java)

    fun draw(batch: SpriteBatch, font: BitmapFont, x: Float) {
        // Draw the ressources count
        batch.draw(citizens, x - 80f, 56f)
        batch.draw(food, x - 80f + 32, 56f)
        batch.draw(stone, x - 80f + 2 * 32, 56f)
        batch.draw(happiness, x - 80f + 3 * 32, 56f)
        batch.draw(research, x - 80f + 4 * 32, 56f)

        // Draw the ressources count
        font.color = Def.color1
        font.draw(batch, String.format("%03d", City.ressources.citizens), x - 80f + 5, 71f)
        font.draw(batch, String.format("%03d", City.ressources.food), x - 80f + 5 + 32, 71f)
        font.draw(batch, String.format("%03d", City.ressources.stone), x - 80f + 5 + 2 * 32, 71f)
        font.draw(batch, String.format("%03d", City.limits.citizens), x - 80f + 19, 65f)
        font.draw(batch, String.format("%03d", City.limits.food), x - 80f + 19 + 32, 65f)
        font.draw(batch, String.format("%03d", City.limits.stone), x - 80f + 19 + 2 * 32, 65f)
        font.draw(batch, String.format("%04d", City.ressources.happiness), x - 80f + 13 + 3 * 32, 68f)
        font.draw(batch, String.format("%04d", City.ressources.research), x - 80f + 12 + 4 * 32, 68f)

        // Draw speed indicator
    }

}