package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color
import com.gbjam6.city.states.City

enum class MenuType {
    CREATION, CATEGORY, BUILDING, CITIZENS, CONFIRM, IMPROVE
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, OTHER
}

data class LBuilding(val type: BuildingType, val name: String, val capacity: Int, var door: Pair<Int, Int>, var s8: Pair<Int, Int>, var s16: Pair<Int, Int>,val cost: Int)

data class Ressources(var citizens: Int = 0, var food: Int = 0, var stone: Int = 0, var happiness: Int = 0, var research: Int = 0){
    infix fun add(ressources: Ressources){
        this.stone += ressources.stone
        this.citizens += ressources.citizens
        this.food += ressources.food
        this.research += ressources.research
        this.happiness += ressources.happiness
    }
    infix  fun addLimit(ressources: Ressources){
        this add ressources
        if (this.food < 0){
            this.food = 0
        }
        if (this.food > City.limits.food && this.food-ressources.food <= City.limits.food){
            this.food = City.limits.food
        }
        if( this.food > City.limits.food && this.food-ressources.food > City.limits.food){
            this.food = this.food-ressources.food
        }
        if (this.stone > City.limits.stone && this.stone-ressources.stone <= City.limits.stone){
            this.stone = City.limits.stone
        }
        if( this.stone > City.limits.stone && this.stone-ressources.stone > City.limits.stone){
            this.stone = this.stone-ressources.stone
        }
        if( this.happiness > City.limits.happiness){
            this.happiness = City.limits.happiness
        }
        if( this.research > City.limits.research){
            this.research = City.limits.research
        }
    }
}

object Def {

    // GENERAL
    val startingRessources = Ressources(happiness = 400, stone = 300)
    val speed1 = 10

    // COLORS
    val color1 = Color.valueOf("000000")
    val color2 = Color.valueOf("545454")
    val color3 = Color.valueOf("A9A9A9")
    val color4 = Color.valueOf("FFFFFF")

    // SIZE
    val nChunks = 50
    val menuWidth = 72f
    val menuY = 52f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("CITIZENS", "HAPPINESS", "FOOD", "RESEARCH", "STONE", "OTHER"),
            MenuType.BUILDING to arrayOf("CITIZENS", "USE", "UPGRADE", "REPAIR", "DESTROY"),
            MenuType.CONFIRM to arrayOf("YES", "NO")
    )

    // BUILDINGS
    val buildings = listOf(
            LBuilding(BuildingType.CITIZENS, "HOUSE", 6, Pair(34, 41), Pair(34, 41), Pair(24, 41),100),
            LBuilding(BuildingType.HAPPINESS, "TAVERN", 2, Pair(13, 20), Pair(13, 26), Pair(13, 39),100),
            LBuilding(BuildingType.FOOD, "FARM", 2, Pair(19, 39), Pair(19, 39), Pair(19, 39),100),
            LBuilding(BuildingType.RESEARCH, "LABORATORY", 2, Pair(20, 28), Pair(20, 29), Pair(20, 38),100),
            LBuilding(BuildingType.STONE, "FACTORY", 2, Pair(6, 19), Pair(6, 30), Pair(6, 22),100),
            LBuilding(BuildingType.OTHER, "WELL", 0, Pair(0, 17), Pair(0, 17), Pair(0, 17),100)
    )
    val customMenus = mapOf<String, Array<String>>(
            "WELL" to arrayOf("CITIZENS", "REPAIR")
    )

    // ACHIEVEMENTS
    val achievements = listOf<Triple<String, String, Boolean>>(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

}