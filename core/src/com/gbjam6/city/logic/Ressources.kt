package com.gbjam6.city.logic

import com.gbjam6.city.states.City
import kotlin.math.max
import kotlin.math.min

data class Ressources(var citizens: Int = 0, var food: Int = 0, var stone: Int = 0, var happiness: Int = 0, var research: Int = 0) {
    infix fun add(ressources: Ressources) {
        this.stone += ressources.stone
        this.citizens += ressources.citizens
        this.food += ressources.food
        this.research += ressources.research
        this.happiness += ressources.happiness
    }

    infix fun addLimit(ressources: Ressources) {
        this add ressources
        food = max(0, food)
        if (food > City.limits.food && food - ressources.food <= City.limits.food) {
            food = City.limits.food
        }
        if (food > City.limits.food && food - ressources.food > City.limits.food ) {
            food -= ressources.food
        }
        if (food > City.limits.food && ressources.food < 0){
            food += ressources.food
        }
        if (stone > City.limits.stone && stone - ressources.stone <= City.limits.stone) {
            stone = City.limits.stone
        }
        if (stone > City.limits.stone && stone - ressources.stone > City.limits.stone) {
            stone -= ressources.stone
        }
        this.happiness = min(City.limits.happiness, happiness)
        this.research = min(this.research, City.limits.research)
    }

    override fun toString(): String{
        return when{
            this.happiness != 0 -> "${this.happiness} Hapiness"
            this.stone != 0 -> "${this.stone} Stone"
            this.research != 0 -> "${this.research} Research"
            this.food > 0 -> "${this.food} Food"
            else -> "Nothing"
        }
    }
}