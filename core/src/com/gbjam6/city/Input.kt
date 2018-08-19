package com.gbjam6.city

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.gbjam6.city.general.Util

/**
 * Manages inputs.
 *
 * DPAD is mapped to WASD / ZQSD / Arrows
 * A is mapped to O / SPACE
 * B is mapped to K / ESC
 * START is mapped to B / ENTER
 * SELECT is mapped to V / TAB
 */
interface Input {

    fun processInputs() {
        if (Util.inputFreeze > 0) {
            Util.inputFreeze--
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP) || Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                up()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                down()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Input.Keys.Q) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                left()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                right()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.O) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                a()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.K) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                b()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.B) || Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                start()
            }
            if (Gdx.input.isKeyPressed(Input.Keys.V) || Gdx.input.isKeyPressed(Input.Keys.TAB)) {
                select()
            }
        }
    }

    fun up() {}

    fun down() {}

    fun left() {}

    fun right() {}

    fun a() {}

    fun b() {}

    fun start() {}

    fun select() {}

}
