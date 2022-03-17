package com.example.minutos.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minutos.models.Places
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlacesNearbyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(places: Places)

    @Query("SELECT * FROM Places")
    fun getFavorites(): Flow<List<Places>?>

    @Query("SELECT * FROM Places WHERE favorite = :favorite")
    suspend fun getFavoriteById(favorite: Int): Places?

    @Query("DELETE FROM Places WHERE favorite = :favorite")
    suspend fun deleteFavoriteById(favorite: Int)

    @Query("SELECT * FROM Places WHERE place_id = :placeId")
    suspend fun getFavoriteByPlaceId(placeId: String): Places?

    @Query("DELETE FROM Places WHERE place_id = :placeId")
    suspend fun deleteFavoriteByPlaceId(placeId: String)

    @Query("DELETE FROM Places")
    suspend fun deleteFavorite()

}