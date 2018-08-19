package com.gbjam6.city

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gbjam6.city.states.City
import ktx.app.KtxScreen

enum class Type {
    CREATION, BUILDING, CITYZENS, SURE, IMPROVE
}

class Menu : KtxScreen, Input {

    var indice = 0
    private var building: Building? = null
    private var visible = false
    private var type = Type.CREATION
    private val creationList = listOf("Blabla", "Blibli")
    private val buildinglist = listOf("Move", "Improve", "Destroye")
    private lateinit var list: List<String>


    fun open(pos: Float) {
        building = City.buildings.firstOrNull { it.x <= pos && pos <= it.x + it.width }
        if (building == null) {
            list = creationList
            type = Type.CREATION
        } else {
            list = buildinglist
            type = Type.BUILDING
        }
        visible = true
    }

    fun use() {
        when (Pair(type, indice)) {
            Pair(Type.BUILDING, 0) -> {
                type = Type.CITYZENS
                list = List(building!!.cityzens.size){building!!.cityzens[it].name}
            }
            Pair(Type.CITYZENS, 0) -> {
            }
            Pair(Type.SURE, 0) -> {
            }
            Pair(Type.IMPROVE, 0) -> {
            }
        }
    }

    fun draw(batch: SpriteBatch) {
        if (visible) {
            //draw les elements de list
        }
    }
}