package com.ipleiria.mothertongue

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ipleiria.mothertongue.camera.CameraSource
import com.ipleiria.mothertongue.camera.CameraSourcePreview
import com.ipleiria.mothertongue.constants.Phrases
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.ActivityLiveCameraBinding
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.object_detection.ImageLabelingProcessor
import com.ipleiria.mothertongue.utils.GraphicOverlay
import java.io.IOException


class LiveCamera : AppCompatActivity() {

    companion object {
        lateinit var mHandler: Handler
        val ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW_KEY: String =
            "ACTION_UPDATE_CURRENT_WORD_TEXT_VIEW"
        val ACTION_TOAST_KEY: String = "ACTION_TOAST"
        val ACTION_LEVEL_DONE: String = "LEVEL_DONE"
    }
}
