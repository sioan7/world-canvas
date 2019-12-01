package com.example.worldcanvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.scale


class CanvasView
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : AppCompatImageView(context) {

    val color = Color.MAGENTA

    private val path = Path()
    private val brush = Paint()
    private val pBackground = Paint()
    private val pText = Paint()

    private val dm = DisplayMetrics()
    private val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var bmpImage: Bitmap = BitmapFactory
        .decodeResource(resources, context.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE).getInt("Canvas",0))
        .copy(Bitmap.Config.ARGB_8888, true)
    private var imagePosition: RectF

    init {
        brush.isAntiAlias = true
        brush.color = color
        brush.style = Paint.Style.STROKE
        brush.strokeJoin = Paint.Join.ROUND
        brush.strokeWidth = 8f

        wm.defaultDisplay.getMetrics(dm)

        imagePosition = centerBitmapViewport(bmpImage, dm)
        bmpImage = bmpImage.scale(
            (imagePosition.right - imagePosition.left).toInt(),
            (imagePosition.bottom - imagePosition.top).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bmpImage, null, imagePosition, brush)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = (event.x - imagePosition.left).toInt()
        val y = (event.y - imagePosition.top).toInt()
        if (x < 0 || x >= bmpImage.width || y < 0 || y >= bmpImage.height) return false
        if (bmpImage.getPixel(x, y) == Color.valueOf(250f / 255f, 250f / 255f, 250f / 255f).toArgb()) return false
        if (bmpImage.getPixel(x, y) == Color.valueOf(244f / 255f, 244f / 255f, 244f / 255f).toArgb()) return false
        if (bmpImage.getPixel(x, y) == Color.BLACK) return false
        if (Color.valueOf(bmpImage.getPixel(x, y)).red() < .5) return false
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val floodFiller = QueueLinearFloodFiller(bmpImage, bmpImage.getPixel(x, y), Color.MAGENTA)
                floodFiller.setTolerance(10)
                floodFiller.floodFill(x, y)
                postInvalidate()
                true
            }
            else -> false
        }
    }

    private fun centerBitmapViewport(bitmap: Bitmap, dm: DisplayMetrics, threshold: Float = 100f): RectF {
         val scalingFactor = (dm.widthPixels - 2 * threshold) / bitmap.width / 2
         return RectF(
             threshold,
             dm.heightPixels / 2 - bitmap.height * scalingFactor,
             dm.widthPixels - threshold,
             dm.heightPixels / 2 + bitmap.height * scalingFactor
         )
    }

}



