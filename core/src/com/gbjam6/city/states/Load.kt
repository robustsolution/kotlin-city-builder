package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.GBJam6
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer


class Load(val gbJam6: GBJam6): KtxScreen {
    private val shapeRenderer = ShapeRenderer()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)

    private val bgColor = Color.valueOf("A7CBD5")

    override fun show() {
        super.show()

        shapeRenderer.projectionMatrix = camera.combined

        gbJam6.manager.load("fonts/skullboy.fnt", BitmapFont::class.java)
        gbJam6.manager.load("sprites/dot.png", Texture::class.java)
        gbJam6.manager.load("sprites/name.png", Texture::class.java)

    }

    override fun render(delta: Float) {

        if(gbJam6.manager.update()) {
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