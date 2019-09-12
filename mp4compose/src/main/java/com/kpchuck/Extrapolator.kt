package com.kpchuck

import android.util.ArrayMap

class Extrapolator(private val effect: Effect, private val defaultValue: Float, private val timeValues: ArrayMap<Int, Float>) {

    enum class Effect(val getValueAt: (x: Int, y: Float, diff: Float) -> Float){
        Linear({x, y, diff -> (diff * x) + y})
    }

    fun valueAt (position: Int): Float {
        val closestFrames = getClosestFrames(position)
        if (closestFrames.first == -1)
            return defaultValue
        val startFrame = closestFrames.first
        val endFrame = closestFrames.second
        val startAttr = timeValues[closestFrames.first]!!
        val endAttr = timeValues[closestFrames.second]!!
        val returnValue = getValueAt(startFrame, endFrame, startAttr, endAttr, position)
        return returnValue
    }

    private fun getValueAt(startFrame: Int, endFrame: Int, startAttr: Float, endAttr: Float, frame: Int): Float {
        if (endFrame == startFrame)
            return startAttr
        val diff = (endAttr - startAttr)/(endFrame - startFrame)
        return effect.getValueAt(frame, startAttr, diff)
    }

    private fun getClosestFrames(position: Int): Pair<Int, Int> {
        // Need to get frames sorted, filtering any that are -1 (unset)
        val keys = timeValues.keys.sorted().filter { key -> return@filter timeValues[key] != -1f }
        if (keys.isEmpty())
            return Pair(-1, -1)
        // If only one frame, return it twice
        if (keys.size == 1)
            return Pair(keys.first(), keys.first())
        // If position is less than first key or greater than last key, return those keys twice
        if (position >= keys.last())
            return Pair(keys.last(), keys.last())
        if (position <= keys.first())
            return Pair(keys.first(), keys.first())

        // Get index of last key smaller than position
        var closest = 0
        keys.forEach {i ->
            if (i >= position) return@forEach
            closest++
        }
        // If closest key is the position, return it
        if (keys[closest] == position)
            return Pair(keys[closest], keys[closest])
        // If closest key is last or key just return it twice
        if (closest == keys.size -1)
            return Pair(keys[closest-1], keys[closest])
        // Now just return closest and next key
        return Pair(keys[closest], keys[closest+1])
    }
}