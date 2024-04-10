package com.tooncoder.livelook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var mapView: MapView

    private lateinit var cameraView: PreviewView
    private lateinit var googleMap: GoogleMap

    private var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        mapView = findViewById(R.id.mapView)
        cameraView = findViewById(R.id.cameraView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        //Switch Camera to MapView
        val btnStartCamera: Button = findViewById(R.id.btnStartCamera)
        btnStartCamera.setOnClickListener {
            //if map is visible, hide it and show camera view and vice versa
            if (mapView.visibility == View.VISIBLE) {
                mapView.visibility = View.GONE
                cameraView.visibility = View.VISIBLE
                startCamera()
                btnStartCamera.text = "Switch to Map"
            } else {
                mapView.visibility = View.VISIBLE
                cameraView.visibility = View.GONE
                btnStartCamera.text = "Switch to Camera"
            }
        }

        val btnStartLocationUpdates: Button = findViewById(R.id.btnStartLocationUpdates)
        btnStartLocationUpdates.setOnClickListener {
            viewModel.startLocationUpdates()
        }

        viewModel.locationUpdates.observe(this, Observer { location ->
            updateMap(location)
        })
    }

    private fun startCamera() {

        val width = cameraView.width
        val height = cameraView.height


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //Get lat and long from viewModel and pass it to overlay
        viewModel.locationUpdates.observe(this, Observer { location ->
            cameraView.addView(TextView(this).apply {
                text = "Latitude: ${location.latitude},\nLongitude: ${location.longitude}"
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            })
        })

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    val viewFinder = cameraView
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))


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
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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

    fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setCameraExecutor(ContextCompat.getMainExecutor(this))
            .build()
    }
}
