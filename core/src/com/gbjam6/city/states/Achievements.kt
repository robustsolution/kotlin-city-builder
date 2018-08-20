package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.general.Def.achievements
import com.gbjam6.city.GBJam6
import com.gbjam6.city.Input
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util
import ktx.app.KtxScreen

/**
 * Achievements screen.
 *
 * TODO: Achievements tiles
 * TODO: Moving cursor
 */
class Achievements(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var font: BitmapFont
    private var selected = 0

    override fun show() {
        super.show()
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
    }

    override fun render(delta: Float) {

        camera.update()
        processInputs()

        // Clear screen
        Gdx.gl.glClearColor(Def.color2.r, Def.color2.g, Def.color2.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Prepare for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Draw title
        font.draw(batch, "ACHIEVEMENTS", -80f, 64f, 160f, 1, true)

        // Draw achievements tiles
        font.draw(batch, "1   2   3   4   5\n\n6   7   8   9   10",
                -80f, 32f, 160f, 1, true)

        // Draw cursor

        // Draw description
        font.draw(batch,
                achievements[selected].first + "\n\n" + achievements[selected].second,
                -80f, -16f, 160f, 1, true)

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun left() {
        selected = (selected + achievements.size - 1) % achievements.size
    }

    override fun right() {
        selected = (selected + 1) % achievements.size
    }

    override fun b() {
        gbJam6.setScreen<TitleScreen>()
    }

}