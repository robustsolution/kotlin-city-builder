package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import com.gbjam6.city.Input
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util
import ktx.app.KtxScreen

/**
 * Lets you listen to the awesome OST by LeArtRemix.
 */
class SoundTest(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var bigFont: BitmapFont
    private lateinit var font: BitmapFont
    private lateinit var fontDisabled: BitmapFont
    private var selected = 0
    private var songs = arrayOf("TITLE SCREEN", "CITY 1", "CITY 2", "CITY 3")
    private var darkBg = Util.generateRectangle(160, 144, Def.color1)

    override fun show() {
        super.show()
        bigFont = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        font = gbJam6.manager.get("fonts/little.fnt", BitmapFont::class.java)
        fontDisabled = gbJam6.manager.get("fonts/littleDisabled.fnt", BitmapFont::class.java)

        // Sets the shader
        ShaderProgram.pedantic = false
        batch.shader = gbJam6.shader
    }

    override fun render(delta: Float) {

        camera.update()
        processInputs()

        // Clear screen
        val clearC = Def.clearColors[gbJam6.colorPalette - 1]
        Gdx.gl.glClearColor(clearC.r, clearC.g, clearC.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        gbJam6.colorTable.bind(1)
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)

        // Prepare for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        gbJam6.shader.setUniformi("colorTable", 1)
        gbJam6.shader.setUniformf("paletteIndex", gbJam6.paletteIndex)

        batch.draw(darkBg, -80f, -72f)

        // Draw title
        bigFont.draw(batch, "SOUND TEST\nChange song with UP & DOWN.", -80f, 56f, 160f, 1, true)

        // Draw song names
        for (i in 0 until songs.size) {
            if (i == selected) {
                font.draw(batch, songs[i], -80f, -10f * i, 160f, 1, true)
            } else {
                fontDisabled.draw(batch, songs[i], -80f, -10f * i, 160f, 1, true)
            }
        }

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    private fun playSong() {
        when (selected) {
            0 -> gbJam6.setMusic("TITLE")
            1 -> gbJam6.setMusic("SMALL CITY")
            2 -> gbJam6.setMusic("MEDIUM CITY")
            3 -> gbJam6.setMusic("BIG CITY")
        }
    }

    override fun up() {
        Util.inputFreeze = 60
        selected = (selected + 5) % 4
        playSong()
    }

    override fun down() {
        Util.inputFreeze = 60
        selected = (selected + 1) % 4
        playSong()
    }

    override fun b() {
        gbJam6.setScreen<TitleScreen>()
    }

    override fun p() {
        gbJam6.colorPalette = (gbJam6.colorPalette + 1) % Def.PALETTE_SIZE
        if (gbJam6.colorPalette == 0) gbJam6.colorPalette++
        gbJam6.updateShader()
    }

}