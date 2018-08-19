package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color

enum class MenuType {
    CREATION, BUILDING, CITIZENS, CONFIRM, IMPROVE
}

object Def {

    // COLORS
    val bgColor = Color.valueOf("A7CBD5")
    val darkColor = Color.valueOf("362C36")

    // SIZE
    val nChunks = 50
    val menuWidth = 80f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("Citizen", "Happiness", "Food", "Research", "Stone", "Water"),
            MenuType.BUILDING to arrayOf("ELEM1", "ELEM2", "ELEM3"),
            MenuType.CONFIRM to arrayOf("YES", "NO")
    )

    // ACHIEVEMENTS
    val achievements = listOf<Triple<String, String, Boolean>>(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

}