package com.gbjam6.city

import java.util.*

data class Chunk(val height: Int, val slope: Int)

class Hills {

    val chunks: List<Chunk>

    companion object {
        val slopes = listOf(-16, -16, -8, -8, -8, -8, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 16, 16)
        val heights = listOf(16, 24, 32, 40, 48, 56, 64)
    }

    init {
        val chunksTemp = mutableListOf<Chunk>()
        chunksTemp.add(Chunk(heights.elementAt(Random().nextInt(heights.size)), 0))
        for (i in 0..48) {
            chunksTemp.add(chunkChoice(chunksTemp.elementAt(i)))
        }
        chunks = chunksTemp.toList()
    }

    private fun chunkChoice(preChunk: Chunk): Chunk {
        val height = preChunk.height + preChunk.slope
        val slopes2 = when {
            height in 33..47 -> slopes
            height in 48..63 -> slopes.filter { it < 16 }
            height >= 64 -> slopes.filter { it < 8 }
            height in 17..32 -> slopes.filter { it > -16 }
            else -> slopes.filter { it > -8 }
        }
        var slope = slopes2.elementAt(Random().nextInt(slopes2.size))
        if (preChunk.slope < 0 && slope > 0 || preChunk.slope > 0 && slope < 0) {
            slope = 0
        }
        return Chunk(height, slope)
    }
}