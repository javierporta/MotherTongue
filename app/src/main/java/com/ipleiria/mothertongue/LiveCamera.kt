package com.ipleiria.mothertongue

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.ipleiria.mothertongue.automl.AutoMLImageLabelerProcessor
import com.ipleiria.mothertongue.camera.CameraSource
import com.ipleiria.mothertongue.camera.CameraSourcePreview
import com.ipleiria.mothertongue.databinding.ActivityLiveCameraBinding
import com.ipleiria.mothertongue.object_detection.ImageLabelingProcessor
import com.ipleiria.mothertongue.object_detection.ObjectRecognitionProcessor
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException
import java.util.*


class LiveCamera : AppCompatActivity() {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null

    private val PERMISSION_REQUESTS = 1
    private val OBJECT_DETECTION = "Object Detection"
    val selectedModel = OBJECT_DETECTION

    private lateinit var binding: ActivityLiveCameraBinding

    private val TAG = "LiveCamera"

    private var firebaseSelectedLanguageEnum: Int = 0

    //endregion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get intent param
        firebaseSelectedLanguageEnum = intent.getIntExtra("firebaseSelectedLanguageEnum", 0)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_camera)
        //FirebaseApp.initializeApp(this);

        preview = binding.cameraSourcePreview;
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = binding.graphicsOverlay
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }


        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
            startCameraSource()
        } else {
            getRuntimePermissions()
        }
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
            cameraSource = CameraSource(this, graphicOverlay!!)
            cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
        }
        Log.i(
            TAG,
            "Using ImageLabelingProcessor"
        )
        try {
            cameraSource?.setMachineLearningFrameProcessor(
                ImageLabelingProcessor(
                    this,
                    firebaseSelectedLanguageEnum, "Taza" //WARNING, hardcoded value to test
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
            if (!isPermissionGranted(this, permission.toString())) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions: MutableList<String> =
            ArrayList()
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
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
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
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
