package com.daasuu.mp4compose.dynamicfilter

import android.graphics.Canvas
import android.opengl.GLES10.GL_FLOAT
import android.opengl.GLES20
import android.util.Log
import com.daasuu.mp4compose.filter.GlFilter
import com.daasuu.mp4compose.filter.GlOverlayFilter
import com.daasuu.mp4compose.gl.GlFramebufferObject

open class GlDynamicFilter(vertexShaderSource: String, fragmentShaderSource: String): GlFilter(vertexShaderSource, fragmentShaderSource) {

    override fun draw(texName: Int, fbo: GlFramebufferObject?) {
        useProgram()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferName)
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"))
        GLES20.glVertexAttribPointer(
            getHandle("aPosition"),
            VERTICES_DATA_POS_SIZE,
            GL_FLOAT,
            false,
            VERTICES_DATA_STRIDE_BYTES,
            VERTICES_DATA_POS_OFFSET
        )
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"))
        GLES20.glVertexAttribPointer(
            getHandle("aTextureCoord"),
            VERTICES_DATA_UV_SIZE,
            GL_FLOAT,
            false,
            VERTICES_DATA_STRIDE_BYTES,
            VERTICES_DATA_UV_OFFSET
        )

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texName)
        GLES20.glUniform1i(getHandle("sTexture"), 0)

        Log.d("hellothere", "Presentation time is ${fbo!!.presentationTime}")
        onDraw(fbo!!.presentationTime/1000000)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(getHandle("aPosition"))
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    open fun onDraw(presentationTime: Long) {}
}