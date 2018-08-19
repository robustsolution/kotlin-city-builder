package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
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
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var hills: Hills
    private lateinit var hillSprites: Array<Sprite>
    private lateinit var pointer: Sprite

    override fun show() {
        super.show()
        hills = Hills()

        camera.position.x = 0f

        pointer = Sprite(gbJam6.manager.get("sprites/pointer.png", Texture::class.java))
        pointer.x = -4f
        pointer.y = -69f

        val ttext = gbJam6.manager.get("sprites/tiles-sheet.png", Texture::class.java)
        hillSprites = arrayOf(
                Sprite(ttext, 1 * 2 + 0, 2, 32, 32),
                Sprite(ttext, 2 * 2 + 32, 2, 32, 32),
                Sprite(ttext, 3 * 2 + 64, 2, 32, 32),
                Sprite(ttext, 4 * 2 + 96, 2, 32, 32),
                Sprite(ttext, 3 * 2 + 64, 2, 32, 32),
                Sprite(ttext, 4 * 2 + 96, 2, 32, 32)
        )
        hillSprites[4].flip(true, false)
        hillSprites[5].flip(true, false)

        updatePointer()

    }

    override fun render(delta: Float) {

        camera.update()
        batch.projectionMatrix = camera.combined

        // Clear screen
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        for ((i, chunk) in hills.chunks.withIndex()) {
            // Draw 7 chunks around camera x position
            val x = Math.floor(camera.position.x / 32.0)
            if (i - 25 in x - 3..x + 3) {

                val chunkX = -800f + 32f * i
                val chunkY = -88f + chunk.height.toFloat()

                batch.draw(hillSprites[0], chunkX, chunkY - 32)
                batch.draw(hillSprites[0], chunkX, chunkY - 64)

                when (chunk.slope) {
                    0 -> batch.draw(hillSprites[1], chunkX, chunkY)
                    8 -> batch.draw(hillSprites[2], chunkX, chunkY)
                    16 -> batch.draw(hillSprites[3], chunkX, chunkY)
                    -8 -> batch.draw(hillSprites[4], chunkX, chunkY - 8)
                    -16 -> batch.draw(hillSprites[5], chunkX, chunkY - 16)
                }

            }
        }

        pointer.draw(batch)
        batch.end()

        processInputs()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    private fun updatePointer() {
        pointer.x = camera.position.x - 4

        // Compute difference between camera x and the current chunk x
        val n = Math.floor(camera.position.x / 32.0).toInt() + 25
        val chunk = hills.chunks[n]
        val diff = camera.position.x - (n - 25) * 32

        // Set height to follow the slopes
        pointer.y = Util.getPixel(-88f + chunk.height + diff * chunk.slope / 32)
    }

    override fun left() {
        Util.inputFreeze = 1
        if (camera.position.x > -798f + 80f) {
            camera.translate(-2f, 0f)
            updatePointer()
        }
    }

    override fun right() {
        Util.inputFreeze = 1
        if (camera.position.x < 798f - 80f) {
            camera.translate(2f, 0f)
            updatePointer()
        }
    }

    override fun b() {
        gbJam6.setScreen<TitleScreen>()
    }

}