package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gbjam6.city.general.Def

/**
 * Loading screen with a loading bar.
 */
class Load(private val gbJam6: GBJam6) : KtxScreen {
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    override fun show() {
        super.show()

        shapeRenderer.projectionMatrix = camera.combined

        // Fonts
        gbJam6.manager.load("fonts/skullboy.fnt", BitmapFont::class.java)
        gbJam6.manager.load("fonts/little.fnt", BitmapFont::class.java)
        gbJam6.manager.load("fonts/littleDark.fnt", BitmapFont::class.java)
        gbJam6.manager.load("fonts/littleDisabled.fnt", BitmapFont::class.java)

        // Music
        gbJam6.titleMusic = gbJam6.player.loadXM(Gdx.files.internal("music/title.xm").readBytes(), 0f)
        gbJam6.cityMusic1 = gbJam6.player.loadXM(Gdx.files.internal("music/city1.xm").readBytes(), 0f)
        gbJam6.cityMusic2 = gbJam6.player.loadXM(Gdx.files.internal("music/city2.xm").readBytes(), 0f)
        gbJam6.cityMusic3 = gbJam6.player.loadXM(Gdx.files.internal("music/city3.xm").readBytes(), 0f)

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
        for (type in arrayOf("Citizens", "Food", "Happiness", "Research", "Stone")) {
            gbJam6.manager.load("sprites/gui/$type.png", Texture::class.java)
        }

        // TREE
        gbJam6.manager.load("sprites/tree/tree.png", Texture::class.java)
        gbJam6.manager.load("sprites/tree/cursor-sheet.png", Texture::class.java)
        gbJam6.manager.load("sprites/tree/unlocked.png", Texture::class.java)

        // SHADER
        gbJam6.manager.load("shaders/colorTable.png", Texture::class.java)

    }

    override fun render(delta: Float) {

        if (gbJam6.manager.update()) {
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
        shapeRenderer.rect(-60f, -1f, progress * 120, 2f)
        shapeRenderer.end()

    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}