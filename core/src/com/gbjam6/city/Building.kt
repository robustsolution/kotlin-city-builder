package com.gbjam6.city

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.LBuilding
import com.gbjam6.city.general.Util
import com.gbjam6.city.states.City
import java.lang.Math.abs

data class Citizen(val name: String)

class Building(lBuilding: LBuilding, var x: Float, var y: Float, manager: AssetManager) {

    val citizens = mutableListOf<Citizen>()

    private var sprite = Sprite(manager.get("sprites/buildings/${lBuilding.name}.png", Texture::class.java))
    val width = sprite.width
    val lBuilding = lBuilding.copy()
    var validPos: Boolean = true

    /**
     * Flip the building.
     */
    fun flip() {
        sprite.flip(true, false)
        lBuilding.door = lBuilding.door.copy(
                width.toInt() - lBuilding.door.second,
                width.toInt() - lBuilding.door.first)
        lBuilding.s8 = lBuilding.s8.copy(
                width.toInt() - lBuilding.s8.second,
                width.toInt() - lBuilding.s8.first)
        lBuilding.s16 = lBuilding.s16.copy(
                width.toInt() - lBuilding.s16.second,
                width.toInt() - lBuilding.s16.first)
    }

    /**
     * Draw the building.
     */
    fun draw(batch: SpriteBatch) {
        batch.draw(sprite, Util.getPixel(x), Util.getPixel(y))
    }

    fun isValid(): Boolean {

        // The door must be placed on a flat surface
        val door = lBuilding.door
        val chunk1 = City.hills.chunks[Math.floor((x + door.first) / 32.0).toInt() + Def.nChunks / 2]
        val chunk2 = City.hills.chunks[Math.floor((x + door.second) / 32.0).toInt() + Def.nChunks / 2]
        if (chunk1.slope != 0 || chunk2.slope != 0) {
            return false
        }

        // No collisions
        val collision = City.buildings.firstOrNull { if (x < it.x) it.x - x <= width else x - it.x <= it.width }
        if (collision != null) {
            return false
        }

        // Slopes limit

        // Left slope
        val leftChunkNb = Math.floor(x / 32.0).toInt() + Def.nChunks / 2
        if (City.hills.chunks[leftChunkNb].slope < 0) {
            // 1st case : the start of the building is in a negative slope
            val overflowLeft = 32 - (x + Def.nChunks * 32) % 32
            println("s8L : ${lBuilding.s8.first} ; s16L : ${lBuilding.s16.first} ; overflowL : $overflowLeft")
            when (City.hills.chunks[leftChunkNb].slope) {
                -8 -> if (overflowLeft > lBuilding.s8.first) return false
                -16 -> if (overflowLeft > lBuilding.s16.first) return false
            }
        } else if (lBuilding.door.first > 32 && City.hills.chunks[leftChunkNb + 1].slope < 0) {
            // 2nd case : the part of the building before the door is in a negative slope, not the beggining
            val overflowLeft = 64 - (x + Def.nChunks * 32) % 32
            println("s8L : ${lBuilding.s8.first} ; s16L : ${lBuilding.s16.first} ; overflowL : $overflowLeft")
            when (City.hills.chunks[leftChunkNb - 1].slope) {
                -8 -> if (overflowLeft > lBuilding.s8.first) return false
                -16 -> if (overflowLeft > lBuilding.s16.first) return false
            }
        }

        // Right slope
        val rightChunkNb = Math.floor((x + width) / 32.0).toInt() + Def.nChunks / 2
        if (City.hills.chunks[rightChunkNb].slope > 0) {
            // 1st case : the end of the building is in a positive slope
            val overflowRight = (x + width + Def.nChunks * 32) % 32
            println("s8R : ${lBuilding.s8.second} ; s16R : ${lBuilding.s16.second} ; overflowR : $overflowRight starting pixel : ${width - overflowRight}")
            when (City.hills.chunks[rightChunkNb].slope) {
                8 -> if (width - overflowRight < lBuilding.s8.second) return false
                16 -> if (width - overflowRight < lBuilding.s16.second) return false
            }
        } else if (rightChunkNb > 0 && width - lBuilding.door.second > 32 && City.hills.chunks[rightChunkNb - 1].slope > 0) {
            // 2nd case : the part of the building after the door is in a positive slope, not the end
            val overflowRight = 32 + (x + width + Def.nChunks * 32) % 32
            println("s8R : ${lBuilding.s8.second} ; s16R : ${lBuilding.s16.second} ; overflowR : $overflowRight starting pixel : ${width - overflowRight}")
            when (City.hills.chunks[rightChunkNb - 1].slope) {
                8 -> if (width - overflowRight < lBuilding.s8.second) return false
                16 -> if (width - overflowRight < lBuilding.s16.second) return false
            }
        }

        return true

    }

}