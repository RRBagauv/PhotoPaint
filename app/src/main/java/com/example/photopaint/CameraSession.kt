package com.example.photopaint

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.photopaint.Constants.REQUEST_CAMERA
import com.example.photopaint.paintPhoto.paintActivity
import com.example.photopaint.paintPhoto.paintPhoto
import com.example.photopaint.paintPhoto.paintPhoto2
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CameraSession(

    private val activity: Activity,
    private val textureView: TextureView

){
    private val cameraManager: CameraManager =
        activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager


    companion object {
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private var cameraDevice: CameraDevice? = null
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            previewWidth = width
            previewHeight = height
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            previewWidth = width
            previewHeight = height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView.surfaceTexture
            texture?.setDefaultBufferSize(previewWidth, previewHeight)
            val surface = Surface(texture)
            val captureRequestBuilder =
                cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            val stateCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    captureRequestBuilder?.let { builder ->
                        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                        try {
                            cameraCaptureSession.setRepeatingRequest(
                                builder.build(),
                                null,
                                backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                cameraDevice?.createCaptureSession(
                    SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        listOf(OutputConfiguration(surface)),
                        activity.mainExecutor,
                        stateCallback
                    )
                )
            } else {
                cameraDevice?.createCaptureSession(
                    listOf(surface),
                    stateCallback,
                    null
                )
            }
        } catch (e: CameraAccessException) {
            Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
        }
    }

    private fun openCamera() {
        try {
            val backCameras = cameraManager.cameraIdList.filter {
                cameraManager.getCameraCharacteristics(it)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            if (backCameras.isNullOrEmpty()) {
                throw NullPointerException("Back facing camera not found")
            }
            val cameraId = backCameras.first()
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
                return
            }
            cameraManager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
        } catch (e: NullPointerException) {
            Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
        }
    }

   fun takePhoto() : String? {
       cameraDevice?.let { cameraDevice ->
           try {
               val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
               val jpegSizes = characteristics
                   .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                   ?.getOutputSizes(ImageFormat.JPEG)
               var width = 640
               var height = 480
               if (!jpegSizes.isNullOrEmpty()) {
                   width = jpegSizes[0].width
                   height = jpegSizes[0].height
               }
               val reader: ImageReader =
                   ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
               val outputSurfaces: MutableList<Surface> = ArrayList(2)
               outputSurfaces.add(reader.surface)
               outputSurfaces.add(Surface(textureView.surfaceTexture))
               val captureBuilder =
                   cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
               captureBuilder.addTarget(reader.surface)
               captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

               //region Orientation
               val rotation = activity.windowManager.defaultDisplay.rotation
               captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
               //Save
               val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.getDefault())
               val name = "/" + simpleDateFormat.format(Calendar.getInstance().time) + ".jpg"
               val file = File(
                   activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(),
                   name
               )
               val readerListener: ImageReader.OnImageAvailableListener =
                   object : ImageReader.OnImageAvailableListener {
                       override fun onImageAvailable(reader: ImageReader) {
                           var image: Image? = null
                           try {
                               image = reader.acquireLatestImage()
                               val buffer: ByteBuffer = image.planes[0].buffer
                               val bytes = ByteArray(buffer.capacity())
                               buffer.get(bytes)
                               save(bytes)

                               Intent(activity.baseContext, paintActivity::class.java).let {
                                   it.putExtra("filename", file.absolutePath)
                                   activity.startActivity(it)
                               }
                           } catch (e: FileNotFoundException) {
                               Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
                           } catch (e: IOException) {
                               Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
                           } finally {
                               image?.close()
                           }
                       }
                       private fun save(bytes: ByteArray) {
                        FileOutputStream(file).use {
                            it.write(bytes)
                        }
                    }
                   }



               reader.setOnImageAvailableListener(readerListener, backgroundHandler)
               val captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                   override fun onCaptureCompleted(
                       session: CameraCaptureSession,
                       request: CaptureRequest,
                       result: TotalCaptureResult
                   ) {
                       super.onCaptureCompleted(session, request, result)
                       Toast.makeText(activity, "Saved:$file", Toast.LENGTH_SHORT).show()
                       createCameraPreview()
                   }
               }
               cameraDevice.createCaptureSession(
                   outputSurfaces,
                   object : CameraCaptureSession.StateCallback() {
                       override fun onConfigured(session: CameraCaptureSession) {
                           try {
                               session.capture(
                                   captureBuilder.build(),
                                   captureListener,
                                   backgroundHandler
                               )
                           } catch (e: CameraAccessException) {
                               e.printStackTrace()
                           }
                       }

                       override fun onConfigureFailed(session: CameraCaptureSession) {}
                   },
                   backgroundHandler
               )
               return name
           } catch (e: CameraAccessException) {
               e.printStackTrace()
           }
       }
       return null
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background")
        backgroundThread?.start()
        backgroundThread?.let { thread ->
            backgroundHandler = Handler(thread.looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun sessionPause() {
        cameraDevice?.close()
        textureView.surfaceTextureListener = null
    }

    fun sessionResume() {


        if(textureView.isAvailable){
            openCamera()
        }else{
            textureView.surfaceTextureListener = textureListener
        }

    }

}