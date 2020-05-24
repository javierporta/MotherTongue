package com.ipleiria.mothertongue.object_detection

import VisionProcessorBase
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.ipleiria.mothertongue.Game
import com.ipleiria.mothertongue.LiveCamera
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.translations.TranslatorService
import com.ipleiria.mothertongue.utils.CameraImageGraphic
import com.ipleiria.mothertongue.utils.FrameMetadata
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException


/** Custom Image Classifier Demo.  */
class ImageLabelingProcessor(
    val liveCameraContext: Context,
    val targetLanguage: Int,
    var objectsToSearch: List<GamePhrase>
) :
    VisionProcessorBase<List<FirebaseVisionImageLabel>>() {

    var hasFoundObject = false


    var currentObjectToSearch: GamePhrase? = null

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
        if (currentObjectToSearch == null) {
            getNextPhrase()
        }

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
        val translatorService = TranslatorService(targetLanguage)
        for (label: FirebaseVisionImageLabel in labels) {
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
        hasFoundObject = false

        translatedLabels.add(it.result!!)
        for (translatedLabel in translatedLabels) {
            if (translatedLabel.toLowerCase()
                    .contains(currentObjectToSearch?.phrase!!.toLowerCase())
            ) {
                hasFoundObject = true
                break
            }
        }
        var labelGraphic = LabelGraphic(graphicOverlay, translatedLabels)
        if (!hasFoundObject) {
            communicateWithUIThread(
                LiveCamera.ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW_KEY,
                currentObjectToSearch?.phrase!!
            )
        } else {
            //Object found by the user
            //ToDo: Prise the user with different phrase, save points somewhere
            //labelGraphic = LabelGraphic(graphicOverlay, emptyList())
            communicateWithUIThread(LiveCamera.ACTION_TOAST_KEY, "Tostada!")
            this.markCurrentPhraseAsGuessed();
            communicateWithUIThread(
                LiveCamera.ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW_KEY,
                currentObjectToSearch?.phrase!!
            )
        }
        graphicOverlay.add(labelGraphic)
        graphicOverlay.postInvalidate()

    }

    private fun communicateWithUIThread(actionName: String, value: String) {
        val message: Message = LiveCamera.mHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putString(actionName, value)
        message.data = bundle
        LiveCamera.mHandler.sendMessage(message)
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Label detection failed.$e")
    }

    fun getNextPhrase() {
        try {

            if (objectsToSearch.last().wasGuessed) {
                //Level is done, show results and go bak to main activity
                communicateWithUIThread(LiveCamera.ACTION_LEVEL_DONE, "Done")
                return
            }

            //Get the first phrase that was not guessed
            this.currentObjectToSearch = objectsToSearch.first { !it.wasGuessed }

        } catch (ex: java.lang.Exception) {
            print("Go back!")
        }
    }

    fun markCurrentPhraseAsGuessed() {

        //WARNING: This is not the best place to do that, since it is called many times
        this.currentObjectToSearch?.wasGuessed = true
        var gamePhraseToUpdate =
            Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex].gamePhrases.find { x -> x.phrase == this.currentObjectToSearch?.phrase }
        var gamePhraseIndexToUpdate =
            Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex].gamePhrases.indexOf(
                gamePhraseToUpdate
            )

        Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex].gamePhrases[gamePhraseIndexToUpdate].wasGuessed =
            true;

        getNextPhrase()
    }

    companion object {

        private const val TAG = "ImageLabelingProcessor"
    }


}
