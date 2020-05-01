package com.ipleiria.mothertongue.object_detection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.ipleiria.mothertongue.utils.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic(
    private val overlay: GraphicOverlay,
    private val labels: List<FirebaseVisionImageLabel>
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60.0f
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.width / 4.0f
        var y = overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label.text, x, y, textPaint)
            y -= 62.0f
        }
    }
}
