package com.gbjam6.city

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

data class Citizen(val name: String){

}

class Building(texture: Texture?, srcWidth: Int, srcHeight: Int, val x: Int, val y: Int) : Sprite(texture, srcWidth, srcHeight) {

    val citizens = mutableListOf<Citizen>()

    init {

    }
}