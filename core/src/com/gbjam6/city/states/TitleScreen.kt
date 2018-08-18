package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import com.gbjam6.city.Util
import com.tanjent.tanjentxm.Player
import ktx.app.KtxScreen

class TitleScreen(val gbJam6: GBJam6) : KtxScreen {

    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private val bgColor = Color.valueOf("A7CBD5")
    private lateinit var font: BitmapFont
    private lateinit var titleName: Sprite
    private lateinit var dot: Sprite

    private val positions = Array(128) { Math.sin(it * Math.PI / 64).toFloat() }
    private val xPos = listOf(-37f - 8f, -33f - 8f, -46f - 8f)
    private var frame = 0
    private var select = 0
    private var input = 0

    override fun show() {
        super.show()

        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        titleName = Sprite(gbJam6.manager.get("sprites/name.png", Texture::class.java))
        dot = Sprite(gbJam6.manager.get("sprites/dot.png", Texture::class.java))

        //val myPlayer = Player(44100, Player.INTERPOLATION_MODE_NONE)
        //val moduleOne = myPlayer.loadXM(Gdx.files.internal("music/strayed.xm").readBytes(), 0f)
        //myPlayer.play(moduleOne, true, true, 0f, 0f)

    }

    override fun render(delta: Float) {

        frame = (frame + 1) % 128
        processInputs()

        camera.update()

        // Clear screen
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Prepare for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        batch.draw(titleName, -64f, 22f + Util.getPixel(2 * positions[frame]))

        font.draw(batch, "PLAY GAME", -80f, 6f, 160f, 1, false)
        font.draw(batch, "TUTORIAL", -80f, -10f, 160f, 1, false)
        font.draw(batch, "ACHIEVEMENTS", -80f, -26f, 160f, 1, false)

        font.draw(batch, "2018 - A_Do, Le Art,\nMirionos, yopox", -80f, -47f, 160f, 1, true)

        batch.draw(dot, xPos[select], -2f - 16f * select)

        batch.end()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    fun processInputs() {

        // If a button was just pressed, wait a moment
        if (input > 0) {
            input--
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP) || Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                select = (select + 2) % 3
                input = 16
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                select = (select + 1) % 3
                input = 16
            }
            if (Gdx.input.isKeyPressed(Input.Keys.O) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                when (select) {
                    0 -> gbJam6.setScreen<City>()
                    1 -> gbJam6.setScreen<Tutorial>()
                    2 -> gbJam6.setScreen<Achievements>()
                }
            }
        }

    }

}
