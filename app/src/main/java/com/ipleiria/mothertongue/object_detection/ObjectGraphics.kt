package com.ipleiria.mothertongue.object_detection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.ipleiria.mothertongue.utils.GraphicOverlay

/** Draw the detected visionObject info in preview.  */
class ObjectGraphic internal constructor(
    overlay: GraphicOverlay,
    private val visionObject: FirebaseVisionObject
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint
    private val textPaint: Paint

    init {
        boxPaint = Paint()
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = STROKE_WIDTH

        textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = TEXT_SIZE
    }

    override fun draw(canvas: Canvas) {
        // Draws the bounding box.
        val rect = RectF(visionObject.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, boxPaint)

        // Draws other object info.
        canvas.drawText(
            getCategoryName(visionObject.classificationCategory),
            rect.left,
            rect.bottom,
            textPaint
        )
        visionObject.trackingId?.let {
            canvas.drawText("id: $it", rect.left, rect.top, textPaint)
        }
        visionObject.classificationConfidence?.let {
            canvas.drawText("confidence: $it", rect.right, rect.bottom, textPaint)
        }
    }

    companion object {
        private const val TEXT_SIZE = 54.0f
        internal const val STROKE_WIDTH = 4.0f

        private fun getCategoryName(@FirebaseVisionObject.Category category: Int): String {
            when (category) {
                FirebaseVisionObject.CATEGORY_UNKNOWN -> return "Unknown"
                FirebaseVisionObject.CATEGORY_HOME_GOOD -> return "Home good"
                FirebaseVisionObject.CATEGORY_FASHION_GOOD -> return "Fashion good"
                FirebaseVisionObject.CATEGORY_PLACE -> return "Place"
                FirebaseVisionObject.CATEGORY_PLANT -> return "Plant"
                FirebaseVisionObject.CATEGORY_FOOD -> return "Food"
            } // fall out
            return ""
        }
    }
}
