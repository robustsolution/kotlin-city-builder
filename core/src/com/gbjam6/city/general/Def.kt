package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color
import com.gbjam6.city.states.City
import kotlin.math.max
import kotlin.math.min

enum class MenuType {
    CREATION, CATEGORY, BUILDING, CITIZENS, CONFIRM, IMPROVE
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, OTHER
}

data class LBuilding(val type: BuildingType, val name: String, val capacity: Int, var door: Pair<Int, Int>, var s8: Pair<Int, Int>, var s16: Pair<Int, Int>, val cost: Int)

data class Ressources(var citizens: Int = 0, var food: Int = 0, var stone: Int = 0, var happiness: Int = 0, var research: Int = 0) {
    infix fun add(ressources: Ressources) {
        this.stone += ressources.stone
        this.citizens += ressources.citizens
        this.food += ressources.food
        this.research += ressources.research
        this.happiness += ressources.happiness
    }

    infix fun addLimit(ressources: Ressources) {
        this add ressources
        food = max(0, food)
        if (food > City.limits.food && food - ressources.food <= City.limits.food) {
            food = City.limits.food
        }
        if (food > City.limits.food && food - ressources.food > City.limits.food) {
            food -= ressources.food
        }
        if (stone > City.limits.stone && stone - ressources.stone <= City.limits.stone) {
            stone = City.limits.stone
        }
        if (stone > City.limits.stone && stone - ressources.stone > City.limits.stone) {
            stone -= ressources.stone
        }
        this.happiness = min(City.limits.happiness, happiness)
        this.research = min(this.research, City.limits.research)
    }
}

object Def {

    // GENERAL
    val startingRessources = Ressources(happiness = 400, stone = 300)
    const val speed1 = 10
    const val RESET: Int = 600

    // GAME DESIGN
    var BIRTH_COST = 100
    var LIFE_TIME = 300
    var BUILD_LIFE_TIME = 300

    // COLORS
    val color1: Color = Color.valueOf("000000")
    val color2: Color = Color.valueOf("545454")
    val color3: Color = Color.valueOf("A9A9A9")
    val color4: Color = Color.valueOf("FFFFFF")

    // SIZE
    const val nChunks = 50
    const val menuWidth = 72f
    const val menuY = 52f
    const val helperWidth = 64f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("CITIZENS", "HAPPINESS", "FOOD", "RESEARCH", "STONE", "OTHER"),
            MenuType.BUILDING to arrayOf("CITIZENS", "USE", "UPGRADE", "REPAIR", "DESTROY"),
            MenuType.CONFIRM to arrayOf("YES", "NO")
    )

    // BUILDINGS
    val buildings = listOf(
            LBuilding(BuildingType.CITIZENS, "HOUSE", 6, Pair(34, 41), Pair(34, 41), Pair(24, 41), 100),
            LBuilding(BuildingType.HAPPINESS, "TAVERN", 2, Pair(13, 20), Pair(13, 26), Pair(13, 39), 100),
            LBuilding(BuildingType.FOOD, "FARM", 2, Pair(19, 39), Pair(19, 39), Pair(19, 39), 100),
            LBuilding(BuildingType.RESEARCH, "LABORATORY", 2, Pair(20, 28), Pair(20, 29), Pair(20, 38), 100),
            LBuilding(BuildingType.STONE, "FACTORY", 2, Pair(6, 19), Pair(6, 30), Pair(6, 22), 100),
            LBuilding(BuildingType.OTHER, "WELL", 0, Pair(0, 17), Pair(0, 17), Pair(0, 17), 100)
    )
    val customMenus = mapOf(
            "WELL" to arrayOf("CITIZENS", "REPAIR"),
            "HOUSE" to arrayOf("CITIZENS", "BIRTH", "UPGRADE", "REPAIR", "DESTROY")
    )

    // ACHIEVEMENTS
    val achievements = listOf(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

    // NAMES
    val names = listOf("Jean", "Pas Jean")

}