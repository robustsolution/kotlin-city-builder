package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gbjam6.city.general.Def.bgColor

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

        gbJam6.manager.load("fonts/skullboy.fnt", BitmapFont::class.java)
        gbJam6.manager.load("sprites/dot.png", Texture::class.java)
        gbJam6.manager.load("sprites/name.png", Texture::class.java)
        gbJam6.manager.load("sprites/pointer.png", Texture::class.java)
        gbJam6.manager.load("sprites/tiles-sheet.png", Texture::class.java)

    }

    override fun render(delta: Float) {

        if (gbJam6.manager.update()) {
            gbJam6.setScreen<TitleScreen>()
        }

        camera.update()

        // Clear screen
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Draw the loading bar
        val progress = gbJam6.manager.progress
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.color = Color.valueOf("362C36");
        shapeRenderer.rect(-60f, -1f, progress * 120, 2f);
        shapeRenderer.end();

    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}