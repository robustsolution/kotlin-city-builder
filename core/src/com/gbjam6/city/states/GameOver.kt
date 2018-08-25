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
class GameOver(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var bigFont: BitmapFont
    private lateinit var font: BitmapFont
    private var darkBg = Util.generateRectangle(160, 144, Def.color1)

    companion object {
        var text = ""
    }

    override fun show() {
        super.show()
        bigFont = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        font = gbJam6.manager.get("fonts/little.fnt", BitmapFont::class.java)

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
        bigFont.draw(batch, "--------\nGAME OVER\n--------", -80f, 56f, 160f, 1, true)


        font.draw(batch, text, -80f, 8f, 160f, 1, true)

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun b() {
        gbJam6.setMusic("TITLE")
        gbJam6.setScreen<TitleScreen>()
    }

    override fun a() {
        b()
    }

}