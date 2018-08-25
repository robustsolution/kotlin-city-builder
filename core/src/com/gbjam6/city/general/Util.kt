package com.gbjam6.city.general

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.gbjam6.city.GBJam6
import com.gbjam6.city.graphics.Building
import com.gbjam6.city.MenuManager
import com.gbjam6.city.Tutorial
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.logic.Citizen
import com.gbjam6.city.logic.Hills
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.states.City
import com.gbjam6.city.states.GameOver
import com.gbjam6.city.states.Progress
import com.gbjam6.city.states.States
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


fun <T> List<T>.random(): T = this[Random().nextInt(this.size)]

object Util {

    // INPUT
    var inputFreeze = 0
    var wasPressed = false

    fun getPixel(f: Float): Float = f.roundToInt().toFloat()

    fun getBuilding(): Building? {
        val x = City.camera.position.x
        return City.buildings.firstOrNull { it.x <= x && x < it.x + it.width }
    }

    /**
     * Returns true if at least one building can host one more citizen.
     */
    fun housingLeft(): Boolean {
        for (building in City.buildings) {
            if (building.citizens.size < building.lBuilding.capacity)
                return true
        }
        return false
    }

    fun tick(menuManager: MenuManager, gbJam6: GBJam6) {
        // println("tick")
        val ressources = Ressources()
        val buildingsToDestroy = mutableListOf<Building>()

        // Gets buildings' production and make them older
        for (building in City.buildings) {
            ressources add building.getProduction()
            building.older(ressources, buildingsToDestroy)
        }

        // Removes destroyed buildings
        for (building in buildingsToDestroy) {
            building.destroy(menuManager)
            GBJam6.playSFX(SFX.COLLAPSE)
        }
        buildingsToDestroy.clear()

        // Updates the ressources count
        City.ressources addLimit ressources

        // Updates the upgraded building animation
        City.buildings.map {
            if (it.upgrade in 0..1) {
                it.upgrade++; it.updateTexture()
            }
        }

        // No food
        if (City.ressources.food == 0) {
            GBJam6.playSFX(SFX.NO_FOOD)
            City.starvingtick++
        } else {
            City.starvingtick = 0
        }
        if (City.starvingtick >= Def.STARVING_KILL_TICK) {
            val buldings = City.buildings.filter { it.citizens.size > 0 }
            val citizens = mutableListOf<Citizen>()
            for (building in buldings)
                for (citizen in building.citizens)
                    citizens.add(citizen)
            if (citizens.size > 0) {
                val citizen = citizens.random()
                citizen.building.citizensToKill.add(citizen)
            }
            City.starvingtick = 0
        }

        // Game Over
        if (City.buildings.none { it.lBuilding.type == BuildingType.STONE }
                && City.ressources.stone < Def.buildings[4].cost
                || City.ressources.citizens == 0
                && City.ressources.happiness < City.progress.birthcost
                && City.ressources.food < Def.EXCHANGE_VALUE) {
            GameOver.text = "GAME OVER!\nPRESS A TO RETURN TO TITLE."
            gbJam6.setScreen<GameOver>()
        }
    }

    /**
     * Called when a building is placed.
     */
    fun placeBuilding(placingB: Building) {
        if (placingB.lBuilding.decoration) {
            City.decorations.add(placingB)
        } else {
            City.buildings.add(placingB)
        }
        placingB.onPlaced()
    }

    /**
     * Shows the helper
     */
    fun showIDLEHelper() {
        MenuManager.helper.visible = !MenuManager.helper.visible
    }

    /**
     * Updates the helper to show informations about the pointed building.
     */
    fun updateHelper(menus: MutableList<Menu>) {

        if (City.tutorial.active) {
            MenuManager.helper.visible = true
            MenuManager.helper.update("TUTORIAL", City.tutorial.progression[0].description)
        } else {

            if (City.state == States.MENU) {
                Util.updateMenuHelper(menus)
            } else {
                val building = getBuilding()
                if (building != null) {
                    // Displays informations about the building
                    MenuManager.helper.update(building.lBuilding.name, building.getDescription())
                } else {
                    // Indicates that the selected point is empty
                    when (City.state) {
                        States.PLACE_CITIZEN -> MenuManager.helper.update("EMPTY", "YOU CANNOT \nPLACE THE\nCITIZEN HERE!")
                        States.IDLE -> MenuManager.helper.update("EMPTY", "YOU CAN BUILD\nHERE!")
                        States.PLACE_DECORATION -> MenuManager.helper.update("EMPTY", "YOU CAN\nDECORATE\nHERE!")
                    }
                }
            }
        }
    }

    fun updateMenuHelper(menus: MutableList<Menu>) {

        // Gets the displayed menu
        val menu = menus.lastOrNull()

        menu?.let {
            val item = menu.items[menu.cursorPos]

            // Updates the helper
            when (menu.type) {
                MenuType.CITIZENS -> {
                    val building = getBuilding()!!
                    if (menu.cursorPos < building.citizens.size) {
                        MenuManager.helper.update(item, building.citizens.elementAt(menu.cursorPos).getDescription())
                    } else {
                        MenuManager.helper.update(item, Def.getDescription("RETURN"))
                    }
                }
                MenuType.EXPAND -> {
                    val nExp = Util.expandsMade()
                    when (menu.items[menu.cursorPos]) {
                        "EXPAND" -> {
                            var desc = ""
                            if (nExp >= Def.EXPAND_COST.size) {
                                desc = "YOU PURCHASED\nALL TERRAIN\nUPGRADES."
                            } else if (nExp >= Def.EXPAND_COST.size - 2 && "EXPAND" !in City.progress.tree) {
                                desc = "UNLOCK THE \nEXPAND RESEARCH \nTO PURCHASE \nMORE TERRAIN."
                            } else {
                                desc = "COST:\n${Def.EXPAND_COST[nExp]} HAPPINESS"
                            }
                            MenuManager.helper.update("EXPAND", desc)
                        }
                    }
                }
                MenuType.CONSTRUCTION -> {
                    val building = getBuilding()
                    when (item) {
                        "UPGRADE" -> {
                            var description = ""
                            if (building!!.canUpgrade()) {
                                description += "Cost :\n${building.lBuilding.upgradeCost} Stones"
                            } else {
                                description += "You need \n${building.lBuilding.upgradeCost} Stones \nand the\n${building.lBuilding.name}+\nResearch"
                            }
                            MenuManager.helper.update(item, description)
                        }

                        "REPAIR" -> MenuManager.helper.update(item, "Integrity :\n${building!!.life}/${City.progress.buildlife}\nRepair cost :\n${((1 - building.life / City.progress.buildlife.toFloat()) * building.lBuilding.cost + 1).toInt()}")
                        "DESTROY" -> MenuManager.helper.update(item, "Cost :\n${building!!.lBuilding.cost * Def.DESTROY_HAP_PCT} Hapiness\nGain :\n${building.lBuilding.cost * Def.DESTROY_STN_PCT} Stones")
                        "BIRTH" -> MenuManager.helper.update(item, "BIRTH A\nCITIZEN FOR\n${City.progress.birthcost} FOOD")
                        else -> MenuManager.helper.update(item, Def.getDescription(item))

                    }
                }
                MenuType.ADD -> {
                    val building = getBuilding()!!
                    if (menu.cursorPos < building.citizensInReach!!.size) {
                        MenuManager.helper.update(item, building.citizensInReach!!.elementAt(menu.cursorPos).getDescription())
                    } else {
                        MenuManager.helper.update(item, Def.getDescription("RETURN"))
                    }
                }
                MenuType.REMOVE -> {
                    val building = getBuilding()!!
                    if (menu.cursorPos < building.wateredCitizens!!.size) {
                        MenuManager.helper.update(item, building.wateredCitizens!!.elementAt(menu.cursorPos).getDescription())
                    } else {
                        MenuManager.helper.update(item, Def.getDescription("RETURN"))
                    }
                }
                else -> MenuManager.helper.update(item, Def.getDescription(item))
            }
        }
    }

    /**
     * Returns a [width] * [height] rectangle [Texture] with the desired [color].
     */
    fun generateRectangle(width: Int, height: Int, color: Color): Texture {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fillRectangle(0, 0, width, height)
        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    /**
     * Returns the number of terrain upgrades purchased.
     */
    fun expandsMade(): Int {
        val (p1, p2) = City.progress.limits
        val (s1, s2) = Def.STARTING_LIMITS
        return abs(p2 - p1 + s1 - s2) / 160
    }

    /**
     * Returns true if the requirements to unlock the research are met.
     */
    fun canUnlock(perk: String): Boolean {
        val requirements = Def.treeRequirements[perk] ?: arrayOf()
        var unlockable = true
        for (requirement in requirements) {
            unlockable = unlockable && requirement in City.progress.tree
        }
        return unlockable
    }

    fun createSounds(gbJam6: GBJam6) {
        GBJam6.sfxMap[SFX.BUILD] = gbJam6.manager.get("sfx/build.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.SWIPE] = gbJam6.manager.get("sfx/swipe.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.COLLAPSE] = gbJam6.manager.get("sfx/collapse.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.DESTROYED] = gbJam6.manager.get("sfx/destroyed.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.DIE] = gbJam6.manager.get("sfx/die.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.EXPAND] = gbJam6.manager.get("sfx/expand.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.NO_FOOD] = gbJam6.manager.get("sfx/noFood.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.PLACE_CITIZEN] = gbJam6.manager.get("sfx/placeCitizen.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.SELECT] = gbJam6.manager.get("sfx/select.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.DISABLED] = gbJam6.manager.get("sfx/disabled.wav", Sound::class.java)
        GBJam6.sfxMap[SFX.RETURN] = gbJam6.manager.get("sfx/b.wav", Sound::class.java)
    }

    /**
     * Updates the pointer according to the slope.
     */
    fun updatePointer(pointer: Sprite, pointerSmiley: Sprite) {
        pointer.x = City.camera.position.x - 4

        // Computes difference between camera x and the current chunk x
        val n = Math.floor(City.camera.position.x / 32.0).toInt() + Def.nChunks / 2
        val chunk = City.hills.chunks[n]
        val diff = City.camera.position.x - (n - Def.nChunks / 2) * 32

        // Sets height to follow the slopes
        pointer.y = Util.getPixel(-87f + chunk.height + diff * chunk.slope / 32)
        pointerSmiley.x = pointer.x
        pointerSmiley.y = pointer.y + 1
    }


    fun reset() {
        City.hills = Hills()
        City.state = States.IDLE
        City.buildings = mutableListOf()
        City.decorations = mutableListOf()
        City.ressources = Def.startingRessources.copy()
        City.limits = Ressources(happiness = 9999, research = 9999)
        City.progress = Progress(mutableListOf(), birthcost = Def.BIRTH_COST, lifetime = Def.LIFE_TIME, buildlife = Def.BUILD_LIFE_TIME)
        City.starvingtick = 0
        City.speed = 1
        City.tutorial = Tutorial()
        City.camera.position.x = 0f
    }

    fun getVolume(sfx: SFX): Float = when (sfx) {
        SFX.COLLAPSE, SFX.DIE, SFX.DESTROYED -> 1f
        else -> 0.25f
    }
}