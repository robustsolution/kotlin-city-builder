package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.*
import com.gbjam6.city.general.Def
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.general.Util
import com.gbjam6.city.graphics.Building
import com.gbjam6.city.graphics.GUI
import com.gbjam6.city.logic.Hills
import ktx.app.KtxScreen

enum class States {
    IDLE, PLACE_BUILDING, MENU, PLACE_CITIZEN
}

/**
 * Main game class.
 */
class City(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(160f, 144f, camera)
    private val menuManager = MenuManager(gbJam6)

    private lateinit var hillSprites: Array<Sprite>
    private lateinit var pointer: Sprite
    private lateinit var pointerSmiley: Sprite
    private lateinit var gui: GUI
    private lateinit var font: BitmapFont
    private lateinit var smallFont: BitmapFont
    private var frame = 0
    private var pause = false

    companion object {
        var hills = Hills()
        var state = States.IDLE
        val buildings = mutableListOf<Building>()
        val ressources = Def.startingRessources.copy()
        val limits = Ressources(happiness = 9999, research = 9999)
    }

    override fun show() {
        super.show()
        camera.position.x = 0f

        // Init fonts
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        smallFont = gbJam6.manager.get("fonts/little.fnt", BitmapFont::class.java)

        // Init pointers
        pointer = Sprite(gbJam6.manager.get("sprites/pointerUp.png", Texture::class.java))
        pointerSmiley = Sprite(gbJam6.manager.get("sprites/pointerSmiley.png", Texture::class.java))
        updatePointer()

        // Init GUI
        gui = GUI(gbJam6)

        // Create sprites for each slope
        val ttext = gbJam6.manager.get("sprites/tiles-sheet.png", Texture::class.java)
        hillSprites = arrayOf(
                Sprite(ttext, 1 * 2 + 0, 2, 32, 32),
                Sprite(ttext, 2 * 2 + 32, 2, 32, 32),
                Sprite(ttext, 3 * 2 + 64, 2, 32, 32),
                Sprite(ttext, 4 * 2 + 96, 2, 32, 32),
                Sprite(ttext, 3 * 2 + 64, 2, 32, 32),
                Sprite(ttext, 4 * 2 + 96, 2, 32, 32)
        )
        hillSprites[4].flip(true, false)
        hillSprites[5].flip(true, false)

        // Play the city music
        gbJam6.player.play(gbJam6.cityMusic1, true, true, 0f, 0f)

    }

    override fun render(delta: Float) {

        if (!pause) {
            frame += 1
            if (frame % Def.speed1 == 0) {
                Util.tick()
            }
            if (frame == Def.RESET - 1) {
                frame = 0
            }
        }

        camera.update()
        menuManager.update()
        batch.projectionMatrix = camera.combined

        // Clear screen
        Gdx.gl.glClearColor(Def.color4.r, Def.color4.g, Def.color4.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        // Draw background

        // Draw buildings
        for (building in buildings) {
            if (Math.abs(building.x - camera.position.x) < 240)
                building.draw(batch)
        }

        // Draw moving building
        menuManager.drawBuilding(batch)

        // Draw chunks
        for ((i, chunk) in hills.chunks.withIndex()) {
            // Draw 7 chunks around camera x position
            val x = Math.floor(camera.position.x / 32.0)
            if (i - Def.nChunks / 2 in x - 3..x + 3) {

                val chunkX = -800f + 32f * i
                val chunkY = -88f + chunk.height.toFloat()

                batch.draw(hillSprites[0], chunkX, chunkY - 32)
                batch.draw(hillSprites[0], chunkX, chunkY - 64)

                when (chunk.slope) {
                    0 -> batch.draw(hillSprites[1], chunkX, chunkY)
                    8 -> batch.draw(hillSprites[2], chunkX, chunkY)
                    16 -> batch.draw(hillSprites[3], chunkX, chunkY)
                    -8 -> batch.draw(hillSprites[4], chunkX, chunkY - 8)
                    -16 -> batch.draw(hillSprites[5], chunkX, chunkY - 16)
                }

            }
        }

        // Draw the pointer
        if (City.state != States.PLACE_CITIZEN) {
            pointer.draw(batch)
        } else {
            val building = Util.getBuilding(camera.position.x)
            if ((building != null && building.citizens.size < building.lBuilding.capacity) || frame % 60 > 30)
                pointerSmiley.draw(batch)
        }

        // Draw the GUI
        gui.draw(batch, smallFont, camera.position.x)

        // Draw the menu
        menuManager.drawMenu(batch, smallFont)
        menuManager.drawHelper(batch, smallFont, camera.position.x)

        batch.end()

        processInputs()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    private fun updatePointer() {
        pointer.x = camera.position.x - 4

        // Compute difference between camera x and the current chunk x
        val n = Math.floor(camera.position.x / 32.0).toInt() + 25
        val chunk = hills.chunks[n]
        val diff = camera.position.x - (n - 25) * 32

        // Set height to follow the slopes
        pointer.y = Util.getPixel(-87f + chunk.height + diff * chunk.slope / 32)
        pointerSmiley.x = pointer.x
        pointerSmiley.y = pointer.y + 1
    }

    override fun up() {
        when (state) {
            States.MENU -> {
                Util.inputFreeze = 8
                menuManager.moveCursor(-1)
            }
        }
    }

    override fun down() {
        when (state) {
            States.MENU -> {
                Util.inputFreeze = 8
                menuManager.moveCursor(1)
            }
            States.PLACE_BUILDING -> menuManager.flip(camera.position.x, pointer.y)
            States.IDLE -> Util.showIDLEHelper(camera.position.x)
            else -> {
            }
        }
    }

    override fun left() {
        if (state == States.IDLE || state == States.PLACE_BUILDING || state == States.PLACE_CITIZEN) {
            // Move the camera
            Util.inputFreeze = 1
            if (camera.position.x > -798f + 80f) {
                if (Util.wasPressed) {
                    camera.translate(-3f, 0f)
                } else {
                    // Special slow first frame (for precise movements)
                    Util.inputFreeze = 4
                    camera.translate(-1f, 0f)
                }
                updatePointer()
            }
            menuManager.updateBuilding(camera.position.x, pointer.y)
            Util.updateHelper(camera.position.x)
        }
    }

    override fun right() {
        if (state == States.IDLE || state == States.PLACE_BUILDING || state == States.PLACE_CITIZEN) {
            // Move the camera
            Util.inputFreeze = 1
            if (camera.position.x < 798f - 80f) {
                if (Util.wasPressed) {
                    camera.translate(3f, 0f)
                } else {
                    // Special slow first frame (for precise movements)
                    Util.inputFreeze = 4
                    camera.translate(1f, 0f)
                }
                updatePointer()
            }
            menuManager.updateBuilding(camera.position.x, pointer.y)
            Util.updateHelper(camera.position.x)
        }
    }

    override fun a() {
        state = when (state) {
            // Open the menu
            States.IDLE -> {
                menuManager.open(camera.position.x)
            }
            // Select the pointed menu option or the pointed spot
            States.MENU, States.PLACE_BUILDING, States.PLACE_CITIZEN -> {
                menuManager.select(camera.position, pointer.y)
            }
        }
    }

    override fun b() {
        state = when (state) {
            States.MENU -> {
                menuManager.close()
                if (menuManager.menus.any()) {
                    States.MENU
                } else {
                    // Close the helper
                    MenuManager.helper.visible = false
                    // Go back to IDLE state
                    States.IDLE
                }
            }
            States.PLACE_BUILDING -> {
                menuManager.placingB = null
                menuManager.updateMenu(camera.position.x)
                States.MENU
            }
            States.PLACE_CITIZEN -> {
                menuManager.placingC = null
                menuManager.updateMenu(camera.position.x)
                menuManager.menus.clear()
                States.IDLE
            }
            else -> States.IDLE
        }
    }

    override fun select() {
        pause = !pause
    }
}