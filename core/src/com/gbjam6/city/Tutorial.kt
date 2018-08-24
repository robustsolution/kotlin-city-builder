package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.Sprite
import com.gbjam6.city.general.SFX
import com.gbjam6.city.general.Util
import com.gbjam6.city.graphics.Menu
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.states.City
import com.gbjam6.city.states.States

data class Step(val action: ACTION, val description: String, val ticks: Int = 0,
                val menuItem: String = "", val building: String = "", val ressource: Ressources = Ressources())

enum class ACTION {
    CLICK, WAIT, PLACE, OPEN_MENU, SELECT, SELECT_EMPTY, SELECT_BUILDING, SELECT_CITIZEN, GIVE
}

class Tutorial {

    private val steps = arrayOf(
            Step(ACTION.CLICK, "Welcome!\n\nPress A."),
            Step(ACTION.CLICK, "To start,\nwe are going\nto build\na house."),
            Step(ACTION.OPEN_MENU, "Press A\nto open the\nbuilding\nmenu."),
            Step(ACTION.CLICK, "Here are the\ndifferent\ncategories."),
            Step(ACTION.SELECT, "Select CITIZENS.", menuItem = "CITIZENS"),
            Step(ACTION.CLICK, "Here you can\nsee the\nbuildings\nconcerning\ncitizens."),
            Step(ACTION.SELECT, "Let's build the\nHOUSE.", menuItem = "HOUSE"),
            Step(ACTION.PLACE, "Select a\ncool spot\nfor our house!"),
            Step(ACTION.CLICK, "Great!"),
            Step(ACTION.CLICK, "Houses are used\nto add citizens."),
            Step(ACTION.OPEN_MENU, "Press A to\nopen the house\nmenu."),
            Step(ACTION.SELECT, "Let's add\na citizen.", menuItem = "BIRTH"),
            Step(ACTION.PLACE, "You can choose\nwhere to place\nit.\nPress A.", building = "HOUSE"),
            Step(ACTION.CLICK, "Citizens\nconsume 1\nfood per\nmonth."),
            Step(ACTION.WAIT, "Look at the\nGUI!", ticks = 4),
            Step(ACTION.CLICK, "If you don't\nhave any food,\none random\ncitizen will\ndie each\n10 months."),
            Step(ACTION.CLICK, "Let's build\na Farm\nto produce\nsome food."),
            Step(ACTION.SELECT_EMPTY, "Select an\nempty spot."),
            Step(ACTION.SELECT, "Select the\nfood category.", menuItem = "FOOD"),
            Step(ACTION.SELECT, "Select the\nfarm.", menuItem = "FARM"),
            Step(ACTION.PLACE, "Select a\ncool spot\nfor the farm :)"),
            Step(ACTION.CLICK, "Each building\nproduces\nressources\ndepending on\nits surroun-\ndings and the\nnumber of\ncitizens\ninside."),
            Step(ACTION.SELECT_BUILDING, "Select the\nhouse.", building = "HOUSE"),
            Step(ACTION.SELECT, "Select\ncitizens.", menuItem = "CITIZENS"),
            Step(ACTION.SELECT_CITIZEN, "Select\nthe citizen."),
            Step(ACTION.PLACE, "Place it into\nthe farm.", building = "FARM"),
            Step(ACTION.CLICK, "Next step:\nlet's build a\nfactory to\nproduce some\nstones."),
            Step(ACTION.SELECT_EMPTY, "Select an\nempty spot."),
            Step(ACTION.SELECT, "Select\nstone.", menuItem = "STONE"),
            Step(ACTION.SELECT, "Select\nthe factory.", menuItem = "FACTORY"),
            Step(ACTION.PLACE, "Select a\nwise spot\nfor the factory!"),
            Step(ACTION.CLICK, "Now we will\nput a citizen\nin this factory."),
            Step(ACTION.SELECT_BUILDING, "Select the\nhouse.", building = "HOUSE"),
            Step(ACTION.SELECT, "Select\nbirth.", menuItem = "BIRTH"),
            Step(ACTION.PLACE, "Select the\nfactory.", building = "FACTORY"),
            Step(ACTION.WAIT, "They started\nproducing stones!", ticks = 4),
            Step(ACTION.CLICK, "Here's some\nextra ;-)."),
            Step(ACTION.GIVE, "", ressource = Ressources(stone = 100)),
            Step(ACTION.CLICK, "Now you have\nenough stone\nto build\nanother\nbuilding."),
            Step(ACTION.SELECT_EMPTY, "Select an\nempty spot."),
            Step(ACTION.SELECT, "Select\nhappiness.", menuItem = "HAPPINESS"),
            Step(ACTION.SELECT, "Select\nthe tavern.", menuItem = "TAVERN"),
            Step(ACTION.PLACE, "Is there\nenough space?"),
            Step(ACTION.SELECT_BUILDING, "Let's put a\ncitizen inside!", building = "HOUSE"),
            Step(ACTION.SELECT, "Like this :)", menuItem = "BIRTH"),
            Step(ACTION.SELECT_BUILDING, "Where is the\ntavern already?", building = "TAVERN"),
            Step(ACTION.SELECT_EMPTY, "Let's build\nsomething\nto gain research\npoints."),
            Step(ACTION.SELECT, "Which one is\nit?", menuItem = "RESEARCH"),
            Step(ACTION.SELECT, "Found it!", menuItem = "LABORATORY"),
            Step(ACTION.PLACE, "I think\nthis one\nlooks pretty\nrad :p")
    )
    var active = false
    var progression = steps.toMutableList()
    var nTicks = 0

    fun reset() {
        progression = steps.toMutableList()
        active = true
        nTicks = 0
    }

    fun progress() {
        if (progression.size > 1) {
            progression.removeAt(0)
        }
    }

    fun a(menuManager: MenuManager, pointer: Sprite) {
        when (City.tutorial.progression[0].action) {
            ACTION.CLICK -> City.tutorial.progress()
            ACTION.OPEN_MENU -> {
                GBJam6.playSFX(SFX.SELECT)
                City.state = menuManager.open()
                progress()
                menuManager.menus.last().changeValidity()
            }
            ACTION.SELECT, ACTION.SELECT_CITIZEN -> {
                if (City.tutorial.progression[0].action == ACTION.SELECT_CITIZEN ||
                        menuManager.menus.last().items[menuManager.menus.last().cursorPos] == progression[0].menuItem) {
                    GBJam6.playSFX(SFX.SELECT)
                    City.state = menuManager.select(pointer.y)
                    progress()
                    menuManager.menus.last().changeValidity()
                }
            }
            ACTION.PLACE -> {
                val possible =
                        menuManager.placingB != null && menuManager.placingB!!.isValid() ||
                                menuManager.placingC != null && Util.getBuilding() != null && Util.getBuilding()!!.lBuilding.name == progression[0].building
                if (possible) {
                    City.state = menuManager.select(pointer.y)
                    progress()
                }
            }
            ACTION.SELECT_EMPTY -> {
                if (Util.getBuilding() == null) {
                    GBJam6.playSFX(SFX.SELECT)
                    City.state = menuManager.open()
                    progress()
                    menuManager.menus.last().changeValidity()
                }
            }
            ACTION.SELECT_BUILDING -> {
                if (Util.getBuilding() != null && Util.getBuilding()!!.lBuilding.name == progression[0].building) {
                    GBJam6.playSFX(SFX.SELECT)
                    City.state = menuManager.open()
                    progress()
                    menuManager.menus.last().changeValidity()
                }
            }
        }

        when (progression[0].action) {
            ACTION.SELECT -> menuManager.menus.last().changeValidity()
            ACTION.WAIT -> {
                nTicks = City.tutorial.progression[0].ticks
                City.speed = 1
            }
            ACTION.GIVE -> {
                City.ressources addLimit progression[0].ressource
                progress()
            }
        }
    }

    fun left(pointer: Sprite, pointerSmiley: Sprite, menuManager: MenuManager) {
        val a = progression[0].action
        if (a in arrayOf(ACTION.PLACE, ACTION.SELECT_EMPTY, ACTION.SELECT_BUILDING)) {
            when (City.state) {
                States.IDLE, States.PLACE_CITIZEN, States.PLACE_BUILDING, States.PLACE_DECORATION -> {
                    // Moves the camera
                    Util.inputFreeze = 1
                    if (City.camera.position.x > City.progress.limits.first - 16) {
                        if (Util.wasPressed) {
                            City.camera.translate(-3f, 0f)
                        } else {
                            // Special slow first frame (for precise movements)
                            Util.inputFreeze = 4
                            City.camera.translate(-1f, 0f)
                        }
                        Util.updatePointer(pointer, pointerSmiley)
                    }
                    menuManager.updateBuilding(pointer.y)
                }
            }
        }
    }

    fun right(pointer: Sprite, pointerSmiley: Sprite, menuManager: MenuManager) {
        val a = progression[0].action
        if (a in arrayOf(ACTION.PLACE, ACTION.SELECT_EMPTY, ACTION.SELECT_BUILDING)) {
            when (City.state) {
                States.IDLE, States.PLACE_CITIZEN, States.PLACE_BUILDING, States.PLACE_DECORATION -> {
                    // Moves the camera
                    Util.inputFreeze = 1
                    if (City.camera.position.x < City.progress.limits.second + 16) {
                        if (Util.wasPressed) {
                            City.camera.translate(3f, 0f)
                        } else {
                            // Special slow first frame (for precise movements)
                            Util.inputFreeze = 4
                            City.camera.translate(1f, 0f)
                        }
                        Util.updatePointer(pointer, pointerSmiley)
                    }
                    menuManager.updateBuilding(pointer.y)
                }
            }
        }
    }

    fun down(menuManager: MenuManager) {
        if (City.tutorial.progression[0].action == ACTION.SELECT) {
            Util.inputFreeze = 8
            menuManager.moveCursor(1)
        }
    }

    fun up(menuManager: MenuManager) {
        if (City.tutorial.progression[0].action == ACTION.SELECT) {
            Util.inputFreeze = 8
            menuManager.moveCursor(-1)
        }
    }

    fun start() {

    }

    fun select() {

    }

    fun tick() {
        if (active) {
            nTicks--
            if (nTicks == 0) {
                progress()
                City.speed = 0
            }
        }
    }

    fun changeValidity(menu: Menu) {
        menu.activated = Array(menu.activated.size) { i -> menu.items[i] == City.tutorial.progression[0].menuItem }
        if (City.tutorial.progression[0].action == ACTION.SELECT_CITIZEN) {
            menu.activated = Array(menu.activated.size) { it < 1 }
        }
    }

}