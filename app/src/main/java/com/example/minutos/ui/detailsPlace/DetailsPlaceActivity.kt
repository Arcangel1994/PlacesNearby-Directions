package com.example.minutos.ui.detailsPlace

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.minutos.MyApplication
import com.example.minutos.R
import com.example.minutos.databinding.ActivityDetailsPlaceBinding
import com.example.minutos.models.Places
import com.example.minutos.models.Steps
import com.example.minutos.util.Constants.Companion.BASE_URL_PHOTO
import com.example.minutos.util.Constants.Companion.KEY_API
import com.example.minutos.util.Dialog
import com.example.minutos.util.Features
import com.example.minutos.util.NetworkResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import javax.inject.Inject


@AndroidEntryPoint
class DetailsPlaceActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private val TAG = "DetailsPlaceActivity"

    //Binding
    private lateinit var binding: ActivityDetailsPlaceBinding

    //ViewModel
    @Inject
    lateinit var detailsPlaceViewModel: DetailsPlaceViewModel

    //Features
    private val features by lazy { Features() }

    //Dialog
    private val dialog by lazy { Dialog(this@DetailsPlaceActivity) }

    //Place
    var place: Places? = null

    //Maps
    private lateinit var mMap: GoogleMap

    //Fused Location Provider API
    private val fusedLocationClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(applicationContext) }

    // Allows Cancel Location Request
    private var cancellationTokenSource = CancellationTokenSource()

    //Location
    private var location: Location? = null

    //Memu Favorite
    var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        place = intent.getSerializableExtra("place") as Places

        supportActionBar!!.title = place!!.name

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@DetailsPlaceActivity)

        initView()

        detailsPlaceViewModel.getFavorites(place?.place_id.toString())

    }

    private fun initView(){

        binding.materialTextPlaceName.text = place?.name

        if(place?.rating != null){
            binding.ratingBarPlacesRating.rating = place?.rating!!
        }

        if(place?.user_ratings_total != null){
            binding.materialTextPlacesComments.text = place?.user_ratings_total.toString()
        }

        binding.materialTextPlacesVicinity.text = place?.vicinity

        binding.materialTextPlaceLat.text = place?.geometry?.location?.lat

        binding.materialTextPlaceLng.text = place?.geometry?.location?.lng

        if(!place?.photos.isNullOrEmpty()){

            if (!place?.photos?.get(0)?.photo_reference.isNullOrEmpty()){

                Glide.with(MyApplication.context!!)
                    .load(BASE_URL_PHOTO+place?.photos?.get(0)?.photo_reference)
                    .error(R.mipmap.ic_launcher)
                    .centerInside()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.shapeImagePlaceImage)

            }

        }

        binding.floatingActionNavigation.setOnClickListener(this@DetailsPlaceActivity)
        binding.floatingActionOpenMaps.setOnClickListener(this@DetailsPlaceActivity)

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
                findPlacesDirections(task.result)
            } else {
                val exception = task.exception
                Toasty.info(this@DetailsPlaceActivity, "Location (failure): $exception", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun findPlacesDirections(result: Location){

        binding.materialTextPlaceKm.text = "%.2f".format(features.distFrom(result!!.latitude.toFloat(), result!!.longitude.toFloat(),
            place?.geometry!!.location!!.lat!!.toFloat(), place?.geometry!!.location!!.lng!!.toFloat()))+"m"

        detailsPlaceViewModel.getDirections(false, "DRIVING", true, KEY_API, "${result.latitude},${result.longitude}", "${place?.geometry?.location?.lat},${place?.geometry?.location?.lng}")

        detailsPlaceViewModel.directions.observe(this@DetailsPlaceActivity) {

            when(it) {

                is NetworkResult.Success -> {

                    dialog.dismissDialog()

                    if(it.data?.routes.isNullOrEmpty()){

                        Toasty.info(this@DetailsPlaceActivity, "Hubo un problema en la peticion.", Toast.LENGTH_LONG, true).show()

                    }else{


                            binding.materialTextPlacesDistance.text = it.data?.routes?.get(0)?.legs?.get(0)?.distance?.text+" en carro"
                            binding.materialTextPlacesDuration.text = it.data?.routes?.get(0)?.legs?.get(0)?.duration?.text+" en carro"

                            cratePolyline(it.data?.routes?.get(0)?.legs?.get(0)?.steps)

                    }

                }

                is NetworkResult.Error -> {

                    dialog.dismissDialog()

                    it.message?.let { it1 -> Toasty.error(this@DetailsPlaceActivity, it1, Toast.LENGTH_LONG, true).show() }

                }

                is NetworkResult.Loading -> {

                    dialog.showDialog()

                }

            }

        }

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(20.0f);

        val placeNearby = LatLng(place?.geometry?.location?.lat!!.toDouble(), place?.geometry?.location?.lng!!.toDouble())
        mMap.addMarker(MarkerOptions().position(placeNearby).title(place?.name))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeNearby))

    }

    private fun cratePolyline(steps: List<Steps>?) {


        if (!steps.isNullOrEmpty()) {

            for (it in steps) {

                    mMap.addPolyline(
                        PolylineOptions()
                            .add(LatLng(it.start_location?.lat!!.toDouble(), it.start_location?.lng!!.toDouble()), LatLng(it.end_location?.lat!!.toDouble(), it.end_location?.lng!!.toDouble()))
                            .width(5f)
                            .color(Color.RED)
                    )

            }

        }else{

            Toasty.info(this@DetailsPlaceActivity, "Hubo un problema en la creacion de interface", Toast.LENGTH_LONG, true).show()

        }

    }

    override fun onClick(p0: View?) {

        if (p0 != null) {
            when(p0.id){

                R.id.floatingActionNavigation -> {

                    val uri: Uri = Uri.parse("google.navigation:q=${place?.geometry?.location?.lat},${place?.geometry?.location?.lng}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)

                }

                R.id.floatingActionOpenMaps -> {

                    val uri = Uri.parse("geo:0,0?q=${place?.geometry?.location?.lat},${place?.geometry?.location?.lng}(${place?.name})")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)

                }

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_datails, menu)
        menuItem = menu!!.findItem(R.id.favorite)
        if(detailsPlaceViewModel.Isfavorites.value == true){
            menuItem!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_icon_heart)
        }else{
            menuItem!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_icon_heart_outline)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorite -> {

                if(detailsPlaceViewModel.Isfavorites.value == true){
                    detailsPlaceViewModel.deleteFavoritesByPlaceId(place?.place_id.toString())
                    detailsPlaceViewModel.Isfavorites.value = false
                    menuItem!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_icon_heart_outline)
                }else{
                    detailsPlaceViewModel.insertFavorites(place!!)
                    detailsPlaceViewModel.Isfavorites.value = true
                    menuItem!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_icon_heart)
                }

            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }


}