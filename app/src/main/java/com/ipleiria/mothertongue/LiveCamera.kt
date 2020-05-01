package com.ipleiria.mothertongue

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.ipleiria.mothertongue.automl.AutoMLImageLabelerProcessor
import com.ipleiria.mothertongue.camera.CameraSource
import com.ipleiria.mothertongue.camera.CameraSourcePreview
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

    private val TAG = "LiveCamera"

    //endregion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_camera)

        //FirebaseApp.initializeApp(this);

        //ToDo: ADD BINDING!
        preview = findViewById<CameraSourcePreview>(R.id.camera_source_preview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById<GraphicOverlay>(R.id.graphics_overlay);
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
        //ToDo: Add object detecto here
        //cameraSource.setMachineLearningFrameProcessor(TextRecognitionProcessor())
        Log.i(
            TAG,
            "Using Object Detector Processor"
        )
        try {
            val objectDetectorOptions =
                FirebaseVisionObjectDetectorOptions.Builder()
                    .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                    .enableClassification().build()
            //object processor
//            cameraSource!!.setMachineLearningFrameProcessor(
//                ObjectRecognitionProcessor(
//                    objectDetectorOptions
//                )
//            )
            //autoML
            //cameraSource?.setMachineLearningFrameProcessor(AutoMLImageLabelerProcessor(this, AutoMLImageLabelerProcessor.Mode.LIVE_PREVIEW))
            //ToDo: JOin with object processor
            //Todo: Try autoML
            cameraSource?.setMachineLearningFrameProcessor(ImageLabelingProcessor())

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
