package com.ipleiria.mothertongue.ui.camera

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.camera.CameraSource
import com.ipleiria.mothertongue.camera.CameraSourcePreview
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.FragmentLiveCameraBinding
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.object_detection.ImageLabelingProcessor
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException


class LiveCamera : Fragment() {

    companion object {
        lateinit var mHandler: Handler
        val ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW_KEY: String =
            "ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW"
        val ACTION_TOAST_KEY: String = "ACTION_TOAST"
        val ACTION_LEVEL_DONE: String = "LEVEL_DONE"
    }

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null

    private val PERMISSION_REQUESTS = 1
    private val OBJECT_DETECTION = "Object Detection"
    val selectedModel = OBJECT_DETECTION

    private lateinit var binding: FragmentLiveCameraBinding

    private val TAG = "LiveCamera"

    private var isLevelDone = false

    private var firebaseSelectedLanguageEnum: Int = 0
    private var gamePhrases: ArrayList<GamePhrase>? = null

    private var lastFeedbackCurrentWord: String? = null
    private var lastFeedbackToast: String? = null
    private var feedbackLevelDone: String? = null


    private lateinit var mediaPlayer: MediaPlayer

    val TIME_TO_STOP_AFTER_FIND = 3000L //MS
    val TIME_TO_STOP_AFTER_GUESS_ALL_WORDS = 5000L //MS


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isLevelDone = false

        binding = DataBindingUtil.inflate<FragmentLiveCameraBinding>(inflater, R.layout.fragment_live_camera, container, false)

        arguments?.let {
            gamePhrases = it.getParcelableArrayList<GamePhrase>("gamePhrases")
            firebaseSelectedLanguageEnum = it.getInt("firebaseSelectedLanguageEnum", 0)
        }

        preview = binding.cameraSourcePreview;
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = binding.graphicsOverlay
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        binding.currentWordTextView.text = "Word to Search"

        initializeProgressBar()


        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
            startCameraSource()
        } else {
            getRuntimePermissions()
        }

        mediaPlayer = MediaPlayer.create(this.activity, R.raw.success_sound)


        return  binding.root
    }


    private fun saveLevelCompleted() {
        var currentGameLevel = Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex]
        currentGameLevel.isComplete = true

    }

    private fun initializeProgressBar() {
        binding.levelProgressBar.max = gamePhrases?.size!!
        binding.levelProgressBar.progress = gamePhrases?.count { it.wasGuessed }!!
    }

    private fun moveProgressBar() {
        binding.levelProgressBar.progress++
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    private fun createCameraSource(model: String) {
        if (cameraSource == null) {
            cameraSource = CameraSource(this.activity, graphicOverlay!!)
            cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
        }
        Log.i(
            TAG,
            "Using ImageLabelingProcessor"
        )
        try {
            cameraSource?.setMachineLearningFrameProcessor(
                ImageLabelingProcessor(
                    this.activity?.applicationContext!!,
                    firebaseSelectedLanguageEnum, gamePhrases!!.toList()
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "can not create camera source: $model")
        }
    }

    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    //Permission section

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this.activity?.applicationContext!!, permission.toString())) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions: MutableList<String> =
            ArrayList()
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this.activity?.applicationContext!!, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            requestPermissions(
                allNeededPermissions.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        Log.i(
            TAG,
            "Permission granted!"
        )
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        }
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
    }

    private fun isPermissionGranted(
        context: Context,
        permission: String
    ): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(
                TAG,
                "Permission granted: $permission"
            )
            return true
        }
        Log.i(
            TAG,
            "Permission NOT granted: $permission"
        )
        return false
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.activity?.packageManager!!
                .getPackageInfo(this.activity?.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.size > 0) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

}
