package com.gbjam6.city

import com.badlogic.gdx.graphics.Color

object Def {

    val bgColor = Color.valueOf("A7CBD5")
    val darkColor = Color.valueOf("362C36")

    val nChunks = 50

    val achievements = listOf<Triple<String, String, Boolean>>(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

}