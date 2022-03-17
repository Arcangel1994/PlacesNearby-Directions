package com.example.minutos.data.network

import com.example.minutos.models.Directions
import com.example.minutos.models.Results
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesNearbyAPI {

    @GET("place/nearbysearch/json")
    suspend fun getPlaceNearby(@Query("location") location: String,
                               @Query("radius") radius: String,
                               @Query("type") type: String,
                               @Query("key") key: String): Response<Results?>

    @GET("directions/json")
    suspend fun getPlaceDirections(@Query("sensor") sensor: Boolean,
                               @Query("mode") mode: String,
                               @Query("alternatives") alternatives: Boolean,
                               @Query("key") key: String,
                                   @Query("origin") origin: String,
                                   @Query("destination") destination: String): Response<Directions?>

}