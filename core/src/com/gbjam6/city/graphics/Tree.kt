package com.gbjam6.city.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Def
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States

class Tree(gbJam6: GBJam6) {

    private val bg = gbJam6.manager.get("sprites/tree/tree.png", Texture::class.java)
    private val cursor = TextureRegion(gbJam6.manager.get("sprites/tree/cursor-sheet.png", Texture::class.java), 2, 2, 16, 16)
    private val cursor2 = TextureRegion(gbJam6.manager.get("sprites/tree/cursor-sheet.png", Texture::class.java), 20, 2, 16, 16)
    private val border = gbJam6.manager.get("sprites/tree/unlocked.png", Texture::class.java)
    private var x = 0
    private var y = 0
    var frame = 0

    fun draw(batch: SpriteBatch, font: BitmapFont) {
        if (City.state == States.TREE) {
            // Draws background
            val leftX = City.camera.position.x - 80
            batch.draw(bg, leftX, -72f)

            // Draws black border for unlocked perks
            for (unlocked in City.progress.tree) {
                val perk = Def.tree.first { it.name == unlocked }
                batch.draw(border, leftX + perk.x, perk.y - 12f)
            }

            // Draws cursor
            val trueY = -14f + if (x % 2 == 1) Def.yPos2[y] else Def.yPos1[y]
            batch.draw(if (frame < 30) cursor else cursor2, leftX + Def.xPos[x] - 2, trueY)

            // Draws the description
            val selected = Def.tree[3 * x + y]
            font.color = Def.color1
            font.draw(batch, selected.name+" : "+selected.cost+" Hapiness\n"+selected.desc, leftX + 4, 62f, 152f, 1, true)
        }
    }

    fun up() {
        y = (y + 2) % 3
    }

    fun down() {
        y = (y + 1) % 3
    }

    fun left() {
        x = (x + 4) % 5
    }

    fun right() {
        x = (x + 1) % 5
    }

    /**
     * Called when an item of the tree is selected.
     */
    fun select(): States {
        // Gets selected item
        val selected = Def.tree[3 * x + y]

        // Buys the item if it's not already purchases and if the player has enough research.
        if (selected.cost <= City.ressources.research && selected.name !in City.progress.tree) {
            City.ressources.research -= selected.cost
            City.progress.tree.add(selected.name)
        }

        return States.TREE
    }
}

