package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.Def.bgColor
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen

/**
 * Tutorial screen.
 *
 * TODO: Tutorial pages
 */
class Tutorial(private val gbJam6: GBJam6) : KtxScreen {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    override fun show() {
        super.show()
    }

    override fun render(delta: Float) {

        camera.update()

        // Clear screen
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Prepare for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}