package com.gbjam6.city

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShaderProgram
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
    var colorPalette = 0
    var paletteIndex = 0f
    lateinit var shader: ShaderProgram

    val player = Player(44100, Player.INTERPOLATION_MODE_CUBIC)
    var titleMusic: Int = 0
    var cityMusic1: Int = 0

    override fun create() {

        shaderVertIndexPalette = Gdx.files.internal("shaders/indexpalette.vert").readString()
        shaderFragIndexPalette = Gdx.files.internal("shaders/indexpalette.frag").readString()

        // Add the different states
        addScreen(Load(this))
        addScreen(TitleScreen(this))
        addScreen(City(this))
        addScreen(Tutorial(this))
        addScreen(Achievements(this))

        // Start loading screen
        setScreen<Load>()

    }

    fun updateShader() {
        colorTable = manager.get("shaders/colorTable.png", Texture::class.java)
        paletteIndex = (colorPalette + 0.5f) / colorTable.height

        shader = ShaderProgram(shaderVertIndexPalette, shaderFragIndexPalette)
    }

}
