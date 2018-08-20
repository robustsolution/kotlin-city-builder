package com.gbjam6.city

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.gbjam6.city.general.BuildingType
import com.gbjam6.city.general.LBuilding

data class Citizen(val name: String)

class Building(private val lBuilding: LBuilding, var x: Float, var y: Float, manager: AssetManager) {

    val citizens = mutableListOf<Citizen>()

    private val texture = manager.get("sprites/buildings/${lBuilding.name}.png", Texture::class.java)
    val width = texture.width

    fun draw(batch: SpriteBatch) {
        batch.draw(texture, x, y)
    }

}