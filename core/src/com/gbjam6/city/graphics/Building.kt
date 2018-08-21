package com.gbjam6.city.graphics

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.LBuilding
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.general.Util
import com.gbjam6.city.logic.Citizen
import com.gbjam6.city.states.City
import kotlin.math.min

class Building(lBuilding: LBuilding, var x: Float, var y: Float, manager: AssetManager) {

    var life = Def.BUILD_LIFE_TIME
    val citizens = mutableListOf<Citizen>()
    val citizensToKill = mutableListOf<Citizen>()

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

    /**
     * Returns true if the building can be placed
     */
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

    fun canUse(): Boolean {
        return true
    }

    fun canRepair(): Boolean {
        return false
    }

    fun canUpgrade(): Boolean {
        return false
    }

    /**
     * Called when the player places the building.
     */
    fun onPlaced() {
        // Update stones count
        City.ressources.stone -= this.lBuilding.cost

        // Update limits
        when (lBuilding.name) {
            "FACTORY" -> City.limits.stone += 100
            "FARM" -> City.limits.food += 100
            "HOUSE" -> City.limits.citizens += 6
        }

        // Make sure limits don't go over 999
        City.limits.stone = min(City.limits.stone, 999)
        City.limits.food = min(City.limits.food, 999)
        City.limits.citizens = min(City.limits.citizens, 999)
    }

    /**
     * Returns the ressources producted by the building.
     */
    fun getProduction(): Ressources {
        return when (lBuilding.name) {
            "FACTORY" -> Ressources(stone = citizens.size, food = -citizens.size)
            "FARM" -> Ressources(food = citizens.size * 3)
            "HOUSE" -> Ressources(food = -citizens.size)
            "TAVERN" -> Ressources(happiness = citizens.size * 1, food = -citizens.size)
            "LABORATORY" -> Ressources(research = citizens.size * 3, food = -citizens.size)
            else -> Ressources()
        }
    }

    /**
     * Returns the description of the building.
     * It will be displayed in [Helper].
     */
    fun getDescription(): String {
        return "CITIZENS : ${citizens.size}/${lBuilding.capacity}"
    }

    /**
     * Called each tick. Make citizens and the building older.
     */
    fun older(ressources: Ressources, buildingsToDestroy: MutableList<Building>) {
        // Make citizens older
        citizens.map { it.older() }

        // Kill dead citizens
        for (citizen in citizensToKill) {
            citizens.remove(citizen)
            if (citizen.life == 0)
                ressources.citizens -= 1
        }
        citizensToKill.clear()

        // Make the building older
        life -= 1
        if (life <= 0) {
            buildingsToDestroy.add(this)
        }
    }
}