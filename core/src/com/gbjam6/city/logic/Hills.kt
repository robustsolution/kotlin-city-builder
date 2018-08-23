package com.gbjam6.city.logic

import com.gbjam6.city.general.Def
import java.util.*

data class Chunk(val height: Int, val slope: Int)

class Hills {

    val chunks: List<Chunk>

    companion object {
        val slopes = listOf(-16, -8, -8, -8, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 16)
        val heights = listOf(16, 24, 32, 40, 48)
    }

    init {
        val chunksTemp = mutableListOf<Chunk>()
        val n = Random().nextInt(heights.size)
        chunksTemp.add(Chunk(heights.elementAt(n), 0))
        chunksTemp.add(Chunk(heights.elementAt(n), 0))
        for (i in 1..Def.nChunks-2) {
            chunksTemp.add(chunkChoice(chunksTemp.elementAt(i - 1), chunksTemp.elementAt(i)))
        }
        chunks = chunksTemp.toList()
    }

    private fun chunkChoice(prepreChunk: Chunk, preChunk: Chunk): Chunk {
        val height = preChunk.height + preChunk.slope
        val slope: Int
        slope = if (prepreChunk.slope == 0 && preChunk.slope == 0) {
            val slopes2 = when {
                height in 33..47 -> slopes.filter { it < 16 }
                height >= 48 -> slopes.filter { it < 8 }
                height == 32 -> slopes
                height in 17..31 -> slopes.filter { it > -16 }
                else -> slopes.filter { it > -8 }
            }
            slopes2.elementAt(Random().nextInt(slopes2.size))
        } else {
            0
        }
        return Chunk(height, slope)
    }
}