package com.ansorod.chromafilterdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 12
    private val MANIFEST_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    private lateinit var mTextureView: TextureView
    private var mPreview: Preview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextureView = findViewById(R.id.textureView)

        if(checkPermissions()) {
            mTextureView.post { startCamera() }
        } else {
            requestPermissions()
        }

        mTextureView.addOnLayoutChangeListener(mLayoutListener)
    }

    private fun checkPermissions() = MANIFEST_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() = ActivityCompat.requestPermissions(this, MANIFEST_PERMISSIONS, REQUEST_CODE)

    private fun startCamera() {
        CameraX.unbindAll()

        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        val aspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetAspectRatio(aspectRatio)
            setTargetRotation(mTextureView.display.rotation)
        }.build()

        mPreview = Preview(previewConfig)

        mPreview?.setOnPreviewOutputUpdateListener {
            val parent = mTextureView.parent as ViewGroup
            parent.removeView(mTextureView)
            parent.addView(mTextureView, 0)

            mTextureView.surfaceTexture = it.surfaceTexture
            updateView()
        }

        CameraX.bindToLifecycle(this, mPreview)
    }

    private fun updateView() {
        val matrix = Matrix()

        val centerX = mTextureView.width / 2f
        val centerY = mTextureView.height / 2f

        val rotation = when(mTextureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(-rotation.toFloat(), centerX, centerY)

        mTextureView.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE) {
            if(checkPermissions()) {
                mTextureView.post { startCamera() }
            } else {
                Toast.makeText(this, "Camera permission denied by the user", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private val mLayoutListener: View.OnLayoutChangeListener =
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            updateView()
        }

}
