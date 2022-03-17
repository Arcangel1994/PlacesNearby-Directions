package com.example.minutos.ui.favoritesPlaces

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minutos.BuildConfig
import com.example.minutos.adapter.AdapterPlaces
import com.example.minutos.databinding.ActivityFavoritesPlacesBinding
import com.example.minutos.models.Places
import com.example.minutos.ui.detailsPlace.DetailsPlaceActivity
import com.example.minutos.util.Dialog
import com.example.minutos.util.Features
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesPlacesActivity : AppCompatActivity() {

    private val TAG = "FavoritesPlacesActivity"

    //Binding
    private lateinit var binding: ActivityFavoritesPlacesBinding

    //ViewModel
    @Inject
    lateinit var favoritesPlacesViewModel: FavoritesPlacesViewModel

    //Features
    private val features by lazy { Features() }

    //Dialog
    private val dialog by lazy { Dialog(this@FavoritesPlacesActivity) }

    //Adapter
    private val adapterPlacesFavorites by lazy { AdapterPlaces(arrayListOf()) }

    //Fused Location Provider API
    private val fusedLocationClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(applicationContext) }

    // Allows Cancel Location Request
    private var cancellationTokenSource = CancellationTokenSource()

    //Location
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFavoritesPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.recyclerViewPlacesFavorites.adapter = adapterPlacesFavorites
        binding.recyclerViewPlacesFavorites.layoutManager = LinearLayoutManager(this@FavoritesPlacesActivity)
        binding.recyclerViewPlacesFavorites.hasFixedSize()

        adapterPlacesFavorites.placesClicklistener = object : AdapterPlaces.PlacesClicklistener{
            override fun onPlacesClick(place: Places, position: Int, view: View) {

                if (features.checkPermission(this@FavoritesPlacesActivity)) {

                    if(features.isConnected(this@FavoritesPlacesActivity)){

                        val intent = Intent(this@FavoritesPlacesActivity, DetailsPlaceActivity::class.java)
                        intent.putExtra("place", place)
                        startActivity(intent)

                    }else{

                        Toasty.warning(this@FavoritesPlacesActivity, "No tienes conexion a internet.", Toast.LENGTH_LONG, true).show()

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

        dialog.showDialog()

        requestCurrentLocation()

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
                findPlacesFavorites(task.result)
            } else {
                val exception = task.exception
                Toasty.info(this@FavoritesPlacesActivity, "Location (failure): $exception", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun findPlacesFavorites(result: Location) {

        favoritesPlacesViewModel.favorites.observe(this@FavoritesPlacesActivity) {

            it.let {

                if(!it.isNullOrEmpty()){
                    adapterPlacesFavorites.setData(it as MutableList<Places>, result)
                    binding.shapeImageNotFavorites.visibility = View.GONE
                    binding.materialTextNotFavorites.visibility = View.GONE
                }else{
                    adapterPlacesFavorites.setData(arrayListOf())
                    binding.shapeImageNotFavorites.visibility = View.VISIBLE
                    binding.materialTextNotFavorites.visibility = View.VISIBLE
                }

                dialog.dismissDialog()

            }

        }

    }

    private fun showSnackbar(mainTextString: String, actionString: String, listener: View.OnClickListener) {
        Snackbar
            .make(findViewById(android.R.id.content), mainTextString, Snackbar.LENGTH_INDEFINITE)
            .setAction(actionString, listener).show()
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

}