package com.gbjam6.city

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.gbjam6.city.general.SFX
import com.gbjam6.city.states.*
import com.tanjent.tanjentxm.Player
import ktx.app.KtxGame

/**
 * Main class.
 */
class GBJam6 : KtxGame<Screen>() {

    val manager = AssetManager()

    private lateinit var shaderVertIndexPalette: String
    private lateinit var shaderFragIndexPalette: String
    lateinit var colorTable: Texture
    var colorPalette = 1
    var paletteIndex = 0f
    lateinit var shader: ShaderProgram

    val player = Player(48000, Player.INTERPOLATION_MODE_NONE)
    var titleMusic: Int = 0
    var cityMusic1: Int = 0
    var cityMusic2: Int = 0
    var cityMusic3: Int = 0

    companion object {
        val sfxMap = mutableMapOf<SFX, Sound>()

        fun playSFX(sfx: SFX) {
            sfxMap[sfx]!!.play()
        }
    }

    override fun create() {

        shaderVertIndexPalette = Gdx.files.internal("shaders/indexpalette.vert").readString()
        shaderFragIndexPalette = Gdx.files.internal("shaders/indexpalette.frag").readString()

        // Add the different states
        addScreen(Preload(this))
        addScreen(Load(this))
        addScreen(TitleScreen(this))
        addScreen(City(this))
        addScreen(SoundTest(this))
        addScreen(GameOver(this))

        // Start loading screen
        setScreen<Preload>()

    }

    fun updateShader() {
        colorTable = manager.get("shaders/colorTable.png", Texture::class.java)
        paletteIndex = (colorPalette + 0.5f) / colorTable.height

        shader = ShaderProgram(shaderVertIndexPalette, shaderFragIndexPalette)
    }

    fun setMusic(music: String) {

        when (music) {
            "BOOT" -> player.play(titleMusic, true, true,
                    0f, 0f)
            "TITLE" -> player.play(titleMusic, true, true,
                    1f, 0.1f)
            "SMALL CITY" -> player.play(cityMusic1, true, true,
                    1f, 0.1f)
            "MEDIUM CITY" -> player.play(cityMusic2, true, true,
                    1f, 0.1f)
            "BIG CITY" -> player.play(cityMusic3, true, true,
                    1f, 0.1f)
        }
    }

}
