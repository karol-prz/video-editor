package com.kpchuck.videoeditor.structures

class KeyFrameStore(val id: Int): HashMap<String, Float>(), Comparable<KeyFrameStore> {

    override fun compareTo(other: KeyFrameStore): Int {
        return compareValues(this[KEYFRAME_STRING], other[KEYFRAME_STRING])
    }

    companion object {
        const val KEYFRAME_STRING = "KeyFrame Time"
    }
}