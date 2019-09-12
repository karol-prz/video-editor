package com.kpchuck

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.Size


data class TextDrawer(val text: String, val color: Int, val size: Extrapolator, val alpha: Extrapolator,
                 val xPosition: Extrapolator, val yPosition: Extrapolator,
                 val rotation: Extrapolator, val dimens: Size
): Drawer() {


    override fun draw(canvas: Canvas, position: Int) {
        val tPaint = Paint()
        tPaint.textSize = size.valueAt(position)
        tPaint.color = color
        tPaint.alpha = ((alpha.valueAt(position)/100) * 255).toInt()
        tPaint.style = Paint.Style.FILL

        val x_coord = (dimens.width.toFloat() / 100 ) * xPosition.valueAt(position)
        val y_coord = (dimens.height.toFloat() / 100 ) * yPosition.valueAt(position)

        val rotationPercent = rotation.valueAt(position)
        val rotation = 3.6f * rotationPercent
        //Draw bounding rect before rotating text:
        val rect = Rect()
        tPaint.getTextBounds(text, 0, text.length, rect)
        canvas.rotate(rotation, x_coord + rect.exactCenterX(), y_coord + rect.exactCenterY())

        // Draw text so it's centered
        canvas.drawText(text, x_coord - rect.width()/2, y_coord, tPaint)
    }


}