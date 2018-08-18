package com.gbjam6.city

import com.badlogic.gdx.Screen
import com.gbjam6.city.states.Achievements
import com.gbjam6.city.states.City
import com.gbjam6.city.states.TitleScreen
import com.gbjam6.city.states.Tutorial
import ktx.app.KtxGame

class GBJam6() : KtxGame<Screen>() {

    override fun create() {

        // Add the different states
        addScreen(TitleScreen(this))
        addScreen(City(this))
        addScreen(Tutorial(this))
        addScreen(Achievements(this))

        setScreen<TitleScreen>()

    }

}
