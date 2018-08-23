package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.states.City

class SpeedIndicator {

    var speed = 1

    fun draw(batch: SpriteBatch, font: BitmapFont) {
        font.color = Def.color1
        font.draw(batch, "SPEED x$speed", City.camera.position.x - 80 + Def.speedOffset, Def.speedY)
    }

}