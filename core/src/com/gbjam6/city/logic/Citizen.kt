package com.gbjam6.city.logic

import com.gbjam6.city.Building
import com.gbjam6.city.general.Def
import com.gbjam6.city.general.Util
import com.gbjam6.city.states.City

class Citizen(val name: String, var building: Building) {
    var life: Int = Def.LIFE_TIME

    fun older(){
        life--
        if (life <= 0){
            building.lCitizenKill.add(this)
        }
    }
}