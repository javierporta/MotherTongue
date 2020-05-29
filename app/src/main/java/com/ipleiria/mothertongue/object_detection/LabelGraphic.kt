package com.ipleiria.mothertongue.object_detection

import android.graphics.*
import com.ipleiria.mothertongue.utils.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic(
    private val overlay: GraphicOverlay,
    private val labels: List<String>
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = ObjectGraphic.STROKE_WIDTH
        alpha = 20
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60.0f
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.x + 30f //overlay.width / 4.0f
        var y = overlay.y + 200f//overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label, x, y, textPaint)
            y += 62.0f
        }

        // Draws the bounding box.
        val rect = RectF(10f, 60f, 200f, y - 20f)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)

        canvas.drawRect(rect, boxPaint)
    }
}
