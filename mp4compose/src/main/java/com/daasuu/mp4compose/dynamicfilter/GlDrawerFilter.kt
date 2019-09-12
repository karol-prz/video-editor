package com.daasuu.mp4compose.dynamicfilter

import android.graphics.Canvas
import com.kpchuck.Drawer

class GlDrawerFilter(val drawer: Drawer): GlDynamicOverlayFilter() {

    override fun drawCanvas(canvas: Canvas, time: Long) {
        drawer.draw(canvas, time.toInt())
    }
}