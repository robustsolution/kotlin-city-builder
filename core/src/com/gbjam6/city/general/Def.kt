package com.gbjam6.city.general

import com.badlogic.gdx.graphics.Color
import com.gbjam6.city.logic.Ressources

enum class MenuType {
    CREATION, CATEGORY, BUILDING, CITIZENS, CONFIRM, IMPROVE, HYDRATE, ADD, REMOVE, EXPAND
}

enum class BuildingType {
    CITIZENS, HAPPINESS, FOOD, RESEARCH, STONE, OTHER
}

enum class SFX {
    SWIPE, SELECT, BUILD, PLACE_CITIZEN, DIE, DESTROYED, COLLAPSE, EXPAND, NO_FOOD, DISABLED, RETURN
}


data class LBuilding(val type: BuildingType, val name: String, val capacity: Int, var door: Pair<Int, Int>, var s8: Pair<Int, Int>, var s16: Pair<Int, Int>, val cost: Int, val upgradeCost: Int = (cost * 1.5).toInt())

data class TreeUpgrade(val x: Int, val y: Int, val name: String, val cost: Int, val desc: String)

object Def {

    // GENERAL
    val startingRessources = Ressources(food = 200, happiness = 900, stone = 900, research = 2000)

    // GAME DESIGN
    const val SPEED = 120
    const val BIRTH_COST = 100
    const val LIFE_TIME = 300
    const val DAMAGED_LIMIT_PCT = 0.30
    const val BUILD_LIFE_TIME = 300
    const val WELL_RANGE = 80
    const val EXCHANGE_VALUE = 100
    const val EXCHANGE_TIME = 10
    const val HOUSE_LIMIT = 6
    const val HOUSE_PLUS_LIMIT = 3
    const val FARM_LIMIT = 100
    const val FARM_PLUS_LIMIT = 50
    const val FACTORY_LIMIT = 100
    const val FACTORY_PLUS_LIMIT = 50
    const val SCHOOL_LIMIT = 4
    const val SCHOOL_CITIZEN_COST = 75
    const val WAREHOUSELIMIT = 200
    const val HOSPITAL_CITIZEN_LIFE = 600
    const val CRAFTMAN_BUILDING_LIFE = 600
    const val DESTROY_HAP_PCT = 0.5
    const val DESTROY_STN_PCT = 0.3
    val STARTING_LIMITS = Pair(-160, 160)
    val EXPAND_COST = arrayOf(1, 1, 1, 1,1,1)
    const val EXPAND_SIZE: Int = 160
    const val BUILDING_RANGE = 90
    const val INTERACTION_PLUS = 0.20
    const val INTERACTION_PLUS_PLUS = 0.40
    const val INTERACTION_MALUS = -0.20
    const val INTERACTION_MALUS_MALUS = -0.40
    const val FARM_PRODUCTION = 4.0
    const val LABORATORY_PRODUCTION = 3.0
    const val TAVERN_PRODUCTION = 2.0
    const val FACTORY_PRODUCTION = 2.0
    const val HOSPITAL_PRODUCTION = 5.0
    const val GARDEN_PRODUCTION = 2.0
    const val CRAFTMAN_PRODUCTION = 4.0
    const val WAREHOUSE_PRODUCTION = 6.0
    const val STARVING_KILL_TICK = 10


    // SIZE
    const val nChunks = 80
    const val menuWidth = 72f
    const val menuY = 52f
    const val helperWidth = 64f
    const val helperY = 46f
    const val speedY = 54f
    const val speedOffset = 4f

    // MENUS
    val menus = mapOf(
            MenuType.CREATION to arrayOf("CITIZENS", "HAPPINESS", "FOOD", "RESEARCH", "STONE", "OTHER"),
            MenuType.BUILDING to arrayOf("CITIZENS", "REPAIR", "DESTROY"),
            MenuType.CONFIRM to arrayOf("YES", "NO"),
            MenuType.HYDRATE to arrayOf("ADD", "REMOVE", "RETURN"),
            MenuType.EXPAND to arrayOf("EXPAND", "RETURN")
    )

    // BUILDINGS
    val buildings = listOf(
            LBuilding(BuildingType.CITIZENS, "HOUSE", 6, Pair(34, 41), Pair(34, 41), Pair(24, 41), 100),
            LBuilding(BuildingType.HAPPINESS, "TAVERN", 2, Pair(13, 20), Pair(13, 26), Pair(13, 39), 100),
            LBuilding(BuildingType.FOOD, "FARM", 2, Pair(19, 39), Pair(19, 39), Pair(19, 39), 100),
            LBuilding(BuildingType.RESEARCH, "LABORATORY", 2, Pair(20, 28), Pair(20, 29), Pair(20, 38), 100),
            LBuilding(BuildingType.STONE, "FACTORY", 2, Pair(6, 19), Pair(6, 30), Pair(6, 22), 100),
            LBuilding(BuildingType.OTHER, "WELL", 0, Pair(0, 17), Pair(0, 17), Pair(0, 17), 100),
            LBuilding(BuildingType.STONE, "CRAFTMAN", 1, Pair(20, 36), Pair(20, 36), Pair(20, 36), 100),
            LBuilding(BuildingType.FOOD, "WAREHOUSE", 1, Pair(24, 51), Pair(24, 51), Pair(24, 51), 200),
            LBuilding(BuildingType.HAPPINESS, "GARDEN", 1, Pair(43, 88), Pair(43, 88), Pair(43, 88), 200),
            LBuilding(BuildingType.RESEARCH, "HOSPITAL", 1, Pair(20, 43), Pair(20, 54), Pair(20, 64), 200),
            LBuilding(BuildingType.CITIZENS, "SCHOOL", 4, Pair(67, 79), Pair(31, 79), Pair(21, 79), 200)
    )

    val upgradedBuilding = listOf(
            LBuilding(BuildingType.CITIZENS, "HOUSE+", 9, Pair(34, 41), Pair(34, 41), Pair(24, 41), 150),
            LBuilding(BuildingType.HAPPINESS, "TAVERN+", 3, Pair(13, 20), Pair(13, 26), Pair(13, 39), 150),
            LBuilding(BuildingType.FOOD, "FARM+", 3, Pair(19, 39), Pair(19, 39), Pair(19, 39), 150),
            LBuilding(BuildingType.RESEARCH, "LABORATORY+", 3, Pair(20, 28), Pair(20, 29), Pair(20, 38), 150),
            LBuilding(BuildingType.STONE, "FACTORY+", 3, Pair(6, 19), Pair(6, 30), Pair(6, 22), 150)
    )

    val initAvailableBuilding = listOf(
            "HOUSE", "TAVERN", "FARM", "FACTORY", "LABORATORY"
    )

    val destroyedRessources = listOf(
            "HOUSE", "HOUSE+", "TAVERN", "TAVERN+", "FARM", "FARM+",
            "LABORATORY", "LABORATORY+", "FACTORY", "FACTORY+",
            "CRAFTMAN", "HOSPITAL", "SCHOOL", "WAREHOUSE"
    )
    val customMenus = mapOf(
            "WELL" to arrayOf("HYDRATE", "REPAIR", "DESTROY"),
            "HOUSE" to arrayOf("CITIZENS", "BIRTH", "UPGRADE", "REPAIR", "DESTROY"),
            "HOUSE+" to arrayOf("CITIZENS", "BIRTH", "REPAIR", "DESTROY"),
            "TAVERN" to arrayOf("CITIZENS", "UPGRADE", "REPAIR", "DESTROY"),
            "LABORATORY" to arrayOf("CITIZENS", "UPGRADE", "REPAIR", "DESTROY"),
            "FACTORY" to arrayOf("CITIZENS", "UPGRADE", "REPAIR", "DESTROY"),
            "FARM" to arrayOf("CITIZENS", "UPGRADE", "REPAIR", "DESTROY"),
            "GARDEN" to arrayOf("CITIZENS", "EXCHANGE", "REPAIR", "DESTROY")
    )

    // TREE
    val xPos = Array(5) { 10 + 32 * it }
    val yPos1 = Array(3) { 6 - 32 * it }
    val yPos2 = Array(3) { 22 - 32 * it }
    val tree = listOf(
            TreeUpgrade(xPos[0], yPos1[0], "FACTORY+", 150, "Factory becomes upgradable.\n(+1 Citizen & +50 Storage)"),
            TreeUpgrade(xPos[0], yPos1[1], "TAVERN+", 150, "Tavern becomes upgradable.\n(+1 Citizen)"),
            TreeUpgrade(xPos[0], yPos1[2], "FARM+", 150, "Farm becomes upgradable.\n(+1 Citizen & +50 Storage)"),
            TreeUpgrade(xPos[1], yPos2[0], "WELL", 200, "Unlocks the Well.\nIt hydrates citizens and increases their production."),
            TreeUpgrade(xPos[1], yPos2[1], "LABORATORY+", 200, "Laboratory becomes upgradable.\n(+1 Citizen)"),
            TreeUpgrade(xPos[1], yPos2[2], "HOUSE+", 200, "House becomes upgradable.\n (+3 Citizens & +3 Population)"),
            TreeUpgrade(xPos[2], yPos1[0], "CRAFTMAN", 250, "Unlocks the Craftman.\nDoubles building's integrity when constructed."),
            TreeUpgrade(xPos[2], yPos1[1], "SCHOOL", 250, "Unlocks the School.\nBirth costs 75 Hapiness when constructed."),
            TreeUpgrade(xPos[2], yPos1[2], "WAREHOUSE", 250, "Unlocks the Warehouse.\nIncreases food and stone storage by 200."),
            TreeUpgrade(xPos[3], yPos2[0], "TREE", 300, ""),
            TreeUpgrade(xPos[3], yPos2[1], "HOSPITAL", 300, "Unlocks the Hospital.\nDoubles your Citizens's lifetime."),
            TreeUpgrade(xPos[3], yPos2[2], "PARENTING", 300, "New born citizens have a parent from the house and receive a bonus of production if he works with him"),
            TreeUpgrade(xPos[4], yPos1[0], "GARDEN", 400, "Unlocks the Garden.\nExchanges 50 Food against 50 Happiness."),
            TreeUpgrade(xPos[4], yPos1[1], "??", 400, ""),
            TreeUpgrade(xPos[4], yPos1[2], "EXPAND", 400, "Allow you to expand 2 more time")
    )
    val treeRequirements = mapOf(
            "WELL" to arrayOf("FACTORY+"),
            "LABORATORY+" to arrayOf("FACTORY+", "TAVERN+"),
            "HOUSE+" to arrayOf("TAVERN+", "FARM+"),
            "CRAFTMAN" to arrayOf("FACTORY+"),
            "SCHOOL" to arrayOf("HOUSE+", "LABORATORY+"),
            "TREE" to arrayOf("WELL"),
            "GARDEN" to arrayOf("TREE", "CRAFTMAN"),
            "HOSPITAL" to arrayOf("LABORATORY+"),
            "WAREHOUSE" to arrayOf("FARM+"),
            "PARENTING" to arrayOf("SCHOOL"),
            "EXPAND" to arrayOf("WAREHOUSE")
    )

    // ACHIEVEMENTS
    val achievements = listOf(
            Triple("ACH1", "Do this n times.", false),
            Triple("ACH2", "Do that n times.", false)
    )

    // NAMES
    val names = listOf("Jean", "yopox", "Mirionos", "A_Do", "Le Art", "Pas-Jean",
            "Aazouf", "Abaddon", "Abalysan", "Abzalon", "Actraus", "Admimar", "Aebron", "Aegis", "Ael", "Aell", "Aelrakys", "Aeris", "Agasha", "Agathorn", "Aghars", "Agon", "Aion", "Aka", "Akarius", "Akashana", "Akilons", "Akodo Tomo", "Akyrh", "Alahel", "Alak Dül", "alamar", "Alanna", "Alark", "Alatarielle", "Albyor", "Alchys", "Aldareis", "aldarel", "Aldou", "Alejandro", "Alekshan", "Alexandre", "alge'n", "Alhvor", "Altan", "Alyana", "Amatsu", "Anamelek", "Andurill", "Anemar", "Angie", "Angus", "Angye", "Anth Rhopy", "Aquilon", "Aramil", "Aravilar", "Arcanius", "Arch Sinner", "Archaos", "Arduin Angcam", "Arkan", "Arkane", "Arkos", "Armanack", "Arnase", "Arse", "Artanis", "Artarien", "Arthis", "Arthus", "Arwen", "Ash", "Ashragor", "Asilurth", "Asterion", "Aubedorée", "Azranil", "Bakemonor", "Balafrus", "Bandidaska", "Barthelby", "Basara", "Bastior", "Bennardi", "Bilechi", "Blader", "Blaz", "Blobac", "Bortzy", "Bouch", "Brator", "Brissaud", "Brutar", "Buace", "Burrich", "Byst", "Caine", "Cal", "Calion", "Caracal", "Céléniel", "Chabi", "Charlatimus", "Cheub", "Chouartz", "Chubby", "Cixi", "Clarisse", "Claw", "Corren", "Corus", "Crim", "Crowyn", "Cuchulain", "Curios", "Cyit", "Cyol", "Cyrull", "Dain", "Dakeyras", "Dalvyn", "Damz", "Dargeun", "Darius", "Darmus", "Darniel", "Darok", "Derfenak", "Dergen", "Desmond", "Devon", "Discab", "Djaal", "Dreike", "Drew", "Driele", "Duncan", "Dunkel", "Dvalinn", "Dworkin", "Ebonit", "Eel Brodavan", "Eickos", "Eilis", "Ekke", "Elbj", "Elendil", "Elenril", "Enas", "Endel", "Enethaeron", "Entrax", "Enval", "Eol", "Epone", "Eredren", "Eregior", "Erquaël", "Erwang", "Eslhe", "Ethudian", "Evlyn", "Ewak", "Eymerich", "Eyolas", "Faenry", "Faeny", "Faeriss", "Farwander", "Faucon de lune", "Fedaykin", "Fedd", "Fenaloeth", "Fenrir", "Feusange", "Finkel", "Finraenor", "Fitz", "Foredrak", "Froston", "Froum", "Fynorel", "Gakhad", "Galenor", "Galenor", "Galfon", "Galhad", "Gallad", "Garth", "Geheimnis", "Genseric", "Georetny", "Gerd", "Gerre", "Ghorghor", "Ghorgor", "Ghylian", "Giftbestcom", "Glad", "Globac", "Gloktar", "Glorim", "Glouk", "Gnarl", "Gnock", "Goldar", "Gorons", "Gortak", "Gouelan", "Gramlot", "Graoumf", "Grimdus", "Grimmbart", "Grinlen", "Grisard", "Grisord", "Grömm", "Grouik", "Grung", "Gungir", "Hades", "Hakufu", "Halex", "Halift", "harlok", "hauoka", "Haymos", "Helkior", "Hepi", "Heras Trydo", "Hherylian", "Hiaron", "Hilarion", "Hinata", "Hoel", "HruKiru", "Iblitz", "Idefel", "Ileuad", "Incanus", "Intylzah", "Iparcos", "Iroke", "Isilwen", "Iuchi", "Iverindor", "Jaffar", "Jalil", "Jarvin", "Jerrel", "Julius", "Jushban", "Kaar", "Kabafort", "Kaizen", "Kakita", "Kalimshar", "Kamuchi", "Kara", "Karel", "Karnarok", "Kazad-dum", "Keldorn", "Kellyan", "Kendashi", "Kenllen", "Kentril", "Kerien", "Kernos", "Kev", "Khazou", "Kherylian", "Khid", "Khiguard", "Khomenor", "Khyros", "Killiam", "Krahor", "Krater", "Krog", "Kronos", "Kueyen", "Kunden", "Kushban", "Kylordan", "Kymisan", "Kyoka", "Kyrios", "Lankeshire", "Lantalasse", "Lasseyka", "Lavos", "Lee Hong", "Leo", "Lequi", "Lexynian", "Licken", "Lieween", "Ligarnes", "Lodek", "Loh", "Lordanor", "Lordar", "Lothar", "Loukae", "Ludark", "Ludoco", "Lufkin", "Luindin", "Lydae", "Lyle", "lysandir", "Mahar", "Mahyar", "Maldoror", "Maliadus", "Malkendar", "Malkiak", "Mando", "Manox", "Mansour", "Markkisil", "Marwenna", "Masika", "Meeks", "Melan", "Mespheber", "Meuarth", "Meurarh", "Mikkhaël", "Milamber", "Mililith", "Minky", "Minorard", "Miraky", "Miriantir", "Mirthor", "Moledrass", "Morcar", "Morcar", "Mordicus", "Moreau", "Morkdull", "Morwan", "Moy", "Murmure", "Musazaï", "Myranda", "Myrtil", "Mystile", "Naliah", "Narcir", "Narral", "Naskyrien", "Nekronen", "Nerath", "Netheb", "Ninik", "Nirannor", "Norhel", "Odhanan", "Odonite", "Olric", "Onirim", "Onizuka", "Orgone", "Orn", "Oronard", "Otargos", "Oxidor", "Palazar", "Percey", "Perin", "Pierral", "Pierrus", "Pinpin", "Plex", "Pockels", "Psotic", "Raeps", "Ragus", "Rahyll", "Ramius", "Randin", "Rankor", "Ranx", "Ratafia", "Raventher", "Ravny", "Reador", "Rectulo", "Redyan", "Remoon", "Rkanjar", "Roan", "Rotten", "Rumix", "Runkah", "Sahe", "Sam", "Samael", "Sandax", "Sanzo", "Scap", "Schimill", "Sedna", "Sensi", "Sentenza", "Septimus", "Sergeiski", "Serguisan", "Sethie", "Sgleurts", "Shaan", "Shad", "Shadrak", "Shaïman", "Shaniah", "Sharvira", "Sheas", "Sherinford", "Sherkan", "Shiguru", "Shinai", "Shuroan", "Silvana", "Silver", "Silvermo", "Simeus", "Sindir", "Sirhun", "Sisse", "Sivan", "Siwu", "Slucha", "Soho", "Sokar", "Sokhef", "Solkjorn", "Sombrelune", "Soolveih", "Sparkle", "Sphax", "Steomp", "Sulphe", "Sun-Tzy", "Sylicer", "Syzia", "Taïrendil", "Tahn", "Tamva", "Taranis", "Taroth", "Taybott", "Tchernopuss", "Tenser", "Tepes", "Terremer", "Thadeus", "Thanatos", "Thathane", "Tholdak", "Thoralff", "Thorin", "Thrudgar", "Thur", "Tigertat", "Tlön", "Tolunks", "Topper", "Torgrim", "Torken", "Tozogawa", "Tratore", "Treme", "Trickster", "Twinky", "Tykyuk", "Ugo", "Ungarth", "Uryen", "Usul", "Vain", "Valmyr", "Valor", "Vanion", "Varan", "Vardjlork", "Varkal", "Varyus", "Veidt", "Vendemein", "Vensu", "Victorio", "Victorius", "Victoryah", "Vilad", "Villon", "Vince", "vinciane", "Violhaine", "Vlaxonne", "Waargh", "Walcom", "Waldham", "Walosprit", "Wamaris", "Wang", "Warfen", "Watson", "Wazz", "Whysmeryll", "Willicmac", "Wismerhill", "Wodian", "Wyzzini", "Xamaris", "Xill", "Xorc", "Yamael", "Yameld", "Yaneck", "Yann", "Yataka", "Yick", "Yolian", "Yorg", "Yorl", "Yrgaël", "Yrrag", "Yukio", "Zack", "Zaf", "Zenithal", "Ziz", "Zool")

    // DESCRIPTIONS
    private const val backupDesc = "MISSING :-c\nADD ME IN\nDEF.DESCRIPTIONS"
    private val descriptions = mapOf(
            "CITIZENS" to "DESC OF\nCITIZENS",
            "HAPPINESS" to "DESC OF\nHAPP",
            "FOOD" to "DESC OF\nFOOD",
            "RESEARCH" to "DESC OF\nRESEARCH",
            "STONE" to "DESC OF\nSTONE",
            "RETURN" to "GO BACK",
            "REPAIR" to "OCULUS\nREPARO :>",
            "BIRTH" to "DESC OF\nBIRTH",
            "EXCHANGE" to "DESC OF\nEXCHANGE"
    )
    fun getTypeOrder(buildingType: BuildingType): Int{
        return when (buildingType){
            BuildingType.CITIZENS -> 1
            BuildingType.HAPPINESS -> 2
            BuildingType.FOOD -> 3
            BuildingType.STONE -> 4
            BuildingType.RESEARCH -> 5
            else -> 0
        }
    }

    fun getDescription(name: String) = descriptions[name] ?: backupDesc

    // COLORS
    val color1: Color = Color.valueOf("000000")
    val color2: Color = Color.valueOf("545454")
    val color3: Color = Color.valueOf("A9A9A9")
    val color4: Color = Color.valueOf("FFFFFF")
    val clearColors = arrayOf(
            Color.valueOf("ffffff"), // GBPocket
            Color.valueOf("9bbc0f"), // GB green
            Color.valueOf("92d3ff"), // NES Blue
            Color.valueOf("d59d9d"), // yopox br
            Color.valueOf("c3a38a") // nyx8
    )
    var PALETTE_SIZE = 6

}