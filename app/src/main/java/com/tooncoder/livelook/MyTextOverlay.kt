package com.tooncoder.livelook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class MyTextOverlay(
    context: Context,
    var text: String,
    private val viewWidth: Float,
    private val viewHeight: Float
) : View(context) {

    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text, viewWidth - 300, 50f, paint)
    }
}
