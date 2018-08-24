package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util

/**
 * Loading screen with a loading bar.
 */
class Preload(private val gbJam6: GBJam6) : KtxScreen {

    override fun show() {
        super.show()

        // Fonts
        gbJam6.manager.load("fonts/skullboy.fnt", BitmapFont::class.java)
        //manager.load("sprites/loadingScreen.png", Texture::class.java)

    }

    override fun render(delta: Float) {

        if (gbJam6.manager.update()) {
            gbJam6.setScreen<Load>()
        }

    }

}