package com.example.minutos.ui.listNearbyPlaces

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minutos.BuildConfig
import com.example.minutos.R
import com.example.minutos.adapter.AdapterPlaces
import com.example.minutos.databinding.ActivityNearbyPlacesBinding
import com.example.minutos.models.Places
import com.example.minutos.ui.detailsPlace.DetailsPlaceActivity
import com.example.minutos.ui.favoritesPlaces.FavoritesPlacesActivity
import com.example.minutos.util.Constants.Companion.KEY_API
import com.example.minutos.util.Dialog
import com.example.minutos.util.Features
import com.example.minutos.util.NetworkResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject


@AndroidEntryPoint
class NearbyPlacesActivity : AppCompatActivity(), MultiplePermissionsListener, View.OnClickListener {

    private val TAG = "NearbyPlacesActivity"

    //Permissions
    private var allPermissionsListener: MultiplePermissionsListener? = null

    //Binding
    private lateinit var binding: ActivityNearbyPlacesBinding

    //ViewModel
    @Inject
    lateinit var nearbyPlacesViewModel: NearbyPlacesViewModel

    //Features
    private val features by lazy { Features() }

    //Dialog
    private val dialog by lazy { Dialog(this@NearbyPlacesActivity) }

    //Fused Location Provider API
    private val fusedLocationClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(applicationContext) }

    // Allows Cancel Location Request
    private var cancellationTokenSource = CancellationTokenSource()

    //Location
    private var location: Location? = null

    //Adapter
    private val adapterPlacesNearby by lazy { AdapterPlaces(arrayListOf()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNearbyPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.recyclerViewPlacesNearby.adapter = adapterPlacesNearby
        binding.recyclerViewPlacesNearby.layoutManager = LinearLayoutManager(this@NearbyPlacesActivity)
        binding.recyclerViewPlacesNearby.hasFixedSize()

        adapterPlacesNearby.placesClicklistener = object : AdapterPlaces.PlacesClicklistener{
            override fun onPlacesClick(place: Places, position: Int, view: View) {

                if (features.checkPermission(this@NearbyPlacesActivity)) {

                    if(features.isConnected(this@NearbyPlacesActivity)){

                        val intent = Intent(this@NearbyPlacesActivity, DetailsPlaceActivity::class.java)
                        intent.putExtra("place", place)
                        startActivity(intent)

                    }else{

                        Toasty.warning(this@NearbyPlacesActivity, "No tienes conexion a internet.", Toast.LENGTH_LONG, true).show()

                    }

                }else{

                    showSnackbar("El permiso fue denegado, pero es necesario para la funcionalidad principal.", "Ajustes") {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }

                }

            }
        }

        if (features.checkPermission(this@NearbyPlacesActivity)) {

            requestCurrentLocation()

        }else{

            checkPermissions()

        }

        binding.floatingActionFavorites.setOnClickListener(this@NearbyPlacesActivity)

    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        currentLocationTask.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                location = task.result
                findPlacesNearby(task.result)
            } else {
                val exception = task.exception
                Toasty.info(this@NearbyPlacesActivity, "Location (failure): $exception", Toast.LENGTH_LONG).show()
            }
        }

    }
    
    private fun findPlacesNearby(result: Location, type: String = ""){

        nearbyPlacesViewModel.getPlaces("${result.latitude},${result.longitude}", "1000000", type, KEY_API)

        nearbyPlacesViewModel.places.observe(this@NearbyPlacesActivity) {

            when(it) {

                is NetworkResult.Success -> {

                    dialog.dismissDialog()

                    if(it.data?.results.isNullOrEmpty()){

                        Toasty.info(this@NearbyPlacesActivity, "Hubo un problema en la peticion.", Toast.LENGTH_LONG, true).show()

                    }else{

                        adapterPlacesNearby.setData(it.data?.results?.sortedBy {
                                obj -> features.distFrom(location?.latitude!!.toFloat(), location?.longitude!!.toFloat(), obj.geometry?.location?.lat!!.toFloat(), obj.geometry?.location?.lng!!.toFloat()) }
                                as MutableList<Places>, location)

                    }

                }

                is NetworkResult.Error -> {

                    dialog.dismissDialog()

                    it.message?.let { it1 -> Toasty.error(this@NearbyPlacesActivity, it1, Toast.LENGTH_LONG, true).show() }

                }

                is NetworkResult.Loading -> {

                    dialog.showDialog()

                }

            }

        }

    }

    override fun onClick(p0: View?) {

        when(p0?.id){

            R.id.floatingActionFavorites -> {

                val intent = Intent(this@NearbyPlacesActivity, FavoritesPlacesActivity::class.java)
                startActivity(intent)

            }

        }

    }

    private fun showSnackbar(mainTextString: String, actionString: String, listener: View.OnClickListener) {
        Snackbar
            .make(findViewById(android.R.id.content), mainTextString, Snackbar.LENGTH_INDEFINITE)
            .setAction(actionString, listener).show()
    }

    private fun checkPermissions(){

        val dialogMultiplePermissionsListener: MultiplePermissionsListener =
            DialogOnAnyDeniedMultiplePermissionsListener.Builder
                .withContext(this@NearbyPlacesActivity)
                .withTitle("Permisos")
                .withMessage("Los permisos solicitados son necesarios para las funciones de la app.")
                .withButtonText("Ok")
                .withIcon(R.mipmap.ic_launcher)
                .build()

        allPermissionsListener = CompositeMultiplePermissionsListener(
            this@NearbyPlacesActivity,
            dialogMultiplePermissionsListener
        )

        Dexter.withActivity(this@NearbyPlacesActivity)
            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            .withListener(allPermissionsListener)
            .check()

    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report != null) {
            if (report.areAllPermissionsGranted()){
                requestCurrentLocation()
            }else{
                showSnackbar("El permiso fue denegado, pero es necesario para la funcionalidad principal.", "Ajustes") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {}

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

}