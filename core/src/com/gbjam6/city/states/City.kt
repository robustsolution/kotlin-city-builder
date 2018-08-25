package com.gbjam6.city.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gbjam6.city.*
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.SFX
import com.gbjam6.city.logic.Ressources
import com.gbjam6.city.general.Util
import com.gbjam6.city.graphics.Building
import com.gbjam6.city.graphics.GUI
import com.gbjam6.city.graphics.Tree
import com.gbjam6.city.logic.Hills
import ktx.app.KtxScreen
import javax.swing.Action
import kotlin.math.abs

enum class States {
    IDLE, PLACE_BUILDING, MENU, PLACE_CITIZEN, TREE, PLACE_DECORATION
}

data class Progress(val tree: MutableList<String> = mutableListOf(), var limits: Pair<Int, Int> = Def.STARTING_LIMITS.copy(), var birthcost: Int, var lifetime: Int, var buildlife: Int)

/**
 * Main game class.
 */
class City(private val gbJam6: GBJam6) : KtxScreen, Input {
    private val batch = SpriteBatch()
    private val viewport = FitViewport(160f, 144f, camera)
    private val menuManager = MenuManager(gbJam6)

    private lateinit var hillSprites: Array<Sprite>
    private lateinit var pointer: Sprite
    private lateinit var pointerSmiley: Sprite
    private lateinit var gui: GUI
    private lateinit var font: BitmapFont
    private lateinit var smallFont: BitmapFont
    private lateinit var smallFontDisabled: BitmapFont
    private lateinit var smallFontDark: BitmapFont
    private lateinit var tree: Tree
    private val greyBg = Util.generateRectangle(120, 144, Def.color3)
    private var frame = 0

    companion object {
        val camera = OrthographicCamera()
        var hills = Hills()
        var state = States.IDLE
        val buildings = mutableListOf<Building>()
        val decorations = mutableListOf<Building>()
        var ressources = Def.startingRessources.copy()
        val limits = Ressources(happiness = 9999, research = 9999)
        val progress = Progress(mutableListOf(), birthcost = Def.BIRTH_COST, lifetime = Def.LIFE_TIME, buildlife = Def.BUILD_LIFE_TIME)
        var starvingtick = 0
        var speed = 1
        val tutorial = Tutorial()
    }

    override fun show() {
        super.show()
        camera.position.x = 0f

        // Inits fonts
        font = gbJam6.manager.get("fonts/skullboy.fnt", BitmapFont::class.java)
        smallFont = gbJam6.manager.get("fonts/little.fnt", BitmapFont::class.java)
        smallFontDisabled = gbJam6.manager.get("fonts/littleDisabled.fnt", BitmapFont::class.java)
        smallFontDark = gbJam6.manager.get("fonts/littleDark.fnt", BitmapFont::class.java)

        // Inits pointers
        pointer = Sprite(gbJam6.manager.get("sprites/pointerUp.png", Texture::class.java))
        pointerSmiley = Sprite(gbJam6.manager.get("sprites/pointerSmiley.png", Texture::class.java))
        Util.updatePointer(pointer, pointerSmiley)

        // Inits GUI
        gui = GUI(gbJam6)
        tree = Tree(gbJam6)

        // Creates sprites for each slope
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

        // Sets the shader
        ShaderProgram.pedantic = false
        batch.shader = gbJam6.shader

    }

    override fun render(delta: Float) {

        // Time based or frame based stuff
        if (speed > 0) {
            if (!tutorial.active || tutorial.active && tutorial.progression[0].action == ACTION.WAIT)
                frame += speed
            if (frame > Def.SPEED) {
                frame = 0
                Util.tick(menuManager)
                menuManager.tick()
                tutorial.tick(menuManager)
            }
        }
        tree.frame = (tree.frame + 1) % 60
        camera.update()
        menuManager.update() // Only used for blinking purposes
        batch.projectionMatrix = camera.combined

        // Clears screen
        val clearC = Def.clearColors[gbJam6.colorPalette - 1]
        Gdx.gl.glClearColor(clearC.r, clearC.g, clearC.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        gbJam6.colorTable.bind(1)
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)

        // Prepares for drawing
        batch.projectionMatrix = camera.combined
        batch.begin()

        gbJam6.shader.setUniformi("colorTable", 1)
        gbJam6.shader.setUniformf("paletteIndex", gbJam6.paletteIndex)

        // Draws background
        // TODO: Add a background

        // Draws limit background
        if (abs(camera.position.x - progress.limits.first) < 180 || abs(camera.position.x - progress.limits.second) < 180) {
            batch.draw(greyBg, progress.limits.first - 120f, -72f)
            batch.draw(greyBg, progress.limits.second.toFloat(), -72f)
        }

        // Draws buildings
        for (building in buildings) {
            if (Math.abs(building.x - camera.position.x) < 240)
                building.draw(batch)
        }

        // Draws decorations
        for (building in decorations) {
            if (Math.abs(building.x - camera.position.x) < 240)
                building.draw(batch)
        }

        // Draws moving building
        menuManager.drawBuilding(batch)

        // Draws chunks
        for ((i, chunk) in hills.chunks.withIndex()) {
            // Draws 7 chunks around camera x position
            val x = Math.floor(camera.position.x / 32.0)
            if (i - Def.nChunks / 2 in x - 3..x + 3) {

                val chunkX = 32f * (i - Def.nChunks / 2)
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

        // Draws the pointer
        if (City.state != States.PLACE_CITIZEN) {
            pointer.draw(batch)
        } else {
            val building = Util.getBuilding()
            if ((building != null && building.citizens.size < building.lBuilding.capacity) || frame % 60 > 30)
                pointerSmiley.draw(batch)
        }

        // Draws the GUI
        gui.draw(batch, smallFontDark)

        // Draws the menu
        menuManager.drawMenu(batch, smallFont, smallFontDisabled)

        // Draw the tree
        tree.draw(batch, smallFontDark)

        // Updates and draw the helper
        Util.updateHelper(menuManager.menus)
        menuManager.drawHelper(batch, smallFont)

        batch.end()

        processInputs()

    }

    override fun dispose() {
        batch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun up() {
        if (tutorial.active) {
            tutorial.up(menuManager)
        } else {
            when (state) {
                States.MENU -> {
                    Util.inputFreeze = 8
                    menuManager.moveCursor(-1)
                }
                States.TREE -> tree.up()
                States.IDLE -> gui.visible = !gui.visible
            }
        }
    }

    override fun down() {
        if (tutorial.active) {
            tutorial.down(menuManager)
        } else {
            when (state) {
                States.MENU -> {
                    Util.inputFreeze = 8
                    menuManager.moveCursor(1)
                }
                States.PLACE_BUILDING -> menuManager.flip(pointer.y)
                States.PLACE_DECORATION -> menuManager.switchDecoration()
                States.IDLE -> Util.showIDLEHelper()
                States.TREE -> tree.down()
                else -> Unit
            }
        }
    }

    override fun left() {
        if (tutorial.active) {
            tutorial.left(pointer, pointerSmiley, menuManager)
        } else {
            when (state) {
                States.IDLE, States.PLACE_CITIZEN, States.PLACE_BUILDING, States.PLACE_DECORATION -> {
                    // Moves the camera
                    Util.inputFreeze = 1
                    if (camera.position.x > progress.limits.first - 16) {
                        if (Util.wasPressed) {
                            camera.translate(-3f, 0f)
                        } else {
                            // Special slow first frame (for precise movements)
                            Util.inputFreeze = 4
                            camera.translate(-1f, 0f)
                        }
                        Util.updatePointer(pointer, pointerSmiley)
                    }
                    menuManager.updateBuilding(pointer.y)
                }
                States.TREE -> tree.left()
            }
        }
    }

    override fun right() {
        if (tutorial.active) {
            tutorial.right(pointer, pointerSmiley, menuManager)
        } else {
            when (state) {
                States.IDLE, States.PLACE_BUILDING, States.PLACE_CITIZEN, States.PLACE_DECORATION -> {
                    // Moves the camera
                    Util.inputFreeze = 1
                    if (camera.position.x < progress.limits.second + 16) {
                        if (Util.wasPressed) {
                            camera.translate(3f, 0f)
                        } else {
                            // Special slow first frame (for precise movements)
                            Util.inputFreeze = 4
                            camera.translate(1f, 0f)
                        }
                        Util.updatePointer(pointer, pointerSmiley)
                    }
                    menuManager.updateBuilding(pointer.y)
                }
                States.TREE -> tree.right()
            }
        }
    }

    override fun a() {
        if (tutorial.active) {
            tutorial.a(menuManager, pointer)
        } else {
            state = when (state) {
                // Opens the menu
                States.IDLE -> {
                    GBJam6.playSFX(SFX.SELECT)
                    gui.visible = true
                    menuManager.open()
                }
                // Selects the pointed menu option or the pointed spot
                States.MENU, States.PLACE_BUILDING, States.PLACE_CITIZEN, States.PLACE_DECORATION -> {
                    if (state == States.MENU && !menuManager.menus.last().activated[menuManager.menus.last().cursorPos]) {
                        GBJam6.playSFX(SFX.DISABLED)
                    } else {
                        GBJam6.playSFX(SFX.SELECT)
                    }
                    menuManager.select(pointer.y)
                }
                States.TREE -> tree.select()
            }
        }
    }

    override fun b() {
        if (tutorial.active) {
            tutorial.b(menuManager)
        } else {
            state = when (state) {
                States.MENU -> {
                    GBJam6.playSFX(SFX.RETURN)
                    menuManager.close()
                    Util.updateMenuHelper(menuManager.menus)
                    if (menuManager.menus.any()) {
                        States.MENU
                    } else {
                        // Closes the helper
                        MenuManager.helper.visible = false
                        // Goes back to IDLE state
                        States.IDLE
                    }
                }
                States.PLACE_BUILDING, States.PLACE_DECORATION -> {
                    GBJam6.playSFX(SFX.RETURN)
                    menuManager.placingB = null
                    menuManager.updateMenu()
                    // Opens the helper
                    MenuManager.helper.visible = true
                    States.MENU
                }
                States.PLACE_CITIZEN -> {
                    GBJam6.playSFX(SFX.RETURN)
                    menuManager.placingC = null
                    menuManager.updateMenu()
                    menuManager.menus.clear()
                    // Closes the helper
                    MenuManager.helper.visible = false
                    States.IDLE
                }
                States.TREE -> {
                    GBJam6.playSFX(SFX.RETURN)
                    States.IDLE
                }
                else -> States.IDLE
            }
        }
    }

    override fun start() {
        if (tutorial.active) {
            tutorial.start(menuManager)
        } else {
            when (state) {
                States.IDLE -> {
                    GBJam6.playSFX(SFX.SELECT)
                    MenuManager.helper.visible = false
                    state = States.TREE
                }
                States.TREE -> {
                    GBJam6.playSFX(SFX.RETURN)
                    state = States.IDLE
                }
                else -> Unit
            }
        }
    }

    override fun select() {
        if (tutorial.active) {
            tutorial.select()
        } else {
            speed = if ("HARD MODE" in City.progress.tree) {
                4 + (speed + 1) % 4
            } else {
                (speed + 1) % 4
            }
        }
    }

    override fun p() {
        gbJam6.colorPalette = (gbJam6.colorPalette + 1) % Def.PALETTE_SIZE
        if (gbJam6.colorPalette == 0) gbJam6.colorPalette++
        gbJam6.updateShader()
    }
}