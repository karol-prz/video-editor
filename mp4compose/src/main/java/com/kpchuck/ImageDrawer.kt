package com.kpchuck

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Size
import com.daasuu.mp4compose.Rotation
import kotlin.math.roundToInt

data class ImageDrawer(val size: Extrapolator,
                       val opacity: Extrapolator,
                       val xPosition: Extrapolator, val yPosition: Extrapolator,
                       val rotation: Extrapolator,
                       val bitmap: Bitmap,
                       val dimens: Size): Drawer() {

    override fun draw(canvas: Canvas, position: Int) {
        if (size.valueAt(position) == 0f) return
        val width = dimens.width.toFloat() / 100 * size.valueAt(position)
        val height = (bitmap.height.toFloat()/bitmap.width) * width

        val alpha = ((opacity.valueAt(position)/100) * 255).toInt()
        val paint = Paint()
        paint.alpha = alpha

        val x_coord = (dimens.width.toFloat() / 100 ) * xPosition.valueAt(position)
        val y_coord = (dimens.height.toFloat() / 100 ) * yPosition.valueAt(position)

        val rotation = rotation.valueAt(position) * 3.6f
        //Draw bounding rect before rotating text:
        val matrix = Matrix()
        val imageCenterX = x_coord + (width/2)
        val imageCenterY = y_coord + (height/2)
        matrix.preTranslate(x_coord, y_coord)
        matrix.preRotate(rotation, imageCenterX, imageCenterY)
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, width.roundToInt() - bitmap.width/2, height.roundToInt(), true), matrix, paint)
    }
}