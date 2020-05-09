package com.ipleiria.mothertongue.object_detection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.ipleiria.mothertongue.utils.GraphicOverlay

/** Graphic instance for rendering a label within an associated graphic overlay view.  */
class LabelGraphic(
    private val overlay: GraphicOverlay,
    private val labels: List<String>,
    private val objectToSearch: String
) : GraphicOverlay.Graphic(overlay) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60.0f
    }

    private val objectToSearchPaint = Paint().apply {
        color = Color.BLUE
        textSize = 100.0f
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = overlay.width / 4.0f
        var y = overlay.height / 2.0f

        for (label in labels) {
            canvas.drawText(label, x, y, textPaint)
            y -= 62.0f
        }

        canvas.drawText(
            objectToSearch,
            overlay.width / 4.0f,
            overlay.height - 10f,
            objectToSearchPaint
        )
    }
}
