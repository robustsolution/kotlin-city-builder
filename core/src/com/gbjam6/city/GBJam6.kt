package com.gbjam6.city

import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.gbjam6.city.states.*
import ktx.app.KtxGame

/**
 * Main class.
 */
class GBJam6() : KtxGame<Screen>() {

    val manager = AssetManager()

    override fun create() {

        // Add the different states
        addScreen(Load(this))
        addScreen(TitleScreen(this))
        addScreen(City(this))
        addScreen(Tutorial(this))
        addScreen(Achievements(this))

        // Start loading screen
        setScreen<Load>()

    }

}
