package com.example.barcodemobilesort

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.random.Random
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
    private lateinit var textureView: TextureView

    lateinit var sortButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.texture_view)
        // Request camera permissions
        if (isCameraPermissionGranted()) {
            textureView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        sortButton = findViewById<Button>(R.id.sortButton)
        sortButton.setOnClickListener {

            // change ean...
            val r = Random.nextDouble()
            var eanToSend:String ="123456"
            if (r < 0.33333) {
                eanToSend = "9782290215715"
            } else if (r < 0.6666) {
                eanToSend = "8076809574631"
            }
            //Log.d("TAG", "$r $eanToSend") // displayed each time the screen is shown

            // Random.nextDouble() does create a value between 0 -> 1
            //for (i in 0..100) {
            //    val x = Random.nextDouble()
            //    Log.d("TAG", "$x") // displayed each time the screen is shown
            //}



            val intent: Intent = Intent(applicationContext, Put2ShelfActivity::class.java)
            intent.putExtra(Put2ShelfActivity.EAN_MSG, eanToSend)
            startActivity(intent)

        }

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
