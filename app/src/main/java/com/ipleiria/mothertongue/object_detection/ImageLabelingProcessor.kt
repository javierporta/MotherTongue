package com.ipleiria.mothertongue.object_detection

import VisionProcessorBase
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
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

import java.util.Timer
import kotlin.concurrent.schedule


/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor(val targetLanguage: Int, val objectToSearch: String) :
    VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    var hasFoundObject = false
    val CONGRATING_USER_TIME = 5000L //MS
    
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

        getTranslatedLabels(labels, graphicOverlay)

    }

    private fun getTranslatedLabels(
        labels: List<FirebaseVisionImageLabel>,
        graphicOverlay: GraphicOverlay
    ): MutableList<String> {
        var translatedLabels = mutableListOf<String>()
        for (label: FirebaseVisionImageLabel in labels) {
            val translatorService =
                TranslatorService(targetLanguage)
            translatorService.translate(label.text).continueWith {
                if (it.isComplete) {
                    if (it.isSuccessful) {
                        onSuccessTranslatingLabels(translatedLabels, it, graphicOverlay)
                    } else {
                        //Error when translating. Add english name
                        translatedLabels.add(label.text)
                    }
                }
            }
        }
        return translatedLabels
    }

    private fun onSuccessTranslatingLabels(
        translatedLabels: MutableList<String>,
        it: Task<String>,
        graphicOverlay: GraphicOverlay
    ) {

        translatedLabels.add(it.result!!)
        for (translatedLabel in translatedLabels) {
            if (translatedLabel.toLowerCase().contains(objectToSearch.toLowerCase())) {
                print("Encontro el objeto")
                hasFoundObject = true
                break
            }
        }
        var labelGraphic: LabelGraphic
        if (!hasFoundObject) {
            labelGraphic = LabelGraphic(graphicOverlay, translatedLabels, objectToSearch)
        } else {
            //Object found by the user
            //ToDo: Prise the user with different phrase, save points somewhere, make a sound!
            labelGraphic = LabelGraphic(graphicOverlay, emptyList(), "Congrats! you found it!!")

            //ToDo: can we stop processing frames here for a while???. BUG: is user keeps the camera in the object congrat label overlaps with the other label
            Timer("SettingUp", false).schedule(CONGRATING_USER_TIME) {
                //ToDo: pick next object from a list!
                hasFoundObject = false
            }

        }
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
