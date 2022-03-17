package com.example.minutos.data

import com.example.minutos.data.network.PlacesNearbyAPI
import com.example.minutos.models.Directions
import com.example.minutos.models.Results
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val placesNearbyAPI: PlacesNearbyAPI) {

    //Place Nearby
    suspend fun getPlaceNearby(location: String = "", radius: String = "", type: String = "", key: String = ""): Response<Results?> {
        return placesNearbyAPI.getPlaceNearby(location,radius,type, key)
    }

    //Place Directions
    suspend fun getPlaceDirections(sensor: Boolean = false, mode: String = "DRIVING", alternatives: Boolean = true, key: String ="", origin: String = "", destination: String = ""): Response<Directions?>{
        return placesNearbyAPI.getPlaceDirections(sensor, mode, alternatives, key, origin, destination)
    }

}