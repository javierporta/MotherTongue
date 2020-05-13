package com.ipleiria.mothertongue.object_detection

import VisionProcessorBase
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.translations.TranslatorService
import com.ipleiria.mothertongue.utils.CameraImageGraphic
import com.ipleiria.mothertongue.utils.FrameMetadata
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule


/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor(
    val liveCameraContext: Context,
    val targetLanguage: Int,
    var objectsToSearch: List<GamePhrase>
) :
    VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    var hasFoundObject = false
    val CONGRATING_USER_TIME = 5000L //MS
    val mediaPlayer: MediaPlayer = MediaPlayer.create(this.liveCameraContext, R.raw.success_sound)

    lateinit var currentObjectToSearch: GamePhrase

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
        //get first object to search
        getNextPhrase()

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
            if (translatedLabel.toLowerCase()
                    .contains(currentObjectToSearch.phrase.toLowerCase())
            ) {
                print("Encontro el objeto")
                hasFoundObject = true
                break
            }
        }
        var labelGraphic: LabelGraphic
        if (!hasFoundObject) {
            labelGraphic =
                LabelGraphic(graphicOverlay, translatedLabels, currentObjectToSearch.phrase)
        } else {
            //Object found by the user
            //ToDo: Prise the user with different phrase, save points somewhere
            labelGraphic = LabelGraphic(graphicOverlay, emptyList(), "Congrats! you found it!!")

            this.markCurrentPhraseAsGuessed();

            //Play a sound
            mediaPlayer.start()

            //ToDo: can we stop processing frames here for a while???. BUG: is user keeps the camera in the object congrat label overlaps with the other label.
            //ToDO: BUG UPDATE: This will not be a problem when we change the object to be searched
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

    fun getNextPhrase() {
        try {

            //Get the first phrase that was not guessed
            this.currentObjectToSearch = objectsToSearch.first { !it.wasGuessed }

        } catch (ex: java.lang.Exception) {
            //User guessed all phrases
            // ToDo: go back to activity
            print("Go back!")
        }
    }

    fun markCurrentPhraseAsGuessed() {
        //ToDo write in a persistant store
        this.currentObjectToSearch.wasGuessed = true
        getNextPhrase()
    }

    companion object {

        private const val TAG = "ImageLabelingProcessor"
    }


}
