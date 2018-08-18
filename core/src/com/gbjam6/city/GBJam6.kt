package com.gbjam6.city

import com.badlogic.gdx.Screen
import ktx.app.KtxGame

class GBJam6() : KtxGame<Screen>() {

    override fun create() {

        // Add the different states
        addScreen(TitleScreen(this))

        setScreen<TitleScreen>()

    }

}
