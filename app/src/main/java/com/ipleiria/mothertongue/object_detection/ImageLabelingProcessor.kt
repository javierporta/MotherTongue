package com.ipleiria.mothertongue.object_detection

import VisionProcessorBase
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.ipleiria.mothertongue.translations.OnGetTranslationListener
import com.ipleiria.mothertongue.translations.TranslatorService
import com.ipleiria.mothertongue.utils.CameraImageGraphic
import com.ipleiria.mothertongue.utils.FrameMetadata
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException


/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor(val targetLanguage: Int) :
    VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    private val detector: FirebaseVisionImageLabeler =
        FirebaseVision.getInstance().onDeviceImageLabeler

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionImageLabel>> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        labels: List<FirebaseVisionImageLabel>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        originalCameraImage.let { image ->
            val imageGraphic = CameraImageGraphic(graphicOverlay, image!!)
            graphicOverlay.add(imageGraphic)
        }

        //toDo: Add as function
        var translatedLabels = mutableListOf<String>()
        for (label: FirebaseVisionImageLabel in labels) {
            val translatorService =
                TranslatorService(targetLanguage)
            translatorService.translate(label.text).continueWith {
                if (it.isComplete) {
                    if (it.isSuccessful) {
                        //Showing Translated -> Original
                        translatedLabels.add(it.result!! + " -> " + label.text)
                    } else {
                        //Error when translating. Add english name
                        translatedLabels.add(label.text)
                    }
                }
            }
        }

        val labelGraphic = LabelGraphic(graphicOverlay, translatedLabels)
        graphicOverlay.add(labelGraphic)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Label detection failed.$e")
    }

    companion object {

        private const val TAG = "ImageLabelingProcessor"
    }


}
