package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util

/**
 * Loading screen with a loading bar.
 */
class Load(private val gbJam6: GBJam6) : KtxScreen {
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private lateinit var font: BitmapFont
    //private lateinit var bg

    override fun show() {
        super.show()

        //bg = gbJam6.manager.get()
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)

        shapeRenderer.projectionMatrix = camera.combined

        // Fonts
        gbJam6.manager.load("fonts/little.fnt", BitmapFont::class.java)
        gbJam6.manager.load("fonts/littleDark.fnt", BitmapFont::class.java)
        gbJam6.manager.load("fonts/littleDisabled.fnt", BitmapFont::class.java)

        // Music
        gbJam6.titleMusic = gbJam6.player.loadXM(Gdx.files.internal("music/0-title-screen.xm").readBytes(), 0f)
        gbJam6.cityMusic1 = gbJam6.player.loadXM(Gdx.files.internal("music/1-early-game.xm").readBytes(), 0f)
        gbJam6.cityMusic2 = gbJam6.player.loadXM(Gdx.files.internal("music/2-getting-started.xm").readBytes(), 0f)
        gbJam6.cityMusic3 = gbJam6.player.loadXM(Gdx.files.internal("music/3-nice-city.xm").readBytes(), 0f)

        // Title screen
        gbJam6.manager.load("sprites/name.png", Texture::class.java)
        gbJam6.manager.load("sprites/titleScreen.png", Texture::class.java)

        // Pointers
        gbJam6.manager.load("sprites/pointerRight.png", Texture::class.java)
        gbJam6.manager.load("sprites/pointerUp.png", Texture::class.java)
        gbJam6.manager.load("sprites/pointerSmiley.png", Texture::class.java)
        gbJam6.manager.load("sprites/smallPointerRight.png", Texture::class.java)

        // Tiles
        gbJam6.manager.load("sprites/tiles-sheet.png", Texture::class.java)
        for (lBuilding in Def.buildings) {
            gbJam6.manager.load("sprites/buildings/${lBuilding.name}.png", Texture::class.java)
        }
        for (building in Def.destroyedRessources) {
            gbJam6.manager.load("sprites/buildings/destroyed/$building DESTROYED.png", Texture::class.java)
        }
        for (building in Def.upgradedBuilding) {
            gbJam6.manager.load("sprites/buildings/${building.name}0.png", Texture::class.java)
            gbJam6.manager.load("sprites/buildings/${building.name}1.png", Texture::class.java)
            gbJam6.manager.load("sprites/buildings/${building.name}2.png", Texture::class.java)
        }

        // GUI
        gbJam6.manager.load("sprites/gui/GUI.png", Texture::class.java)

        // TREE
        gbJam6.manager.load("sprites/tree/tree.png", Texture::class.java)
        gbJam6.manager.load("sprites/tree/cursor-sheet.png", Texture::class.java)
        gbJam6.manager.load("sprites/tree/unlocked.png", Texture::class.java)

        // SHADER
        gbJam6.manager.load("shaders/colorTable.png", Texture::class.java)

        // GFX
        gbJam6.manager.load("sfx/swipe.wav", Sound::class.java)
        gbJam6.manager.load("sfx/select.wav", Sound::class.java)
        gbJam6.manager.load("sfx/build.wav", Sound::class.java)
        gbJam6.manager.load("sfx/placeCitizen.wav", Sound::class.java)
        gbJam6.manager.load("sfx/die.wav", Sound::class.java)
        gbJam6.manager.load("sfx/destroyed.wav", Sound::class.java)
        gbJam6.manager.load("sfx/collapse.wav", Sound::class.java)
        gbJam6.manager.load("sfx/expand.wav", Sound::class.java)
        gbJam6.manager.load("sfx/noFood.wav", Sound::class.java)
        gbJam6.manager.load("sfx/disabled.wav", Sound::class.java)
        gbJam6.manager.load("sfx/b.wav", Sound::class.java)

    }

    override fun render(delta: Float) {

        if (gbJam6.manager.update()) {
            Util.createSounds(gbJam6)
            gbJam6.setMusic("BOOT")
            gbJam6.setScreen<TitleScreen>()
        }

        camera.update()

        // Clear screen
        Gdx.gl.glClearColor(Def.color2.r, Def.color2.g, Def.color2.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Draw the loading bar
        val progress = gbJam6.manager.progress
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Def.color4
        shapeRenderer.rect(-60f, -1f - 32f, progress * 120, 2f)
        shapeRenderer.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        font.draw(batch, "PUBLIC DOMAIN\nLICENSED BY NO ONE", -80f, -48f, 160f, 1, true)
        batch.end()

    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}