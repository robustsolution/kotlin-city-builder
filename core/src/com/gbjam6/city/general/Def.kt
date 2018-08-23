package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color
import com.gbjam6.city.logic.Ressources

enum class MenuType {
    CREATION, CATEGORY, BUILDING, CITIZENS, CONFIRM, IMPROVE, HYDRATE, ADD, REMOVE
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, OTHER
}

data class LBuilding(val type: BuildingType, val name: String, val capacity: Int, var door: Pair<Int, Int>, var s8: Pair<Int, Int>, var s16: Pair<Int, Int>, val cost: Int, var unlock: Boolean)

data class TreeUpgrade(val x: Int, val y: Int, val name: String, val cost: Int, val desc: String)

object Def {

    // GENERAL
    val startingRessources = Ressources(food = 200, happiness = 400, stone = 500)

    // GAME DESIGN
    const val SPEED = 120
    var BIRTH_COST = 100
    var LIFE_TIME = 300
    const val DAMAGED_LIMIT = 30
    var BUILD_LIFE_TIME = 300
    const val WELL_RANGE = 80

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
            LBuilding(BuildingType.CITIZENS, "HOUSE", 6, Pair(34, 41), Pair(34, 41), Pair(24, 41), 100,true),
            LBuilding(BuildingType.HAPPINESS, "TAVERN", 2, Pair(13, 20), Pair(13, 26), Pair(13, 39), 100,true),
            LBuilding(BuildingType.FOOD, "FARM", 2, Pair(19, 39), Pair(19, 39), Pair(19, 39), 100,true),
            LBuilding(BuildingType.RESEARCH, "LABORATORY", 2, Pair(20, 28), Pair(20, 29), Pair(20, 38), 100,true),
            LBuilding(BuildingType.STONE, "FACTORY", 2, Pair(6, 19), Pair(6, 30), Pair(6, 22), 100,true),
            LBuilding(BuildingType.OTHER, "WELL", 0, Pair(0, 17), Pair(0, 17), Pair(0, 17), 100,true),
            LBuilding(BuildingType.STONE, "CRAFTMAN", 1, Pair(20, 36), Pair(20, 36), Pair(20, 36), 100,false),
            LBuilding(BuildingType.FOOD,"WAREHOUSE", 1, Pair(24,51),Pair(24,51),Pair(24,51),200,false),
            LBuilding(BuildingType.HAPPINESS, "GARDEN",1, Pair(43,88),Pair(43,88),Pair(43,88),200,false),
            LBuilding(BuildingType.RESEARCH,"HOSPITAL",1, Pair(20,43), Pair(20,54),Pair(20,64),200,false),
            LBuilding(BuildingType.CITIZENS,"SCHOOL", 4, Pair(67,79),Pair(31,79),Pair(21,79),200,false)
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

    // TREE
    val xPos = Array(5) { 10 + 32 * it }
    val yPos1 = Array(3) { 6 - 32 * it }
    val yPos2 = Array(3) { 22 - 32 * it }
    val tree = listOf(
            TreeUpgrade(xPos[0], yPos1[0], "FACTORY+", 10, ""),
            TreeUpgrade(xPos[0], yPos1[1], "TAVERN+", 10, ""),
            TreeUpgrade(xPos[0], yPos1[2], "FARM+", 10, ""),
            TreeUpgrade(xPos[1], yPos2[0], "WELL", 10, "PURCHASE THE WELL.\nYOU WILL NOT REGRET IT.\nTHE WELL IS AWESOME ;-)"),
            TreeUpgrade(xPos[1], yPos2[1], "LABORATORY+", 10, ""),
            TreeUpgrade(xPos[1], yPos2[2], "HOUSE+", 10, ""),
            TreeUpgrade(xPos[2], yPos1[0], "CRAFTMAN", 10, ""),
            TreeUpgrade(xPos[2], yPos1[1], "SCHOOL", 10, ""),
            TreeUpgrade(xPos[2], yPos1[2], "WAREHOUSE", 10, ""),
            TreeUpgrade(xPos[3], yPos2[0], "TREE", 10, ""),
            TreeUpgrade(xPos[3], yPos2[1], "HOSPITAL", 10, ""),
            TreeUpgrade(xPos[3], yPos2[2], "???", 10, ""),
            TreeUpgrade(xPos[4], yPos1[0], "GARDEN", 10, ""),
            TreeUpgrade(xPos[4], yPos1[1], "GARDEN", 10, ""),
            TreeUpgrade(xPos[4], yPos1[2], "GARDEN", 10, "")
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