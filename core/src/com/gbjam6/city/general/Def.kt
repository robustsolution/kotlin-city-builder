package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color

enum class MenuType {
    CREATION, BUILDING, CITIZENS, CONFIRM, IMPROVE
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, WATER
}

data class LBuilding(val type: BuildingType, val name: String)

object Def {

    // COLORS
    val color1 = Color.valueOf("000000")
    val color2 = Color.valueOf("4D533C")
    val color3 = Color.valueOf("8B956D")
    val color4 = Color.valueOf("C3C2AE")

    // SIZE
    val nChunks = 50
    val menuWidth = 72f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("Citizen", "Happiness", "Food", "Research", "Stone", "Water"),
            MenuType.BUILDING to arrayOf("ELEM1", "ELEM2", "ELEM3"),
            MenuType.CONFIRM to arrayOf("YES", "NO")
    )

    // BUILDINGS
    val buildings = listOf(
            LBuilding(BuildingType.CITIZENS, "CITIZENS1"),
            LBuilding(BuildingType.HAPPINESS, "HAPPINESS1"),
            LBuilding(BuildingType.FOOD, "FOOD1"),
            LBuilding(BuildingType.RESEARCH, "RESEARCH1"),
            LBuilding(BuildingType.STONE, "STONE1"),
            LBuilding(BuildingType.WATER, "WATER1")
    )

    // ACHIEVEMENTS
    val achievements = listOf<Triple<String, String, Boolean>>(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

}