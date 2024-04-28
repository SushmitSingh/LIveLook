package com.tooncoder.livelook

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.service.controls.ControlsProviderService
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var adView: AdView
    private val viewModel: LocationViewModel by viewModels()

    private lateinit var mapView: MapView
    private var isTorchOn: Boolean = false

    private lateinit var cameraView: PreviewView
    private lateinit var ivFlash: ImageView
    private lateinit var ivRatioToggel: ImageView
    private lateinit var ivCameraToggel: ImageView
    private lateinit var googleMap: GoogleMap
    private lateinit var cardView: CardView;
    private var cameraControl: CameraControl? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var zoomSeekBar: SeekBar
    private var isBackCameraSelected = true // Flag to track the currently selected camera
    private var isSquareRatio = false
    private var currentAspectRatio = AspectRatio.RATIO_16_9 // Default aspect ratio
    private lateinit var soundPool: SoundPool;
    private var soundId: Int = 0

    //// permission code
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NOTIFICATION_POLICY
    )
    private val PERMISSION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkPermissions()) { // Initialize SoundPool

            adView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
            }
            soundId = soundPool.load(this, R.raw.cam_sound, 1)
            mapView = findViewById(R.id.mapView)
            ivRatioToggel = findViewById(R.id.ivRatioToggel)
            cameraView = findViewById(R.id.cameraView)
            ivFlash = findViewById(R.id.ivFlash)
            zoomSeekBar = findViewById(R.id.zoomSeekbar)
            ivCameraToggel = findViewById(R.id.ivCameraToggel)
            mapView.onCreate(savedInstanceState)

            mapView.getMapAsync(this)



            //Switch Camera to MapView
//        val btnStartCamera: Button = findViewById(R.id.btnStartCamera)
//        btnStartCamera.setOnClickListener {
//            //if map is visible, hide it and show camera view and vice versa
//            if (mapView.visibility == View.VISIBLE) {
//                mapView.visibility = View.GONE
//                cameraView.visibility = View.VISIBLE
//                startCamera()
//                btnStartCamera.text = "Switch to Map"
//            } else {
//                mapView.visibility = View.VISIBLE
//                cameraView.visibility = View.GONE
//                btnStartCamera.text = "Switch to Camera"
//            }
//        }

//        val btnStartLocationUpdates: Button = findViewById(R.id.btnStartLocationUpdates)
//        btnStartLocationUpdates.setOnClickListener {
//
//            viewModel.startLocationUpdates()
//        }

            viewModel.locationUpdates.observe(this, Observer { location ->
                updateMap(location)
            })
            viewModel.startLocationUpdates()

            if (mapView.visibility == View.VISIBLE) {
                mapView.visibility = View.GONE
                cameraView.visibility = View.VISIBLE
                startCamera()
//            btnStartCamera.text = "Switch to Map"
            } else {
                mapView.visibility = View.VISIBLE
                cameraView.visibility = View.GONE
//            btnStartCamera.text = "Switch to Camera"
            }
            ivFlash.setOnClickListener {
                toggleFlash()

            }
            ivCameraToggel.setOnClickListener {
                // Toggle between front and back cameras
                isBackCameraSelected = !isBackCameraSelected
                startCamera()
            }
            ivRatioToggel.setOnClickListener {
                isSquareRatio = !isSquareRatio // Toggle the ratio flag

                // Set camera ratio based on the flag
                if (isSquareRatio) {
                    currentAspectRatio=AspectRatio.RATIO_16_9
                    startCamera()// Set 1:1 ratio
                } else {
                    currentAspectRatio=AspectRatio.RATIO_4_3 // Set 1:1 ratio
                    startCamera()
                    // Set 3:4 ratio
                }
            }

            zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    handleZoom(p1)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })
            startCamera()

        } else {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }

    }

    private fun checkPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                // All permissions granted, execute your code here
                // For example: startApp()
            } else {
                // Not all permissions granted, show dialog
                showPermissionRequiredDialog()
            }
        }
    }

    private fun showPermissionRequiredDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permissions Required")
        dialog.setMessage("Permissions are required for this feature. Please grant the permissions in the app settings.")
        dialog.setPositiveButton("Go to Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        dialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            finish() // Close the activity if permissions are not granted
        }
        dialog.show()
    }

    private fun toggleFlash() {
        if (isTorchOn){
            ivFlash.setImageResource(R.drawable.ic_flash_off)
        }else{
            ivFlash.setImageResource(R.drawable.ic_flash_on)
        }
        cameraControl?.let { control ->
            isTorchOn = !isTorchOn // Toggle torch state
            control.enableTorch(isTorchOn)
        }
    }
    fun Context.dpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    private fun startCamera() {

        val width = cameraView.width
        val height = cameraView.height


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //Get lat and long from viewModel and pass it to overlay
        viewModel.locationUpdates.observe(this, Observer { location ->
            // Remove any existing TextViews from cameraView
            // Remove all CardViews from cameraView
            for (i in 0 until cameraView.childCount) {
                val childView = cameraView.getChildAt(i)
                if (childView is CardView) {
                    cameraView.removeView(childView)
                }
            }
            cardView = CardView(this).apply {
                // Set layout parameters to wrap_content
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT // Set height to wrap_content
                ).apply {
                    // Align to bottom of parent
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

                    // Set margin
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.card_margin_bottom)
                    topMargin = resources.getDimensionPixelSize(R.dimen.card_margin_bottom)
                }
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.transparent_black_60))

                // Set other attributes as needed
                // radius = resources.getDimensionPixelSize(R.dimen.card_corner_radius).toFloat()
            }
            var addressUtil: AddressUtil =AddressUtil(this)
            var fullAddress:String="Latitude: ${location.latitude}\n" +
                    "Longitude: ${location.longitude}"
            var textView= TextView(this).apply {
                text = fullAddress
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                gravity =  Gravity.CENTER_HORIZONTAL or Gravity.TOP
                textSize=14.0f
            }
            val marginInDp = 5 // Margin in dp
            val marginInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginInDp.toFloat(),
                resources.displayMetrics
            ).toInt()

            val layoutParamsBottom = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                leftMargin = marginInPx*3 // Set bottom margin
                rightMargin = marginInPx*3 // Set bottom margin
                // Set bottom margin
            }
            var textView1= TextView(this).apply {
                text = "${addressUtil.getAddress(location.latitude,location.longitude)}"
                setTextColor(ContextCompat.getColor(context,R.color.yellow))
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                layoutParams=layoutParamsBottom
                textSize= 14.0f
                maxLines=3

            }
            val linearLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER





            }
            val imageView = ImageView(this@MainActivity)
            // Set the image resource or other properties of the ImageView as needed
            imageView.setImageResource(R.drawable.icon_ic)

            // Set the height and width of the ImageView
            val params = LinearLayout.LayoutParams(
                dpToPx(60), // 30dp converted to pixels
                dpToPx(60)
            )
            imageView.layoutParams = params
            val mainLinearLayout=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL


            }
            val imageLinearLayout=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL
            }
            val txtAppName = TextView(this@MainActivity).apply {
                text = "Livelook"
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }
            imageLinearLayout.addView(imageView)
            imageLinearLayout.addView(txtAppName)

            linearLayout.addView(textView)
            linearLayout.addView(textView1)
            mainLinearLayout.addView(imageLinearLayout)
            mainLinearLayout.addView(linearLayout)
            cardView.addView(mainLinearLayout)
            cameraView.addView(cardView)

        })

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras 1to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    val viewFinder = cameraView
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = if (isBackCameraSelected) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera and get CameraControl
                cameraControl = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                ).cameraControl
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )


            } catch (exc: Exception) {
                Log.e(ControlsProviderService.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
        val btnCapture: ImageButton = findViewById(R.id.csptureImage)
        btnCapture.setOnClickListener {
            // Capture image
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(getOutputFile()).build()
            imageCapture?.takePicture(
                outputFileOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        compositeOverlayAndImage(cardView)
                        Log.d("TAG", "Image saved successfully: ${outputFileResults.savedUri}")

                    }

                    override fun onError(exception: ImageCaptureException) {
                        // Image capture failed
                        Log.e("TAG", "Error capturing image: ${exception.message}", exception)
                    }
                })
        }

    }
    private fun handleZoom(progress: Int) {
        // Calculate the zoom level based on the SeekBar progress
        val maxZoom = 4.0f // Maximum zoom level
        val minZoom = 1.0f // Minimum zoom level
        val zoomLevel = minZoom + (maxZoom - minZoom) * (progress / 100f)

        // Set the zoom level using CameraControl
        cameraControl?.setZoomRatio(zoomLevel)
    }
    private fun compositeOverlayAndImage(cardView:CardView) {

        val cameraBitmap = cameraView.bitmap

        // Create a new bitmap with the same dimensions as the camera view bitmap
        val resultBitmap = cameraBitmap?.let {
            Bitmap.createBitmap(
                it.width, cameraBitmap.height, Bitmap.Config.ARGB_8888
            )
        }

        // Draw the camera view bitmap onto the result bitmap's canvas
        val canvas = resultBitmap?.let { Canvas(it) }
        if (cameraBitmap != null) {
            if (canvas != null) {
                canvas.drawBitmap(cameraBitmap, 0f, 0f, null)
            }
        }

        // Iterate through the child views of cameraView to find the CardView


        // Draw the CardView onto the canvas
        if (canvas != null) {
            drawViewToCanvas(cardView, canvas)
        }



        // Save the composite image
        if (resultBitmap != null) {
            saveCompositeImage(resultBitmap)
            saveImageToAppFolder(this@MainActivity,resultBitmap)
        }
    }
    private fun drawViewToCanvas(view: CardView, canvas: Canvas) {
        // Measure and layout the view

//        view.measure( resources.getDimensionPixelSize(R.dimen.card_width),
//            resources.getDimensionPixelSize(R.dimen.card_height))
//        view.layout(0, 0, resources.getDimensionPixelSize(R.dimen.card_width),
//            resources.getDimensionPixelSize(R.dimen.card_height))
        val cardWidth =  canvas.width
        val cardHeight = view.height
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height

// Calculate left position to center the view horizontally at the bottom of the canvas
        val left = (canvasWidth - cardWidth) / 2

// Calculate top position to place the view at the bottom of the canvas
        val top = canvasHeight - cardHeight


        // Draw the view onto the canvas
        canvas.translate(left.toFloat(), top.toFloat())
// Set the position for the view
        view.layout(left, top, left + cardWidth, top + cardHeight)

// Draw the view onto the canvas
        view.draw(canvas)
    }
    private fun saveCompositeImage(bitmap: Bitmap) {
        val outputFile = getOutputFile()
        try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Log.d("TAG", "Composite image saved successfully: ${outputFile.absolutePath}")
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                val intent = Intent(this, DisplayImageActivity::class.java)
                intent.putExtra("imageUri", outputFile.absolutePath)
                startActivity(intent)
            }
        } catch (e: IOException) {
            Log.e("TAG", "Error saving composite image: ${e.message}", e)
        }
    }

    fun saveImageToAppFolder(context: Context, bitmap: Bitmap): Boolean {
        // Check if external storage is available
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return false
        }

        // Get the directory for saving images
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val folder = File(directory, context.getString(R.string.app_name))
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // Create a file name for the image
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(folder, fileName)

        // Save the bitmap to the file
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Insert image into the MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/${context.getString(R.string.app_name)}")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            outputStream?.close()
        }
    }

    fun saveImageToCameraFolder(context: Context, bitmap: Bitmap): Boolean {
        // Check if external storage is available
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return false
        }

        // Get the directory for saving images
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val folder = File(directory, "Camera")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // Create a file name for the image
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(folder, fileName)

        // Save the bitmap to the file
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Insert image into the MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            outputStream?.close()
        }
    }
    private fun getOutputFile(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return File(mediaDir, "${System.currentTimeMillis()}.jpg")
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
    }

    private fun updateMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        if (checkLocationPermission()) {
            googleMap.isMyLocationEnabled = true
            googleMap.addMarker(MarkerOptions().position(latLng).title("User Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        } else {
            showPermissionDeniedDialog()
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Denied")
            .setMessage("Permissions are necessary for the app to function. Please enable them in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->

            }
            .show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }





}
