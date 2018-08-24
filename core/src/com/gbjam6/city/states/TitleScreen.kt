package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.glutils.ShaderProgram


/**
 * Title screen class.
 *
 * TODO: Animated background
 */
class TitleScreen(private val gbJam6: GBJam6) : KtxScreen, com.gbjam6.city.Input {

    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var font: BitmapFont
    private lateinit var titleName: Texture
    private lateinit var bg: Texture
    private lateinit var cursor: Texture
    private lateinit var colorTable: Texture

    companion object {
        // Successive logo y positions
        private val logoY = Array(128) { Util.getPixel(2 * Math.sin(it * Math.PI / 64).toFloat()) }
        // Successive cursor x positions
        private val cursorX = listOf(-37f - 8f, -33f - 8f, -40f - 8f)
    }

    private var frame = 0
    private var cursorPos = 0

    override fun show() {
        super.show()

        // Sets the shader
        gbJam6.updateShader()
        ShaderProgram.pedantic = false
        batch.shader = gbJam6.shader

        // Gets assets
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        titleName = gbJam6.manager.get("sprites/name.png", Texture::class.java)
        cursor = gbJam6.manager.get("sprites/pointerRight.png", Texture::class.java)
        bg = gbJam6.manager.get("sprites/titleScreen.png", Texture::class.java)

        // Resets
        cursorPos = 0
        frame = 0

    }

    override fun render(delta: Float) {

        frame = (frame + 1) % 128
        processInputs()

        camera.update()

        // Clears screen
        val clearC = Def.clearColors[gbJam6.colorPalette - 1]
        Gdx.gl.glClearColor(clearC.r, clearC.g, clearC.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        gbJam6.colorTable.bind(1)
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)

        // Prepares for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        gbJam6.shader.setUniformi("colorTable", 1)
        gbJam6.shader.setUniformf("paletteIndex", gbJam6.paletteIndex)

        // Draws the background
        batch.draw(bg, -80f, -72f)

        // Draws the logo
        batch.draw(titleName, -70f, 12f + logoY[frame])

        // Draws options
        font.draw(batch, "PLAY GAME", -80f, 0f, 160f, 1, false)
        font.draw(batch, "TUTORIAL", -80f, -16f, 160f, 1, false)
        font.draw(batch, "SOUND TEST", -80f, -32f, 160f, 1, false)

        // Draws the cursor
        batch.draw(cursor, cursorX[cursorPos], -7f - 16f * cursorPos)

        // Draws credits
        font.draw(batch, "2018 - A_Do, Le Art,\nMirionos, yopox", -80f, -49f, 160f, 1, true)

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun up() {
        cursorPos = (cursorPos + 2) % 3
    }

    override fun down() {
        cursorPos = (cursorPos + 1) % 3
    }

    override fun a() {
        when (cursorPos) {
            0 -> {
                gbJam6.setMusic("SMALL CITY")
                gbJam6.setScreen<City>()
            }
            1 -> gbJam6.setScreen<Tutorial>()
            2 -> gbJam6.setScreen<SoundTest>()
        }
    }

    override fun p() {
        gbJam6.colorPalette = (gbJam6.colorPalette + 1) % Def.PALETTE_SIZE
        if (gbJam6.colorPalette == 0) gbJam6.colorPalette++
        gbJam6.updateShader()
    }

}
