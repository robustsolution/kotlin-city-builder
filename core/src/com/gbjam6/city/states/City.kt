package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.*
import com.gbjam6.city.Def.bgColor
import ktx.app.KtxScreen

/**
 * Main game class.
 */
class City(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var hills: Hills

    override fun show() {
        super.show()
        hills = Hills()
        println(hills.chunks)

        camera.position.x = 0f
        shapeRenderer.color = Def.darkColor

    }

    override fun render(delta: Float) {

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        processInputs()

        // Clear screen
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for ((i, chunk) in hills.chunks.withIndex()) {
            // Draw 7 chunks around camera x position
            val x = Math.floor(camera.position.x / 32.0)
            if (i - 25 in x - 3..x + 3) {
                shapeRenderer.polygon(
                        floatArrayOf(
                                -800f + 32 * i, -72f + chunk.height.toFloat(),
                                -800f + 32 * (i + 1), -72f + chunk.height.toFloat() + chunk.slope.toFloat(),
                                -800f + 32 * (i + 1), -72f,
                                -800f + 32 * i, -72f))
            }
        }
        shapeRenderer.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun left() {
        Util.inputFreeze = 1
        if (camera.position.x > -798f + 80f)
            camera.translate(-2f, 0f)
    }

    override fun right() {
        Util.inputFreeze = 1
        if (camera.position.x < 798f - 80f)
            camera.translate(2f, 0f)
    }

    override fun b() {
        gbJam6.setScreen<TitleScreen>()
    }

}