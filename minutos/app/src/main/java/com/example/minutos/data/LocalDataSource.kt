package com.example.minutos.data

import com.example.minutos.data.database.dao.FavoritePlacesNearbyDao
import com.example.minutos.models.Places
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val favoritePlacesNearbyDao: FavoritePlacesNearbyDao) {

    //Favorite
    suspend fun insertFavorite(places: Places) {
        favoritePlacesNearbyDao.insertFavorite(places)
    }

    fun getFavorites(): Flow<List<Places>?> {
        return favoritePlacesNearbyDao.getFavorites()
    }

    suspend fun getFavoriteById(favorite: Int): Places? {
        return favoritePlacesNearbyDao.getFavoriteById(favorite)
    }

    suspend fun deleteFavoriteById(favorite: Int) {
        favoritePlacesNearbyDao.deleteFavoriteById(favorite)
    }

    suspend fun getFavoriteByPlaceId(placeId: String): Places? {
        return favoritePlacesNearbyDao.getFavoriteByPlaceId(placeId)
    }

    suspend fun deleteFavoriteByPlaceId(PlaceId: String) {
        favoritePlacesNearbyDao.deleteFavoriteByPlaceId(PlaceId)
    }

    suspend fun deleteFavorite() {
        favoritePlacesNearbyDao.deleteFavorite()
    }

}