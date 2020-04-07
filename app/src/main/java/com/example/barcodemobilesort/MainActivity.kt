package com.example.barcodemobilesort

// vibrate

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
    private lateinit var textureView: TextureView

    @Volatile var acceptBarcode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.texture_view)
        // Request camera permissions
        if (isCameraPermissionGranted()) {
            textureView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE), REQUEST_CAMERA_PERMISSION)
        }


    }

    override fun onResume() {
        super.onResume()
        acceptBarcode = true
        Log.d("TAG", "onResume()")
    }

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder()
            // We want to show input from back camera of the device
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = textureView.parent as ViewGroup //https://stackoverflow.com/questions/56064248/android-camerax-doesnt-show-anything
            parent.removeView(textureView)
            textureView.surfaceTexture = previewOutput.surfaceTexture
            parent.addView(textureView, 0)
            //updateTransform()
        }

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

        val qrCodeAnalyzer = BarCodeAnalyzer { barCodes ->
            barCodes.forEach {
                Log.d("TAG", "BarCode detected: ${it.rawValue}. $acceptBarcode")
            }
            if (barCodes.count() > 0 && acceptBarcode) {
                val intent: Intent = Intent(applicationContext, Put2ShelfActivity::class.java)
                //intent.putExtra(Put2ShelfActivity.EAN_MSG, eanToSend)
                intent.putExtra(Put2ShelfActivity.EAN_MSG, barCodes[0].rawValue)
                startActivity(intent)
                acceptBarcode = false
                // vib
                vibrate()
                // beep
                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 150)
                //toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }
        }

        imageAnalysis.analyzer = qrCodeAnalyzer

        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imageAnalysis)
    }

    private fun isCameraPermissionGranted(): Boolean {
        val selfPermission = ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
        return selfPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (isCameraPermissionGranted()) {
                textureView.post { startCamera() }
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Extension property to check whether device has Vibrator
    val Context.hasVibrator:Boolean
        get() {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            return vibrator.hasVibrator()
        }

    // Extension method to vibrate a phone programmatically
    fun Context.vibrate(milliseconds:Long = 500){
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check whether device/hardware has a vibrator
        val canVibrate:Boolean = vibrator.hasVibrator()

        if(canVibrate){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                // void vibrate (VibrationEffect vibe)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        // The default vibration strength of the device.
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }else{
                // This method was deprecated in API level 26
                vibrator.vibrate(milliseconds)
            }
        }
    }

}
