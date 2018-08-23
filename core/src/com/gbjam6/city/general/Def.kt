package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color
import com.gbjam6.city.logic.Ressources

enum class MenuType {
    CREATION, CATEGORY, BUILDING, CITIZENS, CONFIRM, IMPROVE, HYDRATE, ADD, REMOVE
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, OTHER
}

data class LBuilding(val type: BuildingType, val name: String, val capacity: Int, var door: Pair<Int, Int>, var s8: Pair<Int, Int>, var s16: Pair<Int, Int>, val cost: Int)

object Def {

    // GENERAL
    val startingRessources = Ressources(happiness = 400, stone = 300)

    // GAME DESIGN
    const val SPEED = 120
    var BIRTH_COST = 100
    var LIFE_TIME = 300
    const val DAMAGED_LIMIT = 30
    var BUILD_LIFE_TIME = 300
    val WELL_RANGE = 80


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
    const val helperY = 46f
    const val speedY = 54f
    const val speedOffset = 4f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("CITIZENS", "HAPPINESS", "FOOD", "RESEARCH", "STONE", "OTHER"),
            MenuType.BUILDING to arrayOf("CITIZENS", "REPAIR", "DESTROY"),
            MenuType.CONFIRM to arrayOf("YES", "NO"),
            MenuType.HYDRATE to arrayOf("ADD", "REMOVE", "RETURN")
    )

    // BUILDINGS
    val buildings = listOf(
            LBuilding(BuildingType.CITIZENS, "HOUSE", 6, Pair(34, 41), Pair(34, 41), Pair(24, 41), 100),
            LBuilding(BuildingType.HAPPINESS, "TAVERN", 2, Pair(13, 20), Pair(13, 26), Pair(13, 39), 100),
            LBuilding(BuildingType.FOOD, "FARM", 2, Pair(19, 39), Pair(19, 39), Pair(19, 39), 100),
            LBuilding(BuildingType.RESEARCH, "LABORATORY", 2, Pair(20, 28), Pair(20, 29), Pair(20, 38), 100),
            LBuilding(BuildingType.STONE, "FACTORY", 2, Pair(6, 19), Pair(6, 30), Pair(6, 22), 100),
            LBuilding(BuildingType.OTHER, "WELL", 0, Pair(0, 17), Pair(0, 17), Pair(0, 17), 100),
            LBuilding(BuildingType.STONE, "CRAFTMAN", 1, Pair(20, 36), Pair(20, 36), Pair(20, 36), 100)
    )
    val destroyedRessources = listOf(
            "HOUSE", "HOUSE+", "TAVERN", "TAVERN+", "FARM", "FARM+",
            "LABORATORY", "LABORATORY+", "FACTORY", "FACTORY+",
            "CRAFTMAN", "HOSPITAL", "SCHOOL", "WAREHOUSE"
    )
    val customMenus = mapOf(
            "WELL" to arrayOf("HYDRATE", "REPAIR", "DESTROY"),
            "HOUSE" to arrayOf("CITIZENS", "BIRTH", "UPGRADE", "REPAIR", "DESTROY")
    )

    // ACHIEVEMENTS
    val achievements = listOf(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

    // NAMES
    val names = listOf("Jean", "Pas Jean")

    // DESCRIPTIONS
    const val backupDesc = "MISSING :-c\nADD ME IN\nDEF.DESCRIPTIONS"
    val descriptions = mapOf(
            "CITIZENS" to "DESC OF\nCITIZENS",
            "HAPPINESS" to "DESC OF\nHAPP",
            "FOOD" to "DESC OF\nFOOD",
            "RESEARCH" to "DESC OF\nRESEARCH",
            "STONE" to "DESC OF\nSTONE",
            "RETURN" to "GO BACK",
            "REPAIR" to "OCULUS\nREPARO :>"
    )

    fun getDescription(name: String) = descriptions[name] ?: backupDesc

}