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
    private lateinit var cursor: Texture

    companion object {
        // Successive logo y positions
        private val logoY = Array(128) { Util.getPixel(2 * Math.sin(it * Math.PI / 64).toFloat()) }
        // Successive cursor x positions
        private val cursorX = listOf(-37f - 8f, -33f - 8f, -46f - 8f)
    }

    private var frame = 0
    private var cursorPos = 0

    override fun show() {
        super.show()

        // Getting assets
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        titleName = gbJam6.manager.get("sprites/name.png", Texture::class.java)
        cursor = gbJam6.manager.get("sprites/pointerRight.png", Texture::class.java)

        // Reset
        cursorPos = 0
        frame = 0

        //val myPlayer = Player(44100, Player.INTERPOLATION_MODE_NONE)
        //val moduleOne = myPlayer.loadXM(Gdx.files.internal("music/strayed.xm").readBytes(), 0f)
        //myPlayer.play(moduleOne, true, true, 0f, 0f)

    }

    override fun render(delta: Float) {

        frame = (frame + 1) % 128
        processInputs()

        camera.update()

        // Clear screen
        Gdx.gl.glClearColor(Def.color2.r, Def.color2.g, Def.color2.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Prepare for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Draw the logo
        batch.draw(titleName, -64f, 22f + logoY[frame])

        // Draw options
        font.draw(batch, "PLAY GAME", -80f, 6f, 160f, 1, false)
        font.draw(batch, "TUTORIAL", -80f, -10f, 160f, 1, false)
        font.draw(batch, "ACHIEVEMENTS", -80f, -26f, 160f, 1, false)

        // Draw the cursor
        batch.draw(cursor, cursorX[cursorPos], -2f - 16f * cursorPos)

        // Draw credits
        font.draw(batch, "2018 - A_Do, Le Art,\nMirionos, yopox", -80f, -47f, 160f, 1, true)

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
            0 -> gbJam6.setScreen<City>()
            1 -> gbJam6.setScreen<Tutorial>()
            2 -> gbJam6.setScreen<Achievements>()
        }
    }

}
