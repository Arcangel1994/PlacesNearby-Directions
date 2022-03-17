package com.example.minutos.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.minutos.data.database.converter.ConverterListString
import com.example.minutos.data.database.dao.FavoritePlacesNearbyDao
import com.example.minutos.models.Places

@Database(entities = [Places::class], version = 1, exportSchema = false)
@TypeConverters(ConverterListString::class)
abstract class PlacesNearbyDatabase: RoomDatabase() {

    abstract fun favoritePlacesNearbyDao(): FavoritePlacesNearbyDao

}