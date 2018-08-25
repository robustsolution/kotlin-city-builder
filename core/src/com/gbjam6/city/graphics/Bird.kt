package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util

class Bird(gbJam6: GBJam6) {

    var spr1: Sprite
    var spr2: Sprite
    var x: Float
    var y: Float
    var frame: Int
    var direction: Int

    companion object {
        var stop: Int = 1
    }

    init {
        spr1 = Sprite(gbJam6.manager.get("sprites/bird1.png", Texture::class.java))
        spr2 = Sprite(gbJam6.manager.get("sprites/bird2.png", Texture::class.java))
        if (Math.random() < 0.5) {
            x = (Def.nChunks + 1) * 16f
            spr1.setFlip(true, false)
            direction = -1
        } else {
            x = -(Def.nChunks + 1) * 16f
            direction = 1
        }
        y = Util.getPixel(Math.random().toFloat() * 20 + 9)
        frame = 0
    }

    fun draw(batch: SpriteBatch) {
        val compensation = if (Util.inputFreeze == 1 && direction == stop) direction else 0
        if (frame % 16 < 8) {
            batch.draw(spr1, x + compensation, y)
        } else {
            batch.draw(spr2, x + compensation, y)
        }
    }

    fun update(speed: Int) {
        frame += speed
        x += direction * speed
    }

}