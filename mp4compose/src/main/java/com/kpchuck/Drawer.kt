package com.kpchuck

import android.graphics.Canvas
import android.util.ArrayMap
import android.util.Size

abstract class Drawer {

    abstract fun draw(canvas: Canvas, position: Int)

}