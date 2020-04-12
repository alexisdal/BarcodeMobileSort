package com.example.barcodemobilesort

// vibrate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner



class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
        const val SUCCESS_MSG="msg"
        private var score = 0
    }
    private lateinit var textureView: TextureView

    @Volatile var acceptBarcode: Boolean = true

    private lateinit var chrono: Chronometer
    //private var running: Boolean = false
    //private static var score: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // disable scren timeout


        val msg=intent.getStringExtra(SUCCESS_MSG)
        if (msg == "SUCCESS") { MainActivity.score += 1 }


        val t: TextView = findViewById(R.id.main_text_label)
        t.text = "Score: ${MainActivity.score}"
        Log.d("TAG", "score: ${MainActivity.score}")

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
            //barCodes.forEach {
            //    Log.d("TAG", "BarCode detected: ${it.rawValue}. $acceptBarcode")
            //}
            if (barCodes.count() > 0 && acceptBarcode) {
                val ean = barCodes[0].rawValue
                Log.d("TAG", "BarCode detected: $ean")
                val intent: Intent = Intent(applicationContext, Put2ShelfActivity::class.java)
                //intent.putExtra(Put2ShelfActivity.EAN_MSG, eanToSend)
                intent.putExtra(Put2ShelfActivity.EAN_MSG, ean)
                startActivity(intent)
                acceptBarcode = false
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



}
